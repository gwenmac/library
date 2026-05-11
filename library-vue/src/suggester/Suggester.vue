<template>
  <div class="suggester">
    <h1>Suggester WIP</h1>
    <p>If you leave a field blank, it will search with any.</p>
    <form>
      <div class="field">
        <h4>Pages</h4>
        <label for="minPageCount">Min</label>
        <input id="minPageCount" v-model.number="form.minPageCount" type="number" />

        <label for="maxPageCount">Max</label>
        <input id="maxPageCount" v-model.number="form.maxPageCount" type="number" />

        <div class="field">
          <label for="includeNoPageCount">Do you want to include books with no page count?</label>
          <input type="checkbox" id="includeNoPageCount" v-model="form.includeNoPageCount" />
        </div>
      </div>

      <ChipPicker
          label="Genres"
          :selected="form.selectedGenres"
          :items="genreList"
          create-endpoint="/api/genres"
          select-placeholder="Select genre"
          @update:selected="form.selectedGenres = $event"
          @update:items="genreList = $event"
          @error="$emit('error', $event)"
          :canCreate="false"
      />

      <ChipPicker
          label="Tags"
          :selected="form.selectedTags"
          :items="tagList"
          create-endpoint="/api/tags"
          select-placeholder="Select tags"
          @update:selected="form.selectedTags = $event"
          @update:items="tagList = $event"
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
          label="Status (will also include books without a status)"
          :selected="form.selectedStatuses"
          :items="statusList"
          create-endpoint="/api/statuses"
          select-placeholder="Select statuses"
          @update:selected="form.selectedStatuses = $event"
          @update:items="statusList = $event"
          @error="$emit('error', $event)"
          :canCreate="false"
      />

      <ChipPicker
          label="Language"
          :selected="form.selectedLanguages"
          :items="languageList"
          create-endpoint="/api/languages"
          select-placeholder="Select languages"
          @update:selected="form.selectedLanguages = $event"
          @update:items="languageList = $event"
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
        maxPageCount: null,
        includeNoPageCount: true,
        selectedLanguages: [],
        selectedTags: [],
        selectedGenres: [],
        selectedStatuses: [],
        wantNewSeries: true,
        wantStartedSeries: true,
        wantStandalone: true
      },
      languageList: [],
      tagList: [],
      genreList: [],
      statusList: [],
      error: null
    }
  },
  async mounted() {
    const [statusRes, genresRes, tagsRes, languagesRes] = await Promise.all([
      fetch('/api/statuses/all'),
      fetch('/api/genres/all'),
      fetch('/api/tags/all'),
      fetch('/api/languages/all')
    ])
    if (statusRes.ok) this.statusList = await statusRes.json()
    if (genresRes.ok) this.genreList = await genresRes.json()
    if (tagsRes.ok) this.tagList = await tagsRes.json()
    if (languagesRes.ok) this.languageList = await languagesRes.json()

    // Default to To Be Read
    const tbr = this.statusList.find(l => l.name.toLowerCase() === 'to be read')
    if (tbr) this.form.selectedStatuses = [tbr]

    // Default to English
    const english = this.languageList.find(l => l.name.toLowerCase() === 'english')
    if (english) this.form.selectedLanguages = [english]
  },
  methods: {
    async suggest() {
      console.log("suggesting...")
      if (this.error) return

      const payload = {
        minLength: this.form.minPageCount,
        maxLength: this.form.maxPageCount,
        languages: this.form.selectedLanguages,
        tags: this.form.selectedTags,
        genres: this.form.selectedGenres,
        statuses: this.form.statusList,
        wantNewSeries: this.form.wantNewSeries,
        wantStartedSeries: this.form.wantStartedSeries,
        wantStandalone: this.form.wantStandalone
      }

      const res = await fetch('/api/suggester', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      })

      if (!res.ok) {
        this.error = 'Suggest failed (API returned ' + res.status + ')'
        return
      }

      const books = await res.json()
      console.log(books)
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

.field:has(input[type="checkbox"]) {
  display: flex;
  align-items: center;
}

.field input[type="checkbox"] {
  order: 1;
  margin: 0 0 0 8px;
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