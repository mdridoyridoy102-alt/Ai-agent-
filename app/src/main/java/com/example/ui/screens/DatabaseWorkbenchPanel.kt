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
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DatabaseTable
import com.example.data.service.QueryResult
import com.example.ui.theme.Amber400
import com.example.ui.theme.Cyan400
import com.example.ui.theme.Emerald400
import com.example.ui.theme.Indigo400
import com.example.ui.theme.Indigo500
import com.example.ui.theme.Indigo600
import com.example.ui.theme.Rose500
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950

@Composable
fun DatabaseWorkbenchPanel(
  tables: List<DatabaseTable>,
  lastQueryResult: QueryResult?,
  isExecuting: Boolean,
  onExecuteSql: (String) -> Unit,
  onResetDatabase: () -> Unit,
  modifier: Modifier = Modifier
) {
  var sqlInput by remember { mutableStateOf("SELECT * FROM tasks;") }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(Slate950)
  ) {
    // Top Bar
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .background(Slate900)
        .border(1.dp, Slate800)
        .padding(horizontal = 14.dp, vertical = 8.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        Icon(Icons.Default.Storage, contentDescription = null, tint = Emerald400, modifier = Modifier.size(16.dp))
        Text("DATABASE WORKBENCH", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Slate300, letterSpacing = 1.sp)
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(Emerald400.copy(alpha = 0.15f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
          Text("SQLite / Postgres", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Emerald400)
        }
      }

      Button(
        onClick = onResetDatabase,
        colors = ButtonDefaults.buttonColors(containerColor = Slate800),
        shape = RoundedCornerShape(6.dp),
        modifier = Modifier.height(28.dp)
      ) {
        Icon(Icons.Default.Refresh, contentDescription = null, tint = Slate300, modifier = Modifier.size(12.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text("Seed Sample", fontSize = 10.sp, color = Slate300)
      }
    }

    // Tables quick chips
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .background(Slate900.copy(alpha = 0.4f))
        .horizontalScroll(rememberScrollState())
        .padding(horizontal = 12.dp, vertical = 8.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      tables.forEach { table ->
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Slate800)
            .border(1.dp, Slate700, RoundedCornerShape(6.dp))
            .clickable {
              sqlInput = "SELECT * FROM ${table.name};"
              onExecuteSql("SELECT * FROM ${table.name};")
            }
            .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Icon(Icons.Default.TableChart, contentDescription = null, tint = Cyan400, modifier = Modifier.size(13.dp))
            Text(table.name, fontSize = 11.sp, color = Slate200, fontFamily = FontFamily.Monospace)
            Text("(${table.rowCount})", fontSize = 9.sp, color = Slate500)
          }
        }
      }
    }

    // SQL Console Editor
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .background(Slate950)
        .border(1.dp, Slate800)
        .padding(10.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      OutlinedTextField(
        value = sqlInput,
        onValueChange = { sqlInput = it },
        placeholder = { Text("Enter SQL query (e.g., SELECT * FROM tasks;)", fontSize = 11.sp, color = Slate500) },
        minLines = 2,
        maxLines = 4,
        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, fontFamily = FontFamily.Monospace, color = Slate100),
        colors = OutlinedTextFieldDefaults.colors(
          focusedTextColor = Slate100,
          unfocusedTextColor = Slate100,
          focusedBorderColor = Indigo500,
          unfocusedBorderColor = Slate700,
          focusedContainerColor = Slate900,
          unfocusedContainerColor = Slate900
        ),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("sql_query_input")
      )

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          listOf("SELECT * FROM tasks", "SELECT * FROM users", "SELECT * FROM audit_logs").forEach { quickSql ->
            Text(
              text = quickSql.substringAfter("FROM "),
              fontSize = 10.sp,
              color = Indigo400,
              modifier = Modifier
                .clickable {
                  sqlInput = "$quickSql;"
                  onExecuteSql("$quickSql;")
                }
                .padding(4.dp)
            )
          }
        }

        Button(
          onClick = { onExecuteSql(sqlInput) },
          enabled = sqlInput.isNotBlank() && !isExecuting,
          colors = ButtonDefaults.buttonColors(containerColor = Emerald400),
          shape = RoundedCornerShape(6.dp),
          modifier = Modifier.testTag("execute_sql_button")
        ) {
          if (isExecuting) {
            CircularProgressIndicator(modifier = Modifier.size(14.dp), color = Slate950, strokeWidth = 2.dp)
          } else {
            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Slate950, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Execute SQL", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Slate950)
          }
        }
      }
    }

    // Results Tabular Grid
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
        Text("QUERY RESULTS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Slate400, letterSpacing = 1.sp)
        if (lastQueryResult != null) {
          Text(
            text = lastQueryResult.message ?: "${lastQueryResult.rows.size} rows",
            fontSize = 10.sp,
            color = if (lastQueryResult.isError) Rose500 else Cyan400,
            fontFamily = FontFamily.Monospace
          )
        }
      }

      Spacer(modifier = Modifier.height(6.dp))

      Box(
        modifier = Modifier
          .weight(1f)
          .fillMaxWidth()
          .clip(RoundedCornerShape(8.dp))
          .background(Slate900)
          .border(1.dp, Slate800, RoundedCornerShape(8.dp))
      ) {
        if (lastQueryResult == null) {
          Text("No query executed yet.", color = Slate500, fontSize = 11.sp, modifier = Modifier.padding(10.dp))
        } else if (lastQueryResult.isError) {
          Text(
            text = lastQueryResult.rows.firstOrNull()?.firstOrNull() ?: "Query error",
            color = Rose500,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(10.dp)
          )
        } else {
          // Tabular Grid
          Column(
            modifier = Modifier
              .fillMaxSize()
              .verticalScroll(rememberScrollState())
              .horizontalScroll(rememberScrollState())
          ) {
            // Header Row
            Row(
              modifier = Modifier
                .background(Slate800)
                .padding(vertical = 8.dp, horizontal = 12.dp)
            ) {
              lastQueryResult.columns.forEach { col ->
                Text(
                  text = col.uppercase(),
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  color = Cyan400,
                  fontFamily = FontFamily.Monospace,
                  modifier = Modifier.width(130.dp)
                )
              }
            }

            // Data Rows
            lastQueryResult.rows.forEachIndexed { idx, row ->
              Row(
                modifier = Modifier
                  .background(if (idx % 2 == 0) Slate900 else Slate900.copy(alpha = 0.6f))
                  .padding(vertical = 8.dp, horizontal = 12.dp)
              ) {
                row.forEach { cell ->
                  Text(
                    text = cell,
                    fontSize = 11.sp,
                    color = Slate200,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.width(130.dp)
                  )
                }
              }
            }
          }
        }
      }
    }
  }
}
