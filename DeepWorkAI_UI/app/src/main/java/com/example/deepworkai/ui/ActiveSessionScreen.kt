package com.example.deepworkai.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.Text as M3Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import com.example.deepworkai.ui.theme.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.deepworkai.ui.theme.DeepWorkAITheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.deepworkai.viewmodel.SessionViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlin.math.roundToInt

import com.airbnb.lottie.compose.*
import es.dmoral.toasty.Toasty
import android.widget.Toast

@Composable
fun ActiveSessionScreen(
    onFinish: (com.example.deepworkai.models.EndSessionResponse?) -> Unit,
    viewModel: SessionViewModel = viewModel()
) {
    val coroutineScope = rememberCoroutineScope()
    val focusService = remember { com.example.deepworkai.network.FocusService() }
    val userId = com.example.deepworkai.network.NetworkPreferences.userId ?: "4acbc632-9cb6-4d7c-8bcc-8c3bd226f9c0"
    var sessionId by remember { mutableStateOf<String?>(null) }
    var sessionNumber by remember { mutableIntStateOf(1) }
    
    var seconds by remember { mutableIntStateOf(0) } // Count up from 0
    val maxSeconds = 1500 // 25 mins
    val targetDurationMinutes = maxSeconds / 60
    
    var distractions by remember { mutableIntStateOf(0) }
    val lifecycleOwner = LocalLifecycleOwner.current

    var isPaused by remember { mutableStateOf(false) }
    val cognitiveLoad by viewModel.cognitiveLoad.collectAsState()

    var showNextBreakDialog by remember { mutableStateOf(false) }
    var nextBreakDisplay by remember { mutableStateOf("5 minutes") }

    val context = androidx.compose.ui.platform.LocalContext.current
    var sessionStartTime by remember { mutableLongStateOf(System.currentTimeMillis()) }

    var showEarlyFinishDialog by remember { mutableStateOf(false) }

    // API Call to start the session when this screen opens
    LaunchedEffect(Unit) {
        sessionStartTime = System.currentTimeMillis()
        if (!com.example.deepworkai.utils.AppUsageTracker.hasUsageStatsPermission(context)) {
            com.example.deepworkai.utils.AppUsageTracker.requestUsageStatsPermission(context)
        }
        val session = focusService.startSession(userId)
        if (session != null) {
            sessionId = session.id
            sessionNumber = session.sessionNumber
        }
        
        // Late-Night Toast Warning
        val currentHour = java.time.LocalTime.now().hour
        if (currentHour in 2..4) {
            Toasty.error(context, "It's late, you should take a sleep.", Toast.LENGTH_LONG, true).show()
        }
    }

    // Timer Logic: Increments every second if not paused
    LaunchedEffect(isPaused) {
        while (seconds < maxSeconds) {
            if (!isPaused) {
                delay(1000)
                seconds++
            } else {
                delay(500) // Polling interval while paused
            }
        }
        // Auto-finish if timer completes
        if (seconds >= maxSeconds) {
            coroutineScope.launch {
                var finalResult: com.example.deepworkai.models.EndSessionResponse? = null
                sessionId?.let { id ->
                    val endTime = System.currentTimeMillis()
                    val apps = com.example.deepworkai.utils.AppUsageTracker.getUsedApps(context, sessionStartTime, endTime)
                    finalResult = focusService.endSession(id, distractions, apps, targetDurationMinutes)
                }
                onFinish(finalResult)
            }
        }
    }

    // Distraction Logic: Detects if user leaves the app
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                distractions++
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(
        containerColor = Color(0xFF0D1117) // Deep dark blue-black background matching the image
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            
            // 1. Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Bolt, contentDescription = "Mode", tint = Color(0xFF2DD4BF), modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                M3Text("DEEP FOCUS MODE", color = Color(0xFF94A3B8), fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.weight(1f))
                Icon(Icons.Default.Tune, contentDescription = "Settings", tint = Color(0xFF94A3B8))
            }

            Spacer(modifier = Modifier.height(48.dp))

            // 2. Subtitle
            M3Text("Session #$sessionNumber • Coding", color = Color(0xFF64748B), fontSize = 14.sp)

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Flow State Badge
            Surface(
                color = Color(0xFF0F766E).copy(alpha = 0.15f),
                shape = CircleShape,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF0F766E).copy(alpha = 0.5f))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Box(modifier = Modifier.size(6.dp).background(Color(0xFF2DD4BF), CircleShape))
                    Spacer(modifier = Modifier.width(8.dp))
                    M3Text(
                        text = if (isPaused) "SESSION PAUSED" else "FLOW STATE DETECTED",
                        color = if (isPaused) Color(0xFFFACC15) else Color(0xFF2DD4BF),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 4. Timer Area
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(280.dp)) {
                // Background Ellipses
                Canvas(modifier = Modifier.fillMaxSize()) {
                    withTransform({
                        rotate(degrees = -25f)
                        scale(scaleX = 0.9f, scaleY = 1.3f)
                    }) {
                        drawCircle(
                            color = Color(0xFF3B82F6).copy(alpha = 0.2f),
                            style = Stroke(width = 1.dp.toPx())
                        )
                    }
                    withTransform({
                        rotate(degrees = 55f)
                        scale(scaleX = 0.9f, scaleY = 1.3f)
                    }) {
                        drawCircle(
                            color = Color(0xFF10B981).copy(alpha = 0.2f),
                            style = Stroke(width = 1.dp.toPx())
                        )
                    }
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    M3Text(
                        text = formatTime(seconds),
                        color = Color.White,
                        fontSize = 88.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-2).sp,
                        modifier = Modifier.blur(if (isPaused) 8.dp else 0.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    // Progress bar
                    Canvas(modifier = Modifier.width(100.dp).height(4.dp)) {
                        val totalWidth = size.width
                        drawLine(
                            color = Color(0xFF1E293B),
                            start = Offset(0f, size.height / 2),
                            end = Offset(totalWidth, size.height / 2),
                            strokeWidth = size.height,
                            cap = StrokeCap.Round
                        )
                        val progressRatio = (seconds.toFloat() / maxSeconds.toFloat()).coerceIn(0f, 1f)
                        drawLine(
                            color = Color(0xFF0EA5E9), // cyan-ish blue
                            start = Offset(0f, size.height / 2),
                            end = Offset(totalWidth * progressRatio, size.height / 2),
                            strokeWidth = size.height,
                            cap = StrokeCap.Round
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // 5. Cards Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                MetricCard(
                    icon = Icons.Default.NotificationsOff,
                    value = distractions.toString(),
                    label = "DISTRACTIONS",
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    icon = Icons.Default.Psychology,
                    value = if (cognitiveLoad == "High") "92%" else if (cognitiveLoad == "Medium") "85%" else "64%",
                    label = "BRAIN LOAD ($cognitiveLoad)",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // 6. Next Break
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { showNextBreakDialog = true }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                M3Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(color = Color(0xFF64748B))) {
                            append("Next break in ")
                        }
                        withStyle(style = SpanStyle(color = Color(0xFFCBD5E1))) {
                            append(nextBreakDisplay)
                        }
                    },
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(Icons.Default.Tune, contentDescription = "Edit Break", tint = Color(0xFF64748B), modifier = Modifier.size(12.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 7. Action Button Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Coffee Button
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (isPaused) Color(0xFFFACC15).copy(alpha = 0.2f) else Color(0xFF171A21),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isPaused) Color(0xFFFACC15) else Color.White.copy(alpha=0.03f)),
                    modifier = Modifier.size(64.dp).clickable { isPaused = !isPaused }
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(
                            imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.LocalCafe,
                            contentDescription = if (isPaused) "Resume" else "Pause",
                            tint = if (isPaused) Color(0xFFFACC15) else Color.White
                        )
                    }
                }

                // Slide to Finish
                SlideToFinish(
                    modifier = Modifier.weight(1f),
                    onFinished = {
                        if (seconds < maxSeconds && seconds > 5) { // Only show if more than 5 seconds but less than goal
                            showEarlyFinishDialog = true
                        } else {
                            coroutineScope.launch {
                                var finalResult: com.example.deepworkai.models.EndSessionResponse? = null
                                sessionId?.let { id ->
                                    val endTime = System.currentTimeMillis()
                                    val apps = com.example.deepworkai.utils.AppUsageTracker.getUsedApps(context, sessionStartTime, endTime)
                                    finalResult = focusService.endSession(id, distractions, apps, targetDurationMinutes)
                                }
                                onFinish(finalResult)
                            }
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 8. Audio Player
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFF11151A),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha=0.03f)),
                modifier = Modifier.fillMaxWidth().height(84.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Equalizer Icon Box
                    Box(
                        modifier = Modifier.size(48.dp).background(Color(0xFF064E3B).copy(alpha=0.8f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.BarChart, contentDescription = "Equalizer", tint = Color(0xFF34D399), modifier = Modifier.size(24.dp))
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        M3Text("Binaural Beats 40Hz", color = Color(0xFFF1F5F9), fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(2.dp))
                        M3Text("Focus Frequency", color = Color(0xFF64748B), fontSize = 12.sp)
                    }

                    Icon(Icons.Default.SkipNext, contentDescription = "Next", tint = Color(0xFF94A3B8), modifier = Modifier.size(20.dp).clickable { })
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Box(
                        modifier = Modifier.size(44.dp).background(Color(0xFF262A33), CircleShape).clickable { },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Pause, contentDescription = "Pause", tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }

        if (showNextBreakDialog) {
            AlertDialog(
                onDismissRequest = { showNextBreakDialog = false },
                containerColor = Color(0xFF13171D),
                title = {
                    M3Text("Set Next Break", fontWeight = FontWeight.Bold)
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        val breaks = listOf("5 minutes", "10 minutes", "15 minutes", "20 minutes")
                        breaks.forEach { breakTime ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        nextBreakDisplay = breakTime
                                        showNextBreakDialog = false
                                    },
                                color = if (nextBreakDisplay == breakTime) DeepWorkBlue.copy(alpha = 0.2f) else Color.Transparent,
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (nextBreakDisplay == breakTime) DeepWorkBlue else Color.White.copy(alpha=0.03f))
                            ) {
                                M3Text(
                                    breakTime,
                                    modifier = Modifier.padding(16.dp),
                                    color = if (nextBreakDisplay == breakTime) DeepWorkBlue else Color.White,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showNextBreakDialog = false }) {
                        M3Text("Cancel", color = Color.White)
                    }
                }
            )
        }

        if (showEarlyFinishDialog) {
            EarlyFinishDialog(
                remainingSeconds = maxSeconds - seconds,
                onDismiss = { showEarlyFinishDialog = false },
                onConfirm = {
                    showEarlyFinishDialog = false
                    coroutineScope.launch {
                        var finalResult: com.example.deepworkai.models.EndSessionResponse? = null
                        sessionId?.let { id ->
                            val endTime = System.currentTimeMillis()
                            val apps = com.example.deepworkai.utils.AppUsageTracker.getUsedApps(context, sessionStartTime, endTime)
                            finalResult = focusService.endSession(id, distractions, apps, targetDurationMinutes)
                        }
                        onFinish(finalResult)
                    }
                }
            )
        }
    }
}

