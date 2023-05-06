<script>
import Home from './Home.vue'
import NotFound from './NotFound.vue'
import BookList from './book/List.vue'
import LanguageList from './language/List.vue'
import SeriesList from './series/List.vue'
import StatusList from './status/List.vue'
import TagList from './tag/List.vue'

const routes = {
  '/': Home,
  '/book/list': BookList,
  '/language/list': LanguageList,
  '/series/list': SeriesList,
  '/status/list': StatusList,
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
  <a href="#/book/list">Books</a> |
  <a href="#/language/list">Languages</a> |
  <a href="#/series/list">Series</a> |
  <a href="#/status/list">Statuses</a> |
  <a href="#/tag/list">Tags</a>
  <component :is="currentView" />
</template>