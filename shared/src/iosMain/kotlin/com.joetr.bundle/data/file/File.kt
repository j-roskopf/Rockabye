package com.joetr.bundle.data.file

import com.joetr.bundle.data.model.Gender
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.value
import platform.Foundation.NSBundle
import platform.Foundation.NSData
import platform.Foundation.NSError
import platform.Foundation.NSFileHandle
import platform.Foundation.NSString
import platform.Foundation.NSURL
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.closeFile
import platform.Foundation.create
import platform.Foundation.fileHandleForReadingAtPath
import platform.Foundation.readDataOfLength
import platform.Foundation.stringWithContentsOfFile
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

    /**
     *         val chunkSize = 4096
     *         val list = mutableListOf<String>()
     *         val mainBundle = NSBundle.mainBundle()
     *         val path = mainBundle.pathForResource(name = "compose-resources/yob2022", ofType = "txt") ?: throw Exception("File not found: $file")
     *         val text = NSString.stringWithContentsOfFile(path, NSUTF8StringEncoding, null) as String
     *         list.addAll(text.split("\n"))
     */

    @BetaInteropApi
    @OptIn(ExperimentalForeignApi::class)
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

    class FileResource(
        val fileName: String,
        val extension: String,
        val bundle: NSBundle = NSBundle.mainBundle,
    ) {
        open val path: String
            get() = bundle.pathForResource(
                name = fileName,
                ofType = extension,
                inDirectory = "",
            )!!
        open val url: NSURL
            get() = bundle.URLForResource(
                name = fileName,
                withExtension = extension,
                subdirectory = "",
            )!!

        @OptIn(ExperimentalForeignApi::class)
        fun readText(): String {
            val filePath = path
            val (result: String?, error: NSError?) = memScoped {
                val p = alloc<ObjCObjectVar<NSError?>>()
                val result: String? = runCatching {
                    NSString.stringWithContentsOfFile(
                        path = filePath,
                        encoding = NSUTF8StringEncoding,
                        error = p.ptr,
                    )
                }.getOrNull()
                result to p.value
            }

            if (error != null) {
                throw Exception("asgmkslg")
            } else {
                return result!!
            }
        }
    }
}
