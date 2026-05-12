package com.notifiq.capture

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings

object OemCompatibility {

    enum class OemType {
        STOCK_ANDROID,
        SAMSUNG,
        XIAOMI,
        OPPO_REALME,
        VIVO,
        HUAWEI,
        ONEPLUS,
        OTHER
    }

    fun detectOem(): OemType {
        val manufacturer = Build.MANUFACTURER.lowercase()

        return when {
            manufacturer.contains("samsung") -> OemType.SAMSUNG
            manufacturer.contains("xiaomi") || manufacturer.contains("redmi") -> OemType.XIAOMI
            manufacturer.contains("oppo") || manufacturer.contains("realme") -> OemType.OPPO_REALME
            manufacturer.contains("vivo") -> OemType.VIVO
            manufacturer.contains("huawei") || manufacturer.contains("honor") -> OemType.HUAWEI
            manufacturer.contains("oneplus") -> OemType.ONEPLUS
            manufacturer.contains("stock") -> OemType.STOCK_ANDROID
            else -> OemType.OTHER
        }
    }

    fun getKeepAliveInstructions(oem: OemType): String {
        return when (oem) {
            OemType.XIAOMI -> "Go to Settings → Apps → NotifIQ → Autostart → Enable. Also lock the app in recent apps by swiping down on NotifIQ in the recent apps view."

            OemType.SAMSUNG -> "Go to Settings → Battery → Background usage limits → Never sleeping apps → Add NotifIQ"

            OemType.OPPO_REALME -> "Go to Settings → App management → NotifIQ → Allow auto-launch and Allow background activity"

            OemType.VIVO -> "Go to Settings → Battery → Background app management → Allow NotifIQ"

            OemType.HUAWEI -> "Go to Settings → Battery → App launch → NotifIQ → Manage manually → Enable Auto-launch, Secondary launch, and Run in background"

            OemType.ONEPLUS -> "Go to Settings → Battery → Battery optimization → NotifIQ → Don't optimize"

            OemType.STOCK_ANDROID, OemType.OTHER -> "Make sure battery optimization is disabled for NotifIQ in Settings → Battery → Battery optimization"
        }
    }

    fun getBatteryOptimizationIntent(oem: OemType, context: Context): Intent? {
        return when (oem) {
            OemType.XIAOMI -> {
                try {
                    val intent = Intent()
                    intent.setClassName(
                        "com.miui.securitycenter",
                        "com.miui.permcenter.autostart.AutoStartManagementActivity"
                    )
                    intent.addCategory(Intent.CATEGORY_LAUNCHER)
                    if (isIntentAvailable(context, intent)) intent else getDefaultBatteryIntent()
                } catch (e: Exception) {
                    getDefaultBatteryIntent()
                }
            }
            else -> getDefaultBatteryIntent()
        }
    }

    private fun getDefaultBatteryIntent(): Intent {
        return Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
    }

    private fun isIntentAvailable(context: Context, intent: Intent): Boolean {
        return try {
            val pm = context.packageManager
            intent.resolveActivity(pm) != null
        } catch (e: Exception) {
            false
        }
    }

    fun isAggressiveOem(oem: OemType): Boolean {
        return oem in listOf(
            OemType.XIAOMI,
            OemType.SAMSUNG,
            OemType.OPPO_REALME,
            OemType.VIVO,
            OemType.HUAWEI
        )
    }
}