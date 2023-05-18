<script>
import router from "../router";

export default {
  data() {
    return {
      id: null,
      name: ""
    }
  },
  methods: {
    async getData() {
      if (this.$route.params.id) {
        const res = await fetch("http://localhost:8080/status/get?ids=" + this.$route.params.id)
        const resJson = await res.json();
        this.id = resJson[0].id;
        this.name = resJson[0].name;
      }
    },
    async save() {
      const requestOptions = {
        method: "PUT",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify({id: this.id, name: this.name})
      };
      await fetch("http://localhost:8080/status/update", requestOptions)
      await router.push('/status/list');
    }
  },
  mounted() {
    this.getData()
  }
}
</script>

<template>
  <form>
    <label>Status Name: <input v-model="name"></label>
    <div class="buttons">
      <button type="submit" @click="save">Save</button>
    </div>
  </form>
</template>