package com.example.reactive_redis.config

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializer

@Configuration
class RedisConfig(
    @Value("\${spring.redis.host:localhost}") private val redisHost: String,
    @Value("\${spring.redis.port:6379}") private val redisPort: Int
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    /**
     * Creates a connection factory to Redis.
     */
    @Bean
    fun reactiveRedisConnectionFactory(): ReactiveRedisConnectionFactory {
        val config = RedisStandaloneConfiguration(redisHost, redisPort)
        val lettuceConnectionFactory = LettuceConnectionFactory(config)
        lettuceConnectionFactory.afterPropertiesSet()
        return lettuceConnectionFactory
    }

    /**
     * Configures the ReactiveRedisTemplate with appropriate serializers.
     */
    @Bean
    fun reactiveRedisTemplate(
        reactiveRedisConnectionFactory: ReactiveRedisConnectionFactory
    ): ReactiveRedisTemplate<String, ByteArray> {
        val serializationContext = RedisSerializationContext
            .newSerializationContext<String, ByteArray>(StringRedisSerializer())
            .key(StringRedisSerializer())
            .hashKey(StringRedisSerializer())
            .hashValue(ByteArraySerializer())
            .value(ByteArraySerializer())
            .build()

        return ReactiveRedisTemplate(reactiveRedisConnectionFactory, serializationContext)
    }

    /**
     * Custom serializer for ByteArray.
     */
    class ByteArraySerializer : RedisSerializer<ByteArray> {
        override fun deserialize(bytes: ByteArray?): ByteArray? = bytes
        override fun serialize(t: ByteArray?): ByteArray? = t
    }
}
