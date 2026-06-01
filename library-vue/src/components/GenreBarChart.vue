<template>
  <div class="bar-chart-container">
    <Bar :data="chartData" :options="chartOptions" />
  </div>
</template>

<script>
import { Bar } from 'vue-chartjs'
import { Chart as ChartJS, BarElement, CategoryScale, LinearScale, Tooltip, Legend } from 'chart.js'

ChartJS.register(BarElement, CategoryScale, LinearScale, Tooltip, Legend)

export default {
  components: { Bar },
  props: {
    books: { type: Array, required: true }
  },
  computed: {
    chartData() {
      const counts = {}
      this.books.forEach(book => {
        if (book.genres && book.genres.length) {
          book.genres.forEach(genre => {
            counts[genre.name] = (counts[genre.name] || 0) + 1
          })
        } else {
          counts['No Genre'] = (counts['No Genre'] || 0) + 1
        }
      })

      const sorted = Object.entries(counts).sort((a, b) => b[1] - a[1])
      const labels = sorted.map(e => e[0])
      const data = sorted.map(e => e[1])

      const colors = [
        '#42b983', '#3498db', '#f5a623', '#e74c3c',
        '#9b59b6', '#1abc9c', '#e67e22', '#95a5a6'
      ]

      return {
        labels,
        datasets: [{
          label: 'Books',
          data,
          backgroundColor: colors.slice(0, labels.length)
        }]
      }
    },
    chartOptions() {
      return {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: { display: false },
          tooltip: {
            callbacks: {
              label: (context) => {
                const value = context.raw
                const total = this.books.length
                const pct = ((value / total) * 100).toFixed(1)
                return `${value} books (${pct}% of library)`
              }
            }
          }
        },
        scales: {
          y: {
            beginAtZero: true,
            ticks: { stepSize: 1 }
          }
        }
      }
    }
  }
}
</script>

<style scoped>
.bar-chart-container {
  width: 500px;
  height: 350px;
}
</style>
