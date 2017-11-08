<template>
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

    <div class="columns">
      <!-- left side column with tag filters -->
      <div class="column"
           style="width: 10%; border-right: solid lightgray 1px;">

        <aside class="menu">
          <p class="menu-label is-size-5 has-text-dark"
             style="margin-bottom: 2px;">
            Tags
          </p>

          <!-- list of tags -->
          <ul class="menu-list">
            <li v-for="current in TagJson"
                style="margin-left: -0.5em;">
              <a @click="setTagFilter(current.name)"
                 class="isclickable"
                 v-bind:class="{ 'is-size-6': isSelected(current), highlighted: isSelected(current) }">
                <span>
                  {{ current.name }}
                </span>
              </a>
            </li>
          </ul>
          <!-- end of tag list -->
        </aside>

      </div> <!-- end filters -->

      <!-- right side with books -->
      <div class="column is-11">
        <!-- when 'books' changes, a watcher in bookslist and booksgrid updates their view -->
        <ShelfAsList v-if="ViewState.viewAsList"
                     v-bind:allTags="TagJson"
                     :userBooks="AllData.UserBooksJson"></ShelfAsList>
        <ShelfAsGrid v-else
                     v-bind:allTags="TagJson"
                     :userBooks="AllData.UserBooksJson"></ShelfAsGrid>

        <infinite-loading @infinite="infiniteHandler" ref="infiniteLoading">
          <span slot="no-more">
            -- end shelf ({{ AllData.UserBooksJson.length }})--
          </span>
          <span slot="no-results">
            -- no results --
          </span>
        </infinite-loading>

      </div>
    </div>

    <!-- error message to user -->
    <article v-if="errorMessage"
             class="main-width message is-danger">
      <div class="message-body">
        {{ errorMessage }}
      </div>
    </article>
  </div>
</template>

