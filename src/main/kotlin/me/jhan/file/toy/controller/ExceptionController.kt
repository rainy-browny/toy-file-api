package me.jhan.file.toy.controller

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ExceptionController {

    @ExceptionHandler(IllegalArgumentException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun wrongArgument(exception: IllegalArgumentException): Map<String, Any> {
        return mapOf<String, Any>(Pair("message", exception.message ?: ""), Pair("code", 400));
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun serverError(exception: Exception): Map<String, Any> {
        return mapOf<String, Any>(Pair("message", exception.message ?: ""), Pair("code", 500));
    }
}