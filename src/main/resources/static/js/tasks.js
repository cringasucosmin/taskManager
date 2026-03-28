// tasks.js — handles the dashboard: task CRUD and filtering

// Redirect to login if not authenticated
const token = localStorage.getItem('token');
const username = localStorage.getItem('username');
if (!token) {
    window.location.href = '/index.html';
}

// Show username in navbar
document.getElementById('nav-username').textContent = username || '';

// State
let currentFilter = 'ALL';
let taskToDeleteId = null;
let editingTaskId = null;

// ===== AUTH HEADER =====
function authHeader() {
    return { 'Authorization': 'Bearer ' + token, 'Content-Type': 'application/json' };
}

// ===== LOGOUT =====
document.getElementById('logout-btn').addEventListener('click', async () => {
    await fetch('/api/auth/logout', { method: 'POST', headers: authHeader() });
    localStorage.removeItem('token');
    localStorage.removeItem('username');
    window.location.href = '/index.html';
});

// ===== LOAD TASKS =====
async function loadTasks() {
    const taskList = document.getElementById('task-list');
    taskList.innerHTML = '<div class="loading">Loading tasks...</div>';

    const url = currentFilter === 'ALL' ? '/api/tasks' : `/api/tasks?status=${currentFilter}`;

    try {
        const response = await fetch(url, { headers: authHeader() });

        if (response.status === 401) {
            window.location.href = '/index.html';
            return;
        }

        const tasks = await response.json();
        renderTasks(tasks);
    } catch (err) {
        taskList.innerHTML = '<div class="empty-state">Failed to load tasks.</div>';
    }
}

// ===== RENDER TASKS =====
function renderTasks(tasks) {
    const taskList = document.getElementById('task-list');

    if (tasks.length === 0) {
        taskList.innerHTML = '<div class="empty-state">No tasks found. Add your first task above!</div>';
        return;
    }

    taskList.innerHTML = tasks.map(task => `
        <div class="task-card" data-status="${task.status}" data-id="${task.id}">
            <div class="task-content">
                <div class="task-title">${escapeHtml(task.title)}</div>
                ${task.description ? `<div class="task-description">${escapeHtml(task.description)}</div>` : ''}
                <div class="task-meta">
                    <span class="task-status-badge badge-${task.status}">${formatStatus(task.status)}</span>
                    <span class="task-date">${formatDate(task.createdAt)}</span>
                </div>
            </div>
            <div class="task-actions">
                ${nextStatus(task.status) ? `<button class="btn btn-success btn-sm status-btn" data-id="${task.id}" data-status="${nextStatus(task.status)}">${nextStatusLabel(task.status)}</button>` : ''}
                <button class="btn btn-outline btn-sm edit-btn" data-id="${task.id}">Edit</button>
                <button class="btn btn-danger btn-sm delete-btn" data-id="${task.id}">Delete</button>
            </div>
        </div>
    `).join('');

    // Attach status change listeners
    document.querySelectorAll('.status-btn').forEach(btn => {
        btn.addEventListener('click', () => changeStatus(parseInt(btn.dataset.id), btn.dataset.status));
    });

    // Attach edit listeners
    document.querySelectorAll('.edit-btn').forEach(btn => {
        btn.addEventListener('click', () => startEditTask(parseInt(btn.dataset.id)));
    });

    // Attach delete listeners
    document.querySelectorAll('.delete-btn').forEach(btn => {
        btn.addEventListener('click', () => openDeleteModal(parseInt(btn.dataset.id)));
    });
}

// ===== CREATE / UPDATE TASK =====
document.getElementById('task-form').addEventListener('submit', async (e) => {
    e.preventDefault();

    const title = document.getElementById('task-title').value.trim();
    const description = document.getElementById('task-description').value.trim();
    const errorDiv = document.getElementById('task-error');
    errorDiv.style.display = 'none';

    if (!title) {
        errorDiv.textContent = 'Title is required';
        errorDiv.style.display = 'block';
        return;
    }

    try {
        let response;
        if (editingTaskId) {
            // Update existing task
            response = await fetch(`/api/tasks/${editingTaskId}`, {
                method: 'PUT',
                headers: authHeader(),
                body: JSON.stringify({ title, description })
            });
        } else {
            // Create new task
            response = await fetch('/api/tasks', {
                method: 'POST',
                headers: authHeader(),
                body: JSON.stringify({ title, description })
            });
        }

        if (response.ok) {
            resetForm();
            loadTasks();
        } else {
            const data = await response.json();
            errorDiv.textContent = data.error || 'Failed to save task';
            errorDiv.style.display = 'block';
        }
    } catch (err) {
        errorDiv.textContent = 'Connection error';
        errorDiv.style.display = 'block';
    }
});

