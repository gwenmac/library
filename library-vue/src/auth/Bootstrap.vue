<template>
  <div class="login-page">
    <div class="login-card">
      <h1>📚 Library Setup</h1>
      <p class="subtitle">Create your admin account to get started.</p>
      <form @submit.prevent="bootstrap">
        <div class="field">
          <label>Display Name</label>
          <input v-model="displayName" type="text" required autofocus />
        </div>
        <div class="field">
          <label>Email</label>
          <input v-model="email" type="email" required />
        </div>
        <div class="field">
          <label>Password</label>
          <input v-model="password" type="password" required minlength="8" />
        </div>
        <p v-if="error" class="error">{{ error }}</p>
        <button type="submit" :disabled="loading">{{ loading ? 'Creating...' : 'Create Admin Account' }}</button>
      </form>
    </div>
  </div>
</template>

<script>
import { setAuth } from './api.js'

export default {
  data() {
    return {
      displayName: '',
      email: '',
      password: '',
      error: null,
      loading: false
    }
  },
  methods: {
    async bootstrap() {
      this.error = null
      this.loading = true
      try {
        const res = await fetch('/api/auth/bootstrap', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            email: this.email,
            password: this.password,
            displayName: this.displayName
          })
        })
        if (res.status === 403) {
          this.error = 'An admin account already exists. Please log in.'
          return
        }
        if (!res.ok) {
          this.error = 'Setup failed (status ' + res.status + ')'
          return
        }
        const data = await res.json()
        setAuth(data.token, data.user)
        this.$router.push('/')
      } catch (err) {
        this.error = 'Setup failed: ' + err.message
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
  margin-bottom: 8px;
}

.subtitle {
  text-align: center;
  color: #666;
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
</style>