@Composable
fun EarlyFinishDialog(
    remainingSeconds: Int,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val composition by rememberLottieComposition(LottieCompositionSpec.Url("https://lottie.host/80164c8d-9057-4589-94d3-0599f6671a5c/A8n7U6jC8U.json"))
    val progress by animateLottieCompositionAsState(composition, iterations = LottieConstants.IterateForever)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF13171D),
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                LottieAnimation(
                    composition = composition,
                    progress = { progress },
                    modifier = Modifier.size(120.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                M3Text("Oops! Staying focused?", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            val minutes = remainingSeconds / 60
            val secs = remainingSeconds % 60
            val timeText = if (minutes > 0) "$minutes min $secs sec" else "$secs seconds"
            
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                M3Text(
                    text = "You still have $timeText left to complete your deep work target.",
                    color = Color(0xFF94A3B8),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                M3Text(
                    text = "Ending now will lower your focus score.",
                    color = Color(0xFFF87171),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF87171).copy(alpha = 0.5f))
            ) {
                M3Text("Finish Anyway", color = Color(0xFFF87171))
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = DeepWorkBlue)
            ) {
                M3Text("Keep Going", color = Color.White)
            }
        },
        shape = RoundedCornerShape(28.dp)
    )
}

@Composable
fun MetricCard(icon: androidx.compose.ui.graphics.vector.ImageVector, value: String, label: String, modifier: Modifier) {
    Surface(
        modifier = modifier.height(110.dp),
        color = Color(0xFF13171D),
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha=0.03f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(12.dp))
            M3Text(value, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            M3Text(label, color = Color(0xFF64748B), fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
        }
    }
}

