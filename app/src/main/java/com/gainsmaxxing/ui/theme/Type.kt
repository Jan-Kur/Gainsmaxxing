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

val AppTypography = Typography(
    displayLarge = TextStyle(fontFamily = GeistMonoFontFamily, fontWeight = FontWeight.Bold, fontSize = 56.sp, letterSpacing = (-0.02).sp),
    displayMedium = TextStyle(fontFamily = GeistMonoFontFamily, fontWeight = FontWeight.Bold, fontSize = 40.sp, letterSpacing = (-0.02).sp),
    displaySmall = TextStyle(fontFamily = GeistMonoFontFamily, fontWeight = FontWeight.Bold, fontSize = 36.sp, letterSpacing = (-0.03).sp),
    headlineLarge = TextStyle(fontFamily = GeistMonoFontFamily, fontWeight = FontWeight.Bold, fontSize = 32.sp, letterSpacing = (-0.02).sp),
    headlineMedium = TextStyle(fontFamily = GeistMonoFontFamily, fontWeight = FontWeight.Bold, fontSize = 28.sp, letterSpacing = (-0.02).sp),
    headlineSmall = TextStyle(fontFamily = GeistMonoFontFamily, fontWeight = FontWeight.Bold, fontSize = 22.sp),
    titleLarge = TextStyle(fontFamily = GeistMonoFontFamily, fontWeight = FontWeight.Bold, fontSize = 20.sp, letterSpacing = (-0.01).sp),
    titleMedium = TextStyle(fontFamily = GeistMonoFontFamily, fontWeight = FontWeight.Bold, fontSize = 18.sp, letterSpacing = (-0.01).sp),
    titleSmall = TextStyle(fontFamily = GeistFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 15.sp),
    bodyLarge = TextStyle(fontFamily = GeistFontFamily, fontWeight = FontWeight.Normal, fontSize = 16.sp),
    bodyMedium = TextStyle(fontFamily = GeistFontFamily, fontWeight = FontWeight.Normal, fontSize = 14.sp),
    bodySmall = TextStyle(fontFamily = GeistFontFamily, fontWeight = FontWeight.Normal, fontSize = 13.sp),
    labelLarge = TextStyle(fontFamily = GeistFontFamily, fontWeight = FontWeight.Medium, fontSize = 12.sp),
    labelMedium = TextStyle(fontFamily = GeistFontFamily, fontWeight = FontWeight.Medium, fontSize = 11.sp),
    labelSmall = TextStyle(fontFamily = GeistFontFamily, fontWeight = FontWeight.Medium, fontSize = 10.sp),
)
