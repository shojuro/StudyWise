package com.studywise.ai.presentation.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Define base text sizes that can be scaled
object TextSizes {
    const val DISPLAY_LARGE = 57
    const val DISPLAY_MEDIUM = 45
    const val DISPLAY_SMALL = 36
    const val HEADLINE_LARGE = 32
    const val HEADLINE_MEDIUM = 28
    const val HEADLINE_SMALL = 24
    const val TITLE_LARGE = 22
    const val TITLE_MEDIUM = 16
    const val TITLE_SMALL = 14
    const val BODY_LARGE = 16
    const val BODY_MEDIUM = 14
    const val BODY_SMALL = 12
    const val LABEL_LARGE = 14
    const val LABEL_MEDIUM = 12
    const val LABEL_SMALL = 11
}

fun getScaledTypography(textScale: Float = 1.0f): Typography {
    return Typography(
        displayLarge = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = (TextSizes.DISPLAY_LARGE * textScale).sp,
            lineHeight = (64 * textScale).sp,
            letterSpacing = (-0.25).sp
        ),
        displayMedium = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = (TextSizes.DISPLAY_MEDIUM * textScale).sp,
            lineHeight = (52 * textScale).sp,
            letterSpacing = 0.sp
        ),
        displaySmall = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = (TextSizes.DISPLAY_SMALL * textScale).sp,
            lineHeight = (44 * textScale).sp,
            letterSpacing = 0.sp
        ),
        headlineLarge = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = (TextSizes.HEADLINE_LARGE * textScale).sp,
            lineHeight = (40 * textScale).sp,
            letterSpacing = 0.sp
        ),
        headlineMedium = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = (TextSizes.HEADLINE_MEDIUM * textScale).sp,
            lineHeight = (36 * textScale).sp,
            letterSpacing = 0.sp
        ),
        headlineSmall = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = (TextSizes.HEADLINE_SMALL * textScale).sp,
            lineHeight = (32 * textScale).sp,
            letterSpacing = 0.sp
        ),
        titleLarge = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Medium,
            fontSize = (TextSizes.TITLE_LARGE * textScale).sp,
            lineHeight = (28 * textScale).sp,
            letterSpacing = 0.sp
        ),
        titleMedium = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Medium,
            fontSize = (TextSizes.TITLE_MEDIUM * textScale).sp,
            lineHeight = (24 * textScale).sp,
            letterSpacing = 0.15.sp
        ),
        titleSmall = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Medium,
            fontSize = (TextSizes.TITLE_SMALL * textScale).sp,
            lineHeight = (20 * textScale).sp,
            letterSpacing = 0.1.sp
        ),
        bodyLarge = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = (TextSizes.BODY_LARGE * textScale).sp,
            lineHeight = (24 * textScale).sp,
            letterSpacing = 0.5.sp
        ),
        bodyMedium = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = (TextSizes.BODY_MEDIUM * textScale).sp,
            lineHeight = (20 * textScale).sp,
            letterSpacing = 0.25.sp
        ),
        bodySmall = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = (TextSizes.BODY_SMALL * textScale).sp,
            lineHeight = (16 * textScale).sp,
            letterSpacing = 0.4.sp
        ),
        labelLarge = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Medium,
            fontSize = (TextSizes.LABEL_LARGE * textScale).sp,
            lineHeight = (20 * textScale).sp,
            letterSpacing = 0.1.sp
        ),
        labelMedium = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Medium,
            fontSize = (TextSizes.LABEL_MEDIUM * textScale).sp,
            lineHeight = (16 * textScale).sp,
            letterSpacing = 0.5.sp
        ),
        labelSmall = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Medium,
            fontSize = (TextSizes.LABEL_SMALL * textScale).sp,
            lineHeight = (16 * textScale).sp,
            letterSpacing = 0.5.sp
        )
    )
}

// Default typography
val Typography = getScaledTypography()