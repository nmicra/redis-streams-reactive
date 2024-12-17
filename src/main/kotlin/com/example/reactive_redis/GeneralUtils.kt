package com.example.reactive_redis

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import reactor.core.Disposable
import reactor.core.publisher.Flux

fun <T> Flux<T>.toFlow(): Flow<T> = callbackFlow {
    val subscription: Disposable = this@toFlow.subscribe(
        { value -> trySend(value).isSuccess },
        { error -> close(error) },
        { close() }
    )
    awaitClose { subscription.dispose() }
}