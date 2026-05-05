package com.example.jetpackcomposeapp

import android.Manifest
import android.app.AlertDialog
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.jetpackcomposeapp.Navigation.MainNavigation
import com.example.jetpackcomposeapp.Topic.vc.VideoCallHost
import com.example.jetpackcomposeapp.ui.theme.JetpackComposeAppTheme

val LocalNavController = staticCompositionLocalOf<NavHostController> {
    error("No NavController found!")
}

class MainActivity : FragmentActivity(), VideoCallHost {
    private var wakeLock: PowerManager.WakeLock? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JetpackComposeAppTheme {
                val navController = rememberNavController()
                CompositionLocalProvider(LocalNavController provides navController) {
                    MainNavigation(navController)
                }
            }
        }
    }

    override fun prepareVideoCallPermissions() {
        setupPermissions()
        requestVideoCallPermissions()
    }

    private fun requestVideoCallPermissions() {
        val permissions = mutableListOf<String>()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.CAMERA)
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.RECORD_AUDIO)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        if (permissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissions.toTypedArray(), request_code)
        }
    }

    private fun setupPermissions() {
        if (isMiuiDevice()) {
            if (!getBoolean(this, KEY_MIUI_POPUP_PERMISSION)) {
                showBackgroundPopupWarning(this)
            }
            if (!getBoolean(this, KEY_MIUI_BACKGROUND_AUTOSTART)) {
                showAutoStartPermission(this)
            }
        }
    }

    private fun isMiuiDevice(): Boolean {
        return Build.MANUFACTURER.equals("Xiaomi", ignoreCase = true) ||
            Build.MANUFACTURER.equals("Redmi", ignoreCase = true) ||
            Build.MANUFACTURER.equals("POCO", ignoreCase = true)
    }

    fun showAutoStartPermission(context: Context) {
        AlertDialog.Builder(context)
            .setTitle("Auto Start")
            .setMessage("Enable Auto Start for reliable video calls.")
            .setPositiveButton("OK") { _, _ ->
                openMiuiAutoStart(context)
                saveBoolean(context, KEY_MIUI_BACKGROUND_AUTOSTART, true)
            }
            .show()
    }

    fun openMiuiAutoStart(context: Context) {
        try {
            val intent = Intent().apply {
                component = ComponentName(
                    "com.miui.securitycenter",
                    "com.miui.permcenter.autostart.AutoStartManagementActivity"
                )
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            context.startActivity(
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.parse("package:${context.packageName}")
                }
            )
        }
    }

    fun showBackgroundPopupWarning(context: Context) {
        AlertDialog.Builder(context)
            .setTitle("Background Pop-ups")
            .setMessage("Enable 'Display pop-ups while running in background' so video call alerts can appear properly.")
            .setPositiveButton("OK") { _, _ ->
                openMIUIPopupPermission(context)
                saveBoolean(context, KEY_MIUI_POPUP_PERMISSION, true)
            }
            .show()
    }

    fun openMIUIPopupPermission(context: Context) {
        try {
            val intent = Intent("miui.intent.action.APP_PERM_EDITOR").apply {
                setClassName(
                    "com.miui.securitycenter",
                    "com.miui.permcenter.permissions.PermissionsEditorActivity"
                )
                putExtra("extra_pkgname", context.packageName)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            startActivity(
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                }
            )
        }
    }

    override fun enableVideoCallScreenMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }
        makeActivityShowOnLockScreen()
        acquireWakeLock()
    }

    override fun disableVideoCallScreenMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(false)
            setTurnScreenOn(false)
        }
        window.clearFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )
        releaseWakeLock()
    }

    private fun makeActivityShowOnLockScreen() {
        window.addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )
    }

    @Suppress("DEPRECATION")
    private fun acquireWakeLock() {
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.FULL_WAKE_LOCK or
                PowerManager.ACQUIRE_CAUSES_WAKEUP or
                PowerManager.ON_AFTER_RELEASE,
            "JetpackComposeApp::VideoCallWakeLock"
        )
        wakeLock?.acquire(3 * 60 * 1000L)
    }

    private fun releaseWakeLock() {
        if (wakeLock?.isHeld == true) {
            wakeLock?.release()
        }
        wakeLock = null
    }

    override fun onDestroy() {
        releaseWakeLock()
        super.onDestroy()
    }

    companion object {
        private const val PREFS_NAME = "AppPrefs"
        private const val KEY_MIUI_POPUP_PERMISSION = "miui_popup_checked"
        private const val KEY_MIUI_BACKGROUND_AUTOSTART = "miui_background_autostart_checked"
        private const val request_code = 1001

        private fun getPrefs(context: Context) =
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        fun saveBoolean(context: Context, key: String, value: Boolean) {
            getPrefs(context).edit().putBoolean(key, value).apply()
        }

        fun getBoolean(context: Context, key: String, defaultValue: Boolean = false): Boolean {
            return getPrefs(context).getBoolean(key, defaultValue)
        }
    }
}
