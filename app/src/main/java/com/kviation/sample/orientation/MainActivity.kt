package com.kviation.sample.orientation

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View

class MainActivity : AppCompatActivity(), Orientation.Listener {
    private lateinit var orientation: Orientation
    private var mAttitudeIndicator: AttitudeIndicator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)

        orientation = Orientation(this)
        mAttitudeIndicator = findViewById<View>(R.id.attitude_indicator) as AttitudeIndicator
    }

    override fun onStart() {
        super.onStart()
        orientation.startListening(this)
    }

    override fun onStop() {
        super.onStop()
        orientation.stopListening()
    }

    override fun onOrientationChanged(pitch: Float, roll: Float) {
        LogUtil.w("onOrientationChanged pitch=$pitch roll=$roll")
        mAttitudeIndicator!!.setAttitude(pitch, roll)
    }
}
