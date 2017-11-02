<template>
  <!-- Main container -->
  <div>

    <!-- Header for search, list vs grid -->
    <ViewHeader theThing="Authors"
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

    <AuthorsList v-if="ViewState.viewAsList" 
                 :authors="AllData.AuthorsJson"></AuthorsList>
    <AuthorsGrid v-else 
                 :authors="AllData.AuthorsJson"></AuthorsGrid>


    <infinite-loading @infinite="infiniteHandler" ref="infiniteLoading">
      <span slot="no-more">
        -- end authors ({{ getCurrentLength }}) --
      </span>
      <span slot="no-results">
        -- no results --
      </span>
    </infinite-loading>


  </div>
</template>


<script>
  import Auth from '../../auth'
  import AuthorsList from './AuthorsAsList.vue'
  import AuthorsGrid from './AuthorsAsGrid.vue'
  import ViewHeader from '../ViewHeader.vue'
  import InfiniteLoading from 'vue-infinite-loading'
  import _ from 'lodash'

 /**
  * Default data
  */
  export default {
    // Components we depend on
    components: { AuthorsList, AuthorsGrid, ViewHeader, InfiniteLoading },
    // Data for this component
    data () {
      return {
        // string to search/query authors on
        searchAuthorString: '',
        /**
         * Contains the JSON for Authors
         * and the status of the infinite scrolling, e.g.
         * the current state of querying the server for data.
         */
        AllData: {
          // data from ajax
          AuthorsJson: [],
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
          // Reset the data
          reset: function () {
            this.AuthorsJson = []
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
     * Get length of the author json
     */
    computed: {
      getCurrentLength: function () {
        return this.AllData.AuthorsJson.length
      }
    },
    /**
     * When mounted, get the stored data
     *
     */
    mounted: function () {
      // get authors from store
      let temp = this.$store.state.allAuthors
      if (temp && temp.AuthorsJson) {
        this.AllData = temp
      }

      // Check status of filters and maybe get all values
      this.checkFilterStatus()

      // Get list/grid status
      temp = this.$store.state.authorsView
      if (temp && temp.valid) {
        this.ViewState = temp
      }
    },
    /**
     * Methods
     */
    methods: {
      /**
       * Infinite loading. Get the list of authors
       */
      infiniteHandler ($state) {
        let self = this

        const authString = Auth.getAuthHeader()
        let params = {
          offset: self.AllData.dataStart,
          limit: self.AllData.lengthToGet
        }
        if (this.searchAuthorString !== '') {
          params.name = this.searchAuthorString
        }

        let url = '/author'
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
            if (self.AllData.AuthorsJson.length >= totalSize) {
              if ($state) {
                $state.loaded()
                $state.complete()
              }
              return
            }
            // Data set is unchanged, continue getting data

            // save author data
            self.AllData.AuthorsJson = _.concat(self.AllData.AuthorsJson, incomingData)
            self.AllData.end = start + length
            self.AllData.dataStart = start + self.AllData.lengthToGet
            self.AllData.totalNumData = totalSize

            // save to $store
            this.$store.commit('setAllAuthors', self.AllData)

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
        this.$store.commit('setAuthorsView', this.ViewState)
      },
      /**
       * Show the list view
       */
      showList () {
        this.ViewState.viewAsList = true
        this.$store.commit('setAuthorsView', this.ViewState)
      },
      /**
       * The search string has been updated
       */
      searchStringUpdated (value) {
        this.searchAuthorString = value
        this.AllData.reset()
        this.$refs.infiniteLoading.$emit('$InfiniteLoading:reset')
      },
      /**
       * The clear button was clicked
       */
      clearCalledFromHeader () {
        this.searchAuthorString = ''
        // Check if all filters are clear
        this.checkFilterStatus()
      },
      /**
       * If all filters have been cleared
       * AND the getAll flag is true, re-fetch all
       */
      checkFilterStatus () {
        if (this.searchAuthorString === '') {
          if (this.AllData.getAll) {
            // Search is empty and the GetAll was previously clicked
            let self = this

            const authString = Auth.getAuthHeader()
            // Make query to just get # of authors
            let params = {
              limit: 0
            }
            let url = '/author'
            this.$axios.get(url, {
              headers: { Authorization: authString },
              params: params })
              .then((response) => {
                // We got the number of authors
                let numAuthors = response.data.total

                if (self.AllData.AuthorsJson.length === numAuthors) {
                  // We already have the appropriate number of authors
                  // skip doing a reload
                  return
                }

                // Now get all those authors
                self.AllData.lengthToGet = numAuthors
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
        this.AllData.getAll = true

        // Reset the scrolling
        this.$refs.infiniteLoading.$emit('$InfiniteLoading:reset')
        this.infiniteHandler(null)
      }
    }
  }
</script>

<style>

.fade-enter-active, .fade-leave-active {
  transition: opacity 5s
}

.main-width {
  width: 65%
}

.author {
   cursor: pointer
}
</style>
