package me.jhan.file.toy

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories

@SpringBootApplication
@EnableReactiveMongoRepositories
class ApiApplication

fun main(args: Array<String>) {
    runApplication<ApiApplication>(*args)
}
