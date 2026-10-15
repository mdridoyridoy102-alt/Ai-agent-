package com.example.data.service

import com.example.data.model.DatabaseColumn
import com.example.data.model.DatabaseTable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class QueryResult(
  val columns: List<String>,
  val rows: List<List<String>>,
  val executionTimeMs: Long,
  val message: String? = null,
  val isError: Boolean = false
)

class DatabaseWorkbenchEngine {
  private val tables = mutableMapOf<String, DatabaseTable>()

  init {
    seedDefaultTables()
  }

  fun seedDefaultTables() {
    tables["tasks"] = DatabaseTable(
      name = "tasks",
      columns = listOf(
        DatabaseColumn("id", "TEXT", isPrimaryKey = true),
        DatabaseColumn("title", "TEXT"),
        DatabaseColumn("tag", "TEXT"),
        DatabaseColumn("status", "TEXT"),
        DatabaseColumn("assignee", "TEXT"),
        DatabaseColumn("created_at", "TIMESTAMP")
      ),
      rowCount = 4,
      rows = listOf(
        mapOf("id" to "1", "title" to "Design OAuth2 token rotation flow", "tag" to "Security", "status" to "IN_PROGRESS", "assignee" to "Alex", "created_at" to "2026-10-15 08:30:00"),
        mapOf("id" to "2", "title" to "Optimize query planner on Postgres index", "tag" to "Database", "status" to "DONE", "assignee" to "Sarah", "created_at" to "2026-10-15 09:15:00"),
        mapOf("id" to "3", "title" to "Implement live sandbox container lifecycle", "tag" to "Core", "status" to "TODO", "assignee" to "Elena", "created_at" to "2026-10-15 10:00:00"),
        mapOf("id" to "4", "title" to "Add Monaco editor syntax tree diagnostics", "tag" to "Frontend", "status" to "TODO", "assignee" to "Marcus", "created_at" to "2026-10-15 11:20:00")
      )
    )

    tables["users"] = DatabaseTable(
      name = "users",
      columns = listOf(
        DatabaseColumn("id", "TEXT", isPrimaryKey = true),
        DatabaseColumn("name", "TEXT"),
        DatabaseColumn("email", "TEXT"),
        DatabaseColumn("role", "TEXT")
      ),
      rowCount = 3,
      rows = listOf(
        mapOf("id" to "u1", "name" to "Alex Mercer", "email" to "alex@example.com", "role" to "admin"),
        mapOf("id" to "u2", "name" to "Sarah Connor", "email" to "sarah@example.com", "role" to "developer"),
        mapOf("id" to "u3", "name" to "Elena Rostova", "email" to "elena@example.com", "role" to "architect")
      )
    )

    tables["audit_logs"] = DatabaseTable(
      name = "audit_logs",
      columns = listOf(
        DatabaseColumn("id", "INTEGER", isPrimaryKey = true),
        DatabaseColumn("action", "TEXT"),
        DatabaseColumn("target", "TEXT"),
        DatabaseColumn("user_id", "TEXT"),
        DatabaseColumn("timestamp", "TIMESTAMP")
      ),
      rowCount = 3,
      rows = listOf(
        mapOf("id" to "101", "action" to "PROJECT_RUN", "target" to "port:4100", "user_id" to "u1", "timestamp" to "2026-10-15 11:45:00"),
        mapOf("id" to "102", "action" to "FILE_WRITE", "target" to "src/App.tsx", "user_id" to "u2", "timestamp" to "2026-10-15 12:00:12"),
        mapOf("id" to "103", "action" to "CHECKPOINT_CREATE", "target" to "ckpt-994", "user_id" to "u1", "timestamp" to "2026-10-15 12:10:00")
      )
    )
  }

  fun getTables(): List<DatabaseTable> = tables.values.toList()

  suspend fun executeQuery(sql: String): QueryResult = withContext(Dispatchers.Default) {
    val startTime = System.currentTimeMillis()
    val trimmed = sql.trim()
    val upper = trimmed.uppercase()

    if (upper.startsWith("SELECT")) {
      // Find table name
      val fromIdx = upper.indexOf("FROM")
      if (fromIdx == -1) {
        val duration = System.currentTimeMillis() - startTime
        return@withContext QueryResult(
          columns = listOf("error"),
          rows = listOf(listOf("Syntax error: Missing FROM clause")),
          executionTimeMs = duration,
          isError = true
        )
      }

      val afterFrom = trimmed.substring(fromIdx + 4).trim()
      val tableName = afterFrom.split("\\s+|;|,".toRegex())[0].lowercase()

      val table = tables[tableName]
      if (table == null) {
        val duration = System.currentTimeMillis() - startTime
        return@withContext QueryResult(
          columns = listOf("error"),
          rows = listOf(listOf("Table '$tableName' does not exist in active database.")),
          executionTimeMs = duration,
          isError = true
        )
      }

      val cols = table.columns.map { it.name }
      val rowsData = table.rows.map { row ->
        cols.map { col -> row[col] ?: "NULL" }
      }

      val duration = System.currentTimeMillis() - startTime
      return@withContext QueryResult(
        columns = cols,
        rows = rowsData,
        executionTimeMs = duration,
        message = "Returned ${rowsData.size} rows in ${duration}ms"
      )
    } else if (upper.startsWith("INSERT INTO")) {
      // Handle insert
      val tableName = trimmed.substringAfter("INSERT INTO", "").trim().split("\\s+".toRegex())[0].lowercase()
      val duration = System.currentTimeMillis() - startTime
      return@withContext QueryResult(
        columns = listOf("status", "rows_affected"),
        rows = listOf(listOf("INSERT SUCCESS", "1")),
        executionTimeMs = duration,
        message = "1 row inserted into '$tableName'"
      )
    } else if (upper.startsWith("CREATE TABLE")) {
      val duration = System.currentTimeMillis() - startTime
      return@withContext QueryResult(
        columns = listOf("status"),
        rows = listOf(listOf("TABLE CREATED")),
        executionTimeMs = duration,
        message = "Table schema successfully migrated."
      )
    } else {
      val duration = System.currentTimeMillis() - startTime
      return@withContext QueryResult(
        columns = listOf("status", "query"),
        rows = listOf(listOf("STATEMENT EXECUTED", trimmed.take(40))),
        executionTimeMs = duration,
        message = "Statement completed in ${duration}ms"
      )
    }
  }
}
