package com.joetr.bundle.data.file

import com.joetr.bundle.data.model.Gender

actual interface File {
    actual suspend fun readFileInChunks(
        range: IntRange,
        gender: Gender,
        startsWith: String,
        maxLength: Int,
    ): List<String>
}

class FileImpl : File {
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

    fun parseLargeFile(file: String): List<String> {
        val chunkSize = 4096
        val inputStream = this::class.java.classLoader?.getResourceAsStream(file)
        val list = mutableListOf<String>()

        inputStream?.let { inputStream ->
            inputStream.buffered(chunkSize).use { bufferedInputStream ->
                val buffer = ByteArray(chunkSize)
                var bytesRead: Int
                var leftover = ""

                while (bufferedInputStream.read(buffer).also { bytesRead = it } != -1) {
                    val chunk = leftover + String(buffer, 0, bytesRead)

                    // Find the last newline character in the chunk
                    val lastNewline = chunk.lastIndexOf('\n')
                    if (lastNewline != -1) {
                        // Process the complete lines
                        processChunk(
                            chunk.substring(0, lastNewline + 1),
                            list,
                        )

                        // Save the incomplete line for the next chunk
                        leftover =
                            if (lastNewline + 1 < chunk.length) chunk.substring(lastNewline + 1) else ""
                    } else {
                        leftover += chunk
                    }
                }

                // Process the remaining data
                if (leftover.isNotEmpty()) {
                    processChunk(leftover, list)
                }
            }
        }

        return list
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
// write some tests to benchmark speed
// https://github.com/CodeHavenX/MonoRepo/blob/main/runasimi/mpp-lib/src/androidMain/kotlin/com/cramsan/runasimi/mpplib/AndroidFileReader.kt#L3
