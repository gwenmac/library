<template>
  <div class="book-list">
    <div class="list-header">
      <h2>Books</h2>
      <router-link to="/book/new" class="add-btn">+ Add Book</router-link>
    </div>

    <div class="search-row">
      <input v-model="search" placeholder="Search books..." class="search-input" />
      <span v-if="search" class="clear-filter" @click="search = ''">✕ Clear filter</span>
    </div>

    <p v-if="error" class="error">{{ error }}</p>
    <p v-else-if="loading">Loading...</p>
    <p v-else-if="filteredBooks.length === 0">No books found.</p>

    <table v-else>
      <thead>
        <tr>
          <th>Title</th>
          <th>Author</th>
          <th>Series</th>
          <th>Genres</th>
          <th>Languages</th>
          <th>Pages</th>
          <th>Status</th>
          <th>Actions</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="book in filteredBooks" :key="book.id">
          <td>{{ book.title }}</td>
          <td>
            <a v-if="book.authors !== '—'" class="filter-link" @click="search = book.authors">{{ book.authors }}</a>
            <span v-else>—</span>
          </td>
          <td>
            <a v-if="book.series !== '—'" class="filter-link" @click="search = book.series">{{ book.series }}</a>
            <span v-else>—</span>
          </td>
          <td>{{ book.genres }}</td>
          <td>{{ book.languages }}</td>
          <td>{{ book.pageCount }}</td>
          <td>{{ book.status }}</td>
          <td class="actions">
            <router-link :to="'/book/edit/' + book.id" class="edit-link">Edit</router-link>
            <button class="delete-btn" @click="deleteBook(book)">Delete</button>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script>
export default {
  data() {
    return {
      books: [],
      search: '',
      loading: true,
      error: null
    }
  },
  computed: {
    filteredBooks() {
      const q = this.search.toLowerCase()
      if (!q) return this.books
      return this.books.filter(b =>
        Object.values(b).some(v => String(v).toLowerCase().includes(q))
      )
    }
  },
  methods: {
    async deleteBook(book) {
      if (!confirm('Delete "' + book.title + '"?')) return
      try {
        const res = await fetch('/api/books/' + book.id, { method: 'DELETE' })
        if (!res.ok) {
          this.error = 'Delete failed (API returned ' + res.status + ')'
          return
        }
        this.books = this.books.filter(b => b.id !== book.id)
      } catch (err) {
        this.error = 'Delete failed: ' + err.message
      }
    }
  },
  async mounted() {
    try {
      const res = await fetch('/api/all')
      if (!res.ok) {
        this.error = 'API returned ' + res.status
        return
      }
      const data = await res.json()
      this.books = data.map(row => ({
        id:        row.id,
        title:     row.title,
        authors:   row.authors?.length ? row.authors.map(a => a.name).join(', ') : '—',
        series:    row.series ? row.series.name : '—',
        genres:    row.genres?.length ? row.genres.map(g => g.name).join(', ') : '—',
        languages: row.languages?.length ? row.languages.map(l => l.name).join(', ') : '—',
        pageCount: row.pageCount ?? '—',
        status:    row.bookStatus?.status?.name ?? '—'
      }))
    } catch (err) {
      this.error = 'Failed to load books: ' + err.message
    } finally {
      this.loading = false
    }
  }
}
</script>

<style scoped>
.book-list {
  padding: 16px;
}

.list-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.list-header h2 {
  margin: 0;
}

.add-btn {
  padding: 8px 16px;
  background-color: #42b983;
  color: #fff;
  border-radius: 6px;
  text-decoration: none;
  font-weight: 600;
  font-size: 0.95rem;
}

.add-btn:hover {
  background-color: #369e6f;
}

.search-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.search-input {
  width: 100%;
  max-width: 400px;
  padding: 8px 12px;
  border: 1px solid #ccc;
  border-radius: 6px;
  font-size: 0.95rem;
}

.clear-filter {
  color: #e74c3c;
  font-size: 0.85rem;
  font-weight: 600;
  cursor: pointer;
  white-space: nowrap;
}

.clear-filter:hover {
  text-decoration: underline;
}

.filter-link {
  color: #42b983;
  font-weight: 600;
  cursor: pointer;
}

.filter-link:hover {
  text-decoration: underline;
}

.error {
  color: #e74c3c;
  font-weight: 600;
}

.edit-link {
  color: #42b983;
  font-weight: 600;
  text-decoration: none;
}

.edit-link:hover {
  text-decoration: underline;
}

.actions {
  display: flex;
  gap: 8px;
  align-items: center;
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
