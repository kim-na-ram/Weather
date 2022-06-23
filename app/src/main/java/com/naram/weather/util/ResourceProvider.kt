package com.naram.weather.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.*
import android.os.Bundle
import android.util.Log
import com.naram.weather.R
import com.naram.weather.ui.main.Point
import dagger.hilt.android.qualifiers.ApplicationContext
import jxl.Workbook
import jxl.read.biff.BiffException
import java.io.IOException
import java.util.*
import javax.inject.Inject

class ResourceProvider @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        private val TAG = ResourceProvider::class.java.name
    }

    /**
     * 권한 체크 후 true/false 반환
     */
    fun permissionCheck(): Boolean {
        if (context.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            && context.checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }

    /**
     * 사용자 위치 정보
     */
    fun getLocation(): Point? {

        var currentLocation: Location? = null

        val locationManager =
            context.applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        val hasGps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val hasNetwork = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        val gpsLocationListener: LocationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                currentLocation = location
            }

            override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }

        val networkLocationListener: LocationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                currentLocation = location
            }

            override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }

        if (hasGps) {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                5000,
                0F,
                gpsLocationListener
            )
        }

        val lastKnownLocationByGps =
            locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        lastKnownLocationByGps?.let {
            currentLocation = it
        }

        if (hasNetwork) {
            locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                5000,
                0F,
                networkLocationListener
            )
        }

        val geocoder = Geocoder(context, Locale.getDefault())
        var addresses: List<Address>? = null

        try {
            currentLocation?.let {
                addresses = geocoder.getFromLocation(
                    it.latitude,
                    it.longitude,
                    7
                )
            }
        } catch (e: IOException) {
            Log.d(TAG, "getLocation() - GeoCoder Service 사용 불가")
        }

        addresses?.let {
            val address = it[0]
            Log.d(TAG, "getLocation() - address($address)")
            return Point(address.adminArea, address.latitude.toInt(), address.longitude.toInt())
        } ?: run {
            Log.d(TAG, "getLocation() - addresses is Null, 주소 미발견")
        }

        return null

    }

    /**
     * 사용자가 검색한 도시의 위치
     */
    fun getCityPoint(city: String): Point {
        try {
            val inst = context.resources.assets.open("Location.xls")
            val wb = Workbook.getWorkbook(inst)

            if (wb != null) {
                val sheet = wb.getSheet(0) // 시트 불러오기
                if (sheet != null) {
                    var start = 0 // row 인덱스 시작
                    var end = sheet.rows - 1
                    var mid = 0

                    while (start <= end) {
                        mid = (start + end) / 2

                        val cell2 = sheet.getCell(2, mid).contents
                        val cell3 = sheet.getCell(3, mid).contents

                        val cell = if (majorCityList.contains(city)) cell2 else cell3

                        when {
                            cell.contains(city) -> {
                                val nx = sheet.getCell(5, mid).contents.toInt()
                                val ny = sheet.getCell(6, mid).contents.toInt()

                                return Point(city, nx, ny)
                            }
                            cell > city -> {
                                end = mid - 1
                            }
                            cell < city -> {
                                start = mid + 1
                            }
                        }
                    }

                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: BiffException) {
            e.printStackTrace()
        }

        return Point(getString(R.string.error), 0, 0)
    }

    /**
     * string.xml 변수 가져오기
     */
    fun getString(resId: Int) = context.getString(resId)
}