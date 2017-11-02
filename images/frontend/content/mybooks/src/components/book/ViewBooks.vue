<template>
  <!-- Main container -->
  <div>

    <!-- Header for search, list vs grid -->
    <ViewHeader theThing="Titles"
                :numberOfThings="getCurrentLength"
                :totalNumber="AllData.totalNumData"
                :showAsList="ViewState.viewAsList"
                :isLoading="isLoading"
                @gridOn="showGrid"
                @listOn="showList"
                @searchString="searchStringUpdated"
                @clearCalled="clearCalledFromHeader"
                @grabAll="grabAll"
                >
    </ViewHeader>

    <!-- when 'books' changes, a watcher in BooksAsList and BooksAsGrid updates their view -->
    <BooksAsList v-if="ViewState.viewAsList"
               :books="AllData.BooksJson"></BooksAsList>
    <BooksAsGrid v-else
               :books="AllData.BooksJson"></BooksAsGrid>

    <infinite-loading @infinite="infiniteHandler" ref="infiniteLoading">
      <span slot="no-more">
        -- end books ({{ getCurrentLength }}) --
      </span>
      <span slot="no-results">
        -- no results --
      </span>
    </infinite-loading>


    <!-- Message to user -->
    <div class="animated fadeOutDown is-size-2"
         v-if="messageToUser">
      {{ messageToUser }}
    </div>

  </div>
</template>


