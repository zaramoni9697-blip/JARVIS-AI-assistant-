package com.example.util

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.provider.AlarmClock
import android.provider.Settings
import android.util.Log
import com.example.data.model.DeviceStatus

class DeviceController(private val context: Context) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
    private var isTorchOn: Boolean = false

    fun getDeviceStatus(): DeviceStatus {
        val batteryFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryIntent = context.registerReceiver(null, batteryFilter)
        val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: 100
        val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: 100
        val batteryPct = if (level >= 0 && scale > 0) ((level.toFloat() / scale.toFloat()) * 100).toInt() else 100
        val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL

        val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        actManager?.getMemoryInfo(memInfo)
        val totalRamMb = (memInfo.totalMem) / (1024 * 1024)
        val freeRamMb = (memInfo.availMem) / (1024 * 1024)

        val currentVol = audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC) ?: 5
        val maxVol = audioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC) ?: 15
        val volPercent = if (maxVol > 0) ((currentVol.toFloat() / maxVol.toFloat()) * 100).toInt() else 50

        return DeviceStatus(
            batteryLevel = batteryPct,
            isCharging = isCharging,
            flashlightOn = isTorchOn,
            volumePercent = volPercent,
            freeRamMb = freeRamMb,
            totalRamMb = totalRamMb,
            deviceModel = "${Build.MANUFACTURER.replaceFirstChar { it.uppercase() }} ${Build.MODEL}",
            androidVersion = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"
        )
    }

    fun toggleFlashlight(enable: Boolean): Result<Boolean> {
        return try {
            val cameraId = cameraManager?.cameraIdList?.firstOrNull()
            if (cameraId != null && cameraManager != null) {
                cameraManager.setTorchMode(cameraId, enable)
                isTorchOn = enable
                Result.success(enable)
            } else {
                Result.failure(IllegalStateException("Camera flash not available on this device"))
            }
        } catch (e: CameraAccessException) {
            Log.e("DeviceController", "Flashlight error", e)
            Result.failure(e)
        } catch (e: Exception) {
            Log.e("DeviceController", "Flashlight unexpected error", e)
            Result.failure(e)
        }
    }

    fun setVolumePercent(percent: Int): Result<Int> {
        return try {
            val clamped = percent.coerceIn(0, 100)
            val maxVol = audioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC) ?: 15
            val target = ((clamped / 100f) * maxVol).toInt().coerceIn(0, maxVol)
            audioManager?.setStreamVolume(AudioManager.STREAM_MUSIC, target, AudioManager.FLAG_SHOW_UI)
            Result.success(clamped)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun createAlarm(message: String, hour: Int, minutes: Int): Intent {
        return Intent(AlarmClock.ACTION_SET_ALARM).apply {
            putExtra(AlarmClock.EXTRA_MESSAGE, message)
            putExtra(AlarmClock.EXTRA_HOUR, hour)
            putExtra(AlarmClock.EXTRA_MINUTES, minutes)
            putExtra(AlarmClock.EXTRA_SKIP_UI, false)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    fun createTimer(message: String, seconds: Int): Intent {
        return Intent(AlarmClock.ACTION_SET_TIMER).apply {
            putExtra(AlarmClock.EXTRA_MESSAGE, message)
            putExtra(AlarmClock.EXTRA_LENGTH, seconds)
            putExtra(AlarmClock.EXTRA_SKIP_UI, false)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    fun openSettings(action: String = Settings.ACTION_SETTINGS): Intent {
        return Intent(action).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    fun openApp(packageName: String): Intent? {
        val intent = context.packageManager.getLaunchIntentForPackage(packageName)
        intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return intent
    }

    fun getInstalledAppPackages(): List<Pair<String, String>> {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveList = pm.queryIntentActivities(intent, 0)
        return resolveList.mapNotNull {
            val label = it.loadLabel(pm).toString()
            val pkg = it.activityInfo?.packageName
            if (pkg != null) label to pkg else null
        }.distinctBy { it.second }.take(30)
    }

    fun getStorageSummary(): String {
        return try {
            val path = Environment.getDataDirectory()
            val stat = StatFs(path.path)
            val blockSize = stat.blockSizeLong
            val totalBlocks = stat.blockCountLong
            val availableBlocks = stat.availableBlocksLong

            val totalGb = (totalBlocks * blockSize) / (1024 * 1024 * 1024)
            val freeGb = (availableBlocks * blockSize) / (1024 * 1024 * 1024)
            val usedGb = totalGb - freeGb
            "${usedGb}GB used / ${totalGb}GB (${freeGb}GB free)"
        } catch (e: Exception) {
            "Storage info unavailable"
        }
    }
}
