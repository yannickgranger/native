package com.anonymous.bulwarkmobile

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

// Starts the direct connection again after a reboot and after the app was
// updated, when it is on. Both broadcasts allow a foreground service to start
// from the background, and Android 15 keeps only other service types from
// starting at boot (developer.android.com/about/versions/15/behavior-changes-15#fgs-boot-completed).
// Also takes the service's watchdog alarm, which only a PendingIntent can
// carry while the device is idle (BulwarkDirectPushService.scheduleWatchdog).
class BulwarkDirectPushReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == BulwarkDirectPushService.ACTION_WATCHDOG) {
            BulwarkDirectPushService.watchdogFired()
            return
        }
        if (intent.action != Intent.ACTION_BOOT_COMPLETED && intent.action != Intent.ACTION_MY_PACKAGE_REPLACED) return
        try {
            BulwarkDirectPushService.launch(context)
        } catch (e: RuntimeException) {
            Log.w("BulwarkDirectPush", "Direct push not started after ${intent.action}: ${e.message}")
        }
    }
}
