<template>
  <nav id="nav">
    <div class="nav-brand">📚 Library</div>
    <ul>
      <li><router-link to="/">Home</router-link></li>
      <li><router-link to="/book/list">Books</router-link></li>
      <li><router-link to="/gauge/list">Gauges</router-link></li>
    </ul>
  </nav>
  <main class="page-content">
    <router-view/>
  </main>
</template>
<script setup lang="ts">
</script>