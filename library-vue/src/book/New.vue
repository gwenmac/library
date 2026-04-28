<template>
  <div class="book-edit">
    <h2>Add New Book</h2>

    <p v-if="error" class="error">{{ error }}</p>

    <form @submit.prevent="save">
      <div class="field">
        <label for="title">Title</label>
        <input id="title" v-model="form.title" required />
      </div>

      <div class="field">
        <label for="authors">Authors</label>
        <input id="authors" v-model="form.authors" required placeholder="Separate multiple authors with commas" />
      </div>

      <div class="field">
        <label for="description">Description</label>
        <textarea id="description" v-model="form.description" rows="4"></textarea>
      </div>

      <div class="field">
        <label for="pageCount">Page Count</label>
        <input id="pageCount" v-model.number="form.pageCount" type="number" min="0" />
      </div>

      <div class="field">
        <label for="series">Series</label>
        <div class="series-picker">
          <select v-if="!creatingNewSeries" id="series" v-model="form.seriesId">
            <option :value="null">— None —</option>
            <option v-for="s in seriesList" :key="s.id" :value="s.id">{{ s.name }}</option>
          </select>
          <input v-else id="newSeriesName" v-model="newSeriesName" placeholder="New series name..." />
          <button type="button" class="toggle-series-btn" @click="toggleNewSeries">
            {{ creatingNewSeries ? 'Pick existing' : '+ New series' }}
          </button>
        </div>
      </div>

      <div class="field" v-if="form.seriesId || creatingNewSeries">
        <label for="seriesOrder">Order in Series</label>
        <input id="seriesOrder" v-model.number="form.seriesOrder" type="number" min="1" />
      </div>

      <div class="actions">
        <button type="submit" :disabled="saving">{{ saving ? 'Saving...' : 'Add Book' }}</button>
        <button type="button" @click="$router.push('/book/list')">Cancel</button>
      </div>
    </form>
  </div>
</template>

<script>
export default {
  data() {
    return {
      form: {
        title: '',
        authors: '',
        description: '',
        pageCount: null,
        seriesId: null,
        seriesOrder: null
      },
      seriesList: [],
      creatingNewSeries: false,
      newSeriesName: '',
      saving: false,
      error: null
    }
  },
  async mounted() {
    try {
      const res = await fetch('/api/series/all')
      if (res.ok) {
        this.seriesList = await res.json()
      }
    } catch (err) {
      console.error('Failed to load series:', err)
    }
  },
  methods: {
    toggleNewSeries() {
      this.creatingNewSeries = !this.creatingNewSeries
      if (this.creatingNewSeries) {
        this.form.seriesId = null
        this.newSeriesName = ''
      } else {
        this.newSeriesName = ''
      }
    },
    async save() {
      this.saving = true
      this.error = null
      try {
        // If creating a new series, do that first
        if (this.creatingNewSeries && this.newSeriesName.trim()) {
          const seriesRes = await fetch('/api/series', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ name: this.newSeriesName.trim() })
          })
          if (!seriesRes.ok) {
            this.error = 'Failed to create series (API returned ' + seriesRes.status + ')'
            return
          }
          const newSeries = await seriesRes.json()
          this.form.seriesId = newSeries.id
        }

        const res = await fetch('/api/books', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(this.form)
        })
        if (!res.ok) {
          this.error = 'Failed to add book (API returned ' + res.status + ')'
          return
        }
        this.$router.push('/book/list')
      } catch (err) {
        this.error = 'Failed to add book: ' + err.message
      } finally {
        this.saving = false
      }
    }
  }
}
</script>

<style scoped>
.book-edit {
  padding: 16px;
  max-width: 600px;
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
.field textarea,
.field select {
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

.toggle-series-btn {
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

.toggle-series-btn:hover {
  background: #42b983;
  color: #fff;
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
