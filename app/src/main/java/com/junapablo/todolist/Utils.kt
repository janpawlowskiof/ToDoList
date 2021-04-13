package com.junapablo.todolist

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory

object Utils {
    fun getBitmapFromAsset(context: Context, filePath: String): Bitmap? {
        val istr = context.assets.open(filePath)
        return BitmapFactory.decodeStream(istr)
    }
}