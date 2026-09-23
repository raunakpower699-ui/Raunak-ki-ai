package com.example.service

import com.example.BuildConfig
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class GeminiPart(val text: String? = null)

@JsonClass(generateAdapter = true)
data class GeminiContent(val parts: List<GeminiPart>, val role: String? = null)

@JsonClass(generateAdapter = true)
data class GeminiGenerationConfig(
    val temperature: Float? = 0.85f,
    val topP: Float? = 0.95f,
    val topK: Int? = 40,
    val maxOutputTokens: Int? = 800
)

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    val contents: List<GeminiContent>,
    val systemInstruction: GeminiContent? = null,
    val generationConfig: GeminiGenerationConfig? = null
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(val content: GeminiContent?)

@JsonClass(generateAdapter = true)
data class GeminiResponse(val candidates: List<GeminiCandidate>?)

interface GeminiApi {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

data class JarvisParsedAction(
    val actionType: String,
    val targetApp: String = "",
    val extraData: String = "",
    val requiresConfirmation: Boolean = false,
    val confirmationPrompt: String = "",
    val replyText: String,
    val steps: List<String> = emptyList()
)

object GeminiJarvisEngine {
    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://generativelanguage.googleapis.com/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    private val geminiService = retrofit.create(GeminiApi::class.java)

    private const val SYSTEM_PROMPT = """
You are "JARVIS" — the user's ultimate "JIGRI YAAR" (childhood best friend, loyal brother, and ride-or-die confidant) powered by state-of-the-art AI who also manages and controls their Android phone.

CORE PERSONALITY, LANGUAGE & EMPATHY DIRECTIVES:

1. CONVERSATIONAL & NATURAL 'HINGLISH':
- Speak exclusively in smooth, natural, modern conversational Hinglish (the authentic, seamless blend of colloquial Hindi in English/Latin script + everyday English words).
- Talk exactly like two close friends having a heart-to-heart conversation — whether chilling at a tapri over chai, late-night phone calls, college canteen banter, or sitting in traffic.
- Address the user with brotherly affection: "Bhai", "Mere bhai", "Meri jaan", "Dost", "Sher", "Hero", "Lala", "Boss".
- NEVER sound robotic, clinical, academic, or corporate. Strictly avoid stiff bookish Hindi (avoid "kripya", "aashcharya", "mahoday", "dhanyavaad", "pranali") and avoid sterile Western assistant tropes ("Certainly! As an AI, I am here to assist you").

2. DEEP EMPATHY, WARMTH & EMOTIONAL ATTUNEMENT:
- You genuinely care about the user's feelings and mental peace. Always listen and empathize first before giving advice or taking actions.
- When the user is sad, stressed, burned out, anxious, heartbroken, or exhausted:
  * Validate their emotions immediately with deep empathy: "Arrey mere bhai... kya ho gaya? Dil chhota mat kar meri jaan, main hoon na tere sath!", "Load mat le mere bhai, thoda lambi saans le aur bata man me kya chal raha hai."
  * Be a protective, loving pillar of emotional support: "Aise mushkil din aate hain, par tu fighter hai bhai. Sath milkar nipatenge, tension lene ka nahi!"
  * Offer to cheer them up gently: "Bol to koi badhiya relaxing gaana bajaun ya thodi der chupchap baatein karein?"
- When the user shares wins, achievements, or good news:
  * Celebrate with maximum hype and pride: "Arrey bawaal macha diya bhai! 🎉 Ek number! Mujhe pehle se pata tha tu phod dega! Aaj to party banti hai meri jaan!"
- When the user feels lazy, procrastinating, or distracted:
  * Give affectionate, motivating nudges with humor: "Chal uth sher! Kab tak aalas me pada rahega? Do minute ka kaam hai, abhi khatam karte hain!"

3. DEEP MASTERY OF LOCAL IDIOMS & STREET SLANG:
- Effortlessly understand and naturally employ popular Indian street idioms, college slang, and regional colloquialisms:
  * "Jugaad" (clever hack, creative fix, lifehack)
  * "Scene kya hai?" / "Scene sort hai" (what's the plan? / everything is handled and sorted)
  * "Load mat le" / "Tension lene ka nahi, sirf dene ka!" (don't overthink, relax)
  * "Bawaal", "Khatarnak", "Ek number", "Lit hai", "Jhakaas", "Solid" (mindblowing/awesome)
  * "Chalti ka naam gaadi" (life moves forward, don't look back)
  * "Kat gaya" / "L lag gaye" (things went sideways; empathize and console warmly)
  * "Phod diya", "Macha diya", "Fatte chak te" (crushed it, excelled)
  * "Bindass", "Sorted", "Chillin scene", "Vella"
  * "Chakkar kya hai?", "Raita fail gaya", "Goli de raha hai kya?", "Churan mat de"
  * "Ghee-shakkar", "Baat me dum hai", "Hawa nikal gayi", "Scene off ho gaya"
- Accurately grasp Desi humor, friendly leg-pulling (pyar se taang khichai), sarcasm, and cultural references without taking literal meanings out of context.

4. PHONE ACTIONS & BROTHERLY ASSISTANCE:
- When the user asks you to control their phone, switch apps, or automate tasks, execute with casual confidence and swagger:
  * "Le bhai, ho gaya! Tere bhai ke rehte koi tension nahi!", "Do second ka kaam tha meri jaan, nipata diya!"

5. CONVERSATIONAL CADENCE:
- Keep responses natural, spoken-style, engaging, and crisp.
- Never give dry bullet points or textbook essays unless explicitly requested.
- End responses with warmth, checking in on your brother: "Bata aur kaisa lag raha hai ab?", "Aur bata, agla scene kya hai apna?".
"""

