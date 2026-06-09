<template>
  <div class="series-list">
    <div class="list-header">
      <h2>Series</h2>
    </div>

    <input v-model="searchInput" placeholder="Search series..." class="search-input" />

    <p v-if="loading">Loading...</p>
    <p v-else-if="error" class="error">{{ error }}</p>
    <p v-else-if="filteredSeries.length === 0">No series found.</p>

    <ul v-else class="series-items">
      <li v-for="s in filteredSeries" :key="s.id" class="series-item">
        <router-link :to="seriesLink(s)" class="series-link">{{ s.name }}</router-link>
        <button class="delete-btn" @click="confirmDelete(s)">Delete</button>
      </li>
    </ul>
  </div>
</template>

<script>
export default {
  data() {
    return {
      series: [],
      searchInput: '',
      loading: true,
      error: null
    }
  },
  computed: {
    filteredSeries() {
      if (!this.searchInput) return this.series
      const term = this.searchInput.toLowerCase()
      return this.series.filter(s => s.name.toLowerCase().includes(term))
    }
  },
  async mounted() {
    try {
      const res = await fetch('/api/series/all')
      if (!res.ok) {
        this.error = 'Failed to load series (API returned ' + res.status + ')'
        return
      }
      const data = await res.json()
      this.series = data.sort((a, b) => a.name.localeCompare(b.name))
    } catch (err) {
      this.error = 'Failed to load series: ' + err.message
    } finally {
      this.loading = false
    }
  },
  methods: {
    seriesLink(s) {
      return {
        path: '/book/list',
        query: { seriesId: s.id, sort: 'series', dir: 'asc' }
      }
    },
    async confirmDelete(s) {
      const confirmed = confirm(`Are you sure you want to delete "${s.name}"? This will also delete ALL books in this series.`)
      if (!confirmed) return
      try {
        const res = await fetch('/api/series/' + s.id, { method: 'DELETE' })
        if (!res.ok) {
          this.error = 'Failed to delete series (API returned ' + res.status + ')'
          return
        }
        this.series = this.series.filter(item => item.id !== s.id)
      } catch (err) {
        this.error = 'Failed to delete series: ' + err.message
      }
    }
  }
}
</script>

<style scoped>
.series-list {
  padding: 16px;
  max-width: 600px;
}

.list-header {
  margin-bottom: 12px;
}

.list-header h2 {
  margin: 0;
}

.search-input {
  width: 100%;
  padding: 8px 10px;
  border: 1px solid #ccc;
  border-radius: 6px;
  font-size: 0.95rem;
  margin-bottom: 16px;
}

.series-items {
  list-style: none;
  padding: 0;
  margin: 0;
}

.series-items li {
  border-bottom: 1px solid #e0e0e0;
}

.series-items li:last-child {
  border-bottom: none;
}

.series-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.series-link {
  display: block;
  padding: 12px 16px;
  text-decoration: none;
  color: #333;
  font-weight: 500;
  flex: 1;
  transition: background-color 0.15s;
}

.series-link:hover {
  background-color: #f5f5f5;
  color: #42b983;
}

.delete-btn {
  padding: 4px 12px;
  border: 1px solid #e74c3c;
  border-radius: 4px;
  background: none;
  color: #e74c3c;
  font-size: 0.85rem;
  font-weight: 600;
  cursor: pointer;
  margin-right: 12px;
}

.delete-btn:hover {
  background: #e74c3c;
  color: #fff;
}

.error {
  color: #e74c3c;
  font-weight: 600;
}
</style>
