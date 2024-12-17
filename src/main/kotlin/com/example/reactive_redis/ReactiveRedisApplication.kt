package com.example.reactive_redis

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ReactiveRedisApplication

fun main(args: Array<String>) {
	runApplication<ReactiveRedisApplication>(*args)
}

data class User(val name: String, val age: Int)

val objectMapper by lazy {
	ObjectMapper().also {
		it.registerKotlinModule()
		it.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
		it.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
		it.registerModule(JavaTimeModule())
	}
}