package me.jhan.file.toy.util

object PathUtil {
    fun getFullPath(userId: String, path: String): String {
        val fullPath = "$userId/$path"
        val splitPath = fullPath.split("/").filterNot { it.isBlank() }

        return splitPath.joinToString("/")
    }

    fun splitFile(path: String): Pair<String, String> {
        val splitPath = path.split("/").filterNot { it.isBlank() }
        if (splitPath.isEmpty()) {
            throw IllegalArgumentException("올바르지 않은 path입니다. : $path")
        }
        val lastPath = splitPath.last()
        val exceptLastPath = splitPath.dropLast(1).joinToString("/")
        return Pair(exceptLastPath, lastPath)
    }

    fun splitSubDirectory(path: String): List<String> {
        return path.split("/").filterNot{ it.isBlank() }
    }
}