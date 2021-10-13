package me.jhan.file.toy.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id

data class DirectoryUnitModel(
    val directoryName: String
)

class DirectoryModel(
    @JsonIgnore val directoryName: String,
    @JsonIgnore val directoryFullPath: String,
    @JsonIgnore val subDirectory: MutableMap<String, ObjectId> = hashMapOf(),
    @JsonIgnore val fileList: MutableMap<String, FileModel> = hashMapOf()
) {
    @JsonIgnore @Id var id: ObjectId? = null
    val owner: String
        get() = directoryFullPath.split("/").getOrElse(0) { "" }

    val _directoryName: String
        @JsonProperty("directoryName") get() =
            if (directoryName == directoryFullPath) "" else directoryName

    val _subDirectory: List<DirectoryUnitModel>
        @JsonProperty("subDirectory") get() =
            subDirectory
                .map { it.key }
                .map { DirectoryUnitModel(it) }
    val _fileList: List<FileUnitModel>
        @JsonProperty("fileList") get() =
            fileList
                .map { convertFileUnitModel(it.key, it.value) }

    val _directoryFullPath: String
        @JsonProperty("directoryFullPath") get() =
            directoryFullPath.removePrefix(owner).ifBlank { "/" }
}