package com.example.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Path
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.lang.ref.WeakReference

/**
 * Autonomous Accessibility Service enabling JARVIS to:
 * 1. Navigate between applications (Recents / Task Switcher, Home, Back).
 * 2. Inspect and extract on-screen elements.
 * 3. Programmatically interact with UI elements (click, type, scroll).
 * 4. Dispatch precision touch and swipe gestures across the system.
 */
class JarvisAccessibilityService : AccessibilityService() {

    override fun onServiceConnected() {
        super.onServiceConnected()
        serviceInstance = WeakReference(this)
        _isServiceActive.value = true
        Log.i(TAG, "JarvisAccessibilityService Connected and Active")
        recordEvent("Service Connected and Ready")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        val pkgName = event.packageName?.toString() ?: ""
        if (pkgName.isNotEmpty() && pkgName != _activePackageName.value) {
            _activePackageName.value = pkgName
            recordEvent("App Switched: $pkgName")
        }

        when (event.eventType) {
            AccessibilityEvent.TYPE_VIEW_CLICKED -> {
                val text = event.text.joinToString()
                if (text.isNotBlank()) {
                    recordEvent("Clicked: \"$text\" in $pkgName")
                }
            }
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                val cls = event.className?.toString() ?: ""
                if (cls.isNotBlank()) {
                    recordEvent("Window Changed: $cls ($pkgName)")
                }
            }
        }
    }

    override fun onInterrupt() {
        Log.w(TAG, "JarvisAccessibilityService Interrupted")
        recordEvent("Service Interrupted")
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceInstance = null
        _isServiceActive.value = false
        Log.i(TAG, "JarvisAccessibilityService Destroyed")
    }

    companion object {
        private const val TAG = "JarvisA11yService"

        private var serviceInstance: WeakReference<JarvisAccessibilityService>? = null

        private val _isServiceActive = MutableStateFlow(false)
        val isServiceActive: StateFlow<Boolean> = _isServiceActive.asStateFlow()

        private val _activePackageName = MutableStateFlow("")
        val activePackageName: StateFlow<String> = _activePackageName.asStateFlow()

        private val _recentEvents = MutableStateFlow<List<String>>(emptyList())
        val recentEvents: StateFlow<List<String>> = _recentEvents.asStateFlow()

        private fun recordEvent(desc: String) {
            val current = _recentEvents.value.toMutableList()
            current.add(0, desc)
            if (current.size > 8) {
                _recentEvents.value = current.take(8)
            } else {
                _recentEvents.value = current
            }
        }

        fun getInstance(): JarvisAccessibilityService? = serviceInstance?.get()

        /**
         * Checks if the Accessibility Service is enabled in Android System Settings.
         */
        fun isServiceEnabled(context: Context): Boolean {
            val serviceName = ComponentName(context, JarvisAccessibilityService::class.java).flattenToString()
            val enabledServices = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: return false

            val colonSplitter = TextUtils.SimpleStringSplitter(':')
            colonSplitter.setString(enabledServices)
            while (colonSplitter.hasNext()) {
                val componentName = colonSplitter.next()
                if (componentName.equals(serviceName, ignoreCase = true) ||
                    componentName.contains("com.example.service.JarvisAccessibilityService")
                ) {
                    return true
                }
            }
            return false
        }

        /**
         * Launches Android system Accessibility Settings for user authorization.
         */
        fun openAccessibilitySettings(context: Context) {
            try {
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to open accessibility settings: ${e.message}")
            }
        }

        // ==========================================
        // SYSTEM NAVIGATION CAPABILITIES
        // ==========================================

        fun navigateHome(): Boolean {
            val service = getInstance() ?: return false
            recordEvent("Triggered Global Home")
            return service.performGlobalAction(GLOBAL_ACTION_HOME)
        }

        fun navigateBack(): Boolean {
            val service = getInstance() ?: return false
            recordEvent("Triggered Global Back")
            return service.performGlobalAction(GLOBAL_ACTION_BACK)
        }

        /**
         * Opens Android App Switcher (Recents) allowing user/JARVIS to navigate between apps.
         */
        fun navigateRecents(): Boolean {
            val service = getInstance() ?: return false
            recordEvent("Triggered App Switcher (Recents)")
            return service.performGlobalAction(GLOBAL_ACTION_RECENTS)
        }

        fun openNotifications(): Boolean {
            val service = getInstance() ?: return false
            recordEvent("Opened Notifications Panel")
            return service.performGlobalAction(GLOBAL_ACTION_NOTIFICATIONS)
        }

        fun openQuickSettings(): Boolean {
            val service = getInstance() ?: return false
            recordEvent("Opened Quick Settings Panel")
            return service.performGlobalAction(GLOBAL_ACTION_QUICK_SETTINGS)
        }

        fun lockScreen(): Boolean {
            val service = getInstance() ?: return false
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                recordEvent("Triggered Screen Lock")
                return service.performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN)
            }
            return false
        }

        fun takeScreenshot(): Boolean {
            val service = getInstance() ?: return false
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                recordEvent("Triggered System Screenshot")
                return service.performGlobalAction(GLOBAL_ACTION_TAKE_SCREENSHOT)
            }
            return false
        }

        // ==========================================
        // PROGRAMMATIC UI INTERACTION CAPABILITIES
        // ==========================================

        /**
         * Finds a UI element containing the specified text and programmatically clicks it.
         */
        fun clickElementByText(targetText: String, exactMatch: Boolean = false): Boolean {
            val service = getInstance() ?: return false
            val rootNode = service.rootInActiveWindow ?: return false

            val targetLower = targetText.trim().lowercase()
            val matchedNodes = rootNode.findAccessibilityNodeInfosByText(targetText)

            for (node in matchedNodes) {
                val nodeText = node.text?.toString()?.trim()?.lowercase() ?: ""
                val nodeDesc = node.contentDescription?.toString()?.trim()?.lowercase() ?: ""

                val matches = if (exactMatch) {
                    nodeText == targetLower || nodeDesc == targetLower
                } else {
                    nodeText.contains(targetLower) || nodeDesc.contains(targetLower)
                }

                if (matches) {
                    var clickableNode: AccessibilityNodeInfo? = node
                    while (clickableNode != null && !clickableNode.isClickable) {
                        clickableNode = clickableNode.parent
                    }
                    if (clickableNode != null && clickableNode.isClickable) {
                        val success = clickableNode.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                        if (success) {
                            recordEvent("Programmatic Click: \"$targetText\"")
                            return true
                        }
                    }
                }
            }

            // Fallback recursive traversal if findAccessibilityNodeInfosByText returned partial results
            val found = searchAndClickRecursive(rootNode, targetLower, exactMatch)
            if (found) {
                recordEvent("Recursive Click: \"$targetText\"")
            }
            return found
        }

        private fun searchAndClickRecursive(
            node: AccessibilityNodeInfo?,
            targetLower: String,
            exactMatch: Boolean
        ): Boolean {
            if (node == null) return false

            val text = node.text?.toString()?.lowercase() ?: ""
            val desc = node.contentDescription?.toString()?.lowercase() ?: ""

            val matches = if (exactMatch) {
                text == targetLower || desc == targetLower
            } else {
                text.contains(targetLower) || desc.contains(targetLower)
            }

            if (matches) {
                var current: AccessibilityNodeInfo? = node
                while (current != null && !current.isClickable) {
                    current = current.parent
                }
                if (current != null && current.isClickable) {
                    return current.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                }
            }

            for (i in 0 until node.childCount) {
                val child = node.getChild(i)
                if (searchAndClickRecursive(child, targetLower, exactMatch)) {
                    return true
                }
            }
            return false
        }

        /**
         * Clicks a UI element by its Android View Resource ID (e.g., "com.whatsapp:id/send").
         */
        fun clickElementById(viewId: String): Boolean {
            val service = getInstance() ?: return false
            val rootNode = service.rootInActiveWindow ?: return false

            val nodes = rootNode.findAccessibilityNodeInfosByViewId(viewId)
            for (node in nodes) {
                var current: AccessibilityNodeInfo? = node
                while (current != null && !current.isClickable) {
                    current = current.parent
                }
                if (current != null && current.isClickable) {
                    val clicked = current.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    if (clicked) {
                        recordEvent("Clicked ID: $viewId")
                        return true
                    }
                }
            }
            return false
        }

        /**
         * Types input text into the currently focused or editable UI element.
         */
        fun enterTextIntoFocusedElement(text: String): Boolean {
            val service = getInstance() ?: return false
            val rootNode = service.rootInActiveWindow ?: return false

            // Find focused or editable node
            val focusedNode = rootNode.findFocus(AccessibilityNodeInfo.FOCUS_INPUT)
                ?: findFirstEditableNode(rootNode)

            if (focusedNode != null) {
                val args = Bundle().apply {
                    putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
                }
                val success = focusedNode.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)
                if (success) {
                    recordEvent("Typed text: \"$text\"")
                    return true
                }
            }
            return false
        }

        private fun findFirstEditableNode(node: AccessibilityNodeInfo?): AccessibilityNodeInfo? {
            if (node == null) return false?.let { null }
            if (node.isEditable) return node
            for (i in 0 until node.childCount) {
                val match = findFirstEditableNode(node.getChild(i))
                if (match != null) return match
            }
            return null
        }

        /**
         * Scrolls the active scrollable view forward (down) or backward (up).
         */
        fun scrollWindow(forward: Boolean = true): Boolean {
            val service = getInstance() ?: return false
            val rootNode = service.rootInActiveWindow ?: return false

            val scrollableNode = findScrollableNode(rootNode)
            if (scrollableNode != null) {
                val action = if (forward) {
                    AccessibilityNodeInfo.ACTION_SCROLL_FORWARD
                } else {
                    AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD
                }
                val success = scrollableNode.performAction(action)
                if (success) {
                    recordEvent(if (forward) "Scrolled Down/Forward" else "Scrolled Up/Backward")
                    return true
                }
            }
            return false
        }

        private fun findScrollableNode(node: AccessibilityNodeInfo?): AccessibilityNodeInfo? {
            if (node == null) return null
            if (node.isScrollable) return node
            for (i in 0 until node.childCount) {
                val child = findScrollableNode(node.getChild(i))
                if (child != null) return child
            }
            return null
        }

        /**
         * Dispatches a simulated tap at specific screen coordinates (X, Y).
         */
        fun dispatchTapGesture(x: Float, y: Float, onComplete: ((Boolean) -> Unit)? = null): Boolean {
            val service = getInstance() ?: return false
            val path = Path().apply {
                moveTo(x, y)
            }
            val stroke = GestureDescription.StrokeDescription(path, 0, 50)
            val gesture = GestureDescription.Builder().addStroke(stroke).build()

            return service.dispatchGesture(
                gesture,
                object : AccessibilityService.GestureResultCallback() {
                    override fun onCompleted(gestureDescription: GestureDescription?) {
                        super.onCompleted(gestureDescription)
                        recordEvent("Gesture Tap at ($x, $y)")
                        onComplete?.invoke(true)
                    }

                    override fun onCancelled(gestureDescription: GestureDescription?) {
                        super.onCancelled(gestureDescription)
                        Log.w(TAG, "Gesture Tap cancelled at ($x, $y)")
                        onComplete?.invoke(false)
                    }
                },
                null
            )
        }

        /**
         * Dispatches a swipe gesture across the screen.
         */
        fun dispatchSwipeGesture(
            startX: Float,
            startY: Float,
            endX: Float,
            endY: Float,
            durationMs: Long = 300,
            onComplete: ((Boolean) -> Unit)? = null
        ): Boolean {
            val service = getInstance() ?: return false
            val path = Path().apply {
                moveTo(startX, startY)
                lineTo(endX, endY)
            }
            val stroke = GestureDescription.StrokeDescription(path, 0, durationMs)
            val gesture = GestureDescription.Builder().addStroke(stroke).build()

            return service.dispatchGesture(
                gesture,
                object : AccessibilityService.GestureResultCallback() {
                    override fun onCompleted(gestureDescription: GestureDescription?) {
                        super.onCompleted(gestureDescription)
                        recordEvent("Swipe ($startX,$startY) -> ($endX,$endY)")
                        onComplete?.invoke(true)
                    }

                    override fun onCancelled(gestureDescription: GestureDescription?) {
                        super.onCancelled(gestureDescription)
                        Log.w(TAG, "Swipe gesture cancelled")
                        onComplete?.invoke(false)
                    }
                },
                null
            )
        }

        /**
         * Traverses the active window hierarchy and returns visible on-screen texts and element descriptions.
         */
        fun inspectScreenElements(): List<String> {
            val service = getInstance() ?: return emptyList()
            val rootNode = service.rootInActiveWindow ?: return emptyList()
            val results = mutableListOf<String>()
            collectVisibleTexts(rootNode, results)
            return results
        }

        private fun collectVisibleTexts(node: AccessibilityNodeInfo?, list: MutableList<String>) {
            if (node == null) return
            val text = node.text?.toString()?.trim()
            val desc = node.contentDescription?.toString()?.trim()

            if (!text.isNullOrEmpty() && !list.contains(text)) {
                list.add(text)
            }
            if (!desc.isNullOrEmpty() && desc != text && !list.contains(desc)) {
                list.add("[$desc]")
            }

            for (i in 0 until node.childCount) {
                collectVisibleTexts(node.getChild(i), list)
            }
        }
    }
}
