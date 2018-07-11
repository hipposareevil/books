<!-- Modal to get a URL from user  -->
<template>
  <div v-on:keyup.esc="cancel" v-bind:class="{ 'is-active': isActive }" class="modal">
    <!-- modal -->
    <div class="modal-background"></div>

    <div class="modal-card">

      <header class="modal-card-head" style="text-align: center;">
        <p class="modal-card-title">
          {{ message }}
        </p>
      </header>

      <section class="modal-card-body">
        <div v-if="isTextInput()">
          <input class="input"
                 type="text"
                 @keyup.enter="save"
                 placeholder="new value..."
                 v-model="newValue">
        </div>
        <div v-else>
          <textarea class="textarea"
                    v-model="newValue"
                    rows="25"
                    placeholder="new value...">
          </textarea>
        </div>
      </section>

      <footer class="modal-card-foot">
        <button class="button is-primary" @click="save">OK</button>
        <button class="button" @click="cancel">Cancel</button>
      </footer>
    </div>

  </div>
</template>

<script>
   export default {
    /**
     * active: is this modal active
     * message: Message to user
     * inputType: type of input to be used, 'text' or 'textarea'
     */
     props: [ 'active', 'message', 'inputType', 'initialValue' ],
    /**
     *
     */
     data () {
       return {
         isActive: false,
         theMessage: this.message,
         typeOfInput: this.inputType,
         newValue: ''
       }
     },
     methods: {
      /**
       * Save was clicked, send message to the owning component
       */
       save () {
         this.$emit('okclicked', this.newValue)
         this.isActive = false
       },
      /**
       * Cancel was clicked, send message to the owning component
       */
       cancel () {
         this.$emit('cancelclicked')
         this.isActive = false
       },
      /**
       * Return true if this is to be a text input
       */
       isTextInput () {
         if (this.typeOfInput === 'textarea') {
           return false
         } else {
           return true
         }
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
       message: function (val, oldVal) {
         this.theMessage = val
       },
       inputType: function (val, oldVal) {
         this.typeOfInput = val
       },
       initialValue: function (val, oldVal) {
         this.newValue = val
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
