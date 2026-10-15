package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.ApiEndpointEntity
import com.example.data.model.ApiResponse
import com.example.data.model.ChatMessageEntity
import com.example.data.model.CheckpointEntity
import com.example.data.model.DatabaseTable
import com.example.data.model.ExecutionLog
import com.example.data.model.ProjectEntity
import com.example.data.model.ProjectFileEntity
import com.example.data.model.ProjectStatus
import com.example.data.repository.ProjectRepository
import com.example.data.service.QueryResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class WorkspaceTab(val label: String) {
  CHAT("Agent"),
  EDITOR("Code"),
  PREVIEW("Preview"),
  APIS("APIs"),
  DATABASE("Database"),
  LOGS("Logs"),
  SNAPSHOTS("History"),
  SETTINGS("Config")
}

class ProjectViewModel(application: Application) : AndroidViewModel(application) {
  private val db = AppDatabase.getDatabase(application)
  val repository = ProjectRepository(db)

  private val _activeTab = MutableStateFlow(WorkspaceTab.EDITOR)
  val activeTab: StateFlow<WorkspaceTab> = _activeTab.asStateFlow()

  val allProjects: StateFlow<List<ProjectEntity>> = repository.getAllProjects()
    .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

  val activeProjectId: StateFlow<String?> = repository.activeProjectId

  val activeProject: StateFlow<ProjectEntity?> = activeProjectId.flatMapLatest { id ->
    if (id == null) flowOf(null)
    else repository.getAllProjects().flatMapLatest { list ->
      flowOf(list.find { it.id == id })
    }
  }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

  val projectFiles: StateFlow<List<ProjectFileEntity>> = activeProjectId.flatMapLatest { id ->
    if (id == null) flowOf(emptyList())
    else repository.getFilesForProject(id)
  }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

  private val _selectedFilePath = MutableStateFlow<String?>("src/App.tsx")
  val selectedFilePath: StateFlow<String?> = _selectedFilePath.asStateFlow()

  val selectedFile: StateFlow<ProjectFileEntity?> = combine(projectFiles, selectedFilePath) { files, path ->
    files.find { it.path == path } ?: files.firstOrNull()
  }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

  val chatMessages: StateFlow<List<ChatMessageEntity>> = activeProjectId.flatMapLatest { id ->
    if (id == null) flowOf(emptyList())
    else repository.getMessagesForProject(id)
  }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

  val checkpoints: StateFlow<List<CheckpointEntity>> = activeProjectId.flatMapLatest { id ->
    if (id == null) flowOf(emptyList())
    else repository.getCheckpointsForProject(id)
  }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

  val endpoints: StateFlow<List<ApiEndpointEntity>> = activeProjectId.flatMapLatest { id ->
    if (id == null) flowOf(emptyList())
    else repository.getEndpointsForProject(id)
  }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

  val executionLogs: StateFlow<List<ExecutionLog>> = repository.executionLogs
  val isGenerating: StateFlow<Boolean> = repository.isGenerating
  val generationStatus: StateFlow<String?> = repository.generationStatus

  // AI Provider & Settings
  private val _aiProvider = MutableStateFlow("GEMINI")
  val aiProvider: StateFlow<String> = _aiProvider.asStateFlow()

  private val _aiModel = MutableStateFlow("gemini-2.5-flash")
  val aiModel: StateFlow<String> = _aiModel.asStateFlow()

  private val _customApiKey = MutableStateFlow("")
  val customApiKey: StateFlow<String> = _customApiKey.asStateFlow()

  // API Workbench State
  private val _lastApiResponse = MutableStateFlow<ApiResponse?>(null)
  val lastApiResponse: StateFlow<ApiResponse?> = _lastApiResponse.asStateFlow()

  private val _isApiExecuting = MutableStateFlow(false)
  val isApiExecuting: StateFlow<Boolean> = _isApiExecuting.asStateFlow()