<script>
  import Auth from '../../auth'
  import ViewHeader from '../ViewHeader.vue'
  import ShelfAsList from './ShelfAsList.vue'
  import ShelfAsGrid from './ShelfAsGrid.vue'
  import InfiniteLoading from 'vue-infinite-loading'
  import _ from 'lodash'

  export default {
    // Components we depend on
    components: { ViewHeader, ShelfAsList, ShelfAsGrid, InfiniteLoading },
    // Data for this component
    data () {
      return {
        // Error message
        errorMessage: '',
        // string to search/query books on
        searchBookString: '',
        // tags from server
        TagJson: [],
        /**
         * Contains the JSON for User Books
         * and the status of the infinite scrolling, e.g.
         * the current state of querying the server for data.
         */
        AllData: {
          // data from ajax
          UserBooksJson: [],
          // Index of data to query for
          dataStart: 0,
          // Length of data to get
          lengthToGet: 30,
          // Current end of our data length
          end: -1,
          // Total number of datum to get
          totalNumData: -1,
          // The GetAll button was clicked
          getAll: false,
          // Reset the data
          reset: function () {
            this.UserBooksJson = []
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
          viewAsList: true,
          // currently selected 'tag' filter
          tagFilter: ''
        }
      }
    },
    /**
     * When mounted, get the user books
     *
     */
    mounted: function () {
      // get user books from store
      let temp = this.$store.state.userBooks
      if (temp && temp.UserBooksJson) {
        this.AllData = temp
      }

      // Check status of filters and maybe get all values
      this.checkFilterStatus()

      // Get list/grid status
      temp = this.$store.state.userBooksView
      if (temp && temp.valid) {
        this.ViewState = temp
      }

      this.getTags()
    },
    /**
     * Get length of the userbooks' json
     */
    computed: {
      getCurrentLength: function () {
        return this.AllData.UserBooksJson.length
      }
    },
    /**
     * Methods
     */
    methods: {
      /**
       * Is the user logged in?
       */
      isLoggedIn () {
        return Auth.isAuthenticated()
      },
      /**
       * Infinite loading. Get list of User Books
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
        if (this.ViewState.tagFilter !== '') {
          params.tag = this.ViewState.tagFilter
        }

        let url = '/user_book/' + Auth.user.id
        this.isLoading = true

        // Make call
        this.$axios.get(url, {
          headers: { Authorization: authString },
          params: params })
          .then((response) => {
            self.isLoading = false
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
            if (self.AllData.UserBooksJson.length >= totalSize) {
              if ($state) {
                $state.loaded()
                $state.complete()
              }
              return
            }

            // Data set is unchanged, continue getting data
            // save list of user books
            self.AllData.UserBooksJson = _.concat(self.AllData.UserBooksJson, incomingData)
            self.AllData.end = start + length
            self.AllData.dataStart = start + self.AllData.lengthToGet
            self.AllData.totalNumData = totalSize

            // save to $store
            self.$store.commit('setUserBooks', self.AllData)

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
            self.isLoading = false
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
       * Get tags from database
       */
      getTags () {
        const authString = Auth.getAuthHeader()
        let self = this
        this.TagJson = {}
        this.$axios.get('/tag/', { headers: { Authorization: authString } })
          .then((response) => {
            self.TagJson = response.data.data
            // Sort
            self.TagJson = _.sortBy(self.TagJson, ['name'])
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
       * Set the tag filter
       */
      setTagFilter (newfilter) {
        if (this.ViewState.tagFilter === newfilter) {
          // If the highlighted tag was clicked, set it to off
          this.ViewState.tagFilter = ''
          // Signal that tag was cleared
          this.checkFilterStatus()
        } else {
          this.ViewState.tagFilter = newfilter
        }
        this.AllData.reset()
        this.$refs.infiniteLoading.$emit('$InfiniteLoading:reset')
      },
      /**
       * Return true if the incoming tag is the same as the filter
       */
      isSelected (tag) {
        if (tag.name === this.ViewState.tagFilter) {
          return true
        } else {
          return false
        }
      },
      /**
       * Show the grid view
       */
      showGrid () {
        this.ViewState.viewAsList = false
        this.$store.commit('setUserBooksView', this.ViewState)
      },
      /**
       * Show the list view
       */
      showList () {
        this.ViewState.viewAsList = true
        this.$store.commit('setUserBooksView', this.ViewState)
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
        this.ViewState.tagFilter = ''
        // Check if all filters are clear
        this.checkFilterStatus()
      },
      /**
       * If all filters have been cleared
       * AND the getAll flag is true, re-fetch all
       */
      checkFilterStatus () {
        if (this.ViewState.tagFilter === '' &&
             this.searchBookString === '') {
          if (this.AllData.getAll) {
            // Search is empty and the GetAll was previously clicked
            let self = this

            const authString = Auth.getAuthHeader()
            // Make query to just get # of user books
            let params = {
              limit: 0
            }
            let url = '/user_book/' + Auth.user.id
            this.$axios.get(url, {
              headers: { Authorization: authString },
              params: params })
              .then((response) => {
                // We got the number of user books
                let numBooks = response.data.total

                if (self.AllData.UserBooksJson.length === numBooks) {
                  // We already have the appropriate number of books
                  // skip doing a reload
                  return
                }

                // Now get all those books
                self.AllData.lengthToGet = numBooks
                self.AllData.dataStart = 0

                // Reset the scrolling like in grabAll
                self.AllData.getAll = true
                self.$refs.infiniteLoading.$emit('$InfiniteLoading:reset')
                self.infiniteHandler(null)
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
          this.AllData.lengthToGet = 30
        }
        this.AllData.getAll = true

        // Reset the scrolling
        this.$refs.infiniteLoading.$emit('$InfiniteLoading:reset')
        this.infiniteHandler(null)
      },
      /**
       * Print an Error to the user
       */
      printError (printThis) {
        let self = this
        this.errorMessage = printThis
        // Have the modal with errorMessage go away in bit
        setTimeout(function () {
          self.errorMessage = ''
        }, 2500)
      }
    }
  }
</script>

<style lang="css">
.highlighted {
  background-color: lightgray;
  }
.isclickable {
    cursor: pointer;
}
</style>
