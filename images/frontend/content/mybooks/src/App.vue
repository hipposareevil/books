<template>
  <div>

    <login @dunsaved="loginCalled" 
           @duncanceled="cancelCalled" 
           :active="doLoginModal" ></login>
    <div id="app"
         class="container is-fullhd is-light">

      <nav class="navbar is-size-5"
           role="navigation"
           style="margin-top: 5px;"
           aria-label="main navigation">
        <div class="navbar-brand">

          <!-- Mobile navigation -->
          <div class="navbar-burger"
               @click="toggleMobileNav"
               :class="{ 'is-active': showMobile }">
            <span></span>
            <span></span>
            <span></span>
          </div>
        </div>

        <!-- Mobile -->
        <div v-if="showMobile"
             :class="{ 'is-active': showMobile }"
             class="navbar-menu">

          <!-- left side -->
          <div class="navbar-start">

            <!-- user books -->
            <a class="navbar-item"
               @click="toggleMobileNav"
               v-if="is_authenticated()">
              <router-link tag="div" to="/shelves" exact>
                <span class="icon is-large">
                  <i class="fas fa-wpexplorer"></i>
                </span>
                <span>My Books</span>
              </router-link>
            </a>


            <!-- /books -->
            <a class="navbar-item disabled"
               @click="toggleMobileNav"
               v-if="is_authenticated()">
              <router-link tag="span" to="/books" exact>
                <span class="icon is-large">
                  <i class="fas fa-book"></i>
                </span>
                <span>All Books</span>
              </router-link>
            </a>

            <!-- /authors -->
            <a class="navbar-item"
               @click="toggleMobileNav"
               v-if="is_authenticated()">
              <router-link tag="span" to="/authors" exact>
                <span class="icon is-large"><i class="fab fa-grav"></i></span>
                <span>All Authors</span>
              </router-link>
            </a>


          </div> <!-- end navbar-start -->

          <div class="navbar-item">
            <div v-if="is_authenticated()">
              <button class="button" @click="do_logout()">Log out</button>
            </div>
            <div v-if="! is_authenticated()">
              <button class="button" @click="do_login()">Log In</button>
            </div>
          </div>
        </div>
        <!-- end  mobile -->
        
        <!-- normal -->
        <div v-else
          class="navbar-menu">

          <!-- left side -->
          <div class="navbar-start">

            <!-- home -->
            <router-link tag="div" to="/" exact>
              <a class="navbar-item is-medium">
                <img src="./assets/leaningBooks.png">&nbsp;Home
              </a>
            </router-link>

            <!-- user books -->
            <router-link tag="div" to="/shelves" exact>
              <a class="navbar-item"
                 v-if="is_authenticated()">
                <span class="icon is-medium">
                  <i class="fab fa-wpexplorer"></i>
                </span>
                <span>My Books</span>
              </a>
            </router-link>

            <div class="navbar-item has-dropdown is-hoverable">
              <a class="navbar-link" style="margin-top: -5px;">
                <span class="icon is-medium">
                  <i class="fas fa-eye"></i>
                </span>
                <span>Browse</span>
              </a>

              <div class="navbar-dropdown is-boxed">
                <!-- Books -->
                  <router-link tag="span" to="/books" exact>
                    <a class="navbar-item">
                      <span class="icon is-large"><i class="fa fa-book"></i></span>
                      <span>Books</span>
                    </a>
                  </router-link>

                  <!-- Authors -->
                  <router-link tag="span" to="/authors" exact>
                    <a class="navbar-item">
                      <span class="icon is-large"><i class="fab fa-grav"></i></span>
                      <span>Authors</span>
                    </a>
                  </router-link>
              </div>
            </div>
            <!-- end of dropdown for browse -->

          </div> <!-- end navbar-start -->

          <!-- right side -->
          <div class="navbar-end"
               v-if="is_authenticated('admin')">

            <!-- Admin dropdown -->
            <!-- Do not show if mobila navigation is turned on -->
            <div class="navbar-item has-dropdown is-hoverable">
              <a class="navbar-link">
                <span class="icon is-medium">
                  <i class="fas fa-lock"></i>
                </span>
                <span>Admin</span>
              </a>

              <div class="navbar-dropdown is-boxed">
                <!-- Manage Users -->
                <router-link tag="span" to="/users" exact>
                  <a class="navbar-item">
                    <span class="icon is-large"><i class="fa fa-users"></i></span>
                    <span>Users</span>
                  </a>
                </router-link>


                <!-- Add Author -->
                <router-link tag="span" to="/addauthor" exact>
                  <a class="navbar-item">
                    <span class="icon is-large"><i class="fa fa-user-circle"></i></span>
                    <span>Add Author</span>
                  </a>
                </router-link>

                <!-- Add Book -->
                <router-link tag="span" to="/addbook" exact>
                  <a class="navbar-item">
                    <span class="icon is-large"><i class="fa fa-book"></i></span>
                    <span>Add Book</span>
                  </a>
                </router-link>

                <!-- Add Tag -->
                <router-link tag="span" to="/addtag" exact>
                  <a class="navbar-item">
                    <span class="icon is-large"><i class="fa fa-tags"></i></span>
                    <span>Tags</span>
                  </a>
                </router-link>


                <hr class="navbar-divider">
                <div class="navbar-item">
                  myBooks 0.1.1
                </div>
              </div>
            </div>
          </div> <!-- end dropdown -->


          <div class="navbar-item">
            <div class="tags has-addons">
              <span class="tag">User:</span>
              <span v-if="is_authenticated()"
                    class="tag is-info is-capitalized">
                {{ user.name }}
              </span>
              <span v-else 
                    @click="do_login()"
                    class="tag is-warning">
                -----
              </span>
              
            </div>
          </div>

          <div class="navbar-item">
            <div v-if="is_authenticated()">
              <button class="button" @click="do_logout()">
                <i class="fas fa-sign-out-alt"></i>&nbsp;
                Log out
              </button>
            </div>
            <div v-if="! is_authenticated()">
              <button class="button" @click="do_login()">
                <i class="fas fa-sign-in-alt"></i>&nbsp; 
                Log In
              </button>
            </div>
          </div>
        </div>
        <!-- end normal -->

      </nav> <!-- end navbar -->

      <div class="section">
        <!-- Show the view -->
        <router-view></router-view> 
        
      </div>


    <!-- longer error message to user -->
    <article v-if="errorMessage"
             class="main-width message is-danger">
      <div class="message-body">
        {{ errorMessage }}
      </div>
    </article>



    </div> <!-- end of #app container -->
  </div> <!-- top level div -->
