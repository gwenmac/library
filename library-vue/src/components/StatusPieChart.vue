<template>
  <div class="pie-chart-container">
    <Pie v-if="loaded" :data="chartData" :options="chartOptions" />
  </div>
</template>

<script>
import { Pie } from 'vue-chartjs'
import { Chart as ChartJS, ArcElement, Tooltip, Legend } from 'chart.js'
import { api } from '../auth/api.js'

ChartJS.register(ArcElement, Tooltip, Legend)

export default {
  components: { Pie },
  data() {
    return { statusCounts: [], loaded: false }
  },
  async mounted() {
    try {
      this.statusCounts = await api('/books/status-counts')
    } catch {
      this.statusCounts = []
    }
    this.loaded = true
  },
  computed: {
    chartData() {
      const labels = this.statusCounts.map(s => s.status)
      const data = this.statusCounts.map(s => s.count)

      const colors = [
        '#42b983', '#e74c3c', '#f5a623', '#3498db',
        '#9b59b6', '#1abc9c', '#95a5a6', '#e67e22'
      ]

      return {
        labels,
        datasets: [{
          data,
          backgroundColor: colors.slice(0, labels.length)
        }]
      }
    },
    chartOptions() {
      return {
        responsive: true,
        plugins: {
          legend: { position: 'bottom' },
          tooltip: {
            callbacks: {
              label: (context) => {
                const label = context.label || ''
                const value = context.raw
                const total = context.dataset.data.reduce((a, b) => a + b, 0)
                const pct = ((value / total) * 100).toFixed(1)
                return `${label}: ${value} (${pct}%)`
              }
            }
          }
        }
      }
    }
  }
}
</script>

<style scoped>
.pie-chart-container {
  max-width: 360px;
}
</style>
