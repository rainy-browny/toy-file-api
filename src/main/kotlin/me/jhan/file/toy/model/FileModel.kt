package me.jhan.file.toy.model

import java.time.LocalDateTime

data class FileModel(
    val storagePath: String,
    val updateDt:LocalDateTime
)

data class FileUnitModel(
    val fileName: String,
    val updateDt:LocalDateTime
) {

}

fun convertFileUnitModel(fileName: String, fileModel: FileModel)= FileUnitModel(fileName, fileModel.updateDt)