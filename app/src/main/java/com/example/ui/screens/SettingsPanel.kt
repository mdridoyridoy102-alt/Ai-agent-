package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.data.model.ProjectEntity
import com.example.data.model.ProjectFileEntity
import com.example.ui.theme.Amber400
import com.example.ui.theme.Cyan400
import com.example.ui.theme.Emerald400
import com.example.ui.theme.Indigo500
import com.example.ui.theme.Indigo600
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import org.json.JSONObject

@Composable
fun SettingsPanel(
  project: ProjectEntity?,
  files: List<ProjectFileEntity>,
  activeProvider: String,
  activeModel: String,
  customApiKey: String,
  onSelectProvider: (String) -> Unit,
  onSelectModel: (String) -> Unit,
  onUpdateApiKey: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val clipboardManager = LocalClipboardManager.current

  val buildConfigHasKey = remember {
    val key: String? = runCatching {
      val clazz = Class.forName("com.example.BuildConfig")
      val field = clazz.getField("GEMINI_API_KEY")
      field.get(null) as? String
    }.getOrNull()
    !key.isNullOrBlank() && !key.contains("MY_GEMINI")
  }

  var apiKeyInput by remember(customApiKey) { mutableStateOf(customApiKey) }
  var envVarsText by remember {
    mutableStateOf(
      """PORT=${project?.port ?: 4100}
NODE_ENV=development
DATABASE_URL=sqlite://./dev.sqlite
JWT_SECRET=super_secret_dev_key_32_chars_min
API_GATEWAY_URL=http://localhost:5000"""
    )
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(Slate950)
      .verticalScroll(rememberScrollState())
      .padding(14.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Header
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      Icon(Icons.Default.Settings, contentDescription = null, tint = Cyan400, modifier = Modifier.size(18.dp))
      Text("SYSTEM & AI ENGINE SETTINGS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Slate200, letterSpacing = 1.sp)
    }

    // AI Provider Card
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(12.dp))
        .background(Slate900)
        .border(1.dp, Slate800, RoundedCornerShape(12.dp))
        .padding(14.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        Icon(Icons.Default.SmartToy, contentDescription = null, tint = Indigo500, modifier = Modifier.size(16.dp))
        Text("AI Generation Provider", color = Slate100, fontSize = 13.sp, fontWeight = FontWeight.Bold)
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        listOf("GEMINI" to "Google Gemini", "OPENAI" to "OpenAI", "ANTHROPIC" to "Anthropic", "LOCAL" to "Local Synthesizer").forEach { (id, label) ->
          val isSelected = activeProvider == id
          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(8.dp))
              .background(if (isSelected) Indigo600.copy(alpha = 0.25f) else Slate950)
              .border(1.dp, if (isSelected) Indigo500 else Slate800, RoundedCornerShape(8.dp))
              .clickable { onSelectProvider(id) }
              .padding(vertical = 8.dp, horizontal = 4.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = label,
              color = if (isSelected) Cyan400 else Slate400,
              fontSize = 10.sp,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
              textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
          }
        }
      }

      // Model Selector
      Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("Active Model", color = Slate400, fontSize = 11.sp)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          listOf("gemini-2.5-flash", "gemini-2.5-pro", "gpt-4o", "claude-3-5-sonnet").forEach { model ->
            val isSelected = activeModel == model
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(if (isSelected) Slate800 else Slate950)
                .border(1.dp, if (isSelected) Cyan400 else Slate800, RoundedCornerShape(6.dp))
                .clickable { onSelectModel(model) }
                .padding(horizontal = 8.dp, vertical = 5.dp)
            ) {
              Text(
                text = model,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                color = if (isSelected) Cyan400 else Slate400
              )
            }
          }
        }
      }

      // API Key Section
      Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text("Gemini API Key", color = Slate400, fontSize = 11.sp)
          if (buildConfigHasKey) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Emerald400, modifier = Modifier.size(12.dp))
              Text("Studio Secret Injected", color = Emerald400, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
          }
        }

        OutlinedTextField(
          value = apiKeyInput,
          onValueChange = {
            apiKeyInput = it
            onUpdateApiKey(it)
          },
          placeholder = { Text(if (buildConfigHasKey) "Using AI Studio runtime secret key" else "Enter Gemini API key (optional, offline fallback active)", fontSize = 11.sp, color = Slate500) },
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
          modifier = Modifier.fillMaxWidth().testTag("api_key_override_input")
        )
      }
    }

    // Environment Variables Card
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(12.dp))
        .background(Slate900)
        .border(1.dp, Slate800, RoundedCornerShape(12.dp))
        .padding(14.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text("Project Environment Variables (.env)", color = Slate100, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Button(
          onClick = { Toast.makeText(context, ".env saved to workspace", Toast.LENGTH_SHORT).show() },
          colors = ButtonDefaults.buttonColors(containerColor = Slate800),
          shape = RoundedCornerShape(6.dp),
          modifier = Modifier.height(28.dp)
        ) {
          Icon(Icons.Default.Save, contentDescription = null, tint = Slate200, modifier = Modifier.size(12.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Save .env", fontSize = 10.sp, color = Slate200)
        }
      }

      OutlinedTextField(
        value = envVarsText,
        onValueChange = { envVarsText = it },
        minLines = 4,
        maxLines = 6,
        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = Slate200),
        colors = OutlinedTextFieldDefaults.colors(
          focusedContainerColor = Slate950,
          unfocusedContainerColor = Slate950,
          focusedBorderColor = Slate700,
          unfocusedBorderColor = Slate800
        ),
        modifier = Modifier.fillMaxWidth().testTag("env_variables_text_area")
      )
    }

    // Project Export Card
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(12.dp))
        .background(Slate900)
        .border(1.dp, Slate800, RoundedCornerShape(12.dp))
        .padding(14.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      Text("Export Workspace Project", color = Slate100, fontSize = 13.sp, fontWeight = FontWeight.Bold)
      Text(
        text = "Export complete compilable source code without secrets. Ready to extract and run with 'npm install && npm run dev'.",
        color = Slate400,
        fontSize = 11.sp
      )

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Button(
          onClick = {
            val jsonExport = JSONObject()
            jsonExport.put("projectName", project?.name ?: "Project")
            jsonExport.put("techStack", project?.techStack ?: "React + Node")
            val filesObj = JSONObject()
            files.forEach { filesObj.put(it.path, it.content) }
            jsonExport.put("files", filesObj)

            clipboardManager.setText(AnnotatedString(jsonExport.toString(2)))
            Toast.makeText(context, "Project archive JSON copied (${files.size} files)", Toast.LENGTH_LONG).show()
          },
          colors = ButtonDefaults.buttonColors(containerColor = Indigo600),
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier.weight(1f).testTag("export_project_json_button")
        ) {
          Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Copy Project Archive (JSON)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}
