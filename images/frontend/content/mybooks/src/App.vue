<template>
  <div>

    <login @dunsaved="loginCalled" 
           @duncanceled="cancelCalled" 
           :active="doLoginModal" ></login>
    <div id="app"
         class="container is-fullhd is-light">

      <nav class="navbar is-size-5"
           role="navigation"
           aria-label="main navigation">
        <div class="navbar-brand">
          <a class="navbar-item" href="/">
            <img src="./assets/leaningBooks.png">&nbsp;myBooks
          </a>

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
                <span class="icon is-medium">
                  <i class="fa fa-wpexplorer"></i>
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
                  <i class="fa fa-book"></i>
                </span>
                <span>All Books</span>
              </router-link>
            </a>

            <!-- /authors -->
            <a class="navbar-item"
               @click="toggleMobileNav"
               v-if="is_authenticated()">
              <router-link tag="span" to="/authors" exact>
                <span class="icon is-large"><i class="fa fa-grav"></i></span>
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
        <div v-if="!showMobile"
          class="navbar-menu">

          <!-- left side -->
          <div class="navbar-start">

            <!-- home -->
            <a class="navbar-item">
              <router-link tag="div" to="/" exact>
                <span class="icon is-medium">
                  <i class="fa fa-home"></i>
                </span>
                <span>Home</span>
              </router-link>
            </a>

            <!-- user books -->
            <a class="navbar-item"
               v-if="is_authenticated()">
              <router-link tag="div" to="/shelves" exact>
                <span class="icon is-medium">
                  <i class="fa fa-wpexplorer"></i>
                </span>
                <span>My Books</span>
              </router-link>
            </a>


            <!-- /books -->
            <a class="navbar-item disabled"
               v-if="is_authenticated()">
              <router-link tag="span" to="/books" exact>
                <span class="icon is-large">
                  <i class="fa fa-book"></i>
                </span>
                <span>All Books</span>
              </router-link>
            </a>


            <!-- /authors -->
            <a class="navbar-item"
               v-if="is_authenticated()">
              <router-link tag="span" to="/authors" exact>
                <span class="icon is-large"><i class="fa fa-grav"></i></span>
                <span>All Authors</span>
              </router-link>
            </a>


          </div> <!-- end navbar-start -->

          <!-- right side -->
          <div class="navbar-end"
               v-if="is_authenticated('admin')">

            <!-- Admin dropdown -->
            <!-- Do not show if mobila navigation is turned on -->
            <div class="navbar-item has-dropdown is-hoverable">
              <a class="navbar-link">
                Admin
              </a>

              <div class="navbar-dropdown is-boxed">
                <!-- Manage Users -->
                <a class="navbar-item">
                  <router-link tag="span" to="/users" exact>
                    <span class="icon is-large"><i class="fa fa-users"></i></span>
                    <span>Users</span>
                  </router-link>
                </a>

                <!-- Add Author -->
                <a class="navbar-item">
                  <router-link tag="span" to="/addauthor" exact>
                    <span class="icon is-large"><i class="fa fa-user-circle"></i></span>
                    <span>Add Author</span>
                  </router-link>
                </a>

                <!-- Add Book -->
                <a class="navbar-item">
                  <router-link tag="span" to="/addbook" exact>
                    <span class="icon is-large"><i class="fa fa-book"></i></span>
                    <span>Add Book</span>
                  </router-link>
                </a>

                <!-- Add Tag -->
                <a class="navbar-item">
                  <router-link tag="span" to="/addtag" exact>
                    <span class="icon is-large"><i class="fa fa-tags"></i></span>
                    <span>Tags</span>
                  </router-link>
                </a>

                <hr class="navbar-divider">
                <div class="navbar-item">
                  myBooks 0.1.0
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
              <button class="button" @click="do_logout()">Log out</button>
            </div>
            <div v-if="! is_authenticated()">
              <button class="button" @click="do_login()">Log In</button>
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
  font-size: 160%;
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
