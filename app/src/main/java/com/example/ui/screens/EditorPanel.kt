package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FormatAlignLeft
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.ProjectFileEntity
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
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import com.example.ui.theme.Violet500

@Composable
fun EditorPanel(
  files: List<ProjectFileEntity>,
  selectedFile: ProjectFileEntity?,
  onSelectFile: (String) -> Unit,
  onSaveFile: (path: String, content: String) -> Unit,
  onDeleteFile: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  var showFileListMobile by remember { mutableStateOf(false) }
  var showNewFileDialog by remember { mutableStateOf(false) }
  var fileToDelete by remember { mutableStateOf<String?>(null) }

  var editorText by remember { mutableStateOf("") }
  var isDirty by remember { mutableStateOf(false) }

  val context = LocalContext.current
  val clipboardManager = LocalClipboardManager.current

  LaunchedEffect(selectedFile?.path, selectedFile?.content) {
    editorText = selectedFile?.content ?: ""
    isDirty = false
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(Slate950)
  ) {
    // Top Editor Sub-header: File selection, Path, Dirty Status, Save, Format
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .background(Slate900)
        .border(1.dp, Slate800)
        .padding(horizontal = 12.dp, vertical = 8.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.weight(1f)
      ) {
        // Files Drawer Toggle
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (showFileListMobile) Indigo600.copy(alpha = 0.3f) else Slate800)
            .border(1.dp, if (showFileListMobile) Indigo500 else Slate700, RoundedCornerShape(6.dp))
            .clickable { showFileListMobile = !showFileListMobile }
            .padding(horizontal = 8.dp, vertical = 5.dp)
            .testTag("toggle_file_tree_button")
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Icon(Icons.Default.Folder, contentDescription = "Files", tint = Amber400, modifier = Modifier.size(14.dp))
            Text("Files (${files.size})", fontSize = 11.sp, color = Slate200, fontWeight = FontWeight.SemiBold)
          }
        }

        // Selected File Path Badge
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          Text(
            text = selectedFile?.path ?: "No file selected",
            color = Slate100,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Medium
          )
          if (isDirty) {
            Box(
              modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(Amber400)
            )
          }
        }
      }

      // Actions: Copy, Save
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        IconButton(
          onClick = {
            clipboardManager.setText(AnnotatedString(editorText))
            Toast.makeText(context, "Code copied to clipboard", Toast.LENGTH_SHORT).show()
          },
          modifier = Modifier.size(32.dp)
        ) {
          Icon(Icons.Default.ContentCopy, contentDescription = "Copy code", tint = Slate400, modifier = Modifier.size(16.dp))
        }

        Button(
          onClick = {
            selectedFile?.let {
              onSaveFile(it.path, editorText)
              isDirty = false
              Toast.makeText(context, "Saved ${it.path}", Toast.LENGTH_SHORT).show()
            }
          },
          colors = ButtonDefaults.buttonColors(
            containerColor = if (isDirty) Indigo600 else Slate800
          ),
          shape = RoundedCornerShape(6.dp),
          modifier = Modifier.testTag("save_file_button")
        ) {
          Icon(Icons.Default.Save, contentDescription = "Save", modifier = Modifier.size(14.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text(if (isDirty) "Save *" else "Saved", fontSize = 11.sp, color = if (isDirty) Color.White else Slate400)
        }
      }
    }

    // Horizontal Split / Expandable File Tree
    if (showFileListMobile) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .background(Slate900.copy(alpha = 0.95f))
          .border(1.dp, Slate800)
          .padding(8.dp)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 6.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text("EXPLORER / WORKSPACE FILES", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Slate400, letterSpacing = 0.8.sp)
          TextButton(
            onClick = { showNewFileDialog = true },
            modifier = Modifier.height(28.dp)
          ) {
            Icon(Icons.Default.Add, contentDescription = null, tint = Indigo400, modifier = Modifier.size(12.dp))
            Spacer(modifier = Modifier.width(2.dp))
            Text("New File", fontSize = 11.sp, color = Indigo400)
          }
        }

        LazyColumn(
          modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
        ) {
          items(files, key = { it.id }) { file ->
            val isSelected = file.path == selectedFile?.path
            val ext = file.path.substringAfterLast(".", "")
            val extColor = when (ext) {
              "tsx", "jsx" -> Cyan400
              "ts", "js" -> Indigo400
              "json" -> Amber400
              "sql" -> Emerald400
              "css" -> Violet500
              else -> Slate400
            }

            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(if (isSelected) Indigo600.copy(alpha = 0.2f) else Color.Transparent)
                .clickable {
                  onSelectFile(file.path)
                  showFileListMobile = false
                }
                .padding(horizontal = 8.dp, vertical = 6.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(3.dp))
                    .background(extColor.copy(alpha = 0.15f))
                    .border(0.5.dp, extColor.copy(alpha = 0.4f), RoundedCornerShape(3.dp))
                    .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                  Text(ext.uppercase(), fontSize = 8.sp, fontWeight = FontWeight.Bold, color = extColor)
                }
                Text(
                  text = file.path,
                  fontSize = 12.sp,
                  color = if (isSelected) Slate100 else Slate300,
                  fontFamily = FontFamily.Monospace,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
              }

              IconButton(
                onClick = { fileToDelete = file.path },
                modifier = Modifier.size(24.dp)
              ) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Slate500, modifier = Modifier.size(13.dp))
              }
            }
          }
        }
      }
    }

    // Monaco-Styled Code Viewport
    Box(
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth()
        .background(Slate950)
    ) {
      Row(
        modifier = Modifier
          .fillMaxSize()
          .verticalScroll(rememberScrollState())
          .horizontalScroll(rememberScrollState())
      ) {
        // Line Numbers Gutter
        val lines = editorText.lines()
        val lineCount = maxOf(lines.size, 1)

        Column(
          modifier = Modifier
            .background(Slate900.copy(alpha = 0.6f))
            .border(width = 1.dp, color = Slate800)
            .padding(vertical = 12.dp, horizontal = 10.dp),
          horizontalAlignment = Alignment.End
        ) {
          for (i in 1..lineCount) {
            Text(
              text = "$i",
              color = Slate600,
              fontSize = 12.sp,
              fontFamily = FontFamily.Monospace,
              lineHeight = 20.sp
            )
          }
        }

        // Code Editor Input Area with basic syntax highlighting
        BasicTextField(
          value = editorText,
          onValueChange = {
            editorText = it
            isDirty = true
          },
          textStyle = TextStyle(
            color = Slate100,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            lineHeight = 20.sp
          ),
          cursorBrush = SolidColor(Cyan400),
          modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
            .testTag("code_editor_text_input")
        )
      }
    }
  }

  // Delete Confirmation Dialog
  fileToDelete?.let { path ->
    AlertDialog(
      onDismissRequest = { fileToDelete = null },
      title = { Text("Delete File", color = Slate100) },
      text = { Text("Are you sure you want to delete '$path'?", color = Slate300) },
      containerColor = Slate900,
      confirmButton = {
        Button(
          onClick = {
            onDeleteFile(path)
            fileToDelete = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = Rose500)
        ) {
          Text("Delete")
        }
      },
      dismissButton = {
        TextButton(onClick = { fileToDelete = null }) {
          Text("Cancel", color = Slate400)
        }
      }
    )
  }

  // New File Dialog
  if (showNewFileDialog) {
    var newFilePath by remember { mutableStateOf("") }
    Dialog(onDismissRequest = { showNewFileDialog = false }) {
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
          Text("Create File in Project", color = Slate100, fontWeight = FontWeight.Bold, fontSize = 15.sp)
          OutlinedTextField(
            value = newFilePath,
            onValueChange = { newFilePath = it },
            placeholder = { Text("e.g., src/services/api.ts") },
            colors = OutlinedTextFieldDefaults.colors(
              focusedTextColor = Slate100,
              unfocusedTextColor = Slate100,
              focusedBorderColor = Indigo500,
              unfocusedBorderColor = Slate700
            ),
            modifier = Modifier.fillMaxWidth().testTag("new_file_path_input")
          )
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
          ) {
            TextButton(onClick = { showNewFileDialog = false }) {
              Text("Cancel", color = Slate400)
            }
            Button(
              onClick = {
                if (newFilePath.isNotBlank()) {
                  onSaveFile(newFilePath, "// File: $newFilePath\n")
                  onSelectFile(newFilePath)
                  showNewFileDialog = false
                }
              },
              enabled = newFilePath.isNotBlank(),
              colors = ButtonDefaults.buttonColors(containerColor = Indigo600)
            ) {
              Text("Create")
            }
          }
        }
      }
    }
  }
}

val Slate200 = Color(0xFFE2E8F0)
