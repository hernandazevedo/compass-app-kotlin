package com.devskiller.gyrocompass

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.devskiller.gyrocompass.databinding.ActivityMainBinding
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.kotlin.any
import org.mockito.kotlin.atLeastOnce
import org.mockito.kotlin.eq
import org.mockito.kotlin.isNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.powermock.api.mockito.PowerMockito.mockStatic
import org.powermock.api.mockito.PowerMockito.verifyNoMoreInteractions
import org.powermock.api.mockito.PowerMockito.verifyStatic
import org.powermock.core.classloader.annotations.PowerMockIgnore
import org.powermock.core.classloader.annotations.PrepareOnlyThisForTest
import org.powermock.modules.junit4.rule.PowerMockRule
import org.powermock.reflect.Whitebox
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows
import org.robolectric.shadows.ShadowApplication

@PowerMockIgnore("android.*", "androidx.*", "org.mockito.*", "org.robolectric.*")
@PrepareOnlyThisForTest(SensorManager::class)
class MainActivityVerifyTest : BaseTest() {

    companion object {

        private const val DEFAULT_FLOAT_DELTA = 0.01f
    }

    @Rule
    @JvmField
    var powerMockRule: PowerMockRule = PowerMockRule()

    private val mAccelerometerSensorEventMock: SensorEvent = mock()
    private val mAccelerometerSensorMock: Sensor = mock()
    private val mMagneticFieldSensorEventMock: SensorEvent = mock()
    private val mMagneticFieldSensorMock: Sensor = mock()
    private lateinit var mSensorManagerMock: SensorManager

    @Before
    fun before() {
        mockStatic(SensorManager::class.java)

        mSensorManagerMock = mock()

        val shadowApplication = (Shadows.shadowOf(RuntimeEnvironment.getApplication()) as ShadowApplication)
        shadowApplication.setSystemService(Context.SENSOR_SERVICE, mSensorManagerMock)

        whenever(mSensorManagerMock.getSensorList(eq(Sensor.TYPE_ALL)))
                .thenReturn(listOf(mAccelerometerSensorMock, mMagneticFieldSensorMock))
    }

    @Test
    fun testMagneticFieldSensorDataOnly() {
        val magneticFieldSensorEventListenerCaptor = ArgumentCaptor.forClass(SensorEventListener::class.java)

        whenever(mSensorManagerMock.getDefaultSensor(eq(Sensor.TYPE_ACCELEROMETER))).thenReturn(mAccelerometerSensorMock)
        whenever(mSensorManagerMock.registerListener(any(), eq(mAccelerometerSensorMock), any())).thenReturn(true)
        whenever(mSensorManagerMock.getDefaultSensor(eq(Sensor.TYPE_MAGNETIC_FIELD))).thenReturn(mMagneticFieldSensorMock)
        whenever(mSensorManagerMock.registerListener(
                magneticFieldSensorEventListenerCaptor.capture(),
                eq(mMagneticFieldSensorMock),
                any())
        ).thenReturn(true)

        Whitebox.setInternalState(
                mMagneticFieldSensorEventMock,
                SensorEvent::values.name,
                floatArrayOf(0.9150728f, -0.64618033f, 9.720852f))

        Robolectric.setupActivity(MainActivity::class.java)

        magneticFieldSensorEventListenerCaptor.value.onSensorChanged(mMagneticFieldSensorEventMock)

        verify(mSensorManagerMock, atLeastOnce()).getDefaultSensor(eq(Sensor.TYPE_ACCELEROMETER))
        verify(mSensorManagerMock).registerListener(any(), eq(mAccelerometerSensorMock), any())
        verify(mSensorManagerMock, atLeastOnce()).getDefaultSensor(eq(Sensor.TYPE_MAGNETIC_FIELD))
        verify(mSensorManagerMock).registerListener(any(), eq(mMagneticFieldSensorMock), any())

        verityCommonInteractions()
    }

