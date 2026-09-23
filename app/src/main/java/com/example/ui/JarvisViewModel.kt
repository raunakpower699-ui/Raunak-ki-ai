package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.JarvisDatabase
import com.example.data.local.JarvisRepository
import com.example.data.model.JarvisLog
import com.example.data.model.JarvisReminder
import com.example.data.model.OrbMood
import com.example.data.model.ProactiveAlert
import com.example.data.model.SystemStatusState
import com.example.data.model.VaultItem
import com.example.service.DeviceAutomationManager
import com.example.service.GeminiJarvisEngine
import com.example.service.JarvisAccessibilityService
import com.example.service.JarvisBackgroundService
import com.example.service.JarvisParsedAction
import com.example.service.JarvisVoiceEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ConversationMessage(
    val id: String = "${System.currentTimeMillis()}_${(1000..9999).random()}",
    val isUser: Boolean,
    val text: String,
    val actionType: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

data class IncomingCallData(
    val callerName: String,
    val phoneNumber: String,
    val announcement: String
)

data class ConfirmationData(
    val prompt: String,
    val pendingAction: JarvisParsedAction
)

class JarvisViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: JarvisRepository
    private val voiceEngine: JarvisVoiceEngine

    private val _currentMood = MutableStateFlow(OrbMood.IDLE)
    val currentMood: StateFlow<OrbMood> = _currentMood.asStateFlow()

    private val _systemStatus = MutableStateFlow(SystemStatusState())
    val systemStatus: StateFlow<SystemStatusState> = _systemStatus.asStateFlow()

    private val _messages = MutableStateFlow<List<ConversationMessage>>(emptyList())
    val messages: StateFlow<List<ConversationMessage>> = _messages.asStateFlow()

    private val _pipelineSteps = MutableStateFlow<List<String>>(emptyList())
    val pipelineSteps: StateFlow<List<String>> = _pipelineSteps.asStateFlow()

    private val _currentStepIndex = MutableStateFlow(0)
    val currentStepIndex: StateFlow<Int> = _currentStepIndex.asStateFlow()

    private val _isPipelineExecuting = MutableStateFlow(false)
    val isPipelineExecuting: StateFlow<Boolean> = _isPipelineExecuting.asStateFlow()

    private val _proactiveAlerts = MutableStateFlow<List<ProactiveAlert>>(emptyList())
    val proactiveAlerts: StateFlow<List<ProactiveAlert>> = _proactiveAlerts.asStateFlow()

    private val _incomingCall = MutableStateFlow<IncomingCallData?>(null)
    val incomingCall: StateFlow<IncomingCallData?> = _incomingCall.asStateFlow()

    private val _confirmationData = MutableStateFlow<ConfirmationData?>(null)
    val confirmationData: StateFlow<ConfirmationData?> = _confirmationData.asStateFlow()

    private val _isLiveCallActive = MutableStateFlow(false)
    val isLiveCallActive: StateFlow<Boolean> = _isLiveCallActive.asStateFlow()

    private val _lastUserVoiceTranscript = MutableStateFlow("")
    val lastUserVoiceTranscript: StateFlow<String> = _lastUserVoiceTranscript.asStateFlow()

    private val _lastJarvisVoiceResponse = MutableStateFlow("")
    val lastJarvisVoiceResponse: StateFlow<String> = _lastJarvisVoiceResponse.asStateFlow()

    private val _currentTab = MutableStateFlow(0)
    val currentTab: StateFlow<Int> = _currentTab.asStateFlow()

    private val _isTouchGuardActive = MutableStateFlow(false)
    val isTouchGuardActive: StateFlow<Boolean> = _isTouchGuardActive.asStateFlow()

    val isBackgroundServiceRunning: StateFlow<Boolean> = JarvisBackgroundService.isBackgroundServiceRunning
    val isAccessibilityActive: StateFlow<Boolean> = JarvisAccessibilityService.isServiceActive
    val activeAccessibilityPackage: StateFlow<String> = JarvisAccessibilityService.activePackageName
    val recentAccessibilityEvents: StateFlow<List<String>> = JarvisAccessibilityService.recentEvents

    val isSpeaking: StateFlow<Boolean>
    val isListening: StateFlow<Boolean>
    val audioLevel: StateFlow<Float>

    val allLogs: StateFlow<List<JarvisLog>>
    val allReminders: StateFlow<List<JarvisReminder>>
    val allVaultItems: StateFlow<List<VaultItem>>

    init {
        val db = JarvisDatabase.getDatabase(application, viewModelScope)
        repository = JarvisRepository(db.jarvisDao())

        allLogs = repository.allLogs.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )
        allReminders = repository.allReminders.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )
        allVaultItems = repository.allVaultItems.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        voiceEngine = JarvisVoiceEngine(application) { recognizedSpeech ->
            _lastUserVoiceTranscript.value = recognizedSpeech
            sendCommand(recognizedSpeech, application)
        }

        isSpeaking = voiceEngine.isSpeaking
        isListening = voiceEngine.isListening
        audioLevel = voiceEngine.audioLevel

        // Set initial proactive assistance
        _proactiveAlerts.value = listOf(
            ProactiveAlert(
                id = "alert_1",
                title = "College/Office Alert (15 min)",
                messageHinglish = "Bhai sun na, 15 min me nikalna hai location 20 min door hai, cab dhoondun tere liye?",
                actionLabel = "Cab Dhoondo Yaar",
                commandToExecute = "Uber book karo meeting ke liye",
                iconType = "CAB"
            ),
            ProactiveAlert(
                id = "alert_2",
                title = "WhatsApp from Ravi",
                messageHinglish = "Ravi bhai ne WhatsApp pe pucha: 'aaj shaam ko milte hain?' - reply pelun?",
                actionLabel = "Bhai Reply Kar",
                commandToExecute = "Ravi ko reply karo kal shaam ko",
                iconType = "WHATSAPP"
            ),
            ProactiveAlert(
                id = "alert_3",
                title = "Battery Check",
                messageHinglish = "Battery 84% mast hai bhai! Tension mat le, background saaf rakha hai.",
                actionLabel = "Battery Saver",
                commandToExecute = "Battery saver mode on karo",
                iconType = "BATTERY"
            )
        )

        _messages.value = listOf(
            ConversationMessage(
                isUser = false,
                text = "Arre mere jigri yaar! ❤️ Bol be, tera bhai hazir hai! Aaj phone me kya tufaan machana hai? Insta reels scroll karein, kisi ko WhatsApp pe pel dein, YouTube pe gaana bajayein, ya flashlight chala ke party karein? Bol meri jaan!",
                actionType = "SYSTEM"
            )
        )
    }

    fun setTab(index: Int) {
        _currentTab.value = index
    }

    fun startLiveCall(context: Context) {
        _isLiveCallActive.value = true
        DeviceAutomationManager.vibrateDevice(context, 80)
        val welcomeGreeting = "Haan mere jigri yaar! Main live call pe hoon, bol be kya hukum hai!"
        _lastJarvisVoiceResponse.value = welcomeGreeting
        _currentMood.value = OrbMood.SPEAKING
        voiceEngine.speak(welcomeGreeting) {
            if (_isLiveCallActive.value) {
                startListening()
            }
        }
    }

    fun endLiveCall() {
        _isLiveCallActive.value = false
        voiceEngine.stopListening()
        voiceEngine.stopSpeaking()
        _currentMood.value = OrbMood.IDLE
    }

    fun toggleLiveMic() {
        if (voiceEngine.isListening.value) {
            stopListening()
        } else {
            startListening()
        }
    }

    fun startListening() {
        _currentMood.value = OrbMood.LISTENING
        DeviceAutomationManager.vibrateDevice(getApplication(), 50)
        voiceEngine.startListening()
    }

    fun stopListening() {
        voiceEngine.stopListening()
        _currentMood.value = OrbMood.IDLE
    }

    fun enableTouchGuard() {
        _isTouchGuardActive.value = true
        DeviceAutomationManager.vibrateDevice(getApplication(), 50)
    }

    fun disableTouchGuard() {
        _isTouchGuardActive.value = false
        DeviceAutomationManager.vibrateDevice(getApplication(), 40)
    }

    fun toggleTouchGuard() {
        _isTouchGuardActive.value = !_isTouchGuardActive.value
        DeviceAutomationManager.vibrateDevice(getApplication(), 45)
    }

    fun startBackgroundService(context: Context) {
        JarvisBackgroundService.start(context)
        DeviceAutomationManager.vibrateDevice(context, 40)
    }

    fun stopBackgroundService(context: Context) {
        JarvisBackgroundService.stop(context)
        DeviceAutomationManager.vibrateDevice(context, 40)
    }

    fun toggleBackgroundService(context: Context) {
        JarvisBackgroundService.toggle(context)
        DeviceAutomationManager.vibrateDevice(context, 40)
    }

    fun goToHomeScreen(context: Context) {
        DeviceAutomationManager.goToHomeScreen(context)
    }

    fun closeApp(context: Context) {
        voiceEngine.stopListening()
        voiceEngine.stopSpeaking()
        DeviceAutomationManager.closeApp(context)
    }

    fun stopAndKillApp(context: Context) {
        voiceEngine.stopListening()
        voiceEngine.stopSpeaking()
        DeviceAutomationManager.stopAndKillApp(context)
    }

    fun openRecentApps(context: Context) {
        DeviceAutomationManager.openRecentApps(context)
    }

    fun navigateBack(context: Context) {
        DeviceAutomationManager.navigateBack(context)
    }

    fun openNotifications(context: Context) {
        DeviceAutomationManager.openNotifications(context)
    }

    fun clickElementByText(text: String, context: Context): Boolean {
        return DeviceAutomationManager.clickUiElementByText(text, context)
    }

    fun scrollScreen(down: Boolean, context: Context): Boolean {
        return DeviceAutomationManager.scrollScreen(down, context)
    }

    fun typeTextOnScreen(text: String, context: Context): Boolean {
        return DeviceAutomationManager.typeTextIntoFocusedElement(text, context)
    }

    fun openAccessibilitySettings(context: Context) {
        JarvisAccessibilityService.openAccessibilitySettings(context)
    }

    fun inspectCurrentScreen(): List<String> {
        return DeviceAutomationManager.inspectCurrentScreen()
    }

    fun sendCommand(query: String, context: Context) {
        if (query.isBlank()) return
        voiceEngine.stopSpeaking()

        val userMsg = ConversationMessage(isUser = true, text = query)
        _messages.value = _messages.value + userMsg
        _lastUserVoiceTranscript.value = query

        _currentMood.value = OrbMood.THINKING
        DeviceAutomationManager.vibrateDevice(context, 40)

        viewModelScope.launch(Dispatchers.Main) {
            val parsedAction = GeminiJarvisEngine.processCommand(query)

            if (parsedAction.requiresConfirmation) {
                _confirmationData.value = ConfirmationData(
                    prompt = parsedAction.confirmationPrompt,
                    pendingAction = parsedAction
                )
                _currentMood.value = OrbMood.ALERT
                _lastJarvisVoiceResponse.value = parsedAction.replyText
                voiceEngine.speak(parsedAction.replyText)
                return@launch
            }

            executeAutonomousAction(parsedAction, query, context)
        }
    }

    private suspend fun executeAutonomousAction(
        action: JarvisParsedAction,
        originalQuery: String,
        context: Context
    ) {
        _isPipelineExecuting.value = true
        _pipelineSteps.value = action.steps
        _currentMood.value = OrbMood.EXECUTING

        // Step by step animation
        for (i in action.steps.indices) {
            _currentStepIndex.value = i
            delay(350)
        }
        _isPipelineExecuting.value = false

        // Dispatch real action
        when (action.actionType) {
            "INSTAGRAM" -> {
                DeviceAutomationManager.openInstagram(context, action.targetApp, action.extraData)
            }
            "WHATSAPP" -> {
                DeviceAutomationManager.openWhatsApp(context, message = action.extraData)
            }
            "YOUTUBE" -> {
                DeviceAutomationManager.openYouTube(context, action.extraData)
            }
            "SPOTIFY" -> {
                DeviceAutomationManager.openSpotify(context, action.extraData)
            }
            "TORCH" -> {
                val turnOn = action.extraData == "ON"
                val ok = DeviceAutomationManager.toggleTorch(context, turnOn)
                _systemStatus.value = _systemStatus.value.copy(isTorchOn = turnOn && ok)
            }
            "CALL" -> {
                DeviceAutomationManager.dialCall(context, action.extraData)
            }
            "CAMERA" -> {
                DeviceAutomationManager.openCamera(context, action.extraData == "VIDEO")
            }
            "BATTERY_SAVER" -> {
                DeviceAutomationManager.openSettings(context, android.provider.Settings.ACTION_BATTERY_SAVER_SETTINGS)
            }
            "VOLUME" -> {
                DeviceAutomationManager.setSystemVolume(context, 20)
                _systemStatus.value = _systemStatus.value.copy(volumeLevelPct = 20)
            }
            "REMINDER" -> {
                repository.addReminder(action.extraData, "Today, 7:00 PM", "PERSONAL")
            }
            "MAPS" -> {
                DeviceAutomationManager.openMaps(context, action.extraData)
            }
            "CAB" -> {
                DeviceAutomationManager.bookCab(context, action.extraData)
            }
            "TOUCH_GUARD" -> {
                if (action.extraData == "DISABLE") {
                    disableTouchGuard()
                } else {
                    enableTouchGuard()
                }
            }
            "HOME_SCREEN" -> {
                goToHomeScreen(context)
            }
            "BACKGROUND_SERVICE" -> {
                if (action.extraData == "STOP") {
                    stopBackgroundService(context)
                } else {
                    startBackgroundService(context)
                }
            }
            "CLOSE_APP" -> {
                viewModelScope.launch {
                    delay(1200)
                    closeApp(context)
                }
            }
            "STOP_APP" -> {
                viewModelScope.launch {
                    delay(1200)
                    stopAndKillApp(context)
                }
            }
            "RECENTS" -> {
                openRecentApps(context)
            }
            "BACK" -> {
                navigateBack(context)
            }
            "NOTIFICATIONS" -> {
                openNotifications(context)
            }
            "CLICK_TEXT" -> {
                clickElementByText(action.extraData, context)
            }
            "SCROLL_DOWN" -> {
                scrollScreen(down = true, context = context)
            }
            "SCROLL_UP" -> {
                scrollScreen(down = false, context = context)
            }
            "TYPE_TEXT" -> {
                typeTextOnScreen(action.extraData, context)
            }
            "OPEN_A11Y_SETTINGS" -> {
                openAccessibilitySettings(context)
            }
        }

        // Add assistant reply
        val botMsg = ConversationMessage(
            isUser = false,
            text = action.replyText,
            actionType = action.actionType
        )
        _messages.value = _messages.value + botMsg
        _lastJarvisVoiceResponse.value = action.replyText
        _currentMood.value = OrbMood.SPEAKING

        repository.logCommand(
            query = originalQuery,
            response = action.replyText,
            actionType = action.actionType,
            status = "SUCCESS"
        )

        voiceEngine.speak(action.replyText) {
            if (_isLiveCallActive.value) {
                viewModelScope.launch {
                    delay(500)
                    if (_isLiveCallActive.value) {
                        startListening()
                    }
                }
            } else {
                _currentMood.value = OrbMood.IDLE
            }
        }
    }

    fun resolveConfirmation(confirmed: Boolean, context: Context) {
        val conf = _confirmationData.value ?: return
        _confirmationData.value = null

        if (confirmed) {
            viewModelScope.launch(Dispatchers.Main) {
                executeAutonomousAction(
                    conf.pendingAction.copy(requiresConfirmation = false),
                    "Confirmed: ${conf.pendingAction.actionType}",
                    context
                )
            }
        } else {
            _currentMood.value = OrbMood.IDLE
            val cancelMsg = ConversationMessage(
                isUser = false,
                text = "Action cancelled boss! Safe protocol maintain ho gaya hai.",
                actionType = "SAFETY_CANCEL"
            )
            _messages.value = _messages.value + cancelMsg
            voiceEngine.speak("Action cancel kar diya boss! Tension free raho.")
        }
    }

    fun triggerIncomingCallSimulation() {
        val call = IncomingCallData(
            callerName = "Ravi (College Friend)",
            phoneNumber = "+91 98765 43210",
            announcement = "Boss, Ravi ka call aa raha hai - uthaun ya kat dun?"
        )
        _incomingCall.value = call
        _currentMood.value = OrbMood.ALERT
        voiceEngine.speak(call.announcement)
    }

    fun answerIncomingCall(context: Context) {
        _incomingCall.value = null
        _currentMood.value = OrbMood.SPEAKING
        voiceEngine.speak("Call connect ho gaya boss! Mic live hai.")
        DeviceAutomationManager.vibrateDevice(context, 100)
    }

    fun rejectIncomingCall() {
        _incomingCall.value = null
        _currentMood.value = OrbMood.IDLE
        voiceEngine.speak("Call reject kar diya boss.")
    }

    fun replyIncomingCall(message: String, context: Context) {
        _incomingCall.value = null
        _currentMood.value = OrbMood.IDLE
        DeviceAutomationManager.sendSms(context, "+91 98765 43210", message)
        voiceEngine.speak("Message bhej diya boss: $message")
    }

    fun toggleTorchDirect(context: Context) {
        val nextState = !_systemStatus.value.isTorchOn
        DeviceAutomationManager.toggleTorch(context, nextState)
        _systemStatus.value = _systemStatus.value.copy(isTorchOn = nextState)
        DeviceAutomationManager.vibrateDevice(context, 50)
    }

    fun toggleReminder(reminder: JarvisReminder) {
        viewModelScope.launch {
            repository.toggleReminder(reminder)
        }
    }

    fun deleteReminder(id: Long) {
        viewModelScope.launch {
            repository.deleteReminder(id)
        }
    }

    fun addReminder(title: String, dueTime: String, category: String) {
        viewModelScope.launch {
            repository.addReminder(title, dueTime, category)
        }
    }

    fun addVaultItem(title: String, category: String, secret: String, note: String) {
        viewModelScope.launch {
            repository.addVaultItem(title, category, secret, note)
        }
    }

    fun deleteVaultItem(id: Long) {
        viewModelScope.launch {
            repository.deleteVaultItem(id)
        }
    }

    fun dismissAlert(id: String) {
        _proactiveAlerts.value = _proactiveAlerts.value.filter { it.id != id }
    }

    override fun onCleared() {
        super.onCleared()
        voiceEngine.destroy()
    }
}
