package me.jhan.file.toy.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id

class UploadSessionModel(
    val owner: String,
    val filePath: String,
    val chunkList: ArrayList<UploadChunk> = arrayListOf(),
) {
    @JsonIgnore
    @Id
    var id: ObjectId? = null

    @JsonProperty("id")
    fun _getId() = id?.toString()
}

data class UploadChunk(
    val chunkNumber: Long,
    val filePath: String,
);