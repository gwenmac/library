<template>
  <div class="gauge-list">
    <div class="list-header">
      <h2>Gauges</h2>
      <router-link to="/gauge/new" class="add-btn">+ Add Gauge</router-link>
    </div>

    <p v-if="error" class="error">{{ error }}</p>
    <p v-else-if="loading">Loading...</p>
    <p v-else-if="gauges.length === 0">No gauges yet. Create one to get started.</p>

    <div v-else class="gauge-cards">
      <div v-for="g in gauges" :key="g.id" class="gauge-card" @click="$router.push('/gauge/' + g.id)">
        <h3>{{ g.name }}</h3>
        <p v-if="g.description" class="description">{{ g.description }}</p>
        <GaugeDisplay :value="g.value" />
      </div>
    </div>
  </div>
</template>

<script>
import GaugeDisplay from '../components/GaugeDisplay.vue'

export default {
  components: { GaugeDisplay },
  data() {
    return {
      gauges: [],
      loading: true,
      error: null
    }
  },
  async mounted() {
    try {
      const res = await fetch('/api/gauges')
      if (!res.ok) {
        this.error = 'API returned ' + res.status
        return
      }
      this.gauges = await res.json()
    } catch (err) {
      this.error = 'Failed to load gauges: ' + err.message
    } finally {
      this.loading = false
    }
  }
}
</script>

<style scoped>
.gauge-list {
  padding: 16px;
}

.list-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
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

.gauge-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 16px;
}

.gauge-card {
  padding: 16px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  cursor: pointer;
  transition: box-shadow 0.2s;
}

.gauge-card:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.gauge-card h3 {
  margin: 0 0 6px;
}

.description {
  color: #666;
  font-size: 0.9rem;
  margin: 0 0 12px;
}

.error {
  color: #e74c3c;
  font-weight: 600;
}

@media (max-width: 768px) {
  .gauge-cards {
    grid-template-columns: 1fr;
  }

  .list-header {
    flex-direction: column;
    align-items: stretch;
    gap: 8px;
  }

  .add-btn {
    text-align: center;
  }
}
</style>
