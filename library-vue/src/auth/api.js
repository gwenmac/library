const API_BASE = '/api';

function getToken() {
    return localStorage.getItem('token');
}

function isTokenExpired(token) {
    try {
        const payload = JSON.parse(atob(token.split('.')[1]));
        // expired if less than 30 seconds remaining
        return payload.exp * 1000 < Date.now() + 30000;
    } catch {
        return true;
    }
}

export function getUser() {
    const raw = localStorage.getItem('user');
    return raw ? JSON.parse(raw) : null;
}

export function setAuth(token, user) {
    localStorage.setItem('token', token);
    localStorage.setItem('user', JSON.stringify(user));
}

export function clearAuth() {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
}

export function isLoggedIn() {
    const token = getToken();
    if (!token) return false;
    if (isTokenExpired(token)) {
        clearAuth();
        return false;
    }
    return true;
}

export async function api(path, options = {}) {
    const token = getToken();
    const headers = { 'Content-Type': 'application/json', ...options.headers };
    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }

    const response = await fetch(`${API_BASE}${path}`, { ...options, headers });

    if (response.status === 401) {
        clearAuth();
        window.location.hash = '#/login';
        throw new Error('Unauthorized');
    }

    if (!response.ok) {
        throw new Error(`API error: ${response.status}`);
    }

    if (response.status === 204) return null;
    return response.json();
}
