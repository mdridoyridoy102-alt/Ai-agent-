package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.ApiEndpointEntity
import com.example.data.model.ChatMessageEntity
import com.example.data.model.CheckpointEntity
import com.example.data.model.ProjectEntity
import com.example.data.model.ProjectFileEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
  entities = [
    ProjectEntity::class,
    ProjectFileEntity::class,
    ChatMessageEntity::class,
    CheckpointEntity::class,
    ApiEndpointEntity::class
  ],
  version = 1,
  exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
  abstract fun projectDao(): ProjectDao
  abstract fun projectFileDao(): ProjectFileDao
  abstract fun chatMessageDao(): ChatMessageDao
  abstract fun checkpointDao(): CheckpointDao
  abstract fun apiEndpointDao(): ApiEndpointDao

  companion object {
    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          AppDatabase::class.java,
          "coding_to_ai_convert.db"
        )
          .addCallback(object : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
              super.onCreate(db)
              CoroutineScope(Dispatchers.IO).launch {
                getDatabase(context).seedDefaultData()
              }
            }
          })
          .build()
        INSTANCE = instance
        instance
      }
    }
  }

  suspend fun seedDefaultData() {
    val project1Id = "proj-collab-task-01"
    val project2Id = "proj-markdown-ai-02"

    val proj1 = ProjectEntity(
      id = project1Id,
      name = "CollabTask Pro",
      description = "Full-stack real-time collaborative task board with REST APIs and SQLite persistence",
      techStack = "React + Node.js + Tailwind",
      status = "STOPPED",
      port = 4100
    )

    val proj2 = ProjectEntity(
      id = project2Id,
      name = "AI Markdown Studio",
      description = "Live Markdown docs editor with AI summaries and Express backend",
      techStack = "React + Express + TypeScript",
      status = "STOPPED",
      port = 4101
    )

    projectDao().insertProject(proj1)
    projectDao().insertProject(proj2)

    // Seed Project 1 Files
    val p1Files = listOf(
      ProjectFileEntity(
        projectId = project1Id,
        path = "package.json",
        content = """{
  "name": "collab-task-pro",
  "version": "1.0.0",
  "scripts": {
    "dev": "vite --port 4100",
    "build": "tsc && vite build",
    "server": "node server.js"
  },
  "dependencies": {
    "react": "^18.3.1",
    "react-dom": "^18.3.1",
    "lucide-react": "^0.380.0",
    "express": "^4.19.2",
    "sqlite3": "^5.1.7"
  }
}"""
      ),
      ProjectFileEntity(
        projectId = project1Id,
        path = "src/App.tsx",
        content = """import React, { useState } from 'react';
import TaskBoard from './components/TaskBoard';

export default function App() {
  const [activeView, setActiveView] = useState<'board' | 'analytics'>('board');

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 font-sans p-6">
      <header className="flex items-center justify-between pb-6 border-b border-slate-800">
        <div>
          <h1 className="text-2xl font-bold bg-gradient-to-r from-indigo-400 to-cyan-400 bg-clip-text text-transparent">
            CollabTask Pro
          </h1>
          <p className="text-xs text-slate-400 mt-0.5">Real-time collaborative sprint workspace</p>
        </div>
        <div className="flex gap-2">
          <button 
            onClick={() => setActiveView('board')}
            className={`px-3 py-1.5 rounded-lg text-xs font-medium transition ${'$'}{activeView === 'board' ? 'bg-indigo-600 text-white' : 'bg-slate-800 text-slate-300'}`}
          >
            Sprint Board
          </button>
          <button 
            onClick={() => setActiveView('analytics')}
            className={`px-3 py-1.5 rounded-lg text-xs font-medium transition ${'$'}{activeView === 'analytics' ? 'bg-indigo-600 text-white' : 'bg-slate-800 text-slate-300'}`}
          >
            Velocity Metrics
          </button>
        </div>
      </header>

      <main className="mt-6">
        <TaskBoard />
      </main>
    </div>
  );
}"""
      ),
      ProjectFileEntity(
        projectId = project1Id,
        path = "src/components/TaskBoard.tsx",
        content = """import React, { useState } from 'react';

interface Task {
  id: string;
  title: string;
  tag: string;
  status: 'TODO' | 'IN_PROGRESS' | 'DONE';
  assignee: string;
}

export default function TaskBoard() {
  const [tasks, setTasks] = useState<Task[]>([
    { id: '1', title: 'Design OAuth2 token rotation flow', tag: 'Security', status: 'IN_PROGRESS', assignee: 'Alex' },
    { id: '2', title: 'Optimize query planner on Postgres index', tag: 'Database', status: 'DONE', assignee: 'Sarah' },
    { id: '3', title: 'Implement live sandbox container lifecycle', tag: 'Core', status: 'TODO', assignee: 'Elena' },
    { id: '4', title: 'Add Monaco editor syntax tree diagnostics', tag: 'Frontend', status: 'TODO', assignee: 'Marcus' }
  ]);

  const [newTitle, setNewTitle] = useState('');

  const addTask = () => {
    if (!newTitle.trim()) return;
    setTasks([...tasks, {
      id: Date.now().toString(),
      title: newTitle,
      tag: 'Feature',
      status: 'TODO',
      assignee: 'Dev'
    }]);
    setNewTitle('');
  };

  const columns: Array<'TODO' | 'IN_PROGRESS' | 'DONE'> = ['TODO', 'IN_PROGRESS', 'DONE'];

  return (
    <div className="space-y-6">
      {/* Quick Add Form */}
      <div className="flex gap-2 bg-slate-900 p-3 rounded-xl border border-slate-800">
        <input
          type="text"
          value={newTitle}
          onChange={(e) => setNewTitle(e.target.value)}
          placeholder="Add task specification (e.g., Setup Redis cache layer)..."
          className="flex-1 bg-slate-950 border border-slate-700/60 rounded-lg px-3 py-2 text-xs text-slate-100 placeholder-slate-500 focus:outline-none focus:border-indigo-500"
        />
        <button
          onClick={addTask}
          className="px-4 py-2 bg-indigo-600 hover:bg-indigo-500 text-xs font-semibold rounded-lg text-white transition"
        >
          Add Task
        </button>
      </div>

      {/* Kanban Columns */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        {columns.map((col) => {
          const colTasks = tasks.filter(t => t.status === col);
          return (
            <div key={col} className="bg-slate-900/70 border border-slate-800/80 rounded-xl p-4">
              <div className="flex items-center justify-between pb-3 border-b border-slate-800">
                <span className="text-xs font-bold uppercase tracking-wider text-slate-400">{col.replace('_', ' ')}</span>
                <span className="px-2 py-0.5 rounded-full text-[10px] bg-slate-800 text-indigo-400 font-mono font-semibold">{colTasks.length}</span>
              </div>
              <div className="mt-3 space-y-3">
                {colTasks.map(t => (
                  <div key={t.id} className="p-3 bg-slate-950 border border-slate-800/80 rounded-lg hover:border-indigo-500/50 transition">
                    <p className="text-xs font-medium text-slate-200">{t.title}</p>
                    <div className="flex items-center justify-between mt-2 pt-2 border-t border-slate-900">
                      <span className="text-[10px] px-1.5 py-0.5 rounded bg-indigo-950/60 text-indigo-400 border border-indigo-900/40">{t.tag}</span>
                      <span className="text-[10px] text-slate-500 font-mono">@{t.assignee}</span>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}"""
      ),
      ProjectFileEntity(
        projectId = project1Id,
        path = "server.js",
        content = """const express = require('express');
const app = express();
const PORT = process.env.PORT || 4100;

app.use(express.json());

// In-memory or SQLite backed tasks
let tasks = [
  { id: '1', title: 'Design OAuth2 token rotation flow', status: 'IN_PROGRESS' },
  { id: '2', title: 'Optimize query planner on Postgres index', status: 'DONE' },
  { id: '3', title: 'Implement live sandbox container lifecycle', status: 'TODO' }
];

app.get('/api/health', (req, res) => {
  res.json({ status: 'ok', service: 'collab-task-api', uptime: process.uptime() });
});

app.get('/api/tasks', (req, res) => {
  res.json({ success: true, count: tasks.length, data: tasks });
});

app.post('/api/tasks', (req, res) => {
  const newTask = { id: Date.now().toString(), ...req.body };
  tasks.push(newTask);
  res.status(201).json({ success: true, task: newTask });
});

app.listen(PORT, () => {
  console.log(`[API Server] Running on http://localhost:${'$'}{PORT}`);
});"""
      ),
      ProjectFileEntity(
        projectId = project1Id,
        path = "schema.sql",
        content = """CREATE TABLE IF NOT EXISTS tasks (
  id TEXT PRIMARY KEY,
  title TEXT NOT NULL,
  tag TEXT DEFAULT 'General',
  status TEXT DEFAULT 'TODO',
  assignee TEXT DEFAULT 'Unassigned',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS users (
  id TEXT PRIMARY KEY,
  name TEXT NOT NULL,
  email TEXT UNIQUE NOT NULL,
  role TEXT DEFAULT 'developer'
);

INSERT INTO users (id, name, email, role) VALUES 
('u1', 'Alex Mercer', 'alex@example.com', 'admin'),
('u2', 'Sarah Connor', 'sarah@example.com', 'developer'),
('u3', 'Elena Rostova', 'elena@example.com', 'architect');"""
      )
    )

    projectFileDao().insertAll(p1Files)

    // Seed Checkpoints
    val checkpoint1 = CheckpointEntity(
      projectId = project1Id,
      summary = "Initial full-stack workspace scaffold with Kanban board and Express API",
      filesStateJson = "{}",
      fileCount = p1Files.size
    )
    checkpointDao().insertCheckpoint(checkpoint1)

    // Seed Initial Chat
    val chat1 = ChatMessageEntity(
      projectId = project1Id,
      role = "assistant",
      content = "Welcome to Coding to AI Convert. I have scaffolded CollabTask Pro with complete React frontend components, Express backend endpoints, and SQLite schema. What feature would you like to build next?",
      taskState = "COMPLETED"
    )
    chatMessageDao().insertMessage(chat1)

    // Seed Endpoints
    apiEndpointDao().insertEndpoint(
      ApiEndpointEntity(
        projectId = project1Id,
        name = "Health Check",
        method = "GET",
        path = "/api/health"
      )
    )
    apiEndpointDao().insertEndpoint(
      ApiEndpointEntity(
        projectId = project1Id,
        name = "List Sprint Tasks",
        method = "GET",
        path = "/api/tasks"
      )
    )
    apiEndpointDao().insertEndpoint(
      ApiEndpointEntity(
        projectId = project1Id,
        name = "Create Task",
        method = "POST",
        path = "/api/tasks",
        bodySchemaJson = "{\n  \"title\": \"Implement caching\",\n  \"status\": \"TODO\"\n}"
      )
    )
  }
}
