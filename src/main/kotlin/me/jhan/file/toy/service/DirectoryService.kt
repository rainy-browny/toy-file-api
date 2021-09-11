package me.jhan.file.toy.service

import me.jhan.file.toy.model.DirectoryModel
import me.jhan.file.toy.model.FileModel
import me.jhan.file.toy.repository.DirectoryRepository
import me.jhan.file.toy.util.PathUtil
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.kotlin.core.util.function.component1
import reactor.kotlin.core.util.function.component2

@Service
class DirectoryService(
    private val directoryRepository: DirectoryRepository,
    private val userService: UserService
) {
    fun deleteDirectory(path: String): Mono<DirectoryModel> {
        val userId = userService.getUserId()
        val fullPath = "$userId/$path"
        val (parentPath, pathName) = PathUtil.splitFile(fullPath)

        val delDirMono = directoryRepository.findByDirectoryFullPath(fullPath)
        val parentDelDirMono = directoryRepository.findByDirectoryFullPath(parentPath)
        return Mono.zip(delDirMono, parentDelDirMono)
            .flatMap {
                val (delDir, parentDelDir) = it

                if (delDir.fileList.isNotEmpty() || delDir.subDirectory.isNotEmpty()) {
                    throw IllegalStateException("내부 파일/폴더를 모두 지우고 파일을 삭제해주세요.");
                }

                parentDelDir.subDirectory.remove(pathName)
                return@flatMap Mono.zip(directoryRepository.save(parentDelDir), directoryRepository.delete(delDir))
                    .map { delDir }
            }

    }
    fun createDirectory(path: String): Mono<DirectoryModel> {
        val userId = userService.getUserId()
        val fullPath = "$userId/$path"

        return directoryRepository.findByDirectoryFullPath(fullPath)
            .switchIfEmpty(Mono.defer {
                var appenderDirectory = userId
                var parentsDirectory = getOrCreateUnitDirectory(userId, userId, null)
                val directoryList =  PathUtil.splitSubDirectory(path)
                for (directory: String in directoryList) {
                    val dirName = directory
                    val subFullPath = "$appenderDirectory/$dirName"
                    appenderDirectory = subFullPath

                    parentsDirectory = parentsDirectory.flatMap {
                        getOrCreateUnitDirectory(dirName, subFullPath, it)
                    }
                }

                return@defer parentsDirectory
            })
    }

    fun getDirectory(path:String): Mono<DirectoryModel> {
        val userId = userService.getUserId()
        val fullPath = PathUtil.getFullPath(userId, path)

        return directoryRepository.findByDirectoryFullPath(fullPath)
    }

    private fun getOrCreateUnitDirectory(name: String, fullPath: String, parentDirectoryModel:DirectoryModel?) : Mono<DirectoryModel> {
        return directoryRepository.findByDirectoryFullPath(fullPath)
            .switchIfEmpty(Mono.defer {
                return@defer directoryRepository.save(DirectoryModel(name, fullPath))
                    .flatMap {
                        val createdDirModel = it
                        if (parentDirectoryModel?.id != null && it?.id != null) {
                            parentDirectoryModel.subDirectory.set(name, it.id!!)
                            return@flatMap directoryRepository.save(parentDirectoryModel).map { createdDirModel }
                        } else {
                            return@flatMap Mono.just(createdDirModel)
                        }
                    }
            })
    }
}