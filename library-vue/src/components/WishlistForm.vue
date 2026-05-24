<template>
  <div>
    <div class="field">
      <label for="title">Title</label>
      <input id="title" v-model="form.title" required />
    </div>

    <AuthorPicker
      :selected="selectedAuthors"
      :items="authorList"
      @update:selected="selectedAuthors = $event"
      @update:items="authorList = $event"
      @error="$emit('error', $event)"
    />

    <div class="field">
      <label for="releaseDate">Release Date</label>
      <input id="releaseDate" v-model.number="form.releaseDate" type="date" />
    </div>

    <div class="field">
      <label for="notes">Notes</label>
      <textarea id="notes" v-model="form.notes" rows="4"></textarea>
    </div>

    <slot></slot>
  </div>
</template>

<script>
import ChipPicker from './ChipPicker.vue'
import AuthorPicker from './AuthorPicker.vue'

export default {
  components: { ChipPicker, AuthorPicker },
  props: {
    form: { type: Object, required: true }
  },
  emits: ['error'],
  data() {
    return {
      authorList: [],
      selectedAuthors: []
    }
  },
  methods: {
    async loadLookups() {
      const authorsRes = await fetch('/api/authors/all')
      if (authorsRes.ok) this.authorList = await authorsRes.json()
    },
    setSelections({ authors }) {
      if (authors) this.selectedAuthors = [...authors]
    },
    async preparePayload() {
      return {
        ...this.form,
        authorIds: this.selectedAuthors.map(a => a.id)
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
