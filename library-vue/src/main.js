import { createApp } from 'vue';
import App from './App.vue';
import './assets/main.css';
import router from "./router";

// Global fetch interceptor to attach JWT token and handle 401s
const originalFetch = window.fetch;
window.fetch = function(url, options = {}) {
    if (typeof url === 'string' && url.startsWith('/api')) {
        const token = localStorage.getItem('token');
        if (token) {
            options.headers = {
                ...options.headers,
                'Authorization': `Bearer ${token}`
            };
        }
    }
    return originalFetch.call(this, url, options).then(response => {
        if (response.status === 401 && typeof url === 'string' && url.startsWith('/api') && !url.includes('/auth/')) {
            localStorage.removeItem('token');
            localStorage.removeItem('user');
            window.location.hash = '#/login';
        }
        return response;
    });
};

const app = createApp(App);
app.use(router);
app.mount('#app');