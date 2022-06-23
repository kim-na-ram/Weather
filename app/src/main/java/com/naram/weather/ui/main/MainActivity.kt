package com.naram.weather.ui.main

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
import io.reactivex.rxjava3.disposables.CompositeDisposable

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    companion object {
        private val TAG = MainActivity::class.java.name
    }

    private lateinit var binding: ActivityMainBinding
    private val vm: MainViewModel by viewModels()

    // permission
    private lateinit var permissionResultLauncher: ActivityResultLauncher<Array<String>>

    private lateinit var compositeDisposable: CompositeDisposable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initView()
        initObserver()
        initResultLauncher()

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.setVariable(BR.model, vm)

    }

    private fun initView() {
        window.statusBarColor = Color.TRANSPARENT
    }

    private fun initObserver() {
        compositeDisposable = CompositeDisposable()
        compositeDisposable.add(
            RxEventBus.listen<Boolean>(Event.PERMISSION_CHECK).subscribe { isPermissionCheck ->
                if(isPermissionCheck) {
                    initResultLauncher()
                }
            }
        )
    }

    private fun initResultLauncher() {
        permissionResultLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                permissions.entries.forEach {
                    val permissionName = it.key
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

        permissionResultLauncher.launch(
            arrayOf(
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            )
        )
    }

}