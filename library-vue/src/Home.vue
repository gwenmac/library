<template>
  <div class="home">
    <h1>Home Page</h1>

    <div class="gauge-summary" v-if="gauges.length">
      <h2>Gauges</h2>
      <div v-for="g in gauges" :key="g.id" class="gauge-card">
        <router-link :to="'/gauge/' + g.id">
          <strong>{{ g.name }}</strong>
          <GaugeDisplay :value="g.value" />
        </router-link>
      </div>
    </div>

    <div class="charts">
      <div class="chart-card">
        <h2>Books by Status</h2>
        <p v-if="loading">Loading...</p>
        <StatusPieChart v-else-if="books.length" />
      </div>
      <div class="chart-card">
        <h2>Books by Genre</h2>
        <p v-if="loading">Loading...</p>
        <GenreBarChart v-else-if="books.length" :books="books" />
      </div>
      <div class="chart-card">
        <h2>Total Book Count</h2>
        <p v-if="loading">Loading...</p>
        <Counter v-else-if="books.length" :books="books" />
      </div>
    </div>
  </div>
</template>

<script>
import GaugeDisplay from './components/GaugeDisplay.vue'
import StatusPieChart from './components/StatusPieChart.vue'
import GenreBarChart from './components/GenreBarChart.vue'
import Counter from './components/Counter.vue'

import { api } from './auth/api.js'

export default {
  components: { GaugeDisplay, StatusPieChart, GenreBarChart, Counter },
  data() {
    return { gauges: [], books: [], loading: true }
  },
  async mounted() {
    api('/gauges')
      .then(gauges => { this.gauges = gauges })
      .catch(err => { console.error('Failed to load gauges:', err) })

    api('/all')
      .then(books => { this.books = books })
      .catch(err => { console.error('Failed to load books:', err) })
      .finally(() => { this.loading = false })
  }
}
</script>

<style scoped>
.home {
  padding: 16px;
}

.charts {
  display: flex;
  gap: 32px;
  flex-wrap: wrap;
  margin-top: 24px;
  margin-bottom: 32px;
}

.chart-card h2 {
  margin-bottom: 12px;
}

.gauge-summary {
  margin-top: 24px;
}

.gauge-summary h2 {
  margin-bottom: 12px;
}

.gauge-card {
  margin-bottom: 12px;
}

.gauge-card a {
  display: block;
  padding: 12px 16px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  text-decoration: none;
  color: inherit;
  transition: box-shadow 0.2s;
}

.gauge-card a:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.gauge-card strong {
  display: block;
  margin-bottom: 8px;
}

@media (max-width: 768px) {
  .charts {
    flex-direction: column;
    gap: 20px;
  }

  .chart-card {
    width: 100%;
  }

  .gauge-card a {
    padding: 10px 12px;
  }
}
</style>