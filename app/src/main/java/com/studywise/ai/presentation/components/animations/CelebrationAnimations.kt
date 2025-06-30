package com.studywise.ai.presentation.components.animations

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.*
import kotlin.random.Random

/**
 * Achievement Unlocked Animation
 */
@Composable
fun AchievementUnlockedAnimation(
    achievement: String,
    description: String,
    icon: ImageVector = Icons.Default.EmojiEvents,
    onDismiss: () -> Unit
) {
    var visible by remember { mutableStateOf(true) }
    
    LaunchedEffect(Unit) {
        delay(4000) // Show for 4 seconds
        visible = false
        delay(300) // Wait for exit animation
        onDismiss()
    }
    
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { -it },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        ) + fadeIn(),
        exit = slideOutVertically(
            targetOffsetY = { -it },
            animationSpec = tween(300)
        ) + fadeOut()
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Animated trophy icon
                val infiniteTransition = rememberInfiniteTransition()
                val rotation by infiniteTransition.animateFloat(
                    initialValue = -10f,
                    targetValue = 10f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1000),
                        repeatMode = RepeatMode.Reverse
                    )
                )
                
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(
                            MaterialTheme.colorScheme.primary,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier
                            .size(40.dp)
                            .rotate(rotation),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Achievement Unlocked!",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = achievement,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Sparkle effect
                SparkleEffect(modifier = Modifier.size(32.dp))
            }
        }
    }
}

/**
 * Streak Fire Animation
 */
@Composable
fun StreakFireAnimation(
    streakCount: Int,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition()
    
    // Fire animation values
    val fireScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(500),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    val fireOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        // Fire particles
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawFireParticles(fireOffset)
        }
        
        // Main fire icon
        Icon(
            imageVector = Icons.Default.LocalFireDepartment,
            contentDescription = "Streak",
            modifier = Modifier
                .fillMaxSize()
                .scale(fireScale),
            tint = Color(0xFFFF6B35)
        )
        
        // Streak count
        if (streakCount > 1) {
            Text(
                text = streakCount.toString(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = (-4).dp)
            )
        }
    }
}

/**
 * Confetti Celebration
 */
@Composable
fun ConfettiCelebration(
    modifier: Modifier = Modifier,
    particleCount: Int = 50,
    duration: Int = 3000
) {
    var particles by remember { 
        mutableStateOf(List(particleCount) { ConfettiParticle.random() }) 
    }
    
    val animationProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(duration, easing = LinearEasing)
    )
    
    Canvas(modifier = modifier.fillMaxSize()) {
        particles.forEach { particle ->
            drawConfettiParticle(particle, animationProgress)
        }
    }
    
    LaunchedEffect(animationProgress) {
        if (animationProgress >= 1f) {
            particles = List(particleCount) { ConfettiParticle.random() }
        }
    }
}

/**
 * Points Explosion Animation
 */
@Composable
fun PointsExplosion(
    points: Int,
    modifier: Modifier = Modifier,
    onComplete: () -> Unit = {}
) {
    var visible by remember { mutableStateOf(true) }
    val animationDuration = 1500
    
    LaunchedEffect(Unit) {
        delay(animationDuration.toLong())
        visible = false
        onComplete()
    }
    
    AnimatedVisibility(
        visible = visible,
        enter = scaleIn(
            initialScale = 0.3f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        ) + fadeIn(),
        exit = scaleOut(targetScale = 2f) + fadeOut()
    ) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            // Expanding circles
            val infiniteTransition = rememberInfiniteTransition()
            repeat(3) { index ->
                val delay = index * 200
                val scale by infiniteTransition.animateFloat(
                    initialValue = 0.5f,
                    targetValue = 2f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1000, delayMillis = delay),
                        repeatMode = RepeatMode.Restart
                    )
                )
                val alpha by infiniteTransition.animateFloat(
                    initialValue = 0.5f,
                    targetValue = 0f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1000, delayMillis = delay),
                        repeatMode = RepeatMode.Restart
                    )
                )
                
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .scale(scale)
                        .alpha(alpha)
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                            CircleShape
                        )
                )
            }
            
            // Points text
            Text(
                text = "+$points",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

/**
 * Level Up Animation
 */
@Composable
fun LevelUpAnimation(
    newLevel: Int,
    onDismiss: () -> Unit
) {
    var visible by remember { mutableStateOf(true) }
    
    LaunchedEffect(Unit) {
        delay(3000)
        visible = false
        delay(300)
        onDismiss()
    }
    
    AnimatedVisibility(
        visible = visible,
        enter = scaleIn(
            initialScale = 0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessLow
            )
        ),
        exit = scaleOut() + fadeOut()
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                StarBurstEffect()
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "LEVEL UP!",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary,
                    textAlign = TextAlign.Center
                )
                
                Text(
                    text = "Level $newLevel",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "New abilities unlocked!",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * Correct Answer Celebration
 */
@Composable
fun CorrectAnswerCelebration(
    modifier: Modifier = Modifier
) {
    val animationDuration = 800
    var isAnimating by remember { mutableStateOf(true) }
    
    LaunchedEffect(Unit) {
        delay(animationDuration.toLong())
        isAnimating = false
    }
    
    if (isAnimating) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            // Success checkmark with bounce
            val scale by animateFloatAsState(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
            
            Surface(
                modifier = Modifier
                    .size(80.dp)
                    .scale(scale),
                shape = CircleShape,
                color = Color(0xFF4CAF50)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Correct",
                        modifier = Modifier.size(48.dp),
                        tint = Color.White
                    )
                }
            }
            
            // Particle burst
            ParticleBurst(
                particleCount = 20,
                colors = listOf(
                    Color(0xFF4CAF50),
                    Color(0xFF8BC34A),
                    Color(0xFFCDDC39)
                )
            )
        }
    }
}

