package com.studywise.ai.di

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.studywise.ai.BuildConfig
import com.studywise.ai.data.remote.api.MistralApiService
import com.studywise.ai.data.remote.api.OpenAIService
import com.studywise.ai.data.remote.api.StudyWiseApiService
import com.studywise.ai.data.remote.interceptor.AuthTokenInterceptor
import com.studywise.ai.data.remote.interceptor.TokenAuthenticator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    
    @Provides
    @Singleton
    fun provideGson(): Gson = GsonBuilder()
        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        .create()
    
    @Provides
    @Singleton
    fun provideOkHttpClient(
        authTokenInterceptor: AuthTokenInterceptor,
        tokenAuthenticator: TokenAuthenticator
    ): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
        
        return OkHttpClient.Builder()
            .addInterceptor(authTokenInterceptor)
            .authenticator(tokenAuthenticator)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }
    
    @Provides
    @Singleton
    @Named("OpenAIRetrofit")
    fun provideOpenAIRetrofit(
        okHttpClient: OkHttpClient,
        gson: Gson
    ): Retrofit = Retrofit.Builder()
        .baseUrl("https://api.openai.com/")
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()
    
    @Provides
    @Singleton
    fun provideOpenAIService(
        @Named("OpenAIRetrofit") retrofit: Retrofit
    ): OpenAIService = retrofit.create(OpenAIService::class.java)
    
    @Provides
    @Singleton
    @Named("OpenAIApiKey")
    fun provideOpenAIApiKey(): String {
        // For MVP, this should be stored securely
        // In production, use secure storage or environment variables
        return BuildConfig.OPENAI_API_KEY
    }
    
    @Provides
    @Singleton
    @Named("MistralRetrofit")
    fun provideMistralRetrofit(
        okHttpClient: OkHttpClient,
        gson: Gson
    ): Retrofit = Retrofit.Builder()
        .baseUrl("https://api.mistral.ai/")
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()
    
    @Provides
    @Singleton
    fun provideMistralApiService(
        @Named("MistralRetrofit") retrofit: Retrofit
    ): MistralApiService = retrofit.create(MistralApiService::class.java)
    
    @Provides
    @Singleton
    @Named("MistralApiKey")
    fun provideMistralApiKey(): String {
        return BuildConfig.MISTRAL_API_KEY
    }
    
    @Provides
    @Singleton
    @Named("StudyWiseRetrofit")
    fun provideStudyWiseRetrofit(
        okHttpClient: OkHttpClient,
        gson: Gson
    ): Retrofit = Retrofit.Builder()
        .baseUrl("https://api.studywise.ai/") // Replace with actual backend URL
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()
    
    @Provides
    @Singleton
    fun provideStudyWiseApiService(
        @Named("StudyWiseRetrofit") retrofit: Retrofit
    ): StudyWiseApiService = retrofit.create(StudyWiseApiService::class.java)
}