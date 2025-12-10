package com.mindforge.core.data.di

import android.content.Context
import androidx.room.Room
import com.mindforge.core.common.Constants
import com.mindforge.core.data.local.MindForgeDatabase
import com.mindforge.core.data.local.dao.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideMindForgeDatabase(
        @ApplicationContext context: Context
    ): MindForgeDatabase {
        return Room.databaseBuilder(
            context,
            MindForgeDatabase::class.java,
            Constants.DATABASE_NAME
        ).build()
    }

    @Provides
    @Singleton
    fun provideUserDao(database: MindForgeDatabase): UserDao {
        return database.userDao()
    }
}
