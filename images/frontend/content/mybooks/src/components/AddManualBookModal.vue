<!-- Modal to add manually add book  -->
<template>
  <div v-on:keyup.esc="cancel"
       v-bind:class="{ 'is-active': active }" 
       class="modal">


    <!-- modal -->
    <div class="modal-background"></div>

    <div class="modal-card">

      <header class="modal-card-head" style="text-align: center;">
        <p class="modal-card-title">Add New Book</p>
        <button
          @click="cancel"
          class="delete"
          aria-label="close"></button>
      </header>


      <section class="modal-card-body">

        <!-- Title -->
        <div class="columns"
             style="height: 5em;">
          <div class="column">

            <div class="field">
              <label class="label">Title</label>
              <div class="control">
                <input class="input"
                       type="text"
                       v-model="book.title"
                       v-on:input="debouncedSearchTitle"
                       placeholder="Title">
              </div>
              <p v-if="existingTitle"
                 class="help is-danger">Matches existing Book <span style="font-weight: bold"> '{{ existingTitle }}' </span>
              </p>

            </div>

          </div>
          <div class="column">
            <label class="label">Existing Books</label>
            <ul style="margin-top: 0px;">
              <li v-for="(current, index) in shortBookList"
                  class="is-size-7 titles">
                {{ current.title }}
              </li>
            </ul>
          </div>
        </div>

        <!-- author name -->
        <div class="columns"
             style="height: 5em;">
          <div class="column">

            <div class="field">
              <label class="label">Author</label>
              <div class="control">
                <input class="input"
                       type="text"
                       v-model="authorName"
                       v-on:input="debouncedSearch"
                       placeholder="Author Name">
              </div>
            </div>

          </div>
          <div class="column">
            <label class="label">Existing Authors</label>
            <ul style="margin-top: 0px;">
              <li v-for="(current, index) in shortAuthorList"
                  class="is-size-7 authors">
                <a @click="setAuthor(current)">
                  {{ current.name }}
                </a>
              </li>
            </ul>
          </div>
        </div>
        <!-- end author name -->

        <!-- book date -->
        <div class="field">
          <label class="label">Published Year</label>
          <div class="control">
            <input class="input"
                   type="text"
                   v-model="book.firstPublishedYear"
                   placeholder="Published Year">
          </div>
        </div>
        <!-- end birthdate -->

        <!-- isbns -->
        <div class="field">
          <label class="label">ISBNs</label>
          <div class="control">
            <input class="input"
                   type="text"
                   v-model="isbnsAsCsv"
                   placeholder="ISBNs, comma, separated">
          </div>
        </div>
        <!-- end isbns -->

        <!-- Image url -->
        <div class="field">
          <label class="label">Image URL</label>
          <div class="control">
            <input class="input"
                   type="text"
                   v-model="book.imageMedium"
                   placeholder="Image URL">
          </div>
        </div>
        <!-- end image -->

        <!-- goodreads -->
        <div class="field">
          <label class="label">Goodreads.com URL</label>
          <div class="control">
            <input class="input"
                   type="text"
                   v-model="book.goodreadsUrl"
                   placeholder="Goodreads URL">
          </div>
        </div>
        <!-- end goodreads -->

        <!-- openlibrary -->
        <div class="field">
          <label class="label">openlibrary.org Work URL</label>
          <div class="control">
            <input class="input"
                   type="text"
                   v-model="book.openlibraryWorkUrl"
                   placeholder="Openlibrary.org URL">
          </div>
        </div>
        <!-- end openlibrary -->

        <!-- description -->
        <div class="field">
          <label class="label">Description</label>
          <div class="control">
            <textarea class="textarea is-small"
                      type="text"
                      v-model="book.description"
                      placeholder="Description">
            </textarea>
          </div>
        </div>
        <!-- end description -->


        <!-- add to my books -->
        <div class="content"
             style="margin-bottom: 0px;">
            <input type="checkbox"
                   v-model="checked">
            Add to <span style="font-style: italic;">My Books</span>
        </div>

        <TagsForBookModal
          v-bind:checked="checked"
          v-on:tagListUpdated="tagListUpdated">
        </TagsForBookModal>
 
      </section>
      <!-- end main body -->

      <footer class="modal-card-foot">

        <button class="button is-primary" @click="save">Yes</button>
        <button class="button" @click="cancel">Cancel</button>

        <button class="button"
                @click="clear">Clear</button>
      </footer>
    </div>

  </div>
</template>

