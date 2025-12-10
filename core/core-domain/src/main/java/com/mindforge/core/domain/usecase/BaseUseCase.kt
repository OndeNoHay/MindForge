package com.mindforge.core.domain.usecase

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Base class for use cases with suspending execution
 */
abstract class BaseUseCase<in Params, out Result>(
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    /**
     * Execute the use case
     */
    suspend operator fun invoke(params: Params): Result {
        return withContext(dispatcher) {
            execute(params)
        }
    }

    /**
     * Implement this method with the use case logic
     */
    protected abstract suspend fun execute(params: Params): Result
}

/**
 * Base class for use cases that don't need parameters
 */
abstract class NoParamsUseCase<out Result>(
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    /**
     * Execute the use case
     */
    suspend operator fun invoke(): Result {
        return withContext(dispatcher) {
            execute()
        }
    }

    /**
     * Implement this method with the use case logic
     */
    protected abstract suspend fun execute(): Result
}
