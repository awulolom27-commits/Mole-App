package com.modach.mole

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.*
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.media.AudioManager
import kotlin.math.abs

class MainActivity : ComponentActivity(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var proximitySensor: Sensor? = null
    private lateinit var audioManager: AudioManager
    
    // Core Application Operational States
    var appState by mutableStateOf(1) // 1 = Friendly, 2 = Tensed, 3 = Dispatched
    var voiceLog by mutableStateOf("Listening for Voice Emergency...")
    var isMuteToggleActive by mutableStateOf(false)
    var simulatedAudioVolume by mutableStateOf(0)
    var bleMeshStatus by mutableStateOf("BLE Mesh: Idle")
    
    // Monetization System States (3-Day Trial & 4-Tier Matrix Engine Layout)
    var hasUserPaidPremium by mutableStateOf(false)
    var isTrialActive by mutableStateOf(true)
    var trialDaysRemaining by mutableStateOf(3)
    var showSubscriptionOnboardingWindow by mutableStateOf(true)
    var hasAcceptedAntiSpywareCheckbox by mutableStateOf(false)
    var applicationCurrencyRegion by mutableStateOf("NGN") // "NGN", "USD", "ZAR", "INR"

    private var listeningScope = CoroutineScope(Dispatchers.Main + Job())
    private var sirenJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
        sensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL)

        setContent {
            MoleAppTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF121212)) {
                    if (showSubscriptionOnboardingWindow) {
                        SubscriptionPaywallGateScreen()
                    } else {
                        MainRadarScreenLayout()
                    }
                }
            }
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        if (event.sensor.type == Sensor.TYPE_PROXIMITY) {
            val distance = event.values
            if (distance >= (proximitySensor?.maximumRange ?: 5f) && appState == 1) {
                executeSilentTrackingPayload()
            }
        }

        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values
            val y = event.values
            val z = event.values

            if (abs(x) > 25 || abs(y) > 25 || abs(z) > 25) {
                if (appState == 1) { triggerThreatTensedState() }
            }
        }
    }

    private fun triggerThreatTensedState() {
        appState = 2
        voiceLog = "Listening for Voice Emergency..."
        
        if (!isMuteToggleActive) {
            sirenJob = listeningScope.launch {
                for (i in 1..10) {
                    delay(5000)
                    simulatedAudioVolume = i * 10
                    audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI)
                }
            }
        }

        listeningScope.launch {
            delay(15000)
            if (appState == 2) { resetToFriendlyGuardState() }
        }
    }

    fun simulateVoiceInputVerification(spokenText: String) {
        if (!isTrialActive && !hasUserPaidPremium) {
            voiceLog = "[PREMIUM LOCKED] Please activate account to clear dispatch blocks."
            return
        }

        val query = spokenText.lowercase()
        val isEmergencyMatch = query.contains("thief") || query.contains("robber") || query.contains("help") || query.contains("medical") || query.contains("ambulance")
        
        if (isEmergencyMatch) {
            val headerText = if (query.contains("medical")) "[MEDICAL PROTOCOL CONFIRMED]" else "[THIEF PROTOCOL CONFIRMED]"
            executeEmergencyTelephonyDispatch("112", headerText)
        }
    }

    private fun executeEmergencyTelephonyDispatch(number: String, protocolHeader: String) {
        sirenJob?.cancel()
        appState = 3
        voiceLog = protocolHeader
        bleMeshStatus = "Target heading towards the Main Exit / Gate"
        
        val intent = Intent(Intent.ACTION_CALL).apply { data = Uri.parse("tel:$number") }
        startActivity(intent)
    }

    private fun executeSilentTrackingPayload() {
        if (hasUserPaidPremium || isTrialActive) {
            bleMeshStatus = "Silent GPS Beacon Dropped to Group Networks"
        }
    }

    private fun resetToFriendlyGuardState() {
        sirenJob?.cancel()
        appState = 1
        simulatedAudioVolume = 0
        bleMeshStatus = "BLE Mesh: Idle"
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    // COMPLIANT 4-TIER RECURRING SUBSCRIPTION PAYWALL INTERFACE
    @Composable
    fun SubscriptionPaywallGateScreen() {
        val scrollState = rememberScrollState()
        var enteredActivationKey by remember { mutableStateOf("") }
        var activationErrorMessage by remember { mutableStateOf("") }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            Text("MODACH TECHNOLOGIES", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text("MOLE: Personal Safety App", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(16.dp))
            
            // 4-Tier Regional Currency Navigation Bar
            Row(modifier = Modifier.background(Color(0xFF1A1A1A), RoundedCornerShape(12.dp)).padding(4.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                val tiers = listOf("NGN", "USD", "ZAR", "INR")
                tiers.forEach { tier ->
                    TextButton(
                        onClick = { applicationCurrencyRegion = tier },
                        colors = ButtonDefaults.textButtonColors(containerColor = if (applicationCurrencyRegion == tier) Color(0xFF00FF66) else Color.Transparent),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        val displayLabel = when(tier) {
                            "NGN" -> "NG 🇳🇬"
                            "USD" -> "WEST 🇺🇸"
                            "ZAR" -> "MID 🇿🇦"
                            else -> "VOL 🇮🇳"
                        }
                        Text(displayLabel, color = if (applicationCurrencyRegion == tier) Color.Black else Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Value Specifications Showcase Card
            Box(modifier = Modifier.fillMaxWidth().background(Color(0xFF1A1A1A), RoundedCornerShape(16.dp)).border(1.dp, Color(0xFF00FF66), RoundedCornerShape(16.dp)).padding(16.dp)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val priceLabel = when(applicationCurrencyRegion) {
                        "NGN" -> "₦3,500 / mo"
                        "ZAR" -> "$3.00 / mo"
                        "INR" -> "$1.90 / mo"
                        else -> "$4.99 / mo"
                    }
                    val regionNotes = when(applicationCurrencyRegion) {
                        "NGN" -> "Core Local Matrix Plan"
                        "ZAR" -> "Emerging Tier 1 Economies"
                        "INR" -> "High-Volume Tier 2 Pricing"
                        else -> "Western Premium Specification"
                    }
                    Text(regionNotes, color = Color(0xFF00FF66), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = priceLabel, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("• Hands-Free 112 Voice Audio Call Dispatch\n• Real-Time Encrypted Group Location Matrix\n• Crowdsourced Local Bluetooth Landmark Mesh", color = Color.LightGray, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

Text("BILLING & ACCOUNT SETTLEMENT", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Start)
Spacer(modifier = Modifier.height(8.dp))
// AUTOMATED ONLINE CHECKOUT BUTTON
if (applicationCurrencyRegion == "NGN") {
Button(
onClick = {
val intent = Intent(Intent.ACTION_VIEW, Uri.parse("paystack.shop"))
startActivity(intent)
},
modifier = Modifier.fillMaxWidth().height(48.dp),
colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00D1FF))
) {
Text("Pay Online Instantly (Card / OPay)", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 15.sp)
}
Spacer(modifier = Modifier.height(12.dp))
}
// MULTI-CHANNEL FINANCIAL ROUTING DISPLAY PANEL
Box(modifier = Modifier.fillMaxWidth().background(Color(0xFF1E1E1E), RoundedCornerShape(12.dp)).padding(16.dp)) {
Column(horizontalAlignment = Alignment.Start) {
when (applicationCurrencyRegion) {
"NGN" -> {
Text("🇳🇬 MANUAL DIRECT BANK TRANSFER OPTIONS", color = Color(0xFF00FF66), fontWeight = FontWeight.Bold, fontSize = 12.sp)
Spacer(modifier = Modifier.height(8.dp))
Text("BANK: OPAY\nAcc No: 9160766306\nName: Michael Mololuwa Gbadamosi", color = Color.White, fontSize = 12.sp)
Spacer(modifier = Modifier.height(10.dp))
Text("BANK: WEMA BANK\nAcc No: 0290801712\nName: Michael Mololuwa Gbadamosi", color = Color.White, fontSize = 12.sp)
}
"USD" -> {
Text("🇺🇸 UNITED STATES WIRE (CLEVA)", color = Color(0xFF00D1FF), fontWeight = FontWeight.Bold, fontSize = 12.sp)
Spacer(modifier = Modifier.height(8.dp))
Text("BANK: Lead Bank\nAcc No: 213846490687\nRouting: 101019644\nType: Personal Checking\nName: Michael Gbadamosi\nAddress: 9450 SW Gemini Dr, Beaverton, OR, 97008", color = Color.White, fontSize = 11.sp)
}
"ZAR" -> {
Text("🌐 EMERGING ECONOMIES TIER 1 ($3.00)", color = Color(0xFF00D1FF), fontWeight = FontWeight.Bold, fontSize = 12.sp)
Spacer(modifier = Modifier.height(6.dp))
Text("BANK: Banking Circle S.A. (GBP App Setup)\nAcc No: 48909015\nSort Code: 60-83-82\nType: Business\nName: Bridge Building S.A.\nAddress: 2 Boulevard de la Foire, Luxembourg", color = Color.White, fontSize = 11.sp)
}
"INR" -> {
Text("🌐 HIGH-VOLUME TIER 2 ($1.90)", color = Color(0xFF00D1FF), fontWeight = FontWeight.Bold, fontSize = 12.sp)
Spacer(modifier = Modifier.height(6.dp))
Text("BANK: Banking Circle S.A. (EURO SEPA Setup)\nIBAN: LU354080000048917083\nSwift Code: BCIRLULL\nType: Personal\nName: Michael Gbadamosi\nAddress: 2 Boulevard de la Foire, Luxembourg", color = Color.White, fontSize = 11.sp)
}
}
// WHATSAPP VERIFICATION LINK CORE INTEGRATION
Spacer(modifier = Modifier.height(16.dp))
Button(
onClick = {
val contactNumber = "2349160766306"
val msg = "Hello Modach Support, I have completed my manual transfer payment for MOLE: Personal Safety App. Here is my receipt snapshot to verify my device profile grid."
val whatsappUrl = "wa.me{Uri.encode(msg)}"
val intent = Intent(Intent.ACTION_VIEW, Uri.parse(whatsappUrl))
startActivity(intent)
},
modifier = Modifier.fillMaxWidth().height(40.dp),
colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FF66))
) {
Text("Verify Manual Transfer via WhatsApp ✅", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
}
}
}
// PROPRIETARY OFFLINE PREMIUM ACTIVATION CARD CONSOLE
Spacer(modifier = Modifier.height(16.dp))
Box(modifier = Modifier.fillMaxWidth().background(Color(0xFF222222), RoundedCornerShape(12.dp)).padding(16.dp)) {
Column(horizontalAlignment = Alignment.Start) {
Text("ENTER MODACH SYSTEM ACTIVATION KEY", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
Spacer(modifier = Modifier.height(6.dp))
TextField(
value = enteredActivationKey,
onValueChange = { enteredActivationKey = it },
placeholder = { Text("Paste 6-Digit Key Here...", color = Color.Gray, fontSize = 12.sp) },
modifier = Modifier.fillMaxWidth().background(Color(0xFF121212)),
colors = TextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
)
Spacer(modifier = Modifier.height(8.dp))
Button(
onClick = {
// Validation core checks if key matches formula layout parameters
if (enteredActivationKey.startsWith("MD") && enteredActivationKey.endsWith("26") && enteredActivationKey.length == 6) {
hasUserPaidPremium = true
showSubscriptionOnboardingWindow = false
activationErrorMessage = "Premium Profile Unlocked Successfully! ✅"
} else {
activationErrorMessage = "Invalid Deployment Key. Contact support via WhatsApp. ❌"
}
},
modifier = Modifier.fillMaxWidth(),
colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FF66))
) {
Text("Activate Premium Grid", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
}
if (activationErrorMessage.isNotEmpty()) {
Spacer(modifier = Modifier.height(4.dp))
Text(activationErrorMessage, color = Color(0xFF00FF66), fontSize = 11.sp)
}
}
}
Spacer(modifier = Modifier.height(16.dp))
// MANDATORY LEGAL SAFEGUARD & ANTI-SPYWARE CHECKBOX NODE
Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
Checkbox(checked = hasAcceptedAntiSpywareCheckbox, onCheckedChange = { hasAcceptedAntiSpywareCheckbox = it })
Spacer(modifier = Modifier.width(4.dp))
Text(
text = "I agree that MOLE is a personal safety tool for my own protection. I will not use this application to track or record anyone without explicit consent.",
color = Color.LightGray, fontSize = 11.sp, lineHeight = 14.sp
)
}
Spacer(modifier = Modifier.height(16.dp))
Button(
onClick = { isTrialActive = true; showSubscriptionOnboardingWindow = false },
modifier = Modifier.fillMaxWidth().height(48.dp),
enabled = hasAcceptedAntiSpywareCheckbox, // Locks execution until legal terms are authorized
colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FF66))
) {
Text("Start 3-Day Free Trial", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 15.sp)
}
Spacer(modifier = Modifier.height(8.dp))
TextButton(onClick = { if(hasAcceptedAntiSpywareCheckbox) showSubscriptionOnboardingWindow = false }) {
Text("Continue with Limited Free Tier (Siren Alarm Only)", color = Color.Gray, fontSize = 12.sp)
}
}
}
@Composable
fun MainRadarScreenLayout() {
val themeColor = when(appState) {
1 -> Color(0xFF00FF66)
2 -> Color(0xFFFF3333)
else -> Color(0xFF00D1FF)
}
Column(modifier = Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
Text(
text = if (hasUserPaidPremium) "PREMIUM SUBSCRIPTION ACTIVE" else "TRIAL PROTECTION: $trialDaysRemaining DAYS LEFT",
color = Color(0xFF00FF66), fontSize = 11.sp, fontWeight = FontWeight.Bold
)
TextButton(onClick = { showSubscriptionOnboardingWindow = true }) {
Text("View Billing Hub", color = Color.White, fontSize = 12.sp)
}
}
Spacer(modifier = Modifier.height(20.dp))
Box(modifier = Modifier.size(220.dp).border(4.dp, themeColor, CircleShape), contentAlignment = Alignment.Center) {
Text(text = if (appState == 3) "MOLE: Dispatched" else "MOLE: Active", color = themeColor, fontSize = 20, fontWeight = FontWeight.Bold)
}
Spacer(modifier = Modifier.height(16.dp))
Text(text = if(appState == 1) "Environment: Friendly" else if(appState == 2) "Threat Level: Tensed" else "Status: Alert Transmitted", color = themeColor, fontSize = 15.sp, fontWeight = FontWeight.Medium)
Spacer(modifier = Modifier.height(32.dp))
Box(modifier = Modifier.fillMaxWidth().weight(1f).background(Color(0xFF1A1A1A), RoundedCornerShape(16.dp)).padding(20.dp), contentAlignment = Alignment.Center) {
Column(horizontalAlignment = Alignment.CenterHorizontally) {
if (appState == 2) {
Text("🎙️", fontSize = 40.sp)
Spacer(modifier = Modifier.height(8.dp))
Text(voiceLog, color = Color.White, fontSize = 15, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
} else if (appState == 3) {
Text("🛡️", fontSize = 40.sp)
Spacer(modifier = Modifier.height(8.dp))
Text(voiceLog, color = Color.White, fontSize = 15, fontWeight = FontWeight.Bold)
Text(bleMeshStatus, color = Color(0xFF00D1FF), fontSize = 14.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center)
} else {
Text("🗺️", fontSize = 40.sp)
Spacer(modifier = Modifier.height(8.dp))
Text(voiceLog, color = Color.Gray, fontSize = 14.sp)
Text(if (isTrialActive || hasUserPaidPremium) "Acoustic Dispatched Armed" else "Free Tier Locked: Siren Active Only", color = Color.Gray, fontSize = 12.sp)
}
}
}
Spacer(modifier = Modifier.height(20.dp))
Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
Row(verticalAlignment = Alignment.CenterVertically) {
Checkbox(checked = isMuteToggleActive, onCheckedChange = { isMuteToggleActive = it })
Text("Disturbance Mute Toggle", color = Color.White, fontSize = 14.sp)
}
if (appState == 1) {
Button(onClick = { triggerThreatTensedState() }, colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)) {
Text("Simulate Hardware Grab Snatch")
}
Spacer(modifier = Modifier.height(4.dp))
Button(
onClick = { isTrialActive = false; trialDaysRemaining = 0; showSubscriptionOnboardingWindow = true },
colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
) {
Text("Simulate Expire Trial (Force Paywall)")
}
}
if (appState == 2) {
Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
Button(onClick = { simulateVoiceInputVerification("Help! Thief running away!") }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF3333))) { Text("Shout 'Thief'") }
Button(onClick = { simulateVoiceInputVerification("Medical crisis, call ambulance!") }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00D1FF))) { Text("Shout 'Medical'") }
}
}
if (appState == 3) {
TextButton(onClick = { resetToFriendlyGuardState() }) { Text("Reset System Grid", color = Color.Gray) }
}
}
}
}
}
@Composable
fun MoleAppTheme(content: @Composable () -> Unit) { MaterialTheme(content = content) }
