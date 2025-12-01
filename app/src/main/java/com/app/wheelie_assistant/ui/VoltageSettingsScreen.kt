package com.app.wheelie_assistant.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.wheelie_assistant.ui.theme.MaterialRangeSlider
import com.app.wheelie_assistant.ui.theme.MaterialSlider

@Preview
@Composable
fun VoltageSettingsScreen(
  minVoltage: Float = 1.5f,
  stepRange: ClosedFloatingPointRange<Float> = 1f..4f,
  onMinVoltageChanged: (Float) -> Unit = {},
  onStepRangeChanged: (ClosedFloatingPointRange<Float>) -> Unit = {}
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(Color(0xFF37383B))
      .verticalScroll(rememberScrollState())
      .padding(16.dp)
  ) {
    // Напряжение и шаги (скрытый заголовок)
//    Text(
//      text = "⚡ НАПРЯЖЕНИЕ И ШАГИ",
//      color = Color.White,
//      fontSize = 14.sp,
//      fontWeight = FontWeight.Bold,
//      modifier = Modifier.padding(bottom = 8.dp)
//    )

    // Карточка - аналог MaterialCardView
    Card(
      modifier = Modifier.fillMaxWidth(),
      colors = CardDefaults.cardColors(
        containerColor = Color(0xFF37383B)
      ),
      shape = MaterialTheme.shapes.medium,
      border = BorderStroke(1.dp, Color.White)
    ) {
      Column(
        modifier = Modifier.padding(16.dp)
      ) {
        // Порог включения
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Порог включения",
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
          )

          Text(
            text = "%.1f В".format(minVoltage),
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Right,
            modifier = Modifier.weight(0.5f)
          )
        }

        // Slider для минимального напряжения
        MaterialSlider(
          value = minVoltage,
          onValueChange = onMinVoltageChanged,
          valueRange = 0f..5f,
          steps = 49, // шаг 0.1: (5.0 - 0.0) / 0.1 - 1 = 49
          modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
        )

        // Диапазон шагов
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Диапазон шагов",
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
          )

          Text(
            text = if (stepRange.start != stepRange.endInclusive) {
              "${stepRange.start.toInt()} - ${stepRange.endInclusive.toInt()} кОм"
            } else {
              "${stepRange.start.toInt()} кОм"
            },
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Right,
            modifier = Modifier
                       .weight(0.5f)
          )
        }

        // RangeSlider для диапазона шагов - ТЕПЕРЬ ПРАВИЛЬНО
        MaterialRangeSlider(
          value = 1f..4f,
          onValueChange = onStepRangeChanged,
          valueRange = 1f..10f,
          steps = 9, // шаг 1.0: (10 - 1) - 1 = 8
          modifier = Modifier.fillMaxWidth()
        )
      }
    }
  }
}