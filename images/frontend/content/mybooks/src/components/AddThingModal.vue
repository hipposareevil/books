<!-- Modal to add a book or author  -->
<template>
  <div v-on:keyup.esc="cancel" v-bind:class="{ 'is-active': isActive }" class="modal">

    <!-- modal -->
    <div class="modal-background"></div>

    <div class="modal-card">

      <header class="modal-card-head" style="text-align: center;">
        <p class="modal-card-title">Add {{typeOfThing}}
          <br>
          <span class="is-size-3">"{{theThing}}"</span>
          <br>
          to database?</p>
        <button @click="cancel" class="delete" aria-label="close"></button>
      </header>

      <footer class="modal-card-foot">
        <button class="button is-primary" @click="save">Yes</button>
        <button class="button" @click="cancel">Cancel</button>
      </footer>
    </div>

  </div>
</template>

<script>
   export default {
    /**
     * active: is this modal active
     * whatType: What type of thing we are adding; book, author
     * thing: Name of thing being added
     */
     props: [ 'active', 'whatType', 'thing' ],
     data () {
       return { isActive: '', theThing: '', typeOfThing: this.whatType }
     },
     methods: {
      /**
       * Save was clicked, send message to the owning component
       */
       save () {
         this.$emit('dunsaved', 'foo')
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
      * Watch for the following variables
      *
      */
     watch: {
      // Watch the parent's 'active' value. When it changes
      // we change our 'isActive' value to match.
       active: function (val, oldVal) {
         this.isActive = val
       },
       thing: function (val, oldVal) {
         this.theThing = val
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

<style>

</style>
