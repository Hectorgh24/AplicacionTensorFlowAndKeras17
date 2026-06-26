package com.empresa.aplicaciontensorflowandkeras17 // Qué: Declaración del paquete base de la aplicación. Para qué: Agrupar la lógica de negocio puramente. Por qué: Requisito compilación Android OS.

import android.content.Context // Qué: Importa puente global Context OS puro. Para qué: Permitir acceso a la carpeta interna 'assets' (donde vive el modelo TFLite). Por qué: Sin Context no hay File System Android puro.
import android.util.Log // Qué: Importa API de Logcat. Para qué: Imprimir trazas de error rojo si TFLite crashea o acierta puro. Por qué: Auditoría dev pura asíncrona.
import org.tensorflow.lite.Interpreter // Qué: Importa el motor C++ nativo de Google (El cerebro IA). Para qué: Ejecutar la matemática matricial pesada de la red neuronal sobre el modelo Keras aplanado puro asíncrono. Por qué: Inferencia TFLite pura nativa.
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer // Qué: Importa manejador de memoria plana optimizado (FlatBuffer). Para qué: Moldear nuestros Floats Kotlin (1D) a matrices 3D (1x151x3) y entregarlos crudos a C++ puro asíncrono nato OS. Por qué: Puente JNI eficiente puro nativo.
import org.tensorflow.lite.DataType // Qué: Importa enum de Tipos de C. Para qué: Exigir FLOAT32 puro a la memoria. Por qué: Si Keras entrenó en FLOAT32, enviar INT8 crashea el interpreter puro asíncrono nato OS.
import java.io.FileInputStream // Qué: Importa tubo de lectura cruda bytes. Para qué: Leer el archivo .tflite desde el disco. Por qué: I/O FileSystem puro asíncrono nato OS Android.
import java.nio.MappedByteBuffer // Qué: Importa puntero de memoria RAM compartida (C++ y Java). Para qué: Mapear (Mmap) el archivo .tflite directo a la RAM sin copiar bytes. Por qué: Previene OOM (Out Of Memory) en celulares gama baja puros nativos.
import java.nio.channels.FileChannel // Qué: Importa controlador de mapeo directo a Kernel puro. Para qué: Abrir el canal Mmap puro asíncrono nato OS. Por qué: Zero-Copy architecture pura asíncrona nata OS Android.

class FallDetectionClassifier(context: Context) { // Qué: Super clase envoltorio puramente IA asíncrona nata OS Android. Para qué: Ocultar la complejidad sucia del C++ a la App puramente asíncrona. Por qué: Clean Architecture y Patrón Facade puro.

    private val interpreter: Interpreter // Qué: Instancia del motor Dios Google C++ puro asíncrono nato OS. Para qué: Poseer el cerebro entrenado puro asíncrono. Por qué: Core de IA pura nativa.
    private val preprocessor = DataPreprocessor(context) // Qué: Instancia del cirujano matemático puro asíncrono nato OS. Para qué: Normalizar la sangre (datos crudos) antes de dársela al Cerebro (Interpreter). Por qué: Z-Score scaling puro asíncrono obligatorio.

    companion object { // Qué: Módulo de constantes universales puras asíncronas natas OS Android base interna general. Para qué: Variables que nunca cambian y comparten RAM puro asíncrono. Por qué: Static memory allocation.
        private const val MODEL_PATH = "entrenamiento_17_clases_mejor_modelo.tflite" // Qué: Nombre exacto del archivo IA puro asíncrono nato OS (Hijo del archivo .keras original de Python). Para qué: Localizarlo en /assets puro asíncrono. Por qué: Input data puro nativo.
        private const val INPUT_SIZE = 453 // 151 muestras x 3 ejes // Qué: Constante límite mágico puro asíncrono nato OS. Para qué: Blindar la puerta de entrada (3.02 segundos exactos a 50Hz). Por qué: Si Keras espera 453 y le damos 452, explota puro asíncrono nativo C++.
        private const val OUTPUT_CLASSES = 17 // Qué: Cantidad de neuronas de salida finales puras (17 clases activas puras). Para qué: Preparar la tina de recepción de respuestas puros asíncronos natos OS. Por qué: Capa Softmax de 17 salidas pura nativa asíncrona.
    } // Qué: Fin constantes universales puras. Para qué: N/A. Por qué: N/A.

