<template>
  <div class="book-edit">
    <h2>Add New Wishlist Book</h2>

    <p v-if="error" class="error">{{ error }}</p>

    <form @submit.prevent="save">
      <WishlistForm
          ref="wishlistForm"
          :form="form"
          @error="error = $event"
      />

      <div class="actions">
        <button type="submit" :disabled="saving">{{ saving ? 'Saving...' : 'Add Book' }}</button>
        <button type="button" @click="$router.push('/wishlist/list')">Cancel</button>
      </div>
    </form>
  </div>
</template>

<script>
import WishlistForm from '../components/WishlistForm.vue'

export default {
  components: { WishlistForm },
  data() {
    return {
      form: {
        title: '',
        notes: '',
        releaseDate: null
      },
      saving: false,
      error: null
    }
  },
  async mounted() {
    try {
      await this.$refs.wishlistForm.loadLookups()
    } catch (err) {
      console.error('Failed to load data:', err)
    }
  },
  methods: {
    async save() {
      this.saving = true
      this.error = null
      try {
        const payload = await this.$refs.wishlistForm.preparePayload()
        if (this.error) return

        const res = await fetch('/api/wishlist', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(payload)
        })
        if (!res.ok) {
          this.error = 'Failed to add wishlist book (API returned ' + res.status + ')'
          return
        }
        this.$router.push('/wishlist/list')
      } catch (err) {
        this.error = 'Failed to add wishlist book: ' + err.message
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
