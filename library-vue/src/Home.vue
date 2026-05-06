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

    <div class="charts" v-if="books.length">
      <div class="chart-card">
        <h2>Books by Status</h2>
        <StatusPieChart :books="books" />
      </div>
      <div class="chart-card">
        <h2>Books by Genre</h2>
        <GenreBarChart :books="books" />
      </div>
    </div>
  </div>
</template>

<script>
import GaugeDisplay from './components/GaugeDisplay.vue'
import StatusPieChart from './components/StatusPieChart.vue'
import GenreBarChart from './components/GenreBarChart.vue'

export default {
  components: { GaugeDisplay, StatusPieChart, GenreBarChart },
  data() {
    return { gauges: [], books: [] }
  },
  async mounted() {
    try {
      const [gaugeRes, booksRes] = await Promise.all([
        fetch('/api/gauges'),
        fetch('/api/all')
      ])
      if (gaugeRes.ok) this.gauges = await gaugeRes.json()
      if (booksRes.ok) this.books = await booksRes.json()
    } catch (err) {
      console.error('Failed to load home data:', err)
    }
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
</style>