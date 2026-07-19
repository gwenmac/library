<template>
  <nav id="nav" v-if="user">
    <div class="nav-top">
      <div class="nav-brand">📚 Library</div>
      <button class="hamburger" @click="menuOpen = !menuOpen" aria-label="Toggle menu">
        <span :class="{ open: menuOpen }"></span>
      </button>
    </div>
    <div class="nav-menu" :class="{ open: menuOpen }">
      <ul>
        <li><router-link to="/" @click="menuOpen = false">Home</router-link></li>
        <li><router-link to="/book/list" @click="menuOpen = false">Books</router-link></li>
        <li><router-link to="/series" @click="menuOpen = false">Series</router-link></li>
        <li><router-link to="/wishlist/list" @click="menuOpen = false">Wishlist</router-link></li>
        <li><router-link to="/gauge/list" @click="menuOpen = false">Gauges</router-link></li>
        <li><router-link to="/suggester" @click="menuOpen = false">Suggester</router-link></li>
        <li v-if="user && user.role === 'admin'"><router-link to="/admin/users" @click="menuOpen = false">Users</router-link></li>
        <li v-if="user && user.role === 'admin'"><router-link to="/admin/households" @click="menuOpen = false">Households</router-link></li>
      </ul>
      <div class="nav-user">
        <span class="user-name">{{ user.displayName }}</span>
        <button class="logout-btn" @click="logout">Logout</button>
      </div>
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
#nav {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 12px 16px;
  background: #f8f8f8;
  border-bottom: 1px solid #e0e0e0;
}

#nav .nav-top {
  display: contents;
}

#nav .nav-brand {
  font-weight: 700;
  font-size: 1.1rem;
}

.hamburger {
  display: none;
}

#nav .nav-menu {
  display: contents;
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

/* Global table responsiveness */
table {
  width: 100%;
}

@media (max-width: 768px) {
  #nav {
    flex-direction: column;
    align-items: stretch;
    padding: 0;
  }

  #nav .nav-top {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 12px 16px;
  }

  .hamburger {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 36px;
    height: 36px;
    background: none;
    border: 1px solid #ccc;
    border-radius: 6px;
    cursor: pointer;
    position: relative;
  }

  .hamburger span,
  .hamburger span::before,
  .hamburger span::after {
    display: block;
    width: 18px;
    height: 2px;
    background: #333;
    border-radius: 1px;
    transition: all 0.25s ease;
  }

  .hamburger span::before,
  .hamburger span::after {
    content: '';
    position: absolute;
  }

  .hamburger span::before {
    transform: translateY(-6px);
  }

  .hamburger span::after {
    transform: translateY(6px);
  }

  .hamburger span.open {
    background: transparent;
  }

  .hamburger span.open::before {
    transform: rotate(45deg);
  }

  .hamburger span.open::after {
    transform: rotate(-45deg);
  }

  #nav .nav-menu {
    display: none;
    border-top: 1px solid #e0e0e0;
    padding: 12px 16px;
  }

  #nav .nav-menu.open {
    display: block;
  }

  #nav ul {
    flex-direction: column;
    gap: 0;
  }

  #nav ul li {
    border-bottom: 1px solid #f0f0f0;
  }

  #nav ul li:last-child {
    border-bottom: none;
  }

  #nav ul a {
    display: block;
    padding: 10px 0;
  }

  .nav-user {
    padding-top: 12px;
    border-top: 1px solid #e0e0e0;
    margin-top: 8px;
  }

  /* Global: make all tables scrollable on mobile */
  .table-wrap {
    overflow-x: auto;
    -webkit-overflow-scrolling: touch;
  }
}
</style>