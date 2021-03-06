package me.jhan.file.toy.service

import me.jhan.file.toy.model.FileModel
import me.jhan.file.toy.model.UploadChunk
import me.jhan.file.toy.model.UploadSessionModel
import me.jhan.file.toy.repository.UploadSessionRepository
import me.jhan.file.toy.util.notExistsMono
import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.codec.multipart.FilePart
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.io.File
import java.io.FileReader
import java.io.FileWriter

@Service
class UploadSessionService(
    private val uploadSessionRepository: UploadSessionRepository,
    private val userService: UserService,
    private val fileService: FileService
) {
    @Value("\${fileAPI.storageRoot}")
    private val storageRoot: String = "/"

    private val sessionNotExistsMono = notExistsMono<UploadSessionModel>("업로드 세션")

    fun makeSession(filePath: String): Mono<UploadSessionModel> {
        val userId = userService.getUserId();
        val uploadSession = UploadSessionModel(userId, filePath);
        // TODO check already exists file.
        return uploadSessionRepository.save(uploadSession);
    }

    fun uploadChunk(sessionId: String, chunkNumber: Long, filePart: FilePart): Mono<UploadSessionModel> {
        val tempFile = File.createTempFile("upload", ".dat");
        val userId = userService.getUserId();
        return uploadSessionRepository.findById(ObjectId(sessionId))
            .switchIfEmpty(sessionNotExistsMono)
            .flatMap {
                if (userId != it.owner) {
                    Mono.error(IllegalArgumentException("업로드 대상 유저가 다릅니다."))
                } else {
                    Mono.just(it)
                }
            }
            .flatMap { uploadSessionModel ->
                val fileChunk = UploadChunk(chunkNumber, tempFile.absolutePath);
                uploadSessionModel.chunkList.add(fileChunk);

                filePart.transferTo(tempFile).thenReturn(uploadSessionModel);
            }
            .flatMap { uploadSessionModel ->
                uploadSessionRepository.save(uploadSessionModel);
            }
    }

    fun finishUpload(id: String, totalChunkCount: Int): Mono<FileModel> {
        return uploadSessionRepository.findById(ObjectId(id))
            .switchIfEmpty(sessionNotExistsMono)
            .flatMap { uploadSession ->
                if (uploadSession.chunkList.size != totalChunkCount) {
                    Mono.error(IllegalArgumentException(
                        "전송완료된 파일 청크 갯수(${uploadSession.chunkList.size})와 " +
                                "전달된 파일청크 갯수(${totalChunkCount})가 다릅니다."
                    ))
                } else {
                    Mono.just(uploadSession)
                }
            }
            .flatMap { uploadSession ->
                val fileChunkList = uploadSession.chunkList.sortedBy { it.chunkNumber }

                fileService.uploadFile(uploadSession.filePath) { uploadRealPath ->
                    val targetFile = uploadRealPath.toFile();
                    val targetFileWriter = FileWriter(targetFile, true);

                    Flux.fromIterable(fileChunkList)
                        .map { uploadChunk ->
                            val result = FileReader(uploadChunk.filePath).use { reader ->
                                reader.transferTo(targetFileWriter)
                            }
                            println("toPath : ${targetFile.absolutePath}, fromPath : ${uploadChunk.filePath} , result : ${result}")
                        }
                        .collectList()
                        .doOnNext {
                            targetFileWriter.close()
                        }
                }.map { fileModel ->
                    Pair(uploadSession, fileModel);
                }
            }
            .flatMap { (session, fileModel) ->
                uploadSessionRepository.delete(session).thenReturn(fileModel);
            }
    }
}