package com.naram.weather.data.api

import com.naram.weather.data.model.Weather
import com.naram.weather.util.WeatherAPI
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("getVilageFcst?dataType=JSON&serviceKey=${WeatherAPI}")
    suspend fun getWeather (
        @Query("numOfRows") numOfRows: Int,
        @Query("pageNo") pageNo: Int,
        @Query("base_date") base_date: Int,
        @Query("base_time") base_time: String,
        @Query("nx") nx: Int,
        @Query("ny") ny: Int
    ) : Weather
}