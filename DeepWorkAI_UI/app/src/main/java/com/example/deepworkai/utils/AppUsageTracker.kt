package com.example.deepworkai.utils

import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Process
import android.provider.Settings
import com.example.deepworkai.models.DistractionApp

object AppUsageTracker {

    fun hasUsageStatsPermission(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.unsafeCheckOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun requestUsageStatsPermission(context: Context) {
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    fun getUsedApps(context: Context, startTime: Long, endTime: Long): List<DistractionApp> {
        if (!hasUsageStatsPermission(context)) return emptyList()

        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        
        // queryEvents gives precise start/stop events
        val events = usageStatsManager.queryEvents(startTime, endTime)
        val usageMap = mutableMapOf<String, Long>() // Map package name to foreground time
        val lastEventTimeMap = mutableMapOf<String, Long>()

        val event = android.app.usage.UsageEvents.Event()
        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            val pkg = event.packageName
            
            if (event.eventType == android.app.usage.UsageEvents.Event.ACTIVITY_RESUMED) {
                lastEventTimeMap[pkg] = event.timeStamp
            } else if (event.eventType == android.app.usage.UsageEvents.Event.ACTIVITY_PAUSED || 
                       event.eventType == android.app.usage.UsageEvents.Event.ACTIVITY_STOPPED) {
                val start = lastEventTimeMap[pkg]
                if (start != null && start > 0) {
                    val duration = event.timeStamp - start
                    usageMap[pkg] = (usageMap[pkg] ?: 0L) + duration
                    lastEventTimeMap.remove(pkg) // Reset
                }
            }
        }
        
        // Close any apps that were still resumed at the end of tracking
        lastEventTimeMap.forEach { (pkg, start) ->
            if (start > 0) {
                val duration = endTime - start
                usageMap[pkg] = (usageMap[pkg] ?: 0L) + duration
            }
        }

        val prefs = context.getSharedPreferences("AppTrackingPrefs", Context.MODE_PRIVATE)
        val whitelistedPackages = prefs.getStringSet("tracked_apps", emptySet()) ?: emptySet()

        val packageManager = context.packageManager
        val resultList = mutableListOf<DistractionApp>()

        for ((pkgName, timeMs) in usageMap) {
            val minutes = Math.ceil(timeMs / 60000.0).toInt().coerceAtLeast(1)
            val isSystemOrLauncher = pkgName.contains("launcher") || pkgName.contains("systemui") || pkgName == context.packageName
            // Process any app that is used for more than 1 second, ignoring system/launcher
            if (timeMs > 1000 && !isSystemOrLauncher) {
                val appName = try {
                    val appInfo = packageManager.getApplicationInfo(pkgName, 0)
                    packageManager.getApplicationLabel(appInfo).toString()
                } catch (e: Exception) {
                    pkgName
                }

                resultList.add(DistractionApp(appName, minutes))
            }
        }

        return resultList.sortedByDescending { it.usageTime }
    }
}
