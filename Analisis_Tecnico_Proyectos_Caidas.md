# Documentación Técnica Detallada: Análisis de Proyectos de Detección de Caídas

Este documento presenta una auditoría exhaustiva y un análisis arquitectónico de cuatro proyectos Android diseñados para la detección de caídas. A petición de los requerimientos de auditoría, se hace especial énfasis técnico en los módulos de **Monitoreo en Tiempo Real**, **Generación y Estructura JSON**, y la **Reconstrucción Forense en Python** para cada uno de los ecosistemas.

---

## 1. Proyecto: AplicacionEdgeImpulse17 (17 Clases)
**Ubicación:** `C:\Users\HECTO\AndroidStudioProjects\AplicacionEdgeImpulse17`

### 1.1 Arquitectura de Inferencia
Utiliza el SDK de Edge Impulse en C++ a través de JNI. El sensor reúne 300 floats (100 capturas x 3 ejes) durante 2 segundos y los envía al código nativo, donde la función matemática devuelve una de las 17 clases detectadas (caídas vs actividades diarias).

### 1.2 Sistema de Monitoreo (Logging)
- **Ejecución Persistente:** El monitoreo ocurre de manera ininterrumpida por 120 segundos, gobernado por un `CountDownTimer`. Se garantiza su supervivencia incluso con la pantalla apagada mediante un `PARTIAL_WAKE_LOCK`.
- **Rendimiento de Memoria:** Para los gráficos en vivo del monitoreo, el historial del acelerómetro emplea arreglos primitivos (`LongArray`, `FloatArray`) funcionando como un "Ring Buffer". Esto evita la constante instanciación de objetos de tipo `SensorEventData`, reduciendo el trabajo del recolector de basura (GC) y previniendo la congelación del Main Thread. Además, la frecuencia de actualización gráfica se limitó a 2 segundos con aceleración de hardware desactivada para cuidar la memoria GPU.

### 1.3 Estructura del JSON Exportado
Toda la telemetría se condensa al presionar "Descargar Datos" en el archivo **`datos-monitoreo-edgeimpulse17-clases.json`**. Su interior almacena:
- `sessionStartIso` / `durationSeconds`: Metadatos cronológicos.
- `predictionHistory`: Arreglo temporal que indica el segundo exacto y la clase inferida (ej. `{"timeSeconds": 2, "className": "fall_backward"}`).
- `sensorHistory`: El salvavidas forense. Un volcado brutal de todas las muestras del acelerómetro capturadas en bruto, estructuradas como `{"timeOffsetMillis": 1500, "x": -0.4, "y": 9.8, "z": 0.1}`.

### 1.4 Herramientas Python (`python_tools/`)
La herramienta consume el JSON para crear una auditoría visual exacta:
- **Traducción al Vuelo:** El script de Python detecta la salida del modelo nativo (ej. `going_down_stairs`) y la traduce visualmente ("Bajando escaleras") en la interfaz de la gráfica MP4.
- **Gráficos Independientes:** Genera dos archivos MP4 a 1080p y 30FPS. El de la `linea_tiempo_monitoreo.mp4` que mapea las 17 clases en el Eje Y contra el Tiempo en X, y el `acelerometro_monitoreo.mp4` que anima las variables físicas X, Y, Z.
- **Protección de Hilos:** El renderizado de Matplotlib opera en un `threading.Thread` ajeno a la interfaz Tkinter para evitar los cierres forzados de "(No Responde)" en Windows.

---

## 2. Proyecto: AplicacionEdgeImpulseDeteccionCaidas9clases (9 Clases)
**Ubicación:** `C:\Users\HECTO\AndroidStudioProjects\AplicacionEdgeImpulseDeteccionCaidas9clases`

### 2.1 Arquitectura de Inferencia
Similar a la versión 17, pero operando con un modelo más compacto de 9 clases. Su principal optimización recae en la corrección de *falsos positivos post-caída*, exigiendo 3 ventanas continuas al 90% de confianza, junto con un vaciado de buffer (`.fill(0f)`) y un cooldown estricto de 30 segundos tras cancelar una alerta.

### 2.2 Sistema de Monitoreo (Logging)
- Al igual que la versión de 17 clases, el registro de la sesión en curso reside en un archivo estático `monitoring_log.json` dentro de `filesDir`.
- **I/O Asíncrono Delegado:** El proceso de construir el objeto JSON y escribirlo en memoria flash (NAND) es una tarea lenta. Aquí, el `MonitoringLogManager` confía todas las escrituras a disco a un Executor secundario (`ioExecutor`). De esta manera, el sensor a 50Hz jamás se pausa esperando a que el archivo termine de guardarse.

### 2.3 Estructura del JSON Exportado
Genera un archivo con nomenclatura propia. Pese a que el modelo infiere menos etiquetas, la densidad del `sensorHistory` es la misma. Esto permite comprobar si las fluctuaciones físicas de un desmayo (`fall_syncope`) se grabaron de manera correcta durante los primeros milisegundos del impacto.

### 2.4 Herramientas Python (`python_tools/`)
- **Autodescubrimiento e Instalación:** Si el equipo del usuario es virgen, el archivo `interfaz_grafica.py` realiza un `pip install` interno para librerías como `numpy` e `imageio-ffmpeg`.
- **Aceleración por Hardware y Fallbacks:** A la hora de compilar el video con FFmpeg, el script intenta forzar el uso del códec `h264_nvenc` (tarjetas NVIDIA). Si falla, recae en el encoder `libx264` de CPU configurado en modo `ultrafast`. Si el entorno no soporta la compilación MP4, la herramienta captura la excepción matemáticamente y genera un formato animado `.gif` usando PillowWriter, garantizando que el usuario jamás se quede sin la evidencia visual.