fun formatTime(totalSeconds: Int): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}

@Composable
fun SlideToFinish(modifier: Modifier = Modifier, onFinished: () -> Unit) {
    val thumbSize = 56.dp
    val swipeState = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()
    var maxWidthPx by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = modifier
            .height(64.dp)
            .background(Color(0xFF121822), RoundedCornerShape(32.dp))
            .border(1.dp, Color.White.copy(alpha=0.03f), RoundedCornerShape(32.dp))
            .padding(4.dp)
            .onSizeChanged { size ->
                maxWidthPx = size.width.toFloat()
            }
    ) {
        // Background text
        M3Text(
            text = "Slide to Finish   >", 
            color = Color(0xFF2563EB), 
            modifier = Modifier.align(Alignment.Center).offset(x = 10.dp),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )

        // The thumb
        val thumbPx = with(LocalDensity.current) { thumbSize.toPx() }
        val maxPx = maxWidthPx - thumbPx

        if (maxWidthPx > 0) {
            Box(
                modifier = Modifier
                    .offset { IntOffset(swipeState.value.roundToInt(), 0) }
                    .size(thumbSize)
                    .background(Color(0xFF2563EB), RoundedCornerShape(24.dp))
                    .pointerInput(maxPx) {
                        detectHorizontalDragGestures(
                            onDragEnd = {
                                coroutineScope.launch {
                                    if (swipeState.value > maxPx * 0.8f) {
                                        swipeState.animateTo(maxPx)
                                        delay(100)
                                        onFinished()
                                    } else {
                                        swipeState.animateTo(0f, tween(300))
                                    }
                                }
                            },
                            onHorizontalDrag = { _, dragAmount ->
                                coroutineScope.launch {
                                    val newValue = (swipeState.value + dragAmount).coerceIn(0f, maxPx)
                                    swipeState.snapTo(newValue)
                                }
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                // Small inner white square
                Box(modifier = Modifier.size(10.dp).background(Color.White, RoundedCornerShape(2.dp)))
            }
        }
    }
}

@Preview(
    showBackground = true,
    device = Devices.PIXEL_7,
    name = "Active Session - Deep Work Mode",
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun ActiveSessionPreview() {
    DeepWorkAITheme {
        ActiveSessionScreen(onFinish = { _ -> })
    }
}
