<template>
  <div class="book-edit">
    <h2>Add New Book</h2>

    <p v-if="error" class="error">{{ error }}</p>

    <form @submit.prevent="save">
      <BookForm
        ref="bookForm"
        :form="form"
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
import BookForm from '../components/BookForm.vue'

export default {
  components: { BookForm },
  data() {
    return {
      form: {
        title: '',
        description: '',
        pageCount: null,
        seriesId: null,
        seriesOrder: null,
        editionId: null
      },
      saving: false,
      error: null
    }
  },
  async mounted() {
    try {
      await this.$refs.bookForm.loadLookups()
      this.$refs.bookForm.defaultLanguageToEnglish()
    } catch (err) {
      console.error('Failed to load data:', err)
    }
  },
  methods: {
    async save() {
      this.saving = true
      this.error = null
      try {
        const payload = await this.$refs.bookForm.preparePayload()
        if (this.error) return

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