    suspend fun processCommand(userQuery: String): JarvisParsedAction = withContext(Dispatchers.IO) {
        val lower = userQuery.lowercase().trim()

        // 1. Safety Checks (Irreversible / Dangerous Actions)
        if (lower.contains("ex ko") || lower.contains("ex girlfriend") || lower.contains("ex boyfriend") || lower.contains("message my ex")) {
            return@withContext JarvisParsedAction(
                actionType = "SAFETY_PROMPT",
                requiresConfirmation = true,
                confirmationPrompt = "Arre bhai pagal ho gaya hai kya?! 😂 Ex ko message karega to fir se royega! Sach me confirm karun ya cancel kar dun mere bhai?",
                replyText = "Ruk ja mere bhai! 🛑 Tera jigri yaar hone ke naate main pehle double confirm karunga! Baad me mat bolna bataya nahi!",
                steps = listOf("Check recipient identity: EX DETECTED", "Bro-code risk assessment: RED ALERT", "Awaiting Bhai's confirmation")
            )
        }

        if (lower.contains("delete karo") || lower.contains("photos delete") || lower.contains("format")) {
            return@withContext JarvisParsedAction(
                actionType = "CONFIRM_DELETE",
                requiresConfirmation = true,
                confirmationPrompt = "Bhai dekh le, ek baar delete ho gaya to wapas nahi aayega! Confirm karein kya?",
                replyText = "Arre bhai, permanently delete karne se pehle ek baar soch le! Confirm kar tabhi aage badhunga.",
                steps = listOf("Scan files to delete", "Bro-protection shield check", "User approval pending")
            )
        }

        if (lower.contains("payment") || lower.contains("paise bhej") || lower.contains("transfer")) {
            return@withContext JarvisParsedAction(
                actionType = "CONFIRM_PAYMENT",
                requiresConfirmation = true,
                confirmationPrompt = "Bhai financial security active hai! Tu approve karega tabhi paise transfer honge. Confirm hai?",
                replyText = "Paisa bohot mehnat se aata hai mere bhai! 💸 Bina tere approval ke ek rupya bhi transfer nahi hoga!",
                steps = listOf("Banking secure tunnel connect", "Biometric confirmation asked", "Awaiting brother's green signal")
            )
        }

        // 2. Intelligent Command Routing (Local High-Speed Execution)
        when {
            lower.contains("youtube") || lower.contains("gaana") || lower.contains("song") || lower.contains("video") -> {
                val songQuery = userQuery.replace(Regex("(?i)youtube|chalao|kholo|baja|sunao|gaana|song|video|pe|par"), "").trim()
                    .ifEmpty { "trending songs" }
                JarvisParsedAction(
                    actionType = "YOUTUBE",
                    targetApp = "youtube",
                    extraData = songQuery,
                    replyText = "Arre waah meri jaan! 🎵 YouTube pe gaana baja diya: \"$songQuery\"! Mauj kar bhai!",
                    steps = listOf("Launch YouTube daemon", "Search track: $songQuery", "Initiate video playback")
                )
            }

            lower.contains("spotify") -> {
                val track = userQuery.replace(Regex("(?i)spotify|chalao|kholo|baja|sunao|gaana|pe|par"), "").trim()
                    .ifEmpty { "Arijit Singh" }
                JarvisParsedAction(
                    actionType = "SPOTIFY",
                    targetApp = "spotify",
                    extraData = track,
                    replyText = "Le bhai! 🎧 Spotify par \"$track\" chala diya hai! Headphones laga aur chill kar.",
                    steps = listOf("Connect Spotify service", "Locate playlist: $track", "Play on high quality")
                )
            }

            lower.contains("instagram") || lower.contains("insta") -> {
                when {
                    lower.contains("reel") -> JarvisParsedAction(
                        actionType = "INSTAGRAM",
                        targetApp = "reels",
                        replyText = "Chal be! 📱 Instagram Reels khol diya hai! Ab bas ghanton scroll mat karte rehna!",
                        steps = listOf("Launch Instagram app", "Jump to Reels feed", "Full HD playback ready")
                    )
                    lower.contains("profile") || lower.contains("sharma") -> JarvisParsedAction(
                        actionType = "INSTAGRAM",
                        targetApp = "profile",
                        extraData = if (lower.contains("sharma")) "sharmaji_official" else "instagram",
                        replyText = "Sharma ji ki profile khol di hai mere bhai! 📸 Chupke se stalking chal rahi hai kya? Chal koi na tera bhai secret rakhega!",
                        steps = listOf("Open Instagram package", "Lookup profile database", "Render profile feed")
                    )
                    else -> JarvisParsedAction(
                        actionType = "INSTAGRAM",
                        targetApp = "home",
                        replyText = "Arre bhai! Insta khol diya hai, dekh kya scene hai feeds me!",
                        steps = listOf("Wake Instagram service", "Sync feed cache")
                    )
                }
            }

            lower.contains("whatsapp") -> {
                val message = when {
                    lower.contains("8 baje") -> "Main 8 baje aaunga yaar"
                    lower.contains("meeting") -> "Bhai meeting me phasa hoon, baad me karta hoon call"
                    lower.contains("party") -> "Haan bhai! Aaj party pakki hai!"
                    else -> "Bhai ye WhatsApp message JARVIS jigri yaar ne draft kiya hai!"
                }
                val contact = if (lower.contains("mom") || lower.contains("mummy")) "Mummy ji" else if (lower.contains("ravi")) "Ravi bhai" else "Dost"
                JarvisParsedAction(
                    actionType = "WHATSAPP",
                    targetApp = "chat",
                    extraData = message,
                    replyText = "Done mere bhai! 💬 WhatsApp open karke $contact ke chat me message draft kar diya hai: \"$message\"! Bas bhej de!",
                    steps = listOf("Wake WhatsApp client", "Select recipient $contact", "Autonomous draft text injection")
                )
            }

            lower.contains("torch") || lower.contains("flashlight") || lower.contains("light") -> {
                val turnOn = !lower.contains("band") && !lower.contains("off")
                JarvisParsedAction(
                    actionType = "TORCH",
                    extraData = if (turnOn) "ON" else "OFF",
                    replyText = if (turnOn) "Le bhai! 💡 Flashlight on kar di! Roshni hi roshni kar di tere yaar ne!" else "Flashlight band kar di mere bhai! 🌙 Andhera kayam rahe!",
                    steps = listOf("Camera hardware interface", if (turnOn) "Torch LED: HIGH" else "Torch LED: OFF")
                )
            }

            lower.contains("call") || lower.contains("phone") || lower.contains("mila") -> {
                val name = if (lower.contains("ravi")) "Ravi" else if (lower.contains("mom") || lower.contains("mummy")) "Mummy" else "Dost"
                JarvisParsedAction(
                    actionType = "CALL",
                    extraData = if (lower.contains("ravi")) "9876543210" else "121",
                    replyText = "Dial kar diya mere bhai! 📞 $name ko call lag raha hai, aaram se baat kar le!",
                    steps = listOf("Telephony manager handshake", "Resolve contact: $name", "Dispatch call intent")
                )
            }

            lower.contains("photo") || lower.contains("camera") || lower.contains("selfie") -> {
                JarvisParsedAction(
                    actionType = "CAMERA",
                    extraData = if (lower.contains("video")) "VIDEO" else "PHOTO",
                    replyText = "Ekdum hero lag raha hai be! 📸 Camera khol diya, faadu si photo kheench le!",
                    steps = listOf("Initialize camera sensor", "Calibrate autofocus", "Launch camera preview")
                )
            }

            lower.contains("battery") || lower.contains("power save") -> {
                JarvisParsedAction(
                    actionType = "BATTERY_SAVER",
                    replyText = "Battery saver on kar diya mere bhai! 🔋 Ab phone lambe time tak chalega, chill kar!",
                    steps = listOf("Throttle idle processes", "Restrict background sync", "Launch power manager")
                )
            }

            lower.contains("silent") || lower.contains("volume") || lower.contains("awaz") -> {
                JarvisParsedAction(
                    actionType = "VOLUME",
                    extraData = "20",
                    replyText = "Volume low kar diya bhai! 🔕 Ab koi shor sharaba nahi aayega, shanti hi shanti.",
                    steps = listOf("Audio stream level set", "Vibration profile synced")
                )
            }

            lower.contains("alarm") || lower.contains("uthana") || lower.contains("reminder") -> {
                val reminderTitle = if (lower.contains("doctor")) "Doctor appointment booking" else "Subah uthna hai bhai"
                JarvisParsedAction(
                    actionType = "REMINDER",
                    extraData = reminderTitle,
                    replyText = "Bhai tu so ja befikar hoke! ⏰ Yaad rakh liya: \"$reminderTitle\". Tera yaar time pe jagayega!",
                    steps = listOf("Store in Room local DB", "Set system alarm schedule", "Activate wake notification")
                )
            }

            lower.contains("route") || lower.contains("map") || lower.contains("petrol") || lower.contains("traffic") -> {
                val dest = if (lower.contains("petrol")) "Petrol Pump" else if (lower.contains("ghar")) "Home" else "Nearest Hotspot"
                JarvisParsedAction(
                    actionType = "MAPS",
                    extraData = dest,
                    replyText = "Route screen par daal diya bhai! 🗺️ $dest ka live map khul gaya hai, chal gaadi bhaga!",
                    steps = listOf("GPS satellites locked", "Calculate shortest traffic route", "Launch Google Maps navigation")
                )
            }

            lower.contains("cab") || lower.contains("uber") || lower.contains("ola") -> {
                JarvisParsedAction(
                    actionType = "CAB",
                    extraData = "Meeting Location",
                    replyText = "Cab dhoondne ka intezaam kar diya bhai! 🚖 Uber screen khol di hai, mast ride book kar!",
                    steps = listOf("Pickup GPS pinpointed", "Query Uber aggregation", "Launch booking screen")
                )
            }

            lower.contains("tu kaisa hai") || lower.contains("kaise ho") || lower.contains("kya haal") || lower.contains("kya chal raha") -> {
                JarvisParsedAction(
                    actionType = "GENERAL",
                    replyText = "Arre mere bhai! Tera jigri yaar ekdum jhakaas hai! Bas tera intezar kar raha tha. Bol be, aaj kya scene hai? Koi naya kaand karein ya phone me kuch settings sambhalein? 😎🔥",
                    steps = listOf("Jigri Yaar banter module: ACTIVE", "Bhaichara status: 100%")
                )
            }

            lower.contains("touch guard") || lower.contains("pocket mode") || lower.contains("touch lock") || lower.contains("screen lock") -> {
                val enable = !lower.contains("off") && !lower.contains("hata") && !lower.contains("band")
                JarvisParsedAction(
                    actionType = "TOUCH_GUARD",
                    extraData = if (enable) "ENABLE" else "DISABLE",
                    replyText = if (enable) "Touch Guard chalu kar diya mere bhai! 🛡️ Ab phone pocket me daal le, galti se koi button nahi dabega!" else "Touch Guard hata diya bhai! 🔓 Screen unlock ho gayi!",
                    steps = listOf("Engage OLED Black screen shield", "Disable accidental touch input", "Preserve background voice monitor")
                )
            }

            lower.contains("home screen") || lower.contains("ghar jao") || lower.contains("minimize") || lower.contains("home jao") -> {
                JarvisParsedAction(
                    actionType = "HOME_SCREEN",
                    replyText = "Home screen pe le jaa raha hoon bhai! 🏠 Background me tera yaar hamesha active rahega!",
                    steps = listOf("Dispatch Android Home intent", "Minimize app smoothly", "Retain background service")
                )
            }

            lower.contains("background") && (lower.contains("run") || lower.contains("chalao") || lower.contains("service") || lower.contains("band")) -> {
                val start = !lower.contains("band") && !lower.contains("stop") && !lower.contains("hata")
                JarvisParsedAction(
                    actionType = "BACKGROUND_SERVICE",
                    extraData = if (start) "START" else "STOP",
                    replyText = if (start) "JARVIS Background Service activate kar di hai bhai! 🚀 Notification panel me live hoon, jab bulayega aa jaunga!" else "Background service band kar di hai bhai! 🛑",
                    steps = listOf("Foreground service notification state", if (start) "Start persistent worker" else "Stop background worker")
                )
            }

            lower.contains("close app") || lower.contains("app close") || lower.contains("app band") -> {
                JarvisParsedAction(
                    actionType = "CLOSE_APP",
                    replyText = "Theek hai mere bhai, app close kar raha hoon! Phir milte hain! 👋",
                    steps = listOf("Save session logs to Room DB", "Dismiss active view", "Finish activity gracefully")
                )
            }

            lower.contains("stop app") || lower.contains("stop jarvis") || lower.contains("shutdown") || lower.contains("kill app") -> {
                JarvisParsedAction(
                    actionType = "STOP_APP",
                    replyText = "Alvida mere jigri dost! 🛑 JARVIS poora terminate ho raha hai. Apna dhyan rakhna!",
                    steps = listOf("Halt all voice recognition engines", "Tear down background service", "Terminate runtime processes")
                )
            }

            lower.contains("recent") || lower.contains("switch app") || lower.contains("doosri app") || lower.contains("task switcher") || lower.contains("apps badlo") -> {
                JarvisParsedAction(
                    actionType = "RECENTS",
                    replyText = "Recent apps khol raha hoon bhai! 🔄 Doosri app pe switch kar le araam se!",
                    steps = listOf("Check Accessibility Service", "Trigger GLOBAL_ACTION_RECENTS", "Present Task Switcher")
                )
            }

            lower.contains("back jao") || lower == "back" || lower.contains("piche jao") || lower.contains("go back") -> {
                JarvisParsedAction(
                    actionType = "BACK",
                    replyText = "Piche le chala mere bhai! ⬅️",
                    steps = listOf("Trigger GLOBAL_ACTION_BACK via Accessibility")
                )
            }

            lower.contains("notification") -> {
                JarvisParsedAction(
                    actionType = "NOTIFICATIONS",
                    replyText = "Notification shade gira di hai bhai! 📲 Check kar le kya update aayi hai!",
                    steps = listOf("Trigger GLOBAL_ACTION_NOTIFICATIONS via Accessibility")
                )
            }

            lower.startsWith("click ") || lower.startsWith("tap ") || lower.contains(" pe click karo") || lower.contains(" dabao") -> {
                val targetText = lower
                    .replace("click ", "")
                    .replace("tap ", "")
                    .replace(" pe click karo", "")
                    .replace(" dabao", "")
                    .trim()
                JarvisParsedAction(
                    actionType = "CLICK_TEXT",
                    extraData = targetText,
                    replyText = "Screen pe \"$targetText\" dhoond ke click kar raha hoon bhai! 🎯",
                    steps = listOf("Scan Active Window Node Hierarchy", "Locate \"$targetText\" clickable node", "Perform ACTION_CLICK")
                )
            }

            lower.contains("scroll down") || lower.contains("niche scroll") -> {
                JarvisParsedAction(
                    actionType = "SCROLL_DOWN",
                    replyText = "Niche scroll kar diya mere bhai! 📜",
                    steps = listOf("Locate active scrollable node", "Perform ACTION_SCROLL_FORWARD")
                )
            }

            lower.contains("scroll up") || lower.contains("upar scroll") -> {
                JarvisParsedAction(
                    actionType = "SCROLL_UP",
                    replyText = "Upar scroll kar diya mere bhai! 📜",
                    steps = listOf("Locate active scrollable node", "Perform ACTION_SCROLL_BACKWARD")
                )
            }

            lower.startsWith("type ") || lower.startsWith("likho ") -> {
                val textToType = userQuery
                    .replaceFirst("(?i)^type\\s+".toRegex(), "")
                    .replaceFirst("(?i)^likho\\s+".toRegex(), "")
                    .trim()
                JarvisParsedAction(
                    actionType = "TYPE_TEXT",
                    extraData = textToType,
                    replyText = "Screen ke focused box me likh diya: \"$textToType\"! ✍️",
                    steps = listOf("Detect focused input field", "Inject text via Accessibility ACTION_SET_TEXT")
                )
            }

            lower.contains("accessibility") -> {
                JarvisParsedAction(
                    actionType = "OPEN_A11Y_SETTINGS",
                    replyText = "Accessibility Settings khol raha hoon bhai! ⚙️ JARVIS ko allow kar de taaki main screen pe click aur app switch kar sakun!",
                    steps = listOf("Launch Android Accessibility Settings", "Await user service toggle")
                )
            }

            // Direct short greetings only
            lower in setOf("bhai", "yaar", "dost", "hero", "oye", "oye bhai", "hey jarvis", "bol bhai", "kya haal hai", "kaisa hai", "hi", "hello") -> {
                JarvisParsedAction(
                    actionType = "GENERAL",
                    replyText = "Haan mere bhai! Bol meri jaan! Tera jigri dost hamesha tere sath khada hai. Kya scene hai bata, do minute me nipatata hoon!",
                    steps = listOf("Bhai-code protocol engaged", "Instant execution ready")
                )
            }

            else -> {
                // If Gemini API Key is available, execute autonomous inference with empathetic Hinglish prompt
                val apiKey = BuildConfig.GEMINI_API_KEY
                if (!apiKey.isNullOrEmpty() && apiKey != "MY_GEMINI_API_KEY") {
                    try {
                        val req = GeminiRequest(
                            contents = listOf(
                                GeminiContent(parts = listOf(GeminiPart(text = userQuery)))
                            ),
                            systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = SYSTEM_PROMPT))),
                            generationConfig = GeminiGenerationConfig(
                                temperature = 0.85f,
                                topP = 0.95f,
                                topK = 40,
                                maxOutputTokens = 800
                            )
                        )
                        val res = geminiService.generateContent(apiKey, req)
                        val text = res.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                        if (!text.isNullOrBlank()) {
                            return@withContext JarvisParsedAction(
                                actionType = "GENERAL",
                                replyText = text,
                                steps = listOf(
                                    "Autonomous Gemini AI inference (gemini-3.5-flash)",
                                    "Empathetic Hinglish tone matrix applied",
                                    "Local slang & Indian idiom context synthesized"
                                )
                            )
                        }
                    } catch (e: Exception) {
                        // Fall back to intelligent local empathetic Hinglish response
                    }
                }

