<template>
  <div>

    <p class="subtitle is-4"
       style="border-bottom: solid lightgray 1px;">
      Add author to database
    </p>

    <!-- Modal. dunsaved called when user clicks on Save -->
    <AuthorModal @dunsaved="saveCalled" 
           @duncanceled="cancelCalled" 
           v-bind:active="showmodal"
           v-bind:name="currentAuthor.name">
    </AuthorModal>

    <!-- Modal. dunsaved called when user clicks on Save -->
    <ManualAuthorModal
      @dunsaved="manualSaveCalled" 
      @duncanceled="manualCancelCalled" 
      v-bind:active="showManualModal">
    </ManualAuthorModal>

    <!-- Main container -->
    <nav class="level">
      <!-- Left side -->
      <div class="level-left">
        <div class="level-item">
          <div class="field has-addons">
            <p class="control">
              <form  @submit.prevent="">
                <input class="input"
                       v-model="forminput.name"
                       type="text"
                       v-on:input="debouncedSearch"
                       placeholder="Author name">
              </form>
            </p>
            <p class="control"
               style="padding-left: 1px;">
              <button class="button is-info"
                      v-bind:class="{ 'is-loading': loading}"
                      @click="search">
                Search
              </button>
            </p>

            <p class="control">
              <button class="button is-light"
                      @click="clearInputs">
                Clear
              </button>
            </p>

          </div>
        </div>
      </div>
      <!-- end left -->

      <!-- right -->
      <div class="level-right">
        <div class="level-item">
          <!-- manual -->
          <p class="control"
             style="padding-left: 1em;">
            <button class="button is-primary"
                    @click="addManual">
              Manual Add
            </button>
          </p>

        </div>

      </div>
      <!-- end right -->
    </nav>

    <br>

    <!-- List of authors -->

    <div class="columns">
      <!-- left side column -->
      <div class="column is-half"
           style="border-right: solid lightgray 1px;">

        <h2 class="title is-4">
          <span style="border-bottom: solid gray 1px;">
            Existing Authors
          </span>
        </h2>

        <!-- list of existing authors -->
        <ul>
          <li v-for="(current, index) in existingAuthors">
            <a class="tooltip"
              style="min-width: 15em; padding-bottom: 1em; cursor: default;">
              {{ current.name }} 
              <span class="tooltiptext lefthover"
                    v-html="makeHoverData(current)"></span>
            </a>
          </li>
        </ul>
      </div>


      <!-- right side column -->
      <div class="column is-one-half"
           style="">
        <h2 class="title is-4">
          <span style="border-bottom: solid gray 1px;">
            Author to Add
          </span>
        </h2>

        <!-- list of queried authors -->
        <ul>
          <li v-for="(current, index) in authorJson"
              style="padding-bottom: 1em;">
            <a style="cursor: default">
              <div class="tooltip">

                <button class="button is-info is-small"
                        @click="clickOnAuthor(current)">
                  Add to database
                </button>

                <span style="margin-left: 1em;">
                  {{ current.name }}
                </span>

                <span class="tooltiptext"
                      v-html="makeHoverData(current)"></span>

              </div>
            </a>
          </li>
        </ul>

      </div> <!-- end right column -->

    </div> <!-- end columns -->


    <!-- longer error message to user -->
    <article v-if="errorMessage"
             class="main-width message is-danger">
      <div class="message-body">
        {{ errorMessage }}
      </div>
    </article>

    <!-- Message  -->
    <div v-if="userMessage"
         class="animated fadeOutDown"
         style="font-size: 150%" >
      {{ userMessage }}
    </div>

  </div>
</template>

