package com.example.concentricirclejoinerview

import android.view.View
import android.view.MotionEvent
import android.app.Activity
import android.content.Context
import android.graphics.Paint
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.RectF

val parts : Int = 3
val scGap : Float = 0.02f / parts
val strokeFactor : Float = 90f
val r1Factor : Float = 3.9f
val r2Factor : Float = 6.9f
val delay : Long = 20
val colors : Array<Int> = arrayOf(
    "#F44336",
    "#795548",
    "#4CAF50",
    "#FF9800",
    ""
).map {
    Color.parseColor(it)
}.toTypedArray()