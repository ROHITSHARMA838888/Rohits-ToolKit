package com.rohit.toolkit.ui.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HomeRepairService
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    // 5-second target duration with smooth animations
    LaunchedEffect(Unit) {
        delay(5000L)
        onSplashFinished()
    }

    // Logo entrance scale animation
    val logoScale = remember { Animatable(0.6f) }
    val logoAlpha = remember { Animatable(0f) }
    val textAlpha = remember { Animatable(0f) }

    // Pulsing energy ring animation
    val infiniteTransition = rememberInfiniteTransition(label = "energy_ring")
    val ringPulse by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ring_pulse"
    )

    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_pulse"
    )

    LaunchedEffect(Unit) {
        logoScale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }

    LaunchedEffect(Unit) {
        logoAlpha.animateTo(1f, animationSpec = tween(900))
        delay(300)
        textAlpha.animateTo(1f, animationSpec = tween(1000))
    }

    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF0F172A),
            Color(0xFF1E1B4B),
            Color(0xFF090D16)
        )
    )

    val energyRingBrush = Brush.sweepGradient(
        colors = listOf(
            Color(0xFF38BDF8),
            Color(0xFF6366F1),
            Color(0xFFA855F7),
            Color(0xFFEC4899),
            Color(0xFF38BDF8)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient),
        contentAlignment = Alignment.Center
    ) {
        // Ambient background glow
        Box(
            modifier = Modifier
                .size(280.dp)
                .scale(ringPulse)
                .alpha(glowPulse * 0.45f)
                .blur(48.dp)
                .background(Color(0xFF6366F1), CircleShape)
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            // Central Logo with Energy Ring
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(160.dp)
                    .scale(logoScale.value)
                    .alpha(logoAlpha.value)
            ) {
                // Outer glowing energy ring
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .scale(ringPulse)
                        .border(
                            width = 3.dp,
                            brush = energyRingBrush,
                            shape = CircleShape
                        )
                )

                // Secondary soft ring
                Box(
                    modifier = Modifier
                        .size(126.dp)
                        .border(
                            width = 1.5.dp,
                            color = Color(0xFF6366F1).copy(alpha = 0.5f),
                            shape = CircleShape
                        )
                )

                // Inner Logo Card
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0xFF312E81), Color(0xFF1E1B4B))
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.HomeRepairService,
                        contentDescription = "Rohit's ToolKit Logo",
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // App Name & Tagline
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.alpha(textAlpha.value)
            ) {
                Text(
                    text = "Rohit's ToolKit",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.2.sp
                    ),
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "All Your Tools, In One App",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.5.sp
                    ),
                    color = Color(0xFF94A3B8)
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Author Credit Badge
                Box(
                    modifier = Modifier
                        .background(
                            color = Color(0xFF1E293B).copy(alpha = 0.7f),
                            shape = CircleShape
                        )
                        .border(
                            width = 1.dp,
                            color = Color(0xFF475569).copy(alpha = 0.5f),
                            shape = CircleShape
                        )
                        .padding(horizontal = 18.dp, vertical = 7.dp)
                ) {
                    Text(
                        text = "Made by Rohit Sharma",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = Color(0xFFA5B4FC)
                    )
                }
            }
        }
    }
}
