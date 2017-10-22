<template>
  <!-- Main container -->
  <div>

    <!-- Header for filter, search, list vs grid -->
    <ViewHeader theThing="Authors"
                :numberOfThings="lengthOfViewableAuthors"
                :showAsList="ViewState.viewAsList"
                @gridOn="showGrid"
                @listOn="showList"
                @searchString="searchStringUpdated"
                @filterString="filterStringUpdated"
                >
    </ViewHeader>

    <AuthorsList v-if="ViewState.viewAsList" 
                 :authors="authorList"></AuthorsList>
    <AuthorsGrid v-else 
                 :authors="authorList"></AuthorsGrid>


    <infinite-loading @infinite="infiniteHandler">
      <span slot="no-more">
        -- end authors ({{ lengthOfViewableAuthors }}) --
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
        // string to filter on
        filterAuthorString: '',
        // length of viewable authors
        lengthOfViewableAuthors: 0,
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
          totalNumData: 0
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
     * When mounted, get the stored data
     *
     */
    mounted: function () {
      // get authors from store
      let temp = this.$store.state.allAuthors
      if (temp && temp.AuthorsJson) {
        this.AllData = temp
      }

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
        let url = '/author'

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
              console.log('ViewAuthors: Dataset length for userbooks has changed. Resetting.')

              self.AllData.AuthorsJson = []
              self.AllData.dataStart = 0
              self.AllData.end = -1
              self.AllData.totalNumData = -1

              $state.reset()
              return
            }

            // This method could be called when we come back to the tab
            // so check that we have all the data or not
            if (self.AllData.AuthorsJson.length >= totalSize) {
              $state.loaded()
              $state.complete()
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

            $state.loaded()

            if (self.AllData.end >= self.AllData.totalNumData) {
              $state.complete()
            }
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
      },
      /**
       * The filter string has been updated
      */
      filterStringUpdated (value) {
        this.filterAuthorString = value
      }
    },
    computed: {
      authorList: function () {
        let result = this.AllData.AuthorsJson
        this.lengthOfViewableAuthors = result.length

        if (!this.filterAuthorString) {
          return result
        }

        const filterValue = this.filterAuthorString.toLowerCase()

        const filter = event =>
          event.name.toLowerCase().includes(filterValue)

        let filteredResult = result.filter(filter)
        this.lengthOfViewableAuthors = filteredResult.length
        return filteredResult
      }
    }
  }
</script>

<style>
a:hover {
    color: black;
}

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
