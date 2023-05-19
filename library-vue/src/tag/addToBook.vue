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
        volNum: "",
      },
      tagOptions: [],
      currentTags: [],
      newTagId: null
    }
  },
  methods: {
    async getData() {
      if (this.$route.params.id) {
        const bookRes = await fetch("http://localhost:8080/book/get?ids=" + this.$route.params.id)
        const bookResJson = await bookRes.json();
        this.entry.id = bookResJson[0].id;
        this.entry.title = bookResJson[0].title;
        this.entry.volNum = bookResJson[0].volNum;

        const bookTagRes = await fetch("http://localhost:8080/bookTag/getByBook?bookId=" + this.$route.params.id)
        const bookTagJson = await bookTagRes.json();
        this.currentTags = bookTagJson.map(r => r.tag.name);
      }
    },
    async getTags() {
      const res = await fetch("http://localhost:8080/tag/get");
      const resJson = await res.json();
      this.tagOptions = resJson;
    },
    async save() {
      const tagBookObject = {
        bookId: this.entry.id,
        tagId: this.newTagId
      };
      const requestOptions = {
        method: "PUT",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify(tagBookObject)
      };
      await fetch("http://localhost:8080/bookTag/insert", requestOptions)
      await router.go(0);
    }
  },
  mounted() {
    this.getData()
    this.getTags()
  }
}
</script>

<template>
  <form>
    <label>Book Title: {{ entry.title }} {{ entry.volNum }}</label><br>

    <label>Current tags: {{ currentTags.join(", ") }}</label><br>

    <label>Tag to add:<model-list-select name="newTagId"
                                       :list=tagOptions
                                       v-model="newTagId"
                                       option-value="id"
                                       option-text="name">
    </model-list-select></label><br>

    <div class="buttons">
      <button type="submit" @click="save">Save</button>
    </div>
  </form>
</template>