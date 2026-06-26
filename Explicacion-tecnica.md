# Arquitectura y Flujo de Detección de Caídas: TensorFlow Lite (17 Clases)

Este documento detalla la estructura lógica y el flujo de datos del repositorio `AplicacionTensorFlowAndKeras17`. Este proyecto implementa una inferencia de *Machine Learning* nativa utilizando un modelo de Inteligencia Artificial entrenado en Python (TensorFlow/Keras) y exportado para dispositivos móviles.

## 1. El Núcleo de Inferencia: Desde `.keras` hasta `.tflite`
La red neuronal original fue entrenada en Python guardando su estado en formato `.keras` (o H5). Sin embargo, Android no puede leer este formato pesado.
* **El Puente (TFLite):** En Python, el modelo fue convertido usando `tf.lite.TFLiteConverter` hacia un archivo de formato *FlatBuffer* (`model_17.tflite`). Este archivo es ultra-ligero y optimizado para procesadores ARM de teléfonos móviles.
* **Alojamiento:** El archivo `.tflite` se encuentra inyectado directamente en la carpeta `assets/` de la aplicación Android.
* **Cargador (Interpreter):** El archivo `FallDetectionClassifier.kt` utiliza el motor `Interpreter` de `org.tensorflow.lite`. Mediante la técnica de mapeo de memoria en disco (`FileChannel.MapMode.READ_ONLY`), el modelo se carga instantáneamente en la RAM sin ahogar el Garbage Collector.

## 2. Flujo de Datos para la Detección de Clases

El ciclo de vida de la detección sigue el patrón arquitectónico Productor-Consumidor pero se administra enteramente a través de un **Servicio en Segundo Plano** (`FallDetectionService.kt`) para sobrevivir si la pantalla se apaga.

### A. Adquisición (Productor) - `SensorHandler.kt`
1. **Delegación de Hardware:** A diferencia de Edge Impulse (donde el Activity lee el sensor), aquí el `SensorHandler` es una clase separada y dedicada exclusivamente a chupar datos del Acelerómetro a 50Hz.
2. **Buffer Deslizante (Sliding Window):** Los eventos (X, Y, Z) se almacenan en un `FloatArray` plano. Cuando se llega a 150 muestras (1 segundo de datos a 50Hz * 3 ejes), la ventana se desplaza y avisa al Servicio que hay datos frescos.

### B. Preparación y Escalado (DSP) - `DataPreprocessor.kt`
1. **Limpieza Matemática:** Los modelos Keras/TensorFlow son estrictamente dependientes de cómo fueron entrenados en Python. Si en Python se estandarizaron los datos (Media 0, Varianza 1), en Kotlin hay que hacer lo mismo.
2. **Normalización:** Este módulo toma el buffer crudo del sensor y aplica escalado para que los tensores alimentados al motor TFLite no revienten matemáticamente.

### C. Inferencia (Productor-Consumidor) - `FallDetectionService.kt`
1. **Cola Asíncrona:** Cuando el `SensorHandler` entrega un bloque de datos listo, el servicio lo mete a una cola ejecutada por un `ExecutorService` (Hilo oscuro solitario). Esto asegura que la matemática de matrices no paralice el Hilo Principal (UI Thread).
2. **Clasificación Nativa:** Llama a `classifier.classify()`, el cual inyecta los datos al `Interpreter` nativo en C++ subyacente de Google TensorFlow.
3. **Array de Probabilidades:** TFLite devuelve un arreglo de 17 posiciones (`FloatArray(17)`). Cada posición contiene un valor entre 0.0 y 1.0 (Softmax). La posición con el número más alto dicta qué clase cree la IA que ocurrió.

### D. Evaluación (El Juez Supremo) - `FallDetectionService.kt (processInference)`
1. **Extracción del Ganador:** Se extrae el índice (0 a 16) con la mayor probabilidad y se asocia a su etiqueta (Ej: `14 -> fall_sideward_left`).
2. **Criterio de Alarma:**
   * ¿La etiqueta detectada pertenece a las 8 clases rojas de caída (`FALL_CLASSES`)?
   * ¿La confianza supera el umbral matemático del `85%` (`FALL_THRESHOLD`)?
   * ¿El sistema NO está ya en estado de alarma (`!isAlertActive`)?
3. **Detonación SOS:** Si todo es verdadero, el Servicio invoca al `EmergencyProtocol` o lanza el `AlertActivity` para despertar la pantalla e iniciar el pánico.

## 3. Gestor de Telemetría: `MonitoringLogManager.kt`
El corazón de la tesis IoT. Graba el estado del experimento al disco para su análisis final en Python.
* **Cero Asignaciones:** En un bucle de 50Hz, crear objetos en RAM obliga a Android a pausar la app para limpiar basura (`Garbage Collection`). Este Manager utiliza Arrays primitivos puros estilo C++ (LongArray, FloatArray) pre-asignados a miles de celdas para evitar crear basura.
* **Bloqueo Hilos (Thread-Safety):** Múltiples procesos tocan los logs simultáneamente. El archivo usa variables Atómicas y bloqueos de Mutex (`@Synchronized`) para prevenir corrupción de RAM.
* **Exportación Final:** Al terminar los 120 segundos del protocolo experimental, el archivo se formatea a JSON (`monitoring_log.json`) y se escupe mediante Scoped Storage al folder público `Downloads/` del Android, listo para extraer por cable USB.

## Resumen del Flujo de Ejecución (Pipeline):
1. **Sensor (50Hz)** -> `SensorHandler.kt`
2. **Ventana Llena (150 floats)** -> `FallDetectionService.kt`
3. **Escalado Matemático** -> `DataPreprocessor.kt`
4. **Hilo Secundario (Executor)** -> `FallDetectionClassifier.kt`
5. **TensorFlow Lite Interpreter** -> Lee el modelo entrenado desde archivo plano (derivado de `.keras`)
6. **Retorno de Matriz Softmax (17 clases)** -> `FallDetectionService.kt`
7. **Juez Supremo SOS (>0.85 + Clase Roja)** -> `AlertActivity.kt`
8. **Paralelo:** `MonitoringLogManager.kt` escupe la traza térmica y lógica al disco en formato JSON puro.
