<template>
  <div id = "nav">
    <router-link to="/">Home</router-link> |
    <router-link to="/book/list">Books</router-link> |
    <router-link to="/language/list">Languages</router-link> |
    <router-link to="/series/list">Series</router-link> |
    <router-link to="/status/list">Statuses</router-link> |
    <router-link to="/tag/list">Tags</router-link>
  </div>
  <router-view/>
</template>