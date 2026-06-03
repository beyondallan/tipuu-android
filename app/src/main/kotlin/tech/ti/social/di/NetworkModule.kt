package tech.ti.social.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import tech.ti.social.core.network.KtorClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL = "https://api.ti.social"

    @Provides
    @Singleton
    fun provideHttpClient(): HttpClient = KtorClient.create(BASE_URL)
}