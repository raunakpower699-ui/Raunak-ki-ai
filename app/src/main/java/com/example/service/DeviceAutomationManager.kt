package com.example.service

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log

object DeviceAutomationManager {
    private const val TAG = "JarvisAutomation"

    fun vibrateDevice(context: Context, milliseconds: Long = 100) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createOneShot(milliseconds, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                vibrator?.vibrate(
                    VibrationEffect.createOneShot(milliseconds, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Vibration failed: ${e.message}")
        }
    }

    fun toggleTorch(context: Context, turnOn: Boolean): Boolean {
        return try {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
            val cameraId = cameraManager?.cameraIdList?.firstOrNull { id ->
                val chars = cameraManager.getCameraCharacteristics(id)
                val hasFlash = chars.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
                val isBack = chars.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK
                hasFlash && isBack
            } ?: cameraManager?.cameraIdList?.firstOrNull()

            if (cameraId != null && cameraManager != null) {
                cameraManager.setTorchMode(cameraId, turnOn)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Toggle torch error: ${e.message}")
            false
        }
    }

    fun setSystemVolume(context: Context, percent: Int) {
        try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            if (audioManager != null) {
                val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                val targetVol = (maxVol * (percent.coerceIn(0, 100) / 100f)).toInt()
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, targetVol, AudioManager.FLAG_SHOW_UI)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Volume change error: ${e.message}")
        }
    }

    fun openInstagram(context: Context, mode: String = "home", extra: String = "") {
        try {
            val intent = when (mode) {
                "profile" -> {
                    val username = extra.ifEmpty { "instagram" }.replace("@", "").trim()
                    Intent(Intent.ACTION_VIEW, Uri.parse("https://instagram.com/_u/$username")).apply {
                        setPackage("com.instagram.android")
                    }
                }
                "reels" -> {
                    Intent(Intent.ACTION_VIEW, Uri.parse("https://instagram.com/reels")).apply {
                        setPackage("com.instagram.android")
                    }
                }
                else -> {
                    context.packageManager.getLaunchIntentForPackage("com.instagram.android")
                        ?: Intent(Intent.ACTION_VIEW, Uri.parse("https://instagram.com"))
                }
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://instagram.com"))
            browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(browserIntent)
        }
    }

    fun openWhatsApp(context: Context, phone: String = "", message: String = "") {
        try {
            val cleanPhone = phone.replace(Regex("[^0-9+]"), "")
            val encodedMsg = Uri.encode(message)
            val uri = if (cleanPhone.isNotEmpty()) {
                Uri.parse("https://api.whatsapp.com/send?phone=$cleanPhone&text=$encodedMsg")
            } else {
                Uri.parse("https://api.whatsapp.com/send?text=$encodedMsg")
            }
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                setPackage("com.whatsapp")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, message)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Send via").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        }
    }

    fun dialCall(context: Context, phoneNumber: String) {
        try {
            val cleanNumber = phoneNumber.replace(Regex("[^0-9+]"), "").ifEmpty { "100" }
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$cleanNumber")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Dial call error: ${e.message}")
        }
    }

    fun sendSms(context: Context, phoneNumber: String, message: String) {
        try {
            val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:${phoneNumber.trim()}")).apply {
                putExtra("sms_body", message)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Send SMS error: ${e.message}")
        }
    }

    fun openCamera(context: Context, isVideo: Boolean = false) {
        try {
            val action = if (isVideo) MediaStore.ACTION_VIDEO_CAPTURE else MediaStore.ACTION_IMAGE_CAPTURE
            val intent = Intent(action).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Camera launch error: ${e.message}")
        }
    }

    fun openGallery(context: Context) {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                type = "image/*"
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Gallery launch error: ${e.message}")
        }
    }

    fun openMaps(context: Context, destination: String) {
        try {
            val gmmIntentUri = Uri.parse("geo:0,0?q=${Uri.encode(destination)}")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
                setPackage("com.google.android.apps.maps")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (mapIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(mapIntent)
            } else {
                val webMap = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/?api=1&query=${Uri.encode(destination)}")).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(webMap)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Maps launch error: ${e.message}")
        }
    }

