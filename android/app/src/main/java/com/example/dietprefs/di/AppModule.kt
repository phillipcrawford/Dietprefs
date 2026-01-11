package com.example.dietprefs.di

import com.example.dietprefs.network.DietPrefsApiService
import com.example.dietprefs.network.RetrofitClient
import com.example.dietprefs.repository.VendorRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDietPrefsApiService(): DietPrefsApiService {
        return RetrofitClient.apiService
    }

    @Provides
    @Singleton
    fun provideVendorRepository(
        apiService: DietPrefsApiService
    ): VendorRepository {
        return VendorRepository(apiService)
    }
}
