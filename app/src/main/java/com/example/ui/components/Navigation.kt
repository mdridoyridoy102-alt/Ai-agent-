package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Http
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Web
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.ProjectEntity
import com.example.data.model.ProjectStatus
import com.example.ui.theme.Amber400
import com.example.ui.theme.Cyan400
import com.example.ui.theme.Emerald400
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
import com.example.ui.viewmodel.WorkspaceTab

@Composable
fun TopNavigation(
  activeProject: ProjectEntity?,
  allProjects: List<ProjectEntity>,
  activeTab: WorkspaceTab,
  onSelectTab: (WorkspaceTab) -> Unit,
  onSelectProject: (String) -> Unit,
  onToggleRun: () -> Unit,
  onCreateNewProject: (name: String, desc: String, stack: String) -> Unit,
  modifier: Modifier = Modifier
) {
  var showProjectMenu by remember { mutableStateOf(false) }
  var showNewProjectDialog by remember { mutableStateOf(false) }

  val isRunning = activeProject?.status == ProjectStatus.RUNNING.name
  val assignedPort = activeProject?.port ?: 4100

  Column(
    modifier = modifier
      .fillMaxWidth()
      .background(Slate950)
      .border(1.dp, Slate800)
  ) {
    // Top Row: Brand + Project Dropdown + Run/Stop button
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 10.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Left: Logo & Project Selector
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        // Glowing Icon Badge
        Box(
          modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(
              Brush.linearGradient(listOf(Indigo600, Cyan400))
            ),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Code,
            contentDescription = "Platform Logo",
            tint = Color.White,
            modifier = Modifier.size(20.dp)
          )
        }

        // Project Dropdown Button
        Box {
          Row(
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(Slate900)
              .border(1.dp, Slate800, RoundedCornerShape(8.dp))
              .clickable { showProjectMenu = true }
              .padding(horizontal = 10.dp, vertical = 6.dp)
              .testTag("project_selector_dropdown"),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Column {
              Text(
                text = activeProject?.name ?: "Select Project",
                color = Slate100,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = activeProject?.techStack ?: "React + Node",
                color = Slate400,
                fontSize = 10.sp
              )
            }
            Icon(
              imageVector = Icons.Default.ExpandMore,
              contentDescription = "Expand projects",
              tint = Slate400,
              modifier = Modifier.size(16.dp)
            )
          }

          DropdownMenu(
            expanded = showProjectMenu,
            onDismissRequest = { showProjectMenu = false },
            modifier = Modifier
              .background(Slate900)
              .border(1.dp, Slate800)
          ) {
            allProjects.forEach { proj ->
              DropdownMenuItem(
                text = {
                  Column {
                    Text(
                      text = proj.name,
                      color = if (proj.id == activeProject?.id) Cyan400 else Slate100,
                      fontWeight = FontWeight.SemiBold,
                      fontSize = 13.sp
                    )
                    Text(
                      text = "${proj.techStack} • :${proj.port ?: 4100}",
                      color = Slate500,
                      fontSize = 11.sp
                    )
                  }
                },
                onClick = {
                  onSelectProject(proj.id)
                  showProjectMenu = false
                }
              )
            }

            DropdownMenuItem(
              text = {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                  Icon(Icons.Default.Add, contentDescription = null, tint = Indigo500, modifier = Modifier.size(16.dp))
                  Text("+ Create New Project", color = Indigo500, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
              },
              onClick = {
                showProjectMenu = false
                showNewProjectDialog = true
              }
            )
          }
        }
      }

      // Right: Run / Stop Sandbox Button
      Button(
        onClick = onToggleRun,
        colors = ButtonDefaults.buttonColors(
          containerColor = if (isRunning) Rose500.copy(alpha = 0.2f) else Emerald400.copy(alpha = 0.2f),
          contentColor = if (isRunning) Rose500 else Emerald400
        ),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
          .border(
            1.dp,
            if (isRunning) Rose500.copy(alpha = 0.6f) else Emerald400.copy(alpha = 0.6f),
            RoundedCornerShape(8.dp)
          )
          .testTag("run_stop_project_button")
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          if (isRunning) {
            Box(
              modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(Emerald400)
            )
            Icon(Icons.Default.Stop, contentDescription = "Stop", modifier = Modifier.size(16.dp))
            Text("Stop :$assignedPort", fontSize = 12.sp, fontWeight = FontWeight.Bold)
          } else {
            Icon(Icons.Default.PlayArrow, contentDescription = "Run", modifier = Modifier.size(16.dp))
            Text("Run Project", fontSize = 12.sp, fontWeight = FontWeight.Bold)
          }
        }
      }
    }

    // Bottom Row: Scrollable Workspace Tab Pills
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState())
        .padding(horizontal = 8.dp, vertical = 6.dp),
      horizontalArrangement = Arrangement.spacedBy(4.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      WorkspaceTab.values().forEach { tab ->
        val isSelected = activeTab == tab
        val tabIcon: ImageVector = when (tab) {
          WorkspaceTab.CHAT -> Icons.Default.SmartToy
          WorkspaceTab.EDITOR -> Icons.Default.Code
          WorkspaceTab.PREVIEW -> Icons.Default.Web
          WorkspaceTab.APIS -> Icons.Default.Http
          WorkspaceTab.DATABASE -> Icons.Default.Storage
          WorkspaceTab.LOGS -> Icons.Default.Terminal
          WorkspaceTab.SNAPSHOTS -> Icons.Default.History
          WorkspaceTab.SETTINGS -> Icons.Default.Settings
        }

        Row(
          modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (isSelected) Indigo600.copy(alpha = 0.25f) else Color.Transparent)
            .border(
              width = 1.dp,
              color = if (isSelected) Indigo500 else Color.Transparent,
              shape = RoundedCornerShape(6.dp)
            )
            .clickable { onSelectTab(tab) }
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .testTag("tab_${tab.name.lowercase()}"),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Icon(
            imageVector = tabIcon,
            contentDescription = tab.label,
            tint = if (isSelected) Cyan400 else Slate400,
            modifier = Modifier.size(15.dp)
          )
          Text(
            text = tab.label,
            color = if (isSelected) Slate100 else Slate400,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
          )
        }
      }
    }
  }

  // Create Project Dialog
  if (showNewProjectDialog) {
    NewProjectDialog(
      onDismiss = { showNewProjectDialog = false },
      onCreate = { name, desc, stack ->
        onCreateNewProject(name, desc, stack)
        showNewProjectDialog = false
      }
    )
  }
}

