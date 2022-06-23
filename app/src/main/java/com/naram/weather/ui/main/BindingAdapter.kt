package com.naram.weather.ui.main

import android.util.Log
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import androidx.databinding.ObservableInt
import com.bumptech.glide.Glide

object BindingAdapter {
    @JvmStatic
    @BindingAdapter(
        value = ["loadImage"],
        requireAll = false
    )
    fun loadImage(imageView: ImageView, resId: ObservableInt) {
        Log.d("BindingAdapter", "resId: $resId")
        Glide.with(imageView.context)
            .load(resId.get())
            .into(imageView)
    }
}