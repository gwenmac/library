<template>
  <div class="book-edit">
    <h2>Edit Book</h2>

    <p v-if="error" class="error">{{ error }}</p>
    <p v-if="loading">Loading...</p>

    <form v-show="!loading && !error" @submit.prevent="save">
      <BookForm
        ref="bookForm"
        :form="form"
        @error="error = $event"
      />

      <hr class="section-divider" />

      <div class="field">
        <label for="status">Status</label>
        <select id="status" v-model="statusId">
          <option :value="null">— No status —</option>
          <option v-for="s in statusList" :key="s.id" :value="s.id">{{ s.name }}</option>
        </select>
      </div>

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
        <button type="submit" :disabled="saving">{{ saving ? 'Saving...' : 'Update' }}</button>
        <button type="button" @click="$router.push('/book/list')">Cancel</button>
      </div>
    </form>
  </div>
</template>

<script>
import BookForm from '../components/BookForm.vue'

export default {
  components: { BookForm },
  data() {
    return {
      form: {
        title: '',
        description: '',
        pageCount: null,
        year: null,
        seriesId: null,
        seriesOrder: null,
        editionId: null
      },
      statusList: [],
      statusId: null,
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
      const bookRes = await fetch('/api/books/' + id)
      const statusesRes = await fetch('/api/statuses/all')
      if (!bookRes.ok) {
        this.error = 'Book not found (API returned ' + bookRes.status + ')'
        return
      }
      if (statusesRes.ok) this.statusList = await statusesRes.json()
      const book = await bookRes.json()
      this.form.title = book.title
      this.form.description = book.description || ''
      this.form.pageCount = book.pageCount
      this.form.year = book.year
      this.form.seriesId = book.series ? book.series.id : null
      this.form.seriesOrder = book.seriesOrder
      this.form.editionId = book.edition ? book.edition.id : null

      // Load current user's status separately
      try {
        const statusRes = await fetch('/api/books/' + id + '/status')
        if (statusRes.ok) {
          const status = await statusRes.json()
          this.statusId = status ? status.id : null
        }
      } catch {
        // No status yet — that's fine
      }

      // Load current user's review separately
      try {
        const reviewRes = await fetch('/api/books/' + id + '/review')
        if (reviewRes.ok) {
          const review = await reviewRes.json()
          if (review) {
            this.review.rating = review.rating
            this.review.notes = review.notes || ''
          }
        }
      } catch {
        // No review yet — that's fine
      }

      await this.$refs.bookForm.loadLookups()
      this.$refs.bookForm.setSelections({
        authors: book.authors,
        genres: book.genres,
        tags: book.tags,
        languages: book.languages
      })
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
        const payload = await this.$refs.bookForm.preparePayload()
        if (this.error) return

        const res = await fetch('/api/books/' + id, {
          method: 'PUT',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(payload)
        })
        if (!res.ok) {
          this.error = 'Save failed (API returned ' + res.status + ')'
          return
        }

        const statusPayload = {
          statusId: this.statusId
        }
        const statusRes = await fetch('/api/books/' + id + '/status', {
          method: 'PUT',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(statusPayload)
        })
        if (!statusRes.ok) {
          this.error = 'Status save failed (API returned ' + statusRes.status + ')'
          return
        }

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

.field textarea {
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

.section-divider {
  margin: 24px 0 16px;
  border: none;
  border-top: 1px solid #e0e0e0;
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
