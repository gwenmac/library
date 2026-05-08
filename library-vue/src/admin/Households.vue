<template>
  <div class="admin-households">
    <div class="list-header">
      <h2>Manage Households</h2>
      <button class="add-btn" @click="showForm = !showForm">{{ showForm ? 'Cancel' : '+ New Household' }}</button>
    </div>

    <div v-if="showForm" class="create-form">
      <form @submit.prevent="createHousehold">
        <div class="form-row">
          <input v-model="form.name" placeholder="Household name" required />
          <button type="submit">Create</button>
        </div>
        <p v-if="formError" class="error">{{ formError }}</p>
      </form>
    </div>

    <p v-if="error" class="error">{{ error }}</p>
    <p v-else-if="loading">Loading...</p>

    <table v-else-if="households.length">
      <thead>
        <tr>
          <th>Name</th>
          <th>Members</th>
          <th>Created</th>
          <th>Actions</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="h in households" :key="h.id">
          <td>
            <span v-if="editingId !== h.id">{{ h.name }}</span>
            <input
              v-else
              v-model="editName"
              @keyup.enter="saveEdit(h)"
              @keyup.escape="cancelEdit"
              class="edit-input"
            />
          </td>
          <td>{{ countUsers(h.id) }}</td>
          <td>{{ formatDate(h.createdAt) }}</td>
          <td>
            <button v-if="editingId !== h.id" class="edit-btn" @click="startEdit(h)">Rename</button>
            <template v-else>
              <button class="save-btn" @click="saveEdit(h)">Save</button>
              <button class="cancel-btn" @click="cancelEdit">Cancel</button>
            </template>
          </td>
        </tr>
      </tbody>
    </table>
    <p v-else>No households yet.</p>
  </div>
</template>

<script>
import { api } from '../auth/api.js'

export default {
  data() {
    return {
      households: [],
      users: [],
      loading: true,
      error: null,
      showForm: false,
      form: { name: '' },
      formError: null,
      editingId: null,
      editName: ''
    }
  },
  methods: {
    formatDate(dt) {
      if (!dt) return '—'
      return new Date(dt).toLocaleDateString()
    },
    countUsers(id) {
      return this.users.filter(u => {return u.household.id === id}).length
    },
    async loadData() {
      try {
        const [users, households] = await Promise.all([
          api('/admin/users'),
          api('/admin/households')
        ])
        this.users = users
        this.households = households
      } catch (err) {
        this.error = 'Failed to load data: ' + err.message
      } finally {
        this.loading = false
      }
    },
    async createHousehold() {
      this.formError = null
      try {
        await api('/admin/households', {
          method: 'POST',
          body: JSON.stringify(this.form)
        })
        this.form = { name: '' }
        this.showForm = false
        await this.loadData()
      } catch (err) {
        this.formError = 'Failed to create household: ' + err.message
      }
    },
    startEdit(h) {
      this.editingId = h.id
      this.editName = h.name
    },
    cancelEdit() {
      this.editingId = null
      this.editName = ''
    },
    async saveEdit(h) {
      try {
        await api('/admin/households/' + h.id, {
          method: 'PUT',
          body: JSON.stringify({ name: this.editName })
        })
        h.name = this.editName
        this.cancelEdit()
      } catch (err) {
        this.error = 'Failed to rename household: ' + err.message
      }
    }
  },
  mounted() {
    this.loadData()
  }
}
</script>

<style scoped>
.admin-households {
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

.create-form {
  margin-bottom: 16px;
  padding: 16px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
}

.form-row {
  display: flex;
  gap: 8px;
  align-items: center;
}

.form-row input {
  padding: 8px 12px;
  border: 1px solid #ccc;
  border-radius: 6px;
  font-size: 0.95rem;
  flex: 1;
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

table {
  width: 100%;
  border-collapse: collapse;
}

th, td {
  padding: 10px 12px;
  text-align: left;
  border-bottom: 1px solid #e0e0e0;
}

th {
  font-weight: 600;
  font-size: 0.9rem;
  color: #555;
}

.edit-input {
  padding: 4px 8px;
  border: 1px solid #42b983;
  border-radius: 4px;
  font-size: 0.95rem;
  width: 100%;
}

.edit-btn {
  padding: 4px 12px;
  background: none;
  border: 1px solid #ccc;
  border-radius: 4px;
  font-size: 0.85rem;
  cursor: pointer;
  color: #333;
}

.edit-btn:hover {
  background: #f0f0f0;
}

.save-btn {
  padding: 4px 12px;
  background-color: #42b983;
  color: #fff;
  border: none;
  border-radius: 4px;
  font-size: 0.85rem;
  font-weight: 600;
  cursor: pointer;
  margin-right: 4px;
}

.cancel-btn {
  padding: 4px 12px;
  background: none;
  border: 1px solid #ccc;
  border-radius: 4px;
  font-size: 0.85rem;
  cursor: pointer;
  color: #333;
}
</style>
