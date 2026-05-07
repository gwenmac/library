<template>
  <div>
    <div class="field">
      <label for="title">Title</label>
      <input id="title" v-model="form.title" required />
    </div>

    <ChipPicker
      label="Authors"
      :selected="selectedAuthors"
      :items="authorList"
      create-endpoint="/api/authors"
      select-placeholder="Select author"
      new-placeholder="New author name..."
      @update:selected="selectedAuthors = $event"
      @update:items="authorList = $event"
      @error="$emit('error', $event)"
    />

    <div class="field">
      <label for="description">Description</label>
      <textarea id="description" v-model="form.description" rows="4"></textarea>
    </div>

    <div class="field">
      <label for="pageCount">Page Count</label>
      <input id="pageCount" v-model.number="form.pageCount" type="number" min="0" />
    </div>

    <div class="field">
      <label for="year">Year</label>
      <input id="year" v-model.number="form.year" type="number" />
    </div>

    <SeriesPicker
      ref="seriesPicker"
      v-model="form.seriesId"
      :series-list="seriesList"
      @update:series-list="seriesList = $event"
      @error="$emit('error', $event)"
    />

    <div class="field" v-if="form.seriesId || ($refs.seriesPicker && $refs.seriesPicker.creatingNew)">
      <label for="seriesOrder">Order in Series</label>
      <input id="seriesOrder" v-model.number="form.seriesOrder" type="number" min="1" />
    </div>

    <ChipPicker
      label="Genres"
      :selected="selectedGenres"
      :items="genreList"
      create-endpoint="/api/genres"
      select-placeholder="Select genre"
      new-placeholder="New genre name..."
      @update:selected="selectedGenres = $event"
      @update:items="genreList = $event"
      @error="$emit('error', $event)"
    />

    <ChipPicker
        label="Tags"
        :selected="selectedTags"
        :items="tagList"
        create-endpoint="/api/tags"
        select-placeholder="Select tag"
        new-placeholder="New tag name..."
        @update:selected="selectedTags = $event"
        @update:items="tagList = $event"
        @error="$emit('error', $event)"
    />

    <ChipPicker
      label="Language"
      :selected="selectedLanguages"
      :items="languageList"
      create-endpoint="/api/languages"
      select-placeholder="Select languages"
      new-placeholder="New language..."
      @update:selected="selectedLanguages = $event"
      @update:items="languageList = $event"
      @error="$emit('error', $event)"
    />

    <EditionPicker
      ref="editionPicker"
      v-model="form.editionId"
      :edition-list="editionList"
      @update:edition-list="editionList = $event"
      @error="$emit('error', $event)"
    />

    <div class="field" v-if="showStatus">
      <label for="status">Status</label>
      <select id="status" v-model="form.statusId">
        <option :value="null">— No status —</option>
        <option v-for="s in statusList" :key="s.id" :value="s.id">{{ s.name }}</option>
      </select>
    </div>

    <slot></slot>
  </div>
</template>

<script>
import ChipPicker from './ChipPicker.vue'
import SeriesPicker from './SeriesPicker.vue'
import EditionPicker from './EditionPicker.vue'

export default {
  components: { ChipPicker, SeriesPicker, EditionPicker },
  props: {
    form: { type: Object, required: true },
    showStatus: { type: Boolean, default: false }
  },
  emits: ['error'],
  data() {
    return {
      authorList: [],
      selectedAuthors: [],
      seriesList: [],
      genreList: [],
      tagList: [],
      selectedGenres: [],
      selectedTags: [],
      languageList: [],
      selectedLanguages: [],
      editionList: [],
      statusList: []
    }
  },
  methods: {
    async loadLookups() {
      const [seriesRes, authorsRes, genresRes, tagsRes, languagesRes, editionsRes, statusesRes] = await Promise.all([
        fetch('/api/series/all'),
        fetch('/api/authors/all'),
        fetch('/api/genres/all'),
        fetch('/api/tags/all'),
        fetch('/api/languages/all'),
        fetch('/api/editions/all'),
        fetch('/api/statuses/all')
      ])
      if (seriesRes.ok) this.seriesList = await seriesRes.json()
      if (authorsRes.ok) this.authorList = await authorsRes.json()
      if (genresRes.ok) this.genreList = await genresRes.json()
      if (tagsRes.ok) this.tagList = await tagsRes.json()
      if (languagesRes.ok) this.languageList = await languagesRes.json()
      if (editionsRes.ok) this.editionList = await editionsRes.json()
      if (statusesRes.ok) this.statusList = await statusesRes.json()
    },
    setSelections({ authors, genres, tags, languages }) {
      if (authors) this.selectedAuthors = [...authors]
      if (genres) this.selectedGenres = [...genres]
      if (tags) this.selectedTags = [...tags]
      if (languages) this.selectedLanguages = [...languages]
    },
    defaultLanguageToEnglish() {
      const english = this.languageList.find(l => l.name.toLowerCase() === 'english')
      if (english) this.selectedLanguages = [english]
    },
    async preparePayload() {
      await this.$refs.seriesPicker.createIfNeeded()
      await this.$refs.editionPicker.createIfNeeded()

      return {
        ...this.form,
        authorIds: this.selectedAuthors.map(a => a.id),
        genreIds: this.selectedGenres.map(g => g.id),
        tagIds: this.selectedTags.map(g => g.id),
        languageIds: this.selectedLanguages.map(l => l.id)
      }
    }
  }
}
</script>

<style scoped>
.field {
  margin-bottom: 14px;
}

.field label {
  display: block;
  font-weight: 600;
  margin-bottom: 4px;
}

.field input,
.field textarea,
.field select {
  width: 100%;
  padding: 8px 10px;
  border: 1px solid #ccc;
  border-radius: 6px;
  font-size: 0.95rem;
}
</style>
