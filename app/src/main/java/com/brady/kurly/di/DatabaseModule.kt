package com.brady.kurly.di

import android.content.Context
import androidx.room.Room
import com.brady.kurly.data.local.KurlyDatabase
import com.brady.kurly.data.local.dao.WishDao
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
    fun provideDatabase(
        @ApplicationContext context: Context
    ): KurlyDatabase {
        return Room.databaseBuilder(
            context,
            KurlyDatabase::class.java,
            "kurly_database"
        ).build()
    }

    @Provides
    @Singleton
    fun provideWishDao(database: KurlyDatabase): WishDao {
        return database.wishDao()
    }
}
