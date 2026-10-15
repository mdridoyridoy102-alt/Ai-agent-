package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

enum class ProjectStatus {
  IDLE,
  GENERATING,
  RUNNING,
  STOPPED,
  ERROR
}

@Entity(tableName = "projects")
data class ProjectEntity(
  @PrimaryKey val id: String = UUID.randomUUID().toString(),
  val name: String,
  val description: String = "",
  val techStack: String = "React + Node.js",
  val status: String = ProjectStatus.IDLE.name,
  val port: Int? = null,
  val createdAt: Long = System.currentTimeMillis(),
  val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "project_files")
data class ProjectFileEntity(
  @PrimaryKey val id: String = UUID.randomUUID().toString(),
  val projectId: String,
  val path: String,
  val content: String,
  val updatedAt: Long = System.currentTimeMillis(),
  val isModifiedLocally: Boolean = false
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
  @PrimaryKey val id: String = UUID.randomUUID().toString(),
  val projectId: String,
  val role: String, // "user", "assistant", "system"
  val content: String,
  val taskState: String? = null, // "PLANNING", "CODE_GENERATION", "COMPLETED", "ERROR"
  val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "checkpoints")
data class CheckpointEntity(
  @PrimaryKey val id: String = UUID.randomUUID().toString(),
  val projectId: String,
  val summary: String,
  val filesStateJson: String, // Serialized map of path -> content
  val fileCount: Int = 0,
  val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "api_endpoints")
data class ApiEndpointEntity(
  @PrimaryKey val id: String = UUID.randomUUID().toString(),
  val projectId: String,
  val name: String,
  val method: String, // GET, POST, PUT, DELETE, PATCH
  val path: String,
  val headersJson: String = "{}",
  val bodySchemaJson: String = "{}"
)

data class ExecutionLog(
  val id: String = UUID.randomUUID().toString(),
  val projectId: String,
  val timestamp: Long = System.currentTimeMillis(),
  val type: String, // "STDOUT", "STDERR", "SYSTEM"
  val message: String
)

data class DatabaseColumn(
  val name: String,
  val type: String,
  val isPrimaryKey: Boolean = false,
  val isNullable: Boolean = true
)

data class DatabaseTable(
  val name: String,
  val columns: List<DatabaseColumn>,
  val rowCount: Int,
  val rows: List<Map<String, String>> = emptyList()
)

data class GeneratedFileOperation(
  val action: String, // "CREATE", "UPDATE", "DELETE"
  val path: String,
  val content: String? = null
)

data class GenerationResult(
  val plan: String,
  val files: List<GeneratedFileOperation>
)

data class ApiResponse(
  val statusCode: Int,
  val statusMessage: String,
  val durationMs: Long,
  val headers: Map<String, String>,
  val body: String,
  val isError: Boolean = false
)
