package me.jhan.file.toy.service

import me.jhan.file.toy.model.FileInfoModel
import me.jhan.file.toy.model.FileModel
import me.jhan.file.toy.repository.DirectoryRepository
import me.jhan.file.toy.util.PathUtil
import org.apache.commons.lang3.RandomStringUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.FileSystemResource
import org.springframework.http.codec.multipart.FilePart
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.io.File
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlin.io.path.Path

@Service
class FileService(
    private val directoryRepository: DirectoryRepository,
    private val userService: UserService
) {

    @Value("\${fileAPI.storageRoot}")
    private val storageRoot: String = "/"

    fun getFileInfo(path:String): Mono<FileInfoModel> {
        val (dirPath, fileName) = PathUtil.splitFile(path)
        val userId = userService.getUserId()
        val fullPath = "${userId}/${dirPath}"

        return directoryRepository.findByDirectoryFullPath(fullPath)
            .map { it.fileList }
            .map { it[fileName] }
            .map { checkNotNull(it) }
            .map { FileInfoModel(fileName, it) }
    }

    fun getFile(path:String): Mono<FileSystemResource> {
        val (dirPath, fileName) = PathUtil.splitFile(path)
        val userId = userService.getUserId()
        val fullPath = "${userId}/${dirPath}"

        return directoryRepository.findByDirectoryFullPath(fullPath)
            .map { it.fileList }
            .map { it[fileName] }
            .map { checkNotNull(it) }
            .map { FileSystemResource(it.storagePath) }
    }

    fun deleteFile(path:String): Mono<Void> {
        val (dirPath, fileName) = PathUtil.splitFile(path)
        val userId = userService.getUserId()
        val fullPath = "${userId}/${dirPath}"

        return directoryRepository.findByDirectoryFullPath(fullPath)
            .flatMap {
                val dirModel = it
                val fileList = it.fileList
                val file = fileList[fileName]

                fileList.remove(fileName)
                val deleteFile = Mono.fromCallable { File(file?.storagePath!!).delete() }
                val deleteFileDB = directoryRepository.save(dirModel)

                return@flatMap Mono.`when`(deleteFile, deleteFileDB).then()
            }
    }

    fun uploadFile(path:String, filePart: FilePart): Mono<FileModel> {
        val (dirPath, fileName) = PathUtil.splitFile(path)
        val userId = userService.getUserId()
        val fullPath = "${userId}/${dirPath}"

        return directoryRepository.findByDirectoryFullPath(fullPath)
            .flatMap {
                val dirModel = it
                val fileList = it.fileList

                val newFileName = generateStorageFileName()
                val newFileStoragePath = Path(storageRoot, newFileName)
                val newFileModel = FileModel(newFileStoragePath.toString(), LocalDateTime.now())

                fileList[fileName] = newFileModel
                val saveFileStorage = filePart
                    .transferTo(newFileStoragePath)

                val saveFileDB = directoryRepository.save(dirModel)

                return@flatMap Mono
                    .`when`(saveFileStorage, saveFileDB)
                    .map { newFileModel }
            }
    }


    private fun generateStorageFileName(): String {
        return RandomStringUtils.randomAlphanumeric(5) +
                LocalDateTime.now().toEpochSecond(ZoneOffset.UTC) + ".bin"
    }
}