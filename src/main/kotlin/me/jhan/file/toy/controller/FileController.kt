package me.jhan.file.toy.controller

import org.springframework.web.bind.annotation.*

@RestController
class FileController {

    @GetMapping("/list")
    fun getFileList(@RequestParam path: String, @RequestParam userId: String) : List<String> {
        println("testr")
        return listOf("a", "b", "c")
    }
}