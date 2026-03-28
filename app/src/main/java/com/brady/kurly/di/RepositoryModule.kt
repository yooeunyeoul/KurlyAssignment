package com.brady.kurly.di

import com.brady.kurly.data.repository.SectionRepositoryImpl
import com.brady.kurly.data.repository.WishRepositoryImpl
import com.brady.kurly.domain.repository.SectionRepository
import com.brady.kurly.domain.repository.WishRepository
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
    abstract fun bindSectionRepository(
        impl: SectionRepositoryImpl
    ): SectionRepository

    @Binds
    @Singleton
    abstract fun bindWishRepository(
        impl: WishRepositoryImpl
    ): WishRepository
}
