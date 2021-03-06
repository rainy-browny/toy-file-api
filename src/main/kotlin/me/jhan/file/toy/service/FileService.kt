package me.jhan.file.toy.service

import me.jhan.file.toy.model.FileInfoModel
import me.jhan.file.toy.model.FileModel
import me.jhan.file.toy.repository.DirectoryRepository
import me.jhan.file.toy.util.PathUtil
import me.jhan.file.toy.util.notExistsMono
import org.apache.commons.lang3.RandomStringUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.FileSystemResource
import org.springframework.http.codec.multipart.FilePart
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.io.File
import java.nio.file.Path
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


    fun getFileInfo(path: String): Mono<FileInfoModel> {
        val (dirPath, fileName) = PathUtil.splitFile(path)
        val userId = userService.getUserId()
        val fullPath = "${userId}/${dirPath}"

        return directoryRepository.findByDirectoryFullPath(fullPath)
            .switchIfEmpty(notExistsMono("파일"))
            .map { it.fileList }
            .map { it[fileName] }
            .map { checkNotNull(it) }
            .map { FileInfoModel(fileName, it) }
    }

    fun getFile(path: String): Mono<FileSystemResource> {
        val (dirPath, fileName) = PathUtil.splitFile(path)
        val userId = userService.getUserId()
        val fullPath = "${userId}/${dirPath}"

        return directoryRepository.findByDirectoryFullPath(fullPath)
            .switchIfEmpty(notExistsMono("디렉토리"))
            .map { it.fileList }
            .map { it[fileName] }
            .map { checkNotNull(it) }
            .map { FileSystemResource(it.storagePath) }
    }

    fun moveFile(oldPath: String, newPath: String): Mono<FileModel> {
        val (oldDirPath, oldFileName) = PathUtil.splitFile(oldPath);
        val (newDirPath, newFileName) = PathUtil.splitFile(newPath);

        val userId = userService.getUserId();
        val oldFullPath = "${userId}/${oldDirPath}";
        val newFullPath = "${userId}/${newDirPath}"

        val oldDirectory = directoryRepository.findByDirectoryFullPath(oldFullPath)
            .switchIfEmpty(notExistsMono("기존 디렉토리"))
        val newDirectory = directoryRepository.findByDirectoryFullPath(newFullPath)
            .switchIfEmpty(notExistsMono("대상 디렉토리"))
        return Mono.zip(oldDirectory, newDirectory)
            .flatMap {
                if (!it.t1.fileList.containsKey(oldFileName)) {
                    Mono.error(IllegalArgumentException("이전 파일이 존재하지 않습니다. : ${oldPath}"))
                } else if (it.t2.fileList.containsKey(newFileName)) {
                    Mono.error(IllegalArgumentException("이동할 대상 파일이 이미 존재 합니다.. : ${newPath}"))
                } else {
                    Mono.just(it)
                }
            }
            .flatMap {
                val oldFileList = it.t1.fileList;
                val newFileList = it.t2.fileList;
                val fileModel = it.t1.fileList[oldFileName]!!;

                oldFileList.remove(oldFileName);
                newFileList.put(newFileName, fileModel);

                Mono.zip(directoryRepository.save(it.t1), directoryRepository.save(it.t2))
                    .onErrorMap { IllegalStateException("파일 이동에 실패했습니다. oldPath: ${oldPath}, newPath: ${newPath}", it) }
                    .map { fileModel }
            }
    }

    fun deleteFile(path: String): Mono<Void> {
        val (dirPath, fileName) = PathUtil.splitFile(path)
        val userId = userService.getUserId()
        val fullPath = "${userId}/${dirPath}"

        return directoryRepository.findByDirectoryFullPath(fullPath)
            .switchIfEmpty(notExistsMono("디렉토리"))
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

    fun uploadFile(path: String, filePart: FilePart): Mono<FileModel> {
        return uploadFile(path) {
            filePart.transferTo(it);
        };
    }

    fun <T> uploadFile(path: String, saveFunction: (Path) -> Mono<T>): Mono<FileModel> {
        val (dirPath, fileName) = PathUtil.splitFile(path)
        val userId = userService.getUserId()
        val fullPath = "${userId}/${dirPath}"

        return directoryRepository.findByDirectoryFullPath(fullPath)
            .switchIfEmpty(notExistsMono("삭제할 디렉토리"))
            .flatMap {
                if (it.fileList.containsKey(fileName)) {
                    Mono.error(IllegalArgumentException("다음 파일 경로가 이미 존재합니다. : ${path}"))
                } else {
                    Mono.just(it)
                }
            }
            .flatMap {
                val dirModel = it
                val fileList = it.fileList

                val newFileName = generateStorageFileName()
                val newFileStoragePath = Path(storageRoot, newFileName)
                val newFileModel = FileModel(newFileStoragePath.toString(), LocalDateTime.now())

                fileList[fileName] = newFileModel
                val saveFileStorage = saveFunction(newFileStoragePath);
                val saveFileDB = directoryRepository.save(dirModel)

                Mono.zip(saveFileStorage, saveFileDB)
                    .thenReturn(newFileModel)
            }

    }
}

fun generateStorageFileName(): String = RandomStringUtils.randomAlphanumeric(5) +
        LocalDateTime.now().toEpochSecond(ZoneOffset.UTC) + ".bin"