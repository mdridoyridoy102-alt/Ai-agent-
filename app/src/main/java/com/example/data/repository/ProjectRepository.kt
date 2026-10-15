package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.model.ApiEndpointEntity
import com.example.data.model.ApiResponse
import com.example.data.model.ChatMessageEntity
import com.example.data.model.CheckpointEntity
import com.example.data.model.ExecutionLog
import com.example.data.model.ProjectEntity
import com.example.data.model.ProjectFileEntity
import com.example.data.model.ProjectStatus
import com.example.data.service.AiAgentEngine
import com.example.data.service.ApiWorkbenchRunner
import com.example.data.service.DatabaseWorkbenchEngine
import com.example.data.service.ExecutionSandbox
import com.example.data.service.QueryResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.UUID

class ProjectRepository(private val db: AppDatabase) {
  val aiAgentEngine = AiAgentEngine()
  val executionSandbox = ExecutionSandbox()
  val apiRunner = ApiWorkbenchRunner()
  val dbEngine = DatabaseWorkbenchEngine()

  private val _activeProjectId = MutableStateFlow<String?>("proj-collab-task-01")
  val activeProjectId: StateFlow<String?> = _activeProjectId.asStateFlow()

  private val _executionLogs = MutableStateFlow<List<ExecutionLog>>(emptyList())
  val executionLogs: StateFlow<List<ExecutionLog>> = _executionLogs.asStateFlow()

  private val _isGenerating = MutableStateFlow(false)
  val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

  private val _generationStatus = MutableStateFlow<String?>(null)
  val generationStatus: StateFlow<String?> = _generationStatus.asStateFlow()

  fun getAllProjects(): Flow<List<ProjectEntity>> = db.projectDao().getAllProjects()

  suspend fun getProject(id: String): ProjectEntity? = db.projectDao().getProjectById(id)

  fun selectProject(id: String) {
    _activeProjectId.value = id
  }

  fun getFilesForProject(projectId: String): Flow<List<ProjectFileEntity>> =
    db.projectFileDao().getFilesForProject(projectId)

  fun getMessagesForProject(projectId: String): Flow<List<ChatMessageEntity>> =
    db.chatMessageDao().getMessagesForProject(projectId)

  fun getCheckpointsForProject(projectId: String): Flow<List<CheckpointEntity>> =
    db.checkpointDao().getCheckpointsForProject(projectId)

  fun getEndpointsForProject(projectId: String): Flow<List<ApiEndpointEntity>> =
    db.apiEndpointDao().getEndpointsForProject(projectId)

  suspend fun saveFile(projectId: String, path: String, content: String) {
    val existing = db.projectFileDao().getFileByPath(projectId, path)
    if (existing != null) {
      db.projectFileDao().insertOrUpdateFile(
        existing.copy(content = content, updatedAt = System.currentTimeMillis(), isModifiedLocally = false)
      )
    } else {
      db.projectFileDao().insertOrUpdateFile(
        ProjectFileEntity(
          projectId = projectId,
          path = path,
          content = content,
          isModifiedLocally = false
        )
      )
    }
    // Update project timestamp
    val project = db.projectDao().getProjectById(projectId)
    if (project != null) {
      db.projectDao().updateProject(project.copy(updatedAt = System.currentTimeMillis()))
    }
  }

  suspend fun deleteFile(projectId: String, path: String) {
    db.projectFileDao().deleteFile(projectId, path)
  }

  suspend fun createCheckpoint(projectId: String, summary: String): String {
    val files = db.projectFileDao().getFilesSnapshot(projectId)
    val stateMap = JSONObject()
    files.forEach { stateMap.put(it.path, it.content) }

    val checkpoint = CheckpointEntity(
      projectId = projectId,
      summary = summary,
      filesStateJson = stateMap.toString(),
      fileCount = files.size
    )
    db.checkpointDao().insertCheckpoint(checkpoint)
    return checkpoint.id
  }

  suspend fun rollbackToCheckpoint(projectId: String, checkpointId: String) {
    val checkpoint = db.checkpointDao().getCheckpointById(checkpointId) ?: return
    val json = JSONObject(checkpoint.filesStateJson)
    val restoredFiles = mutableListOf<ProjectFileEntity>()

    val keys = json.keys()
    while (keys.hasNext()) {
      val path = keys.next()
      val content = json.getString(path)
      restoredFiles.add(
        ProjectFileEntity(
          projectId = projectId,
          path = path,
          content = content,
          updatedAt = System.currentTimeMillis()
        )
      )
    }

    if (restoredFiles.isNotEmpty()) {
      db.projectFileDao().deleteAllForProject(projectId)
      db.projectFileDao().insertAll(restoredFiles)
    }

    // Add audit log message
    db.chatMessageDao().insertMessage(
      ChatMessageEntity(
        projectId = projectId,
        role = "system",
        content = "Rolled back project files to checkpoint: '${checkpoint.summary}'.",
        taskState = "COMPLETED"
      )
    )
  }

