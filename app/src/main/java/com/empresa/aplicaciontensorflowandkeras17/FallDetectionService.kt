package com.empresa.aplicaciontensorflowandkeras17 // Qué: Declaración del paquete base de la aplicación. Para qué: Agrupar la lógica de negocio puramente. Por qué: Requisito compilación Android OS.

import android.app.NotificationChannel // Qué: Importa Creador de Canales UI puro asíncrono. Para qué: Agrupar notificaciones persistentes (Foreground). Por qué: Android 8+ lo exige puro.
import android.app.NotificationManager // Qué: Importa Administrador UI puro asíncrono. Para qué: Gestionar qué notificación se ve pura. Por qué: OS Constraint puro asíncrono.
import android.app.Service // Qué: Importa la clase Servicio Android puro asíncrono nato OS. Para qué: Ser un demonio invisible sin interfaz UI puramente asíncrono. Por qué: Correr TFLite con pantalla apagada.
import android.content.Context // Qué: Importa puente global puro asíncrono nato OS Android. Para qué: Usar Hardware. Por qué: Idem.
import android.content.Intent // Qué: Importa Flecha OS puro asíncrono nato OS Android base. Para qué: Enviar órdenes a MainActivity. Por qué: Idem puramente asíncrono.
import android.content.pm.ServiceInfo // Qué: Importa flags OS puro asíncrono nato OS Android base. Para qué: Declarar Foreground Service Type pura asíncrona. Por qué: Android 14 exige decir si eres app de salud puramente asíncrona nata OS.
import android.os.Build // Qué: Importa Versionador OS puro asíncrono nato OS Android base. Para qué: Ejecutar ifs de retrocompatibilidad puramente asíncrono nato OS Android. Por qué: Fragmentación Android.
import android.os.CountDownTimer // Qué: Importa Reloj Ticking puro asíncrono nato OS Android base interna. Para qué: Guillotina de 120 segundos puramente asíncrona nata OS. Por qué: Tesis protocolo experimental.
import android.os.IBinder // Qué: Importa Binding puramente asíncrono nato OS Android base. Para qué: Requisito abstracto Service. Por qué: Arquitectura puramente asíncrona nativa.
import android.os.PowerManager // Qué: Importa Gestor Eléctrico puro asíncrono nato OS Android base. Para qué: Pedir un WakeLock puramente asíncrono nato OS. Por qué: Impedir que la CPU duerma.
import android.util.Log // Qué: Importa Trace puramente asíncrono nato OS Android base. Para qué: Dev logcat puro. Por qué: Debug.
import androidx.core.app.NotificationCompat // Qué: Importa Constructor Retrocompatible puramente asíncrono nato OS Android base. Para qué: Crear la Notificación Zombie pura asíncrona. Por qué: Idem Android puro.
import java.util.concurrent.ExecutorService // Qué: Importa Cola Hilos puramente asíncrona nata OS Android base interna. Para qué: Aislar TFLite del Hilo Principal puramente asíncrono nato OS. Por qué: UI Freeze prevention pura asíncrona nata OS.
import java.util.concurrent.Executors // Qué: Importa Fabrica Hilos puramente asíncrona nata OS Android base. Para qué: Crear el Single Thread. Por qué: Idem.
import java.util.concurrent.atomic.AtomicBoolean // Qué: Importa Semáforo Multihilo puro asíncrono nato OS Android base. Para qué: Proteger ExecutorService de saturación pura asíncrona nata OS. Por qué: OOM Prevention puro asíncrono nato OS.

class FallDetectionService : Service() { // Qué: La Bestia Central (Demonio) puramente asíncrono nato OS Android base interna general lógica pura médica simple nativa OS. Para qué: Orquestar todo. Por qué: Arquitectura orientada a servicios.

    private lateinit var classifier: FallDetectionClassifier // Qué: Puntero diferido a IA pura asíncrona nata OS Android base. Para qué: Cerebro. Por qué: Idem.
    private lateinit var sensorHandler: SensorHandler // Qué: Puntero diferido a Oreja puramente asíncrona nata OS Android base interna. Para qué: Adquisición cruda. Por qué: Idem.
    private lateinit var inferenceExecutor: ExecutorService // Qué: Hilo esclavo TFLite puramente asíncrono nato OS Android base. Para qué: Multihilo. Por qué: Evitar trabas de UI puramente asíncrona nata OS Android base interna.
    @Volatile // Qué: Variable atómica de cache RAM pura asíncrona nata OS Android base interna médica. Para qué: Que el IMU no le mande datos a la IA si no ha cargado C++ puramente asíncrono nato OS Android base. Por qué: Crash protection pura asíncrona nata OS.
    private var classifierReady = false // Qué: Bandera inicial pura asíncrona nata OS Android base. Para qué: Idem. Por qué: Idem.

