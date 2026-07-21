<template>
  <div class="field">
    <label>Authors</label>
    <div class="chip-list" v-if="selected.length">
      <span class="chip" v-for="item in selected" :key="item.id">
        {{ item.name }}
        <button type="button" class="chip-remove" @click="remove(item.id)">✕</button>
      </span>
    </div>
    <div class="picker-row">
      <select v-if="!creatingNew" v-model="itemToAdd">
        <option :value="null">— Select author —</option>
        <option v-for="item in available" :key="item.id" :value="item.id">{{ item.name }}</option>
      </select>
      <template v-else>
        <input v-model="newFirstName" placeholder="First name (optional)" class="name-input" />
        <input v-model="newLastName" placeholder="Last name" class="name-input" />
      </template>
      <button type="button" class="picker-btn picker-btn-add" @click="add" :disabled="creatingNew ? !newLastName.trim() : !itemToAdd">
        Add
      </button>
      <button type="button" class="picker-btn picker-btn-toggle" @click="toggleNew">
        {{ creatingNew ? 'Pick existing' : '+ New' }}
      </button>
    </div>
  </div>
</template>

<script>
export default {
  props: {
    selected: { type: Array, required: true },
    items: { type: Array, required: true }
  },
  emits: ['update:selected', 'update:items', 'error'],
  data() {
    return {
      itemToAdd: null,
      creatingNew: false,
      newFirstName: '',
      newLastName: ''
    }
  },
  computed: {
    available() {
      const selectedIds = new Set(this.selected.map(i => i.id))
      return this.items.filter(i => !selectedIds.has(i.id))
    }
  },
  methods: {
    toggleNew() {
      this.creatingNew = !this.creatingNew
      this.itemToAdd = null
      this.newFirstName = ''
      this.newLastName = ''
    },
    async add() {
      if (this.creatingNew) {
        const lastName = this.newLastName.trim()
        if (!lastName) return
        const firstName = this.newFirstName.trim() || null
        try {
          const res = await fetch('/api/authors', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ firstName, lastName })
          })
          if (!res.ok) {
            this.$emit('error', 'Failed to create author (API returned ' + res.status + ')')
            return
          }
          const created = await res.json()
          this.$emit('update:items', [...this.items, created])
          this.$emit('update:selected', [...this.selected, created])
          this.newFirstName = ''
          this.newLastName = ''
          this.creatingNew = false
        } catch (err) {
          this.$emit('error', 'Failed to create author: ' + err.message)
        }
      } else {
        if (!this.itemToAdd) return
        const item = this.items.find(i => i.id === this.itemToAdd)
        if (item) this.$emit('update:selected', [...this.selected, item])
        this.itemToAdd = null
      }
    },
    remove(id) {
      this.$emit('update:selected', this.selected.filter(i => i.id !== id))
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

.chip-list {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 8px;
}

.chip {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 10px;
  background: #e8f5e9;
  border: 1px solid #42b983;
  border-radius: 16px;
  font-size: 0.85rem;
  font-weight: 600;
  color: #2e7d5e;
}

.chip-remove {
  background: none;
  border: none;
  color: #e74c3c;
  cursor: pointer;
  font-size: 0.8rem;
  padding: 0 2px;
  line-height: 1;
}

.picker-row {
  display: flex;
  gap: 8px;
  align-items: center;
}

.picker-row select,
.name-input {
  flex: 1;
  min-width: 0;
  padding: 8px 10px;
  border: 1px solid #ccc;
  border-radius: 6px;
  font-size: 0.95rem;
  box-sizing: border-box;
}

.picker-btn {
  padding: 8px 12px;
  border: 1px solid #42b983;
  border-radius: 6px;
  font-size: 0.85rem;
  font-weight: 600;
  cursor: pointer;
  white-space: nowrap;
}

.picker-btn-add {
  background: #42b983;
  color: #fff;
}

.picker-btn-add:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.picker-btn-toggle {
  background: #fff;
  color: #42b983;
}

.picker-btn-toggle:hover {
  background: #42b983;
  color: #fff;
}
</style>
