package com.example.reactive_redis.config

import io.swagger.v3.oas.models.OpenAPI
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class OpenApiConfig {
    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    @Bean
    fun customOpenAPI(): OpenAPI {
        val openAPI = OpenAPI()
        return openAPI
    }
}