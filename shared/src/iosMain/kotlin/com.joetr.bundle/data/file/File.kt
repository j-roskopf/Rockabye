package com.joetr.bundle.data.file

import com.joetr.bundle.data.model.Gender
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSBundle
import platform.Foundation.NSData
import platform.Foundation.NSFileHandle
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.closeFile
import platform.Foundation.create
import platform.Foundation.fileHandleForReadingAtPath
import platform.Foundation.readDataOfLength
import platform.posix.memcpy

actual interface File {

    actual suspend fun readFileInChunks(
        range: IntRange,
        gender: Gender,
        startsWith: String,
        maxLength: Int,
    ): List<String>
}

class FileImpl : File {
    @BetaInteropApi
    override suspend fun readFileInChunks(
        range: IntRange,
        gender: Gender,
        startsWith: String,
        maxLength: Int,
    ): List<String> {
        val files = range.map {
            "yob$it.txt"
        }
        return files.map {
            parseLargeFile(it)
        }.flatten()
    }

    @BetaInteropApi
    fun parseLargeFile(file: String): List<String> {
        val chunkSize = 4096
        val list = mutableListOf<String>()

        val path = NSBundle.mainBundle.pathForResource(name = "compose-resources/$file", ofType = null)
            ?: throw Exception("File not found in bundle: $file")

        val fileHandle = NSFileHandle.fileHandleForReadingAtPath(path)
            ?: throw Exception("Unable to open file at path: $path")

        var data: NSData? = fileHandle?.readDataOfLength(4098.toULong())
        var leftover = ""

        while (data != null && data.length > 0u) {
            data.let {
                val chunk = NSString.create(data = it, encoding = NSUTF8StringEncoding)
                val chunkAsString = leftover + (chunk as String)

                // Find the last newline character in the chunk
                val lastNewline = chunkAsString.lastIndexOf('\n')
                if (lastNewline != -1) {
                    // Process the complete lines
                    processChunk(
                        chunkAsString.substring(0, lastNewline + 1),
                        list,
                    )

                    // Save the incomplete line for the next chunk
                    leftover =
                        if (lastNewline + 1 < chunkAsString.length) chunkAsString.substring(lastNewline + 1) else ""
                } else {
                    leftover += chunkAsString
                }
            }
            data = fileHandle.readDataOfLength(chunkSize.toULong())
        }

        // Process the remaining data
        if (leftover.isNotEmpty()) {
            processChunk(leftover, list)
        }

        fileHandle?.closeFile()

        return list
    }

    @OptIn(ExperimentalForeignApi::class)
    public fun NSData.toByteArray(): ByteArray = ByteArray(this@toByteArray.length.toInt()).apply {
        usePinned {
            memcpy(it.addressOf(0), this@toByteArray.bytes, this@toByteArray.length)
        }
    }

    private fun processChunk(
        chunk: String,
        list: MutableList<String>,
    ) {
        // Split the chunk by newline and process each line
        val lines = chunk.split('\n')
        lines.forEach { line ->
            // Process each line (e.g., split by comma and handle data)
            val parts = line.split(',')
            if (parts.size == 5) {
                list.add(line)
            }
        }
    }
}
