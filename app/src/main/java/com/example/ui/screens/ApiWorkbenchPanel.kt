package com.example.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.window.Dialog
import com.example.data.model.ApiEndpointEntity
import com.example.data.model.ApiResponse
import com.example.ui.theme.Amber400
import com.example.ui.theme.Cyan400
import com.example.ui.theme.Emerald400
import com.example.ui.theme.Indigo500
import com.example.ui.theme.Indigo600
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
fun ApiWorkbenchPanel(
  endpoints: List<ApiEndpointEntity>,
  lastResponse: ApiResponse?,
  isExecuting: Boolean,
  onExecuteRequest: (method: String, path: String, headers: Map<String, String>, body: String) -> Unit,
  onSaveEndpoint: (name: String, method: String, path: String, headers: String, body: String) -> Unit,
  modifier: Modifier = Modifier
) {
  var selectedMethod by remember { mutableStateOf("GET") }
  var pathUrl by remember { mutableStateOf("/api/tasks") }
  var bodyContent by remember {
    mutableStateOf("{\n  \"title\": \"Implement JWT session guard\",\n  \"status\": \"TODO\"\n}")
  }
  var showMethodDropdown by remember { mutableStateOf(false) }
  var activeReqTab by remember { mutableStateOf("body") } // "body", "headers", "saved"
  var showSaveDialog by remember { mutableStateOf(false) }

  val methodColors = mapOf(
    "GET" to Emerald400,
    "POST" to Indigo500,
    "PUT" to Amber400,
    "DELETE" to Rose500,
    "PATCH" to Cyan400
  )

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
      Text("API WORKBENCH", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Slate300, letterSpacing = 1.sp)
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        listOf("/api/health", "/api/tasks", "/api/users").forEach { quickPath ->
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(4.dp))
              .background(Slate800)
              .clickable {
                pathUrl = quickPath
                selectedMethod = "GET"
              }
              .padding(horizontal = 6.dp, vertical = 3.dp)
          ) {
            Text(quickPath, fontSize = 10.sp, color = Cyan400, fontFamily = FontFamily.Monospace)
          }
        }
      }
    }

    // Request Bar: Method selector + Path + Send Button
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .background(Slate900.copy(alpha = 0.6f))
        .padding(10.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      // Method Dropdown
      Box {
        Row(
          modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(methodColors[selectedMethod]?.copy(alpha = 0.15f) ?: Slate800)
            .border(1.dp, methodColors[selectedMethod] ?: Slate700, RoundedCornerShape(6.dp))
            .clickable { showMethodDropdown = true }
            .padding(horizontal = 10.dp, vertical = 8.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          Text(
            text = selectedMethod,
            color = methodColors[selectedMethod] ?: Slate100,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
          )
          Icon(Icons.Default.ExpandMore, contentDescription = null, tint = Slate400, modifier = Modifier.size(14.dp))
        }

        DropdownMenu(
          expanded = showMethodDropdown,
          onDismissRequest = { showMethodDropdown = false },
          modifier = Modifier.background(Slate900)
        ) {
          listOf("GET", "POST", "PUT", "DELETE", "PATCH").forEach { m ->
            DropdownMenuItem(
              text = {
                Text(m, color = methodColors[m] ?: Slate100, fontWeight = FontWeight.Bold)
              },
              onClick = {
                selectedMethod = m
                showMethodDropdown = false
              }
            )
          }
        }
      }

      // Path / URL input
      OutlinedTextField(
        value = pathUrl,
        onValueChange = { pathUrl = it },
        placeholder = { Text("e.g. /api/tasks or https://api.example.com", fontSize = 11.sp, color = Slate500) },
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
          focusedTextColor = Slate100,
          unfocusedTextColor = Slate100,
          focusedBorderColor = Indigo500,
          unfocusedBorderColor = Slate700,
          focusedContainerColor = Slate950,
          unfocusedContainerColor = Slate950
        ),
        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, fontFamily = FontFamily.Monospace),
        modifier = Modifier
          .weight(1f)
          .testTag("api_endpoint_path_input")
      )

      // Send Button
      Button(
        onClick = {
          onExecuteRequest(selectedMethod, pathUrl, mapOf("Content-Type" to "application/json"), bodyContent)
        },
        enabled = !isExecuting,
        colors = ButtonDefaults.buttonColors(containerColor = Indigo600),
        shape = RoundedCornerShape(6.dp),
        modifier = Modifier.testTag("api_send_request_button")
      ) {
        if (isExecuting) {
          CircularProgressIndicator(modifier = Modifier.size(14.dp), color = Color.White, strokeWidth = 2.dp)
        } else {
          Icon(Icons.Default.Send, contentDescription = "Send", modifier = Modifier.size(14.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Send", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
      }
    }

    // Request Tabs: Body, Headers, Endpoints
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .background(Slate900)
        .padding(horizontal = 12.dp, vertical = 4.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      listOf("body" to "Request Body", "headers" to "Headers", "saved" to "Saved Endpoints (${endpoints.size})").forEach { (tabId, label) ->
        val active = activeReqTab == tabId
        Text(
          text = label,
          fontSize = 11.sp,
          fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
          color = if (active) Cyan400 else Slate400,
          modifier = Modifier
            .clickable { activeReqTab = tabId }
            .padding(vertical = 4.dp, horizontal = 6.dp)
        )
      }
    }

    // Request Payload Section
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(110.dp)
        .background(Slate950)
        .border(1.dp, Slate800)
        .padding(8.dp)
    ) {
      when (activeReqTab) {
        "body" -> {
          OutlinedTextField(
            value = bodyContent,
            onValueChange = { bodyContent = it },
            modifier = Modifier.fillMaxSize(),
            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = Slate200),
            colors = OutlinedTextFieldDefaults.colors(
              focusedContainerColor = Slate900,
              unfocusedContainerColor = Slate900,
              focusedBorderColor = Slate700,
              unfocusedBorderColor = Slate800
            )
          )
        }
        "headers" -> {
          Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            Text("Content-Type: application/json", color = Slate300, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
            Text("Accept: application/json", color = Slate300, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
            Text("X-Client: Coding-to-AI-Convert", color = Slate500, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
          }
        }
        "saved" -> {
          LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(endpoints) { ep ->
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(4.dp))
                  .clickable {
                    selectedMethod = ep.method
                    pathUrl = ep.path
                    if (ep.bodySchemaJson.isNotBlank() && ep.bodySchemaJson != "{}") {
                      bodyContent = ep.bodySchemaJson
                    }
                  }
                  .padding(vertical = 4.dp, horizontal = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Text("${ep.method} ${ep.path}", color = Cyan400, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                Text(ep.name, color = Slate400, fontSize = 11.sp)
              }
            }
          }
        }
      }
    }

    // Response Section
    Column(
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth()
        .background(Slate950)
        .padding(10.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text("RESPONSE VIEWER", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Slate400, letterSpacing = 1.sp)

        if (lastResponse != null) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            // Status Code Badge
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(
                  if (lastResponse.statusCode < 300) Emerald400.copy(alpha = 0.2f)
                  else if (lastResponse.statusCode < 500) Amber400.copy(alpha = 0.2f)
                  else Rose500.copy(alpha = 0.2f)
                )
                .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
              Text(
                "${lastResponse.statusCode} ${lastResponse.statusMessage}",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (lastResponse.statusCode < 300) Emerald400 else if (lastResponse.statusCode < 500) Amber400 else Rose500
              )
            }

            // Latency Badge
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(Slate800)
                .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
              Text("${lastResponse.durationMs} ms", fontSize = 10.sp, color = Cyan400, fontFamily = FontFamily.Monospace)
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(6.dp))

      // Response Body Monospace Viewer
      Box(
        modifier = Modifier
          .weight(1f)
          .fillMaxWidth()
          .clip(RoundedCornerShape(8.dp))
          .background(Slate900)
          .border(1.dp, Slate800, RoundedCornerShape(8.dp))
          .padding(10.dp)
      ) {
        if (lastResponse == null) {
          Text("No response yet. Select an endpoint and click Send.", color = Slate500, fontSize = 11.sp)
        } else {
          Text(
            text = lastResponse.body,
            color = if (lastResponse.isError) Rose500 else Slate100,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            lineHeight = 17.sp,
            modifier = Modifier
              .fillMaxSize()
              .verticalScroll(rememberScrollState())
              .horizontalScroll(rememberScrollState())
          )
        }
      }
    }
  }
}
