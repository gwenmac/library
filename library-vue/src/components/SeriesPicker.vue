<template>
  <div class="field">
    <label for="series">Series</label>
    <div class="series-picker">
      <select v-if="!creatingNew" id="series" :value="modelValue" @change="$emit('update:modelValue', $event.target.value ? Number($event.target.value) : null)">
        <option :value="null">— None —</option>
        <option v-for="s in seriesList" :key="s.id" :value="s.id">{{ s.name }}</option>
      </select>
      <input v-else id="newSeriesName" v-model="newName" placeholder="New series name..." />
      <button type="button" class="series-toggle-btn" @click="toggleNew">
        {{ creatingNew ? 'Pick existing' : '+ New series' }}
      </button>
    </div>
  </div>
</template>

<script>
export default {
  props: {
    modelValue: { default: null },
    seriesList: { type: Array, required: true }
  },
  emits: ['update:modelValue', 'update:seriesList', 'error'],
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
        const res = await fetch('/api/series', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ name: this.newName.trim() })
        })
        if (!res.ok) {
          this.$emit('error', 'Failed to create series (API returned ' + res.status + ')')
          return null
        }
        const newSeries = await res.json()
        this.$emit('update:seriesList', [...this.seriesList, newSeries])
        this.$emit('update:modelValue', newSeries.id)
        this.creatingNew = false
        this.newName = ''
        return newSeries
      } catch (err) {
        this.$emit('error', 'Failed to create series: ' + err.message)
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

.series-picker {
  display: flex;
  gap: 8px;
  align-items: center;
}

.series-picker select,
.series-picker input {
  flex: 1;
}

.series-toggle-btn {
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

.series-toggle-btn:hover {
  background: #42b983;
  color: #fff;
}
</style>
