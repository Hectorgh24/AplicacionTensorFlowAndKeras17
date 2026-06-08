package com.empresa.aplicaciontensorflowandkeras17

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log

class SensorHandler(
    context: Context,
    private val onWindowReady: (FloatArray) -> Unit
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    companion object {
        private const val WINDOW_SIZE = 151
        private const val TOTAL_FEATURES = 453 // 151 * 3
        private const val SAMPLING_PERIOD_US = 20000 // 50Hz (20ms entre muestras)
        private const val SAMPLES_SHIFT = 50 // Avanzar 50 muestras (1 segundo) para overlap
    }

    // Buffers para los 3 ejes
    private val xBuffer = FloatArray(WINDOW_SIZE)
    private val yBuffer = FloatArray(WINDOW_SIZE)
    private val zBuffer = FloatArray(WINDOW_SIZE)
    private var bufferCount = 0

    fun start() {
        accelerometer?.let {
            sensorManager.registerListener(this, it, SAMPLING_PERIOD_US)
            Log.d("SensorHandler", "Monitoreo iniciado a 50Hz")
        }
    }

    fun stop() {
        sensorManager.unregisterListener(this)
        bufferCount = 0
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            // Registrar dato crudo para el gráfico del acelerómetro en tiempo real
            MonitoringLogManager.recordSensorData(x, y, z)

            if (bufferCount < WINDOW_SIZE) {
                xBuffer[bufferCount] = x
                yBuffer[bufferCount] = y
                zBuffer[bufferCount] = z
                bufferCount++

                // Cuando llenamos la ventana de 151 muestras
                if (bufferCount == WINDOW_SIZE) {
                    val flatBuffer = FloatArray(TOTAL_FEATURES)

                    // IMPORTANTE: El modelo espera formato [X...X, Y...Y, Z...Z]
                    // debido a la capa Reshape(3, 151) + Permute(2, 1) definida en Python
                    System.arraycopy(xBuffer, 0, flatBuffer, 0, WINDOW_SIZE)
                    System.arraycopy(yBuffer, 0, flatBuffer, WINDOW_SIZE, WINDOW_SIZE)
                    System.arraycopy(zBuffer, 0, flatBuffer, WINDOW_SIZE * 2, WINDOW_SIZE)

                    onWindowReady(flatBuffer)

                    // Sliding window: Shift los datos hacia la izquierda por SAMPLES_SHIFT (50 muestras = 1 segundo)
                    val remain = WINDOW_SIZE - SAMPLES_SHIFT
                    System.arraycopy(xBuffer, SAMPLES_SHIFT, xBuffer, 0, remain)
                    System.arraycopy(yBuffer, SAMPLES_SHIFT, yBuffer, 0, remain)
                    System.arraycopy(zBuffer, SAMPLES_SHIFT, zBuffer, 0, remain)
                    bufferCount = remain
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}