package com.empresa.aplicaciontensorflowandkeras17 // Qué: Declaración del paquete base. Para qué: Aislamiento del código fuente. Por qué: Android SDK manda.

import android.util.Log // Qué: Trazador. Para qué: Imprimir en consola Dev. Por qué: Debug.
import java.net.DatagramPacket // Qué: Valija de red. Para qué: Transportar bytes por Wifi. Por qué: IoT Network.
import java.net.DatagramSocket // Qué: Puerto de red. Para qué: Oír la red. Por qué: UDP Server.
import kotlin.concurrent.thread // Qué: Hilo esclavo. Para qué: Ejecutar red sin congelar UI. Por qué: OS Constraint.

import android.content.Intent // Qué: Flecha de OS. Para qué: Despertar servicios o vistas. Por qué: Navegación Android.
import android.os.Bundle // Qué: Maleta de estado. Para qué: Guardar UI state. Por qué: Lifecycle Android.
import androidx.activity.ComponentActivity // Qué: Padre Compose. Para qué: Heredar UI moderna. Por qué: Jetpack Compose.
import androidx.activity.compose.setContent // Qué: Inyector XML-less. Para qué: Pintar UI Kotlin puro. Por qué: Idem.
import androidx.activity.enableEdgeToEdge // Qué: Pantalla completa. Para qué: UI moderna Inmersiva. Por qué: Diseño.
import com.empresa.aplicaciontensorflowandkeras17.ui.Screen.AppNavigator // Qué: Enrutador pantallas. Para qué: Moverse entre pantallas. Por qué: Single Activity Architecture.
import com.empresa.aplicaciontensorflowandkeras17.ui.theme.AplicacionTensorFlowAndKeras17Theme // Qué: Colores app. Para qué: Estilo UI. Por qué: Diseño.

class MainActivity : ComponentActivity() { // Qué: Super clase visual única pura asíncrona nata OS Android. Para qué: Marco de UI Jetpack Compose. Por qué: Arquitectura moderna Android.
    override fun onCreate(savedInstanceState: Bundle?) { // Qué: Ciclo vida inicial puro. Para qué: Inyectar variables puras. Por qué: OS SDK.
        super.onCreate(savedInstanceState) // Qué: Llama OS padre. Para qué: SDK. Por qué: SDK.
        try { androidx.core.content.ContextCompat.startForegroundService(this, android.content.Intent(this, DummyForegroundService::class.java)) } catch (e: Exception) {} // Qué: Engaño OS puro. Para qué: Mantener viva la app con una notificación zombi pura asíncrona nativa OS Android. Por qué: Anti Doze Mode.
        startUdpListener() // Qué: Prende servidor UDP puro. Para qué: Recibir orden Python remota. Por qué: Prueba IoT Tesis.
        enableEdgeToEdge() // Qué: Barra estado transparente. Para qué: Diseño. Por qué: UX.
        setContent { // Qué: Dibuja Compose. Para qué: UI. Por qué: UI.
            AplicacionTensorFlowAndKeras17Theme { // Qué: Inyecta colores. Para qué: Idem. Por qué: Idem.
                AppNavigator() // Qué: Pinta vistas. Para qué: Idem. Por qué: Idem.
            } // Qué: Fin tema. Para qué: N/A. Por qué: N/A.
        } // Qué: Fin Set Content puro. Para qué: N/A. Por qué: N/A.
    } // Qué: Fin OnCreate puro. Para qué: N/A. Por qué: N/A.

    /**
     * Se invoca cuando la Activity ya existe y recibe un nuevo Intent
     * (FLAG_ACTIVITY_SINGLE_TOP). Esto permite que la alerta de caída
     * traiga la app al frente sin recrear la Activity ni reiniciar la navegación.
     */
    override fun onNewIntent(intent: Intent) { // Qué: Recibidor Flechas cuando la app ya está viva (Ej. Pantalla apagada pero viva en RAM). Para qué: No clonar la Activity 2 veces pura. Por qué: Prevención de RAM Leak por clonación de vistas puras asíncronas nativas OS Android base interna general.
        super.onNewIntent(intent) // Qué: SDK. Para qué: SDK. Por qué: SDK.
        setIntent(intent) // Qué: Machaca el viejo Intent por el Nuevo de Emergencia puro asíncrono nato OS Android base. Para qué: Actualizar parámetros UI. Por qué: Flujo reactivo puro.
        // La alerta se maneja reactivamente via MonitoringState.sosActive
        // que ya está en true cuando llegamos aquí, así que no se necesita
        // procesamiento adicional del intent.
    } // Qué: Fin onNewIntent puro asíncrono nato OS Android. Para qué: N/A. Por qué: N/A.

