<template>
  <div style="padding-bottom: 0.5em; margin-bottom: 1em; border-bottom: solid gray 1px;">

    <nav class="level">

      <div class="level-left">
        <div class="level-item has-text-centered">

          <div class="level-item">

            <!-- search and clear -->
            <div class="field has-addons">
              <p class="control">
                <input class="input"
                       type="text"
                       v-on:input="debouncedSearch"
                       v-bind:placeholder="getPlaceHolder()"
                       v-model="searchString">
              </p>
              <p class="control">
                <button class="button is-link"
                        v-bind:class="{'is-loading' : showLoading }"
                        @click="search">
                  Search
                </button>
              </p>
              <p class="control">
                <button class="button"
                        @click="clearSearch">
                  Clear
                </button>
              </p>

              <!-- grab all -->
              <p class="control">
                <button class="button"
                        title="Get all"
                        @click="grabAll()">
                  <span class="has-text-info">
                    Get All
                  </span>
                </button>
              </p>
            </div> <!-- search and clear -->
          </div>
        </div> 
      </div> <!-- level left -->


      <div class="level-right">

        <div class="level-item has-text-centered">

          <div class="level-item">
            <div class="field has-addons">

            <!-- list the number of things -->
            <p class="control"
               style="margin-right: 1em;"
              v-if="numberOfThings">
              <a class="button is-static">
                <span class="is-size-7">
                ({{ numberOfThings }}
                <span v-if="totalNumber">&nbsp;of {{ totalNumber }})</span>
                </span>
              </a>
            </p>

              <!-- List -->
              <button class="button"
                      v-bind:class="{ 'is-info': showList, 'is-outlined': showList }"
                      @click="viewAsList">
                <span class="icon is-medium">
                  <i class="fa fa-bars"></i>
                </span>
              </button>

              <!-- Grid -->
              <button v-bind:class="{ 'is-info': !showList, 'is-outlined': !showList }"
                      class="button"
                      @click="viewAsGrid" >
                <span class="icon is-medium">
                  <i class="fa fa-th"></i>
                </span>
              </button>
            </div>
          </div>


        </div>
      </div>
    </nav>

  </div>
</template>

<script>
  import _ from 'lodash'

  /**
   * Data and methods
   */
  export default {
    // Props for this component
    props: [ 'theThing', 'numberOfThings', 'showAsList', 'totalNumber', 'isLoading' ],
    // Data for this component
    data () {
      return {
        // String to search on
        searchString: '',
        // when true, view as list
        showList: this.showAsList,
        // when true, the clear button is loading
        showLoading: this.isLoading
      }
    },
    methods: {
      /**
       * User clicked on List view
       */
      viewAsList () {
        this.showList = true
        this.$emit('listOn')
      },
      /**
       * User clicked on Grid view
       */
      viewAsGrid () {
        this.showList = false
        this.$emit('gridOn')
      },
      /**
       * Create 'placeholder' string
       *
       */
      getPlaceHolder () {
        return 'Search ' + this.theThing
      },
      /**
       * Send the search string to the parent
       */
      search () {
        this.$emit('searchString', this.searchString)
      },
      /**
       * Clear the search string
       */
      clearSearch () {
        this.searchString = ''
        this.$emit('searchString', this.searchString)
        this.$emit('clearCalled')
      },
      /**
       * Signal to grab everything
       */
      grabAll () {
        this.$emit('grabAll')
      }
    },
    /**
     * debounce some functions
     */
    created: function () {
      this.debouncedSearch = _.debounce(this.search, 250)
    },
   /**
    * What strings to watch for
    */
    watch: {
      showAsList: function (val, oldVal) {
        this.showList = val
      },
      isLoading: function (val, oldVal) {
        this.showLoading = val
      }
    }
  }
</script>

<style>
</style>
