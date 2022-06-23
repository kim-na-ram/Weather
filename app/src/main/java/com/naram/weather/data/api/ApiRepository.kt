package com.naram.weather.data.api

import android.util.Log
import com.naram.weather.data.model.Item
import kotlinx.coroutines.flow.*
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class ApiRepository @Inject constructor(private val service: ApiService) {
    companion object {
        private val TAG = ApiRepository::class.java.name
    }

    private val _weather = MutableSharedFlow<List<Item>>(replay = 1)
    val weather = _weather.asSharedFlow()

    suspend fun getWeather(
        numOfRows: Int,
        pageNo: Int,
        base_date: Int,
        base_time: String,
        nx: Int,
        ny: Int
    ): Flow<List<Item>> {
        requestWeatherData(numOfRows, pageNo, base_date, base_time, nx, ny)
        return weather.map { it }
    }

    private suspend fun requestWeatherData(
        numOfRows: Int,
        pageNo: Int,
        base_date: Int,
        base_time: String,
        nx: Int,
        ny: Int
    ): Boolean {

        var successful = try {
            val repo = service.getWeather(numOfRows, pageNo, base_date, base_time, nx, ny)
            if (repo.response.header.resultCode == "00") {
                _weather.emit(repo.response.body!!.items.item)
                Log.d(TAG, "requestWeatherData Success")
                true
            } else {
                Log.d(TAG, "requestWeatherData Failure: ${repo.response.header.resultMsg}")
                false
            }
        } catch (e: HttpException) {
            Log.d(TAG, "requestWeatherData Failure: ${e.printStackTrace()}")
            false
        } catch (e: IOException) {
            Log.d(TAG, "requestWeatherData Failure: ${e.printStackTrace()}")
            false
        }

        return successful

    }
}