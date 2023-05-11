<script>
import router from "../router";
import { ModelListSelect } from 'vue-search-select';
import "vue-search-select/dist/VueSearchSelect.css";

export default {
  components: {
    ModelListSelect
  },
  data() {
    return {
      entry: {
        id: null,
        title: "",
        series: null,
        volNum: null,
        language: null,
        furigana: false,
        lnLevel: null,
        englishSortName: "",
        status: null,
        startTs: null,
        completeTs: null
      },
      languageOptions: [],
      statusOptions: [],
      seriesOptions: []
    }
  },
  methods: {
    async getData() {
      if (this.$route.params.id) {
        const res = await fetch("http://localhost:8080/book/get?ids=" + this.$route.params.id)
        const resJson = await res.json();
        this.entry.id = resJson[0].id;
        this.entry.title = resJson[0].title;
        this.entry.series = resJson[0].series;
        this.entry.volNum = resJson[0].volNum;
        this.entry.language = resJson[0].language;
        this.entry.furigana = resJson[0].furigana;
        this.entry.lnLevel = resJson[0].lnLevel;
        this.entry.englishSortName = resJson[0].englishSortName;
        this.entry.status = resJson[0].status;
        this.entry.startTs = resJson[0].startTs;
        this.entry.completeTs = resJson[0].completeTs;
      }
    },
    async getLanguages() {
      const res = await fetch("http://localhost:8080/language/get");
      const resJson = await res.json();
      this.languageOptions = resJson;
    },
    async getStatuses() {
      const res = await fetch("http://localhost:8080/status/get");
      const resJson = await res.json();
      this.statusOptions = resJson;
    },
    async getSeries() {
      const res = await fetch("http://localhost:8080/series/get");
      const resJson = await res.json();
      this.seriesOptions = resJson;
    },
    async save() {
      console.log(this.entry.language);
      const bookObject = {
        id: this.entry.id,
        title: this.entry.title,
        series: this.entry.series,
        volNum: this.entry.volNum,
        language: this.entry.language,
        furigana: this.entry.furigana,
        lnLevel: this.entry.lnLevel,
        englishSortName: this.entry.englishSortName,
        status: this.entry.status,
        startTs: this.entry.startTs,
        completeTs: this.entry.completeTs
      };
      console.log(this.entry);
      const requestOptions = {
        method: "PUT",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify(bookObject)
      };
      if (this.entry.id == null) await fetch("http://localhost:8080/book/insert", requestOptions)
      else await fetch("http://localhost:8080/book/upsert", requestOptions)
      await router.push('/book/list');
    }
  },
  mounted() {
    this.getData()
    this.getLanguages()
    this.getStatuses()
    this.getSeries()
  }
}
</script>

<template>
  <form>
    <label>Book Title: <input v-model="entry.title"></label><br>

    <label>Series: <model-list-select name="series"
                                      :list="seriesOptions"
                                      v-model="entry.series"
                                      option-value="id"
                                      option-text="name">
    </model-list-select></label><br>

    <label>Language:<model-list-select name="language"
                       :list="languageOptions"
                       v-model="entry.language"
                      option-value="id"
                      option-text="name">
  </model-list-select></label><br>

    <label>Status:<model-list-select name="status"
                       :list="statusOptions"
                       v-model="entry.status"
                       option-value="id"
                       option-text="name">
    </model-list-select></label><br>

    <label>Started Reading On: <input type="date" v-model="entry.startTs"></label><br>
    <label>Finished Reading On: <input type="date" v-model="entry.completeTs"></label><br>

    <!--    Only applicable if series-->
    <label>Volume Number: <input v-model="entry.volNum"></label><br>

    <!--    Only applicable if in Japanese-->
    <label for="furigana">Has Furigana?</label>
    <input type="checkbox" id="furigana" v-model="entry.furigana" /><br>
    <label>LearnNatively Level: <input v-model="entry.lnLevel"></label><br>
    <label>English Version of Title: <input v-model="entry.englishSortName"></label>

    <div class="buttons">
      <button type="submit" @click="save">Save</button>
    </div>
  </form>
</template>