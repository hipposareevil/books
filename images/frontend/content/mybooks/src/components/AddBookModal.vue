<!-- Modal to add a book or author  -->
<template>
  <div v-on:keyup.esc="cancel"
       v-bind:class="{ 'is-active': isActive }" 
       class="modal">


    <!-- modal -->
    <div class="modal-background"></div>

    <div class="modal-card">

      <header class="modal-card-head" style="text-align: center;">
        <p class="modal-card-title">Add book
          <br>
          <span class="is-size-2">"{{bookTitle}}"</span>
          <br>
          to database?</p>
        <button @click="cancel" class="delete" aria-label="close"></button>
      </header>

      <section class="modal-card-body">
        <div class="content">
          <p>
            <input type="checkbox"
                   v-model="checked">
            Add to <span style="font-style: italic;">My Books</span>
          </p>
        </div>

        <div class="title is-5">Current tags</div>
        <div class="tags">
          <span v-for="current in tagsToAdd"
                class="tag is-info">
            {{current}}
            <button class="delete is-small"
                    @click="deleteTag(current)">
            </button>
          </span>
        </div>


        <!-- All Tags in system -->
        <div class="title is-5">All tags</div>
        <div class="tags">
          <span v-for="current in currentTagList"
                title="click to add to tags"
                @click="addTag(current)"
                class="tag clickable">
            {{current}}
          </span>
        </div>
        <!-- end of tag list -->

        
      </section>

      <footer class="modal-card-foot">
        <button class="button is-primary" @click="save">Yes</button>
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
     * title: title of book
     */
     props: [ 'active', 'title' ],
     mounted: function () {
       // Get tags
       this.getTags()
     },
     /**
      * Data
      */
     data () {
       return {
         // Is this modal active?
         isActive: '',
         // Title of book
         bookTitle: this.title,
         // Is add-to-user-books clicked
         checked: true,
         // All Tags
         TagJson: {},
         /**
          * Get list of userbook's tags.
          * Make a copy of the list so we don't mutate the parent's UserBook.
          */
         tagsToAdd: [],
         /**
          * all incoming tag names
          */
         allTagsAsList: []
       }
     },
     /**
      * Methods
      */
     methods: {
       /**
        * Get tags from database
        */
       getTags () {
         const authString = Auth.getAuthHeader()
         let self = this
         this.TagJson = {}
         this.$axios.get('/tag/', { headers: { Authorization: authString } })
           .then((response) => {
             self.TagJson = response.data.data
             // Sort
             self.TagJson = _.sortBy(self.TagJson, ['name'])

             // Set all tags as a list of just tag names
             self.allTagsAsList = this.TagJson.map(function (o) { return o.name })
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
        * Add tag to the currentUserBook
        */
       addTag (tagName) {
         // only add if it isn't already in lsit
         let result = _.includes(this.tagsToAdd, tagName)
         if (!result) {
           this.tagsToAdd.push(tagName)
         }
       },
       /**
        * Delete tag from the currentUserBook
        */
       deleteTag (tagName) {
         console.log('remove tag: ' + tagName)
         let index = this.tagsToAdd.indexOf(tagName)
         this.tagsToAdd.splice(index, 1)
       },

       /**
       * Save was clicked, send message to the owning component
       */
       save () {
         let AddUserBookInformation = {
           addFlag: this.checked,
           tags: this.tagsToAdd
         }
         this.$emit('saveClicked', AddUserBookInformation)
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
      * Computed values
      */
     computed: {
       currentTagList: function () {
         let result = _.difference(this.allTagsAsList, this.tagsToAdd)
         return result
       }
     },
     /**
      * Watch for the following variables
      *
      */
     watch: {
      // Watch the parent's 'active' value. When it changes
      // we change our 'isActive' value to match.
       active: function (val, oldVal) {
         this.isActive = val
       },
       title: function (val, oldVal) {
         this.bookTitle = val
       }
     },
    /**
     * Add listener for keys, especially 'escape'
     */
     created: function () {
       window.addEventListener('keyup', this.keychanged)
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
</style>
