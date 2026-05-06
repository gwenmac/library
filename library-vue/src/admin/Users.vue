<template>
  <div class="admin-users">
    <div class="list-header">
      <h2>Manage Users</h2>
      <button class="add-btn" @click="showForm = !showForm">{{ showForm ? 'Cancel' : '+ Invite User' }}</button>
    </div>

    <div v-if="showForm" class="invite-form">
      <form @submit.prevent="createUser">
        <div class="form-row">
          <input v-model="form.displayName" placeholder="Display Name" required />
          <input v-model="form.email" type="email" placeholder="Email" required />
          <input v-model="form.password" type="password" placeholder="Temporary Password" required minlength="8" />
          <button type="submit">Create</button>
        </div>
        <p v-if="formError" class="error">{{ formError }}</p>
      </form>
    </div>

    <p v-if="error" class="error">{{ error }}</p>
    <p v-else-if="loading">Loading...</p>

    <table v-else-if="users.length">
      <thead>
        <tr>
          <th>Name</th>
          <th>Email</th>
          <th>Role</th>
          <th>Created</th>
          <th>Actions</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="u in users" :key="u.id">
          <td>{{ u.displayName }}</td>
          <td>{{ u.email }}</td>
          <td><span class="role-badge" :class="u.role">{{ u.role }}</span></td>
          <td>{{ formatDate(u.createdAt) }}</td>
          <td>
            <button
              v-if="u.role !== 'admin'"
              class="delete-btn"
              @click="deleteUser(u)"
            >Delete</button>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script>
import { api } from '../auth/api.js'

export default {
  data() {
    return {
      users: [],
      loading: true,
      error: null,
      showForm: false,
      form: { displayName: '', email: '', password: '' },
      formError: null
    }
  },
  methods: {
    formatDate(dt) {
      if (!dt) return '—'
      return new Date(dt).toLocaleDateString()
    },
    async loadUsers() {
      try {
        this.users = await api('/admin/users')
      } catch (err) {
        this.error = 'Failed to load users: ' + err.message
      } finally {
        this.loading = false
      }
    },
    async createUser() {
      this.formError = null
      try {
        await api('/admin/users', {
          method: 'POST',
          body: JSON.stringify(this.form)
        })
        this.form = { displayName: '', email: '', password: '' }
        this.showForm = false
        await this.loadUsers()
      } catch (err) {
        this.formError = 'Failed to create user: ' + err.message
      }
    },
    async deleteUser(u) {
      if (!confirm(`Delete user "${u.displayName}"?`)) return
      try {
        await api('/admin/users/' + u.id, { method: 'DELETE' })
        this.users = this.users.filter(x => x.id !== u.id)
      } catch (err) {
        this.error = 'Failed to delete user: ' + err.message
      }
    }
  },
  mounted() {
    this.loadUsers()
  }
}
</script>

<style scoped>
.admin-users {
  padding: 16px;
}

.list-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.list-header h2 {
  margin: 0;
}

.add-btn {
  padding: 8px 16px;
  background-color: #42b983;
  color: #fff;
  border: none;
  border-radius: 6px;
  font-weight: 600;
  font-size: 0.95rem;
  cursor: pointer;
}

.add-btn:hover {
  background-color: #369e6f;
}

.invite-form {
  margin-bottom: 16px;
  padding: 16px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
}

.form-row {
  display: flex;
  gap: 8px;
  align-items: center;
  flex-wrap: wrap;
}

.form-row input {
  padding: 8px 12px;
  border: 1px solid #ccc;
  border-radius: 6px;
  font-size: 0.95rem;
  flex: 1;
  min-width: 140px;
}

.form-row button {
  padding: 8px 16px;
  background-color: #42b983;
  color: #fff;
  border: none;
  border-radius: 6px;
  font-weight: 600;
  cursor: pointer;
}

.error {
  color: #e74c3c;
  font-weight: 600;
  font-size: 0.9rem;
  margin-top: 8px;
}

.role-badge {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 0.8rem;
  font-weight: 600;
  text-transform: uppercase;
}

.role-badge.admin {
  background: #f0e6ff;
  color: #7c3aed;
}

.role-badge.user {
  background: #e6f9f0;
  color: #059669;
}

.delete-btn {
  padding: 4px 12px;
  background-color: #e74c3c;
  color: #fff;
  border: none;
  border-radius: 4px;
  font-size: 0.85rem;
  font-weight: 600;
  cursor: pointer;
}

.delete-btn:hover {
  background-color: #c0392b;
}
</style>
