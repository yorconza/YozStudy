package com.example.yozstudy.util

import android.content.Context
import android.content.Intent

object util {
    fun openActivity(context: Context, destinationClass: Class<*>) {
        val intent = Intent(context, destinationClass)
        context.startActivity(intent)
    }
}