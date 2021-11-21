package me.jhan.file.toy.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id

data class DirectoryUnitModel(
    val directoryName: String
)

class DirectoryModel(
    directoryName: String,
    directoryFullPath: String,
    subDirectory: MutableMap<String, ObjectId> = hashMapOf(),
    fileList: MutableMap<String, FileModel> = hashMapOf()
) {
    @JsonIgnore @Id var id: ObjectId? = null
    val owner: String
        get() = directoryFullPath.split("/").getOrElse(0) { "" }

    val directoryName: String
        get() = if (field == directoryFullPath) "" else field
    @JsonIgnore val directoryFullPath: String
    
    @JsonIgnore val subDirectory: MutableMap<String, ObjectId>
    @JsonIgnore val fileList: MutableMap<String, FileModel>
    
    @JsonProperty("directoryFullPath") fun _getDirectoryFullPath(): String = 
        directoryFullPath.removePrefix(owner).ifBlank { "/" }
    @JsonProperty("subDirectory") fun getSubDirectory(): List<DirectoryUnitModel> = subDirectory
        .map { it.key }
        .map { DirectoryUnitModel(it) }
    @JsonProperty("fileList") fun getFileList(): List<FileUnitModel> = fileList
        .map { convertFileUnitModel(it.key, it.value)}
    
    init {
        this.directoryName = directoryName
        this.directoryFullPath = directoryFullPath
        this.subDirectory = subDirectory
        this.fileList = fileList
    }
    
    
}
