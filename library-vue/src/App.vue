<template>
  <nav id="nav" v-if="user">
    <div class="nav-brand">📚 Library</div>
    <ul>
      <li><router-link to="/">Home</router-link></li>
      <li><router-link to="/book/list">Books</router-link></li>
      <li><router-link to="/gauge/list">Gauges</router-link></li>
      <li><router-link to="/suggester">Suggester</router-link></li>
      <li v-if="user && user.role === 'admin'"><router-link to="/admin/users">Users</router-link></li>
      <li v-if="user && user.role === 'admin'"><router-link to="/admin/households">Households</router-link></li>
    </ul>
    <div class="nav-user">
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
    return { user: getUser() }
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
#nav {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 12px 16px;
  background: #f8f8f8;
  border-bottom: 1px solid #e0e0e0;
}

#nav .nav-brand {
  font-weight: 700;
  font-size: 1.1rem;
}

#nav ul {
  display: flex;
  list-style: none;
  gap: 16px;
  margin: 0;
  padding: 0;
  flex: 1;
}

#nav ul a {
  text-decoration: none;
  color: #333;
  font-weight: 500;
}

#nav ul a.router-link-active {
  color: #42b983;
}

.nav-user {
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-name {
  font-size: 0.9rem;
  font-weight: 500;
  color: #ccc;
}

.logout-btn {
  padding: 4px 12px;
  background: none;
  border: 1px solid #ccc;
  border-radius: 4px;
  font-size: 0.85rem;
  cursor: pointer;
  color: #ccc;
}

.logout-btn:hover {
  background: #eee;
}
</style>