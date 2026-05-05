<template>
  <div class="gauge-new">
    <h2>New Gauge</h2>

    <p v-if="error" class="error">{{ error }}</p>

    <form @submit.prevent="save">
      <div class="field">
        <label>Name</label>
        <input v-model="form.name" required />
      </div>
      <div class="field">
        <label>Description</label>
        <textarea v-model="form.description" rows="3"></textarea>
      </div>
      <div class="actions">
        <button type="submit" :disabled="saving">{{ saving ? 'Creating...' : 'Create Gauge' }}</button>
        <button type="button" @click="$router.push('/gauge/list')">Cancel</button>
      </div>
    </form>
  </div>
</template>

<script>
export default {
  data() {
    return {
      form: { name: '', description: '' },
      saving: false,
      error: null
    }
  },
  methods: {
    async save() {
      this.saving = true
      this.error = null
      try {
        const res = await fetch('/api/gauges', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(this.form)
        })
        if (!res.ok) {
          this.error = 'Failed to create gauge (API returned ' + res.status + ')'
          return
        }
        this.$router.push('/gauge/list')
      } catch (err) {
        this.error = 'Failed to create gauge: ' + err.message
      } finally {
        this.saving = false
      }
    }
  }
}
</script>

<style scoped>
.gauge-new {
  padding: 16px;
  max-width: 500px;
}

.field {
  margin-bottom: 14px;
}

.field label {
  display: block;
  font-weight: 600;
  margin-bottom: 4px;
}

.field input,
.field textarea {
  width: 100%;
  padding: 8px 10px;
  border: 1px solid #ccc;
  border-radius: 6px;
  font-size: 0.95rem;
}

.actions {
  display: flex;
  gap: 10px;
  margin-top: 18px;
}

.actions button {
  padding: 8px 20px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.95rem;
}

.actions button[type="submit"] {
  background-color: #42b983;
  color: #fff;
}

.actions button[type="submit"]:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.actions button[type="button"] {
  background-color: #e7e7e7;
  color: #333;
}

.error {
  color: #e74c3c;
  font-weight: 600;
}
</style>