    @Test
    fun testAccelerometerSensorDataOnly() {
        val accelerometerSensorEventListenerCaptor = ArgumentCaptor.forClass(SensorEventListener::class.java)

        whenever(mSensorManagerMock.getDefaultSensor(eq(Sensor.TYPE_ACCELEROMETER))).thenReturn(mAccelerometerSensorMock)
        whenever(mSensorManagerMock.registerListener(
                accelerometerSensorEventListenerCaptor.capture(),
                eq(mAccelerometerSensorMock),
                any())
        ).thenReturn(true)
        whenever(mSensorManagerMock.getDefaultSensor(eq(Sensor.TYPE_MAGNETIC_FIELD))).thenReturn(mMagneticFieldSensorMock)
        whenever(mSensorManagerMock.registerListener(any(), eq(mMagneticFieldSensorMock), any())).thenReturn(true)

        Whitebox.setInternalState(
                mAccelerometerSensorEventMock,
                SensorEvent::values.name,
                floatArrayOf(-2.8375f, 16.5f, -30.1375f))

        Robolectric.setupActivity(MainActivity::class.java)

        accelerometerSensorEventListenerCaptor.value.onSensorChanged(mAccelerometerSensorEventMock)

        verify(mSensorManagerMock, atLeastOnce()).getDefaultSensor(eq(Sensor.TYPE_ACCELEROMETER))
        verify(mSensorManagerMock).registerListener(any(), eq(mAccelerometerSensorMock), any())
        verify(mSensorManagerMock, atLeastOnce()).getDefaultSensor(eq(Sensor.TYPE_MAGNETIC_FIELD))
        verify(mSensorManagerMock).registerListener(any(), eq(mMagneticFieldSensorMock), any())

        verityCommonInteractions()
    }

    @Test
    fun testBitmapWithZeroRotation() = performBitmapTestWithSomeRotation(
            floatArrayOf(-2.8375f, 16.5f, -30.1375f),
            floatArrayOf(0.9150728f, -0.64618033f, 9.720852f),
            0.0f)

    @Test
    fun testBitmapWithNinetyRotation() = performBitmapTestWithSomeRotation(
            floatArrayOf(1.6223378f, 0.11258749f, 9.6968975f),
            floatArrayOf(5.25f, -0.3625f, -32.975002f),
            90.0f)

    private fun performBitmapTestWithSomeRotation(
        accelerometerSensorEventValues: FloatArray,
        magneticFieldSensorEventValues: FloatArray,
        expectedNeedleRotation: Float
    ) {
        val accelerometerSensorEventListenerCaptor = ArgumentCaptor.forClass(SensorEventListener::class.java)
        val magneticFieldSensorEventListenerCaptor = ArgumentCaptor.forClass(SensorEventListener::class.java)

        whenever(mSensorManagerMock.getDefaultSensor(eq(Sensor.TYPE_ACCELEROMETER))).thenReturn(mAccelerometerSensorMock)
        whenever(mSensorManagerMock.registerListener(
                accelerometerSensorEventListenerCaptor.capture(),
                eq(mAccelerometerSensorMock),
                any())
        ).thenReturn(true)
        whenever(mSensorManagerMock.getDefaultSensor(eq(Sensor.TYPE_MAGNETIC_FIELD))).thenReturn(mMagneticFieldSensorMock)
        whenever(mSensorManagerMock.registerListener(
                magneticFieldSensorEventListenerCaptor.capture(),
                eq(mMagneticFieldSensorMock),
                any())
        ).thenReturn(true)
        whenever(SensorManager.getRotationMatrix(any(), isNull(), any(), any())).thenAnswer {
            val r = it.getArgument<FloatArray>(0)
            val gravity = it.getArgument<FloatArray>(2)
            val geomagnetic = it.getArgument<FloatArray>(3)

            assertEquals(9, r.size)
            assertArrayEquals(accelerometerSensorEventValues, gravity, DEFAULT_FLOAT_DELTA)
            assertArrayEquals(magneticFieldSensorEventValues, geomagnetic, DEFAULT_FLOAT_DELTA)

            getRotationMatrix(r, gravity, geomagnetic)
        }
        whenever(SensorManager.getOrientation(any(), any())).thenAnswer {
            val r = it.getArgument<FloatArray>(0)
            val orientation = it.getArgument<FloatArray>(1)

            assertEquals(9, r.size)
            assertEquals(3, orientation.size)

            getOrientation(r, orientation)
        }

        Whitebox.setInternalState(mAccelerometerSensorEventMock, SensorEvent::values.name, accelerometerSensorEventValues)
        Whitebox.setInternalState(mMagneticFieldSensorEventMock, SensorEvent::values.name, magneticFieldSensorEventValues)

        val shadowActivity = Shadows.shadowOf(Robolectric.setupActivity(MainActivity::class.java))

        accelerometerSensorEventListenerCaptor.value.onSensorChanged(mAccelerometerSensorEventMock)
        magneticFieldSensorEventListenerCaptor.value.onSensorChanged(mMagneticFieldSensorEventMock)

        assertEquals(expectedNeedleRotation, ActivityMainBinding.bind(shadowActivity.contentView).ivNeedle.rotation, DEFAULT_FLOAT_DELTA)

        verify(mSensorManagerMock, atLeastOnce()).getDefaultSensor(eq(Sensor.TYPE_ACCELEROMETER))
        verify(mSensorManagerMock).registerListener(any(), eq(mAccelerometerSensorMock), any())
        verify(mSensorManagerMock, atLeastOnce()).getDefaultSensor(eq(Sensor.TYPE_MAGNETIC_FIELD))
        verify(mSensorManagerMock).registerListener(any(), eq(mMagneticFieldSensorMock), any())
        verifyStatic(SensorManager::class.java)
        SensorManager.getRotationMatrix(any(), isNull(), eq(accelerometerSensorEventValues), eq(magneticFieldSensorEventValues))
        verifyStatic(SensorManager::class.java)
        SensorManager.getOrientation(any(), any())

        verityCommonInteractions()
    }

