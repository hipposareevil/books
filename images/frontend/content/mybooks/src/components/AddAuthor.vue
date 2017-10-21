<template>
  <div>

    <p class="subtitle is-4"
       style="border-bottom: solid lightgray 1px;">
      Add author to database
    </p>

    <!-- Modal. dunsaved called when user clicks on Save -->
    <modal @dunsaved="saveCalled" 
           @duncanceled="cancelCalled" 
           v-bind:active="showmodal"
           whatType="author"
           v-bind:thing="currentAuthor.name"></modal>

    <!-- Main container -->
    <nav class="level">
      <!-- Left side -->
      <div class="level-left">
        <div class="level-item">
        </div>
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
            <p class="control">
              <button class="button is-info"
                      v-bind:class="{ 'is-loading': loading}"
                      @click="search">
                Search
              </button>
            </p>
          </div>
        </div>
      </div>

    </nav>

    <div style="text-align: center;">
    <ul>
      <li v-for="(current, index) in authorJson">
        <a @click="clickOnAuthor(current)">        
          <span class="icon is-small"><i class="fa fa-user"></i></span>
          <div class="tooltip">
            <span class="tooltiptext" v-html="makeHoverData(current)"></span>
            {{ current.name }} 
          </div>
        </a>
      </li>
    </ul>
    </div>
    <br>
    
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
  import Modal from './AddThingModal.vue'
  import Auth from '../auth'
  import UpdateDb from '../updatedb'

  export default {
    components: { Modal },
    data () {
      return {
        // currently chosen author
        currentAuthor: {},
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
    methods: {
      /**
       * Search the /query endpoint for an author
       *
       */
      search () {
        console.log('AddAuthor.search called')
        let self = this
        this.loading = true
        this.$axios.get('/query/author?author=' + this.forminput.name + '')
          .then((response) => {
            this.authorJson = response.data.data
            this.loading = false
          })
        .catch(function (error) {
          self.loading = false
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
        this.authorJson = {}
      },
      /**
       * Save was called via the popup modal.
       * Try to save the author to the DB
       */
      saveCalled (value) {
        let self = this
        console.log('save called in parent:' + value)
        console.log('Saving author: ' + this.currentAuthor.name)

        // Clear the modal
        self.showmodal = false
        self.clearInputs()

        // Update in database
        UpdateDb.tryAddAuthor(this, this.currentAuthor)

        // When we get booksaved event, print out message
        Event.$on('updatedb.authorcreated', (message) => this.printMessage(message))

        // If we get booksaved error, print out error
        Event.$on('updatedb.error', (error) => this.printError(error))
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
    },
    /**
     * This is called after this component is created.
     *
     */
    created: function () {
      this.debouncedSearch = _.debounce(this.search, 250)
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

/* Tooltip container */
.tooltip {
    position: relative;
    display: inline-block;
    border-bottom: 1px dotted black; /* If you want dots under the hoverable text */
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
