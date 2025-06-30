package com.studywise.ai.presentation.components.animations

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.unit.IntOffset

/**
 * Smooth fade and scale transition
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SmoothContentTransition(
    targetState: Any,
    modifier: Modifier = Modifier,
    content: @Composable AnimatedVisibilityScope.(targetState: Any) -> Unit
) {
    AnimatedContent(
        targetState = targetState,
        modifier = modifier,
        transitionSpec = {
            fadeIn(
                animationSpec = tween(
                    durationMillis = 300,
                    easing = FastOutSlowInEasing
                )
            ) + scaleIn(
                initialScale = 0.92f,
                transformOrigin = TransformOrigin.Center,
                animationSpec = tween(
                    durationMillis = 300,
                    easing = FastOutSlowInEasing
                )
            ) with fadeOut(
                animationSpec = tween(
                    durationMillis = 200,
                    easing = FastOutLinearInEasing
                )
            ) + scaleOut(
                targetScale = 0.92f,
                transformOrigin = TransformOrigin.Center,
                animationSpec = tween(
                    durationMillis = 200,
                    easing = FastOutLinearInEasing
                )
            )
        },
        content = content
    )
}

/**
 * Slide up animation for new content
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SlideUpTransition(
    targetState: Any,
    modifier: Modifier = Modifier,
    content: @Composable AnimatedVisibilityScope.(targetState: Any) -> Unit
) {
    AnimatedContent(
        targetState = targetState,
        modifier = modifier,
        transitionSpec = {
            slideInVertically(
                initialOffsetY = { height -> height },
                animationSpec = tween(
                    durationMillis = 400,
                    easing = FastOutSlowInEasing
                )
            ) + fadeIn(
                animationSpec = tween(
                    durationMillis = 400,
                    delayMillis = 100,
                    easing = LinearEasing
                )
            ) with slideOutVertically(
                targetOffsetY = { height -> -height / 3 },
                animationSpec = tween(
                    durationMillis = 300,
                    easing = FastOutLinearInEasing
                )
            ) + fadeOut(
                animationSpec = tween(
                    durationMillis = 200,
                    easing = LinearEasing
                )
            )
        },
        content = content
    )
}

/**
 * Card flip transition
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun CardFlipTransition(
    targetState: Boolean,
    modifier: Modifier = Modifier,
    front: @Composable AnimatedVisibilityScope.() -> Unit,
    back: @Composable AnimatedVisibilityScope.() -> Unit
) {
    AnimatedContent(
        targetState = targetState,
        modifier = modifier,
        transitionSpec = {
            if (targetState) {
                // Flip to back
                (fadeIn(animationSpec = tween(200, delayMillis = 200)) +
                        scaleIn(
                            initialScale = 0.8f,
                            transformOrigin = TransformOrigin(0.5f, 0.5f),
                            animationSpec = tween(400)
                        )) with
                        (fadeOut(animationSpec = tween(200)) +
                                scaleOut(
                                    targetScale = 0.8f,
                                    transformOrigin = TransformOrigin(0.5f, 0.5f),
                                    animationSpec = tween(400)
                                ))
            } else {
                // Flip to front
                (fadeIn(animationSpec = tween(200, delayMillis = 200)) +
                        scaleIn(
                            initialScale = 0.8f,
                            transformOrigin = TransformOrigin(0.5f, 0.5f),
                            animationSpec = tween(400)
                        )) with
                        (fadeOut(animationSpec = tween(200)) +
                                scaleOut(
                                    targetScale = 0.8f,
                                    transformOrigin = TransformOrigin(0.5f, 0.5f),
                                    animationSpec = tween(400)
                                ))
            }
        }
    ) { isBack ->
        if (isBack) {
            back()
        } else {
            front()
        }
    }
}

/**
 * Animated progress bar with smooth transitions
 */
@Composable
fun AnimatedProgressBar(
    progress: Float,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessLow
        )
    )
    
    androidx.compose.material3.LinearProgressIndicator(
        progress = { animatedProgress },
        modifier = modifier
    )
}

/**
 * Pulsating effect for important elements
 */
@Composable
fun PulsatingEffect(
    modifier: Modifier = Modifier,
    content: @Composable (Modifier) -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition()
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    content(modifier.scale(scale))
}

/**
 * Shimmer effect for loading states
 */
@Composable
fun ShimmerEffect(
    modifier: Modifier = Modifier,
    content: @Composable (Modifier) -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition()
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    content(modifier.alpha(alpha))
}

/**
 * Bounce animation for interactive elements
 */
@Composable
fun BounceAnimation(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (Modifier) -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        )
    )
    
    content(
        modifier
            .scale(scale)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    isPressed = true
                    onClick()
                }
            )
    )
    
    LaunchedEffect(isPressed) {
        if (isPressed) {
            kotlinx.coroutines.delay(100)
            isPressed = false
        }
    }
}

/**
 * Number counter animation
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AnimatedCounter(
    count: Int,
    modifier: Modifier = Modifier,
    style: androidx.compose.ui.text.TextStyle = androidx.compose.material3.MaterialTheme.typography.headlineMedium
) {
    var oldCount by remember { mutableStateOf(count) }
    
    SideEffect {
        oldCount = count
    }
    
    Row(modifier = modifier) {
        val countString = count.toString()
        val oldCountString = oldCount.toString()
        
        for (i in countString.indices) {
            val char = countString[i]
            val oldChar = oldCountString.getOrNull(i)
            
            if (char != oldChar) {
                AnimatedContent(
                    targetState = char,
                    transitionSpec = {
                        slideInVertically(
                            initialOffsetY = { if (count > oldCount) it else -it }
                        ) with slideOutVertically(
                            targetOffsetY = { if (count > oldCount) -it else it }
                        )
                    }
                ) { targetChar ->
                    androidx.compose.material3.Text(
                        text = targetChar.toString(),
                        style = style
                    )
                }
            } else {
                androidx.compose.material3.Text(
                    text = char.toString(),
                    style = style
                )
            }
        }
    }
}

/**
 * Staggered animation for lists
 */
@Composable
fun StaggeredAnimationColumn(
    modifier: Modifier = Modifier,
    delayPerItem: Int = 50,
    content: @Composable ColumnScope.() -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        isVisible = true
    }
    
    Column(modifier = modifier) {
        if (isVisible) {
            content()
        }
    }
}

@Composable
fun StaggeredAnimationItem(
    index: Int,
    delayPerItem: Int = 50,
    content: @Composable () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(index) {
        kotlinx.coroutines.delay(index * delayPerItem.toLong())
        isVisible = true
    }
    
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn() + slideInHorizontally(
            initialOffsetX = { -it / 2 }
        ),
        exit = fadeOut() + slideOutHorizontally(
            targetOffsetX = { it / 2 }
        )
    ) {
        content()
    }
}

/**
 * Expandable card animation
 */
@Composable
fun ExpandableCard(
    expanded: Boolean,
    onToggle: () -> Unit,
    header: @Composable () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
    modifier: Modifier = Modifier
) {
    androidx.compose.material3.Card(
        modifier = modifier,
        onClick = onToggle
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
        ) {
            header()
            
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    content()
                }
            }
        }
    }
}

/**
 * Floating action button with scale animation
 */
@Composable
fun AnimatedFloatingActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 1.1f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        finishedListener = {
            isPressed = false
        }
    )
    
    androidx.compose.material3.FloatingActionButton(
        onClick = {
            isPressed = true
            onClick()
        },
        modifier = modifier.scale(scale)
    ) {
        content()
    }
}