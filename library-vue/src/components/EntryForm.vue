<template>
  <form @submit.prevent="onSubmit" class="entry-form">
    <input v-model.number="delta" type="number" placeholder="+1 or -1" required />
    <input v-model="note" type="text" placeholder="Note (optional)" />
    <button type="submit">Add</button>
    <div class="quick-actions">
      <button type="button" class="quick-btn positive" @click="quickAdd(1, 'Read a book')">+1 Read</button>
      <button type="button" class="quick-btn negative" @click="quickAdd(-1, 'Bought a book')">−1 Bought</button>
    </div>
  </form>
</template>

<script>
export default {
  emits: ['submit'],
  data() {
    return {
      delta: null,
      note: ''
    }
  },
  methods: {
    onSubmit() {
      if (this.delta == null) return
      this.$emit('submit', { delta: this.delta, note: this.note })
      this.delta = null
      this.note = ''
    },
    quickAdd(delta, note) {
      this.$emit('submit', { delta, note })
    }
  }
}
</script>

<style scoped>
.entry-form {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
  margin: 16px 0;
}

.entry-form input[type="number"] {
  width: 80px;
  padding: 8px 10px;
  border: 1px solid #ccc;
  border-radius: 6px;
  font-size: 0.95rem;
}

.entry-form input[type="text"] {
  flex: 1;
  min-width: 150px;
  padding: 8px 10px;
  border: 1px solid #ccc;
  border-radius: 6px;
  font-size: 0.95rem;
}

.entry-form button[type="submit"] {
  padding: 8px 16px;
  background-color: #42b983;
  color: #fff;
  border: none;
  border-radius: 6px;
  font-weight: 600;
  cursor: pointer;
}

.entry-form button[type="submit"]:hover {
  background-color: #369e6f;
}

.quick-actions {
  display: flex;
  gap: 6px;
  width: 100%;
  margin-top: 4px;
}

.quick-btn {
  padding: 6px 12px;
  border: none;
  border-radius: 6px;
  font-size: 0.85rem;
  font-weight: 600;
  cursor: pointer;
}

.quick-btn.positive {
  background: #d4edda;
  color: #155724;
}

.quick-btn.positive:hover {
  background: #c3e6cb;
}

.quick-btn.negative {
  background: #f8d7da;
  color: #721c24;
}

.quick-btn.negative:hover {
  background: #f5c6cb;
}
</style>
