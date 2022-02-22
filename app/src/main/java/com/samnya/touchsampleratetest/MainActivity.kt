package com.samnya.touchsampleratetest

import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.MotionEvent.*
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.common.collect.EvictingQueue
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity(), View.OnGenericMotionListener, View.OnTouchListener {

    var text: TextView? = null
    var history: TextView? = null
    var lastTime: Long = System.currentTimeMillis()
    val avgQueue = EvictingQueue.create<Long>(100)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        text = findViewById(R.id.touch_rate)
        history = findViewById(R.id.touch_history)
        val mainView = findViewById<View>(R.id.main_view)
        mainView.setOnGenericMotionListener(this)
        mainView.setOnTouchListener(this)
    }

    override fun onGenericMotion(v: View, ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_HOVER_MOVE) {
            val x = ev.x
            val y = ev.y

            val toolType = ev.getToolType(0)
            val inputType = when (toolType) {
                TOOL_TYPE_FINGER -> InputType.TOUCH
                TOOL_TYPE_MOUSE -> InputType.MOUSE
                TOOL_TYPE_STYLUS -> InputType.STYLUS
                TOOL_TYPE_ERASER -> InputType.ERASER
                else -> InputType.UNKNOWN
            }


            if (ev.historySize > 0) {
                for (i in 0 .. (ev.historySize - 1)) {
                    updateMotion(inputType, x, y, ev.getHistoricalEventTime(i))
                }
            } else {
                updateMotion(inputType, x, y, System.currentTimeMillis())
            }
        }

        return true
    }

    override fun onTouch(v: View, ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_MOVE) {
            val x = ev.x
            val y = ev.y

            if (ev.historySize > 0) {
                for (i in 0..(ev.historySize - 1)) {
                    updateMotion(InputType.TOUCH, x, y, ev.getHistoricalEventTime(i))
                }
            } else {
                updateMotion(InputType.TOUCH, x, y, System.currentTimeMillis())
            }
        }

        return true
    }

    private fun updateMotion(type: InputType, posX: Float, posY: Float, time: Long) {
        val currentTime = time
        val timePassed = currentTime - lastTime
        if (timePassed > 0) {
            val hertz = 1000 / timePassed
            if (hertz > 0) {
                avgQueue.add(hertz)
            }

            history?.text = avgQueue.take(20).joinToString("\n") { "${it}hz" }
            text?.text = "INPUT: ${type}\n SAMPLE RATE: ${avgQueue.average().roundToInt()}hz\nPOSITION: ${posX.roundToInt()} ${posY.roundToInt()}"
        }
        lastTime = currentTime
    }
}

enum class InputType {
    TOUCH,
    MOUSE,
    STYLUS,
    ERASER,
    UNKNOWN
}