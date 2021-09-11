package me.jhan.file.toy.model

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id

class DirectoryModel(
    val directoryName: String,
    val directoryFullPath: String,
    val subDirectory: MutableMap<String, ObjectId> = hashMapOf(),
    val fileList: MutableMap<String, FileModel> = hashMapOf()
) {
    @Id var id: ObjectId? = null
    val owner: String?
        get() = directoryFullPath.split("/").getOrNull(0)

}