  // DB Workbench State
  private val _lastQueryResult = MutableStateFlow<QueryResult?>(null)
  val lastQueryResult: StateFlow<QueryResult?> = _lastQueryResult.asStateFlow()

  private val _isDbExecuting = MutableStateFlow(false)
  val isDbExecuting: StateFlow<Boolean> = _isDbExecuting.asStateFlow()

  init {
    viewModelScope.launch {
      // Execute initial query to populate DB results
      val initialRes = repository.executeSql("SELECT * FROM tasks;")
      _lastQueryResult.value = initialRes
    }
  }

  fun setTab(tab: WorkspaceTab) {
    _activeTab.value = tab
  }

  fun selectProject(projectId: String) {
    repository.selectProject(projectId)
    _selectedFilePath.value = "src/App.tsx"
  }

  fun selectFile(path: String) {
    _selectedFilePath.value = path
  }

  fun saveFile(path: String, content: String) {
    val projId = activeProjectId.value ?: return
    viewModelScope.launch {
      repository.saveFile(projId, path, content)
    }
  }

  fun deleteFile(path: String) {
    val projId = activeProjectId.value ?: return
    viewModelScope.launch {
      repository.deleteFile(projId, path)
      if (_selectedFilePath.value == path) {
        _selectedFilePath.value = projectFiles.value.firstOrNull { it.path != path }?.path
      }
    }
  }

  fun sendPrompt(prompt: String) {
    val projId = activeProjectId.value ?: return
    viewModelScope.launch {
      repository.executeAiPrompt(
        projectId = projId,
        prompt = prompt,
        customApiKey = _customApiKey.value,
        provider = _aiProvider.value,
        modelName = _aiModel.value
      )
    }
  }

  fun toggleRunProject() {
    val proj = activeProject.value ?: return
    viewModelScope.launch {
      if (proj.status == ProjectStatus.RUNNING.name) {
        repository.stopProject(proj.id)
      } else {
        repository.startProject(proj.id)
      }
    }
  }

  fun createCheckpoint(summary: String) {
    val projId = activeProjectId.value ?: return
    viewModelScope.launch {
      repository.createCheckpoint(projId, summary)
    }
  }

  fun rollbackToCheckpoint(checkpointId: String) {
    val projId = activeProjectId.value ?: return
    viewModelScope.launch {
      repository.rollbackToCheckpoint(projId, checkpointId)
    }
  }

  fun executeApiRequest(method: String, path: String, headers: Map<String, String>, body: String) {
    val port = activeProject.value?.port ?: 4100
    _isApiExecuting.value = true
    viewModelScope.launch {
      val res = repository.executeApiRequest(method, path, headers, body, port)
      _lastApiResponse.value = res
      _isApiExecuting.value = false
    }
  }

  fun saveEndpoint(name: String, method: String, path: String, headers: String, body: String) {
    val projId = activeProjectId.value ?: return
    viewModelScope.launch {
      repository.saveEndpoint(projId, name, method, path, headers, body)
    }
  }

  fun executeSql(sql: String) {
    _isDbExecuting.value = true
    viewModelScope.launch {
      val res = repository.executeSql(sql)
      _lastQueryResult.value = res
      _isDbExecuting.value = false
    }
  }

  fun resetDatabase() {
    repository.dbEngine.seedDefaultTables()
    executeSql("SELECT * FROM tasks;")
  }

  fun getDatabaseTables(): List<DatabaseTable> {
    return repository.dbEngine.getTables()
  }

  fun setAiProvider(provider: String) {
    _aiProvider.value = provider
  }

  fun setAiModel(model: String) {
    _aiModel.value = model
  }

  fun setCustomApiKey(key: String) {
    _customApiKey.value = key
  }

  fun createNewProject(name: String, desc: String, stack: String) {
    viewModelScope.launch {
      repository.createNewProject(name, desc, stack)
    }
  }

  fun clearLogs() {
    repository.clearLogs()
  }
}