    private fun getRotationMatrix(
        R: FloatArray?,
        gravity: FloatArray?,
        geomagnetic: FloatArray?
    ): Boolean {
        var Ax: Float = gravity!![0]
        var Ay: Float = gravity[1]
        var Az: Float = gravity[2]

        val normsqA = (Ax * Ax + Ay * Ay + Az * Az)
        val g = 9.81f
        val freeFallGravitySquared = (0.01f * g * g)
        if (normsqA < freeFallGravitySquared) {
            return false
        }

        val Ex = geomagnetic!![0]
        val Ey = geomagnetic[1]
        val Ez = geomagnetic[2]
        var Hx = ((Ey * Az) - (Ez * Ay))
        var Hy = ((Ez * Ax) - (Ex * Az))
        var Hz = ((Ex * Ay) - (Ey * Ax))
        val normH = Math.sqrt((Hx * Hx + Hy * Hy + Hz * Hz).toDouble()).toFloat()

        if (normH < 0.1f) {
            return false
        }
        val invH = (1.0f / normH)
        Hx *= invH
        Hy *= invH
        Hz *= invH
        val invA = (1.0f / Math.sqrt((Ax * Ax + Ay * Ay + Az * Az).toDouble()).toFloat())
        Ax *= invA
        Ay *= invA
        Az *= invA
        val Mx = ((Ay * Hz) - (Az * Hy))
        val My = ((Az * Hx) - (Ax * Hz))
        val Mz = ((Ax * Hy) - (Ay * Hx))
        if (R != null) {
            if (R.size == 9) {
                R[0] = Hx
                R[1] = Hy
                R[2] = Hz
                R[3] = Mx
                R[4] = My
                R[5] = Mz
                R[6] = Ax
                R[7] = Ay
                R[8] = Az
            } else if (R.size == 16) {
                R[0] = Hx
                R[1] = Hy
                R[2] = Hz
                R[3] = 0.0f
                R[4] = Mx
                R[5] = My
                R[6] = Mz
                R[7] = 0.0f
                R[8] = Ax
                R[9] = Ay
                R[10] = Az
                R[11] = 0.0f
                R[12] = 0.0f
                R[13] = 0.0f
                R[14] = 0.0f
                R[15] = 1.0f
            }
        }

        return true
    }

    private fun getOrientation(
        R: FloatArray,
        values: FloatArray
    ): FloatArray {
        if (R.size == 9) {
            values[0] = Math.atan2(R[1].toDouble(), R[4].toDouble()).toFloat()
            values[1] = Math.asin((-R[7]).toDouble()).toFloat()
            values[2] = Math.atan2((-R[6]).toDouble(), R[8].toDouble()).toFloat()
        } else {
            values[0] = Math.atan2(R[1].toDouble(), R[5].toDouble()).toFloat()
            values[1] = Math.asin((-R[9]).toDouble()).toFloat()
            values[2] = Math.atan2((-R[8]).toDouble(), R[10].toDouble()).toFloat()
        }

        return values
    }

    private fun verityCommonInteractions() {
        verify(mSensorManagerMock).getSensorList(eq(Sensor.TYPE_ALL))

        verifyNoMoreInteractions(
                mAccelerometerSensorEventMock,
                mAccelerometerSensorMock,
                mMagneticFieldSensorEventMock,
                mMagneticFieldSensorMock,
                mSensorManagerMock,
                SensorManager::class.java)
    }
}
