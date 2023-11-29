package com.example.jpadeletedemo

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@SpringBootApplication
@EnableJpaRepositories
class JpaDeleteDemoApplication

fun main(args: Array<String>) {
    runApplication<JpaDeleteDemoApplication>(*args)
}
