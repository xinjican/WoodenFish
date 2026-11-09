package com.example.woodenfish

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WoodenFishApp()
        }
    }
}

data class FloatingText(val id: Long, val text: String = "功德 +1")

@Composable
fun WoodenFishApp() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var count by remember { mutableIntStateOf(0) }
    val scale = remember { Animatable(1f) }
    val floatingTexts = remember { mutableStateListOf<FloatingText>() }

    val soundPool = remember {
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        SoundPool.Builder().setMaxStreams(5).setAudioAttributes(attrs).build()
    }
    val soundId = remember { soundPool.load(context, R.raw.muyu_sound, 1) }

    DisposableEffect(Unit) {
        onDispose {
            soundPool.release()
        }
    }

    fun triggerVibration() {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(35, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(35)
            }
        } catch (_: Exception) {}
    }

    fun onKnock() {
        count++
        try {
            if (soundId != 0) {
                soundPool.play(soundId, 1f, 1f, 0, 0, 1f)
            }
        } catch (_: Exception) {}
        triggerVibration()

        coroutineScope.launch {
            scale.snapTo(0.88f)
            scale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }

        floatingTexts.add(FloatingText(id = System.currentTimeMillis()))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "当前功德",
                color = Color(0xFF888888),
                fontSize = 18.sp,
                fontWeight = FontWeight.Normal
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "$count",
                color = Color(0xFFFFD54F),
                fontSize = 54.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(50.dp))

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(260.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_muyu),
                    contentDescription = "木鱼",
                    modifier = Modifier
                        .size(220.dp)
                        .scale(scale.value)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            onKnock()
                        }
                )

                floatingTexts.forEach { item ->
                    FloatingLabel(
                        item = item,
                        onAnimationEnd = { floatingTexts.remove(item) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
            Text(
                text = "点击木鱼 · 心如止水",
                color = Color(0xFF555555),
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun FloatingLabel(item: FloatingText, onAnimationEnd: () -> Unit) {
    val offsetY = remember { Animatable(0f) }
    val alpha = remember { Animatable(1f) }

    LaunchedEffect(item.id) {
        coroutineScope {
            launch {
                offsetY.animateTo(-100f, animationSpec = tween(durationMillis = 650))
            }
            launch {
                alpha.animateTo(0f, animationSpec = tween(durationMillis = 650))
            }
        }
        onAnimationEnd()
    }

    Text(
        text = item.text,
        color = Color(0xFFFFF8E1),
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .offset { IntOffset(0, offsetY.value.toInt()) }
            .alpha(alpha.value)
    )
}
