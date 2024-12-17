package com.example.reactive_redis.controller

import com.example.reactive_redis.User
import com.example.reactive_redis.service.PipelineRedisPublisher
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class EchoController(
    private val pipelineRedisPublisher: PipelineRedisPublisher
) {

    @GetMapping("/echo/{txt}")
    suspend fun echo(@PathVariable txt: String): String = "$txt !!!"

    @PostMapping("/users")
    suspend fun publishUser(
        @RequestParam name: String,
        @RequestParam age: Int
    ): String {
        val user = User(name, age)
        // Publish to "users" stream
        pipelineRedisPublisher.forwardToStream("users", user.name, user)
        return "User $user published to 'users' stream."
    }
}
