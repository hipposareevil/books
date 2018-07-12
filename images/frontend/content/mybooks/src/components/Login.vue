
<template>
  <div v-on:keyup.esc="cancel" v-bind:class="{ 'is-active': isActive }" class="modal">
    <div class="modal-background"></div>

    <div class="modal-card">
      <header class="modal-card-head">
        <h1 class="modal-card-title">Books Login</h1>
        <button @click="cancel" class="delete" aria-label="close"></button>
      </header>

      <section class="modal-card-body">
        <div class="alert alert-danger" v-if="error">
          <p>{{ error }}</p>
        </div>

        <!-- name -->
        <div class="field">
          <label class="label">Name</label>
          <div class="control has-icons-left">
            <input class="input is-medium"
                   style="width: 80%;"
                   type="text"
                   placeholder="Your name"
                   v-model="credentials.name">
            <span class="icon is-medium is-left">
              <i class="fab fa-grav"></i>
            </span>
          </div>
        </div>

        <!-- password -->
        <div class="field">
          <p class="control has-icons-left">
            <input class="input is-medium"
                   style="width: 80%;"
                   type="password"
                   placeholder="Password"
                   @keyup.enter="submit"
                   v-model="credentials.password">
            <span class="icon is-medium is-left">
              <i class="fa fa-lock"></i>
            </span>
          </p>
        </div>

      </section>

      <!-- buttons -->
      <footer class="modal-card-foot">
        <div class="field is-grouped is-grouped-left">

          <!-- OK -->
          <p class="control">
            <button class="button is-success"
                    @click="submit">
              Login
            </button>
          </p>

          <!-- Cancel -->
          <p class="control">
            <button class="button is-light"
                    @click="cancel">
              Cancel
            </button>
          </p>
        </div> <!-- end field -->
      </footer>

    </div> <!-- modal-card -->
  </div>
</template>


<script>
  import Auth from '../auth'

  export default {
    /**
     * 'active' property is passed in to denote if this modal
     * is viewed or not
     */
    props: [ 'active' ],
    data () {
      return {
        // are we active?
        isActive: '',
        // Credentials in the input form
        credentials: {
          name: '',
          password: ''
        },
        // error to user
        error: ''
      }
    },
    methods: {
      /**
       * Submit the log in, go to the Auth component.
       *
       */
      submit () {
        // Credentials to be sent to the login
        var credentials = {
          name: this.credentials.name,
          password: this.credentials.password
        }
        // Log in, this may call 'emitMessage' later
        Auth.login(this, credentials, '/')

        // Send notification to the enclosing app
        this.$emit('dunsaved')
        this.isActive = false
      },
      /**
       * Emit a message. This is used by the auth component
       * to make calls back.
       */
      emitMessage (errorName, errorMessage) {
        Event.$emit('got500', errorMessage)
      },
      /**
       * The cancel button was called
       *
       */
      cancel () {
        this.$emit('duncanceled')
        this.isActive = false
      },
      /**
       * Looking for the escape key, which
       * will call cancel
       */
      keychanged (event) {
        // Escape
        if (event.which === 27) {
          this.cancel()
        }
      }
    },
    /**
     * Watch the 'active' property
     *
     */
    watch: {
      // Watch the parent's 'active' value. When it changes
      // we change our 'isActive' value to match.
      active: function (val, oldVal) {
        this.isActive = val
      }
    },
    /**
     * Created is called once this component is created.
     * Add a listener for 'keyup'
     */
    created: function () {
      window.addEventListener('keyup', this.keychanged)
    }
  }
</script>
