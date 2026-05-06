<template>
  <div class="login-page">
    <div class="login-card">
      <h1>📚 Library</h1>
      <form @submit.prevent="login">
        <div class="field">
          <label>Email</label>
          <input v-model="email" type="email" required autofocus />
        </div>
        <div class="field">
          <label>Password</label>
          <input v-model="password" type="password" required />
        </div>
        <p v-if="error" class="error">{{ error }}</p>
        <button type="submit" :disabled="loading">{{ loading ? 'Logging in...' : 'Log In' }}</button>
      </form>
      <p class="bootstrap-link" v-if="noAdmin">
        No admin account exists. <router-link to="/bootstrap">Set up your account →</router-link>
      </p>
    </div>
  </div>
</template>

<script>
import { setAuth } from './api.js'

export default {
  data() {
    return {
      email: '',
      password: '',
      error: null,
      loading: false,
      noAdmin: false
    }
  },
  async mounted() {
    // Check if bootstrap is needed
    try {
      const res = await fetch('/api/auth/bootstrap', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email: '', password: '', displayName: '' })
      })
      // If we don't get 403, it means no admin exists
      if (res.status !== 403) {
        this.noAdmin = true
      }
    } catch {
      // Ignore — just show login form
    }
  },
  methods: {
    async login() {
      this.error = null
      this.loading = true
      try {
        const res = await fetch('/api/auth/login', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ email: this.email, password: this.password })
        })
        if (!res.ok) {
          this.error = 'Invalid email or password'
          return
        }
        const data = await res.json()
        setAuth(data.token, data.user)
        this.$router.push('/')
      } catch (err) {
        this.error = 'Login failed: ' + err.message
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style scoped>
.login-page {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 80vh;
}

.login-card {
  width: 100%;
  max-width: 380px;
  padding: 32px;
  border: 1px solid #e0e0e0;
  border-radius: 12px;
  background: #fff;
}

.login-card h1 {
  text-align: center;
  margin-bottom: 24px;
}

.field {
  margin-bottom: 16px;
}

.field label {
  display: block;
  margin-bottom: 4px;
  font-weight: 600;
  font-size: 0.9rem;
}

.field input {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #ccc;
  border-radius: 6px;
  font-size: 1rem;
  box-sizing: border-box;
}

button {
  width: 100%;
  padding: 12px;
  background-color: #42b983;
  color: #fff;
  border: none;
  border-radius: 6px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
}

button:hover:not(:disabled) {
  background-color: #369e6f;
}

button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.error {
  color: #e74c3c;
  font-weight: 600;
  font-size: 0.9rem;
  margin-bottom: 12px;
}

.bootstrap-link {
  margin-top: 16px;
  text-align: center;
  font-size: 0.9rem;
}

.bootstrap-link a {
  color: #42b983;
  font-weight: 600;
}
</style>
