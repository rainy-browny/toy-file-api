package me.jhan.file.toy.controller

import io.swagger.annotations.ApiImplicitParam
import io.swagger.annotations.ApiImplicitParams
import me.jhan.file.toy.model.FileModel
import me.jhan.file.toy.model.UploadSessionModel
import me.jhan.file.toy.service.UploadSessionService
import org.springframework.http.codec.multipart.FilePart
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
class UploadSessionController(
    private val uploadSessionService: UploadSessionService
) {
    @PostMapping("/session")
    fun makeSession(@RequestParam(required = true) filePath: String): Mono<UploadSessionModel> {
        return uploadSessionService.makeSession(filePath);
    }

    @PostMapping("/session/chunkFile")
    @ApiImplicitParams(
        ApiImplicitParam(name = "sessionId", paramType = "query", required = true),
        ApiImplicitParam(name = "chunkNumber", paramType = "query", dataType = "long", required = true),
        ApiImplicitParam(name = "file", dataType = "__file", paramType = "form", required = true)
    )
    fun uploadFile(
        @RequestParam("sessionId") sessionId: String,
        @RequestParam("chunkNumber") chunkNumber: Long,
        @RequestPart("file") file: FilePart
    ): Mono<UploadSessionModel> {
        return uploadSessionService.uploadChunk(sessionId, chunkNumber, file);
    }

    @PostMapping("/session/finish")
    fun finishUpload(
        @RequestParam("sessionId") sessionId: String,
        @RequestParam("totalChunkCount") chunkCount: Int
    ): Mono<FileModel> {
        return uploadSessionService.finishUpload(sessionId, chunkCount);
    }
}