<script>
  import _ from 'lodash'
  import AuthorModal from './AddAuthorModal.vue'
  import ManualAuthorModal from './AddManualAuthorModal.vue'
  import Auth from '../auth'
  import UpdateDb from '../updatedb'

  export default {
    components: { AuthorModal, ManualAuthorModal },
    data () {
      return {
        // currently chosen author
        currentAuthor: {},
        // authors from db
        existingAuthors: {},
        // author JSON data
        authorJson: {},
        // input name
        forminput: { name: '' },
        // loading icon
        loading: false,
        // message to user
        userMessage: '',
        // show the modal for adding an author
        showmodal: false,
        // show the manual add author modal
        showManualModal: false,
        // Error message
        errorMessage: ''
      }
    },
    /**
     * Called before this component would be loaded.   This halts the rendering if the current
     * user is not authenticated as being in the 'admin' group.
     */
    beforeRouteEnter (to, from, next) {
      if (Auth.isAuthenticated('admin')) {
        next()
      } else {
        next(false)
      }
    },
    /**
     * This is called after this component is created.
     *
     */
    created: function () {
      this.debouncedSearch = _.debounce(this.search, 250)

      // When we get booksaved event, print out message
      Event.$on('updatedb_authorcreated', (message) => this.printMessage(message))

      // If we get booksaved error, print out error
      Event.$on('updatedb_error', (error) => this.printError(error))
    },
    /**
     * Remove event listeners
     */
    destroyed: function () {
      Event.$off('updatedb_authorcreated')
      Event.$off('updatedb_error')
    },
    // Methods
    methods: {
      /**
       * Manually add an author
       */
      addManual () {
        this.showManualModal = true
      },
      /**
       * Search the /query endpoint for an author.
       * Place that data into authorJson
       */
      search () {
        let self = this
        const authString = Auth.getAuthHeader()

        // Get author from query
        this.authorJson = {}
        this.loading = true
        this.$axios.get('/query/author?author=' + this.forminput.name + '')
          .then((response) => {
            self.authorJson = response.data.data
            self.loading = false
          })
        .catch(function (error) {
          self.loading = false
          if (error.response.status === 401) {
            Event.$emit('got401')
          } else {
            console.log(error)
          }
        })

        // Get books from database
        this.existingAuthors = {}
        let url = '/author?name=' + this.forminput.name
        this.$axios.get(url, { headers: { Authorization: authString } })
          .then((response) => {
            self.existingAuthors = response.data.data
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
       * Clear the form inputs
       *
       */
      clearInputs () {
        this.forminput.name = ''
        this.showmodal = false
        this.showManualModal = false
        this.authorJson = {}
        this.existingAuthors = {}
      },
      /**
       * Save was called via the popup modal for manual author add
       * Try to save the author to the DB
       */
      manualSaveCalled (newAuthor) {
        let self = this

        // Clear the modal
        self.showManualModal = false
        self.clearInputs()

        // Update in database
        UpdateDb.tryAddAuthor(this, newAuthor)
      },
      /**
       * Cancel was called via the popup modal for manual add of author
       */
      manualCancelCalled () {
        this.showManualModal = false
      },
      /**
       * Save was called via the popup modal.
       * Try to save the author to the DB
       */
      saveCalled () {
        let self = this
        console.log('Saving author: ' + this.currentAuthor.name)

        // Clear the modal
        self.showmodal = false
        self.clearInputs()

        // Update in database
        UpdateDb.tryAddAuthor(this, this.currentAuthor)

        // When we get booksaved event, print out message
        Event.$on('updatedb_authorcreated', (message) => this.printMessage(message))

        // If we get booksaved error, print out error
        Event.$on('updatedb_error', (error) => this.printError(error))
      },
      /**
       * Cancel was called via the popup modal
       *
       */
      cancelCalled () {
        console.log('Cancel called in author parent')
        this.showmodal = false
      },
      /**
       * An author was clicked on in the list
       */
      clickOnAuthor (authorParam) {
        this.currentAuthor = authorParam
        // bring up modal
        this.showmodal = true
        this.userMessage = ''
      },
      /**
       * Make HTML data used to hover over a given author
       *
       */
      makeHoverData: function (currentAuthor) {
        let subjects = currentAuthor.subjects
        let listvalue = ''
        listvalue += '<img src="' + currentAuthor.imageMedium + '"><br>'
        listvalue += '<b>Birthdate:</b><br>'
        if (currentAuthor.birthDate) {
          listvalue += '[' + currentAuthor.birthDate + ']'
        } else {
          listvalue += 'n/a'
        }
        listvalue += '<br>'

        listvalue += '<b>Subjects</b>:<br>'
        if (subjects) {
          for (var i = 0; i < subjects.length; i++) {
            listvalue += subjects[i] + '<br>'
          }
        } else {
          listvalue += 'n/a'
        }
        return listvalue
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
        }, 2500)
      },
      /**
       * Print a message to the user
       */
      printMessage (printThis) {
        let self = this
        self.userMessage = printThis
        // Have the modal with userMessage go away in 1 second
        setTimeout(function () {
          self.userMessage = false
        }, 2000)
      }
    }
  }
</script> 

<style>
.fade-enter-active, .fade-leave-active {
  transition: opacity 5s
}
.fade-enter, .fade-leave-to /* .fade-leave-active below version 2.1.8 */ {
  opacity: 0
}

.fadeOutDown {
  animation-duration: 3s;
}

.tooltip:hover {
  background-color: rgb(245,245,245)
}

/* Tooltip container */
.tooltip {
    position: relative;
    display: inline-block;
}

/* Tooltip text */
.tooltip .tooltiptext {
    visibility: hidden;
    width: 250px;
    background-color: rgb(57, 134, 175);
    color: rgb(238,240,240);
    text-align: center;
    padding: 5px 0;
    border-radius: 6px;
 
    /* Position the tooltip text - see examples below! */
    position: absolute;
    z-index: 1;
}

.tooltiptext {
  visibility: hidden;
}

/* Show the tooltip text when you mouse over the tooltip container */
.tooltip:hover .tooltiptext {
    top: -25px;
    left: 120%;
    visibility: visible;
}


</style>
