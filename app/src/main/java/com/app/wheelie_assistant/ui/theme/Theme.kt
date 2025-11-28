package com.app.wheelie_assistant.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.*
import androidx.compose.material.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp

private val LightColors = lightColors(
  primary = White,
  primaryVariant = White,
  onPrimary = Black,
  secondary = White,
  onSecondary = Black,
  background = Background,
  surface = Surface,
  onBackground = TextPrimary,
  onSurface = TextPrimary,
  error = Error,
)

private val DarkColors = darkColors(
  primary = Primary,
  primaryVariant = PrimaryDark,
  onPrimary = TextPrimaryDark,
  secondary = Secondary,
  onSecondary = TextPrimaryDark,
  background = BackgroundDark,
  surface = DividerDark,
  onBackground = TextPrimaryDark,
  onSurface = TextPrimaryDark,
  error = Error,
)

private val AppTypography = Typography(
  h1 = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 96.sp),
  h2 = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 60.sp),
  h3 = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 48.sp),
  body1 = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 16.sp),
  button = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 14.sp)
)

@Composable
fun AppTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit
) {
  val colors = if (darkTheme) DarkColors else LightColors

  MaterialTheme(
    colors = colors,
    typography = AppTypography,
    shapes = Shapes(),
    content = content
  )
}
