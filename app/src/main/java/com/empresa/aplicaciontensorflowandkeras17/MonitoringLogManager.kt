package com.empresa.aplicaciontensorflowandkeras17

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

data class MonitoringSessionLog(
    val sessionStartMillis: Long,
    val sessionEndMillis: Long? = null,
    val windowsProcessed: Int = 0,
    val fallCount: Int = 0,
    val alertsTriggered: Int = 0,
    val emergencyNumber: String = "",
    val currentPrediction: String = "Inactivo"
) {
    val durationSeconds: Long
        get() = if (sessionEndMillis != null) {
            (sessionEndMillis - sessionStartMillis) / 1000
        } else {
            (System.currentTimeMillis() - sessionStartMillis) / 1000
        }

    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("sessionStartMillis", sessionStartMillis)
            put("sessionStartIso", isoFormat(sessionStartMillis))
            put("sessionEndMillis", sessionEndMillis ?: JSONObject.NULL)
            put("sessionEndIso", sessionEndMillis?.let { isoFormat(it) } ?: JSONObject.NULL)
            put("durationSeconds", durationSeconds)
            put("windowsProcessed", windowsProcessed)
            put("fallCount", fallCount)
            put("alertsTriggered", alertsTriggered)
            put("emergencyNumber", emergencyNumber)
            put("currentPrediction", currentPrediction)
        }
    }

    companion object {
        private fun isoFormat(timestamp: Long): String {
            val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
            return formatter.format(Date(timestamp))
        }

        fun fromJson(json: JSONObject): MonitoringSessionLog {
            return MonitoringSessionLog(
                sessionStartMillis = json.optLong("sessionStartMillis"),
                sessionEndMillis = if (json.isNull("sessionEndMillis")) null else json.optLong("sessionEndMillis"),
                windowsProcessed = json.optInt("windowsProcessed"),
                fallCount = json.optInt("fallCount"),
                alertsTriggered = json.optInt("alertsTriggered"),
                emergencyNumber = json.optString("emergencyNumber"),
                currentPrediction = json.optString("currentPrediction", "Inactivo")
            )
        }
    }
}

object MonitoringLogManager {
    private val _currentSession = MutableStateFlow<MonitoringSessionLog?>(null)
    val currentSession = _currentSession.asStateFlow()

    private const val LOG_FILE_NAME = "monitoring_log.json"
    private const val EXPORT_PREFIX = "monitoring_report_"

    fun startSession(context: Context, emergencyNumber: String) {
        val session = MonitoringSessionLog(
            sessionStartMillis = System.currentTimeMillis(),
            emergencyNumber = emergencyNumber
        )
        _currentSession.value = session
        saveCurrentSession(context)
    }

    fun recordWindow(context: Context) {
        _currentSession.value?.let {
            _currentSession.value = it.copy(windowsProcessed = it.windowsProcessed + 1)
            saveCurrentSession(context)
        }
    }

    fun recordFall(context: Context) {
        _currentSession.value?.let {
            _currentSession.value = it.copy(fallCount = it.fallCount + 1)
            saveCurrentSession(context)
        }
    }

    fun recordAlert(context: Context) {
        _currentSession.value?.let {
            _currentSession.value = it.copy(alertsTriggered = it.alertsTriggered + 1)
            saveCurrentSession(context)
        }
    }

    fun updatePrediction(context: Context, prediction: String) {
        _currentSession.value?.let {
            _currentSession.value = it.copy(currentPrediction = prediction)
            saveCurrentSession(context)
        }
    }

    fun stopSession(context: Context) {
        _currentSession.value?.let {
            _currentSession.value = it.copy(sessionEndMillis = System.currentTimeMillis())
            saveCurrentSession(context)
        }
    }

    fun loadLastSession(context: Context): MonitoringSessionLog? {
        val file = File(context.filesDir, LOG_FILE_NAME)
        return if (file.exists()) {
            try {
                val json = JSONObject(file.readText())
                MonitoringSessionLog.fromJson(json)
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }

    fun exportReport(context: Context): String? {
        val session = _currentSession.value ?: loadLastSession(context)
        return session?.let {
            val exportFile = File(context.filesDir, "$EXPORT_PREFIX${System.currentTimeMillis()}.json")
            exportFile.writeText(it.toJson().toString(2))
            exportFile.absolutePath
        }
    }

    private fun saveCurrentSession(context: Context) {
        _currentSession.value?.let {
            val file = File(context.filesDir, LOG_FILE_NAME)
            file.writeText(it.toJson().toString(2))
        }
    }
}
