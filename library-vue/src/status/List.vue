<script>
import DataTable from '../components/DataTable.vue'

export default {
  components: {
    DataTable
  },
  data: () => ({
    searchQuery: '',
    tableColumns: ['name'],
    tableData: []
  }),
  methods: {
    async getData() {
      const res = await fetch("http://localhost:8080/status/get");
      const finalRes = await res.json();
      this.tableData = finalRes;
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
  <router-link to="/status/edit">New</router-link>
  <DataTable
      :data="tableData"
      :columns="tableColumns"
      :filter-key="searchQuery">
  </DataTable>
</template>