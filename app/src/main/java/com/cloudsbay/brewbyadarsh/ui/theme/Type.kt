package com.cloudsbay.brewbyadarsh.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.cloudsbay.brewbyadarsh.R


// Set of Material typography styles to start with
// Provider for downloading Google Fonts via the Play Services provider
val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

// Declare the GoogleFont objects for the fonts you want to use
private val GoogleSans = GoogleFont("Google Sans")
private val Roboto = GoogleFont("Roboto")

// Create FontFamily instances that load fonts from the provider
val GoogleSansFontFamily = FontFamily(
    Font(GoogleSans, provider, FontWeight.Normal, FontStyle.Normal),
    Font(GoogleSans, provider, FontWeight.Medium, FontStyle.Normal),
    Font(GoogleSans, provider, FontWeight.Bold, FontStyle.Normal)
)

val RobotoFontFamily = FontFamily(
    Font(Roboto, provider, FontWeight.Light, FontStyle.Normal),
    Font(Roboto, provider, FontWeight.Normal, FontStyle.Normal),
    Font(Roboto, provider, FontWeight.Medium, FontStyle.Normal),
    Font(Roboto, provider, FontWeight.Bold, FontStyle.Normal)
)

val Typography = Typography(
    bodyLarge = TextStyle(
        // Use Roboto for body text
        fontFamily = RobotoFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    titleLarge = TextStyle(
        // Use Google Sans for larger titles
        fontFamily = GoogleSansFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    )
    /* Other default text styles to override
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
    */
)
