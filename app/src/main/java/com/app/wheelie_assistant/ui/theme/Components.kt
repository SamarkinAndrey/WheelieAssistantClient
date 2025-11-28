package com.app.wheelie_assistant.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/* MaterialSwitch: имитация ваших атрибутов:
   - scaleX/Y = 0.8
   - paddingStart/End = 8dp
   - thumb color / track color from resources
*/
@Composable
fun MaterialSwitch(
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true
) {
  Box(modifier = modifier.padding(start = 8.dp, end = 8.dp), contentAlignment = Alignment.Center) {
    Switch(
      checked = checked,
      onCheckedChange = onCheckedChange,
      enabled = enabled,
      modifier = Modifier.scale(0.8f),
      colors = SwitchDefaults.colors(
        checkedThumbColor = ThumbActive,
        uncheckedThumbColor = ThumbInactive,
        checkedTrackColor = TrackActive,
        uncheckedTrackColor = TrackInactive,
        checkedTrackAlpha = 1f,
        uncheckedTrackAlpha = 1f
      )
    )
  }
}

/* MaterialSlider: имитация ваших свойств:
   - stepSize = 0.1 -> steps = (100/0.1) - 1 = 999 -> слишком много; в Compose steps Int, обычно используем steps = 999.
   - valueFrom = 0f, valueTo = 100f
   - thumbColor = red
   - haloRadius = 0 (no halo) -> in Compose we can't remove focus halo entirely, but can set interactionSource or use no elevation.
   - tickVisible = false (Compose slider doesn't show ticks by default)
*/
@Composable
fun MaterialSlider(
  value: Float,
  onValueChange: (Float) -> Unit,
  modifier: Modifier = Modifier,
  valueRange: ClosedFloatingPointRange<Float> = 0f..100f,
  steps: Int = 999 // corresponds to stepSize 0.1 between 0..100
) {
  Slider(
    value = value,
    onValueChange = onValueChange,
    valueRange = valueRange,
    steps = steps,
    modifier = modifier,
    colors = SliderDefaults.colors(
      thumbColor = Red,
      activeTrackColor = TrackActive,
      inactiveTrackColor = TrackInactive,
      activeTickColor = Color.Transparent,
      inactiveTickColor = Color.Transparent,
      disabledActiveTickColor = Color.Transparent,
      disabledInactiveTickColor = Color.Transparent
    )
  )
}

/* MaterialTabs: imitate MaterialTabStyle
   - background = #37383B
   - tab width min/max = 150dp
   - tab text size 12sp, fontFamily sans-serif
   - tabRippleColor = transparent (disable ripple)
   - tabIndicator = red
*/
@Composable
fun MaterialTabs(
  titles: List<String>,
  selectedIndex: Int,
  onTabSelected: (index: Int) -> Unit,
  modifier: Modifier = Modifier
) {
  val backgroundColor = Color(0xFF37383B)
  TabRow(
    selectedTabIndex = selectedIndex,
    modifier = modifier
      .fillMaxWidth()
      .background(backgroundColor),
    backgroundColor = backgroundColor,
    contentColor = White,
    indicator = { tabPositions ->
      val current = tabPositions[selectedIndex]
      val indicatorWidth = current.width
      val indicatorOffset = current.left
      Box(
        Modifier
          .wrapContentSize(Alignment.BottomStart)
          .offset(x = indicatorOffset)
          .width(indicatorWidth)
          .height(3.dp)
          .background(Red)
      )
    },
    divider = {}
  ) {
    titles.forEachIndexed { index, title ->
      Tab(
        selected = index == selectedIndex,
        onClick = { onTabSelected(index) },
        modifier = Modifier
          .width(150.dp),
        enabled = true,
        text = {
          Text(
            text = title,
            fontSize = 12.sp,
            fontFamily = FontFamily.SansSerif,
            color = if (index == selectedIndex) White else Color(0xFF98999A)
          )
        },
        // disable ripple by using no indication
        interactionSource = remember { MutableInteractionSource() },
        selectedContentColor = White,
        unselectedContentColor = Color(0xFF98999A)
      )
    }
  }
}

@Composable
fun MaterialSliderExact(
  value: Float,
  onValueChange: (Float) -> Unit,
  modifier: Modifier = Modifier,
  valueRange: ClosedFloatingPointRange<Float>,
  steps: Int
) {
  Slider(
    value = value,
    onValueChange = onValueChange,
    valueRange = valueRange,
    steps = steps,

    modifier = modifier,

    colors = SliderDefaults.colors(
      thumbColor = ThumbActive,                // #20303C
      disabledThumbColor = ThumbActive,
      activeTrackColor = TrackActive,          // #E02828
      inactiveTrackColor = TrackInactive,      // #6E7881
      disabledActiveTrackColor = TrackInactive,
      disabledInactiveTrackColor = TrackInactive//,
//      haloColor = Color.Transparent            // 1:1 haloRadius=0dp
    )
  )
}