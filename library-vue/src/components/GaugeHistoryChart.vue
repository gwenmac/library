<template>
  <div class="gauge-history-chart" v-if="entries.length">
    <Line :data="chartData" :options="chartOptions" />
  </div>
  <p v-else class="no-data">No entries yet.</p>
</template>

<script>
import { Line } from 'vue-chartjs'
import {
  Chart as ChartJS,
  LineElement,
  PointElement,
  CategoryScale,
  LinearScale,
  Tooltip,
  Filler
} from 'chart.js'

ChartJS.register(LineElement, PointElement, CategoryScale, LinearScale, Tooltip, Filler)

export default {
  components: { Line },
  props: {
    entries: { type: Array, default: () => [] }
  },
  computed: {
    cumulativeData() {
      let sum = 0
      return this.entries.map(e => {
        sum += e.delta
        return { date: new Date(e.createdAt), value: sum }
      })
    },
    chartData() {
      const data = this.cumulativeData
      const labels = data.map(d =>
        d.date.toLocaleDateString(undefined, { month: 'short', day: 'numeric' })
      )
      const values = data.map(d => d.value)

      return {
        labels,
        datasets: [{
          label: 'Cumulative',
          data: values,
          borderColor: '#42b983',
          backgroundColor: 'rgba(66, 185, 131, 0.1)',
          fill: true,
          tension: 0.3,
          pointRadius: 3,
          pointBackgroundColor: '#42b983'
        }]
      }
    },
    chartOptions() {
      return {
        responsive: true,
        plugins: {
          legend: { display: false },
          tooltip: {
            callbacks: {
              label: (context) => `Value: ${context.raw}`
            }
          }
        },
        scales: {
          y: {
            beginAtZero: false,
            grid: { color: '#eee' }
          },
          x: {
            grid: { display: false }
          }
        }
      }
    }
  }
}
</script>

<style scoped>
.gauge-history-chart {
  width: 100%;
  max-width: 600px;
  margin: 16px 0;
}

.no-data {
  color: #999;
  font-style: italic;
}
</style>
