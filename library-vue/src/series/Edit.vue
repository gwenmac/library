<script>
import router from "../router";

export default {
  data() {
    return {
      id: null,
      name: "",
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
        this.name = resJson[0].name;
        this.ongoing = resJson[0].ongoing;
        this.availableCount = resJson[0].availableCount;
        this.readAllOwned = resJson[0].readAllOwned;
        this.finished = resJson[0].finished;
      }
    },
    save() {
      const requestOptions = {
        method: "PUT",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify(
            {
              id: this.id,
              name: this.name,
              ongoing: this.ongoing,
              availableCount: this.availableCount,
              readAllOwned: this.readAllOwned,
              ownAll: this.ownAll,
              finished: this.finished
            }
        )
      };
      fetch("http://localhost:8080/series/upsert", requestOptions)
          .then(res => router.push('/series/list'));
    }
  },
  mounted() {
    this.getData()
  }
}
</script>

<template>
  <form>
    <label>Series Name: <input v-model="name"></label><br>

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