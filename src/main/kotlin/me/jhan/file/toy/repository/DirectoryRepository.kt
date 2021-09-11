package me.jhan.file.toy.repository

import me.jhan.file.toy.model.DirectoryModel
import org.bson.types.ObjectId
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Mono

interface DirectoryRepository: ReactiveCrudRepository<DirectoryModel, ObjectId>{
    fun findByDirectoryFullPath(name: String): Mono<DirectoryModel>
}