package com.gainsmaxxing.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.gainsmaxxing.R

private val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs,
)

val GeistFontFamily = FontFamily(
    Font(googleFont = GoogleFont("Geist"), fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = GoogleFont("Geist"), fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = GoogleFont("Geist"), fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = GoogleFont("Geist"), fontProvider = provider, weight = FontWeight.Bold),
)

val GeistMonoFontFamily = FontFamily(
    Font(googleFont = GoogleFont("Geist Mono"), fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = GoogleFont("Geist Mono"), fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = GoogleFont("Geist Mono"), fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = GoogleFont("Geist Mono"), fontProvider = provider, weight = FontWeight.Bold),
)

/**
 * App-wide typography. Geist for copy, Geist Mono for numbers.
 *
 * Weight roles: Medium = de-emphasised, SemiBold = default UI/body, Bold = headers & big numbers.
 */
val AppTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = GeistMonoFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 56.sp,
        letterSpacing = (-0.02).sp,
    ),
    displayMedium = TextStyle(
        fontFamily = GeistMonoFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 40.sp,
        letterSpacing = (-0.02).sp,
    ),
    displaySmall = TextStyle(
        fontFamily = GeistMonoFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        letterSpacing = (-1.08).sp,
        lineHeight = 36.sp,
    ),
    headlineLarge = TextStyle(
        fontFamily = GeistMonoFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        letterSpacing = (-0.02).sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = GeistMonoFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        letterSpacing = (-0.56).sp,
        lineHeight = 30.sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = GeistMonoFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        letterSpacing = (-0.48).sp,
    ),
    titleLarge = TextStyle(
        fontFamily = GeistMonoFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = GeistMonoFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 15.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = GeistFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        letterSpacing = 0.13.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = GeistFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = GeistFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = GeistFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        lineHeight = 13.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = GeistFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        lineHeight = 13.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = GeistFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        letterSpacing = 1.15.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = GeistFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 9.sp,
    ),
)

/** Uppercase card sub-labels (e.g. exercise name on a PR card). */
val Typography.labelLargeCaps: TextStyle
    get() = captionEmphasis.copy(letterSpacing = 1.0.sp)

/** Secondary supporting text — tooltips, legends. */
val Typography.caption: TextStyle
    get() = TextStyle(
        fontFamily = GeistFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
    )

/** Emphasised caption — energy tags, compact labels. */
val Typography.captionEmphasis: TextStyle
    get() = caption.copy(fontWeight = FontWeight.SemiBold)

/** De-emphasised mono numbers (day-of-month, etc.). */
val Typography.monoSmall: TextStyle
    get() = TextStyle(
        fontFamily = GeistMonoFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 13.sp,
    )

/** Mono unit suffixes and secondary numeric labels. */
val Typography.monoLabel: TextStyle
    get() = TextStyle(
        fontFamily = GeistMonoFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
    )

/** Mono unit next to a large display value (e.g. "kg"). */
val Typography.monoTitle: TextStyle
    get() = TextStyle(
        fontFamily = GeistMonoFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
    )

/** Emphasised mono body text — tooltip values. */
val Typography.monoBodyEmphasis: TextStyle
    get() = bodyMedium.copy(
        fontFamily = GeistMonoFontFamily,
        fontWeight = FontWeight.Bold,
    )

/** Screen / sheet titles. */
val Typography.screenTitle: TextStyle
    get() = TextStyle(
        fontFamily = GeistMonoFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        letterSpacing = (-0.18).sp,
    )

/** SemiBold mono at body-small size — compact values (e.g. unit toggle). */
val Typography.monoBodySmall: TextStyle
    get() = bodySmall.copy(fontFamily = GeistMonoFontFamily)
