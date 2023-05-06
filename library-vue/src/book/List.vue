<script>
import DataTable from '../components/DataTable.vue'

export default {
  components: {
    DataTable
  },
  data: () => ({
    searchQuery: '',
    tableColumns: [
      'id',
      'title',
      'series',
      'volNum',
      'language',
      'furigana',
      'lnLevel',
      'englishSortName',
      'status',
      'startTs',
      'completeTs'
    ],
    tableData: []
  }),
  methods: {
    async getData() {
      const res = await fetch("http://localhost:8080/book/get");
      const rawJsonRes = await res.json();
      console.log(rawJsonRes)
      this.tableData = rawJsonRes.map(row => {
        return {
          id: row.id,
          title: row.title,
          series: row.series.name,
          volNum: row.volNum,
          language: row.language.name,
          furigana: row.furigana,
          lnLevel: row.lnLevel,
          englishSortName: row.englishSortName,
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
  <DataTable
      :data="tableData"
      :columns="tableColumns"
      :filter-key="searchQuery">
  </DataTable>
</template>