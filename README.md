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

---

Autor: Hector (Licenciatura en Tecnologias Computacionales)  
Ultima actualizacion: Abril 2026
