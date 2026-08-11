package com.fedeveloper95.games.services

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.view.accessibility.AccessibilityEvent

class GameBubbleAccessibilityService : AccessibilityService() {

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "com.fedeveloper95.games.TAKE_SCREENSHOT") {
                performGlobalAction(GLOBAL_ACTION_TAKE_SCREENSHOT)
            }
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        try {
            val info = AccessibilityServiceInfo().apply {
                eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
                feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
                flags = 0
                notificationTimeout = 250
            }
            this.serviceInfo = info

            val filter = IntentFilter("com.fedeveloper95.games.TAKE_SCREENSHOT")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                registerReceiver(receiver, filter, RECEIVER_EXPORTED)
            } else {
                registerReceiver(receiver, filter)
            }
        } catch (e: Exception) {}
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        try {
            if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
                val packageName = event.packageName?.toString() ?: return
                val intent = Intent("com.fedeveloper95.games.PACKAGE_CHANGED").apply {
                    putExtra("PACKAGE_NAME", packageName)
                }
                sendBroadcast(intent)
            }
        } catch (e: Exception) {}
    }

    override fun onInterrupt() {}

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(receiver)
        } catch (e: Exception) {}
    }
}