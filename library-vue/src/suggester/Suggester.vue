<template>
  <div class="suggester">
    <h1>Suggester</h1>
    <p>For each section, if no selection is specified, all options will be searched.</p>
    <form>
      <h4>Pages</h4>
      <div class="field">
        <label for="minPageCount">Mininum</label>
        <input id="minPageCount" v-model.number="form.minPageCount" type="number" />

        <label for="maxPageCount">Maximum</label>
        <input id="maxPageCount" v-model.number="form.maxPageCount" type="number" />

        <label for="includeNoPageCount">Do you want to include books with no page count?</label>
        <input type="checkbox" id="includeNoPageCount" v-model="form.includeNoPageCount" />
      </div>

      <ChipPicker
          label="Genres"
          :selected="form.selectedGenres"
          :excluded="form.excludedGenres"
          :items="genreList"
          create-endpoint="/api/genres"
          select-placeholder="Select genre"
          @update:selected="form.selectedGenres = $event"
          @update:excluded="form.excludedGenres = $event"
          @update:items="genreList = $event"
          @error="$emit('error', $event)"
          :canCreate="false"
      />

      <ChipPicker
          label="Tags"
          :selected="form.selectedTags"
          :excluded="form.excludedTags"
          :items="tagList"
          create-endpoint="/api/tags"
          select-placeholder="Select tags"
          @update:selected="form.selectedTags = $event"
          @update:excluded="form.excludedTags = $event"
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
          label="Status"
          :selected="form.selectedStatuses"
          :items="statusList"
          create-endpoint="/api/statuses"
          select-placeholder="Select statuses"
          @update:selected="form.selectedStatuses = $event"
          @update:items="statusList = $event"
          @error="$emit('error', $event)"
          :canCreate="false"
      />

      <div class="field">
        <label for="includeNoStatus">Do you want to include books with no status set?</label>
        <input type="checkbox" id="includeNoStatus" v-model="form.includeNoStatus" />
      </div>

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

    <div v-if="results" ref="results" class="results">
      <h2>Results ({{ results.length }})</h2>
      <p v-if="results.length === 0">No books match your criteria.</p>
      <div v-else class="table-wrap">
      <table>
        <thead>
          <tr>
            <th>Title</th>
            <th>Author</th>
            <th>Series</th>
            <th>Genres</th>
            <th>Languages</th>
            <th>Status</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="book in results" :key="book.id">
            <td data-label="Title"><router-link :to="'/book/edit/' + book.id">{{ book.title }}</router-link></td>
            <td data-label="Author">{{ book.authors?.map(a => a.name).join(', ') || '—' }}</td>
            <td data-label="Series">{{ book.series?.name || '—' }}</td>
            <td data-label="Genres">{{ book.genres?.map(g => g.name).join(', ') || '—' }}</td>
            <td data-label="Languages">{{ book.languages?.map(l => l.name).join(', ') || '—' }}</td>
            <td data-label="Status">{{ book._status || '—' }}</td>
          </tr>
        </tbody>
      </table>
      </div>
    </div>
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
        maxPageCount: 99999,
        includeNoPageCount: true,
        selectedLanguages: [],
        selectedTags: [],
        excludedTags: [],
        selectedGenres: [],
        excludedGenres: [],
        selectedStatuses: [],
        includeNoStatus: true,
        wantNewSeries: true,
        wantStartedSeries: true,
        wantStandalone: true
      },
      languageList: [],
      tagList: [],
      genreList: [],
      statusList: [],
      results: null,
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
      if (this.error) return
      this.results = null

      const payload = {
        minLength: this.form.minPageCount,
        maxLength: this.form.maxPageCount,
        includeNoPageCount: this.form.includeNoPageCount,
        languages: this.form.selectedLanguages,
        tags: this.form.selectedTags,
        excludedTags: this.form.excludedTags,
        genres: this.form.selectedGenres,
        excludedGenres: this.form.excludedGenres,
        statuses: this.form.selectedStatuses,
        includeNoStatus: this.form.includeNoStatus,
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

      this.results = await res.json()

      const statusPromises = this.results.map(book =>
        fetch('/api/books/' + book.id + '/status')
          .then(r => r.ok ? r.json() : null)
          .catch(() => null)
      )
      const statuses = await Promise.all(statusPromises)
      statuses.forEach((status, i) => {
        this.results[i]._status = status ? status.name : null
      })

      this.$nextTick(() => {
        this.$refs.results?.scrollIntoView({ behavior: 'smooth' })
      })
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

.results {
  margin-top: 32px;
}

.results h2 {
  margin-bottom: 12px;
}

.results table {
  width: 100%;
  border-collapse: collapse;
}

.results th, .results td {
  text-align: left;
  padding: 8px 12px;
  border-bottom: 1px solid #e0e0e0;
  font-size: 0.95rem;
}

.results th {
  font-weight: 600;
  color: #555;
}

.results a {
  color: #42b983;
  font-weight: 600;
  text-decoration: none;
}

.results a:hover {
  text-decoration: underline;
}

.table-wrap {
  overflow-x: auto;
  -webkit-overflow-scrolling: touch;
}

@media (max-width: 768px) {
  .suggester {
    max-width: none;
  }

  .field input[type="number"] {
    width: 80px;
  }

  .results table thead {
    display: none;
  }

  .results table tbody tr {
    display: block;
    border: 1px solid #e0e0e0;
    border-radius: 8px;
    margin-bottom: 12px;
    padding: 12px;
  }

  .results table tbody td {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 4px 0;
    border-bottom: none;
  }

  .results table tbody td::before {
    content: attr(data-label);
    font-weight: 600;
    color: #555;
    margin-right: 12px;
    flex-shrink: 0;
  }
}
</style>