    fun openSettings(context: Context, action: String = Settings.ACTION_SETTINGS) {
        try {
            val intent = Intent(action).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            val fallback = Intent(Settings.ACTION_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(fallback)
        }
    }

    fun searchWeb(context: Context, query: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=${Uri.encode(query)}")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Web search error: ${e.message}")
        }
    }

    fun bookCab(context: Context, destination: String) {
        try {
            val uberUri = Uri.parse("https://m.uber.com/ul/?action=setPickup&pickup=my_location&dropoff[formatted_address]=${Uri.encode(destination)}")
            val intent = Intent(Intent.ACTION_VIEW, uberUri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            searchWeb(context, "Book cab to $destination")
        }
    }

    fun openYouTube(context: Context, query: String = "") {
        try {
            val intent = if (query.isNotBlank()) {
                Intent(Intent.ACTION_SEARCH).apply {
                    setPackage("com.google.android.youtube")
                    putExtra("query", query)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
            } else {
                context.packageManager.getLaunchIntentForPackage("com.google.android.youtube")
                    ?: Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com"))
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            val webIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse(if (query.isNotBlank()) "https://www.youtube.com/results?search_query=${Uri.encode(query)}" else "https://www.youtube.com")
            ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
            context.startActivity(webIntent)
        }
    }

    fun openSpotify(context: Context, query: String = "") {
        try {
            val intent = if (query.isNotBlank()) {
                Intent(Intent.ACTION_VIEW, Uri.parse("spotify:search:${Uri.encode(query)}")).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            } else {
                context.packageManager.getLaunchIntentForPackage("com.spotify.music")
                    ?: Intent(Intent.ACTION_VIEW, Uri.parse("https://open.spotify.com"))
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://open.spotify.com")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(webIntent)
        }
    }

    fun launchAppByName(context: Context, appName: String): Boolean {
        try {
            val pm = context.packageManager
            val packages = pm.getInstalledApplications(0)
            val match = packages.firstOrNull {
                val label = pm.getApplicationLabel(it).toString().lowercase()
                label.contains(appName.lowercase())
            }
            if (match != null) {
                val launchIntent = pm.getLaunchIntentForPackage(match.packageName)
                if (launchIntent != null) {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(launchIntent)
                    return true
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Launch app error: ${e.message}")
        }
        return false
    }

    fun goToHomeScreen(context: Context) {
        try {
            vibrateDevice(context, 40)
            if (JarvisAccessibilityService.isServiceActive.value) {
                if (JarvisAccessibilityService.navigateHome()) return
            }
            val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(homeIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to navigate to Home screen: ${e.message}")
        }
    }

    /**
     * Navigates between apps by triggering the system app switcher (Recents).
     */
    fun openRecentApps(context: Context): Boolean {
        vibrateDevice(context, 40)
        return if (JarvisAccessibilityService.isServiceActive.value) {
            JarvisAccessibilityService.navigateRecents()
        } else {
            false
        }
    }

    /**
     * Triggers global Back action.
     */
    fun navigateBack(context: Context): Boolean {
        vibrateDevice(context, 40)
        return if (JarvisAccessibilityService.isServiceActive.value) {
            JarvisAccessibilityService.navigateBack()
        } else {
            false
        }
    }

    /**
     * Opens system notifications shade.
     */
    fun openNotifications(context: Context): Boolean {
        vibrateDevice(context, 40)
        return if (JarvisAccessibilityService.isServiceActive.value) {
            JarvisAccessibilityService.openNotifications()
        } else {
            false
        }
    }

    /**
     * Programmatically clicks on a UI element displaying or describing the specified text.
     */
    fun clickUiElementByText(text: String, context: Context): Boolean {
        vibrateDevice(context, 35)
        return JarvisAccessibilityService.clickElementByText(text)
    }

    /**
     * Programmatically clicks on a UI element with a specific View ID.
     */
    fun clickUiElementById(viewId: String, context: Context): Boolean {
        vibrateDevice(context, 35)
        return JarvisAccessibilityService.clickElementById(viewId)
    }

    /**
     * Programmatically inputs text into the currently focused edit field.
     */
    fun typeTextIntoFocusedElement(text: String, context: Context): Boolean {
        vibrateDevice(context, 30)
        return JarvisAccessibilityService.enterTextIntoFocusedElement(text)
    }

    /**
     * Scrolls the current active screen down or up.
     */
    fun scrollScreen(down: Boolean, context: Context): Boolean {
        vibrateDevice(context, 30)
        return JarvisAccessibilityService.scrollWindow(forward = down)
    }

    /**
     * Performs a simulated screen tap at specific coordinates.
     */
    fun tapCoordinates(x: Float, y: Float, context: Context): Boolean {
        vibrateDevice(context, 30)
        return JarvisAccessibilityService.dispatchTapGesture(x, y)
    }

    /**
     * Reads visible text on the active window.
     */
    fun inspectCurrentScreen(): List<String> {
        return JarvisAccessibilityService.inspectScreenElements()
    }

    fun closeApp(context: Context) {
        try {
            vibrateDevice(context, 50)
            if (context is Activity) {
                context.finish()
            } else {
                goToHomeScreen(context)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to close app: ${e.message}")
            goToHomeScreen(context)
        }
    }

    fun stopAndKillApp(context: Context) {
        try {
            vibrateDevice(context, 120)
            JarvisBackgroundService.stop(context)
            if (context is Activity) {
                context.finishAffinity()
            }
            android.os.Process.killProcess(android.os.Process.myPid())
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop and kill app: ${e.message}")
        }
    }
}