<script>
  import Auth from '../../auth'
  import BooksAsList from './BooksAsList.vue'
  import BooksAsGrid from './BooksAsGrid.vue'
  import ViewHeader from '../ViewHeader.vue'
  import InfiniteLoading from 'vue-infinite-loading'
  import _ from 'lodash'

  /**
   * Default data
   */
  export default {
    // Components we depend on
    components: { BooksAsList, BooksAsGrid, ViewHeader, InfiniteLoading },
    // Data for this component
    data () {
      return {
        // string to search/query books on
        searchBookString: '',
        // animated message content
        messageToUser: '',
        /**
         * Contains the JSON for Books
         * and the status of the infinite scrolling, e.g.
         * the current state of querying the server for data.
         */
        AllData: {
          // data from ajax
          BooksJson: [],
          // Index of data to query for
          dataStart: 0,
          // Length of data to get
          lengthToGet: 15,
          // Current end of our data length
          end: -1,
          // Total number of datum to get
          totalNumData: 0,
          // The GetAll button was clicked
          getAll: false,
          reset: function () {
            this.BooksJson = []
            this.dataStart = 0
            this.end = -1
          }
        },
        // flag denoting loading or not
        isLoading: false,
        /**
         * Current state of the view
         */
        ViewState: {
          valid: 'yes',
          // flag to determine if we present as list or grid
          viewAsList: true
        }
      }
    },
    /**
     * Get length of the books' json
     */
    computed: {
      getCurrentLength: function () {
        return this.AllData.BooksJson.length
      }
    },
    /**
     * When mounted, get list of users from database.
     * Add event listeners
     */
    mounted: function () {
      // get books from store
      let temp = this.$store.state.allBooks
      if (temp && temp.BooksJson) {
        this.AllData = temp
      }

      // Check status of filters and maybe get all values
      this.checkFilterStatus()

      // Get list/grid status
      temp = this.$store.state.booksView
      if (temp && temp.valid) {
        this.ViewState = temp
      }

      Event.$on('updatedb.book.409', (eventmessage) => this.conflictError(eventmessage))
      Event.$on('updatedb.user_book.409', (eventmessage) => this.conflictError(eventmessage))
    },
    /**
     * Methods
     */
    methods: {
      /**
       * Infinite loading. Get list of Books
       */
      infiniteHandler ($state) {
        let self = this

        const authString = Auth.getAuthHeader()
        let params = {
          offset: self.AllData.dataStart,
          limit: self.AllData.lengthToGet
        }
        if (this.searchBookString !== '') {
          params.title = this.searchBookString
        }

        let url = '/book'
        this.isLoading = true

        // Make call
        this.$axios.get(url, {
          headers: { Authorization: authString },
          params: params })
          .then((response) => {
            this.isLoading = false
            // Get data segment information
            let incomingData = response.data.data
            // start of data inside total set
            let start = response.data.offset
            // length of data in this response
            let length = response.data.limit
            // Total # of datum
            let totalSize = response.data.total

            // Has the dataset size changed?
            if (self.AllData.totalNumData >= 0 && totalSize !== self.AllData.totalNumData) {
              self.AllData.reset()
              self.AllData.totalNumData = totalSize

              if ($state) {
                $state.reset()
              }
              return
            }

            // This method could be called when we come back to the tab
            // so check that we have all the data or not
            if (self.AllData.BooksJson.length >= totalSize) {
              if ($state) {
                $state.loaded()
                $state.complete()
              }
              return
            }

            // Data set is unchanged, continue getting data
            // save list of user books
            self.AllData.BooksJson = _.concat(self.AllData.BooksJson, incomingData)
            self.AllData.end = start + length
            self.AllData.dataStart = start + self.AllData.lengthToGet
            self.AllData.totalNumData = totalSize
            self.AllData.lengthToGet = 15

            // save to $store
            this.$store.commit('setAllBooks', self.AllData)

            if ($state) {
              $state.loaded()
            }

            if (self.AllData.end >= self.AllData.totalNumData) {
              if ($state) {
                $state.complete()
              }
            }
          })
          .catch(function (error) {
            this.isLoading = false
            if ($state) {
              $state.complete()
            }
            if (error.response) {
              if (error.response.status === 401) {
                Event.$emit('got401')
              }
            } else {
              console.log(error)
            }
          })
      },
      /**
       * Show the grid view
       */
      showGrid () {
        this.ViewState.viewAsList = false
        this.$store.commit('setBooksView', this.ViewState)
      },
      /**
       * Show the list view
       */
      showList () {
        this.ViewState.viewAsList = true
        this.$store.commit('setBooksView', this.ViewState)
      },
      /**
       * The search string has been updated
       */
      searchStringUpdated (value) {
        this.searchBookString = value
        this.AllData.reset()
        this.$refs.infiniteLoading.$emit('$InfiniteLoading:reset')
      },
      /**
       * The clear button was clicked
       */
      clearCalledFromHeader () {
        this.searchBookString = ''
        // Check if all filters are clear
        this.checkFilterStatus()
      },
      /**
       * If all filters have been cleared
       * AND the getAll flag is true, re-fetch all
       */
      checkFilterStatus () {
        if (this.searchBookString === '') {
          if (this.AllData.getAll) {
            // Search is empty and the GetAll was previously clicked
            let self = this

            const authString = Auth.getAuthHeader()
            // Make query to just get # of books
            let params = {
              limit: 0
            }
            let url = '/book/'
            this.$axios.get(url, {
              headers: { Authorization: authString },
              params: params })
              .then((response) => {
                // We got the number of user books
                let numBooks = response.data.total

                // Now get all those books
                self.AllData.lengthToGet = numBooks
                self.AllData.dataStart = 0

                // Reset the scrolling like in grabAll
                this.AllData.getAll = true
                this.$refs.infiniteLoading.$emit('$InfiniteLoading:reset')
                this.infiniteHandler(null)
              })
              .catch(function (error) {
                if (error.response.status === 401) {
                  Event.$emit('got401')
                } else {
                  console.log(error)
                }
              })
          }
        }
      },
      /**
       * Grab all values
       */
      grabAll () {
        // Calculate the length to get in order to grab all data
        this.AllData.lengthToGet = this.AllData.totalNumData - this.AllData.end
        if (this.AllData.lengthToGet <= 0) {
          this.AllData.lengthToGet = 15
        }
        this.AllData.getAll = true

        // Reset the scrolling
        this.$refs.infiniteLoading.$emit('$InfiniteLoading:reset')
        this.infiniteHandler(null)
      },
      /**
       * Print a message to the user
       */
      printMessage (printThis) {
        let self = this
        this.messageToUser = printThis
        // Have the modal with userMessage go away in 1 second
        setTimeout(function () {
          self.messageToUser = ''
        }, 3000)
      },
      /**
       * Error while contacting server
       *
       */
      conflictError (serverError) {
        console.log('Got server error:' + serverError)
        this.printMessage(serverError)
      }
    }
  }
</script>

<style>
.fade-enter-active, .fade-leave-active {
}

.main-width {
  width: 65%
}

.book {
   cursor: pointer
}

</style>
