package me.jhan.file.toy.util

import reactor.core.publisher.Mono

fun <T> notExistsMono(objectName: String) : Mono<T> = Mono.error {
    IllegalArgumentException("${objectName}이(가) 존재하지 않습니다.")
}

fun <T> brokenStableDBMono(objectName: String, objectKey: Any) : Mono<T> = Mono.error {
    IllegalStateException("${objectName} 모델의 DB 오류가 있습니다.. key: ${objectKey}")
}

