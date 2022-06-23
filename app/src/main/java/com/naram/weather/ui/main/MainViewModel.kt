package com.naram.weather.ui.main

import android.util.Log
import androidx.databinding.ObservableField
import androidx.databinding.ObservableInt
import androidx.lifecycle.ViewModel
import com.naram.weather.R
import com.naram.weather.data.model.Item
import com.naram.weather.util.ResourceProvider
import com.naram.weather.util.baseTimes
import com.naram.weather.util.eventbus.Event
import com.naram.weather.util.eventbus.RxEventBus
import com.naram.weather.data.api.ApiRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.disposables.CompositeDisposable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val apiRepository: ApiRepository,
    private val resourceProvider: ResourceProvider
) : ViewModel() {

    companion object {
        private val TAG = MainViewModel::class.java.name
    }

    private lateinit var compositeDisposable: CompositeDisposable
    val etSearchCity = ObservableField<String>()
    val tvCity = ObservableField<String>()
    val tvTemperature = ObservableField<String>()
    val tvMinTemperature = ObservableField<String>()
    val tvMaxTemperature = ObservableField<String>()
    val tvPrecipitation = ObservableField<String>()
    val resId = ObservableInt(0)

    fun clickSearchButton() {
        etSearchCity.get()?.let {
            val (name, nx, ny) = resourceProvider.getCityPoint(it)

            tvCity.set(name)
            getDate()
            getWeather(nx, ny)
        }
    }

    private var baseDate: Int = 0
    private lateinit var baseTime: String

    init {
        initObserver()
        if(permissionCheck()) {
            getLocation()?.let {
                val (name, nx, ny) = it
                tvCity.set(name)
                getDate()
                getWeather(nx, ny)
            } ?: apply {
                tvCity.set(getString(R.string.error))
            }
        } else {
            RxEventBus.post(Event.PERMISSION_CHECK, true)
        }
    }

    private fun initObserver() {
        compositeDisposable = CompositeDisposable()
        compositeDisposable.add(
            RxEventBus.listen<Boolean>(Event.PERMISSION_GRANTED).subscribe { isPermissionGranted ->
                if(isPermissionGranted) {
                    getLocation()?.let {
                        val (name, nx, ny) = it
                        tvCity.set(name)
                        getDate()
                        getWeather(nx, ny)
                    } ?: apply {
                        tvCity.set(getString(R.string.error))
                    }
                } else {
                    RxEventBus.post(Event.PERMISSION_CHECK, true)
                }
            }
        )
    }

    private fun permissionCheck() = resourceProvider.permissionCheck()

    private fun getLocation() = resourceProvider.getLocation()

    private fun getString(resId: Int) = resourceProvider.getString(resId)

    private fun getDate() {
        val today = Date()

        val dateFormat = SimpleDateFormat("yyyyMMdd")
        val hourFormat = SimpleDateFormat("HH")

        baseDate = dateFormat.format(today).toInt()
        val baseHour = hourFormat.format(today).toInt()
        baseTime = baseHour.toString()

        Log.d(TAG, "baseDate: $baseDate")

        baseTimes.forEach {

            var s1 = if (it[0] == '0') {
                it.substring(1, 2)
            } else {
                it.substring(0, 2)
            }

            Log.d(TAG, "$baseHour, $s1")

            if (baseHour == s1.toInt()) {
                // 둘이 같으면
                baseTime = if (baseHour < 10) "0${baseHour}00" else "${baseHour}00"
                Log.d(TAG, "baseTime: $baseTime")
                return
            } else {
                if ((baseHour == s1.toInt() + 1) || (baseHour == s1.toInt() + 2)) {
                    baseTime = if (s1.toInt() < 10) "0${s1}00" else "${s1}00"
                    Log.d(TAG, "baseTime: $baseTime")
                    return
                }
            }
        }

    }

    private fun getWeather(nx: Int, ny: Int) {
        Log.d(TAG, "nx: ${nx}, ny: $ny")

        CoroutineScope(Dispatchers.Main).launch {
            apiRepository.getWeather(10, 1, baseDate, baseTime, nx, ny).collect {
                Log.d(TAG, it.toString())
                processWeather(it)
            }
        }
    }

    private fun processWeather(item: List<Item>) {
        item.forEach {
            when (it.category) {
                "TMP" -> tvTemperature.set(it.fcstValue)
                "TMX" -> tvMaxTemperature.set(it.fcstValue)
                "TMN" -> tvMinTemperature.set(it.fcstValue)
                "POP" -> tvPrecipitation.set(it.fcstValue)
                "SKY" -> {
                    when (it.fcstValue) {
                        "1" -> resId.set(R.drawable.ic_sunny)
                        "3" -> resId.set(R.drawable.ic_partly_cloudy_day)
                        "4" -> resId.set(R.drawable.ic_clouds_100)
                    }
                }
                "PTY" -> {
                    when (it.fcstValue) {
                        "1" -> resId.set(R.drawable.ic_rain)
                        "3" -> resId.set(R.drawable.ic_snow)
                        "4" -> resId.set(R.drawable.ic_rainfall)
                    }
                }
            }
        }

        if (tvMaxTemperature.get().isNullOrEmpty()) {
        }
        if (tvMinTemperature.get().isNullOrEmpty()) {

        }
    }

}