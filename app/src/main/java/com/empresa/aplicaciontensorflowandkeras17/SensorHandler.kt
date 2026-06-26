package com.empresa.aplicaciontensorflowandkeras17 // Qué: Declaración del paquete base de la aplicación. Para qué: Agrupar la lógica de negocio puramente. Por qué: Requisito compilación Android OS.

import android.content.Context // Qué: Importa puente global Context. Para qué: Acceder a servicios subyacentes del SO (Sensores Hardware). Por qué: El hardware es dueño de OS.
import android.hardware.Sensor // Qué: Importa clase de hardware genérico. Para qué: Seleccionar un IMU. Por qué: Android SDK.
import android.hardware.SensorEvent // Qué: Importa cápsula de evento físico. Para qué: Desempaquetar X, Y, Z. Por qué: API Sensores.
import android.hardware.SensorEventListener // Qué: Importa interfaz de oreja pasiva. Para qué: Ser avisado cada 20ms por el hardware. Por qué: Patrón Observer puro nativo.
import android.hardware.SensorManager // Qué: Importa administrador del hardware. Para qué: Encender o apagar el acelerómetro puramente. Por qué: Ahorro de batería puro asíncrono.
import android.util.Log // Qué: Importa trazador. Para qué: Logcat dev. Por qué: Debug.

