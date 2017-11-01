<template>
  <!-- Main container -->
  <div>

    <!-- Header for filter, search, list vs grid -->
    <ViewHeader theThing="Titles"
                :numberOfThings="lengthOfViewableBooks"
                :totalNumber="AllData.totalNumData"
                :showAsList="ViewState.viewAsList"
                @gridOn="showGrid"
                @listOn="showList"
                @searchString="searchStringUpdated"
                @filterString="filterStringUpdated"
                @grabAll="grabAll"
                >
    </ViewHeader>

    <!-- when 'books' changes, a watcher in BooksAsList and BooksAsGrid updates their view -->
    <BooksAsList v-if="ViewState.viewAsList"
               :books="bookList"></BooksAsList>
    <BooksAsGrid v-else
               :books="bookList"></BooksAsGrid>

    <infinite-loading @infinite="infiniteHandler" ref="infiniteLoading">
      <span slot="no-more">
        -- end books ({{ lengthOfViewableBooks }}) --
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
        // string to filter books on
        filterBookString: '',
        // string to search/query books on
        searchBookString: '',
        // animated message content
        messageToUser: '',
        // how many books are viewable
        lengthOfViewableBooks: 0,
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
          reset: function () {
            this.BooksJson = []
            this.dataStart = 0
            this.end = -1
          }
        },
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
     * When mounted, get list of users from database.
     * Add event listeners
     */
    mounted: function () {
      // get books from store
      let temp = this.$store.state.allBooks
      if (temp && temp.BooksJson) {
        this.AllData = temp
      }

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
        let url = '/book'
        if (this.searchBookString !== '') {
          url = url + '?title=' + this.searchBookString
        }

        this.$axios.get(url, {
          headers: { Authorization: authString },
          params: params })
          .then((response) => {
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
              console.log('ViewBooks: Dataset length for userbooks has changed. Resetting.')

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
       * The filter string has been updated
      */
      filterStringUpdated (value) {
        this.filterBookString = value
      },
      /**
       * Grab all values
       */
      grabAll () {
        this.AllData.lengthToGet = this.AllData.totalNumData - this.AllData.end
        if (this.AllData.lengthToGet <= 0) {
          this.AllData.lengthToGet = 15
        }
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
    },
    /**
     * A computed value for bookList, based on BookJson and
     * filtering by title name.
     */
    computed: {
      bookList: function () {
        let result = this.AllData.BooksJson
        this.lengthOfViewableBooks = result.length

        if (!this.filterBookString) {
          return result
        }

        // What to filter on
        const filterValue = this.filterBookString.toLowerCase()

        // create a filter for the books json
        const filter = event =>
              event.title.toLowerCase().includes(filterValue) ||
              event.authorName.toLowerCase().includes(filterValue) ||
              String(event.firstPublishedYear).includes(filterValue)

        let filteredResult = result.filter(filter)
        this.lengthOfViewableBooks = filteredResult.length
        return filteredResult
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
