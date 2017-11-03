<template>
  <div>

{{ showmodal }}

    <p class="subtitle is-4"
       style="border-bottom: solid lightgray 1px;">
      Add book to database
    </p>

    <!-- Modal. dunsaved called when user clicks on Save -->
    <BookModal @saveClicked="saveCalled" 
           @cancelClicked="cancelCalled" 
           :active="showmodal"
           :title="currentBook.title">
    </BookModal>

    <!-- Main container -->
    <nav class="level">
      <!-- Left side -->
      <div class="level-left">
        <div class="level-item">
          <div class="field has-addons">
            <p class="control">
              <input class="input"
                     v-model="forminput.authorName"
                     type="text"
                     placeholder="Author name">
            </p>
            <p class="control">
              <input class="input"
                     v-model="forminput.title"
                     v-on:input="debouncedSearch"
                     type="text"
                     placeholder="Book title">
            </p>
            <p class="control">
              <input class="input"
                     v-model="forminput.isbn"
                     type="text"
                     placeholder="ISBN">
            </p>
          </div>
        </div>

        <div class="level-item">
          <p class="control">
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

    </nav>

    <br>
    <!-- List of books -->

    <div class="columns">
      <!-- left side column -->
      <div class="column is-half"
           style="border-right: solid lightgray 1px;">

        <h2 class="title is-4">
          <span style="border-bottom: solid gray 1px;">
            Existing Books
          </span>
        </h2>

        <!-- list of existing books -->
        <ul>
          <li v-for="(current, index) in existingBooks">
            <a class="tooltip"
              style="min-width: 15em; padding-bottom: 1em; cursor: default;">
              {{ current.title }}
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
            Books to Add
          </span>
        </h2>

        <!-- list of queried books -->
        <ul>
          <li v-for="(current, index) in bookJson"
              style="padding-bottom: 1em;">
            <a style="cursor: default">
              <div class="tooltip">

                <button class="button is-info is-small"
                        @click="clickOnBook(current)">
                  Add to database
                </button>

                <span style="margin-left: 1em;">
                  {{ current.title }}
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
  import Auth from '../auth'
  import _ from 'lodash'
  import Message from './Message.vue'
  import BookModal from './AddBookModal.vue'
  import UpdateDb from '../updatedb'

  export default {
    /**
     * Components used
     */
    components: { Message, BookModal },
    /**
     * Data for AddBook
     */
    data () {
      return {
        // Currently chosen book
        currentBook: {},
        // message for user
        userMessage: '',
        // book JSON data
        bookJson: {},
        // books from db
        existingBooks: {},
        // Form inputs
        forminput: {
          title: '',
          authorName: '',
          isbn: ''
        },
        // loading icon
        loading: false,
        // show the modal for adding a book
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
       * Clear inputs and json
       */
      clearInputs () {
        this.forminput.title = ''
        this.forminput.authorName = ''
        this.forminput.isbn = ''
        this.showmodal = false
        this.bookJson = {}
        this.existingBooks = {}
      },
      /**
       * Save was called via the popup modal.
       * Try to save the book to the DB
       *
       * params:
       * AddUserBookInformation- struct of format:
       * {
       * addFlag: true/false
       * tags: {}
       * }
       */
      saveCalled (AddUserBookInformation) {
        this.showmodal = false

        // Clear inputs except for author name
        let temp = this.forminput.authorName
        this.clearInputs()
        this.forminput.authorName = temp

        let newTitle = this.currentBook.title
        newTitle = _.startCase(_.toLower(newTitle))
        this.currentBook.title = newTitle

        // Send the current book and the AddUserBookInformation about the book
        UpdateDb.addBook(this, this.currentBook, AddUserBookInformation)

        // When we get booksaved event, print out message
        // Send the AddUserBookInformation bean along.
        Event.$on('updatedb_bookcreated', (message, AddUserBookInformation) => this.bookCreated(message, AddUserBookInformation))

        // Error on book saved
        Event.$on('updatedb_book_409', (message) => this.printMessage(message))

        // If we get booksaved error, print out error
        Event.$on('updatedb_error', (error) => this.printError(error))
      },
      /**
       * The book was created. See if we need to add it to the users shelf
       *
       */
      bookCreated (message, AddUserBookInformation) {
        if (AddUserBookInformation.addFlag === true) {
          // Add book to the user shelf with the incoming tags
          this.addBookToList(AddUserBookInformation)
        }

        this.printMessage(message)
      },
      /**
       * Add a book to a shelf.
       */
      addBookToList (AddUserBookInformation) {
        // Get user id from Auth
        let userId = Auth.user.id

        let self = this
        let ourHeaders = {
          'Authorization': Auth.getAuthHeader()
        }

        // Data to update in database
        let data = { }
        data.bookId = AddUserBookInformation.bookId
        if (AddUserBookInformation.tags.length > 0) {
          data.tags = AddUserBookInformation.tags
        }

        // Try to create a new user_book
        let url = '/user_book/' + userId
        this.$axios.post(url, data, { headers: ourHeaders })
          .then((response) => {
            self.printMessage('Book "' + AddUserBookInformation.title + '" added to My Books.')
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
       * Cancel was called via the popup modal
       *
       */
      cancelCalled () {
        console.log('Cancel called in parent')
        this.showmodal = false
      },
      /**
       * Search the /query endpoint for an book
       */
      search () {
        let self = this
        const authString = Auth.getAuthHeader()

        // Get books from query
        this.bookJson = {}
        this.loading = true
        this.$axios.get('/query/book?author=' + this.forminput.authorName +
                        '&title=' + this.forminput.title +
                        '&isbn=' + this.forminput.isbn)
          .then((response) => {
            this.bookJson = response.data.data
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

        // Get books from database
        this.existingBooks = {}
        let url = '/book?title=' + this.forminput.title
        this.$axios.get(url, { headers: { Authorization: authString } })
          .then((response) => {
            this.existingBooks = response.data.data
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
       * Clicked on a book in the list
       *
       */
      clickOnBook (book) {
        this.showmodal = true
        this.currentBook = book
        this.userMessage = ''
      },
      /**
       * Make the list of data for a given :hover
       *
       */
      makeHoverData (chosenBook) {
        let subjects = chosenBook.subjects
        let listvalue = ''
        if (chosenBook.imageMedium) {
          listvalue += '<img src="' + chosenBook.imageMedium + '"><br>'
        }
        listvalue += '<b>Author</b><br>'
        listvalue += chosenBook.authorName + '<br>'
        listvalue += '<b>Year</b><br>'
        listvalue += chosenBook.firstPublishedYear + '<br>'

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
          self.userMessage = ''
        }, 2000)
      }
    },
    created: function () {
      this.debouncedSearch = _.debounce(this.search, 250)
    }
  }
</script> 

<style>
.main-width {
  width: 95%
}

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
}

.tooltip:hover {
  background-color: rgb(245,245,245)
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

    position: absolute;
    z-index: 1;
}

.tooltiptext {
  visibility: hidden;
}

/* Show the tooltip text when you mouse over the tooltip container */
.tooltip:hover .tooltiptext {
    top: -25px;
    left: 125%;
    visibility: visible;
}

/* Show the tooltip text when you mouse over the tooltip container */
.tooltip:hover .tooltiptext {
    top: -80px;
    left: 125%;
    visibility: visible;
}

.tooltip:hover .lefthover {
    left: 100%;
}
</style>