---

## 3. Proyecto: tflite-keras-9class-app (9 Clases - Kotlin/LiteRT)
**Ubicación:** `C:\Users\HECTO\AndroidStudioProjects\tflite-keras-9class-app`

### 3.1 Arquitectura de Inferencia
Aquí se abandona Edge Impulse en favor de TensorFlow Lite nativo, Kotlin 2.0 y Jetpack Compose. Destaca el uso del `DataPreprocessor.kt` que emula el escalado "Z-Score" inyectando valores de media y desviación de un JSON paralelo en memoria, antes de ejecutar la red neuronal cargada mediante `MappedByteBuffer`.

### 3.2 Sistema de Monitoreo (Sliding Window & Throttling)
- **Monitoreo sin Puntos Ciegos:** A diferencia de Edge Impulse que espera 2 segundos cerrados, aquí se emplea un *Sliding Window*. Tras juntar 151 muestras (3 segundos), el buffer descarta sólo las 50 muestras más antiguas (1 segundo). Esto permite una inyección ininterrumpida al log de monitoreo *cada segundo exacto*.
- **Optimización en Compose (Throttling):** Para poder pintar el gráfico del acelerómetro sin trabar la hiper-reactividad de Jetpack Compose, el componente de recolección física genera una barrera ("Throttle"). Solo clona la variable reactiva del acelerómetro hacia la Interfaz Gráfica de Usuario (StateFlow) cada 12 muestras recibidas (~4 veces por segundo visuales), manteniendo un Main Thread despejado.

### 3.3 Estructura del JSON Exportado
El archivo **`datos-monitoreo-tensorflow-keras-9-clases.json`** encapsula las predicciones con una fidelidad superior. Debido a la ventana deslizante, el arreglo de `predictionHistory` ahora contiene un registro consecutivo garantizado (`timeSeconds`: 1, 2, 3, 4...). Esto facilita enormemente las auditorías de sincronía temporal al analizar a qué milisegundo exacto (`timeOffsetMillis`) se cruza una alteración de Gravedad con la inferencia arrojada por TFLite.

### 3.4 Herramientas Python (`python_tools/`)
- En este script, se resolvieron problemas críticos de Matplotlib. Cuando el historial comenzaba en 0 segundos, Matplotlib colapsaba (`array is 1-dimensional`). La solución inyectada consistió en inicializar y alimentar matrices bidimensionales vacías (`np.empty((0, 2))`) hacia los *Scatters* del gráfico mientras el tiempo llega al primer segundo.
- Al igual que los demás proyectos, el canvas se redimensiona autocalculando su "padding" (`pack()`), y adquiere la API *DPI Awareness* de Windows para evitar difuminados en textos.

---

## 4. Proyecto: AplicacionTensorFlowAndKeras17 (17 Clases - Kotlin/LiteRT)
**Ubicación:** `C:\Users\HECTO\AndroidStudioProjects\AplicacionTensorFlowAndKeras17`

### 4.1 Arquitectura de Inferencia
El ecosistema final, con 17 clases neuronales Conv1D puras en Kotlin. Demanda que el Buffer de datos reorganice manualmente los arreglos 3D para pasarlos a formato plano estratificado (`[X1...X151, Y1...Y151, Z1...Z151]`), emulando perfectamente las capas *Reshape* y *Permute* del modelo en Python.

### 4.2 Sistema de Monitoreo (Sincronización y Memoria Extrema)
- **Problema Masivo del GC:** Al operar una interfaz Compose a 50Hz, la iteración previa usaba el objeto de concurrencia de Java `CopyOnWriteArrayList` en los StateFlows. Esto generaba un estrangulamiento masivo de memoria al crear cientos de clones inútiles por segundo, colapsando el sistema invariablemente en el segundo 44 de sesión.
- **La Solución Estructural (`MonitoringLogManager.kt`):** 
  Se aislaron las bases de datos. 
  1. Se creó `fullSensorHistory`: Un arreglo protegido mediante bloque de semáforo (`synchronized(sensorLock)`) que traga de forma ilimitada la telemetría forense. 
  2. Se creó `displaySensorBuffer`: Un arreglo circular truncado a máximo 500 índices, que es el único que la Pantalla de Compose tiene permitido leer. 
  Al separar lo gráfico de la carga pura, la App logra las 17 inferencias de forma fluida logrando una sesión perpetua sin congelamientos ni colisiones de hilos.

### 4.3 Estructura del JSON Exportado
Esta arquitectura de doble buffer garantiza que, al presionar Exportar (`datos-monitoreo-tensorflow-keras-17-clases.json`), el archivo tome el `fullSensorHistory` intacto, guardando cada micro de segundo de los 120 de la sesión, mientras que las variables en tiempo real como `alertsTriggered` o `fallCount` exponen la eficiencia bruta de la red frente a los falsos positivos.

### 4.4 Herramientas Python (`python_tools/`)
- **Precisión Temporal Extrema:** Aquí se inyectó una regla de hierro (`MultipleLocator(1)`) en la gráfica de Línea de Tiempo. Esto fuerza a la librería a trazar una cuadrícula que cruce verticalmente la gráfica **exactamente cada un segundo**. De este modo, la animación H.264 exportada emula una regla matemática visual, lo cual permite auditar que las predicciones de Compose y TFLite verdaderamente hayan arrojado la clase calculada cada 1000 milisegundos tras la técnica de "Sliding Window".
- Además de conservar la aceleración de hardware `h264_nvenc` y el encapsulado `yuv420p` nativo, la herramienta limita su visualización retrospectiva de la cuadrícula a 15 segundos para no agolpar datos, empujando la cámara hacia adelante perpetuamente hasta terminar el procesamiento.
