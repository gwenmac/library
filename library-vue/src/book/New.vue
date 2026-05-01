<template>
  <div class="book-edit">
    <h2>Add New Book</h2>

    <p v-if="error" class="error">{{ error }}</p>

    <form @submit.prevent="save">
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
        @error="error = $event"
      />

      <div class="field">
        <label for="description">Description</label>
        <textarea id="description" v-model="form.description" rows="4"></textarea>
      </div>

      <div class="field">
        <label for="pageCount">Page Count</label>
        <input id="pageCount" v-model.number="form.pageCount" type="number" min="0" />
      </div>

      <SeriesPicker
        ref="seriesPicker"
        v-model="form.seriesId"
        :series-list="seriesList"
        @update:series-list="seriesList = $event"
        @error="error = $event"
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
        @error="error = $event"
      />

      <div class="actions">
        <button type="submit" :disabled="saving">{{ saving ? 'Saving...' : 'Add Book' }}</button>
        <button type="button" @click="$router.push('/book/list')">Cancel</button>
      </div>
    </form>
  </div>
</template>

<script>
import ChipPicker from '../components/ChipPicker.vue'
import SeriesPicker from '../components/SeriesPicker.vue'

export default {
  components: { ChipPicker, SeriesPicker },
  data() {
    return {
      form: {
        title: '',
        description: '',
        pageCount: null,
        seriesId: null,
        seriesOrder: null
      },
      authorList: [],
      selectedAuthors: [],
      seriesList: [],
      genreList: [],
      selectedGenres: [],
      saving: false,
      error: null
    }
  },
  async mounted() {
    try {
      const [seriesRes, authorsRes, genresRes] = await Promise.all([
        fetch('/api/series/all'),
        fetch('/api/authors/all'),
        fetch('/api/genres/all')
      ])
      if (seriesRes.ok) this.seriesList = await seriesRes.json()
      if (authorsRes.ok) this.authorList = await authorsRes.json()
      if (genresRes.ok) this.genreList = await genresRes.json()
    } catch (err) {
      console.error('Failed to load data:', err)
    }
  },
  methods: {
    async save() {
      this.saving = true
      this.error = null
      try {
        await this.$refs.seriesPicker.createIfNeeded()
        if (this.error) return

        const payload = {
          ...this.form,
          authorIds: this.selectedAuthors.map(a => a.id),
          genreIds: this.selectedGenres.map(g => g.id)
        }

        const res = await fetch('/api/books', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(payload)
        })
        if (!res.ok) {
          this.error = 'Failed to add book (API returned ' + res.status + ')'
          return
        }
        this.$router.push('/book/list')
      } catch (err) {
        this.error = 'Failed to add book: ' + err.message
      } finally {
        this.saving = false
      }
    }
  }
}
</script>

<style scoped>
.book-edit {
  padding: 16px;
  max-width: 600px;
}

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


.actions {
  display: flex;
  gap: 10px;
  margin-top: 18px;
}

.actions button {
  padding: 8px 20px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.95rem;
}

.actions button[type="submit"] {
  background-color: #42b983;
  color: #fff;
}

.actions button[type="submit"]:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.actions button[type="button"] {
  background-color: #e7e7e7;
  color: #333;
}

.error {
  color: #e74c3c;
  font-weight: 600;
}
</style>
