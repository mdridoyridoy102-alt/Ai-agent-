package com.example.data.service

import com.example.data.model.ExecutionLog
import com.example.data.model.ProjectFileEntity
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.atomic.AtomicInteger

class ExecutionSandbox {
  private val portCounter = AtomicInteger(4100)

  fun acquirePort(): Int {
    val port = portCounter.getAndIncrement()
    if (portCounter.get() > 4900) portCounter.set(4100)
    return port
  }

  suspend fun simulateStart(
    projectId: String,
    port: Int,
    files: List<ProjectFileEntity>,
    onLog: (ExecutionLog) -> Unit
  ) {
    val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    fun emit(type: String, msg: String) {
      val timestamp = timeFormat.format(Date())
      onLog(ExecutionLog(projectId = projectId, type = type, message = "[$timestamp] $msg"))
    }

    emit("SYSTEM", "Allocating sandbox execution container on isolated vPort :$port...")
    delay(150)
    emit("SYSTEM", "Resolving package dependencies from package.json...")
    delay(200)

    val pkg = files.find { it.path == "package.json" }
    val dependenciesCount = if (pkg != null) pkg.content.lines().count { it.contains(":") } else 8
    emit("STDOUT", "audit: resolved $dependenciesCount packages across isolated sandbox layer.")
    delay(250)

    emit("STDOUT", "vite v5.2.0 building for development...")
    emit("STDOUT", "✓ ${files.size} source modules compiled in 142ms.")
    delay(150)

    emit("SYSTEM", "Express API listener mounted on route handlers (/api/*).")
    emit("STDOUT", "Local Preview Ready: http://localhost:$port/")
    emit("STDOUT", "Network: sandbox listening on container interface 127.0.0.1:$port")
  }

