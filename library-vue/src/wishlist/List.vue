<template>
  <div class="book-list">
    <div class="list-header">
      <h2>Wishlist</h2>
      <router-link to="/wishlist/new" class="add-btn">+ Add to wishlist</router-link>
    </div>

    <div class="search-row">
      <input v-model="search" placeholder="Search books..." class="search-input" />
      <select v-model="releaseFilter" class="release-filter">
        <option value="all">All</option>
        <option value="released">Released</option>
        <option value="not-released">Not Yet Released</option>
      </select>
      <span v-if="search || releaseFilter !== 'all'" class="clear-filter" @click="search = ''; releaseFilter = 'all'">✕ Clear filter</span>
    </div>

    <p v-if="error" class="error">{{ error }}</p>
    <p v-else-if="loading">Loading...</p>
    <p v-else-if="filteredBooks.length === 0">No books found.</p>

    <div v-else class="table-wrap">
    <table>
      <thead>
      <tr>
        <th class="sortable" @click="toggleSort('title')">Title {{ sortField === 'title' ? (sortOrder === 'asc' ? '▲' : '▼') : '' }}</th>
        <th class="sortable" @click="toggleSort('author')">Author {{ sortField === 'author' ? (sortOrder === 'asc' ? '▲' : '▼') : '' }}</th>
        <th class="sortable" @click="toggleSort('releaseDate')">Release Date {{ sortField === 'releaseDate' ? (sortOrder === 'asc' ? '▲' : '▼') : ''}} </th>
        <th>Notes</th>
        <th>Actions</th>
      </tr>
      </thead>
      <tbody>
      <tr v-for="book in paginatedBooks" :key="book.id">
        <td data-label="Title">{{ book.title }}</td>
        <td data-label="Author">
          <a v-if="book.authors !== '—'" class="filter-link" @click="search = book.authors">{{ book.authors }}</a>
          <span v-else>—</span>
        </td>
        <td data-label="Release Date">{{ book.releaseDate }}</td>
        <td data-label="Notes">{{ book.notes }}</td>
        <td data-label="Actions" class="actions">
          <router-link :to="'/wishlist/edit/' + book.id" class="edit-link">Edit</router-link>
          <router-link :to="'/wishlist/convert/' + book.id" class="edit-link">Add to Library</router-link>
          <button class="delete-btn" @click="deleteWishlistBook(book)">Delete</button>
        </td>
      </tr>
      </tbody>
    </table>
    </div>

    <div v-if="totalPages > 1" class="pagination">
      <button :disabled="currentPage === 1" @click="currentPage = 1">«</button>
      <button :disabled="currentPage === 1" @click="currentPage--">‹</button>
      <span class="page-info">Page {{ currentPage }} of {{ totalPages }} ({{ filteredBooks.length }} books)</span>
      <button :disabled="currentPage === totalPages" @click="currentPage++">›</button>
      <button :disabled="currentPage === totalPages" @click="currentPage = totalPages">»</button>
    </div>
  </div>
</template>

<script>
import moment from 'moment';
export default {
  data() {
    return {
      books: [],
      search: '',
      releaseFilter: 'all',
      statusFilter: '',
      genreFilter: '',
      sortField: 'title',
      sortOrder: 'asc',
      statusOptions: [],
      editingStatusBookId: null,
      editingStatusValue: null,
      loading: true,
      error: null,
      currentPage: 1,
      pageSize: 20
    }
  },
  computed: {
    filteredBooks() {
      let result = this.books
      const q = this.search.toLowerCase()
      if (q) {
        result = result.filter(b =>
            Object.values(b).some(v => String(v).toLowerCase().includes(q))
        )
      }
      if (this.releaseFilter !== 'all') {
        const today = moment().startOf('day')
        result = result.filter(b => {
          if (!b.rawReleaseDate) return false
          const release = moment(b.rawReleaseDate)
          return this.releaseFilter === 'released' ? release.isSameOrBefore(today) : release.isAfter(today)
        })
      }
      result = result.slice().sort((a, b) => {
        let cmp
        if (this.sortField === 'author') {
          cmp = a.authorSort.localeCompare(b.authorSort)
        } else if (this.sortField === 'title') {
          cmp = a.sortTitle.localeCompare(b.sortTitle)
        } else if (this.sortField === 'releaseDate') {
          cmp = moment(a.releaseDate, 'MMMM Do, YYYY').diff(moment(b.releaseDate, 'MMMM Do, YYYY'))
        }
        return this.sortOrder === 'asc' ? cmp : -cmp
      })
      return result
    },
    totalPages() {
      return Math.max(1, Math.ceil(this.filteredBooks.length / this.pageSize))
    },
    paginatedBooks() {
      const start = (this.currentPage - 1) * this.pageSize
      return this.filteredBooks.slice(start, start + this.pageSize)
    }
  },
  watch: {
    search() { this.currentPage = 1 },
    releaseFilter() { this.currentPage = 1 }
  },
  methods: {
    toggleSort(field) {
      if (this.sortField === field) {
        this.sortOrder = this.sortOrder === 'asc' ? 'desc' : 'asc'
      } else {
        this.sortField = field
        this.sortOrder = 'asc'
      }
    },
    async deleteWishlistBook(book) {
      if (!confirm('Delete "' + book.title + '"?')) return
      try {
        const res = await fetch('/api/wishlist/' + book.id, { method: 'DELETE' })
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
      const wishlistRes = await fetch('/api/wishlist/all')
      if (!wishlistRes.ok) {
        this.error = 'API returned ' + wishlistRes.status
        return
      }
      const data = await wishlistRes.json()
      this.books = data.map(row => ({
        id:        row.id,
        title:     row.title,
        sortTitle:  row.sortTitle || row.title,
        authors:   row.authors?.length ? row.authors.map(a => a.name).join(', ') : '—',
        authorSort: row.authors?.length ? row.authors.map(a => a.lastName || '').sort().join(', ') : '',
        rawReleaseDate: row.releaseDate || null,
        releaseDate: row.releaseDate ? moment(row.releaseDate).format('MMMM Do, YYYY') : '',
        notes: row.notes
      }))
    } catch (err) {
      this.error = 'Failed to load wishlist: ' + err.message
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

.release-filter {
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

.table-wrap {
  overflow-x: auto;
  -webkit-overflow-scrolling: touch;
}

@media (max-width: 768px) {
  .list-header {
    flex-direction: column;
    align-items: stretch;
    gap: 8px;
  }

  .add-btn {
    text-align: center;
  }

  .search-row {
    flex-direction: column;
    align-items: stretch;
  }

  .search-input {
    max-width: none;
  }

  table thead {
    display: none;
  }

  table tbody tr {
    display: block;
    border: 1px solid #e0e0e0;
    border-radius: 8px;
    margin-bottom: 12px;
    padding: 12px;
  }

  table tbody td {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 4px 0;
    border-bottom: none;
  }

  table tbody td::before {
    content: attr(data-label);
    font-weight: 600;
    color: #555;
    margin-right: 12px;
    flex-shrink: 0;
  }

  table tbody td.actions {
    justify-content: flex-end;
    flex-wrap: wrap;
    padding-top: 8px;
    border-top: 1px solid #f0f0f0;
    margin-top: 4px;
  }

  table tbody td.actions::before {
    display: none;
  }

  .pagination {
    flex-wrap: wrap;
  }
}
</style>