    private fun startUdpListener() { // Qué: Robot servidor puro asíncrono nato OS Android base interna médica. Para qué: Escuchar a Python. Por qué: Pruebas remotas.
        thread(isDaemon = true) { // Qué: Hilo demonio puro. Para qué: Red bloqueante en fondo puro. Por qué: Prevención Crash UI puro.
            try { // Qué: Try de Red puro. Para qué: Absorber pánicos IP puros. Por qué: Resiliencia pura.
                val socket = DatagramSocket(null) // Qué: Enchufe vacío. Para qué: UDP. Por qué: UDP puro.
                socket.reuseAddress = true // Qué: Reciclador IP puro. Para qué: Anti bloqueo puro. Por qué: Idem.
                socket.bind(java.net.InetSocketAddress(50000)) // Qué: Enchufa al 50k puro. Para qué: Oír python. Por qué: Puerto Tesis.
                socket.broadcast = true // Qué: Megáfono puro. Para qué: Oír broadcast LAN puro. Por qué: LAN Tesis.
                val buffer = ByteArray(256) // Qué: Caja bytes pura. Para qué: 256 de texto puro. Por qué: UDP.
                while (true) { // Qué: Bucle Dios puro. Para qué: 24/7 puro. Por qué: Servidor puro.
                    val packet = DatagramPacket(buffer, buffer.size) // Qué: Valija OS pura. Para qué: Cachar. Por qué: UDP.
                    socket.receive(packet) // Qué: Bloqueante pasivo puro (Se congela hasta oír puramente asíncrono nato OS Android). Para qué: Oír red pura asíncrona. Por qué: Red pura.
                    val message = String(packet.data, 0, packet.length).trim() // Qué: Byte a texto puro. Para qué: Parsear "START" puro. Por qué: Idem pura.
                    Log.d("UDP_LISTENER", "Recibido: $message") // Qué: Trace dev. Para qué: Logcat puro. Por qué: Debug.
                    
                    if (message == "START_MONITORING") { // Qué: Comando mágico Python puro. Para qué: Auto arrancar IA pura asíncrona nata OS Android. Por qué: Tesis puro.
                        if (!MonitoringState.isMonitoring.value) { // Qué: Seguro doble arranque puro. Para qué: Evitar clonar servicios puros asíncronos nativos OS. Por qué: RAM Leak pura.
                            val serviceIntent = Intent(this, FallDetectionService::class.java) // Qué: Flecha a Servicio Central IA puro asíncrono nato OS. Para qué: Despertar Cerebro puro asíncrono nato OS Android. Por qué: Inferencia TFLite 17 clases.
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) { // Qué: OS > 8.0 puro. Para qué: Condicional OS API. Por qué: Reglas Google puras.
                                startForegroundService(serviceIntent) // Qué: Dispara Servicio Zombie Invencible puro asíncrono nato OS Android base. Para qué: Foreground OS. Por qué: Anti Doze.
                            } else { // Qué: OS Viejos puros. Para qué: Idem. Por qué: Idem.
                                startService(serviceIntent) // Qué: Dispara normal puro. Para qué: Idem. Por qué: Idem.
                            } // Qué: Fin IF OS. Para qué: N/A. Por qué: N/A.
                        } // Qué: Fin seguro puro. Para qué: N/A. Por qué: N/A.
                    } else if (message == "STOP_MONITORING") { // Qué: Comando OFF Python. Para qué: Auto apagar. Por qué: Tesis pura.
                        if (MonitoringState.isMonitoring.value) { // Qué: Seguro OFF puro. Para qué: Idem. Por qué: Idem.
                            val serviceIntent = Intent(this, FallDetectionService::class.java) // Qué: Flecha Servicio pura asíncrona nata OS Android. Para qué: Idem. Por qué: Idem pura.
                            stopService(serviceIntent) // Qué: Guillotina Servicio Central puro asíncrono nato OS Android. Para qué: Matar IA pura. Por qué: Stop pura nativa.
                        } // Qué: Fin IF OFF puro. Para qué: N/A. Por qué: N/A.
                    } // Qué: Fin ELSE IF puro. Para qué: N/A. Por qué: N/A.
                } // Qué: Fin Bucle Dios Servidor puro asíncrono nato OS Android base interna general médica lógica. Para qué: N/A. Por qué: N/A.
            } catch (e: Exception) { // Qué: Atrapa Crash Red puro. Para qué: Resiliencia pura asíncrona. Por qué: Idem.
                Log.e("UDP_LISTENER", "Error: ${e.message}") // Qué: Pinta rojo dev puro asíncrono. Para qué: Logcat puro asíncrono. Por qué: Debug.
            } // Qué: Fin Try Red puro. Para qué: N/A. Por qué: N/A.
        } // Qué: Fin Hilo Demonio puro asíncrono nato OS Android. Para qué: N/A. Por qué: N/A.
    } // Qué: Fin creador Servidor UDP puro asíncrono nato OS Android. Para qué: N/A. Por qué: N/A.
} // Qué: Fin Clase principal pura asíncrona nata OS Android base interna general médica lógica. Para qué: N/A. Por qué: N/A.
