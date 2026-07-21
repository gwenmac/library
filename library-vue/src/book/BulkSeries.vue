<template>
  <div class="book-edit">
    <h2>Bulk Add Series</h2>

    <p v-if="error" class="error">{{ error }}</p>
    <p v-if="successMsg" class="success">{{ successMsg }}</p>

    <form @submit.prevent="save">
      <BookForm
        ref="bookForm"
        :form="form"
        :hide-title="true"
        :hide-series-order="true"
        @error="error = $event"
      />

      <hr class="section-divider" />

      <h3>Volume Range</h3>
      <p class="hint">
        Each volume will be created as a separate book with the series order set to its volume number.
      </p>

      <div class="volume-range">
        <div class="field">
          <label for="volumeStart">First Volume #</label>
          <input id="volumeStart" v-model.number="volumeStart" type="number" min="1" required />
        </div>
        <div class="field">
          <label for="volumeEnd">Last Volume #</label>
          <input id="volumeEnd" v-model.number="volumeEnd" type="number" min="1" required />
        </div>
      </div>

      <hr class="section-divider" />

      <div class="field">
        <label for="status">Status</label>
        <select id="status" v-model="statusId">
          <option :value="null">— No status —</option>
          <option v-for="s in statusList" :key="s.id" :value="s.id">{{ s.name }}</option>
        </select>
      </div>

      <div class="actions">
        <button type="submit" :disabled="saving">
          {{ saving ? `Creating... (${progress}/${total})` : 'Add Volumes' }}
        </button>
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
      volumeStart: 1,
      volumeEnd: 1,
      statusList: [],
      statusId: null,
      saving: false,
      progress: 0,
      total: 0,
      error: null,
      successMsg: null
    }
  },
  async mounted() {
    try {
      await this.$refs.bookForm.loadLookups()
      const statusesRes = await fetch('/api/statuses/all')
      if (statusesRes.ok) this.statusList = await statusesRes.json()
      this.$refs.bookForm.defaultLanguageToEnglish()
    } catch (err) {
      console.error('Failed to load data:', err)
    }
  },
  methods: {
    async save() {
      this.saving = true
      this.error = null
      this.successMsg = null

      if (this.volumeStart > this.volumeEnd) {
        this.error = 'First volume must be less than or equal to last volume.'
        this.saving = false
        return
      }

      if (this.volumeEnd - this.volumeStart > 99) {
        this.error = 'Cannot create more than 100 volumes at once.'
        this.saving = false
        return
      }

      try {
        const payload = await this.$refs.bookForm.preparePayload()
        if (this.error) {
          this.saving = false
          return
        }

        if (!payload.seriesId) {
          this.error = 'A series is required for bulk manga creation.'
          this.saving = false
          return
        }

        const seriesName = this.$refs.bookForm.seriesList.find(s => s.id === payload.seriesId)?.name || 'Unknown'

        this.total = this.volumeEnd - this.volumeStart + 1
        this.progress = 0

        for (let vol = this.volumeStart; vol <= this.volumeEnd; vol++) {
          const volumePayload = {
            ...payload,
            title: `${seriesName} Vol. ${vol}`,
            seriesOrder: vol
          }

          const res = await fetch('/api/books', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(volumePayload)
          })
          if (!res.ok) {
            this.error = `Failed to create volume ${vol} (API returned ${res.status})`
            return
          }
          const book = await res.json()

          if (this.statusId) {
            const statusRes = await fetch('/api/books/' + book.id + '/status', {
              method: 'PUT',
              headers: { 'Content-Type': 'application/json' },
              body: JSON.stringify({ statusId: this.statusId })
            })
            if (!statusRes.ok) {
              this.error = `Status save failed for volume ${vol} (API returned ${statusRes.status})`
              return
            }
          }

          this.progress = vol - this.volumeStart + 1
        }

        this.successMsg = `Successfully created ${this.total} manga volume(s)!`
        this.$router.push('/book/list')
      } catch (err) {
        this.error = 'Failed to create volumes: ' + err.message
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

.volume-range {
  display: flex;
  gap: 16px;
}

.volume-range .field {
  flex: 1;
  min-width: 0;
}

.hint {
  font-size: 0.85rem;
  color: #666;
  margin-bottom: 12px;
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

.success {
  color: #42b983;
  font-weight: 600;
}

.section-divider {
  margin: 24px 0 16px;
  border: none;
  border-top: 1px solid #e0e0e0;
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
  box-sizing: border-box;
}
</style>
