package com.example.ui.screens

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DesktopMac
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.PhoneIphone
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tablet
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.model.ProjectEntity
import com.example.data.model.ProjectFileEntity
import com.example.data.model.ProjectStatus
import com.example.data.service.ExecutionSandbox
import com.example.ui.theme.Cyan400
import com.example.ui.theme.Emerald400
import com.example.ui.theme.Indigo500
import com.example.ui.theme.Indigo600
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950

enum class ViewportMode(val label: String, val widthDp: Int?) {
  DESKTOP("100%", null),
  TABLET("Tablet", 600),
  MOBILE("Mobile", 380)
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun LivePreviewPanel(
  project: ProjectEntity?,
  files: List<ProjectFileEntity>,
  onStartProject: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val isRunning = project?.status == ProjectStatus.RUNNING.name
  val port = project?.port ?: 4100
  val sandbox = remember { ExecutionSandbox() }

  var viewportMode by remember { mutableStateOf(ViewportMode.DESKTOP) }
  var webViewRef by remember { mutableStateOf<WebView?>(null) }
  var reloadTrigger by remember { mutableStateOf(0) }

  val previewHtml = remember(files, port, reloadTrigger) {
    sandbox.generateInteractiveWebBundle(files, port)
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(Slate950)
  ) {
    // Address & Control Bar
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .background(Slate900)
        .border(1.dp, Slate800)
        .padding(horizontal = 12.dp, vertical = 8.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      // URL Bar
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
          .weight(1f)
          .clip(RoundedCornerShape(6.dp))
          .background(Slate950)
          .border(1.dp, Slate800, RoundedCornerShape(6.dp))
          .padding(horizontal = 10.dp, vertical = 6.dp)
      ) {
        Box(
          modifier = Modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(if (isRunning) Emerald400 else Slate600)
        )
        Text(
          text = if (isRunning) "http://localhost:$port" else "Sandbox stopped",
          color = if (isRunning) Cyan400 else Slate500,
          fontSize = 12.sp,
          fontFamily = FontFamily.Monospace,
          fontWeight = FontWeight.Medium
        )
      }

      Spacer(modifier = Modifier.width(8.dp))

      // Viewport Switchers: Mobile, Tablet, Desktop
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        IconButton(
          onClick = {
            viewportMode = when (viewportMode) {
              ViewportMode.DESKTOP -> ViewportMode.TABLET
              ViewportMode.TABLET -> ViewportMode.MOBILE
              ViewportMode.MOBILE -> ViewportMode.DESKTOP
            }
          },
          modifier = Modifier.size(32.dp)
        ) {
          Icon(
            imageVector = when (viewportMode) {
              ViewportMode.DESKTOP -> Icons.Default.DesktopMac
              ViewportMode.TABLET -> Icons.Default.Tablet
              ViewportMode.MOBILE -> Icons.Default.PhoneIphone
            },
            contentDescription = "Viewport Mode",
            tint = Slate400,
            modifier = Modifier.size(16.dp)
          )
        }

        IconButton(
          onClick = {
            reloadTrigger++
            webViewRef?.loadDataWithBaseURL("http://localhost:$port", previewHtml, "text/html", "UTF-8", null)
          },
          enabled = isRunning,
          modifier = Modifier.size(32.dp)
        ) {
          Icon(Icons.Default.Refresh, contentDescription = "Reload", tint = if (isRunning) Slate300 else Slate600, modifier = Modifier.size(16.dp))
        }

        IconButton(
          onClick = {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://ais-dev-qwctsfcfaf6bsjovtpzpin-242406910000.asia-southeast1.run.app"))
            context.startActivity(browserIntent)
          },
          modifier = Modifier.size(32.dp)
        ) {
          Icon(Icons.Default.OpenInBrowser, contentDescription = "Open in browser", tint = Indigo500, modifier = Modifier.size(16.dp))
        }
      }
    }

    // Viewport Container
    if (!isRunning) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(24.dp),
        contentAlignment = Alignment.Center
      ) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
          Box(
            modifier = Modifier
              .size(56.dp)
              .clip(CircleShape)
              .background(Slate900)
              .border(1.dp, Slate800, CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Emerald400, modifier = Modifier.size(28.dp))
          }

          Text(
            text = "Application Sandbox is Stopped",
            color = Slate100,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
          )

          Text(
            text = "Click 'Run Project' to launch Vite dev server and load the interactive sandbox.",
            color = Slate400,
            fontSize = 12.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
          )

          Button(
            onClick = onStartProject,
            colors = ButtonDefaults.buttonColors(containerColor = Emerald400),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.testTag("preview_start_sandbox_button")
          ) {
            Text("Launch Sandbox Runner", color = Slate950, fontWeight = FontWeight.Bold, fontSize = 13.sp)
          }
        }
      }
    } else {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(Slate950),
        contentAlignment = Alignment.TopCenter
      ) {
        val widthModifier = if (viewportMode.widthDp != null) {
          Modifier
            .width(viewportMode.widthDp!!.dp)
            .border(1.dp, Slate800)
        } else {
          Modifier.fillMaxWidth()
        }

        AndroidView(
          factory = { ctx ->
            WebView(ctx).apply {
              settings.javaScriptEnabled = true
              settings.domStorageEnabled = true
              settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
              webViewClient = WebViewClient()
              loadDataWithBaseURL("http://localhost:$port", previewHtml, "text/html", "UTF-8", null)
              webViewRef = this
            }
          },
          update = { webView ->
            webView.loadDataWithBaseURL("http://localhost:$port", previewHtml, "text/html", "UTF-8", null)
          },
          modifier = widthModifier
            .fillMaxSize()
            .testTag("live_preview_webview")
        )
      }
    }
  }
}
