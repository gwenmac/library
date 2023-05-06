<script>
import DataTable from '../components/DataTable.vue'

export default {
  components: {
    DataTable
  },
  data: () => ({
    searchQuery: '',
    tableColumns: ['tagId', 'tagName'],
    tableData: [
      { tagId: '1', tagName: "ebook" },
      { tagId: '2', tagName: "novel" }
    ]
  })
}
</script>

<template>
  <form id="search">
    Search <input name="query" v-model="searchQuery">
  </form>
  <DataTable
      :data="tableData"
      :columns="tableColumns"
      :filter-key="searchQuery">
  </DataTable>
</template>