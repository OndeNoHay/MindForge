package com.mindforge.core.common.extensions

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart

/**
 * Extension functions for Flow
 */

/**
 * Wraps a Flow with a Result type for better error handling
 */
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: Throwable) : Result<Nothing>()
    object Loading : Result<Nothing>()
}

/**
 * Transforms a Flow to emit Result values
 */
fun <T> Flow<T>.asResult(): Flow<Result<T>> {
    return this
        .onStart { emit(Result.Loading) }
        .catch { emit(Result.Error(it)) }
}
