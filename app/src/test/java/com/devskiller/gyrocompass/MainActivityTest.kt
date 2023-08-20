package com.devskiller.gyrocompass

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import org.junit.Before
import org.junit.Test
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.shadows.ShadowSensor

class MainActivityTest : BaseTest() {

    @Before
    fun before() {
        val sensorManager = (RuntimeEnvironment.getApplication().getSystemService(Context.SENSOR_SERVICE) as SensorManager)
        val sensorManagerShadow = shadowOf(sensorManager)
        sensorManagerShadow.addSensor(ShadowSensor.newInstance(Sensor.TYPE_ACCELEROMETER))
        sensorManagerShadow.addSensor(ShadowSensor.newInstance(Sensor.TYPE_MAGNETIC_FIELD))
    }

    @Test
    fun testIdle() {
        Robolectric.setupActivity(MainActivity::class.java)
    }
}
