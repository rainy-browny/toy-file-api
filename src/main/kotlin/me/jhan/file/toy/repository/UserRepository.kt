package me.jhan.file.toy.repository

import me.jhan.file.toy.model.UserModel
import org.springframework.data.repository.reactive.ReactiveCrudRepository

interface UserRepository : ReactiveCrudRepository<UserModel, String> {
}