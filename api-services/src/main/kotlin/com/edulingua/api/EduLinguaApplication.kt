package com.edulingua.api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@SpringBootApplication(scanBasePackages = ["com.edulingua"])
@EnableJpaRepositories(basePackages = ["com.edulingua.core.repository"])
@EntityScan(basePackages = ["com.edulingua.core.domain"])
class EduLinguaApplication

fun main(args: Array<String>) {
    runApplication<EduLinguaApplication>(*args)
}