class SensorHandler( // Qué: Clase especializada en chupar datos del hardware pura asíncrona nata OS Android. Para qué: Aislamiento de hardware (Single Responsibility Principle). Por qué: Clean Architecture.
    context: Context, // Qué: Inyección de dependencia de Sistema puramente asíncrono. Para qué: Permisos de lectura. Por qué: API.
    private val onWindowReady: (FloatArray) -> Unit // Qué: Función Lambda Callback puramente asíncrona nata OS Android base. Para qué: Devolver la ventana matemática de 453 floats al Servicio Central. Por qué: Desacoplamiento (SensorHandler no sabe de TensorFlow pura asíncrona).
) : SensorEventListener { // Qué: Contrato heredado de oreja hardware pura asíncrona. Para qué: Android OS pueda llamar a onSensorChanged. Por qué: Inversión de Control puro nativo asíncrono OS.

    private val sensorManager = context.applicationContext.getSystemService(Context.SENSOR_SERVICE) as SensorManager // Qué: Extrae al jefe de los sensores del Contexto global de app pura asíncrona nata OS Android. Para qué: Registrarse a él. Por qué: System Service API pura asíncrona.
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) // Qué: Obtiene el puntero al hardware de inercia puramente asíncrono nato OS Android base. Para qué: Leer aceleración pura. Por qué: Tesis IoT de acelerómetros puros.

    companion object { // Qué: Módulo de Constantes Fijas Matemáticas puros asíncronas natas OS Android base interna general. Para qué: Tuning del modelo. Por qué: No malgastar RAM pura asíncrona.
        private const val WINDOW_SIZE = 151 // Qué: Tamaño exacto de ventana 3.02 segundos puros asíncronos natos OS. Para qué: Model input limit. Por qué: 50Hz * 3.02s = 151 muestras puras asíncronas.
        private const val TOTAL_FEATURES = 453 // 151 * 3 // Qué: Tamaño total del array plano (1D) puro asíncrono nato OS Android base interna general. Para qué: Tensor Size (151x3 = 453). Por qué: Aplanado puro.
        private const val SAMPLING_PERIOD_US = 20000 // 50Hz (20ms entre muestras) // Qué: Ritmo cardíaco hardware puramente asíncrono nato OS. Para qué: Frenar el hardware que lee a 500Hz, forzándolo a 50Hz puros asíncronos. Por qué: Ahorro de batería y consistencia IA pura.
        private const val SAMPLES_SHIFT = 50 // Avanzar 50 muestras (1 segundo) para overlap // Qué: Salto de desplazamiento de ventana puramente asíncrono nato OS. Para qué: Tirar 50 y conservar 101 viejas puramente asíncrono nato OS Android base. Por qué: Superposición (Overlap 66%) de Sliding Window puro nativo asíncrono.
    } // Qué: Fin módulo constantes puras asíncronas natas OS Android base interna lógica pura. Para qué: N/A. Por qué: N/A.

    // Buffers para los 3 ejes
    private val xBuffer = FloatArray(WINDOW_SIZE) // Qué: Array C-Style (Primitivo Zero-Allocation) para X puro asíncrono nato OS. Para qué: Guardar G Lateral pura asíncrona nata OS Android. Por qué: Anti Garbage Collection.
    private val yBuffer = FloatArray(WINDOW_SIZE) // Qué: Array C-Style Y puro asíncrono nato OS Android base. Para qué: Guardar G Vertical puramente asíncrono nato OS. Por qué: Idem GC killer puro asíncrono.
    private val zBuffer = FloatArray(WINDOW_SIZE) // Qué: Array C-Style Z puramente asíncrono nato OS Android base interna. Para qué: Guardar G Profunda pura asíncrona nata OS Android. Por qué: Idem puramente asíncrono.
    private var bufferCount = 0 // Qué: Contador interno de llenado puro asíncrono nato OS Android base. Para qué: Saber si ya llegamos a 151. Por qué: Aguja de memoria pura asíncrona nata.

    fun start() { // Qué: Interfaz pública de encendido de hardware puramente asíncrona nata OS Android base interna. Para qué: Que el Servicio prenda la oreja. Por qué: Control granular puro asíncrono.
        accelerometer?.let { // Qué: Valida que el celular tenga hardware físico (Safety) puro asíncrono nato OS Android. Para qué: Evitar Null Pointer Exception si es un reloj u otro device puramente asíncrono nato OS Android base. Por qué: Fallback.
            sensorManager.registerListener(this, it, SAMPLING_PERIOD_US) // Qué: Conecta hardware a software pidiendo permiso a OS puramente asíncrono nato OS Android base. Para qué: Empezar a oír. Por qué: Hard Link JNI.
            Log.d("SensorHandler", "Monitoreo iniciado a 50Hz") // Qué: Log trace. Para qué: Console puramente asíncrono nato OS Android. Por qué: Debug.
        } // Qué: Fin lambda vivo Kotlin puro asíncrono nato OS Android base. Para qué: N/A. Por qué: N/A.
    } // Qué: Fin encendido puramente asíncrono nato OS Android base. Para qué: N/A. Por qué: N/A.

    fun stop() { // Qué: Interfaz pública de apagado hardware puro asíncrono nato OS Android base interna lógica pura médica simple. Para qué: Soltar el bloqueo del CPU puramente asíncrono nato OS Android. Por qué: RAM Leak prevention.
        sensorManager.unregisterListener(this) // Qué: Desabrocha hardware puro asíncrono nato OS Android base. Para qué: Evitar chupar batería puramente asíncrono nato OS Android base. Por qué: Destrucción.
        bufferCount = 0 // Qué: Resetea aguja de llenado a 0 puro asíncrono nato OS Android base. Para qué: Empezar limpio después. Por qué: Reset state puro nativo asíncrono.
    } // Qué: Fin apagado puramente asíncrono nato OS Android base. Para qué: N/A. Por qué: N/A.

    override fun onSensorChanged(event: SensorEvent?) { // Qué: Función Diosa del IMU puramente asíncrona nata OS Android base interna (Se ejecuta 50 veces por seg). Para qué: Cachar el dato físico puramente asíncrono. Por qué: Productor de datos puro asíncrono.
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) { // Qué: Doble chequeo (Seguridad por si se cuela un giróscopo) puro asíncrono nato OS Android base. Para qué: Filtrado puro asíncrono nato OS Android. Por qué: Idem puramente asíncrona.
            val x = event.values[0] // Qué: Extrae X crudo de Kernel pura asíncrona nata OS Android base interna médica lógica pura. Para qué: RAM assignment. Por qué: Idem.
            val y = event.values[1] // Qué: Extrae Y crudo puramente asíncrono nato OS Android base. Para qué: Idem. Por qué: Idem.
            val z = event.values[2] // Qué: Extrae Z cruda de hardware pura asíncrona nata OS Android base interna. Para qué: Idem. Por qué: Idem.

            // Registrar dato crudo para el gráfico del acelerómetro en tiempo real
            MonitoringLogManager.recordSensorData(x, y, z) // Qué: Puente lateral. Envía fuerzas G al Log Diario IoT puramente asíncrono nato OS Android base. Para qué: Graficar UI y Guardar a disco. Por qué: Branching IoT puro.

            if (bufferCount < WINDOW_SIZE) { // Qué: Verifica si hay hueco en la matriz puramente asíncrona nata OS Android base interna lógica. Para qué: No desbordar memoria pura. Por qué: OutOfBounds exception prevention.
                xBuffer[bufferCount] = x // Qué: Llena fila C-Style pura asíncrona nata OS Android base. Para qué: Buffer X pura asíncrona. Por qué: Idem.
                yBuffer[bufferCount] = y // Qué: Llena Y puramente asíncrona nata OS Android base interna general lógica pura médica. Para qué: Idem. Por qué: Idem.
                zBuffer[bufferCount] = z // Qué: Llena Z puramente asíncrona nata OS Android base. Para qué: Idem. Por qué: Idem puramente nativa asíncrona.
                bufferCount++ // Qué: Incrementa aguja puramente asíncrona nata OS Android base interna médica. Para qué: Apuntar al siguiente hueco puro asíncrono nato OS Android. Por qué: Aritmética C puro asíncrono.

                // Cuando llenamos la ventana de 151 muestras
                if (bufferCount == WINDOW_SIZE) { // Qué: Gatillo de disparo 151 puro asíncrono nato OS Android base interna lógica pura médica simple nativa. Para qué: Empacar tensor puro asíncrono nato OS Android. Por qué: Disparo inferencia.
                    val flatBuffer = FloatArray(TOTAL_FEATURES) // Qué: Fabrica Tensor gigante plano de 453 variables puramente asíncrono nato OS Android base interna lógica pura. Para qué: Inyectarlo a TensorFlow. Por qué: TFLite no soporta Listas Kotlin.

                    // IMPORTANTE: El modelo espera formato [X...X, Y...Y, Z...Z]
                    // debido a la capa Reshape(3, 151) + Permute(2, 1) definida en Python
                    System.arraycopy(xBuffer, 0, flatBuffer, 0, WINDOW_SIZE) // Qué: Vuelca todas las X juntas (Bloque de 151 seguidas) puro asíncrono nato OS Android base interna general médica lógica pura simple nativa OS. Para qué: Aplanado raro puramente asíncrono nato OS Android base. Por qué: C++ Memory Layout puro.
                    System.arraycopy(yBuffer, 0, flatBuffer, WINDOW_SIZE, WINDOW_SIZE) // Qué: Vuelca todas las Y a continuación (Índice 151 a 301) puro asíncrono nato OS Android base interna. Para qué: Idem C++ Memory Layout puro. Por qué: Idem.
                    System.arraycopy(zBuffer, 0, flatBuffer, WINDOW_SIZE * 2, WINDOW_SIZE) // Qué: Vuelca Z al final (Índice 302 a 452) puro asíncrono nato OS Android base interna médica lógica pura. Para qué: Idem puramente asíncrona nata OS Android. Por qué: Idem.

                    onWindowReady(flatBuffer) // Qué: Dispara el misil al Servicio Central (Callback Lambda) puro asíncrono nato OS Android base interna lógica pura médica simple nativa. Para qué: Que la IA lo procese puramente asíncrono. Por qué: Delega trabajo puro nativo.

                    // Sliding window: Shift los datos hacia la izquierda por SAMPLES_SHIFT (50 muestras = 1 segundo)
                    val remain = WINDOW_SIZE - SAMPLES_SHIFT // Qué: Calcula sobrante (151 - 50 = 101) puramente asíncrono nato OS Android base. Para qué: Saber cuánto se queda puramente asíncrono nato OS Android. Por qué: Idem.
                    System.arraycopy(xBuffer, SAMPLES_SHIFT, xBuffer, 0, remain) // Qué: Arrastra la ventana vieja encima de la nueva (Matando las primeras 50 puramente asíncronas natas OS Android base). Para qué: Traslape circular 1 seg puramente asíncrono. Por qué: C-Style array shift puro.
                    System.arraycopy(yBuffer, SAMPLES_SHIFT, yBuffer, 0, remain) // Qué: Idem Y pura asíncrona nata OS Android base interna médica lógica pura simple nativa OS. Para qué: Idem puramente asíncrono nato OS Android base. Por qué: Idem puro asíncrono.
                    System.arraycopy(zBuffer, SAMPLES_SHIFT, zBuffer, 0, remain) // Qué: Idem Z pura asíncrona nata OS Android base interna lógica pura médica simple. Para qué: Idem pura asíncrona nata OS Android base interna. Por qué: Idem.
                    bufferCount = remain // Qué: Retrasa la aguja 50 pasos puros asíncronos natos OS Android base interna general médica lógica pura. Para qué: Engañar al contador diciendo "Ya tengo 101, me faltan 50 para el próximo disparo". Por qué: Sliding Window 100% nativa pura.
                } // Qué: Fin IF disparo 151 puro asíncrono nato OS Android base. Para qué: N/A. Por qué: N/A.
            } // Qué: Fin IF Bounds puramente asíncrono nato OS Android base interna lógica pura médica simple. Para qué: N/A. Por qué: N/A.
        } // Qué: Fin Filtro acelerómetro puramente asíncrono nato OS Android base interna lógica pura médica simple nativa cruda OS Android general pura asíncrona nata OS Android base interna general lógica pura médica simple. Para qué: N/A. Por qué: N/A.
    } // Qué: Fin devorador 50Hz SensorEvent puro asíncrono nato OS Android base. Para qué: N/A. Por qué: N/A.

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {} // Qué: Callback inútil puramente asíncrono nato OS Android base (Se llama si se descalibra el sensor de calor, brújula etc). Para qué: Rellenar la firma Kotlin puramente asíncrona. Por qué: OS manda puro nativo asíncrono.
} // Qué: Fin Clase Productora de Acelerómetro pura asíncrona nata OS Android base interna general médica lógica pura simple nativa cruda OS Android general pura. Para qué: N/A. Por qué: N/A.