<script>
   import Auth from '../auth'
   import _ from 'lodash'
   import TagsForBookModal from './TagsForBookModal.vue'

   export default {
    /**
     * Components used
     */
     components: { TagsForBookModal },
    /**
     * active: is this modal active
     */
     props: [ 'active' ],
    /**
     * Add listener for keys, especially 'escape'
     */
     created: function () {
       window.addEventListener('keyup', this.keychanged)
       this.debouncedSearch = _.debounce(this.search, 150)
       this.debouncedSearchTitle = _.debounce(this.searchTitle, 150)
     },
     /**
      * Data
      */
     data () {
       return {
         // Is this modal active?
         isActive: '',
         // Is add-to-user-books clicked
         checked: true,
         /**
          * Book to create
          */
         book: {
           title: '',
           authorId: '',
           description: '',
           firstPublishedYear: '',
           openlibraryWorkUrl: '',
           goodreadsUrl: '',
           imageMedium: ''
         },
         // List of userbook's tags.
         tagsToAdd: [],
         // title of already existing book
         existingTitle: '',
         // books from db
         existingBooks: [],
         // authors from db
         existingAuthors: [],
         // author name
         authorName: '',
         // isbns as csv
         isbnsAsCsv: ''
       }
     },
     /**
      * Methods
      */
     methods: {
       /**
        * list of selected tags was updated via TagsForBooKModal
        */
       tagListUpdated (taglist) {
         this.tagsToAdd = taglist
       },
       /**
        * Search the /author endpoint for an author.
        */
       search () {
         // clear out author
         this.book.authorId = ''

         let self = this
         const authString = Auth.getAuthHeader()

         // Get books from database
         this.existingAuthors = []

         let params = {
           name: this.authorName
         }
         let url = '/author'
         this.$axios.get(url, {
           headers: { Authorization: authString },
           params: params })
           .then((response) => {
             self.existingAuthors = response.data.data

             // See if we have a match
             let matching = _.find(self.existingAuthors, function (o) { return o.name.toUpperCase() === self.authorName.toUpperCase() })
             if (matching) {
               self.book.authorId = matching.id
             }
           })
           .catch(function (error) {
             if (error.response) {
               if (error.response.status === 401) {
                 Event.$emit('got401')
               } else {
                 console.log(error)
               }
             }
           })
       },
       /**
        * Search the /book endpoint for an book.
        */
       searchTitle () {
         let self = this
         const authString = Auth.getAuthHeader()

         // Get books from database
         this.existingBooks = []
         this.existingTitle = ''

         let params = {
           title: this.book.title
         }
         let url = '/book'
         this.$axios.get(url, {
           headers: { Authorization: authString },
           params: params })
           .then((response) => {
             self.existingBooks = response.data.data

             // See if we have a match
             let matching = _.find(self.existingBooks, function (o) { return o.title.toUpperCase() === self.book.title.toUpperCase() })
             if (matching) {
               self.existingTitle = matching.title
             }
           })
           .catch(function (error) {
             if (error.response) {
               if (error.response.status === 401) {
                 Event.$emit('got401')
               } else {
                 console.log(error)
               }
             }
           })
       },
       /**
        * Set the authors name
        */
       setAuthor (current) {
         this.authorName = current.name
         this.book.authorId = current.id
       },
       /**
        * Save was clicked, send message to the owning component
        */
       save () {
         // convert csv to array for isbns
         if (this.isbnsAsCsv) {
           this.book.isbns = this.isbnsAsCsv.split(',')
         }
         this.book.authorName = this.authorName
         this.book.imageSmall = this.book.imageMedium
         this.book.imageLarge = this.book.imageMedium

         // Send tags
         let AddUserBookInformation = {
           addFlag: this.checked,
           tags: this.tagsToAdd
         }

         this.$emit('saveClicked', this.book, AddUserBookInformation)
         this.tagsToAdd = []
         this.isActive = false
       },
      /**
       * Cancel was clicked, send message to the owning component
       */
       cancel () {
         this.$emit('cancelClicked')
         this.isActive = false
       },
      /**
       * Clear was clicked
       */
       clear () {
         // clear book
         this.book.authorId = ''
         this.book.title = ''
         this.book.description = ''
         this.book.firstPublishedYear = ''
         this.book.goodreadsUrl = ''
         this.book.openlibraryWorkUrl = ''

         this.tagsToAdd = []
         this.existingTitle = ''
         this.existingBooks = []
         this.authorName = ''
         Event.$emit('resetTags')
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
       shortAuthorList: function () {
         if (this.existingAuthors) {
           return this.existingAuthors.slice(0, 5)
         }
       },
       shortBookList: function () {
         if (this.existingBooks) {
           return this.existingBooks.slice(0, 4)
         }
       }
     },
     /**
      * Watch for the following variables
      */
     watch: {
      // Watch the parent's 'active' value. When it changes
      // we change our 'isActive' value to match.
       active: function (val, oldVal) {
         this.isActive = val
         if (val === true) {
           this.clear()
         }
       }
     }
   }
</script>

<style scoped>
div {
cursor: auto
}
.clickable {
cursor: pointer
}
  
.authors {
  list-style: none;
    margin-left: 2px;
}
  
.titles {
    list-style: none;
    margin-left: 2px;
}
</style>
