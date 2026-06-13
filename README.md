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

### Optimizacion de graficos y memoria (Anti-congelamiento)
- Se separaron los datos del sensor en dos buffers:
  - `fullSensorHistory`: almacena **todas** las muestras para la exportacion JSON completa.
  - `displaySensorBuffer`: buffer circular para el grafico en pantalla.
- Se implemento **throttle de publicacion**: el `StateFlow` del sensor solo se actualiza cada 12 muestras (~4Hz visual), evitando recomposiciones excesivas de Compose que causaban ralentizaciones.
- Se elimino el uso de `CopyOnWriteArrayList` (el cual generaba un grave problema de *Garbage Collection* y congelaba la app alrededor del segundo 44 al crear miles de arreglos por segundo). En su lugar, se emplean listas mutables nativas protegidas por bloques `synchronized` para asegurar rendimiento maximo sin colisiones de hilos.

### Inferencia de Ventana Deslizante (Sliding Window)
- Para cumplir con el requerimiento de graficar y detectar actividad **cada 1 segundo**, se modifico la logica del `SensorHandler`.
- Una vez llena la ventana de 151 muestras requerida por TensorFlow, el buffer ya no se vacia desde cero. En cambio, realiza un desplazamiento de datos (shift) retrocediendo solo 50 muestras (equivalente a 1 segundo a 50Hz).
- Esto permite al modelo tener siempre una ventana solida de 3 segundos de historial fisico para analizar, pero arrojando resultados continuamente **segundo a segundo** de manera fluida.

### Exportacion completa de datos del acelerometro
- El reporte JSON ahora incluye el campo `sensorHistory` con **todos** los datos brutos del acelerometro (offset en ms, ejes X/Y/Z). El archivo JSON guarda la telemetría exacta en el milisegundo en que ocurre, asegurando un registro ininterrumpido sin pérdida de datos.
- Esto permite reconstruir graficos exactos en Python usando la nueva herramienta grafica.

### Estandarizacion de Intervalos Visuales a 1 Segundo
- Se ajustaron los Canvas nativos en Jetpack Compose (`TimelineChart` y `SensorChart`) para que las cuadriculas y marcas de tiempo de las graficas de monitoreo se dibujen estrictamente cada **1 segundo**.
- Esto garantiza paridad visual con los videos generados en Python, permitiendo un analisis preciso segundo a segundo tanto en la aplicacion Android como en la herramienta de escritorio, manteniendo intacta la recoleccion de los datos en bruto en el JSON.

### 🐍 Herramienta Python de Reconstrucción Visual (JSON a MP4)
Se diseñó un módulo externo de Python (ubicado en la carpeta `python_tools/`) para leer el JSON exportado y generar animaciones precisas.

**Características Técnicas de Generación de Video:**
- **Alta Definición y Fluidez (1080p a 30 FPS):** La herramienta matemática fue recalibrada para generar videos fluidos animando el recorrido del sensor a 30 cuadros por segundo. Su lienzo está escalado a 16:9 con 120 DPI y 8000 kbps de bitrate, lo que garantiza una salida nítida exactamente a 1920x1080 píxeles, sin textos borrosos.
- **Cuadrícula Matemática Precisa:** Se configuró un Localizador Múltiple (`MultipleLocator(1)`) en ambos gráficos para forzar estrictamente el dibujo de los ejes y líneas de cuadrícula en intervalos exactos de **1 segundo**. Esto evita las auto-escalas de Matplotlib (saltos de 5s o 10s) permitiendo analizar la telemetría de caídas segundo a segundo. Además, se ajustó el eje X (rotación de 45 grados y ventana visual de 15 segundos) para mejorar la legibilidad y evitar el solapamiento de etiquetas en conjuntos de datos grandes.
- **Compatibilidad Universal MP4 (yuv420p):** La herramienta inyecta comandos avanzados a FFmpeg (`-vcodec libx264`, `-pix_fmt yuv420p`, `-profile:v high`) forzando una codificación de color estándar. Esto repara el error típico de matplotlib donde los videos MP4 generados bloquean los controles del reproductor (impidiendo adelantar o atrasar). Ahora son nativamente compatibles con QuickTime, Windows Media y navegadores web.
- **Procesamiento Asíncrono (Anti-Congelamiento) y Aceleración por Hardware:** La codificación de video se delega a un hilo en segundo plano (`threading`) operando bajo el motor gráfico `Agg` de Matplotlib. Para maximizar la velocidad de exportación, implementa soporte primario para aceleración de hardware por GPU (NVIDIA `h264_nvenc`) con fallback automático a CPU (`libx264` con preset `ultrafast`). Esto protege el hilo principal de Tkinter, asegurando que la interfaz no se congele ("No Responde") durante renderizados largos.
- **Interfaz DPI-Aware Autoadaptable:** Al invocar `SetProcessDpiAwareness(1)`, la interfaz se dibuja usando los píxeles reales de Windows, eliminando el difuminado tradicional de Tkinter. Sus dimensiones se autocalculan dinámicamente (`pack` padding) para no cortar textos en monitores 2K o 4K.
- **Sin Dependencias de Sistema:** La herramienta instala y utiliza el paquete `imageio-ffmpeg` para descargar un binario portátil de FFmpeg interno, eliminando la necesidad de que el usuario lo instale manualmente en el sistema operativo.
- **Tolerancia a fallos (Fallback a GIF):** Si ocurre alguna excepción crítica al codificar en H.264 (.mp4), el bloque `try-except` captura el fallo y delega la tarea a `PillowWriter` para generar una animación en formato `.gif` de respaldo.
- **Prevención de Bugs Gráficos:** Para evitar los cierres forzados (`array is 1-dimensional`) de Matplotlib al inicializar la gráfica cuando aún no hay puntos detectados, se inyectan matrices bidimensionales vacías mediante `np.empty((0, 2))`.

