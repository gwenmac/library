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
        title: "",
        englishSortTitle: "",
        ongoing: false,
        availableCount: 0,
        language: 1,
        furigana: null,
        lnLevel: null,
        unreadVols: "",
        inProgressVols: "",
        readVols: ""
      },
      languageOptions: [],
      statusOptions: []
    }
  },
  methods: {
    async getLanguages() {
      const res = await fetch("http://localhost:8080/language/get");
      const resJson = await res.json();
      this.languageOptions = resJson;
    },
    async save() {
      const bulkAddObject = {
        title: this.entry.title,
        englishSortTitle: this.entry.englishSortTitle,
        ongoing: this.entry.ongoing,
        availableCount: this.entry.availableCount,
        language: this.entry.language,
        furigana: this.entry.furigana,
        lnLevel: this.entry.lnLevel,
        unreadVols: this.entry.unreadVols,
        inProgressVols: this.entry.inProgressVols,
        readVols: this.entry.readVols
      };
      const requestOptions = {
        method: "PUT",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify(bulkAddObject)
      };
      await fetch("http://localhost:8080/book/bulkAdd", requestOptions)
      await router.push('/book/list');
    }
  },
  mounted() {
    this.getLanguages()
  }
}
</script>

<template>
  <form>
    <label>Series Title: <input v-model="entry.title"></label><br>

    <label>English Sort Title: <input v-model="entry.englishSortTitle"></label><br>

    <label for="ongoing">Is Ongoing?</label>
    <input type="checkbox" id="ongoing" v-model="entry.ongoing" /><br>

    <label>Available Count: <input v-model="entry.availableCount"></label><br>

    <label>Language:<model-list-select name="language"
                                       :list="languageOptions"
                                       v-model="entry.language"
                                       option-value="id"
                                       option-text="name">
    </model-list-select></label><br>

    <label>Unread Volume Numbers: <input v-model="entry.unreadVols"></label><br>
    <label>In Progress Volume Numbers: <input v-model="entry.inProgressVols"></label><br>
    <label>Read Volume Numbers: <input v-model="entry.readVols"></label><br>

    <!--    Only applicable if in Japanese-->
    <label for="furigana">Has Furigana?</label>
    <input type="checkbox" id="furigana" v-model="entry.furigana" /><br>
    <label>LearnNatively Level: <input v-model="entry.lnLevel"></label><br>

    <div class="buttons">
      <button type="submit" @click="save">Save</button>
    </div>
  </form>
</template>