package me.jhan.file.toy.repository

import me.jhan.file.toy.model.DirectoryModel
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class DirectoryModelTest(
    @Autowired private val directoryRepository: DirectoryRepository
    ){

    @Test
    fun t() {
        val directoryModel =  DirectoryModel("abcef", "jeonghan/abcef")
        directoryRepository.save(directoryModel)

        println(directoryModel)
    }

    @Test
    fun t2() {
        directoryRepository.findByDirectoryFullPath("jeonghan/abcef")
            .subscribe { println(it) }
    }
}