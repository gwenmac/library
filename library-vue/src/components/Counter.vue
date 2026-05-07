<template>
  <div class="counter-container">
    {{ books.length }}
  </div>
</template>

<script>
export default {
  props: {
    books: { type: Array, required: true }
  }
}
</script>

<style scoped>
.counter-container {
  padding: 12px 16px;
  background: #f8f8f8;
  border-bottom: 1px solid #e0e0e0;
  color: #333333;
  font-size: 25px;
  font-weight: bold;
}
</style>