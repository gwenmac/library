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
  </div>
</template>

<script>
import GaugeDisplay from './components/GaugeDisplay.vue'

export default {
  components: { GaugeDisplay },
  data() {
    return { gauges: [] }
  },
  async mounted() {
    try {
      const res = await fetch('/api/gauges')
      if (res.ok) this.gauges = await res.json()
    } catch (err) {
      console.error('Failed to load gauges:', err)
    }
  }
}
</script>

<style scoped>
.home {
  padding: 16px;
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