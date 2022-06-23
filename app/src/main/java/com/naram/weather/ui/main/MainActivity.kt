package com.naram.weather.ui.main

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import com.naram.weather.BR
import com.naram.weather.R
import com.naram.weather.databinding.ActivityMainBinding
import com.naram.weather.util.eventbus.Event
import com.naram.weather.util.eventbus.RxEventBus
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    companion object {
        private val TAG = MainActivity::class.java.name
    }

    private lateinit var binding: ActivityMainBinding
    private val vm: MainViewModel by viewModels()

    // permission
    private lateinit var permissionResultLauncher: ActivityResultLauncher<Array<String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initView()
        initResultLauncher()

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.setVariable(BR.model, vm)

    }

    private fun initView() {
        window.statusBarColor = Color.TRANSPARENT
    }

    private fun initResultLauncher() {
        permissionResultLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                permissions.entries.forEach {
                    val isGranted = it.value
                    if (isGranted) {
                        // Permission is granted
                        RxEventBus.post(Event.PERMISSION_GRANTED, true)
                    } else {
                        // Permission is denied
                        RxEventBus.post(Event.PERMISSION_GRANTED, false)
                    }
                }
            }

        if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            || checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionResultLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            )
        }
    }

}