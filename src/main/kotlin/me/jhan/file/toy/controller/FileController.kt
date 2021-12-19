package me.jhan.file.toy.controller

import io.swagger.annotations.ApiImplicitParam
import io.swagger.annotations.ApiImplicitParams
import me.jhan.file.toy.model.FileInfoModel
import me.jhan.file.toy.model.FileModel
import me.jhan.file.toy.service.FileService
import org.springframework.core.io.FileSystemResource
import org.springframework.http.MediaType
import org.springframework.http.codec.multipart.FilePart
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
class FileController(
    private val fileService: FileService
) {

    @GetMapping("/fileInfo")
    fun getFileInfo(@RequestParam filePath: String): Mono<FileInfoModel> {
        return fileService.getFileInfo(filePath)
    }

    @PostMapping("/file")
    @ApiImplicitParams(
        ApiImplicitParam(name = "filePath", paramType = "query", required = true),
        ApiImplicitParam(name = "file", dataType = "__file", paramType = "form", required = true)
    )
    fun uploadFile(@RequestParam("filePath") filePath: String, @RequestPart("file") file: FilePart): Mono<FileModel> {
        return fileService.uploadFile(filePath, file)
    }

    @GetMapping("/file", produces = [MediaType.APPLICATION_OCTET_STREAM_VALUE])
    fun downloadFile(@RequestParam("filePath") filePath: String): Mono<FileSystemResource> {
        return fileService.getFile(filePath)
    }

    @DeleteMapping("/file")
    fun deleteFile(@RequestParam("filePath") filePath: String): Mono<Void> {
        return fileService.deleteFile(filePath)
    }

    @PutMapping("/file/move")
    fun moveFile(
        @RequestParam("oldFilePath") oldFilePath: String,
        @RequestParam("newFilePath") newFilePath: String
    ): Mono<FileModel> {
        return fileService.moveFile(oldFilePath, newFilePath);
    }
}