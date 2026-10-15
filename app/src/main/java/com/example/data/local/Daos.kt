package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ApiEndpointEntity
import com.example.data.model.ChatMessageEntity
import com.example.data.model.CheckpointEntity
import com.example.data.model.ProjectEntity
import com.example.data.model.ProjectFileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {
  @Query("SELECT * FROM projects ORDER BY updatedAt DESC")
  fun getAllProjects(): Flow<List<ProjectEntity>>

  @Query("SELECT * FROM projects WHERE id = :id LIMIT 1")
  suspend fun getProjectById(id: String): ProjectEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertProject(project: ProjectEntity)

  @Update
  suspend fun updateProject(project: ProjectEntity)

  @Query("DELETE FROM projects WHERE id = :id")
  suspend fun deleteProject(id: String)
}

@Dao
interface ProjectFileDao {
  @Query("SELECT * FROM project_files WHERE projectId = :projectId ORDER BY path ASC")
  fun getFilesForProject(projectId: String): Flow<List<ProjectFileEntity>>

  @Query("SELECT * FROM project_files WHERE projectId = :projectId AND path = :path LIMIT 1")
  suspend fun getFileByPath(projectId: String, path: String): ProjectFileEntity?

  @Query("SELECT * FROM project_files WHERE projectId = :projectId")
  suspend fun getFilesSnapshot(projectId: String): List<ProjectFileEntity>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertOrUpdateFile(file: ProjectFileEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAll(files: List<ProjectFileEntity>)

  @Query("DELETE FROM project_files WHERE projectId = :projectId AND path = :path")
  suspend fun deleteFile(projectId: String, path: String)

  @Query("DELETE FROM project_files WHERE projectId = :projectId")
  suspend fun deleteAllForProject(projectId: String)
}

@Dao
interface ChatMessageDao {
  @Query("SELECT * FROM chat_messages WHERE projectId = :projectId ORDER BY createdAt ASC")
  fun getMessagesForProject(projectId: String): Flow<List<ChatMessageEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertMessage(message: ChatMessageEntity)

  @Query("DELETE FROM chat_messages WHERE projectId = :projectId")
  suspend fun clearMessagesForProject(projectId: String)
}

@Dao
interface CheckpointDao {
  @Query("SELECT * FROM checkpoints WHERE projectId = :projectId ORDER BY createdAt DESC")
  fun getCheckpointsForProject(projectId: String): Flow<List<CheckpointEntity>>

  @Query("SELECT * FROM checkpoints WHERE id = :id LIMIT 1")
  suspend fun getCheckpointById(id: String): CheckpointEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertCheckpoint(checkpoint: CheckpointEntity)

  @Query("DELETE FROM checkpoints WHERE id = :id")
  suspend fun deleteCheckpoint(id: String)
}

@Dao
interface ApiEndpointDao {
  @Query("SELECT * FROM api_endpoints WHERE projectId = :projectId ORDER BY name ASC")
  fun getEndpointsForProject(projectId: String): Flow<List<ApiEndpointEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertEndpoint(endpoint: ApiEndpointEntity)

  @Query("DELETE FROM api_endpoints WHERE id = :id")
  suspend fun deleteEndpoint(id: String)
}
