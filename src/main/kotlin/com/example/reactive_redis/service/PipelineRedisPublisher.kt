package com.example.reactive_redis.service

import com.example.reactive_redis.objectMapper
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.stereotype.Service

@Service
class PipelineRedisPublisher @Autowired constructor(
    private val reactiveRedisTemplate: ReactiveRedisTemplate<String, ByteArray>
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    /**
     * Publishes a message to a specified Redis Stream.
     *
     * @param streamName The name of the Redis Stream.
     * @param key The key associated with the message.
     * @param payload The message payload to publish.
     */
    suspend fun forwardToStream(streamName: String, key: String, payload: Any) {
        try {
            // Serialize the payload to JSON bytes
            val messageBytes = objectMapper.writeValueAsBytes(payload)

            // Create a map of fields to publish
            val fields: Map<String, ByteArray> = mapOf(
                "key" to key.toByteArray(),
                "value" to messageBytes
            )

            // Publish the fields to the Redis Stream
            reactiveRedisTemplate.opsForStream<String, ByteArray>()
                .add(streamName, fields)
                .doOnNext { recordId ->
                    logger.info("Message published to stream '$streamName' with id $recordId for key '$key'")
                }
                .awaitFirstOrNull() // Await the completion of the reactive operation
        } catch (e: Exception) {
            logger.error("Failed to publish message to stream '$streamName' for key '$key'", e)
            throw e // Re-throw the exception if needed
        }
    }
}
