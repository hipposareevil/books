<!-- Modal to verify an action -->
<!-- 
attributes on this modal:
- active
- tagToDelete

events sent back to caller:
- verifyCalled  (tag id)
- verifyCanceled

-->

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
          Delete the tag "{{ whichTagToDelete.name }}"?
        </div>
        <button @click="cancel" class="delete" aria-label="close"></button>
      </header>

      <!-- content -->
      <section class="modal-card-body">
        This will remove all "{{ whichTagToDelete.name }}" tags from user's books.
      </section>

      <!-- footer -->
      <footer class="modal-card-foot">
        <button class="button is-primary" @click="save">Yes</button>
        <button class="button" @click="cancel">No</button>
      </footer>
    </div>

  </div>
  </div>
</template>

<script>
   export default {
    /**
     * Set of incoming properties.
     *
     * active: is this modal active
     * message: Message to print
     */
     props: [ 'active', 'tagToDelete' ],
     // Data
     data () {
       return {
         /**
          * Is this modal active
          */
         isActive: this.active,
         /**
          * tag to delete
          */
         whichTagToDelete: this.tagToDelete
       }
     },
     // Methods
     methods: {
       /**
        * Save was clicked, send message to the owning component,
        * along with the current set of tags
        */
       save () {
         this.$emit('verifyCalled', this.whichTagToDelete.id)
         this.isActive = false
       },
       /**
        * Cancel was clicked, send message to the owning component
        */
       cancel () {
         this.$emit('verifyCanceled')
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
         console.log('modal is active: ' + val)
         this.isActive = val
       },
       tagToDelete: function (val, oldVal) {
         console.log('set tag')
         this.whichTagToDelete = val
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
