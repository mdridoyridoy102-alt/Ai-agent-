package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.TopNavigation
import com.example.ui.screens.ApiWorkbenchPanel
import com.example.ui.screens.ChatPanel
import com.example.ui.screens.DatabaseWorkbenchPanel
import com.example.ui.screens.EditorPanel
import com.example.ui.screens.LivePreviewPanel
import com.example.ui.screens.LogsViewerPanel
import com.example.ui.screens.SettingsPanel
import com.example.ui.screens.SnapshotsPanel
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.Slate950
import com.example.ui.viewmodel.ProjectViewModel
import com.example.ui.viewmodel.WorkspaceTab

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme(darkTheme = true) {
        CodingToAiConvertApp()
      }
    }
  }
}

@Composable
fun CodingToAiConvertApp(
  viewModel: ProjectViewModel = viewModel()
) {
  val activeTab by viewModel.activeTab.collectAsState()
  val allProjects by viewModel.allProjects.collectAsState()
  val activeProject by viewModel.activeProject.collectAsState()
  val projectFiles by viewModel.projectFiles.collectAsState()
  val selectedFile by viewModel.selectedFile.collectAsState()
  val chatMessages by viewModel.chatMessages.collectAsState()
  val checkpoints by viewModel.checkpoints.collectAsState()
  val endpoints by viewModel.endpoints.collectAsState()
  val executionLogs by viewModel.executionLogs.collectAsState()
  val isGenerating by viewModel.isGenerating.collectAsState()
  val generationStatus by viewModel.generationStatus.collectAsState()

  val aiProvider by viewModel.aiProvider.collectAsState()
  val aiModel by viewModel.aiModel.collectAsState()
  val customApiKey by viewModel.customApiKey.collectAsState()

  val lastApiResponse by viewModel.lastApiResponse.collectAsState()
  val isApiExecuting by viewModel.isApiExecuting.collectAsState()

  val lastQueryResult by viewModel.lastQueryResult.collectAsState()
  val isDbExecuting by viewModel.isDbExecuting.collectAsState()

  // Handle Android back button: navigate back to Editor tab if on another screen
  BackHandler(enabled = activeTab != WorkspaceTab.EDITOR) {
    viewModel.setTab(WorkspaceTab.EDITOR)
  }

  Scaffold(
    contentWindowInsets = WindowInsets.safeDrawing,
    modifier = Modifier
      .fillMaxSize()
      .background(Slate950)
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .background(Slate950)
        .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
      // Top Navigation with brand, project picker, run/stop button, and tab pills
      TopNavigation(
        activeProject = activeProject,
        allProjects = allProjects,
        activeTab = activeTab,
        onSelectTab = { viewModel.setTab(it) },
        onSelectProject = { viewModel.selectProject(it) },
        onToggleRun = { viewModel.toggleRunProject() },
        onCreateNewProject = { name, desc, stack ->
          viewModel.createNewProject(name, desc, stack)
        }
      )

      // Active Workspace Screen
      Box(modifier = Modifier.weight(1f)) {
        when (activeTab) {
          WorkspaceTab.CHAT -> {
            ChatPanel(
              messages = chatMessages,
              isGenerating = isGenerating,
              generationStatus = generationStatus,
              onSendPrompt = { viewModel.sendPrompt(it) },
              onNavigateToCode = { viewModel.setTab(WorkspaceTab.EDITOR) }
            )
          }

          WorkspaceTab.EDITOR -> {
            EditorPanel(
              files = projectFiles,
              selectedFile = selectedFile,
              onSelectFile = { viewModel.selectFile(it) },
              onSaveFile = { path, content -> viewModel.saveFile(path, content) },
              onDeleteFile = { viewModel.deleteFile(it) }
            )
          }

          WorkspaceTab.PREVIEW -> {
            LivePreviewPanel(
              project = activeProject,
              files = projectFiles,
              onStartProject = { viewModel.toggleRunProject() }
            )
          }

          WorkspaceTab.APIS -> {
            ApiWorkbenchPanel(
              endpoints = endpoints,
              lastResponse = lastApiResponse,
              isExecuting = isApiExecuting,
              onExecuteRequest = { method, path, headers, body ->
                viewModel.executeApiRequest(method, path, headers, body)
              },
              onSaveEndpoint = { name, method, path, headers, body ->
                viewModel.saveEndpoint(name, method, path, headers, body)
              }
            )
          }

          WorkspaceTab.DATABASE -> {
            DatabaseWorkbenchPanel(
              tables = viewModel.getDatabaseTables(),
              lastQueryResult = lastQueryResult,
              isExecuting = isDbExecuting,
              onExecuteSql = { viewModel.executeSql(it) },
              onResetDatabase = { viewModel.resetDatabase() }
            )
          }

          WorkspaceTab.LOGS -> {
            LogsViewerPanel(
              logs = executionLogs,
              onClearLogs = { viewModel.clearLogs() },
              onRestartSandbox = { viewModel.toggleRunProject() }
            )
          }

          WorkspaceTab.SNAPSHOTS -> {
            SnapshotsPanel(
              checkpoints = checkpoints,
              onCreateCheckpoint = { viewModel.createCheckpoint(it) },
              onRollback = { viewModel.rollbackToCheckpoint(it) }
            )
          }

          WorkspaceTab.SETTINGS -> {
            SettingsPanel(
              project = activeProject,
              files = projectFiles,
              activeProvider = aiProvider,
              activeModel = aiModel,
              customApiKey = customApiKey,
              onSelectProvider = { viewModel.setAiProvider(it) },
              onSelectModel = { viewModel.setAiModel(it) },
              onUpdateApiKey = { viewModel.setCustomApiKey(it) }
            )
          }
        }
      }
    }
  }
}
