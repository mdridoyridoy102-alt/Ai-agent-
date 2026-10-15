package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ExecutionLog
import com.example.ui.theme.Cyan400
import com.example.ui.theme.Emerald400
import com.example.ui.theme.Rose500
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950

@Composable
fun LogsViewerPanel(
  logs: List<ExecutionLog>,
  onClearLogs: () -> Unit,
  onRestartSandbox: () -> Unit,
  modifier: Modifier = Modifier
) {
  var filterType by remember { mutableStateOf("ALL") }
  val listState = rememberLazyListState()

  val filteredLogs = remember(logs, filterType) {
    if (filterType == "ALL") logs else logs.filter { it.type == filterType }
  }

  LaunchedEffect(filteredLogs.size) {
    if (filteredLogs.isNotEmpty()) {
      listState.animateScrollToItem(filteredLogs.size - 1)
    }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(Slate950)
  ) {
    // Header
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .background(Slate900)
        .border(1.dp, Slate800)
        .padding(horizontal = 14.dp, vertical = 8.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        Icon(Icons.Default.Terminal, contentDescription = null, tint = Cyan400, modifier = Modifier.size(16.dp))
        Text("EXECUTION LOGS & TERMINAL", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Slate300, letterSpacing = 1.sp)
      }

      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        Button(
          onClick = onRestartSandbox,
          colors = ButtonDefaults.buttonColors(containerColor = Slate800),
          shape = RoundedCornerShape(6.dp),
          modifier = Modifier.height(28.dp)
        ) {
          Icon(Icons.Default.Refresh, contentDescription = null, tint = Cyan400, modifier = Modifier.size(12.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Re-run", fontSize = 10.sp, color = Cyan400)
        }

        IconButton(
          onClick = onClearLogs,
          modifier = Modifier.size(28.dp)
        ) {
          Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Slate400, modifier = Modifier.size(14.dp))
        }
      }
    }

    // Filter Chips
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .background(Slate900.copy(alpha = 0.5f))
        .padding(horizontal = 12.dp, vertical = 6.dp),
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      listOf("ALL" to "All (${logs.size})", "STDOUT" to "Stdout", "STDERR" to "Stderr", "SYSTEM" to "System").forEach { (typeKey, label) ->
        val isSelected = filterType == typeKey
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(if (isSelected) Slate800 else Color.Transparent)
            .border(1.dp, if (isSelected) Slate700 else Color.Transparent, RoundedCornerShape(4.dp))
            .clickable { filterType = typeKey }
            .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
          Text(
            text = label,
            fontSize = 11.sp,
            color = if (isSelected) Cyan400 else Slate400,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
          )
        }
      }
    }

    // Terminal Black Screen Viewport
    Box(
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth()
        .background(Slate950)
        .padding(10.dp)
    ) {
      if (filteredLogs.isEmpty()) {
        Text(
          text = "No runtime logs recorded. Start project to see Vite/Node execution output.",
          color = Slate600,
          fontSize = 11.sp,
          fontFamily = FontFamily.Monospace,
          modifier = Modifier.padding(10.dp)
        )
      } else {
        LazyColumn(
          state = listState,
          modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF030712))
            .border(1.dp, Slate800, RoundedCornerShape(8.dp))
            .padding(10.dp),
          verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          items(filteredLogs, key = { it.id }) { log ->
            val color = when (log.type) {
              "STDERR" -> Rose500
              "SYSTEM" -> Cyan400
              else -> Emerald400
            }

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Text(
                text = "[${log.type}]",
                color = color,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
              )
              Text(
                text = log.message,
                color = if (log.type == "STDERR") Rose500 else Slate200,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                lineHeight = 16.sp
              )
            }
          }
        }
      }
    }
  }
}
