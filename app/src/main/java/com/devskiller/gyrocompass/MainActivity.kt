package com.devskiller.gyrocompass

import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import com.devskiller.gyrocompass.databinding.ActivityMainBinding
import java.lang.Math.round
import java.lang.ref.WeakReference
import kotlin.math.roundToInt


class MainActivity : FragmentActivity() {
    data class Angle(val value: Int)
    var locationLiveData = MutableLiveData<Angle>()

    companion object {
        val KEY_ANGLE = "angle"
        val KEY_DIRECTION = "direction"
        val KEY_BACKGROUND = "background"
        val KEY_NOTIFICATION_ID = "notificationId"
        val KEY_ON_SENSOR_CHANGED_ACTION = "com.raywenderlich.android.locaty.ON_SENSOR_CHANGED"
        val KEY_NOTIFICATION_STOP_ACTION = "com.raywenderlich.android.locaty.NOTIFICATION_STOP"
    }
    private class AccelerometerSensorDataChangedListener(parent: MainActivity) : SensorEventListener {

        private val mWeakParent = WeakReference(parent)

        override fun onSensorChanged(event: SensorEvent) {
            mWeakParent.get()?.run {
                // START CHANGES
//                if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                    // 3
                    mGravityValues = FloatArray(3)
                    mGravityValues?.let { System.arraycopy(event.values, 0, mGravityValues, 0, it.size) }
//                }
                // END CHANGES

                tryToCalculateRotation()
            }
        }

        override fun onAccuracyChanged(
            sensor: Sensor,
            accuracy: Int
        ) = Unit
    }

    private class MagneticFieldSensorDataChangedListener(parent: MainActivity) : SensorEventListener {

        private val mWeakParent = WeakReference(parent)

        override fun onSensorChanged(event: SensorEvent) {
            mWeakParent.get()?.run {
                // START CHANGES
//                if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                    mGeomagneticValues = FloatArray(3)
                    mGeomagneticValues?.let { System.arraycopy(event.values, 0, mGeomagneticValues, 0, it.size) }
//            }
                // END CHANGES

                tryToCalculateRotation()
            }
        }

        override fun onAccuracyChanged(
            sensor: Sensor,
            accuracy: Int
        ) = Unit
    }

    private var mGeomagneticValues: FloatArray? = null
    private var mGravityValues: FloatArray? = null
    private var mLastAccelerometerSensorEventListener: SensorEventListener? = null
    private var mLastMagneticFieldSensorEventListener: SensorEventListener? = null
    private var mViewBinding: ActivityMainBinding? = null
    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(ActivityMainBinding.inflate(layoutInflater).run {
            mViewBinding = this
            root
        })

        val sensorManager = (getSystemService(Context.SENSOR_SERVICE) as SensorManager)
        val sensorsAvailable = sensorManager.getSensorList(Sensor.TYPE_ALL)
        val sensorsRequired = listOf(
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD))
        if (!sensorsAvailable.containsAll(sensorsRequired)) {
            Toast.makeText(this, R.string.sensors_unavailable, Toast.LENGTH_SHORT)
                    .show()

            finish()
        }

        locationLiveData.observe(this@MainActivity) {
            mViewBinding?.ivNeedle?.rotation = it.value.toFloat() * -1
        }
    }

    override fun onStart() {
        super.onStart()

        (getSystemService(Context.SENSOR_SERVICE) as SensorManager).run {
            // START CHANGES
            mLastAccelerometerSensorEventListener = AccelerometerSensorDataChangedListener(this@MainActivity)
            mLastMagneticFieldSensorEventListener = MagneticFieldSensorDataChangedListener(this@MainActivity)
            getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.also { accelerometer ->
                registerListener(mLastAccelerometerSensorEventListener,
                    accelerometer,
                    SensorManager.SENSOR_DELAY_NORMAL)
            }
            getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)?.also { magneticField ->
                registerListener(mLastMagneticFieldSensorEventListener,
                    magneticField,
                    SensorManager.SENSOR_DELAY_NORMAL)
            }
            // END CHANGES
        }
    }

    override fun onStop() {
        (getSystemService(Context.SENSOR_SERVICE) as SensorManager).run {
            unregisterListener(mLastAccelerometerSensorEventListener!!)
            mLastAccelerometerSensorEventListener = null

            unregisterListener(mLastMagneticFieldSensorEventListener!!)
            mLastMagneticFieldSensorEventListener = null
        }

        super.onStop()
    }

    override fun onDestroy() {
        mViewBinding = null

        super.onDestroy()
    }

    private fun tryToCalculateRotation() {
        if ((mGeomagneticValues != null) && (mGravityValues != null)) {

            SensorManager.getRotationMatrix(rotationMatrix, null, mGravityValues, mGeomagneticValues)

            val orientation = SensorManager.getOrientation(rotationMatrix, orientationAngles)

            val degrees = (Math.toDegrees(orientation.get(0).toDouble()) + 360.0) % 360.0

            val angle = (degrees * 100).roundToInt() / 100

            locationLiveData.postValue(Angle(value = angle))
        // END CHANGES
        }
    }
}
