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
        <label>Authors</label>
        <div class="chip-list" v-if="selectedAuthors.length">
          <span class="chip" v-for="a in selectedAuthors" :key="a.id">
            {{ a.name }}
            <button type="button" class="chip-remove" @click="removeAuthor(a.id)">✕</button>
          </span>
        </div>
        <div class="author-picker">
          <select v-if="!creatingNewAuthor" v-model="authorToAdd">
            <option :value="null">— Select author —</option>
            <option v-for="a in availableAuthors" :key="a.id" :value="a.id">{{ a.name }}</option>
          </select>
          <input v-else v-model="newAuthorName" placeholder="New author name..." />
          <button type="button" class="toggle-btn" @click="addAuthor" :disabled="creatingNewAuthor ? !newAuthorName.trim() : !authorToAdd">
            Add
          </button>
          <button type="button" class="toggle-btn toggle-new-btn" @click="toggleNewAuthor">
            {{ creatingNewAuthor ? 'Pick existing' : '+ New' }}
          </button>
        </div>
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

      <div class="field">
        <label>Genres</label>
        <div class="chip-list" v-if="selectedGenres.length">
          <span class="chip" v-for="g in selectedGenres" :key="g.id">
            {{ g.name }}
            <button type="button" class="chip-remove" @click="removeGenre(g.id)">✕</button>
          </span>
        </div>
        <div class="genre-picker">
          <select v-if="!creatingNewGenre" v-model="genreToAdd">
            <option :value="null">— Select genre —</option>
            <option v-for="g in availableGenres" :key="g.id" :value="g.id">{{ g.name }}</option>
          </select>
          <input v-else v-model="newGenreName" placeholder="New genre name..." />
          <button type="button" class="toggle-btn" @click="addGenre" :disabled="creatingNewGenre ? !newGenreName.trim() : !genreToAdd">
            Add
          </button>
          <button type="button" class="toggle-btn toggle-new-btn" @click="toggleNewGenre">
            {{ creatingNewGenre ? 'Pick existing' : '+ New' }}
          </button>
        </div>
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
        description: '',
        pageCount: null,
        seriesId: null,
        seriesOrder: null
      },
      authorList: [],
      selectedAuthors: [],
      authorToAdd: null,
      creatingNewAuthor: false,
      newAuthorName: '',
      seriesList: [],
      creatingNewSeries: false,
      newSeriesName: '',
      genreList: [],
      selectedGenres: [],
      genreToAdd: null,
      creatingNewGenre: false,
      newGenreName: '',
      saving: false,
      error: null
    }
  },
  computed: {
    availableAuthors() {
      const selectedIds = new Set(this.selectedAuthors.map(a => a.id))
      return this.authorList.filter(a => !selectedIds.has(a.id))
    },
    availableGenres() {
      const selectedIds = new Set(this.selectedGenres.map(g => g.id))
      return this.genreList.filter(g => !selectedIds.has(g.id))
    }
  },
  async mounted() {
    try {
      const [seriesRes, authorsRes, genresRes] = await Promise.all([
        fetch('/api/series/all'),
        fetch('/api/authors/all'),
        fetch('/api/genres/all')
      ])
      if (seriesRes.ok) this.seriesList = await seriesRes.json()
      if (authorsRes.ok) this.authorList = await authorsRes.json()
      if (genresRes.ok) this.genreList = await genresRes.json()
    } catch (err) {
      console.error('Failed to load data:', err)
    }
  },
  methods: {
    toggleNewAuthor() {
      this.creatingNewAuthor = !this.creatingNewAuthor
      this.authorToAdd = null
      this.newAuthorName = ''
    },
    async addAuthor() {
      if (this.creatingNewAuthor) {
        const name = this.newAuthorName.trim()
        if (!name) return
        try {
          const res = await fetch('/api/authors', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ name })
          })
          if (!res.ok) {
            this.error = 'Failed to create author (API returned ' + res.status + ')'
            return
          }
          const newAuthor = await res.json()
          this.authorList.push(newAuthor)
          this.selectedAuthors.push(newAuthor)
          this.newAuthorName = ''
          this.creatingNewAuthor = false
        } catch (err) {
          this.error = 'Failed to create author: ' + err.message
        }
      } else {
        if (!this.authorToAdd) return
        const author = this.authorList.find(a => a.id === this.authorToAdd)
        if (author) this.selectedAuthors.push(author)
        this.authorToAdd = null
      }
    },
    removeAuthor(id) {
      this.selectedAuthors = this.selectedAuthors.filter(a => a.id !== id)
    },
    toggleNewGenre() {
      this.creatingNewGenre = !this.creatingNewGenre
      this.genreToAdd = null
      this.newGenreName = ''
    },
    async addGenre() {
      if (this.creatingNewGenre) {
        const name = this.newGenreName.trim()
        if (!name) return
        try {
          const res = await fetch('/api/genres', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ name })
          })
          if (!res.ok) {
            this.error = 'Failed to create genre (API returned ' + res.status + ')'
            return
          }
          const newGenre = await res.json()
          this.genreList.push(newGenre)
          this.selectedGenres.push(newGenre)
          this.newGenreName = ''
          this.creatingNewGenre = false
        } catch (err) {
          this.error = 'Failed to create genre: ' + err.message
        }
      } else {
        if (!this.genreToAdd) return
        const genre = this.genreList.find(g => g.id === this.genreToAdd)
        if (genre) this.selectedGenres.push(genre)
        this.genreToAdd = null
      }
    },
    removeGenre(id) {
      this.selectedGenres = this.selectedGenres.filter(g => g.id !== id)
    },
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

        const payload = {
          ...this.form,
          authorIds: this.selectedAuthors.map(a => a.id),
          genreIds: this.selectedGenres.map(g => g.id)
        }

        const res = await fetch('/api/books', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(payload)
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

.author-picker {
  display: flex;
  gap: 8px;
  align-items: center;
}

.author-picker select,
.author-picker input {
  flex: 1;
}

.genre-picker {
  display: flex;
  gap: 8px;
  align-items: center;
}

.genre-picker select,
.genre-picker input {
  flex: 1;
}

.toggle-btn {
  padding: 8px 12px;
  border: 1px solid #42b983;
  border-radius: 6px;
  background: #42b983;
  color: #fff;
  font-size: 0.85rem;
  font-weight: 600;
  cursor: pointer;
  white-space: nowrap;
}

.toggle-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.toggle-new-btn {
  background: #fff;
  color: #42b983;
}

.toggle-new-btn:hover {
  background: #42b983;
  color: #fff;
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
