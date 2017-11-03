<template>
<div>
  <div v-on:keyup.esc="cancel"
       v-bind:class="{ 'is-active': isActive }"
       class="modal">

    <!-- modal -->
    <div class="modal-background"></div>
    <div class="modal-card">
      <!-- header -->
      <header class="modal-card-head" style="text-align: center;">
        <div class="modal-card-title">
          Update tags for <span class="is-size-3">{{ book.title }}</span>
        </div>
        <button @click="cancel" class="delete" aria-label="close"></button>
      </header>

      <!-- content -->
      <section class="modal-card-body">

        <!-- tags for UserBook -->
        <h3 class="title is-4">Current tags</h3>
        <div class="tags">
          <span v-for="current in userBookTags"
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

      <!-- footer -->
      <footer class="modal-card-foot">
        <button class="button is-primary" @click="save">Save</button>
        <button class="button" @click="cancel">Cancel</button>
      </footer>
    </div>

  </div>
  </div>
</template>

<script>
   import _ from 'lodash'

   export default {
    /**
     * Set of incoming properties.
     *
     * active: is this modal active
     * book: book being looked at
     * userBook: userbook being edited
     * allTags: list of all tags in the system
     */
     props: [ 'active', 'book', 'userBook', 'allTags' ],
     // Data
     data () {
       return {
         /**
          * Is this modal active
          */
         isActive: this.active,
         /**
          * Get list of userbook's tags.
          * Make a copy of the list so we don't mutate the parent's UserBook.
          */
         userBookTags: this.userBook.tags.slice(),
         /**
          * all incoming tag names
          */
         allTagsAsList: this.allTags.map(function (o) { return o.name })
       }
     },
     // Methods
     methods: {
       /**
        * Save was clicked, send message to the owning component,
        * along with the current set of tags
        */
       save () {
         this.$emit('savedCalled', this.userBookTags)
         this.isActive = false
       },
       /**
        * Cancel was clicked, send message to the owning component
        */
       cancel () {
         this.$emit('cancelCalled')
         this.isActive = false
       },
       /**
        * Add tag to the currentUserBook
        */
       addTag (tagName) {
         // only add if it isn't already in lsit
         let result = _.includes(this.userBookTags, tagName)
         if (!result) {
           this.userBookTags.push(tagName)
         }
       },
       /**
        * Delete tag from the currentUserBook
        */
       deleteTag (tagName) {
         console.log('remove tag: ' + tagName)
         let index = this.userBookTags.indexOf(tagName)
         this.userBookTags.splice(index, 1)
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
         console.log('get tags')
         let result = _.difference(this.allTagsAsList, this.userBookTags)
         console.log('result:' + result)
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
         console.log('modal is active: ' + val)
         this.isActive = val
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
