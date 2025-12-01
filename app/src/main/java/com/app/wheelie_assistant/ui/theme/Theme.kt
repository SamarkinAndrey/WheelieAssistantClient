package com.app.wheelie_assistant.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Точная копия Theme.WheelieAssistant из themes.xml
private val LightColorPalette = lightColors(
  primary = White,
  primaryVariant = White,
  onPrimary = Black,
  secondary = White,
  secondaryVariant = White,
  onSecondary = Black,
  background = Black, // android:windowBackground
  surface = Color(0xFF37383B), // Из MaterialTabStyle
  onSurface = White,
  error = Error
)

private val DarkColorPalette = darkColors(
  primary = White,
  primaryVariant = White,
  onPrimary = Black,
  secondary = White,
  secondaryVariant = White,
  onSecondary = Black,
  background = Black, // android:windowBackground
  surface = Color(0xFF37383B), // Из MaterialTabStyle
  onSurface = White,
  error = Error
)

// Точная копия Typography из styles.xml
private val AppTypography = Typography(
  h1 = TextStyle(
    fontFamily = FontFamily.SansSerif,
    fontWeight = FontWeight.Normal,
    fontSize = 96.sp
  ),
  h2 = TextStyle(
    fontFamily = FontFamily.SansSerif,
    fontWeight = FontWeight.Normal,
    fontSize = 60.sp
  ),
  h3 = TextStyle(
    fontFamily = FontFamily.SansSerif,
    fontWeight = FontWeight.Normal,
    fontSize = 48.sp
  ),
  h4 = TextStyle(
    fontFamily = FontFamily.SansSerif,
    fontWeight = FontWeight.Normal,
    fontSize = 34.sp
  ),
  h5 = TextStyle(
    fontFamily = FontFamily.SansSerif,
    fontWeight = FontWeight.Normal,
    fontSize = 24.sp
  ),
  h6 = TextStyle(
    fontFamily = FontFamily.SansSerif,
    fontWeight = FontWeight.Normal,
    fontSize = 20.sp
  ),
  subtitle1 = TextStyle(
    fontFamily = FontFamily.SansSerif,
    fontWeight = FontWeight.Normal,
    fontSize = 16.sp
  ),
  subtitle2 = TextStyle(
    fontFamily = FontFamily.SansSerif,
    fontWeight = FontWeight.Medium,
    fontSize = 14.sp
  ),
  body1 = TextStyle(
    fontFamily = FontFamily.SansSerif,
    fontWeight = FontWeight.Normal,
    fontSize = 16.sp
  ),
  body2 = TextStyle(
    fontFamily = FontFamily.SansSerif,
    fontWeight = FontWeight.Normal,
    fontSize = 14.sp
  ),
  button = TextStyle(
    fontFamily = FontFamily.SansSerif,
    fontWeight = FontWeight.Medium,
    fontSize = 14.sp,
    color = White
  ),
  caption = TextStyle(
    fontFamily = FontFamily.SansSerif,
    fontWeight = FontWeight.Normal,
    fontSize = 12.sp
  ),
  overline = TextStyle(
    fontFamily = FontFamily.SansSerif,
    fontWeight = FontWeight.Normal,
    fontSize = 10.sp
  )
)

@Composable
fun WheelieAssistantTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit
) {
  val colors = if (darkTheme) {
    DarkColorPalette
  } else {
    LightColorPalette
  }

  MaterialTheme(
    colors = colors,
    typography = AppTypography,
    shapes = Shapes(),
    content = content
  )
}