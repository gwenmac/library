<template>
  <div class="gauge-history-chart" v-if="entries.length">
    <svg :viewBox="viewBox" preserveAspectRatio="none" class="chart-svg">
      <!-- Zero line -->
      <line
        :x1="padding" :y1="zeroY"
        :x2="width - padding" :y2="zeroY"
        stroke="#999" stroke-dasharray="4,3" stroke-width="1"
      />
      <!-- Data line -->
      <polyline
        :points="polylinePoints"
        fill="none"
        stroke="#42b983"
        stroke-width="2"
        stroke-linejoin="round"
      />
      <!-- Data points -->
      <circle
        v-for="(pt, i) in points" :key="i"
        :cx="pt.x" :cy="pt.y" r="3"
        fill="#42b983"
      />
    </svg>
  </div>
  <p v-else class="no-data">No entries yet.</p>
</template>

<script>
export default {
  props: {
    entries: { type: Array, default: () => [] }
  },
  data() {
    return {
      width: 600,
      height: 200,
      padding: 30
    }
  },
  computed: {
    cumulativeData() {
      let sum = 0
      return this.entries.map(e => {
        sum += e.delta
        return { date: new Date(e.createdAt), value: sum }
      })
    },
    yMin() {
      const vals = this.cumulativeData.map(d => d.value)
      return Math.min(0, ...vals) - 1
    },
    yMax() {
      const vals = this.cumulativeData.map(d => d.value)
      return Math.max(0, ...vals) + 1
    },
    points() {
      const data = this.cumulativeData
      if (!data.length) return []
      const xStart = this.padding
      const xEnd = this.width - this.padding
      const yStart = this.padding
      const yEnd = this.height - this.padding
      const xRange = data.length > 1 ? data.length - 1 : 1
      const yRange = this.yMax - this.yMin

      return data.map((d, i) => ({
        x: xStart + (i / xRange) * (xEnd - xStart),
        y: yEnd - ((d.value - this.yMin) / yRange) * (yEnd - yStart)
      }))
    },
    polylinePoints() {
      return this.points.map(p => `${p.x},${p.y}`).join(' ')
    },
    zeroY() {
      const yStart = this.padding
      const yEnd = this.height - this.padding
      const yRange = this.yMax - this.yMin
      return yEnd - ((0 - this.yMin) / yRange) * (yEnd - yStart)
    },
    viewBox() {
      return `0 0 ${this.width} ${this.height}`
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

.chart-svg {
  width: 100%;
  height: 200px;
  border: 1px solid #eee;
  border-radius: 8px;
  background: #fafafa;
}

.no-data {
  color: #999;
  font-style: italic;
}
</style>
