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
      //todo get the id from the url if it exists, otherwise leave blank
      // should be able to get value from the router thingy
    },
    save() {
      const requestOptions = {
        method: "PUT",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify({id: this.id, name: this.name})
      };
      fetch("http://localhost:8080/language/upsert", requestOptions)
          .then(res => router.push('/language/list'));
    }
  },
  mounted() {
    this.getData()
  }
}
</script>

<template>
  <form>
    <label>Language Name: <input v-model="name"></label>
    <div class="buttons">
      <button type="submit" :value="name" @click="save">Save</button>
    </div>
  </form>
</template>