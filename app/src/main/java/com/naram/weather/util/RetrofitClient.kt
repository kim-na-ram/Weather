package com.naram.weather.util

import com.google.gson.GsonBuilder
import com.naram.weather.data.api.ApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RetrofitClient {
    private var instance: Retrofit? = null
    private val gson = GsonBuilder().setLenient().create()

    private fun getInstance(): Retrofit {
        if(instance == null) {
            instance = Retrofit.Builder()
                .baseUrl("http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
        }
        return instance!!
    }

    @Provides
    @Singleton
    fun create(): ApiService {
        return getInstance().create(ApiService::class.java)
    }
}