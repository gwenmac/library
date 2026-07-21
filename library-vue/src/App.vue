<template>
  <nav id="nav" v-if="user">
    <div class="nav-brand">📚 Library</div>
    <button class="hamburger" @click="menuOpen = !menuOpen" aria-label="Toggle menu">
      <span :class="{ open: menuOpen }"></span>
    </button>
    <ul :class="{ open: menuOpen }">
      <li><router-link to="/" @click="menuOpen = false">Home</router-link></li>
      <li><router-link to="/book/list" @click="menuOpen = false">Books</router-link></li>
      <li><router-link to="/series" @click="menuOpen = false">Series</router-link></li>
      <li><router-link to="/wishlist/list" @click="menuOpen = false">Wishlist</router-link></li>
      <li><router-link to="/gauge/list" @click="menuOpen = false">Gauges</router-link></li>
      <li><router-link to="/suggester" @click="menuOpen = false">Suggester</router-link></li>
      <li v-if="user && user.role === 'admin'"><router-link to="/admin/users" @click="menuOpen = false">Users</router-link></li>
      <li v-if="user && user.role === 'admin'"><router-link to="/admin/households" @click="menuOpen = false">Households</router-link></li>
    </ul>
    <div class="nav-user" :class="{ open: menuOpen }">
      <span class="user-name">{{ user.displayName }}</span>
      <button class="logout-btn" @click="logout">Logout</button>
    </div>
  </nav>
  <main class="page-content">
    <router-view/>
  </main>
</template>
<script>
import { getUser, clearAuth } from './auth/api.js'

export default {
  data() {
    return { user: getUser(), menuOpen: false }
  },
  watch: {
    '$route'() {
      this.user = getUser()
    }
  },
  methods: {
    logout() {
      clearAuth()
      this.$router.push('/login')
    }
  }
}
</script>
<style>
/* Nav styles are in assets/main.css */
</style>