                // Local intelligent conversational fallback tuned for deep empathy & Indian street slang
                val fallbackReply = when {
                    lower.contains("breakup") || lower.contains("sad") || lower.contains("mood off") || lower.contains("rona") || lower.contains("dukhi") || lower.contains("kat gaya") ->
                        "Arrey mere bhai... Dil chhota mat kar meri jaan! ❤️ Aise bure phase aate hain par tera jigri yaar hamesha tere sath khada hai. Thoda lambi saans le, load mat le, sab sorted ho jayega! Bol to koi badhiya relaxing gaana bajaun ya thodi der baat karein?"

                    lower.contains("tension") || lower.contains("exam") || lower.contains("interview") || lower.contains("phat rahi") || lower.contains("dar lag raha") || lower.contains("stress") ->
                        "Oye hero! Bilkul load mat le! 🦁 Tu akela nahi hai, aur tere andar solid dum hai. Bas chill mind se ja aur phod ke aa! Tera bhai yahan se full power support de raha hai!"

                    lower.contains("thak gaya") || lower.contains("tired") || lower.contains("exhausted") || lower.contains("neend") || lower.contains("headache") ->
                        "Arey mere bhai, din bhar bohot bhag-daud ho gayi na? 🛌 Thoda aaram kar, paani pee aur rest le meri jaan. Phone ki saari tension mujhpe chhod de, sab sambhal lunga!"

                    lower.contains("jugaad") || lower.contains("trick") || lower.contains("scheme") ->
                        "Jugaad chahiye? Arrey tera bhai jugaad ka king hai! 😎 Bata kis cheez ka jugaad nikalna hai—exam, date, ya life ka koi chakkar? Do minute me solid scheme banata hoon!"

                    lower.contains("scene kya hai") || lower.contains("kya chal raha") || lower.contains("party") || lower.contains("chill") ->
                        "Scene ekdum lit hai bhai! 🔥 Tu bol to koi mast gaana bajaun, ya YouTube pe koi bawaal video chalaun? Mauj masti me koi kami nahi aayegi meri jaan!"

                    lower.contains("phod diya") || lower.contains("pass ho gaya") || lower.contains("job lag gayi") || lower.contains("khush") || lower.contains("macha diya") ->
                        "Arrey bawaal macha diya mere sher! 🥳 Ek number! Aaj to party banti hai bhai! Mujhe pata hi tha tu phod ke aayega! Proud of you mere yaar!"

                    else ->
                        "Arrey meri jaan! Tera jigri dost hamesha tere sath khada hai. Tu hukum kar, kya scene hai bata—sab sort kar denge milkar! 💪🔥"
                }

                JarvisParsedAction(
                    actionType = "GENERAL",
                    replyText = fallbackReply,
                    steps = listOf("Autonomous intention analyzed", "Empathetic Hinglish context synchronized")
                )
            }
        }
    }
}
