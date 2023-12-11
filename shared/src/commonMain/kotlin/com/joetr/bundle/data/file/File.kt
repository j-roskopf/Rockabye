package com.joetr.bundle.data.file

import com.joetr.bundle.data.model.Gender

expect interface File {
    suspend fun readFileInChunks(range: IntRange, gender: Gender, startsWith: String, maxLength: Int): List<String>
}
