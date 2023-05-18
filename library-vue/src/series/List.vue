<script>
import DataTable from '../components/DataTable.vue'

export default {
  components: {
    DataTable
  },
  data: () => ({
    searchQuery: '',
    tableColumns: ['title', 'englishSortTitle',  'ongoing', 'availableCount', 'readAllOwned', 'ownAll', 'finished'],
    tableData: []
  }),
  methods: {
    async getData() {
      const res = await fetch("http://localhost:8080/series/get");
      const finalRes = await res.json();
      this.tableData = finalRes.map(row => {
        return {
          id: row.id,
          title: row.title,
          englishSortTitle: row.englishSortTitle,
          ongoing: row.ongoing,
          availableCount: row.availableCount,
          readAllOwned: row.readAllOwned,
          ownAll: row.ownAll,
          finished: row.finished
        }
      });
    }
  },
  mounted() {
    this.getData()
  }
}
</script>

<template>
  <form id="search">
    Search <input name="query" v-model="searchQuery">
  </form>
  <router-link to="/series/edit">New</router-link>
  <DataTable
      :data="tableData"
      :columns="tableColumns"
      :filter-key="searchQuery">
  </DataTable>
</template>