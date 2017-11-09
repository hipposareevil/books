<!-- Modal to manually author  -->
<template>
  <div v-on:keyup.esc="cancel" v-bind:class="{ 'is-active': isActive }" class="modal">

    <!-- modal -->
    <div class="modal-background"></div>

    <div class="modal-card">
      <header class="modal-card-head" style="text-align: center;">
        <p class="modal-card-title">Add New Author</p>
        <button @click="cancel" class="delete" aria-label="close"></button>
      </header>

      <section class="modal-card-body">
        <div class="content">

          <!-- author name -->
          <div class="columns"
               style="height: 5em;">
            <div class="column">

              <div class="field">
                <label class="label">Author</label>
                <div class="control">
                  <input class="input"
                         type="text"
                         v-model="author.name"
                         v-on:input="debouncedSearch"
                         placeholder="Author Name">
                </div>
                <p v-if="authorExists"
                   class="help is-danger">Matches existing Author  <span style="font-weight: bold"> '{{nameOfExistingAuthor}}' </span>
                </p>
                <p v-else
                   class="help is-success">This is a new Author</p>
              </div>

            </div>
            <div class="column">
              <label class="label">Existing Authors</label>
              <ul style="margin-top: 0px;">
                <li v-for="(current, index) in shortAuthorList"
                    class="is-size-7 authors">
                  {{ current.name }}
                </li>
              </ul>
            </div>
          </div>
          <!-- end author name -->

          <!-- Birthdate -->
          <div class="field">
            <label class="label">Birthdate</label>
            <div class="control">
              <input class="input"
                     type="text"
                     v-model="author.birthDate"
                     placeholder="Birthdate">
            </div>
          </div>
          <!-- end birthdate -->

          <!-- ol key -->
          <div class="field">
            <label class="label">OpenLibrary.org key</label>
            <div class="control">
              <input class="input"
                     type="text"
                     v-model="author.olKey"
                     placeholder="OL key">
            </div>
          </div>
          <!-- end olkey -->


          <!-- goodreads -->
          <div class="field">
            <label class="label">Goodreads.com URL</label>
            <div class="control">
              <input class="input"
                     type="text"
                     v-model="author.goodreadsUrl"
                     placeholder="Goodreads URL">
            </div>
          </div>
          <!-- end goodreads -->


          <!-- Image url -->
          <div class="field">
            <label class="label">Image URL</label>
            <div class="control">
              <input class="input"
                     type="text"
                     v-model="author.imageMedium"
                     placeholder="Image URL">
            </div>
          </div>
          <!-- end image -->

          <!-- Subjects -->
          <div class="field">
            <label class="label">Subjects</label>
            <div class="control">
              <input class="input"
                     type="text"
                     v-model="subjectsAsCsv"
                     placeholder="Subjects, comma, separated">
            </div>
          </div>
          <!-- end subjects -->

        </div>

      </section>

      <footer class="modal-card-foot">
        <button class="button is-primary" @click="save">Save</button>
        <button class="button" @click="cancel">Cancel</button>
      </footer>
    </div>

  </div>
</template>

<script>
   import Auth from '../auth'
   import _ from 'lodash'

   export default {
    /**
     * active: is this modal active
     * name: Name of author
     */
     props: [ 'active' ],
     /**
      * This is called after this component is created.
      */
     created: function () {
       window.addEventListener('keyup', this.keychanged)
       this.debouncedSearch = _.debounce(this.search, 250)
     },
     /**
      * Data
      */
     data () {
       return {
         // Is this modal active?
         isActive: '',
         // New author data
         author: {
         },
         subjectsAsCsv: '',
         // authors from db
         existingAuthors: [],
         // name of existing author
         nameOfExistingAuthor: ''
       }
     },
     /**
      * Methods
      */
     methods: {
       /**
        * Search the /query endpoint for an author.
        * Place that data into authorJson
        */
       search () {
         let self = this
         const authString = Auth.getAuthHeader()

         // Get books from database
         this.existingAuthors = []

         let params = {
           name: this.author.name
         }
         let url = '/author'
         this.$axios.get(url, {
           headers: { Authorization: authString },
           params: params })
           .then((response) => {
             self.existingAuthors = response.data.data
           })
           .catch(function (error) {
             if (error.response.status === 401) {
               Event.$emit('got401')
             } else {
               console.log(error)
             }
           })
       },
       /**
        * Save was clicked, send message to the owning component
        */
       save () {
         // convert csv to array for subjects
         if (this.subjectsAsCsv) {
           this.author.subjects = this.subjectsAsCsv.split(',')
         }
         // save medium as small/large
         this.author.imageSmall = this.author.imageMedium
         this.author.imageLarge = this.author.imageMedium
         this.$emit('dunsaved', this.author)
         this.isActive = false
       },
       /**
        * Cancel was clicked, send message to the owning component
        */
       cancel () {
         this.$emit('duncanceled')
         this.isActive = false
       },
       /**
        * Check for escape key and call cancel
        */
       keychanged (event) {
         // Escape
         if (event.which === 27) {
           this.cancel()
         }
       }
     },
     /**
      * Computed
      */
     computed: {
       authorExists: function () {
         let self = this
         let exists = _.find(self.existingAuthors, function (o) { return o.name.toUpperCase() === self.author.name.toUpperCase() })
         if (exists) {
           self.nameOfExistingAuthor = exists.name
           return true
         } else {
           return false
         }
       },
       shortAuthorList: function () {
         if (this.existingAuthors) {
           return this.existingAuthors.slice(0, 5)
         }
       }
     },
     /**
      * Watch for the following variables:
      * - active
      */
     watch: {
      // Watch the parent's 'active' value. When it changes
      // we change our 'isActive' value to match.
       active: function (val, oldVal) {
         this.isActive = val
         if (this.isActive) {
           // Clear the author out
           this.author = {}
         }
       }
     }
   }
</script>

<style>
  
.authors {
  list-style: none;
  margin-left: -4em;
}
</style>
