package me.jhan.file.toy.service

import me.jhan.file.toy.model.DirectoryModel
import me.jhan.file.toy.repository.DirectoryRepository
import me.jhan.file.toy.util.PathUtil
import me.jhan.file.toy.util.brokenStableDBMono
import me.jhan.file.toy.util.notExistsMono
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
        val fullPath = PathUtil.getFullPath(userId, path)
        val (parentPath, pathName) = PathUtil.splitFile(fullPath)

        val delDirMono = directoryRepository.findByDirectoryFullPath(fullPath)
        val parentDelDirMono = directoryRepository.findByDirectoryFullPath(parentPath)
        return Mono.zip(delDirMono, parentDelDirMono)
            .switchIfEmpty(notExistsMono("삭제대상 디렉토리"))
            .doOnNext {
                val (delDir, parentDelDir) = it

                if (delDir.fileList.isNotEmpty() || delDir.subDirectory.isNotEmpty()) {
                    throw IllegalArgumentException("내부 파일/폴더를 모두 지우고 파일을 삭제해주세요.");
                }

                parentDelDir.subDirectory.remove(pathName)
            }
            .flatMap {
                val next = it;
                directoryRepository.save(it.t2).map { next }
            }
            .flatMap {
                val next = it;
                directoryRepository.deleteById(it.t1.id!!).map { next }
            }
            .map { it.t1 }
    }

    fun createDirectory(path: String): Mono<DirectoryModel> {
        val userId = userService.getUserId()
        val fullPath = PathUtil.getFullPath(userId, path);

        return directoryRepository.findByDirectoryFullPath(fullPath)
            .switchIfEmpty(Mono.defer {
                var appenderDirectory = userId
                var parentsDirectory = getOrCreateUnitDirectory(userId, userId, null)
                val directoryList = PathUtil.splitSubDirectory(path)
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

    fun getDirectory(path: String): Mono<DirectoryModel> {
        val userId = userService.getUserId()
        val fullPath = PathUtil.getFullPath(userId, path)

        return directoryRepository.findByDirectoryFullPath(fullPath)
            .switchIfEmpty(notExistsMono("디렉토리"))
    }

    fun moveDirectory(oldPath: String, newPath: String): Mono<DirectoryModel> {
        val userId = userService.getUserId();
        val oldFullPath = PathUtil.getFullPath(userId, oldPath);
        val newFullPath = PathUtil.getFullPath(userId, newPath)
        val (oldParentPath, oldDirName) = PathUtil.splitFile(oldFullPath);
        val (newParentPath, newDirName) = PathUtil.splitFile(newFullPath);

        return Mono.zip(
            directoryRepository.findByDirectoryFullPath(oldParentPath)
                .switchIfEmpty(notExistsMono("기존 부모 디렉토리")),
            directoryRepository.findByDirectoryFullPath(newParentPath)
                .switchIfEmpty(notExistsMono("대상 부모 디렉토리")),
        )
            .doOnNext { // 검증
                val (oldParDir, newParDir) = it;

                if (!oldParDir.subDirectory.containsKey(oldDirName)) {
                    throw IllegalArgumentException("기존 디렉토리가 존재하지 않습니다. : ${oldPath}");
                }

                if (newParDir.subDirectory.containsKey(newDirName)) {
                    throw IllegalArgumentException("이동할 대상 디렉토리가 이미 존재합니다.. : ${newPath}");
                }

            }
            .flatMap { // 과거 디렉토리 모델 조회
                val (oldParDir, newParDir) = it;
                val dirModelId = oldParDir.subDirectory[oldDirName]!!;

                directoryRepository.findById(dirModelId)
                    .switchIfEmpty(brokenStableDBMono("서브 디렉토리 모델", dirModelId))
                    .map { targetDir ->
                        Pair(
                            targetDir,
                            DirectoryModel(newDirName, newFullPath, targetDir.subDirectory, targetDir.fileList)
                        )
                    }
                    .map { Pair(Pair(oldParDir, newParDir), it) }
            }
            .doOnNext {
                if (it.second.first.subDirectory.isNotEmpty()) {
                    throw IllegalArgumentException("이동할 디렉토리에는 서브 디렉토리가 없어야 합니다. (추후 개선파트)")
                }
            }
            .flatMap { // 새로운 디렉토리 모델 저장
                val next = it;
                directoryRepository.save(it.second.second).map { next }
            }
            .flatMap {  // 부모디렉토리 수정
                val (parDirSet, targetDirSet) = it;
                val (oldParDir, newParDir) = parDirSet;
                val (oldTargetDir, newTargetDir) = targetDirSet;

                val oldSubDirectory = oldParDir.subDirectory
                val newSubDirectory = newParDir.subDirectory

                oldSubDirectory.remove(oldDirName);

                val isSameParDir = oldParDir.directoryFullPath == newParDir.directoryFullPath;

                val newDirMono = (if (isSameParDir) {
                    oldSubDirectory.set(newDirName, newTargetDir.id!!)
                    Mono.just(it)
                } else {
                    newSubDirectory.set(newDirName, newTargetDir.id!!)
                    directoryRepository.save(newParDir)
                })

                newDirMono.flatMap { directoryRepository.save(oldParDir) }
                    .doOnError {
                        throw IllegalStateException("디렉토리 이동중 부모 디렉토리 모델 처리에 오류가 생겼습니다.", it);
                    }
                    .map {
                        targetDirSet;
                    }
            }
            .flatMap { // 과거 디렉토리 모델 삭제
                val next = it;
                directoryRepository.delete(it.first).map { next.second }
            }
    }

    private fun getOrCreateUnitDirectory(
        name: String,
        fullPath: String,
        parentDirectoryModel: DirectoryModel?
    ): Mono<DirectoryModel> {
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
