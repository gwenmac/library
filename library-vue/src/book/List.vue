<script>
import DataTable from '../components/DataTable.vue'

export default {
  components: {
    DataTable
  },
  data: () => ({
    searchQuery: '',
    tableColumns: [
      'title',
      'englishSortTitle',
      'series',
      'volNum',
      'language',
      'furigana',
      'lnLevel',
      'status'
    ],
    tableData: []
  }),
  methods: {
    async getData() {
      const res = await fetch("http://localhost:8080/book/get");
      const rawJsonRes = await res.json();
      this.tableData = rawJsonRes.map(row => {
        return {
          id: row.id,
          title: row.title,
          englishSortTitle: row.englishSortTitle,
          series: (row.series? row.series.title : null),
          volNum: row.volNum,
          language: row.language.name,
          furigana: row.furigana,
          lnLevel: row.lnLevel,
          status: row.status.name,
          startTs: row.startTs,
          completeTs: row.completeTs
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
  <router-link to="/book/edit">New</router-link>
  <DataTable
      :data="tableData"
      :columns="tableColumns"
      :filter-key="searchQuery">
  </DataTable>
</template>