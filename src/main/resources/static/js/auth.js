// auth.js — handles login and register on index.html

// If user is already logged in, redirect to dashboard
if (localStorage.getItem('token')) {
    window.location.href = '/dashboard.html';
}

const loginForm = document.getElementById('login-form');
const registerForm = document.getElementById('register-form');
const errorMsg = document.getElementById('error-message');
const successMsg = document.getElementById('success-message');

function showError(message) {
    errorMsg.textContent = message;
    errorMsg.style.display = 'block';
    successMsg.style.display = 'none';
}

function showSuccess(message) {
    successMsg.textContent = message;
    successMsg.style.display = 'block';
    errorMsg.style.display = 'none';
}

function hideMessages() {
    errorMsg.style.display = 'none';
    successMsg.style.display = 'none';
}

// Login
loginForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    hideMessages();

    const username = document.getElementById('login-username').value.trim();
    const password = document.getElementById('login-password').value;

    if (!username || !password) {
        showError('Please fill in all fields');
        return;
    }

    try {
        const response = await fetch('/api/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password })
        });

        const data = await response.json();

        if (response.ok) {
            localStorage.setItem('token', data.token);
            localStorage.setItem('username', username);
            window.location.href = '/dashboard.html';
        } else {
            showError(data.error || 'Login failed');
        }
    } catch (err) {
        showError('Connection error. Is the server running?');
    }
});

// Register
registerForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    hideMessages();

    const username = document.getElementById('register-username').value.trim();
    const password = document.getElementById('register-password').value;

    if (!username || !password) {
        showError('Please fill in all fields');
        return;
    }

    try {
        const response = await fetch('/api/auth/register', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password })
        });

        const data = await response.json();

        if (response.ok) {
            showSuccess('Account created! You can now log in.');
            document.getElementById('register-username').value = '';
            document.getElementById('register-password').value = '';
        } else {
            showError(data.error || 'Registration failed');
        }
    } catch (err) {
        showError('Connection error. Is the server running?');
    }
});
