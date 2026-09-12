package com.jongchan.androidarchi.common.data.remoteConfig

import android.app.ActivityManager
import android.content.Context
import android.util.Log
import java.io.File

/**
 * Remote Config custom signal(`isLowEndDevice`) 용 저사양 단말 판정. (prex-android `DeviceHelperImpl.isLowEndDevice` 이식)
 * RAM 8GB 미만 이면서 CPU 최대 클럭 2048MHz 미만일 때 저사양으로 본다. 판정 실패 시 false.
 */
internal object LowEndDeviceDetector {
    private const val TAG = "RemoteConfigV2"
    private const val LOW_RAM_THRESHOLD_GB = 8.0
    private const val LOW_CPU_THRESHOLD_MHZ = 2048L

    fun isLowEndDevice(context: Context): Boolean = runCatching {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager?.getMemoryInfo(memoryInfo)

        val totalRamInGB = memoryInfo.totalMem / (1024.0 * 1024.0 * 1024.0)
        val cpuMaxFreqInMHz = getMaxCpuFrequencyInKHz() / 1000

        val isLowRam = totalRamInGB < LOW_RAM_THRESHOLD_GB
        val isLowCpu = cpuMaxFreqInMHz < LOW_CPU_THRESHOLD_MHZ

        Log.i(TAG, "Device specs - RAM: ${totalRamInGB}GB, CPU Max: ${cpuMaxFreqInMHz}MHz")
        isLowRam && isLowCpu
    }.onFailure {
        Log.w(TAG, "Device specs check failed: ${it.message}")
    }.getOrElse { false }

    private fun getMaxCpuFrequencyInKHz(): Long {
        val cpuDir = File("/sys/devices/system/cpu")
        val cores = cpuDir.listFiles { f -> f.name.matches(Regex("cpu[0-9]+")) }.orEmpty()
        return cores
            .mapNotNull { core ->
                File(core, "cpufreq/cpuinfo_max_freq").takeIf { it.exists() }
                    ?.readText()?.trim()?.toLongOrNull()
            }
            .maxOrNull() ?: 0L
    }
}
