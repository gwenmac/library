<template>
  <div class="pie-chart-container">
    <Pie :data="chartData" :options="chartOptions" />
  </div>
</template>

<script>
import { Pie } from 'vue-chartjs'
import { Chart as ChartJS, ArcElement, Tooltip, Legend } from 'chart.js'

ChartJS.register(ArcElement, Tooltip, Legend)

export default {
  components: { Pie },
  props: {
    books: { type: Array, required: true }
  },
  data() {
    return { statusMap: {} }
  },
  async mounted() {
    // Fetch per-user status for each book
    const promises = this.books.map(book =>
      fetch('/api/books/' + book.id + '/status')
        .then(res => res.ok ? res.json() : null)
        .catch(() => null)
    )
    const statuses = await Promise.all(promises)
    const map = {}
    statuses.forEach((status, i) => {
      map[this.books[i].id] = status ? status.name : null
    })
    this.statusMap = map
  },
  computed: {
    chartData() {
      const counts = {}
      this.books.forEach(book => {
        const status = this.statusMap[book.id] || 'No Status'
        counts[status] = (counts[status] || 0) + 1
      })

      const labels = Object.keys(counts)
      const data = Object.values(counts)

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
