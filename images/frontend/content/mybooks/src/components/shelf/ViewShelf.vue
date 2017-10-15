<template>
  <div>

    <!-- Header for filter, search, list vs grid -->
    <ViewHeader theThing="My Books"
                :numberOfThings="getCurrentLength"
                @gridOn="showGrid"
                @listOn="showList"
                @searchString="searchStringUpdated"
                @filterString="filterStringUpdated"
                >
    </ViewHeader>

    <div class="columns">
      <!-- left side column with tag filters -->
      <div class="column is-1 is-narrow"
           style="border-right: solid lightgray 1px;">

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
        <ShelfAsList v-if="viewAsList"
                     v-bind:filter="filterBookString"
                     v-bind:tagFilter="tagFilter"
                     v-bind:allTags="TagJson"
                     :userBooks="AllData.UserBooksJson"></ShelfAsList>
        <ShelfAsGrid v-else
                     v-bind:filter="filterBookString"
                     v-bind:tagFilter="tagFilter"
                     v-bind:allTags="TagJson"
                     :userBooks="AllData.UserBooksJson"></ShelfAsGrid>

        <infinite-loading @infinite="infiniteHandler">
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
        // flag to determine if we present as list or grid
        viewAsList: true,
        // string to filter books on
        filterBookString: '',
        // string to search/query books on
        searchBookString: '',
        // currently selected 'tag' filter
        tagFilter: '',
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
          lengthToGet: 15,
          // Current end of our data length
          end: -1,
          // Total number of datum to get
          totalNumData: 0
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
       * Infinite loading. Get list of User Books
       */
      infiniteHandler ($state) {
        let self = this

        let url = '/user_book/' + Auth.user.id
        const authString = Auth.getAuthHeader()
        this.$axios.get(url, { headers: { Authorization: authString }, params: { start: self.AllData.dataStart, segmentSize: self.AllData.lengthToGet } })
          .then((response) => {
            let incomingData = response.data.data
            let start = response.data.start
            let length = response.data.length
            // save list of user books
            self.AllData.UserBooksJson = _.concat(self.AllData.UserBooksJson, incomingData)

            $state.loaded()

            // Update our pointers (start, end, so on
            self.AllData.totalNumData = response.data.totalFound
            self.AllData.end = start + length
            self.AllData.dataStart = start + self.AllData.lengthToGet

            if (self.AllData.end >= self.AllData.totalNumData) {
              $state.complete()
            }
            // save to $store
            this.$store.commit('setUserBooks', self.AllData)
          })
          .catch(function (error) {
            $state.complete()
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
        if (this.tagFilter === newfilter) {
          // Reset tag filter to empty
          this.tagFilter = ''
        } else {
          this.tagFilter = newfilter
        }
      },
      /**
       * Return true if the incoming tag is the same as the filter
       */
      isSelected (tag) {
        if (tag.name === this.tagFilter) {
          return true
        } else {
          return false
        }
      },
      /**
       * Show the grid view
       */
      showGrid () {
        this.viewAsList = false
      },
      /**
       * Show the list view
       */
      showList () {
        this.viewAsList = true
      },
      /**
       * The search string has been updated
       */
      searchStringUpdated (value) {
        this.searchBookString = value
      },
      /**
       * The filter string has been updated
      */
      filterStringUpdated (value) {
        this.filterBookString = value
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
</style>
