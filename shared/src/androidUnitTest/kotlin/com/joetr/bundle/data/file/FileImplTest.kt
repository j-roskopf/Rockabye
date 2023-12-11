package com.joetr.bundle.data.file

import org.junit.Test

class FileImplTest {

    @Test
    fun fileCanBeParsed() {
        val fileImpl = FileImpl()

        val file = fileImpl.parseLargeFile(
            "yob2022.txt",
        )
        assert(file.size == 31915)
    }
}