@Composable
fun NewProjectDialog(
  onDismiss: () -> Unit,
  onCreate: (String, String, String) -> Unit
) {
  var name by remember { mutableStateOf("") }
  var desc by remember { mutableStateOf("") }
  var stack by remember { mutableStateOf("React + Node.js") }

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(16.dp),
      color = Slate900,
      border = androidx.compose.foundation.BorderStroke(1.dp, Slate800),
      modifier = Modifier.padding(16.dp)
    ) {
      Column(
        modifier = Modifier.padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        Text("Create New Project", color = Slate100, fontSize = 18.sp, fontWeight = FontWeight.Bold)

        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text("Project Name") },
          placeholder = { Text("e.g. AI Workflow Engine") },
          colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Slate100,
            unfocusedTextColor = Slate100,
            focusedBorderColor = Indigo500,
            unfocusedBorderColor = Slate700
          ),
          modifier = Modifier.fillMaxWidth().testTag("new_project_name_input")
        )

        OutlinedTextField(
          value = desc,
          onValueChange = { desc = it },
          label = { Text("Short Description") },
          placeholder = { Text("e.g. Autonomous agent execution graph") },
          colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Slate100,
            unfocusedTextColor = Slate100,
            focusedBorderColor = Indigo500,
            unfocusedBorderColor = Slate700
          ),
          modifier = Modifier.fillMaxWidth().testTag("new_project_desc_input")
        )

        Column {
          Text("Tech Stack", color = Slate400, fontSize = 12.sp)
          Row(
            modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            listOf("React + Node", "Next.js", "Express + SQL").forEach { s ->
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(6.dp))
                  .background(if (stack == s) Indigo600.copy(alpha = 0.3f) else Slate800)
                  .border(1.dp, if (stack == s) Indigo500 else Slate700, RoundedCornerShape(6.dp))
                  .clickable { stack = s }
                  .padding(horizontal = 8.dp, vertical = 5.dp)
              ) {
                Text(s, fontSize = 11.sp, color = if (stack == s) Cyan400 else Slate300)
              }
            }
          }
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.End
        ) {
          TextButton(onClick = onDismiss) {
            Text("Cancel", color = Slate400)
          }
          Button(
            onClick = {
              if (name.isNotBlank()) {
                onCreate(name, desc, stack)
              }
            },
            enabled = name.isNotBlank(),
            colors = ButtonDefaults.buttonColors(containerColor = Indigo600),
            modifier = Modifier.testTag("submit_create_project_button")
          ) {
            Text("Create Project", color = Color.White)
          }
        }
      }
    }
  }
}