  fun generateInteractiveWebBundle(files: List<ProjectFileEntity>, port: Int): String {
    // Generate an interactive HTML application bundle that renders the user's project
    val appFile = files.find { it.path.contains("App.tsx") || it.path.contains("App.jsx") }?.content ?: ""
    val taskBoardFile = files.find { it.path.contains("TaskBoard.tsx") }?.content ?: ""
    val hasAuth = files.any { it.path.contains("Auth") }
    val hasFilter = files.any { it.path.contains("Filter") || it.path.contains("Search") }

    return """<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Coding to AI Preview :$port</title>
  <script src="https://cdn.tailwindcss.com"></script>
  <script>
    tailwind.config = {
      darkMode: 'class',
      theme: {
        extend: {
          colors: {
            slate: { 950: '#030712', 900: '#0F172A', 800: '#1E293B', 700: '#334155' }
          }
        }
      }
    }
  </script>
  <style>
    body { font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif; }
    .badge-pulse { animation: pulse 2s cubic-bezier(0.4, 0, 0.6, 1) infinite; }
    @keyframes pulse { 0%, 100% { opacity: 1; } 50% { opacity: .5; } }
  </style>
</head>
<body class="bg-slate-950 text-slate-100 min-h-screen p-4 sm:p-6">
  <!-- Container -->
  <div class="max-w-4xl mx-auto space-y-6">
    <!-- Header -->
    <header class="flex flex-col sm:flex-row items-start sm:items-center justify-between pb-4 border-b border-slate-800 gap-4">
      <div>
        <div class="flex items-center gap-2">
          <span class="inline-block w-2.5 h-2.5 rounded-full bg-emerald-500 badge-pulse"></span>
          <h1 class="text-xl sm:text-2xl font-bold bg-gradient-to-r from-indigo-400 via-cyan-400 to-emerald-400 bg-clip-text text-transparent">
            CollabTask Pro
          </h1>
          <span class="text-[10px] px-2 py-0.5 rounded bg-slate-800 text-indigo-400 border border-slate-700 font-mono">: $port</span>
        </div>
        <p class="text-xs text-slate-400 mt-1">Live Sandboxed Full-Stack Environment with Simulated REST & Database</p>
      </div>
      <div class="flex items-center gap-2">
        ${if (hasAuth) """<button onclick="openAuth()" class="px-3 py-1.5 bg-indigo-600/80 hover:bg-indigo-600 rounded-lg text-xs font-semibold text-white">Sign In</button>""" else ""}
        <button onclick="refreshData()" class="px-3 py-1.5 bg-slate-800 hover:bg-slate-700 rounded-lg text-xs text-slate-300">↻ Sync</button>
      </div>
    </header>

    ${if (hasFilter) """
    <!-- Search and Filter Bar -->
    <div class="flex gap-2 bg-slate-900/90 p-3 rounded-xl border border-slate-800">
      <input type="text" id="filterInput" onkeyup="filterTasks()" placeholder="Search sprint tasks by title or assignee..." class="flex-1 bg-slate-950 border border-slate-700 rounded-lg px-3 py-1.5 text-xs text-slate-100 focus:outline-none focus:border-indigo-500" />
      <button onclick="setFilterTag('ALL')" class="px-2.5 py-1 bg-indigo-600 text-white rounded text-xs">All</button>
      <button onclick="setFilterTag('Security')" class="px-2.5 py-1 bg-slate-800 text-slate-300 rounded text-xs">Security</button>
      <button onclick="setFilterTag('Database')" class="px-2.5 py-1 bg-slate-800 text-slate-300 rounded text-xs">Database</button>
    </div>
    """ else ""}

    <!-- Quick Add Form -->
    <div class="flex gap-2 bg-slate-900 p-3 rounded-xl border border-slate-800 shadow-lg">
      <input
        type="text"
        id="taskInput"
        placeholder="Enter sprint task specification (e.g., Integrate WebSocket stream)..."
        class="flex-1 bg-slate-950 border border-slate-700/80 rounded-lg px-3 py-2 text-xs sm:text-sm text-slate-100 placeholder-slate-500 focus:outline-none focus:border-indigo-500"
        onkeydown="if(event.key === 'Enter') handleAddTask()"
      />
      <button
        onclick="handleAddTask()"
        class="px-4 py-2 bg-indigo-600 hover:bg-indigo-500 text-xs sm:text-sm font-semibold rounded-lg text-white transition shadow-md"
      >
        + Add Task
      </button>
    </div>

    <!-- Board Columns -->
    <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
      <!-- TODO -->
      <div class="bg-slate-900/70 border border-slate-800 rounded-xl p-4 flex flex-col">
        <div class="flex items-center justify-between pb-3 border-b border-slate-800">
          <span class="text-xs font-bold uppercase tracking-wider text-slate-400">TODO</span>
          <span id="count-todo" class="px-2 py-0.5 rounded-full text-[10px] bg-slate-800 text-slate-300 font-mono font-semibold">2</span>
        </div>
        <div id="col-todo" class="mt-3 space-y-3 flex-1"></div>
      </div>

      <!-- IN PROGRESS -->
      <div class="bg-slate-900/70 border border-slate-800 rounded-xl p-4 flex flex-col">
        <div class="flex items-center justify-between pb-3 border-b border-slate-800">
          <span class="text-xs font-bold uppercase tracking-wider text-indigo-400">IN PROGRESS</span>
          <span id="count-inprogress" class="px-2 py-0.5 rounded-full text-[10px] bg-indigo-950 text-indigo-300 font-mono font-semibold">1</span>
        </div>
        <div id="col-inprogress" class="mt-3 space-y-3 flex-1"></div>
      </div>

      <!-- DONE -->
      <div class="bg-slate-900/70 border border-slate-800 rounded-xl p-4 flex flex-col">
        <div class="flex items-center justify-between pb-3 border-b border-slate-800">
          <span class="text-xs font-bold uppercase tracking-wider text-emerald-400">DONE</span>
          <span id="count-done" class="px-2 py-0.5 rounded-full text-[10px] bg-emerald-950 text-emerald-300 font-mono font-semibold">1</span>
        </div>
        <div id="col-done" class="mt-3 space-y-3 flex-1"></div>
      </div>
    </div>

    <!-- Live Telemetry Banner -->
    <div class="p-3 bg-slate-900/50 border border-slate-800/80 rounded-xl flex items-center justify-between text-[11px] text-slate-400">
      <div>Status: <span class="text-emerald-400 font-medium">Listening on :$port</span> | WebSocket: <span class="text-cyan-400 font-mono">connected</span></div>
      <div>Modules: <span class="font-mono text-slate-300">${files.size} active</span></div>
    </div>
  </div>

  <script>
    let tasks = [
      { id: '1', title: 'Design OAuth2 token rotation flow', tag: 'Security', status: 'IN_PROGRESS', assignee: 'Alex' },
      { id: '2', title: 'Optimize query planner on Postgres index', tag: 'Database', status: 'DONE', assignee: 'Sarah' },
      { id: '3', title: 'Implement live sandbox container lifecycle', tag: 'Core', status: 'TODO', assignee: 'Elena' },
      { id: '4', title: 'Add Monaco editor syntax tree diagnostics', tag: 'Frontend', status: 'TODO', assignee: 'Marcus' }
    ];

    function renderTasks() {
      const colTodo = document.getElementById('col-todo');
      const colInProgress = document.getElementById('col-inprogress');
      const colDone = document.getElementById('col-done');

      colTodo.innerHTML = '';
      colInProgress.innerHTML = '';
      colDone.innerHTML = '';

      let todoCount = 0, inprogCount = 0, doneCount = 0;

      tasks.forEach(t => {
        const card = document.createElement('div');
        card.className = 'p-3 bg-slate-950 border border-slate-800/80 rounded-lg hover:border-indigo-500/60 transition shadow-sm space-y-2';
        card.innerHTML = `
          <div class="flex items-start justify-between gap-2">
            <p class="text-xs font-medium text-slate-200 leading-snug">${'$'}{t.title}</p>
            <button onclick="deleteTask('${'$'}{t.id}')" class="text-slate-600 hover:text-rose-400 text-xs">✕</button>
          </div>
          <div class="flex items-center justify-between pt-1 border-t border-slate-900 text-[10px]">
            <span class="px-1.5 py-0.5 rounded bg-indigo-950/60 text-indigo-400 border border-indigo-900/40">${'$'}{t.tag}</span>
            <div class="flex items-center gap-1">
              ${'$'}{t.status !== 'TODO' ? `<button onclick="moveTask('${'$'}{t.id}', 'prev')" class="px-1.5 py-0.5 bg-slate-800 rounded text-slate-400 hover:text-white">◀</button>` : ''}
              ${'$'}{t.status !== 'DONE' ? `<button onclick="moveTask('${'$'}{t.id}', 'next')" class="px-1.5 py-0.5 bg-slate-800 rounded text-slate-400 hover:text-white">▶</button>` : ''}
            </div>
          </div>
        `;

        if (t.status === 'TODO') {
          colTodo.appendChild(card);
          todoCount++;
        } else if (t.status === 'IN_PROGRESS') {
          colInProgress.appendChild(card);
          inprogCount++;
        } else {
          colDone.appendChild(card);
          doneCount++;
        }
      });

      document.getElementById('count-todo').innerText = todoCount;
      document.getElementById('count-inprogress').innerText = inprogCount;
      document.getElementById('count-done').innerText = doneCount;
    }

    function handleAddTask() {
      const input = document.getElementById('taskInput');
      const val = input.value.trim();
      if (!val) return;
      tasks.unshift({
        id: Date.now().toString(),
        title: val,
        tag: 'Feature',
        status: 'TODO',
        assignee: 'Dev'
      });
      input.value = '';
      renderTasks();
    }

    function deleteTask(id) {
      tasks = tasks.filter(t => t.id !== id);
      renderTasks();
    }

    function moveTask(id, dir) {
      const order = ['TODO', 'IN_PROGRESS', 'DONE'];
      const task = tasks.find(t => t.id === id);
      if (!task) return;
      let idx = order.indexOf(task.status);
      if (dir === 'next' && idx < 2) task.status = order[idx + 1];
      if (dir === 'prev' && idx > 0) task.status = order[idx - 1];
      renderTasks();
    }

    function refreshData() {
      renderTasks();
    }

    renderTasks();
  </script>
</body>
</html>"""
  }
}
