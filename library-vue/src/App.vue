<script>
import Home from './Home.vue'
import TagList from './components/tag/List.vue'
import NotFound from './NotFound.vue'

const routes = {
  '/': Home,
  '/tag/list': TagList
}

export default {
  data() {
    return {
      currentPath: window.location.hash
    }
  },
  computed: {
    currentView() {
      return routes[this.currentPath.slice(1) || '/'] || NotFound
    }
  },
  mounted() {
    window.addEventListener('hashchange', () => {
      this.currentPath = window.location.hash
    })
  }
}
</script>

<template>
  <a href="#/">Home</a> |
  <a href="#/tag/list">Tag List</a>
  <component :is="currentView" />
</template>