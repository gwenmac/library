<template>
  <div class="gauge-detail">
    <p v-if="error" class="error">{{ error }}</p>
    <p v-else-if="loading">Loading...</p>

    <template v-else>
      <div class="header">
        <div>
          <h2>{{ gauge.name }}</h2>
          <p v-if="gauge.description" class="description">{{ gauge.description }}</p>
        </div>
        <button class="delete-btn" @click="deleteGauge">Delete Gauge</button>
      </div>

      <GaugeDisplay :value="gauge.value" />

      <h3>History</h3>
      <GaugeHistoryChart :entries="entries" />

      <h3>Add Entry</h3>
      <EntryForm @submit="addEntry" />

      <h3>Entries</h3>
      <table v-if="entries.length">
        <thead>
          <tr>
            <th>Date</th>
            <th>Delta</th>
            <th>Note</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="e in reversedEntries" :key="e.id">
            <td>{{ formatDate(e.createdAt) }}</td>
            <td :class="{ positive: e.delta > 0, negative: e.delta < 0 }">
              {{ e.delta > 0 ? '+' : '' }}{{ e.delta }}
            </td>
            <td>{{ e.note || '—' }}</td>
            <td>
              <button class="delete-entry-btn" @click="deleteEntry(e)">✕</button>
            </td>
          </tr>
        </tbody>
      </table>
      <p v-else class="no-entries">No entries yet.</p>
    </template>
  </div>
</template>

<script>
import GaugeDisplay from '../components/GaugeDisplay.vue'
import GaugeHistoryChart from '../components/GaugeHistoryChart.vue'
import EntryForm from '../components/EntryForm.vue'

export default {
  components: { GaugeDisplay, GaugeHistoryChart, EntryForm },
  data() {
    return {
      gauge: null,
      entries: [],
      loading: true,
      error: null
    }
  },
  computed: {
    reversedEntries() {
      return [...this.entries].reverse()
    }
  },
  async mounted() {
    const id = this.$route.params.id
    try {
      const [gaugeRes, entriesRes] = await Promise.all([
        fetch('/api/gauges/' + id),
        fetch('/api/gauges/' + id + '/entries')
      ])
      if (!gaugeRes.ok) {
        this.error = 'Gauge not found'
        return
      }
      this.gauge = await gaugeRes.json()
      if (entriesRes.ok) {
        this.entries = await entriesRes.json()
      }
    } catch (err) {
      this.error = 'Failed to load gauge: ' + err.message
    } finally {
      this.loading = false
    }
  },
  methods: {
    async addEntry({ delta, note }) {
      const id = this.$route.params.id
      try {
        const res = await fetch('/api/gauges/' + id + '/entries', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ delta, note })
        })
        if (!res.ok) {
          this.error = 'Failed to add entry'
          return
        }
        const entry = await res.json()
        this.entries.push(entry)
        this.gauge.value = this.entries.reduce((sum, e) => sum + e.delta, 0)
      } catch (err) {
        this.error = 'Failed to add entry: ' + err.message
      }
    },
    async deleteEntry(entry) {
      const id = this.$route.params.id
      try {
        const res = await fetch('/api/gauges/' + id + '/entries/' + entry.id, { method: 'DELETE' })
        if (!res.ok) {
          this.error = 'Failed to delete entry'
          return
        }
        this.entries = this.entries.filter(e => e.id !== entry.id)
        this.gauge.value = this.entries.reduce((sum, e) => sum + e.delta, 0)
      } catch (err) {
        this.error = 'Failed to delete entry: ' + err.message
      }
    },
    async deleteGauge() {
      if (!confirm('Delete "' + this.gauge.name + '" and all its entries?')) return
      try {
        const res = await fetch('/api/gauges/' + this.gauge.id, { method: 'DELETE' })
        if (!res.ok) {
          this.error = 'Failed to delete gauge'
          return
        }
        this.$router.push('/gauge/list')
      } catch (err) {
        this.error = 'Failed to delete gauge: ' + err.message
      }
    },
    formatDate(dateStr) {
      return new Date(dateStr).toLocaleDateString(undefined, {
        year: 'numeric', month: 'short', day: 'numeric',
        hour: '2-digit', minute: '2-digit'
      })
    }
  }
}
</script>

<style scoped>
.gauge-detail {
  padding: 16px;
  max-width: 700px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}

.header h2 { margin: 0; }

.description {
  color: #666;
  font-size: 0.9rem;
  margin: 4px 0 0;
}

.delete-btn {
  padding: 6px 14px;
  background-color: #e74c3c;
  color: #fff;
  border: none;
  border-radius: 6px;
  font-weight: 600;
  cursor: pointer;
}

.delete-btn:hover { background-color: #c0392b; }

table {
  width: 100%;
  border-collapse: collapse;
  margin-top: 8px;
}

th, td {
  padding: 8px 12px;
  text-align: left;
  border-bottom: 1px solid #eee;
}

th { font-weight: 600; }

.positive { color: #27ae60; font-weight: 600; }
.negative { color: #e74c3c; font-weight: 600; }

.delete-entry-btn {
  background: none;
  border: none;
  color: #e74c3c;
  font-size: 1rem;
  cursor: pointer;
}

.delete-entry-btn:hover { color: #c0392b; }

.no-entries {
  color: #999;
  font-style: italic;
}

.error {
  color: #e74c3c;
  font-weight: 600;
}
</style>