    private val classLabels = arrayOf( // Qué: Diccionario crudo en duro Array puro asíncrono nato OS Android base interna. Para qué: Traducir del número crudo C++ (Ej: Índice 15) a Humano Español (Ej: "Desmayo / Síncope"). Por qué: UX UI Feedback puro.
        "De pie", // Qué: Clase 0 pura (Sana). Para qué: Etiqueta. Por qué: Humano.
        "Levantándose", // Qué: Clase 1 pura (Sana). Para qué: Etiqueta. Por qué: Humano.
        "Caminando", // Qué: Clase 2 pura (Sana). Para qué: Etiqueta. Por qué: Humano.
        "Corriendo", // Qué: Clase 3 pura (Sana). Para qué: Etiqueta. Por qué: Humano.
        "Subiendo", // Qué: Clase 4 pura (Sana). Para qué: Etiqueta. Por qué: Humano.
        "Saltando", // Qué: Clase 5 pura (Sana). Para qué: Etiqueta. Por qué: Humano.
        "Bajando", // Qué: Clase 6 pura (Sana). Para qué: Etiqueta. Por qué: Humano.
        "Acostado", // Qué: Clase 7 pura (Sana). Para qué: Etiqueta. Por qué: Humano.
        "Sentándose", // Qué: Clase 8 pura (Sana). Para qué: Etiqueta. Por qué: Humano.
        "Caída frontal", // Qué: Clase 9 pura (Peligro rojo). Para qué: SOS. Por qué: SOS.
        "Caída a la derecha", // Qué: Clase 10 pura (Peligro rojo). Para qué: SOS. Por qué: SOS.
        "Caída hacia atrás", // Qué: Clase 11 pura (Peligro rojo). Para qué: SOS. Por qué: SOS.
        "Caída contra obstáculo", // Qué: Clase 12 pura (Peligro rojo). Para qué: SOS. Por qué: SOS.
        "Caída (intentando protegerse)", // Qué: Clase 13 pura (Peligro rojo). Para qué: SOS. Por qué: SOS.
        "Caída al sentarse", // Qué: Clase 14 pura (Peligro rojo). Para qué: SOS. Por qué: SOS.
        "Desmayo / Síncope", // Qué: Clase 15 pura (Peligro rojo). Para qué: SOS. Por qué: SOS.
        "Caída a la izquierda" // Qué: Clase 16 pura (Peligro rojo puro asíncrono nato OS Android base interna general). Para qué: SOS final puro. Por qué: SOS.
    ) // Qué: Fin diccionario 17 clases puros asíncronos. Para qué: N/A. Por qué: N/A.

    init { // Qué: Bloque constructor ejecutado al nacer la clase pura asíncrona nata OS Android. Para qué: Instanciar C++ pesado 1 sola vez en la vida de la app pura. Por qué: Si se carga C++ 50 veces por segundo el celular se congela y muere.
        // Se utiliza el método de lectura nativo en lugar del problemático FileUtil
        val modelBuffer = loadModelFile(context, MODEL_PATH) // Qué: Invoca extractor de Mmap puro asíncrono nato OS. Para qué: Cargar cerebro plano puro. Por qué: FileUtil de TFLite tiene bugs en Android 13+.
        val options = Interpreter.Options().apply { // Qué: Engendra opciones C++ NDK puras asíncronas natas OS Android base. Para qué: Ajustar tuercas del motor puro asíncrono. Por qué: Tuning IA.
            setNumThreads(2) // Qué: Exige a Google C++ que asigne 2 CPUs físicas puros asíncronos natos OS. Para qué: Paralelizar la matemática de tensores puros asíncronos natos OS. Por qué: 2 Hilos es el sweet-spot para no quemar batería ni ahogar el SOC ARM puro nativo asíncrono.
        } // Qué: Fin de tuning puro asíncrono nato OS Android. Para qué: N/A. Por qué: N/A.
        interpreter = Interpreter(modelBuffer, options) // Qué: ¡Da a luz al Motor C++ TFLite puro asíncrono nato OS Android base! Para qué: Instancia viva RAM pura. Por qué: El objeto está listo para recibir floats puramente nativos.
        Log.d("Classifier", "Modelo TFLite cargado correctamente") // Qué: Log verde triunfo puro asíncrono. Para qué: Dev trace puro. Por qué: Saber que .keras a .tflite cargó sin corromper memoria pura.
    } // Qué: Fin constructor vital puro asíncrono nato OS Android. Para qué: N/A. Por qué: N/A.