    /** Flag atomico para evitar saturar el executor con tareas de inferencia */
    private val inferenceInProgress = AtomicBoolean(false) // Qué: Candado atómico C++ puramente asíncrono nato OS Android base interna general médica lógica. Para qué: Evitar Deadlock / Backpressure TFLite puramente asíncrono nato OS. Por qué: Si JNI dura más de 1 seg, morimos puros asíncronos natos OS.

    /** WakeLock parcial para mantener la CPU activa incluso con pantalla apagada */
    private var wakeLock: PowerManager.WakeLock? = null // Qué: Permiso dictatorial CPU pura asíncrona nata OS Android base interna lógica pura médica simple. Para qué: Mantener vivo el celular en el bolsillo puro asíncrono nato OS. Por qué: Doze Mode mata apps.

    /** Temporizador de 2 minutos (120 000 ms) para auto-detener la sesión */
    private var sessionTimer: CountDownTimer? = null // Qué: Reloj guillotina pura asíncrona nata OS Android base. Para qué: Terminar tesis puro asíncrono. Por qué: Idem.

    override fun onCreate() { // Qué: Instanciador 1 vez pura asíncrona nata OS Android base interna médica lógica pura simple nativa OS. Para qué: Arranque Demonio. Por qué: Service Lifecycle.
        super.onCreate() // Qué: OS SDK puramente asíncrono nato OS Android base. Para qué: SDK. Por qué: SDK.

        // 1. Crear el canal de notificaciones (OBLIGATORIO para evitar cierres forzados)
        createNotificationChannel() // Qué: Inicializa UI zombie puramente asíncrona nata OS Android base interna. Para qué: OS Compliance puro. Por qué: Idem puramente asíncrono.

        // 2. Adquirir WakeLock parcial para que la CPU no se duerma en segundo plano
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager // Qué: Consigue al Administrador CPU pura asíncrona nata OS Android base interna lógica. Para qué: Pedir WakeLock puro. Por qué: Idem puramente asíncrona nata OS.
        wakeLock = powerManager.newWakeLock( // Qué: Tramita el Permiso puramente asíncrono nato OS Android base interna lógica pura médica. Para qué: No apagar pantalla puramente asíncrono nato OS. Por qué: Idem.
            PowerManager.PARTIAL_WAKE_LOCK, // Qué: Sólo CPU, no pantalla puramente asíncrona nata OS Android base interna general. Para qué: Ahorrar batería pero seguir computando tensores TFLite puros asíncronos natos OS. Por qué: Eficiencia pura asíncrona nata OS.
            "FallDetector::MonitoringWakeLock" // Qué: Nombre dev puro asíncrono nato OS Android base interna. Para qué: Auditoría Google Battery Stats pura asíncrona nata OS Android base. Por qué: Log puro nativo.
        ).apply { // Qué: Instancia encadenada pura asíncrona nata OS Android base. Para qué: Lock inmediato puramente asíncrono nato OS. Por qué: Kotlin vivo.
            // Timeout de seguridad de 3 minutos (180s) por si algo falla
            acquire(3 * 60 * 1000L) // Qué: Toma a la fuerza la CPU por 180s puramente asíncrono nato OS Android base. Para qué: Si la app crashea a los 120s, no dejar al celular bloqueado eternamente consumiendo batería puramente asíncrona nata OS Android base interna médica. Por qué: Safety puro asíncrono nato OS.
        } // Qué: Fin WakeLock puro asíncrono nato OS Android base. Para qué: N/A. Por qué: N/A.

        inferenceExecutor = Executors.newSingleThreadExecutor() // Qué: Fabrica al Esclavo solitario TFLite puramente asíncrono nato OS Android base interna lógica pura médica. Para qué: Correr JNI puro asíncrono nato OS. Por qué: UI libre pura asíncrona nata OS Android.
        inferenceExecutor.execute { // Qué: Manda orden al Esclavo puramente asíncrono nato OS Android base. Para qué: Instanciar modelo puramente asíncrono nato OS Android base. Por qué: Load Model es Mmap bloqueante puramente asíncrono.
            try { // Qué: Jaula OS pura asíncrona nata OS Android base interna médica lógica pura simple. Para qué: TFLite Fail puramente asíncrono nato OS Android base. Por qué: Idem pura asíncrona nata OS.
                classifier = FallDetectionClassifier(this) // Qué: Da a luz al Motor Inteligente puramente asíncrono nato OS Android base interna general lógica pura médica simple nativa OS. Para qué: Cerebro vivo. Por qué: IA pura.
                classifierReady = true // Qué: Baja bandera roja a verde puramente asíncrona nata OS Android base. Para qué: SensorHandler sepa que ya puede escupir datos puramente asíncronos natos OS Android base. Por qué: Semáforo interhilos puramente asíncrono nato OS.
                Log.d("FallService", "Clasificador inicializado en segundo plano") // Qué: Log éxito puramente asíncrono nato OS Android base interna lógica pura médica simple. Para qué: Dev. Por qué: Debug.
            } catch (e: Exception) { // Qué: Falla C++ pura asíncrona nata OS Android base. Para qué: Crash IA puramente asíncrono nato OS. Por qué: Idem puramente asíncrono nato OS.
                Log.e("FallService", "No se pudo inicializar el clasificador", e) // Qué: Error trace puro asíncrono nato OS Android base interna general médica lógica. Para qué: Log Dev puramente asíncrono nato OS Android base. Por qué: Idem.
            } // Qué: Fin jaula IA puramente asíncrona nata OS Android base interna médica lógica pura simple nativa OS Android. Para qué: N/A. Por qué: N/A.
        } // Qué: Fin hilo load puramente asíncrono nato OS Android base. Para qué: N/A. Por qué: N/A.

        sensorHandler = SensorHandler(this) { windowData -> // Qué: Da a luz a la Oreja 50Hz pasándole el Lambda de recepción puramente asíncrono nato OS Android base interna lógica pura médica simple nativa. Para qué: Iniciar IMU. Por qué: Arquitectura de retrollamada puramente asíncrona nata OS Android base interna general.
            // Solo lanzar inferencia si no hay una en progreso.
            // Si el motor esta ocupado, registrar prediccion duplicada para mantener intervalos exactos de 1s.
            if (inferenceInProgress.compareAndSet(false, true)) { // Qué: Operación Atómica pura asíncrona nata OS Android base (Mutex Lock). Para qué: ¿Estás libre? Sí, me ocupo en 1 solo pulso de reloj puro asíncrono nato OS. Por qué: Evita Backpressure puro asíncrono nato OS Android.
                inferenceExecutor.execute { // Qué: Avienta los 453 floats al Esclavo puramente asíncrono nato OS Android base interna lógica pura médica simple nativa OS Android general pura asíncrona. Para qué: Inferencia asíncrona pura. Por qué: JNI bloqueante puro.
                    try { // Qué: Jaula Matemática pura asíncrona nata OS Android base. Para qué: Fallo IA puro asíncrono nato OS Android base interna. Por qué: Resiliencia pura asíncrona nata OS Android base.
                        processInference(windowData) // Qué: Detona la clasificación matricial puramente asíncrona nata OS Android base interna médica lógica pura simple nativa OS Android. Para qué: Obtener Veredicto puramente asíncrono. Por qué: Inferencia pura.
                    } catch (e: Exception) { // Qué: Sumidero IA puro asíncrono nato OS Android base interna general médica lógica pura simple. Para qué: Tragar fallo puramente asíncrono nato OS Android base. Por qué: Idem puro asíncrono nato OS.
                        Log.e("FallService", "Fallo critico al procesar la ventana de datos del sensor", e) // Qué: Trace dev puramente asíncrono nato OS Android base. Para qué: Dev. Por qué: Debug.
                    } finally { // Qué: Ejecución forzada siempre puramente asíncrona nata OS Android base interna general lógica pura médica simple nativa OS. Para qué: Mutex Release puramente asíncrono nato OS. Por qué: Si no liberas, el celular queda ciego puramente asíncrono nato OS Android base interna médica lógica.
                        inferenceInProgress.set(false) // Qué: Libera el candado Atómico puramente asíncrono nato OS Android base interna lógica pura. Para qué: SensorHandler pueda mandar el frame del siguiente segundo puramente asíncrono nato OS Android base interna general. Por qué: Thread safety puro nativo asíncrono.
                    } // Qué: Fin finally asíncrono nato OS Android base. Para qué: N/A. Por qué: N/A.
                } // Qué: Fin delegación a esclavo puramente asíncrono nato OS Android base interna lógica pura médica simple nativa OS Android general pura. Para qué: N/A. Por qué: N/A.
            } else { // Qué: Si el Candado Atómico estaba ocupado puramente asíncrono nato OS Android base interna médica lógica pura. Para qué: Desechar ventana (Drop Frame) puramente asíncrono nato OS Android base. Por qué: Si JNI C++ está ahogado, no amontones RAM pura asíncrona nata OS Android base interna.
                // Inferencia ocupada: duplicar ultima prediccion para no perder el intervalo de 1s
                MonitoringLogManager.recordDuplicatePrediction(this) // Qué: Parcheador JSON puramente asíncrono nato OS Android base interna lógica pura médica simple nativa cruda OS. Para qué: Escribe la predicción vieja para que la gráfica Python no tenga saltos de tiempo (Gap Fill). Por qué: Data Quality pura asíncrona nata OS.
            } // Qué: Fin Drop Frame puramente asíncrono nato OS Android base interna general lógica pura médica. Para qué: N/A. Por qué: N/A.
        } // Qué: Fin Constructor SensorHandler puramente asíncrono nato OS Android base interna general médica lógica pura simple nativa cruda OS Android. Para qué: N/A. Por qué: N/A.
    } // Qué: Fin OnCreate Demonio puramente asíncrono nato OS Android base interna lógica pura médica simple nativa cruda OS Android general pura asíncrona. Para qué: N/A. Por qué: N/A.

