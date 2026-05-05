<template>
  <div class="book-edit">
    <h2>Edit Book</h2>

    <p v-if="error" class="error">{{ error }}</p>
    <p v-else-if="loading">Loading...</p>

    <form v-else @submit.prevent="save">
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

      <div class="field" v-if="form.seriesId">
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

      <ChipPicker
          label="Language"
          :selected="selectedLanguages"
          :items="languageList"
          create-endpoint="/api/languages"
          select-placeholder="Select languages"
          new-placeholder="New language..."
          @update:selected="selectedLanguages = $event"
          @update:items="languageList = $event"
          @error="error = $event"
      />

      <EditionPicker
          ref="editionPicker"
          v-model="form.editionId"
          :edition-list="editionList"
          @update:edition-list="editionList = $event"
          @error="error = $event"
      />

      <div class="field">
        <label for="status">Status</label>
        <select id="status" v-model="form.statusId">
          <option :value="null">— No status —</option>
          <option v-for="s in statusList" :key="s.id" :value="s.id">{{ s.name }}</option>
        </select>
      </div>

      <hr class="section-divider" />

      <h3>Review</h3>

      <div class="field">
        <label>Rating</label>
        <div class="star-rating">
          <span
            v-for="n in 5"
            :key="n"
            class="star"
            :class="{ filled: n <= review.rating }"
            @click="review.rating = n"
          >★</span>
          <button v-if="review.rating" type="button" class="clear-rating" @click="review.rating = null">✕</button>
        </div>
      </div>

      <div class="field">
        <label for="reviewNotes">Notes</label>
        <textarea id="reviewNotes" v-model="review.notes" rows="4" placeholder="Your thoughts on this book..."></textarea>
      </div>

      <div class="actions">
        <button type="submit" :disabled="saving">{{ saving ? 'Saving...' : 'Save' }}</button>
        <button type="button" @click="$router.push('/book/list')">Cancel</button>
      </div>
    </form>
  </div>
</template>

<script>
import ChipPicker from '../components/ChipPicker.vue'
import SeriesPicker from '../components/SeriesPicker.vue'
import EditionPicker from '../components/EditionPicker.vue'

export default {
  components: { ChipPicker, SeriesPicker, EditionPicker },
  data() {
    return {
      form: {
        title: '',
        description: '',
        pageCount: null,
        seriesId: null,
        seriesOrder: null,
        statusId: null,
        editionId: null
      },
      authorList: [],
      selectedAuthors: [],
      seriesList: [],
      genreList: [],
      selectedGenres: [],
      languageList: [],
      selectedLanguages: [],
      editionList: [],
      statusList: [],
      review: {
        rating: null,
        notes: ''
      },
      loading: true,
      saving: false,
      error: null
    }
  },
  async mounted() {
    const id = this.$route.params.id
    try {
      const [bookRes, seriesRes, authorsRes, genresRes, languagesRes, editionsRes, statusesRes] = await Promise.all([
        fetch('/api/books/' + id),
        fetch('/api/series/all'),
        fetch('/api/authors/all'),
        fetch('/api/genres/all'),
        fetch('/api/languages/all'),
        fetch('/api/editions/all'),
        fetch('/api/statuses/all')
      ])

      if (!bookRes.ok) {
        this.error = 'Book not found (API returned ' + bookRes.status + ')'
        return
      }

      const book = await bookRes.json()
      this.form.title = book.title
      this.form.description = book.description || ''
      this.form.pageCount = book.pageCount
      this.form.seriesId = book.series ? book.series.id : null
      this.form.seriesOrder = book.seriesOrder
      this.form.statusId = book.bookStatus ? book.bookStatus.status.id : null
      this.form.editionId = book.edition ? book.edition.id : null
      this.selectedAuthors = book.authors ? [...book.authors] : []
      this.selectedGenres = book.genres ? [...book.genres] : []
      this.selectedLanguages = book.languages ? [...book.languages] : []
      if (book.review) {
        this.review.rating = book.review.rating
        this.review.notes = book.review.notes || ''
      }

      if (seriesRes.ok) this.seriesList = await seriesRes.json()
      if (authorsRes.ok) this.authorList = await authorsRes.json()
      if (genresRes.ok) this.genreList = await genresRes.json()
      if (languagesRes.ok) this.languageList = await languagesRes.json()
      if (editionsRes.ok) this.editionList = await editionsRes.json()
      if (statusesRes.ok) this.statusList = await statusesRes.json()
    } catch (err) {
      this.error = 'Failed to load book: ' + err.message
    } finally {
      this.loading = false
    }
  },
  methods: {
    async save() {
      this.saving = true
      this.error = null
      const id = this.$route.params.id
      try {
        await this.$refs.seriesPicker.createIfNeeded()
        if (this.error) return

        await this.$refs.editionPicker.createIfNeeded()
        if (this.error) return

        const payload = {
          ...this.form,
          authorIds: this.selectedAuthors.map(a => a.id),
          genreIds: this.selectedGenres.map(g => g.id),
          languageIds: this.selectedLanguages.map(l => l.id)
        }

        const res = await fetch('/api/books/' + id, {
          method: 'PUT',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(payload)
        })
        if (!res.ok) {
          this.error = 'Save failed (API returned ' + res.status + ')'
          return
        }

        // Save review
        const reviewPayload = {
          rating: this.review.rating,
          notes: this.review.notes || null
        }
        const reviewRes = await fetch('/api/books/' + id + '/review', {
          method: 'PUT',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(reviewPayload)
        })
        if (!reviewRes.ok) {
          this.error = 'Review save failed (API returned ' + reviewRes.status + ')'
          return
        }

        this.$router.push('/book/list')
      } catch (err) {
        this.error = 'Save failed: ' + err.message
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

.section-divider {
  margin: 24px 0 16px;
  border: none;
  border-top: 1px solid #e0e0e0;
}

.star-rating {
  display: flex;
  align-items: center;
  gap: 4px;
}

.star {
  font-size: 1.6rem;
  cursor: pointer;
  color: #ccc;
  transition: color 0.15s;
}

.star.filled {
  color: #f5a623;
}

.star:hover {
  color: #f5a623;
}

.clear-rating {
  background: none;
  border: none;
  color: #e74c3c;
  font-size: 0.85rem;
  font-weight: 600;
  cursor: pointer;
  margin-left: 8px;
}
</style>
