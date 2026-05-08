<template>
  <div class="suggester">
    <h1>Suggester</h1>
    <form>
      <div class="field">
        <h3>Page Count</h3>
        <label for="minPageCount">Minimum</label>
        <input id="minPageCount" v-model.number="form.minPageCount" type="number" />

        <label for="maxPageCount">Maximum</label>
        <input id="maxPageCount" v-model.number="form.maxPageCount" type="number" />
      </div>



      <div class="actions">
        <button type="button" @click="suggest()">Suggest</button>
      </div>
    </form>
  </div>
</template>

<script>
import ChipPicker from '../components/ChipPicker.vue'

export default {
  components: { ChipPicker },
  data() {
    return {
      form: {
        minPageCount: 0,
        maxPageCount: 2000,
        selectedLanguages: [],
        selectedTags: [],
        selectedGenres: [],
        selectedStatuses: [],
        languageList: [],
        tagList: [],
        genreList: [],
        statusList: []
      }
    }
  },
  methods: {
    suggest() {
      console.log("suggesting...")
    }
  }
}
</script>