  suspend fun executeAiPrompt(
    projectId: String,
    prompt: String,
    customApiKey: String? = null,
    provider: String = "GEMINI",
    modelName: String = "gemini-2.5-flash"
  ) {
    _isGenerating.value = true
    _generationStatus.value = "Analyzing requirements & planning architecture..."

    // Record user message
    db.chatMessageDao().insertMessage(
      ChatMessageEntity(
        projectId = projectId,
        role = "user",
        content = prompt,
        taskState = "PLANNING"
      )
    )

    try {
      // 1. Capture auto-checkpoint before modifications
      createCheckpoint(projectId, "Auto-checkpoint before: ${prompt.take(35)}")

      // 2. Fetch existing files
      val currentFiles = db.projectFileDao().getFilesSnapshot(projectId)

      _generationStatus.value = "Synthesizing full-stack code changes..."

      // 3. Plan & Generate
      val result = aiAgentEngine.planAndGenerate(
        projectId = projectId,
        prompt = prompt,
        existingFiles = currentFiles,
        customApiKey = customApiKey,
        provider = provider,
        modelName = modelName
      )

      _generationStatus.value = "Applying ${result.files.size} file changes to workspace..."

      // 4. Apply file changes
      for (op in result.files) {
        if (op.action == "DELETE") {
          db.projectFileDao().deleteFile(projectId, op.path)
        } else if (op.content != null) {
          saveFile(projectId, op.path, op.content)
        }
      }

      // 5. Record assistant response
      val assistantMsg = "${result.plan}\n\nModified ${result.files.size} files:\n" +
          result.files.joinToString("\n") { "• [${it.action}] ${it.path}" }

      db.chatMessageDao().insertMessage(
        ChatMessageEntity(
          projectId = projectId,
          role = "assistant",
          content = assistantMsg,
          taskState = "COMPLETED"
        )
      )
    } catch (e: Exception) {
      db.chatMessageDao().insertMessage(
        ChatMessageEntity(
          projectId = projectId,
          role = "assistant",
          content = "Generation encountered an issue: ${e.message}",
          taskState = "ERROR"
        )
      )
    } finally {
      _isGenerating.value = false
      _generationStatus.value = null
    }
  }

  suspend fun startProject(projectId: String) {
    val project = db.projectDao().getProjectById(projectId) ?: return
    val port = project.port ?: executionSandbox.acquirePort()

    db.projectDao().updateProject(project.copy(status = ProjectStatus.RUNNING.name, port = port))

    val files = db.projectFileDao().getFilesSnapshot(projectId)
    executionSandbox.simulateStart(projectId, port, files) { log ->
      _executionLogs.value = _executionLogs.value + log
    }
  }

  suspend fun stopProject(projectId: String) {
    val project = db.projectDao().getProjectById(projectId) ?: return
    db.projectDao().updateProject(project.copy(status = ProjectStatus.STOPPED.name))
    _executionLogs.value = _executionLogs.value + ExecutionLog(
      projectId = projectId,
      type = "SYSTEM",
      message = "[${System.currentTimeMillis()}] Process terminated. Sandbox port ${project.port} released."
    )
  }

  fun clearLogs() {
    _executionLogs.value = emptyList()
  }

  suspend fun executeApiRequest(
    method: String,
    urlOrPath: String,
    headers: Map<String, String>,
    body: String,
    port: Int = 4100
  ): ApiResponse {
    return apiRunner.execute(method, urlOrPath, headers, body, port)
  }

  suspend fun saveEndpoint(
    projectId: String,
    name: String,
    method: String,
    path: String,
    headersJson: String = "{}",
    bodySchemaJson: String = "{}"
  ) {
    db.apiEndpointDao().insertEndpoint(
      ApiEndpointEntity(
        projectId = projectId,
        name = name,
        method = method,
        path = path,
        headersJson = headersJson,
        bodySchemaJson = bodySchemaJson
      )
    )
  }

  suspend fun executeSql(sql: String): QueryResult {
    return dbEngine.executeQuery(sql)
  }

  suspend fun createNewProject(name: String, description: String, techStack: String): String {
    val newId = "proj-" + UUID.randomUUID().toString().take(8)
    val newProj = ProjectEntity(
      id = newId,
      name = name,
      description = description,
      techStack = techStack,
      status = ProjectStatus.STOPPED.name,
      port = executionSandbox.acquirePort()
    )
    db.projectDao().insertProject(newProj)

    // Base template files
    val templateFiles = listOf(
      ProjectFileEntity(
        projectId = newId,
        path = "package.json",
        content = """{
  "name": "${name.lowercase().replace(" ", "-")}",
  "version": "1.0.0",
  "dependencies": {
    "react": "^18.3.1",
    "express": "^4.19.2"
  }
}"""
      ),
      ProjectFileEntity(
        projectId = newId,
        path = "src/App.tsx",
        content = """import React from 'react';

export default function App() {
  return (
    <div className="p-6 bg-slate-950 text-slate-100 min-h-screen">
      <h1 className="text-xl font-bold text-indigo-400">$name</h1>
      <p className="text-xs text-slate-400 mt-1">$description</p>
    </div>
  );
}"""
      )
    )
    db.projectFileDao().insertAll(templateFiles)
    selectProject(newId)
    return newId
  }
}
