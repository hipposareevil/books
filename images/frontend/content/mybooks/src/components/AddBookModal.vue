<!-- Modal to add a book  -->
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

        <TagsForBookModal
          v-bind:checked="checked"
          v-on:tagListUpdated="tagListUpdated">
        </TagsForBookModal>

      </section>

      <footer class="modal-card-foot">
        <button class="button is-primary" @click="save">Yes</button>
        <button class="button" @click="cancel">Cancel</button>
      </footer>
    </div>

  </div>
</template>

<script>
   import TagsForBookModal from './TagsForBookModal.vue'

   export default {
    /**
     * Components used
     */
     components: { TagsForBookModal },
    /**
     * active: is this modal active
     * title: title of book
     */
     props: [ 'active', 'title' ],
    /**
     * When mounted
     */
     mounted: function () {
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
         /**
          * Get list of userbook's tags.
          * Make a copy of the list so we don't mutate the parent's UserBook.
          */
         tagsToAdd: []
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
       * Save was clicked, send message to the owning component
       */
       save () {
         let AddUserBookInformation = {
           addFlag: this.checked,
           tags: this.tagsToAdd
         }
         this.$emit('saveClicked', AddUserBookInformation)
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
