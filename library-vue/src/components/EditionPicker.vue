<template>
  <div class="field">
    <label for="edition">Edition</label>
    <div class="edition-picker">
      <select v-if="!creatingNew" id="edition" :value="modelValue" @change="$emit('update:modelValue', $event.target.value ? Number($event.target.value) : null)">
        <option :value="null">— None —</option>
        <option v-for="e in editionList" :key="e.id" :value="e.id">{{ e.name }}</option>
      </select>
      <input v-else id="newEditionName" v-model="newName" placeholder="New edition name..." />
      <button type="button" class="edition-toggle-btn" @click="toggleNew">
        {{ creatingNew ? 'Pick existing' : '+ New edition' }}
      </button>
    </div>
  </div>
</template>

<script>
export default {
  props: {
    modelValue: { default: null },
    editionList: { type: Array, required: true }
  },
  emits: ['update:modelValue', 'update:editionList', 'error'],
  data() {
    return {
      creatingNew: false,
      newName: ''
    }
  },
  methods: {
    toggleNew() {
      this.creatingNew = !this.creatingNew
      if (this.creatingNew) {
        this.$emit('update:modelValue', null)
        this.newName = ''
      } else {
        this.newName = ''
      }
    },
    async createIfNeeded() {
      if (!this.creatingNew || !this.newName.trim()) return null
      try {
        const res = await fetch('/api/editions', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ name: this.newName.trim() })
        })
        if (!res.ok) {
          this.$emit('error', 'Failed to create edition (API returned ' + res.status + ')')
          return null
        }
        const newEdition = await res.json()
        this.$emit('update:editionList', [...this.editionList, newEdition])
        this.$emit('update:modelValue', newEdition.id)
        this.creatingNew = false
        this.newName = ''
        return newEdition
      } catch (err) {
        this.$emit('error', 'Failed to create edition: ' + err.message)
        return null
      }
    }
  }
}
</script>

<style scoped>
.field {
  margin-bottom: 14px;
}

.field label {
  display: block;
  font-weight: 600;
  margin-bottom: 4px;
}

.field select,
.field input {
  width: 100%;
  padding: 8px 10px;
  border: 1px solid #ccc;
  border-radius: 6px;
  font-size: 0.95rem;
}

.edition-picker {
  display: flex;
  gap: 8px;
  align-items: center;
}

.edition-picker select,
.edition-picker input {
  flex: 1;
}

.edition-toggle-btn {
  padding: 8px 12px;
  border: 1px solid #42b983;
  border-radius: 6px;
  background: #fff;
  color: #42b983;
  font-size: 0.85rem;
  font-weight: 600;
  cursor: pointer;
  white-space: nowrap;
}

.edition-toggle-btn:hover {
  background: #42b983;
  color: #fff;
}
</style>
