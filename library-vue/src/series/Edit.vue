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
      id: null,
      title: "",
      englishSortTitle: "",
      ongoing: false,
      availableCount: 0,
      readAllOwned: false,
      ownAll: false,
      finished: false
    }
  },
  methods: {
    async getData() {
      if (this.$route.params.id) {
        const res = await fetch("http://localhost:8080/series/get?ids=" + this.$route.params.id)
        const resJson = await res.json();
        this.id = resJson[0].id;
        this.title = resJson[0].title;
        this.englishSortTitle = resJson[0].englishSortTitle;
        this.ongoing = resJson[0].ongoing;
        this.availableCount = resJson[0].availableCount;
        this.readAllOwned = resJson[0].readAllOwned;
        this.ownAll = resJson[0].ownAll;
        this.finished = resJson[0].finished;
      }
    },
    async save() {
      const requestOptions = {
        method: "PUT",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify(
            {
              id: this.id,
              title: this.title,
              englishSortTitle: this.englishSortTitle,
              ongoing: this.ongoing,
              availableCount: this.availableCount,
              readAllOwned: this.readAllOwned,
              ownAll: this.ownAll,
              finished: this.finished
            }
        )
      };
      if (this.id == null) await fetch("http://localhost:8080/series/insert", requestOptions)
      else await fetch("http://localhost:8080/series/update", requestOptions)
      await router.push('/series/list');
    }
  },
  mounted() {
    this.getData()
  }
}
</script>

<template>
  <form>
    <label>Series Title: <input v-model="title"></label><br>

    <label>English Sort Title: <input v-model="englishSortTitle"></label><br>

    <label for="ongoing">Ongoing?</label>
    <input type="checkbox" id="ongoing" v-model="ongoing" /><br>

    <label>Volumes Available: <input v-model="availableCount"></label><br>

    <label for="readAllOwned">Read all that you own?</label>
    <input type="checkbox" id="readAllOwned" v-model="readAllOwned" /><br>

    <label for="ownAll">Do you own all available?</label>
    <input type="checkbox" id="ownAll" v-model="ownAll" /><br>

    <label for="finished">Bought and read all volumes?</label>
    <input type="checkbox" id="finished" v-model="finished" /><br>

    <div class="buttons">
      <button type="submit" @click="save">Save</button><br>
    </div>
  </form>
</template>