<template>
  <div class="book-list">
    <div class="list-header">
      <h2>Books</h2>
      <div class="header-actions">
        <router-link to="/book/new" class="add-btn">+ Add Book</router-link>
        <router-link to="/book/bulk-series" class="add-btn bulk-btn">+ Bulk Add Series</router-link>
      </div>
    </div>

    <div class="search-row">
      <input v-model="searchInput" @input="debouncedSearch" placeholder="Search books..." class="search-input" />
      <select v-model="statusFilter" @change="resetAndFetch" class="status-filter">
        <option value="">All Statuses</option>
        <option value="__none__">No Status</option>
        <option v-for="s in statuses" :key="s" :value="s">{{ s }}</option>
      </select>
      <select v-model="genreFilter" @change="resetAndFetch" class="status-filter">
        <option value="">All Genres</option>
        <option value="__none__">No Genre</option>
        <option v-for="g in genres" :key="g" :value="g">{{ g }}</option>
      </select>
      <span v-if="searchInput || statusFilter || genreFilter || seriesFilter" class="clear-filter" @click="clearFilters">✕ Clear filter</span>
    </div>

    <p v-if="error" class="error">{{ error }}</p>
    <p v-else-if="loading">Loading...</p>
    <p v-else-if="books.length === 0">No books found.</p>

    <table v-else>
      <thead>
        <tr>
          <th class="sortable" @click="toggleSort('title')">Title {{ sortField === 'title' ? (sortOrder === 'asc' ? '▲' : '▼') : '' }}</th>
          <th class="sortable" @click="toggleSort('author')">Author {{ sortField === 'author' ? (sortOrder === 'asc' ? '▲' : '▼') : '' }}</th>
          <th class="sortable" @click="toggleSort('series')">Series {{ sortField === 'series' ? (sortOrder === 'asc' ? '▲' : '▼') : '' }}</th>
          <th>Genres</th>
          <th>Edition</th>
          <th>Languages</th>
          <th>Status</th>
          <th>Actions</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="book in books" :key="book.id">
          <td>{{ book.title }}</td>
          <td>
            <a v-if="book.authors !== '—'" class="filter-link" @click="searchByText(book.authors)">{{ book.authors }}</a>
            <span v-else>—</span>
          </td>
          <td>
            <a v-if="book.series !== '—'" class="filter-link" @click="filterBySeries(book.seriesId)">{{ book.series }}</a>
            <span v-else>—</span>
          </td>
          <td>{{ book.genres }}</td>
          <td>{{ book.edition }}</td>
          <td>{{ book.languages }}</td>
          <td class="status-cell" @click="startEditingStatus(book)">
            <select
              v-if="editingStatusBookId === book.id"
              v-model="editingStatusValue"
              @change="saveStatus(book)"
              @blur="cancelEditingStatus"
              class="inline-status-select"
              ref="statusSelect"
            >
              <option :value="null">— None —</option>
              <option v-for="s in statusOptions" :key="s.id" :value="s.id">{{ s.name }}</option>
            </select>
            <span v-else class="editable-status">{{ book.status }}</span>
          </td>
          <td class="actions">
            <router-link :to="'/book/edit/' + book.id" class="edit-link">Edit</router-link>
            <button class="delete-btn" @click="deleteBook(book)">Delete</button>
          </td>
        </tr>
      </tbody>
    </table>

    <div v-if="totalPages > 1" class="pagination">
      <button :disabled="currentPage === 0" @click="goToPage(0)">«</button>
      <button :disabled="currentPage === 0" @click="goToPage(currentPage - 1)">‹</button>
      <span class="page-info">Page
        <input
          type="number"
          class="page-jump"
          :min="1"
          :max="totalPages"
          :value="currentPage + 1"
          @keyup.enter="jumpToPage($event)"
          @blur="jumpToPage($event)"
        />
        of {{ totalPages }} ({{ totalElements }} books)
      </span>
      <button :disabled="currentPage === totalPages - 1" @click="goToPage(currentPage + 1)">›</button>
      <button :disabled="currentPage === totalPages - 1" @click="goToPage(totalPages - 1)">»</button>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      books: [],
      searchInput: '',
      search: '',
      statusFilter: '',
      genreFilter: '',
      seriesFilter: null,
      sortField: 'title',
      sortOrder: 'asc',
      statusOptions: [],
      statuses: [],
      genres: [],
      editingStatusBookId: null,
      editingStatusValue: null,
      loading: true,
      error: null,
      currentPage: 0,
      pageSize: 20,
      totalPages: 0,
      totalElements: 0,
      debounceTimer: null
    }
  },
  methods: {
    updateUrl() {
      const query = {}
      if (this.currentPage > 0) query.page = this.currentPage + 1
      if (this.search) query.search = this.search
      if (this.statusFilter) query.status = this.statusFilter
      if (this.genreFilter) query.genre = this.genreFilter
      if (this.seriesFilter) query.seriesId = this.seriesFilter
      if (this.sortField !== 'title') query.sort = this.sortField
      if (this.sortOrder !== 'asc') query.dir = this.sortOrder
      const current = this.$route.query
      const changed = Object.keys(query).length !== Object.keys(current).length ||
        Object.keys(query).some(k => String(query[k]) !== String(current[k]))
      if (changed) {
        this.$router.replace({ query })
      }
    },
    debouncedSearch() {
      clearTimeout(this.debounceTimer)
      this.debounceTimer = setTimeout(() => {
        this.search = this.searchInput
        this.currentPage = 0
        this.fetchBooks()
      }, 300)
    },
    searchByText(text) {
      this.searchInput = text
      this.search = text
      this.currentPage = 0
      this.fetchBooks()
    },
    filterBySeries(seriesId) {
      this.seriesFilter = seriesId
      this.sortField = 'series'
      this.sortOrder = 'asc'
      this.currentPage = 0
      this.fetchBooks()
    },
    clearFilters() {
      this.searchInput = ''
      this.search = ''
      this.statusFilter = ''
      this.genreFilter = ''
      this.seriesFilter = null
      this.currentPage = 0
      this.fetchBooks()
    },
    resetAndFetch() {
      this.currentPage = 0
      this.fetchBooks()
    },
    goToPage(page) {
      this.currentPage = page
      this.fetchBooks()
    },
    jumpToPage(event) {
      const val = parseInt(event.target.value, 10)
      if (isNaN(val)) return
      const page = Math.max(0, Math.min(val - 1, this.totalPages - 1))
      if (page !== this.currentPage) {
        this.goToPage(page)
      }
    },
    toggleSort(field) {
      if (this.sortField === field) {
        this.sortOrder = this.sortOrder === 'asc' ? 'desc' : 'asc'
      } else {
        this.sortField = field
        this.sortOrder = 'asc'
      }
      this.fetchBooks()
    },
    async fetchBooks() {
      this.loading = true
      this.error = null
      try {
        const params = new URLSearchParams({
          page: this.currentPage,
          size: this.pageSize,
          search: this.search,
          sort: this.sortField,
          dir: this.sortOrder
        })
        if (this.statusFilter) params.set('status', this.statusFilter)
        if (this.genreFilter) params.set('genre', this.genreFilter)
        if (this.seriesFilter) params.set('seriesId', this.seriesFilter)

        const res = await fetch('/api/books/page?' + params.toString())
        if (!res.ok) {
          this.error = 'API returned ' + res.status
          return
        }
        const data = await res.json()
        this.totalPages = data.totalPages
        this.totalElements = data.totalElements
        this.currentPage = data.number

        this.books = data.content.map(row => ({
          id:         row.id,
          title:      row.title,
          sortTitle:  row.sortTitle || row.title,
          authors:    row.authors?.length ? row.authors.map(a => a.name).join(', ') : '—',
          authorSort: row.authors?.length ? row.authors.map(a => a.lastName || '').sort().join(', ') : '',
          series:     row.series ? row.series.name + (row.seriesOrder ? ': ' + row.seriesOrder : '') : '—',
          seriesId:   row.series ? row.series.id : null,
          seriesName: row.series ? row.series.name : null,
          seriesOrder: row.seriesOrder || null,
          genres:     row.genres?.length ? row.genres.map(g => g.name).join(', ') : '—',
          edition:    row.edition ? row.edition.name : '—',
          languages:  row.languages?.length ? row.languages.map(l => l.name).join(', ') : '—',
          status:     row.statusName || '—',
          statusId:   row.statusId || null
        }))
      } catch (err) {
        this.error = 'Failed to load books: ' + err.message
      } finally {
        this.loading = false
        this.updateUrl()
      }
    },
    startEditingStatus(book) {
      this.editingStatusBookId = book.id
      this.editingStatusValue = book.statusId || null
      this.$nextTick(() => {
        const sel = this.$refs.statusSelect
        if (sel) {
          const el = Array.isArray(sel) ? sel[0] : sel
          if (el) el.focus()
        }
      })
    },
    cancelEditingStatus() {
      this.editingStatusBookId = null
      this.editingStatusValue = null
    },
    async saveStatus(book) {
      const statusId = this.editingStatusValue
      try {
        const res = await fetch('/api/books/' + book.id + '/status', {
          method: 'PUT',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ statusId })
        })
        if (!res.ok) {
          this.error = 'Failed to update status (API returned ' + res.status + ')'
          return
        }
        const matched = this.statusOptions.find(s => s.id === statusId)
        book.status = matched ? matched.name : '—'
        book.statusId = statusId
      } catch (err) {
        this.error = 'Failed to update status: ' + err.message
      } finally {
        this.editingStatusBookId = null
        this.editingStatusValue = null
      }
    },
    async deleteBook(book) {
      if (!confirm('Delete "' + book.title + '"?')) return
      try {
        const res = await fetch('/api/books/' + book.id, { method: 'DELETE' })
        if (!res.ok) {
          this.error = 'Delete failed (API returned ' + res.status + ')'
          return
        }
        this.fetchBooks()
      } catch (err) {
        this.error = 'Delete failed: ' + err.message
      }
    }
  },
  async mounted() {
    const q = this.$route.query
    if (q.page) this.currentPage = Math.max(0, parseInt(q.page, 10) - 1)
    if (q.search) { this.search = q.search; this.searchInput = q.search }
    if (q.status) this.statusFilter = q.status
    if (q.genre) this.genreFilter = q.genre
    if (q.seriesId) this.seriesFilter = Number(q.seriesId)
    if (q.sort) this.sortField = q.sort
    if (q.dir) this.sortOrder = q.dir

    try {
      const [statusesRes, genresRes] = await Promise.all([
        fetch('/api/statuses/all'),
        fetch('/api/genres/all')
      ])
      if (statusesRes.ok) {
        this.statusOptions = await statusesRes.json()
        this.statuses = this.statusOptions.map(s => s.name).sort()
      }
      if (genresRes.ok) {
        const genreData = await genresRes.json()
        this.genres = genreData.map(g => g.name).sort()
      }
    } catch (err) {
      // Non-critical: filters just won't populate
    }
    this.fetchBooks()
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

.header-actions {
  display: flex;
  gap: 8px;
}

.bulk-btn {
  background-color: #7c4dff;
}

.bulk-btn:hover {
  background-color: #6200ea !important;
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

.status-filter {
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

.sortable {
  cursor: pointer;
  user-select: none;
}

.sortable:hover {
  color: #186c45;
}

.status-cell {
  cursor: pointer;
}

.editable-status:hover {
  color: #42b983;
  text-decoration: underline;
}

.inline-status-select {
  padding: 4px 8px;
  border: 1px solid #42b983;
  border-radius: 4px;
  font-size: 0.85rem;
  outline: none;
}

.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-top: 16px;
  padding: 8px 0;
}

.pagination button {
  padding: 6px 12px;
  background-color: #42b983;
  color: #fff;
  border: none;
  border-radius: 4px;
  font-size: 0.9rem;
  font-weight: 600;
  cursor: pointer;
}

.pagination button:hover:not(:disabled) {
  background-color: #369e6f;
}

.pagination button:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

.page-info {
  font-size: 0.9rem;
  color: #555;
  margin: 0 8px;
}

.page-jump {
  width: 3.5em;
  padding: 4px 6px;
  border: 1px solid #ccc;
  border-radius: 4px;
  font-size: 0.9rem;
  text-align: center;
}
</style>
