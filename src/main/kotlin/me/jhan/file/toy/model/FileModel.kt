package me.jhan.file.toy.model

import java.time.LocalDateTime

data class FileModel(
    val storagePath: String,
    val updateDt:LocalDateTime
)