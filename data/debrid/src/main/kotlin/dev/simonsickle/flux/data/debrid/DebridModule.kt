package dev.simonsickle.flux.data.debrid

import retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.simonsickle.flux.core.common.SettingsRepository
import dev.simonsickle.flux.data.debrid.api.RealDebridApi
import dev.simonsickle.flux.domain.repository.DebridRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DebridProviderModule {

    @Provides
    @Singleton
    fun provideRealDebridApi(
        okHttpClient: OkHttpClient,
        json: Json,
        settingsRepository: SettingsRepository
    ): RealDebridApi {
        val authedClient = okHttpClient.newBuilder()
            .addInterceptor { chain ->
                val token = runBlocking { settingsRepository.realDebridToken.first() }
                val request: Request = if (token != null) {
                    chain.request().newBuilder()
                        .addHeader("Authorization", "Bearer $token")
                        .build()
                } else {
                    chain.request()
                }
                chain.proceed(request)
            }
            .build()

        return Retrofit.Builder()
            .baseUrl("https://api.real-debrid.com/rest/1.0/")
            .client(authedClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(RealDebridApi::class.java)
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class DebridBindingModule {

    @Binds
    @Singleton
    abstract fun bindDebridRepository(impl: DebridRepositoryImpl): DebridRepository
}
