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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.window.Dialog
import com.example.data.model.CheckpointEntity
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SnapshotsPanel(
  checkpoints: List<CheckpointEntity>,
  onCreateCheckpoint: (summary: String) -> Unit,
  onRollback: (checkpointId: String) -> Unit,
  modifier: Modifier = Modifier
) {
  var showCreateDialog by remember { mutableStateOf(false) }
  var rollbackTarget by remember { mutableStateOf<CheckpointEntity?>(null) }
  val context = LocalContext.current
  val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.getDefault()) }

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
        Icon(Icons.Default.History, contentDescription = null, tint = Indigo500, modifier = Modifier.size(16.dp))
        Text("VERSION CONTROL & SNAPSHOTS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Slate300, letterSpacing = 1.sp)
      }

      Button(
        onClick = { showCreateDialog = true },
        colors = ButtonDefaults.buttonColors(containerColor = Indigo600),
        shape = RoundedCornerShape(6.dp),
        modifier = Modifier.height(28.dp).testTag("create_snapshot_button")
      ) {
        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(12.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text("Create Snapshot", fontSize = 10.sp, fontWeight = FontWeight.Bold)
      }
    }

    // Timeline List
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(14.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      if (checkpoints.isEmpty()) {
        item {
          Text("No checkpoints recorded yet.", color = Slate500, fontSize = 12.sp)
        }
      }

      items(checkpoints, key = { it.id }) { ckpt ->
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Slate900)
            .border(1.dp, Slate800, RoundedCornerShape(10.dp))
            .padding(14.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
          ) {
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Indigo600.copy(alpha = 0.2f))
                .border(1.dp, Indigo500.copy(alpha = 0.4f), CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Icon(Icons.Default.History, contentDescription = null, tint = Cyan400, modifier = Modifier.size(18.dp))
            }

            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
              Text(
                text = ckpt.summary,
                color = Slate100,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
              )
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Text(
                  text = dateFormat.format(Date(ckpt.createdAt)),
                  color = Slate400,
                  fontSize = 10.sp,
                  fontFamily = FontFamily.Monospace
                )
                if (ckpt.fileCount > 0) {
                  Text(
                    text = "• ${ckpt.fileCount} files tracked",
                    color = Slate500,
                    fontSize = 10.sp
                  )
                }
              }
            }
          }

          Button(
            onClick = { rollbackTarget = ckpt },
            colors = ButtonDefaults.buttonColors(containerColor = Slate800),
            shape = RoundedCornerShape(6.dp),
            modifier = Modifier.height(30.dp)
          ) {
            Icon(Icons.Default.Restore, contentDescription = "Rollback", tint = Cyan400, modifier = Modifier.size(12.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Rollback", fontSize = 11.sp, color = Cyan400, fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }

  // Rollback Confirmation Dialog
  rollbackTarget?.let { target ->
    AlertDialog(
      onDismissRequest = { rollbackTarget = null },
      title = { Text("Rollback to Checkpoint?", color = Slate100, fontSize = 16.sp) },
      text = {
        Text(
          "This will restore all files in this project to the snapshot:\n'${target.summary}'\n\nAny unsaved changes will be overridden.",
          color = Slate300,
          fontSize = 12.sp
        )
      },
      containerColor = Slate900,
      confirmButton = {
        Button(
          onClick = {
            onRollback(target.id)
            rollbackTarget = null
            Toast.makeText(context, "Rolled back to checkpoint", Toast.LENGTH_SHORT).show()
          },
          colors = ButtonDefaults.buttonColors(containerColor = Indigo600)
        ) {
          Text("Confirm Rollback")
        }
      },
      dismissButton = {
        TextButton(onClick = { rollbackTarget = null }) {
          Text("Cancel", color = Slate400)
        }
      }
    )
  }

  // Create Snapshot Dialog
  if (showCreateDialog) {
    var summaryText by remember { mutableStateOf("") }
    Dialog(onDismissRequest = { showCreateDialog = false }) {
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = Slate900,
        border = androidx.compose.foundation.BorderStroke(1.dp, Slate800),
        modifier = Modifier.padding(16.dp)
      ) {
        Column(
          modifier = Modifier.padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Text("Create Workspace Checkpoint", color = Slate100, fontSize = 15.sp, fontWeight = FontWeight.Bold)
          OutlinedTextField(
            value = summaryText,
            onValueChange = { summaryText = it },
            placeholder = { Text("e.g., Before refactoring database schema") },
            colors = OutlinedTextFieldDefaults.colors(
              focusedTextColor = Slate100,
              unfocusedTextColor = Slate100,
              focusedBorderColor = Indigo500,
              unfocusedBorderColor = Slate700
            ),
            modifier = Modifier.fillMaxWidth().testTag("checkpoint_summary_input")
          )
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
          ) {
            TextButton(onClick = { showCreateDialog = false }) {
              Text("Cancel", color = Slate400)
            }
            Button(
              onClick = {
                if (summaryText.isNotBlank()) {
                  onCreateCheckpoint(summaryText)
                  showCreateDialog = false
                  Toast.makeText(context, "Checkpoint created", Toast.LENGTH_SHORT).show()
                }
              },
              enabled = summaryText.isNotBlank(),
              colors = ButtonDefaults.buttonColors(containerColor = Indigo600)
            ) {
              Text("Save Snapshot")
            }
          }
        }
      }
    }
  }
}
