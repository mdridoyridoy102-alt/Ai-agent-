package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChatMessageEntity
import com.example.ui.theme.Cyan400
import com.example.ui.theme.Emerald400
import com.example.ui.theme.Indigo500
import com.example.ui.theme.Indigo600
import com.example.ui.theme.Rose500
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950

@Composable
fun ChatPanel(
  messages: List<ChatMessageEntity>,
  isGenerating: Boolean,
  generationStatus: String?,
  onSendPrompt: (String) -> Unit,
  onNavigateToCode: () -> Unit,
  modifier: Modifier = Modifier
) {
  var inputPrompt by remember { mutableStateOf("") }
  val listState = rememberLazyListState()

  LaunchedEffect(messages.size, isGenerating) {
    if (messages.isNotEmpty()) {
      listState.animateScrollToItem(messages.size - 1)
    }
  }

  val promptInspirations = listOf(
    "Add JWT Auth & Login modal",
    "Add Search and Priority Filter",
    "Add Dark/Light Theme Provider",
    "Add JSON/CSV Export utils",
    "Create Comments Schema & API"
  )

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(Slate950)
  ) {
    // Top Chat Header
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .background(Slate900.copy(alpha = 0.9f))
        .border(1.dp, Slate800)
        .padding(horizontal = 16.dp, vertical = 10.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Icon(
          imageVector = Icons.Default.SmartToy,
          contentDescription = null,
          tint = Cyan400,
          modifier = Modifier.size(18.dp)
        )
        Text(
          text = "AI ENGINEERING AGENT",
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 1.sp,
          color = Slate300
        )
      }

      Text(
        text = "JSON Planner • Zero Placeholder",
        fontSize = 10.sp,
        color = Slate500,
        fontFamily = FontFamily.Monospace
      )
    }

    // Inspiration Chips
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .background(Slate900.copy(alpha = 0.4f))
        .horizontalScroll(rememberScrollState())
        .padding(horizontal = 12.dp, vertical = 8.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      promptInspirations.forEach { inspiration ->
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Slate800)
            .border(1.dp, Slate700, RoundedCornerShape(20.dp))
            .clickable {
              inputPrompt = inspiration
            }
            .padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Cyan400, modifier = Modifier.size(12.dp))
            Text(inspiration, fontSize = 11.sp, color = Slate300)
          }
        }
      }
    }

    // Messages Stream
    LazyColumn(
      state = listState,
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 8.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      items(messages, key = { it.id }) { msg ->
        ChatMessageBubble(
          message = msg,
          onViewCodeClick = onNavigateToCode
        )
      }

      if (isGenerating) {
        item {
          GeneratingProgressCard(status = generationStatus ?: "Engineering plan & code synthesis in progress...")
        }
      }
    }

    // Prompt Input Area
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .background(Slate900)
        .border(1.dp, Slate800)
        .padding(12.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      OutlinedTextField(
        value = inputPrompt,
        onValueChange = { inputPrompt = it },
        placeholder = {
          Text(
            "Describe the feature, API endpoint, or component to generate...",
            fontSize = 12.sp,
            color = Slate500
          )
        },
        minLines = 2,
        maxLines = 4,
        colors = OutlinedTextFieldDefaults.colors(
          focusedTextColor = Slate100,
          unfocusedTextColor = Slate100,
          focusedBorderColor = Indigo500,
          unfocusedBorderColor = Slate700,
          focusedContainerColor = Slate950,
          unfocusedContainerColor = Slate950
        ),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("ai_prompt_input_field")
      )

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Generates complete files with unit state",
          fontSize = 10.sp,
          color = Slate500
        )

        Button(
          onClick = {
            if (inputPrompt.isNotBlank() && !isGenerating) {
              val promptToSend = inputPrompt
              inputPrompt = ""
              onSendPrompt(promptToSend)
            }
          },
          enabled = inputPrompt.isNotBlank() && !isGenerating,
          colors = ButtonDefaults.buttonColors(
            containerColor = Indigo600,
            disabledContainerColor = Slate800
          ),
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier.testTag("submit_ai_prompt_button")
        ) {
          if (isGenerating) {
            CircularProgressIndicator(
              modifier = Modifier.size(16.dp),
              color = Color.White,
              strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.size(6.dp))
            Text("Generating...", fontSize = 12.sp)
          } else {
            Icon(Icons.Default.Send, contentDescription = "Send", modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.size(6.dp))
            Text("Execute Changes", fontSize = 12.sp, fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}

@Composable
fun ChatMessageBubble(
  message: ChatMessageEntity,
  onViewCodeClick: () -> Unit
) {
  val isUser = message.role == "user"
  val isSystem = message.role == "system"

  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
  ) {
    if (!isUser) {
      Box(
        modifier = Modifier
          .size(28.dp)
          .clip(CircleShape)
          .background(if (isSystem) Slate800 else Indigo600)
          .border(1.dp, Slate700, CircleShape),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = if (isSystem) Icons.Default.CheckCircle else Icons.Default.SmartToy,
          contentDescription = null,
          tint = if (isSystem) Emerald400 else Color.White,
          modifier = Modifier.size(16.dp)
        )
      }
      Spacer(modifier = Modifier.width(8.dp))
    }

    Column(
      modifier = Modifier
        .fillMaxWidth(if (isUser) 0.85f else 0.90f)
        .clip(RoundedCornerShape(12.dp))
        .background(
          when {
            isUser -> Indigo600.copy(alpha = 0.25f)
            isSystem -> Slate900.copy(alpha = 0.6f)
            else -> Slate900
          }
        )
        .border(
          1.dp,
          when {
            isUser -> Indigo500.copy(alpha = 0.5f)
            isSystem -> Slate700
            else -> Slate800
          },
          RoundedCornerShape(12.dp)
        )
        .padding(12.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = when (message.role) {
            "user" -> "Developer Prompt"
            "system" -> "System Audit"
            else -> "Agent Solution"
          },
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          color = when (message.role) {
            "user" -> Cyan400
            "system" -> Emerald400
            else -> Indigo500
          }
        )

        if (message.taskState != null) {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(4.dp))
              .background(
                if (message.taskState == "COMPLETED") Emerald400.copy(alpha = 0.15f)
                else Rose500.copy(alpha = 0.15f)
              )
              .padding(horizontal = 6.dp, vertical = 2.dp)
          ) {
            Text(
              text = message.taskState,
              fontSize = 9.sp,
              fontWeight = FontWeight.Bold,
              color = if (message.taskState == "COMPLETED") Emerald400 else Rose500
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(6.dp))

      Text(
        text = message.content,
        color = Slate100,
        fontSize = 12.sp,
        lineHeight = 18.sp,
        fontFamily = if (message.content.contains("• [") || message.content.contains("{")) FontFamily.Monospace else FontFamily.Default
      )

      if (message.role == "assistant" && message.content.contains("Modified")) {
        Spacer(modifier = Modifier.height(8.dp))
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Indigo600.copy(alpha = 0.2f))
            .border(1.dp, Indigo500.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
            .clickable { onViewCodeClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
          Text("Inspect in Code Editor →", fontSize = 11.sp, color = Cyan400, fontWeight = FontWeight.SemiBold)
        }
      }
    }

    if (isUser) {
      Spacer(modifier = Modifier.width(8.dp))
      Box(
        modifier = Modifier
          .size(28.dp)
          .clip(CircleShape)
          .background(Cyan400.copy(alpha = 0.2f))
          .border(1.dp, Cyan400, CircleShape),
        contentAlignment = Alignment.Center
      ) {
        Icon(Icons.Default.Person, contentDescription = null, tint = Cyan400, modifier = Modifier.size(16.dp))
      }
    }
  }
}

@Composable
fun GeneratingProgressCard(status: String) {
  val transition = rememberInfiniteTransition(label = "pulse")
  val alpha by transition.animateFloat(
    initialValue = 0.4f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(800, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "pulseAlpha"
  )

  Box(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(12.dp))
      .background(Slate900)
      .border(1.dp, Indigo500.copy(alpha = alpha), RoundedCornerShape(12.dp))
      .padding(14.dp)
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      CircularProgressIndicator(
        modifier = Modifier.size(18.dp),
        color = Cyan400,
        strokeWidth = 2.5.dp
      )
      Column {
        Text(
          text = "Autonomous Agent Running",
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          color = Cyan400
        )
        Text(
          text = status,
          fontSize = 11.sp,
          color = Slate300,
          modifier = Modifier.alpha(alpha)
        )
      }
    }
  }
}
