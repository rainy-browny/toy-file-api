package me.jhan.file.toy.controller

import me.jhan.file.toy.model.DirectoryModel
import me.jhan.file.toy.service.DirectoryService
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
class DirectoryController(
    private val directoryService: DirectoryService
) {

    @PostMapping("/directory")
    fun createDirectory(@RequestParam path:String): Mono<DirectoryModel> {
        return directoryService.createDirectory(path)
    }

    @DeleteMapping("/directory")
    fun deleteDirectory(@RequestParam path:String): Mono<DirectoryModel> {
        return directoryService.deleteDirectory(path)
    }

    @GetMapping("/directory")
    fun getDirectory(@RequestParam path:String): Mono<DirectoryModel> {
        return directoryService.getDirectory(path)
    }
}