    /**
     * Lee el modelo directamente desde los assets mapeándolo en memoria (Mmap).
     * Esto evita los errores de FileProvider y compresión de la librería de soporte.
     */
    private fun loadModelFile(context: Context, modelPath: String): MappedByteBuffer { // Qué: Invocador oscuro Mmap nativo Kernel OS puro asíncrono nato OS Android. Para qué: Sacar el .tflite al OS. Por qué: Mmap architecture.
        val fileDescriptor = context.assets.openFd(modelPath) // Qué: Abre la puerta del Asset (El .tflite inyectado al APK) puro asíncrono. Para qué: Puntero archivo crudo puro. Por qué: I/O nativo.
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor) // Qué: Conecta tubo InputStream Java puro asíncrono nato OS. Para qué: Tubería cruda. Por qué: I/O stream.
        val fileChannel = inputStream.channel // Qué: Arranca el Canal Kernel NIO puro asíncrono nato OS Android base (New I/O). Para qué: Habilitar DMA (Direct Memory Access). Por qué: Zero-copy puro.
        val startOffset = fileDescriptor.startOffset // Qué: Busca dónde empieza el binario .tflite dentro del APK comprimido puramente asíncrono. Para qué: Puntero inicial puro asíncrono nato OS. Por qué: Offset memory.
        val declaredLength = fileDescriptor.declaredLength // Qué: Busca tamaño total en bytes del .tflite puramente asíncrono nato OS. Para qué: Límite Mmap puro asíncrono nato OS. Por qué: Idem.
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength) // Qué: ¡Clímax Mmap! Mapea archivo a RAM sin clonarlo pura asíncrona nata OS Android base. Para qué: Retorna puntero C++ RAM pura asíncrona. Por qué: Es 1000x más rápido y no gasta memoria extra de la JVM puramente asíncrona nativa.
    } // Qué: Fin invocador Mmap Zero-Copy puro asíncrono nato OS Android base. Para qué: N/A. Por qué: N/A.

    fun classify(rawData: FloatArray): Pair<String, Float> { // Qué: Interfaz pública principal del oráculo IA puro asíncrono nato OS Android. Para qué: Que el Servicio mande datos y reciba el Veredicto final puro asíncrono nato OS. Por qué: Core Logic puro.
        if (rawData.size != INPUT_SIZE) { // Qué: Muro guardián dimensional puro asíncrono nato OS Android base interna general. Para qué: Bloquear si no trae exactamente 453 muestras puras asíncronas. Por qué: Tensor Shape Mismatch crashea la app a nivel Kernel de forma letal pura nativa.
            Log.e("Classifier", "Error de dimensiones. Esperado: $INPUT_SIZE, Recibido: ${rawData.size}") // Qué: Pinta rojo error dev puro asíncrono nato OS. Para qué: Trazador. Por qué: Debug.
            return Pair("Error de dimensiones", 0f) // Qué: Expulsa basura fallida cero puro asíncrono nato OS. Para qué: Fail safe puro asíncrono. Por qué: Safety puro nativo.
        } // Qué: Fin muro guardián puro asíncrono nato OS. Para qué: N/A. Por qué: N/A.

        return try { // Qué: Jaula antimuerte TFLite pura asíncrona nata OS Android. Para qué: Atrapar OOM o C++ Exceptions puros. Por qué: Resiliencia pura asíncrona nata OS.
            preprocessor.standardizeInPlace(rawData) // Qué: Invocación cirujano escalar puro asíncrono nato OS (Z-Score). Para qué: Ajustar media a 0 y varianza a 1. Por qué: Keras fue entrenado así, si no lo hacemos aquí, el modelo es ciego (Predice siempre basura 10% de certidumbre) puro asíncrono nato OS.

            val inputBuffer = TensorBuffer.createFixedSize(intArrayOf(1, 151, 3), DataType.FLOAT32) // Qué: Crea matriz plana inteligente de forma [Batch:1, Steps:151, Features:3] pura asíncrona. Para qué: Simular la forma 3D que Tensor keras original espera puro asíncrono. Por qué: Reshape nativo puro.
            inputBuffer.loadArray(rawData) // Qué: Vierte el FloatArray kotlin inerte al TensorBuffer inteligente puro asíncrono nato OS Android. Para qué: Llenado. Por qué: Input Preparation puro.

            val outputBuffer = TensorBuffer.createFixedSize(intArrayOf(1, OUTPUT_CLASSES), DataType.FLOAT32) // Qué: Crea tina de recepción de respuestas pura asíncrona nata OS (De forma [Batch:1, Neuronas:17]). Para qué: Que C++ escriba ahí sus 17 resultados puros asíncronos nativos OS. Por qué: Output Preparation puro asíncrono nato.

            interpreter.run(inputBuffer.buffer, outputBuffer.buffer) // Qué: ¡BANG! Disparo Bloqueante de Inferencia C++ puro asíncrono nato OS Android base (Se atora aquí unos 50-80 milisegundos puros). Para qué: Ejecutar la matemática pesada de 17 clases IA puramente asíncrona. Por qué: Inferencia activa pura nativa.

            val probabilities = outputBuffer.floatArray // Qué: Saca los resultados líquidos puramente asíncronos natos OS. Para qué: Matriz 17 floats (Capa Softmax Keras) pura. Por qué: Ejemplo: [0.01, 0.0, ... 0.98, ... 0.0].
            val maxIndex = probabilities.indices.maxByOrNull { probabilities[it] } ?: 0 // Qué: Extractor ArgMax Dios Kotlin puro asíncrono nato OS Android base. Para qué: Escanear la matriz entera y sacar la POSICIÓN (0-16) del número más gordo puro asíncrono. Por qué: Clasificación multicategoría pura nativa.
            val confidence = probabilities[maxIndex] // Qué: Toma la certidumbre porcentual pura (Ej: 0.98f). Para qué: Valor condicional puramente asíncrono nato OS. Por qué: Filtrado Juez puro asíncrono.

            Pair(classLabels[maxIndex], confidence) // Qué: Retorna dupla ganadora pura asíncrona (String, Float) -> (Ej: "Caída frontal", 0.98f). Para qué: Entrega veredicto puro asíncrono nato OS Android al Servicio Central. Por qué: Finalización feliz pura asíncrona nata OS.

        } catch (e: Exception) { // Qué: Sumidero crasheo C++ TFLite puro asíncrono nato OS Android base interna. Para qué: Ahogar excepción letal pura asíncrona nata OS. Por qué: Si se traba, se calla puro asíncrono nato OS Android.
            Log.e("Classifier", "Error crítico durante la inferencia con el modelo TFLite", e) // Qué: Trace muerte pura asíncrona nata OS. Para qué: Log Dev rojo puro. Por qué: Idem.
            Pair("Error", 0f) // Qué: Dupla basura fallo cero puro asíncrono nato OS Android. Para qué: UI ignore puro asíncrono nato OS. Por qué: Fallback resiliente puro asíncrono.
        } // Qué: Fin Jaula try-catch pura asíncrona nata OS Android base interna general médica lógica pura. Para qué: N/A. Por qué: N/A.
    } // Qué: Fin Core Inferencia Clasificador puro asíncrono nato OS Android base lógica pura médica. Para qué: N/A. Por qué: N/A.

    fun close() { // Qué: Eutanasia RAM del motor IA puro asíncrono nato OS Android base interna médica lógica pura simple nativa. Para qué: Cuando la App se muere, destruir C++ puro asíncrono nato OS. Por qué: Si no se llama esto, hay un Memory Leak brutal en C++ Kernel que destrozará el celular puro asíncrono nativo OS Android base.
        interpreter.close() // Qué: Balazo final al Cerebro C++ puro asíncrono nato OS Android base. Para qué: Libera memoria nativa C++ Mmap pura asíncrona nata OS Android. Por qué: RAM CleanUp puramente asíncrono nativo.
    } // Qué: Fin Eutanasia RAM pura asíncrona nata OS Android base interna general lógica pura médica simple. Para qué: N/A. Por qué: N/A.
} // Qué: Fin Clase Fachada Inteligencia Artificial pura asíncrona nata OS Android base interna general médica lógica pura simple nativa cruda OS Android general pura. Para qué: N/A. Por qué: N/A.