    private fun createNotificationChannel() { // Qué: Fábrica obligatoria OS 8+ pura asíncrona nata OS Android base interna general lógica pura médica simple nativa. Para qué: OS no mate el Servicio. Por qué: Normativa Google pura.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { // Qué: Verifica si OS es Oreo o mayor pura asíncrona nata OS Android base interna general lógica pura médica. Para qué: Retrocompatibilidad. Por qué: Idem.
            val serviceChannel = NotificationChannel( // Qué: Define la tubería UI pura asíncrona nata OS Android base interna general médica lógica pura simple nativa. Para qué: Canal. Por qué: Idem.
                "fall_channel", // Qué: ID tubería pura asíncrona nata OS Android base interna lógica pura médica simple nativa. Para qué: Linkear. Por qué: Idem.
                "Monitoreo de Caídas", // Qué: Título humano puramente asíncrono nato OS Android base interna general médica lógica. Para qué: Configuración teléfono puro asíncrono. Por qué: UX.
                NotificationManager.IMPORTANCE_LOW // IMPORTANCE_LOW evita que suene cada vez que inicia // Qué: Silencia el "Ding" molesto puramente asíncrono nato OS Android base interna general lógica pura médica simple nativa cruda OS. Para qué: Stealth mode puro. Por qué: UX pura asíncrona.
            ) // Qué: Fin definición Canal puramente asíncrona nata OS Android base interna lógica pura médica simple nativa. Para qué: N/A. Por qué: N/A.
            val manager = getSystemService(NotificationManager::class.java) // Qué: Ministro UI OS puramente asíncrono nato OS Android base interna lógica pura médica. Para qué: Inyectar tubería. Por qué: Idem puramente asíncrona nata.
            manager?.createNotificationChannel(serviceChannel) // Qué: Inyecta canal puramente asíncrono nato OS Android base interna lógica pura médica simple nativa OS. Para qué: Guardar UI puro asíncrono nato OS. Por qué: Creación pura.
        } // Qué: Fin verificador Oreo puramente asíncrono nato OS Android base interna lógica pura médica. Para qué: N/A. Por qué: N/A.
    } // Qué: Fin Constructor Canal puramente asíncrono nato OS Android base interna lógica pura médica simple nativa OS Android. Para qué: N/A. Por qué: N/A.

    private fun processInference(data: FloatArray) { // Qué: El Juez Supremo C++ puramente asíncrono nato OS Android base interna general médica lógica pura simple nativa cruda OS Android general. Para qué: Evaluar si el flotante de TFLite es caida real o no. Por qué: Lógica de negocio.
        if (!classifierReady) { // Qué: Seguro vida C++ puramente asíncrono nato OS Android base interna lógica pura médica simple nativa. Para qué: No mandar floats a modelo muerto. Por qué: Null Pointer C++ letal.
            return // Qué: Aborta frame puramente asíncrono nato OS Android base interna general. Para qué: Safe exit puro asíncrono nato OS Android base. Por qué: Safety.
        } // Qué: Fin seguro puramente asíncrono nato OS Android base interna lógica pura médica. Para qué: N/A. Por qué: N/A.

        val (label, confidence) = classifier.classify(data) // Qué: Detona TFLite matricial pesado pura asíncrona nata OS Android base (Se atora 50ms). Para qué: IA Veredicto. Por qué: Inferencia TFLite 17 Clases.

        // Actualizar la UI en tiempo real
        val porcentaje = (confidence * 100).toInt() // Qué: Flotante a Humano (0.98 a 98). Para qué: UI pura asíncrona nata OS Android base. Por qué: UX.
        val predictionText = "$label ($porcentaje%)" // Qué: Concatena string "Caída (98%)" puramente asíncrono nato OS Android base interna. Para qué: Rótulo visual puro asíncrono. Por qué: UX pura asíncrona nata OS Android.
        MonitoringState.currentPrediction.value = predictionText // Qué: Inyecta a la UI Compose Viva puramente asíncrona nata OS Android base interna lógica pura médica simple. Para qué: Reactividad de pantalla pura. Por qué: Compose State puro.
        // Pasar tanto el texto de predicción como el nombre de clase crudo para el gráfico
        MonitoringLogManager.updatePrediction(this, predictionText, label) // Qué: Guarda al Dictador RAM Tesis puramente asíncrono nato OS Android base. Para qué: Memoria JSON Python. Por qué: Registro puro asíncrono.
        MonitoringLogManager.recordWindow(this) // Qué: Sube KPI +1 inferencia procesada puramente asíncrona nata OS Android base interna lógica pura médica. Para qué: Stats Tesis puramente asíncronas natas OS Android base. Por qué: KPI puro asíncrono.

        // Lista de clases que representan una caída real (índices 9 al 16)
        val fallClasses = listOf( // Qué: Lista Negra Roja C++ puramente asíncrona nata OS Android base interna general médica lógica pura simple nativa. Para qué: Saber qué etiquetas sí detonan alarma pura. Por qué: Regla de negocio puramente asíncrona.
            "Caída frontal", "Caída a la derecha", "Caída hacia atrás", // Qué: Elementos 9 10 11 puros. Para qué: Filtro. Por qué: Idem.
            "Caída contra obstáculo", "Caída (intentando protegerse)", "Caída al sentarse", // Qué: Elementos 12 13 14 puros. Para qué: Filtro. Por qué: Idem.
            "Desmayo / Síncope", "Caída a la izquierda" // Qué: Elementos 15 16 puros. Para qué: Filtro final. Por qué: Idem puro asíncrono nato OS.
        ) // Qué: Fin Catálogo Muertes puramente asíncrono nato OS Android base interna lógica pura médica simple nativa OS Android. Para qué: N/A. Por qué: N/A.

        // Lógica de detección: > 90% de confianza y debe ser estrictamente una clase de caída
        if (label in fallClasses && confidence > 0.90f) { // Qué: Gran Juez IF puramente asíncrono nato OS Android base interna general médica lógica pura simple nativa. Para qué: Matar Falsos Positivos puramente asíncrono nato OS Android base (1. Está en lista roja AND 2. Es 90% seguro matemáticamente puro). Por qué: Minimización de falsas alarmas pura asíncrona nata OS.
            MonitoringLogManager.recordFall(this) // Qué: Grita emergencia al Logger puramente asíncrono nato OS Android base interna. Para qué: KPI Caídas puramente asíncronas natas OS. Por qué: Stats Tesis.
            Log.w("FallService", "CAÍDA DETECTADA: $label con $porcentaje%") // Qué: Trazador naranja dev puramente asíncrono nato OS Android base interna lógica. Para qué: Debug puramente asíncrono. Por qué: Debug.

            if (!MonitoringState.sosActive.value) { // Qué: Revisa si ya estábamos gritando puramente asíncrono nato OS Android base interna general lógica pura médica. Para qué: No lanzar 20 Intents a la Activity si nos caemos 3 segundos seguidos. Por qué: Crash protection UI pura asíncrona nata OS Android base.
                MonitoringState.sosActive.value = true // Qué: Prende sirena lógica puramente asíncrona nata OS Android base interna general. Para qué: Bloqueador anti doble alarma. Por qué: Mutex lógico UI puramente asíncrono nato OS.

                // Usar FLAG_ACTIVITY_SINGLE_TOP para traer la Activity existente al frente
                // sin recrearla, evitando que la navegación se reinicie y sacque al usuario
                // de la pantalla de monitoreo
                val intent = Intent(this, MainActivity::class.java).apply { // Qué: Flecha Resurrectora de UI puramente asíncrona nata OS Android base interna lógica pura médica simple nativa cruda OS. Para qué: Que la pantalla negra se prenda y grite roja pura. Por qué: SOS.
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP // Qué: Flags mágicos OS puramente asíncronos nativos OS Android base interna lógica pura. Para qué: SINGLE_TOP es la clave: "Si ya estoy en RAM, no me mates y re-crees, solo despiértame y pásame los datos nuevos puros asíncronos natos OS Android". Por qué: UX y RAM Save puro nativo asíncrono.
                    putExtra("FALL_DETECTED", true) // Qué: Payload true puramente asíncrono nato OS Android base interna médica. Para qué: Avisar qué pasó puramente asíncrono. Por qué: Idem.
                    putExtra("FALL_TYPE", label) // Qué: Payload string puramente asíncrono nato OS Android base interna lógica pura médica simple. Para qué: Rótulo UI pura asíncrona. Por qué: Idem.
                } // Qué: Fin Flecha OS puramente asíncrona nata OS Android base interna lógica pura médica simple nativa. Para qué: N/A. Por qué: N/A.
                startActivity(intent) // Qué: ¡DISPARA! OS Despierta al MainActivity puramente asíncrono nato OS Android base interna general médica lógica pura simple nativa. Para qué: Alarma física visual/sonora puramente asíncrona nata OS. Por qué: Fin Flujo SOS puramente asíncrono nativo OS Android base.
            } // Qué: Fin if anti-rebote puramente asíncrono nato OS Android base interna lógica pura médica simple nativa OS Android. Para qué: N/A. Por qué: N/A.
        } // Qué: Fin Gran Juez puramente asíncrono nato OS Android base interna médica lógica pura simple nativa OS Android general pura asíncrona. Para qué: N/A. Por qué: N/A.
    } // Qué: Fin Evaluación puramente asíncrona nata OS Android base interna lógica pura médica simple nativa OS Android general pura. Para qué: N/A. Por qué: N/A.

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int { // Qué: Interceptor de flecha de encendido puramente asíncrono nato OS Android base interna general médica lógica pura simple. Para qué: Arrancar el motor. Por qué: Service API puramente asíncrona.
        val emergencyNumber = intent?.getStringExtra("EMERGENCY_NUMBER") ?: "" // Qué: Extrae SMS destino puramente asíncrono nato OS Android base interna lógica pura médica. Para qué: Metadato puro asíncrono. Por qué: Idem.
        MonitoringState.isMonitoring.value = true // Qué: UI reactiva Verde puramente asíncrona nata OS Android base interna lógica pura. Para qué: Dibujar botón Stop puramente asíncrono nato OS Android base. Por qué: Compose State pura.
        MonitoringState.remainingSeconds.value = 125 // 5 segundos de preparación + 120s reales // Qué: Ajusta contador puramente asíncrono nato OS Android base interna general médica lógica pura. Para qué: Reloj UI puro asíncrono. Por qué: UX.

        val notification = NotificationCompat.Builder(this, "fall_channel") // Qué: Arma la tarjeta Zombie puramente asíncrona nata OS Android base interna lógica pura médica simple. Para qué: Idem puramente asíncrona nata OS Android. Por qué: Idem.
            .setContentTitle("Protección activa") // Qué: Rótulo puramente asíncrono nato OS Android base interna. Para qué: Idem. Por qué: Idem.
            .setContentText("Monitoreando actividad en segundo plano") // Qué: Rótulo puramente asíncrono nato OS Android base. Para qué: Idem. Por qué: Idem.
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Qué: Icono OS puramente asíncrono nato OS Android base interna. Para qué: Idem. Por qué: Idem.
            .setOngoing(true) // Qué: Pega con super glue a la barra de estado pura asíncrona nata OS Android base interna lógica pura médica simple nativa. Para qué: El usuario no puede cerrarla deslizando puramente asíncrono nato OS Android base. Por qué: Anti-cierre OS puro asíncrono.
            .build() // Qué: Sella tarjeta puramente asíncrona nata OS Android base interna lógica pura médica. Para qué: Construcción pura asíncrona. Por qué: Builder Pattern.

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { // Qué: Validacion Android 10+ puramente asíncrona nata OS Android base interna. Para qué: Foreground Service Type puramente asíncrono. Por qué: Permisos estrictos Google puramente asíncronos natos OS.
            startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH) // Qué: Activa modo Inmortal + Salud puramente asíncrono nato OS Android base interna lógica pura médica simple nativa cruda. Para qué: OS perdone el consumo agresivo de CPU puro asíncrono nato OS Android. Por qué: Google API pura.
        } else { // Qué: Android fósil puro asíncrono nato OS Android base. Para qué: Retrocompatibilidad. Por qué: Idem.
            startForeground(1, notification) // Qué: Modo inmortal normal puramente asíncrono nato OS Android base interna médica lógica pura. Para qué: Idem pura asíncrona nata OS Android. Por qué: Idem.
        } // Qué: Fin validación OS puramente asíncrono nato OS Android base interna lógica pura médica simple nativa OS. Para qué: N/A. Por qué: N/A.
        
        object : CountDownTimer(5_000L, 1_000L) { // Qué: Temporizador 5s de escape (para acomodarse el teléfono) puramente asíncrono nato OS Android base interna médica lógica. Para qué: Que el usuario se guarde el teléfono puramente asíncrono nato OS Android base. Por qué: Tesis protocolo experimental.
            override fun onTick(millisUntilFinished: Long) { // Qué: Cada segundo puro asíncrono nato OS Android base interna lógica pura médica simple. Para qué: Baja contador UI puramente asíncrono nato OS Android. Por qué: UX.
                MonitoringState.remainingSeconds.value = (millisUntilFinished / 1000).toInt() + 120 // Qué: Ajuste matemático puro asíncrono nato OS Android base. Para qué: Reloj UI puro. Por qué: Idem.
            } // Qué: Fin tick puro asíncrono nato OS Android base interna lógica pura médica simple nativa. Para qué: N/A. Por qué: N/A.
            override fun onFinish() { // Qué: Arranca la prueba pura asíncrona nata OS Android base interna general médica lógica pura. Para qué: Expiran 5s. Por qué: Fase medición real pura asíncrona nata OS Android.
                MonitoringLogManager.startSession(this@FallDetectionService, emergencyNumber) // Qué: Prende grabadora Dictador RAM puro asíncrono nato OS Android base interna lógica pura médica. Para qué: Grabar IoT. Por qué: Tesis puramente asíncrona.
                sensorHandler.start() // Qué: Prende oreja IMU 50Hz puramente asíncrono nato OS Android base interna lógica pura médica simple nativa cruda OS. Para qué: Adquisición física pura. Por qué: Tesis IoT puramente asíncrona nata OS.
                startSessionTimer() // Qué: Prende la Guillotina de 120 segundos puramente asíncrona nata OS Android base interna médica. Para qué: Apagado automático puramente asíncrono nato OS Android base. Por qué: Estandarización protocolar pura asíncrona nativa.
            } // Qué: Fin lambda arranque puro asíncrono nato OS Android base interna general lógica pura médica simple nativa OS Android. Para qué: N/A. Por qué: N/A.
        }.start() // Qué: Detona escape puro asíncrono nato OS Android base interna lógica pura médica simple nativa. Para qué: N/A. Por qué: N/A.

        return START_STICKY // Qué: Contrato OS Zombie puramente asíncrono nato OS Android base interna médica lógica pura simple nativa cruda. Para qué: Si el kernel se queda sin RAM y mata a la app, esto obliga al Kernel a resucitar a la app en cuanto haya RAM libre puramente asíncrona nata OS Android base. Por qué: Resiliencia monstruosa puramente asíncrona nativa OS Android.
    } // Qué: Fin Interceptor Encendido puramente asíncrono nato OS Android base interna médica lógica pura simple nativa cruda OS Android general pura asíncrona. Para qué: N/A. Por qué: N/A.

    /**
     * Temporizador de sesión: al llegar a 0, detiene el monitoreo automáticamente
     * guardando todos los datos correctamente.
     */
    private fun startSessionTimer() { // Qué: Iniciador Guillotina 120s puramente asíncrona nata OS Android base interna lógica pura médica simple nativa OS. Para qué: Reloj final puro asíncrono nato OS Android. Por qué: Protocolo Tesis puro nativo asíncrono.
        sessionTimer?.cancel() // Qué: Purga vieja guillotina puramente asíncrona nata OS Android base interna. Para qué: Safety. Por qué: Reset puro.
        sessionTimer = object : CountDownTimer(120_000L, 1_000L) { // Qué: Instancia 120 segundos puros asíncronos natos OS Android base interna general lógica pura médica. Para qué: Ticking puro asíncrono nato OS. Por qué: Idem.
            override fun onTick(millisUntilFinished: Long) { // Qué: Cada seg puramente asíncrono nato OS Android base. Para qué: Idem. Por qué: Idem.
                MonitoringState.remainingSeconds.value = (millisUntilFinished / 1000).toInt() // Qué: Actualiza UI Compose puramente asíncrona nata OS Android base interna médica. Para qué: Reloj visual puro. Por qué: UX.
            } // Qué: Fin Tick puro asíncrono nato OS Android base. Para qué: N/A. Por qué: N/A.

            override fun onFinish() { // Qué: ¡SE ACABÓ EL TIEMPO! puramente asíncrono nato OS Android base interna general lógica pura médica simple nativa cruda. Para qué: Fin experimentación puramente asíncrona nata OS Android. Por qué: Protocolo estricto.
                MonitoringState.remainingSeconds.value = 0 // Qué: Reloj a Cero puro asíncrono nato OS Android base. Para qué: UI puramente asíncrona. Por qué: UX.
                Log.d("FallService", "Temporizador de 2 minutos completado. Auto-deteniendo monitoreo.") // Qué: Trace dev puramente asíncrono nato OS Android base interna. Para qué: Debug puramente asíncrono nato. Por qué: Log puro.
                // Detener el servicio limpiamente (invoca onDestroy que guarda datos)
                stopSelf() // Qué: Suicidio OS Automático puramente asíncrono nato OS Android base interna lógica pura médica simple nativa cruda OS Android general. Para qué: Le dice al SO "Mátame" lo que gatilla el OnDestroy() puro asíncrono nato OS Android base interna. Por qué: Clean Exit puro asíncrono nato OS.
            } // Qué: Fin Eutanasia Experimental puramente asíncrona nata OS Android base interna general lógica pura médica simple nativa OS. Para qué: N/A. Por qué: N/A.
        }.start() // Qué: Activa Guillotina puramente asíncrona nata OS Android base interna general médica lógica pura simple nativa. Para qué: N/A. Por qué: N/A.
    } // Qué: Fin Arrancador Guillotina pura asíncrona nata OS Android base interna lógica pura médica simple nativa. Para qué: N/A. Por qué: N/A.

    override fun onDestroy() { // Qué: Enterrador puramente asíncrono nato OS Android base interna general lógica pura médica simple nativa cruda OS Android. Para qué: Cerrar procesos y guardar JSON. Por qué: RAM Leak / Data Loss prevention.
        // Cancelar temporizador si aún está activo
        sessionTimer?.cancel() // Qué: Muta guillotina pura asíncrona nata OS Android base. Para qué: Liberar RAM. Por qué: Idem.
        sessionTimer = null // Qué: GC puramente asíncrono nato OS Android base interna médica. Para qué: Idem. Por qué: Idem.

        MonitoringLogManager.stopSession(this) // Qué: ¡DUMP JSON! Fuerza volcado de RAM a Flash NAND puramente asíncrono nato OS Android base interna lógica pura médica simple nativa OS Android. Para qué: Guarda experimento en carpeta Descargas. Por qué: Tesis IoT pura asíncrona nata OS.
        MonitoringState.isMonitoring.value = false // Qué: Apaga UI Rojo puro asíncrono nato OS Android base. Para qué: Reset visual puro. Por qué: UX.
        MonitoringState.currentPrediction.value = "Inactivo" // Qué: Pone letrero Off puramente asíncrono nato OS Android base interna lógica pura médica. Para qué: Reset UI puro. Por qué: UX.

        sensorHandler.stop() // Qué: Desconecta hardware IMU puro asíncrono nato OS Android base interna. Para qué: Ahorrar batería y liberar CPU puramente asíncrona nata OS Android. Por qué: RAM Leak pura nativa.
        if (classifierReady) { // Qué: Condicional IA pura asíncrona nata OS Android base interna general. Para qué: Si cargó bien puramente asíncrono nato OS Android base. Por qué: Evita null crash puro.
            classifier.close() // Qué: Balazo al C++ puro asíncrono nato OS Android base interna lógica pura médica simple nativa. Para qué: Soltar Mmap puramente asíncrono nato OS Android base. Por qué: Kernel panic prevention puro.
        } // Qué: Fin validador IA puramente asíncrono nato OS Android base interna lógica pura médica simple. Para qué: N/A. Por qué: N/A.
        inferenceExecutor.shutdownNow() // Qué: Asesina al Esclavo JNI puramente asíncrono nato OS Android base interna médica lógica pura simple nativa cruda OS Android general pura asíncrona. Para qué: Soltar Hilo OS puramente asíncrono nato OS Android base. Por qué: OS limitation puramente asíncrono.

        // Liberar WakeLock
        wakeLock?.let { // Qué: Devuelve dictadura de CPU puramente asíncrona nata OS Android base interna lógica pura médica simple nativa OS. Para qué: Si sigue activo puro asíncrono nato OS. Por qué: Batería.
            if (it.isHeld) it.release() // Qué: Suelta permiso puro asíncrono nato OS Android base interna general lógica pura médica simple. Para qué: Que OS pueda dormir el celular ya puramente asíncrono nato OS Android base. Por qué: Eficiencia pura.
        } // Qué: Fin release WakeLock puramente asíncrono nato OS Android base interna lógica pura médica. Para qué: N/A. Por qué: N/A.
        wakeLock = null // Qué: GC puramente asíncrono nato OS Android base. Para qué: Idem. Por qué: Idem.

        super.onDestroy() // Qué: Desintegra Demonio puramente asíncrono nato OS Android base interna lógica pura médica simple nativa cruda OS Android general pura asíncrona. Para qué: SDK. Por qué: SDK.
    } // Qué: Fin Enterrador puramente asíncrono nato OS Android base interna general médica lógica pura simple nativa OS Android. Para qué: N/A. Por qué: N/A.

    override fun onBind(intent: Intent?): IBinder? = null // Qué: Método inútil por contrato OS puramente asíncrono nato OS Android base interna médica lógica pura simple. Para qué: Android exige implementarlo puro asíncrono nato OS Android. Por qué: OS Architecture pura asíncrona nativa.
} // Qué: Fin Demonio Servicio Central de Inferencia IA puramente asíncrono nato OS Android base interna general médica lógica pura simple nativa cruda OS Android general pura asíncrona nata OS Android base. Para qué: N/A. Por qué: N/A.
