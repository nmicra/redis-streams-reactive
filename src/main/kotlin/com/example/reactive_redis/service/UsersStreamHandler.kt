package com.example.reactive_redis.service

import com.example.reactive_redis.User
import com.example.reactive_redis.objectMapper
import kotlinx.coroutines.*
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.data.redis.connection.stream.*
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration
import kotlin.system.measureTimeMillis

@Component
class UsersStreamHandler(
    private val reactiveRedisTemplate: ReactiveRedisTemplate<String, ByteArray>,
    private val pipelineRedisPublisher: PipelineRedisPublisher
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    private val groupId = "Users-GRP"
    private val inputStreamName = "users"
    private val consumerName = "UsersConsumer"

    @EventListener(ApplicationReadyEvent::class)
    fun startConsuming() {
        CoroutineScope(Dispatchers.IO).launch {
            createConsumerGroupIfNotExists()

            while (isActive) {
                val messages = readMessages()
                if (messages.isNullOrEmpty()) {
                    continue
                }

                for (message in messages) {
                    val elapsed = measureTimeMillis {
                        try {
                            processMessage(message)
                            acknowledgeMessage(message)
                        } catch (e: Exception) {
                            logger.error("Error processing messageId [${message.id}]", e)
                            // Decide whether to acknowledge on error or not.
                        }
                    }
                    logger.info("Processed messageId [${message.id}] in ${elapsed}ms")
                }
            }
        }
    }

    private suspend fun createConsumerGroupIfNotExists() {
        try {
            reactiveRedisTemplate
                .opsForStream<String, ByteArray>() // Note <String, ByteArray>
                .createGroup(inputStreamName, groupId)
                .awaitFirstOrNull()
            logger.info("Consumer group '$groupId' created for stream '$inputStreamName'.")
        } catch (e: Exception) {
            logger.debug("Consumer group '$groupId' may already exist. Continuing...", e)
        }
    }

    private suspend fun readMessages(): List<MapRecord<String, String, ByteArray>>? {
        return reactiveRedisTemplate
            .opsForStream<String, ByteArray>()  // K = String, V = ByteArray
            .read(
                Consumer.from(groupId, consumerName),
                StreamReadOptions.empty().block(Duration.ofSeconds(1)),
                StreamOffset.create(inputStreamName, ReadOffset.from(">"))
            )
            .collectList()
            .awaitFirstOrNull()
    }

    private suspend fun processMessage(message: MapRecord<String, String, ByteArray>) {
        val keyBytes = message.value["key"]
        val payload = message.value["value"]

        if (payload == null) {
            logger.error("Payload is null for messageId [${message.id}], fields: ${message.value.keys}")
            return
        }

        val messageKey = keyBytes?.toString(Charsets.UTF_8) ?: "unknown-key"
        val user = objectMapper.readValue(payload, User::class.java)
        logger.info("Received User from [key=$messageKey]: $user")

        val updatedUser = user.copy(age = user.age * 2)
        pipelineRedisPublisher.forwardToStream("elderly", updatedUser.name, updatedUser)
    }

    private suspend fun acknowledgeMessage(message: MapRecord<String, String, ByteArray>) {
        reactiveRedisTemplate
            .opsForStream<String, ByteArray>()
            .acknowledge(inputStreamName, groupId, message.id)
            .awaitFirstOrNull()
    }
}