**Instalación y Uso Automático:**
La herramienta contiene lógica de autodescubrimiento. Si falta alguna librería (`matplotlib`, `numpy`, `Pillow`, `imageio-ffmpeg`), invocará a `pip` internamente para instalarla y se reiniciará automáticamente.

1. Entra a la carpeta `python_tools/`.
2. Ejecuta la interfaz gráfica haciendo doble clic o usando la terminal:
```bash
python interfaz_grafica.py
```
3. La herramienta creará automáticamente las carpetas `input_json/` y `output_videos/`.
4. Coloca **solo un archivo JSON** en la carpeta `python_tools/input_json/`.
5. Presiona el boton verde "Generar Videos" en la interfaz.
6. Los videos MP4 generados (`TensorFlowKeras17_linea_tiempo_monitoreo.mp4` y `TensorFlowKeras17_acelerometro_monitoreo.mp4`) apareceran en `output_videos/`.

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


## 🔬 Integración con Orquestador Multimodelo (Actualización)
Esta aplicación fue modificada para operar simultáneamente con otros 3 modelos de Inteligencia Artificial en un solo dispositivo (Poco F7) durante protocolos de investigación científica.

### Mejoras Críticas Implementadas:
1. **Independencia del Servicio (DummyForegroundService)**: Se separó la lógica de inmunidad en segundo plano de sus servicios anteriores, asegurando un canal de notificación exclusivo (monitoreo_channel_high) para forzar la notificación flotante permanente.
2. **Permisos Híbridos en Manifest**: A los permisos previos de salud (health) se les sumó la declaración explícita de DATA_SYNC en el manifiesto para el DummyForegroundService.
3. **Inyección en Ciclo de Vida**: El archivo MainActivity.kt fue parcheado para arrancar este proceso en segundo plano en onResume(), blindando la lectura ininterrumpida del acelerómetro a 100Hz.
4. **Sincronización UDP**: Capacidad de iniciar/detener la recolección mediante START_MONITORING / STOP_MONITORING vía Broadcast UDP en toda la subred.

### ⏱️ Rendimiento de Generación de Videos (Aceleración AMF)
Durante las pruebas de campo en un equipo HP Victus (AMD Radeon RX 6550M), el renderizado de gráficos de la telemetría tardó lo siguiente:
* **Video de Línea de Tiempo (Predicciones)**: ~46 minutos (241.92 MB)
* **Video de Acelerómetro (Ejes X,Y,Z)**: ~30 minutos (92.19 MB)
* **Tiempo Total por Ciclo (120s)**: ~1 hora y 16 minutos.
> Nota: El incremento de tiempo respecto a modelos más pequeños se debe a la inmensa cantidad de cálculos visuales requeridos para trazar la inferencia de 17 clases por segundo usando Matplotlib.
