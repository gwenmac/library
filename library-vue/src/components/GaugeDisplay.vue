<template>
  <div class="gauge-display">
    <div class="gauge-value" :class="{ positive: value > 0, negative: value < 0 }">
      {{ value > 0 ? '+' : '' }}{{ value }}
    </div>
    <div class="gauge-bar">
      <div class="gauge-track">
        <div class="gauge-center-line"></div>
        <div
          class="gauge-fill"
          :class="{ positive: value > 0, negative: value < 0 }"
          :style="fillStyle"
        ></div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  props: {
    value: { type: Number, default: 0 },
    min: { type: Number, default: -20 },
    max: { type: Number, default: 20 }
  },
  computed: {
    fillStyle() {
      const range = this.max - this.min
      const center = Math.abs(this.min) / range * 100
      const clamped = Math.max(this.min, Math.min(this.max, this.value))
      const width = Math.abs(clamped) / range * 100

      if (clamped >= 0) {
        return { left: center + '%', width: width + '%' }
      } else {
        return { left: (center - width) + '%', width: width + '%' }
      }
    }
  }
}
</script>

<style scoped>
.gauge-display {
  display: flex;
  align-items: center;
  gap: 10px;
}

.gauge-value {
  font-weight: 700;
  font-size: 1.1rem;
  min-width: 40px;
  text-align: right;
}

.gauge-value.positive { color: #27ae60; }
.gauge-value.negative { color: #e74c3c; }

.gauge-bar {
  flex: 1;
  min-width: 120px;
}

.gauge-track {
  position: relative;
  height: 12px;
  background: #eee;
  border-radius: 6px;
  overflow: hidden;
}

.gauge-center-line {
  position: absolute;
  left: 50%;
  top: 0;
  bottom: 0;
  width: 2px;
  background: #999;
  transform: translateX(-1px);
}

.gauge-fill {
  position: absolute;
  top: 2px;
  bottom: 2px;
  border-radius: 4px;
  transition: width 0.3s, left 0.3s;
}

.gauge-fill.positive { background: #27ae60; }
.gauge-fill.negative { background: #e74c3c; }
</style>
