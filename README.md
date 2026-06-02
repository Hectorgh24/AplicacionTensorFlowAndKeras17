# Aplicacion TensorFlow and Keras - 17 Clases

Aplicacion movil Android desarrollada en Kotlin + Jetpack Compose para monitoreo de actividad humana con acelerometro y deteccion de caidas en tiempo real usando TensorFlow Lite.

## Indice
1. [Descripcion general](#descripcion-general)
2. [Recursos de IA (17 clases)](#recursos-de-ia-17-clases)
3. [Clases que predice el modelo](#clases-que-predice-el-modelo)
4. [Instalacion y ejecucion](#instalacion-y-ejecucion)
5. [Estructura del proyecto](#estructura-del-proyecto)
6. [Permisos y comportamiento de emergencia](#permisos-y-comportamiento-de-emergencia)
7. [Monitoreo y visualizacion de graficos](#monitoreo-y-visualizacion-de-graficos)
8. [Registro y exportacion de reportes](#registro-y-exportacion-de-reportes)
9. [Optimizaciones de rendimiento y estabilidad](#optimizaciones-de-rendimiento-y-estabilidad)

## Descripcion general
- La app captura datos del acelerometro a 50 Hz y procesa ventanas de 151 muestras x 3 ejes (453 valores).
- El flujo de inferencia estandariza los datos con `StandardScaler` y ejecuta un modelo TFLite en dispositivo.
- Si la prediccion supera el umbral de confianza (85%) y no corresponde a "Caminando", se activa el flujo de alerta.
- El protocolo de emergencia intenta WhatsApp y despues SMS + llamada telefonica (si hay permisos).

## Recursos de IA (17 clases)
Archivos usados en `app/src/main/assets`:

1. `entrenamiento_17_clases_mejor_modelo.tflite`
2. `scaler_17_clases.json`

Estos dos recursos deben mantenerse sincronizados (modelo y parametros de normalizacion del mismo entrenamiento).

## Clases que predice el modelo
El clasificador devuelve una de estas 17 etiquetas:

1. De pie
2. Levantandose
3. Caminando
4. Corriendo
5. Subiendo
6. Saltando
7. Bajando
8. Acostado
9. Sentandose
10. Caida frontal
11. Caida a la derecha
12. Caida hacia atras
13. Caida contra obstaculo
14. Caida (intentando protegerse)
15. Caida al sentarse
16. Desmayo / Sincope
17. Caida a la izquierda

## Instalacion y ejecucion
### Clonar
```bash
git clone https://github.com/Hectorgh24/AplicacionTensorFlowAndKeras17.git
cd AplicacionTensorFlowAndKeras17
```

### Abrir en Android Studio
- Abre el proyecto y espera sincronizacion de Gradle.
- Compila y ejecuta en dispositivo fisico (recomendado por uso de sensores).

## Estructura del proyecto
Archivos principales:

- `app/src/main/java/com/empresa/aplicaciontensorflowandkeras17/FallDetectionService.kt`: servicio en primer plano para monitoreo continuo.
- `app/src/main/java/com/empresa/aplicaciontensorflowandkeras17/FallDetectionClassifier.kt`: carga del modelo TFLite e inferencia.
- `app/src/main/java/com/empresa/aplicaciontensorflowandkeras17/DataPreprocessor.kt`: lectura de `scaler_17_clases.json` y estandarizacion.
- `app/src/main/java/com/empresa/aplicaciontensorflowandkeras17/SensorHandler.kt`: adquisicion del acelerometro y armado de ventanas.
- `app/src/main/java/com/empresa/aplicaciontensorflowandkeras17/EmergencyProtocol.kt`: intento de WhatsApp, SMS y llamada.
- `app/src/main/java/com/empresa/aplicaciontensorflowandkeras17/MonitoringLogManager.kt`: persistencia de sesion y exportacion de reporte JSON.
- `app/src/main/java/com/empresa/aplicaciontensorflowandkeras17/ui/Screen/`: pantallas principales (`MainScreen`, `MonitorScreen`, `AlertScreen`, `SettingsScreen`, `AppNavigator`).

## Permisos y comportamiento de emergencia
Permisos definidos en `app/src/main/AndroidManifest.xml`:

- `SEND_SMS`
- `CALL_PHONE`
- `INTERNET`
- `SENSOR`
- `POST_NOTIFICATIONS`
- `FOREGROUND_SERVICE`
- `FOREGROUND_SERVICE_HEALTH`

Notas:
- Si no se conceden permisos de SMS o llamada, la app muestra aviso y evita ejecutar esa accion.
- La deteccion se ejecuta con servicio foreground para mantener monitoreo en segundo plano.

## Monitoreo y visualizacion de graficos
La pantalla de monitoreo cuenta con un dashboard visual interactivo compuesto por dos graficos principales, los cuales garantizan un rendimiento fluido en tiempo real sin congelamientos incluso en sesiones prolongadas:

1. **Grafico de Acelerometro (Datos en bruto)**: Muestra los valores de los ejes X, Y, Z capturados por el sensor a 50Hz, dibujando de forma continua mediante un esquema de scroll horizontal para observar con precision la fisica de los movimientos.
2. **Grafico de Inferencia**: Presenta un historial interactivo (scatter plot) de todas las predicciones arrojadas por el modelo TFLite, mapeando el tiempo de operacion contra la actividad o caida detectada. Soporta un amplio registro historico en memoria sin degradar el rendimiento.

## Registro y exportacion de reportes
- La sesion de monitoreo se guarda en `monitoring_log.json` dentro de `filesDir`.
- Desde Ajustes se puede exportar un reporte JSON con prefijo `monitoring_report_`.
- Metricas incluidas: inicio/fin, duracion, ventanas procesadas, caidas detectadas, alertas enviadas, numero de emergencia y ultima prediccion.

## Optimizaciones de rendimiento y estabilidad
Mejoras implementadas para garantizar una operacion confiable del servicio de monitoreo en segundo plano y una experiencia de usuario fluida:

### Temporizador de sesion de 2 minutos
- Cada sesion de monitoreo dura exactamente **120 segundos** y se auto-detiene al finalizar.
- Se muestra un temporizador visual en la pantalla de monitoreo que cambia a rojo en los ultimos 10 segundos.
- Implementado con `CountDownTimer` que actualiza el estado reactivo `MonitoringState.remainingSeconds`.

### WakeLock parcial para ejecucion en segundo plano
- Se adquiere un `PARTIAL_WAKE_LOCK` al iniciar el servicio para mantener la CPU activa incluso con la pantalla apagada.
- Timeout de seguridad de 3 minutos para evitar fugas de recursos.
- Se agrego el permiso `WAKE_LOCK` al `AndroidManifest.xml`.

### Optimizacion de graficos en tiempo real (anti-congelamiento)
- Se separaron los datos del sensor en dos buffers:
  - `fullSensorHistory`: almacena **todas** las muestras para la exportacion JSON completa.
  - `displaySensorBuffer`: buffer circular de 500 puntos para el grafico en pantalla.
- Se implemento **throttle de publicacion**: el `StateFlow` del sensor solo se actualiza cada 12 muestras (~4Hz visual), evitando recomposiciones excesivas de Compose que causaban congelamiento de graficos.
- Se utiliza `CopyOnWriteArrayList` para evitar `ConcurrentModificationException` desde el hilo del sensor.

### Exportacion completa de datos del acelerometro
- El reporte JSON ahora incluye el campo `sensorHistory` con **todos** los datos brutos del acelerometro (offset en ms, ejes X/Y/Z).
- Esto permite reconstruir graficos exactos en Python usando la nueva herramienta gráfica.

### Herramienta Python de Reconstrucción Visual
Se movieron los scripts de Python a una carpeta dedicada para mantener limpio el código de la app.
1. Entra a la carpeta `python_tools/`.
2. Ejecuta la interfaz gráfica haciendo doble clic o usando la terminal:
```bash
python interfaz_grafica.py
```
*(Nota: la herramienta revisará e instalará automáticamente las dependencias necesarias como `matplotlib` o `numpy`).*
3. Coloca **solo un archivo JSON** en la carpeta `python_tools/input_json/`.
4. Presiona el botón "Generar Videos" en la interfaz.
5. Los resultados se guardarán en la carpeta `python_tools/output_videos/`.

### Correccion de navegacion en alertas de caida
- Se cambio `FLAG_ACTIVITY_CLEAR_TOP` por `FLAG_ACTIVITY_SINGLE_TOP` en el intent de alerta.
- Se agrego `android:launchMode="singleTop"` en el `AndroidManifest.xml`.
- Se implemento `onNewIntent()` en `MainActivity` para manejar intents sin recrear la Activity.
- Esto evita que la navegacion se reinicie y saque al usuario de la pantalla de monitoreo cuando se detecta una caida.

### Notificacion persistente
- Se agrego `.setOngoing(true)` a la notificacion del servicio para evitar que el usuario la descarte accidentalmente.

---

Autor: Hector (Licenciatura en Tecnologias Computacionales)  
Ultima actualizacion: Junio 2026
