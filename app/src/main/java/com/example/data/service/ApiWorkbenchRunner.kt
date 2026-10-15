package com.example.data.service

import com.example.data.model.ApiResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class ApiWorkbenchRunner {
  private val client = OkHttpClient.Builder()
    .connectTimeout(10, TimeUnit.SECONDS)
    .readTimeout(15, TimeUnit.SECONDS)
    .build()

  suspend fun execute(
    method: String,
    urlOrPath: String,
    headersMap: Map<String, String>,
    bodyPayload: String,
    projectPort: Int = 4100
  ): ApiResponse = withContext(Dispatchers.IO) {
    val startTime = System.currentTimeMillis()

    // If external URL (http:// or https://)
    if (urlOrPath.startsWith("http://") || urlOrPath.startsWith("https://")) {
      try {
        val reqBuilder = Request.Builder().url(urlOrPath)
        headersMap.forEach { (k, v) -> reqBuilder.addHeader(k, v) }

        val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
        val requestBody = if (method in listOf("POST", "PUT", "PATCH")) {
          bodyPayload.toRequestBody(mediaType)
        } else null

        reqBuilder.method(method, requestBody)
        val response = client.newCall(reqBuilder.build()).execute()
        val duration = System.currentTimeMillis() - startTime
        val bodyStr = response.body?.string() ?: ""

        val headers = response.headers.names().associateWith { response.headers[it] ?: "" }

        return@withContext ApiResponse(
          statusCode = response.code,
          statusMessage = response.message.ifEmpty { "OK" },
          durationMs = duration,
          headers = headers,
          body = bodyStr,
          isError = !response.isSuccessful
        )
      } catch (e: Exception) {
        val duration = System.currentTimeMillis() - startTime
        return@withContext ApiResponse(
          statusCode = 502,
          statusMessage = "Bad Gateway / Network Error",
          durationMs = duration,
          headers = mapOf("Content-Type" to "application/json"),
          body = """{"error": "${e.message?.replace("\"", "\\\"")}"}""",
          isError = true
        )
      }
    }

    // Local simulated Express API routes
    val cleanPath = if (urlOrPath.startsWith("/")) urlOrPath else "/$urlOrPath"
    kotlinx.coroutines.delay(45) // Simulate realistic microservice latency
    val duration = System.currentTimeMillis() - startTime

    val mockHeaders = mapOf(
      "Content-Type" to "application/json; charset=utf-8",
      "X-Powered-By" to "Express (Coding to AI Sandbox)",
      "Access-Control-Allow-Origin" to "*",
      "X-Response-Time" to "${duration}ms",
      "Server" to "CodingToAI-Sandbox/:$projectPort"
    )

    when {
      cleanPath == "/api/health" -> {
        ApiResponse(
          statusCode = 200,
          statusMessage = "OK",
          durationMs = duration,
          headers = mockHeaders,
          body = """{
  "status": "ok",
  "service": "coding-to-ai-sandbox",
  "port": $projectPort,
  "uptime": 1420.84,
  "memoryUsage": {
    "rss": "42.8 MB",
    "heapTotal": "28.4 MB",
    "heapUsed": "19.1 MB"
  },
  "timestamp": "${System.currentTimeMillis()}"
}"""
        )
      }

      cleanPath == "/api/tasks" && method == "GET" -> {
        ApiResponse(
          statusCode = 200,
          statusMessage = "OK",
          durationMs = duration,
          headers = mockHeaders,
          body = """{
  "success": true,
  "total": 4,
  "data": [
    {
      "id": "1",
      "title": "Design OAuth2 token rotation flow",
      "tag": "Security",
      "status": "IN_PROGRESS",
      "assignee": "Alex",
      "priority": "HIGH"
    },
    {
      "id": "2",
      "title": "Optimize query planner on Postgres index",
      "tag": "Database",
      "status": "DONE",
      "assignee": "Sarah",
      "priority": "MEDIUM"
    },
    {
      "id": "3",
      "title": "Implement live sandbox container lifecycle",
      "tag": "Core",
      "status": "TODO",
      "assignee": "Elena",
      "priority": "HIGH"
    },
    {
      "id": "4",
      "title": "Add Monaco editor syntax tree diagnostics",
      "tag": "Frontend",
      "status": "TODO",
      "assignee": "Marcus",
      "priority": "LOW"
    }
  ]
}"""
        )
      }

      cleanPath == "/api/tasks" && method == "POST" -> {
        val parsedTitle = runCatching {
          JSONObject(bodyPayload).optString("title", "New Task from API Workbench")
        }.getOrDefault("New Task from API Workbench")

        ApiResponse(
          statusCode = 201,
          statusMessage = "Created",
          durationMs = duration,
          headers = mockHeaders,
          body = """{
  "success": true,
  "message": "Task created successfully in SQLite storage.",
  "data": {
    "id": "${System.currentTimeMillis()}",
    "title": "$parsedTitle",
    "status": "TODO",
    "createdAt": "${System.currentTimeMillis()}"
  }
}"""
        )
      }

      cleanPath.startsWith("/api/tasks/") && method == "DELETE" -> {
        val taskId = cleanPath.substringAfterLast("/")
        ApiResponse(
          statusCode = 200,
          statusMessage = "OK",
          durationMs = duration,
          headers = mockHeaders,
          body = """{
  "success": true,
  "message": "Task $taskId successfully deleted."
}"""
        )
      }

      cleanPath == "/api/users" -> {
        ApiResponse(
          statusCode = 200,
          statusMessage = "OK",
          durationMs = duration,
          headers = mockHeaders,
          body = """{
  "success": true,
  "users": [
    { "id": "u1", "name": "Alex Mercer", "email": "alex@example.com", "role": "admin" },
    { "id": "u2", "name": "Sarah Connor", "email": "sarah@example.com", "role": "developer" },
    { "id": "u3", "name": "Elena Rostova", "email": "elena@example.com", "role": "architect" }
  ]
}"""
        )
      }

      else -> {
        ApiResponse(
          statusCode = 404,
          statusMessage = "Not Found",
          durationMs = duration,
          headers = mockHeaders,
          body = """{
  "error": "Route $method $cleanPath not found in active Express router.",
  "availableRoutes": [
    "GET  /api/health",
    "GET  /api/tasks",
    "POST /api/tasks",
    "DELETE /api/tasks/:id",
    "GET  /api/users"
  ]
}""",
          isError = true
        )
      }
    }
  }
}
