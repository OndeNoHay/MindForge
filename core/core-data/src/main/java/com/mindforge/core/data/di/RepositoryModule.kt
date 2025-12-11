package com.mindforge.core.data.di

import com.mindforge.core.data.repository.GameSessionRepositoryImpl
import com.mindforge.core.data.repository.UserRepositoryImpl
import com.mindforge.core.domain.repository.GameSessionRepository
import com.mindforge.core.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        userRepositoryImpl: UserRepositoryImpl
    ): UserRepository

    @Binds
    @Singleton
    abstract fun bindGameSessionRepository(
        gameSessionRepositoryImpl: GameSessionRepositoryImpl
    ): GameSessionRepository
}