// ===== EDIT TASK =====
async function startEditTask(taskId) {
    // Find task data from the DOM
    const taskCard = document.querySelector(`.task-card[data-id="${taskId}"]`);
    if (!taskCard) return;

    const title = taskCard.querySelector('.task-title').textContent;
    const descEl = taskCard.querySelector('.task-description');
    const description = descEl ? descEl.textContent : '';

    // Populate form
    document.getElementById('task-id').value = taskId;
    document.getElementById('task-title').value = title;
    document.getElementById('task-description').value = description;
    document.getElementById('task-submit-btn').textContent = 'Save Changes';
    document.getElementById('task-cancel-btn').style.display = 'inline-block';

    editingTaskId = taskId;

    // Scroll to form
    document.getElementById('task-form').scrollIntoView({ behavior: 'smooth' });
}

// Cancel edit
document.getElementById('task-cancel-btn').addEventListener('click', resetForm);

function resetForm() {
    document.getElementById('task-form').reset();
    document.getElementById('task-id').value = '';
    document.getElementById('task-submit-btn').textContent = 'Add Task';
    document.getElementById('task-cancel-btn').style.display = 'none';
    document.getElementById('task-error').style.display = 'none';
    editingTaskId = null;
}

// ===== DELETE TASK =====
function openDeleteModal(taskId) {
    taskToDeleteId = taskId;
    document.getElementById('delete-modal').style.display = 'flex';
}

document.getElementById('cancel-delete-btn').addEventListener('click', () => {
    document.getElementById('delete-modal').style.display = 'none';
    taskToDeleteId = null;
});

document.getElementById('confirm-delete-btn').addEventListener('click', async () => {
    if (!taskToDeleteId) return;

    try {
        const response = await fetch(`/api/tasks/${taskToDeleteId}`, {
            method: 'DELETE',
            headers: authHeader()
        });

        document.getElementById('delete-modal').style.display = 'none';
        taskToDeleteId = null;

        if (response.ok || response.status === 204) {
            loadTasks();
        }
    } catch (err) {
        document.getElementById('delete-modal').style.display = 'none';
    }
});

// ===== FILTER =====
document.querySelectorAll('.filter-btn').forEach(btn => {
    btn.addEventListener('click', () => {
        document.querySelectorAll('.filter-btn').forEach(b => b.classList.remove('active'));
        btn.classList.add('active');
        currentFilter = btn.dataset.status;
        loadTasks();
    });
});

// ===== HELPERS =====
function formatStatus(status) {
    const map = { PENDING: 'Pending', IN_PROGRESS: 'In Progress', DONE: 'Done' };
    return map[status] || status;
}

function nextStatus(status) {
    const map = { PENDING: 'IN_PROGRESS', IN_PROGRESS: 'DONE' };
    return map[status] || null;
}

function nextStatusLabel(status) {
    const map = { PENDING: '→ In Progress', IN_PROGRESS: '→ Done' };
    return map[status] || '';
}

async function changeStatus(taskId, newStatus) {
    const taskCard = document.querySelector(`.task-card[data-id="${taskId}"]`);
    const title = taskCard.querySelector('.task-title').textContent;
    const descEl = taskCard.querySelector('.task-description');
    const description = descEl ? descEl.textContent : '';

    await fetch(`/api/tasks/${taskId}`, {
        method: 'PUT',
        headers: authHeader(),
        body: JSON.stringify({ title, description, status: newStatus })
    });
    loadTasks();
}

function formatDate(dateStr) {
    if (!dateStr) return '';
    const date = new Date(dateStr);
    return date.toLocaleDateString('en-GB', { day: '2-digit', month: 'short', year: 'numeric' });
}

function escapeHtml(text) {
    const div = document.createElement('div');
    div.appendChild(document.createTextNode(text));
    return div.innerHTML;
}

// ===== INIT =====
loadTasks();