// Helper Components

@Composable
private fun SparkleEffect(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition()
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    Canvas(modifier = modifier) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        val radius = size.minDimension / 3
        
        repeat(8) { i ->
            val angle = (i * 45f).toRadians()
            val x = centerX + cos(angle) * radius
            val y = centerY + sin(angle) * radius
            
            drawCircle(
                color = Color.White.copy(alpha = alpha),
                radius = 2.dp.toPx(),
                center = Offset(x, y)
            )
        }
    }
}

@Composable
private fun StarBurstEffect() {
    val infiniteTransition = rememberInfiniteTransition()
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )
    
    Canvas(modifier = Modifier.size(100.dp)) {
        rotate(rotation) {
            drawStar(
                color = Color.White.copy(alpha = 0.3f),
                radius = size.minDimension / 2
            )
        }
    }
}

@Composable
private fun ParticleBurst(
    particleCount: Int,
    colors: List<Color>
) {
    val particles = remember {
        List(particleCount) {
            BurstParticle(
                angle = Random.nextFloat() * 360f,
                velocity = Random.nextFloat() * 200f + 100f,
                color = colors.random(),
                size = Random.nextFloat() * 8f + 4f
            )
        }
    }
    
    val animationProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(1000, easing = FastOutSlowInEasing)
    )
    
    Canvas(modifier = Modifier.fillMaxSize()) {
        particles.forEach { particle ->
            val distance = particle.velocity * animationProgress
            val x = center.x + cos(particle.angle.toRadians()) * distance
            val y = center.y + sin(particle.angle.toRadians()) * distance
            val alpha = 1f - animationProgress
            
            drawCircle(
                color = particle.color.copy(alpha = alpha),
                radius = particle.size,
                center = Offset(x, y)
            )
        }
    }
}

// Extension functions
private fun DrawScope.drawFireParticles(offset: Float) {
    val particleCount = 5
    repeat(particleCount) { i ->
        val x = size.width / 2 + sin(i.toFloat() + offset * 0.1f) * 20f
        val y = size.height - (i * 10f + offset)
        val alpha = (1f - (y / size.height)).coerceIn(0f, 1f)
        
        drawCircle(
            color = Color(0xFFFF6B35).copy(alpha = alpha * 0.5f),
            radius = 3.dp.toPx(),
            center = Offset(x, y)
        )
    }
}

private fun DrawScope.drawConfettiParticle(particle: ConfettiParticle, progress: Float) {
    val y = particle.startY + (size.height + 100f) * progress
    val x = particle.startX + sin(progress * particle.swaySpeed) * particle.swayAmount
    val rotation = progress * particle.rotationSpeed
    
    rotate(rotation, Offset(x, y)) {
        drawRect(
            color = particle.color,
            topLeft = Offset(x - particle.width / 2, y - particle.height / 2),
            size = androidx.compose.ui.geometry.Size(particle.width, particle.height)
        )
    }
}

private fun DrawScope.drawStar(color: Color, radius: Float) {
    val path = Path()
    val innerRadius = radius * 0.5f
    val angleStep = 360f / 10f
    
    for (i in 0..9) {
        val angle = (i * angleStep - 90f).toRadians()
        val r = if (i % 2 == 0) radius else innerRadius
        val x = center.x + cos(angle) * r
        val y = center.y + sin(angle) * r
        
        if (i == 0) {
            path.moveTo(x, y)
        } else {
            path.lineTo(x, y)
        }
    }
    path.close()
    
    drawPath(path, color)
}

private fun Float.toRadians() = this * PI.toFloat() / 180f

// Data classes
private data class ConfettiParticle(
    val startX: Float,
    val startY: Float,
    val color: Color,
    val width: Float,
    val height: Float,
    val swayAmount: Float,
    val swaySpeed: Float,
    val rotationSpeed: Float
) {
    companion object {
        fun random() = ConfettiParticle(
            startX = Random.nextFloat() * 1000f,
            startY = Random.nextFloat() * -200f,
            color = listOf(
                Color(0xFFFF6B6B),
                Color(0xFF4ECDC4),
                Color(0xFFFFE66D),
                Color(0xFF95E1D3),
                Color(0xFFF38181)
            ).random(),
            width = Random.nextFloat() * 15f + 5f,
            height = Random.nextFloat() * 10f + 3f,
            swayAmount = Random.nextFloat() * 100f + 50f,
            swaySpeed = Random.nextFloat() * 5f + 2f,
            rotationSpeed = Random.nextFloat() * 720f + 360f
        )
    }
}

private data class BurstParticle(
    val angle: Float,
    val velocity: Float,
    val color: Color,
    val size: Float
)