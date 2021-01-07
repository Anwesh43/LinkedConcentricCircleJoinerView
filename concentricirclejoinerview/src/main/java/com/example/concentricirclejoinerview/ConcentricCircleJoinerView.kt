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
val lines : Int = 4
val backColor : Int = Color.parseColor("#BDBDBD")

fun Int.inverse() : Float = 1f / this
fun Float.maxScale(i : Int, n : Int) : Float = Math.max(0f, this - i * n.inverse())
fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.inverse(), maxScale(i, n)) * n
fun Float.sinify() : Float = Math.sin(this * Math.PI).toFloat()

fun Canvas.drawConcentricCircleJoiner(scale : Float, w : Float, h : Float, paint : Paint) {
    val r1 : Float = Math.min(w, h) / r1Factor
    val r2 : Float = Math.min(w, h) / r2Factor
    val sf : Float = scale.sinify()
    val sf1 : Float = sf.divideScale(0, parts)
    val sf2 : Float = sf.divideScale(1, parts)
    val sf3 : Float = sf.divideScale(2, parts)
    save()
    translate(w / 2, h / 2)
    drawArc(RectF(-r2, -r2, r2, r2), 0f, 360f * sf1, false, paint)
    drawArc(RectF(-r1, -r1, r1, r1), 0f, 360f * sf2, false, paint)
    for (j in 0..(lines - 1)) {
        save()
        rotate((360f / lines) * j)
        drawLine(r1, 0f, r1 + (r2 - r1) * sf3, 0f, paint)
        restore()
    }
    restore()
}

fun Canvas.drawCCJNode(i : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    paint.color = colors[i]
    paint.strokeCap = Paint.Cap.ROUND
    paint.strokeWidth = Math.min(w, h) / strokeFactor
    drawConcentricCircleJoiner(scale, w, h, paint)
}

class ConcentricCircleJoinerView(ctx : Context) : View(ctx) {

    private val renderer : Renderer = Renderer(this)

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var dir : Float = 0f, var prevScale : Float = 0f) {

        fun update(cb : (Float) -> Unit) {
            scale += dir * scGap
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                cb(prevScale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (dir == 0f) {
                dir = 1f - 2 * prevScale
                cb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(delay)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class CCJNode(var i : Int, val state : State = State()) {

        private var next : CCJNode? = null
        private var prev : CCJNode? = null

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < colors.size - 1) {
                next = CCJNode(i + 1)
                next?.prev = this
            }
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawCCJNode(i, state.scale, paint)
        }

        fun update(cb : (Float) -> Unit) {
            state.update(cb)
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : CCJNode {
            var curr : CCJNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class ConcentricCircleJoiner(var i : Int) {

        private var curr : CCJNode = CCJNode(0)
        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            curr.draw(canvas, paint)
        }

        fun update(cb : (Float) -> Unit) {
            curr.update {
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                cb(it)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }
    }

    data class Renderer(var view : ConcentricCircleJoinerView) {

        private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val ccj : ConcentricCircleJoiner = ConcentricCircleJoiner(0)
        private val animator : Animator = Animator(view)

        fun render(canvas : Canvas) {
            canvas.drawColor(backColor)
            ccj.draw(canvas, paint)
            animator.animate {
                ccj.update {
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            ccj.startUpdating {
                animator.start()
            }
        }
    }

    companion object {

        fun create(activity : Activity) : ConcentricCircleJoinerView {
            val view : ConcentricCircleJoinerView = ConcentricCircleJoinerView(activity)
            activity.setContentView(view)
            return view
        }
    }
}