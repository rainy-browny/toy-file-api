package me.jhan.file.toy.repository

import me.jhan.file.toy.model.UploadSessionModel
import org.bson.types.ObjectId
import org.springframework.data.repository.reactive.ReactiveCrudRepository

interface UploadSessionRepository : ReactiveCrudRepository<UploadSessionModel, ObjectId> {
}