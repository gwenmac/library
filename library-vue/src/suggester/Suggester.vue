<template>
  <div class="suggester">
    <h1>Suggester</h1>
    <p>If you leave a field blank, it will search with any.</p>
    <form>
      <div class="field">
        <h4>Page Count</h4>
        <label for="minPageCount">Minimum</label>
        <input id="minPageCount" v-model.number="form.minPageCount" type="number" />

        <label for="maxPageCount">Maximum</label>
        <input id="maxPageCount" v-model.number="form.maxPageCount" type="number" />
      </div>

      <ChipPicker
          label="Genres"
          :selected="form.selectedGenres"
          :items="form.genreList"
          create-endpoint="/api/genres"
          select-placeholder="Select genre"
          @update:selected="form.selectedGenres = $event"
          @update:items="form.genreList = $event"
          @error="$emit('error', $event)"
          :canCreate="false"
      />

      <ChipPicker
          label="Tags"
          :selected="form.selectedTags"
          :items="form.tagList"
          create-endpoint="/api/tags"
          select-placeholder="Select tags"
          @update:selected="form.selectedTags = $event"
          @update:items="form.tagList = $event"
          @error="$emit('error', $event)"
          :canCreate="false"
      />

      <h4>Series</h4>
      <div class="field">
        <label for="wantNewSeries">Do you want to read a new series?</label>
        <input type="checkbox" id="wantNewSeries" v-model="form.wantNewSeries" />
      </div>
      <div class="field">
        <label for="wantStartedSeries">Do you want to read a series you've already started?</label>
        <input type="checkbox" id="wantStartedSeries" v-model="form.wantStartedSeries" />
      </div>
      <div class="field">
        <label for="wantStandalone">Do you want to read standalone?</label>
        <input type="checkbox" id="wantStandalone" v-model="form.wantStandalone" />
      </div>

      <ChipPicker
          label="Status"
          :selected="form.selectedStatuses"
          :items="form.statusList"
          create-endpoint="/api/statuses"
          select-placeholder="Select statuses"
          @update:selected="form.selectedStatuses = $event"
          @update:items="form.statusList = $event"
          @error="$emit('error', $event)"
          :canCreate="false"
      />

      <ChipPicker
          label="Language"
          :selected="form.selectedLanguages"
          :items="form.languageList"
          create-endpoint="/api/languages"
          select-placeholder="Select languages"
          @update:selected="form.selectedLanguages = $event"
          @update:items="form.languageList = $event"
          @error="$emit('error', $event)"
          :canCreate="false"
      />

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
        statusList: [],
        wantNewSeries: true,
        wantStartedSeries: true,
        wantStandalone: true
      }
    }
  },
  async mounted() {
    const [statusRes, genresRes, tagsRes, languagesRes] = await Promise.all([
      fetch('/api/statuses/all'),
      fetch('/api/genres/all'),
      fetch('/api/tags/all'),
      fetch('/api/languages/all')
    ])
    if (statusRes.ok) this.form.statusList = await statusRes.json()
    if (genresRes.ok) this.form.genreList = await genresRes.json()
    if (tagsRes.ok) this.form.tagList = await tagsRes.json()
    if (languagesRes.ok) this.form.languageList = await languagesRes.json()

    // Default to To Be Read
    const tbr = this.form.statusList.find(l => l.name.toLowerCase() === 'to be read')
    if (tbr) this.form.selectedStatuses = [tbr]

    // Default to English
    const english = this.form.languageList.find(l => l.name.toLowerCase() === 'english')
    if (english) this.form.selectedLanguages = [english]
  },
  methods: {
    suggest() {
      console.log("suggesting...")
    }
  }
}
</script>

<style scoped>
.suggester {
  padding: 16px;
  max-width: 600px;
}

.suggester h1 {
  margin-bottom: 20px;
}

.field {
  margin-bottom: 16px;
}

.field h3 {
  margin-bottom: 8px;
}

.field label {
  display: inline-block;
  margin-right: 8px;
  font-size: 0.9rem;
  color: #555;
}

.field input[type="number"] {
  width: 100px;
  padding: 8px 10px;
  border: 1px solid #ccc;
  border-radius: 6px;
  font-size: 0.95rem;
  margin-right: 16px;
}

.field input[type="checkbox"] {
  display: block;
  margin: 10px 0;
}

.actions {
  display: flex;
  gap: 10px;
  margin-top: 24px;
}

.actions button {
  padding: 8px 20px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.95rem;
  background-color: #42b983;
  color: #fff;
  transition: background-color 0.2s;
}

.actions button:hover {
  background-color: #38a373;
}
</style>