</template>

<script>
import Message from './components/Message.vue'
import AddAuthor from './components/AddAuthor.vue'
import Login from './components/Login.vue'
import Auth from './auth'
import 'bulma/css/bulma.css'

export default {
  name: 'app',
  components: { Message, AddAuthor, Login },

  data () {
    return {
      // User from authentication
      user: Auth.user,
      // When true bring up the 'login' modal
      doLoginModal: false,
      // Error message
      errorMessage: '',
      // Show the navigation on right
      showMobile: false
    }
  },
  /**
   * Called when this component is mounted.
   * This will validate the stored tokens and register a callback for 401s
   */
  mounted: function () {
    // When we are created the first time
    // validate the tokens
    Auth.validateTokens(this)
    // register event listeners
    Event.$on('got401', () => this.wasLoggedOut())
    Event.$on('got500', (eventmessage) => this.serverError(eventmessage))
    Event.$on('clearEverything', () => this.clearEverything())
  },
  methods: {
    /**
     * Toggle the mobile navigation
     *
     */
    toggleMobileNav () {
      this.showMobile = !this.showMobile
    },
    /**
     * Login was called by the modal
     *
     */
    loginCalled (value) {
      this.doLoginModal = false
    },
    /**
     * Cancel to the login was called by the modal
     */
    cancelCalled () {
      this.doLoginModal = false
    },
    /**
     * perform a Logout
     */
    do_logout () {
      Auth.logout(this)
    },
    /**
     * perform a login
     */
    do_login () {
      this.doLoginModal = true
    },
    /**
     *
     */
    clearEverything () {
      this.$store.commit('clearEverything')
    },
    /*
     * Server sent a 401, so we are logged out
     *
     */
    wasLoggedOut () {
      console.log('We were logged out')
      Auth.logout(this)
    },
    /**
     * Error while contacting server
     *
     */
    serverError (serverError) {
      this.printError('Server error: ' + serverError)
    },
    /**
     * Print an Error to the user
     */
    printError (printThis) {
      let self = this
      this.errorMessage = printThis
      // Have the modal with userMessage go away in bit
      setTimeout(function () {
        self.errorMessage = ''
      }, 5500)
    },
    /**
     * Check if the current user (in Auth) is authenticated.
     * optional groupToCheck used to verify the user's group
     */
    is_authenticated (groupToCheck) {
      // Refresh from local storage
      Auth.checkAuth()
      // If user has been set
      if (this.user) {
        let value = Auth.isAuthenticated(groupToCheck)
        return value
      } else {
        return false
      }
    }
  }
}
</script>

<style lang="css">
a {
  color: #4a4a4a;
}
.is-active {
  font-weight: bold;
  border-bottom: dashed lightgray 1px;
}

.grid-div {
    border: solid lightgray 1px;
    padding: 1em;
    margin: 1em;
}

.grid-figure {
    margin: auto;
}

.grid-image {
    max-height: 128px;
    width: auto;
}
</style>
