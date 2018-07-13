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

            </div> <!-- search and clear -->
          </div>
        </div> 
      </div> <!-- level left -->


      <div class="level-right">
        <div class="level-item has-text-centered">
          <div class="level-item">

              <!-- right side of header -->
              <RightHeader
                :numberOfThings="numberOfThings"
                :totalNumber="totalNumber"
                :showAsList="showAsList"
                :isLoading="isLoading"
                :grabIsLoading="showGrabLoading"
                @gridOn="sendShowGrid"
                @listOn="sendShowList"
                @grabAll="sendGrabAll">
              </RightHeader>

          </div>
        </div>
      </div>
    </nav>

  </div>
</template>

<script>
  import _ from 'lodash'
  import RightHeader from './RightHeader.vue'

  /**
   * Data and methods
   */
  export default {
    components: { RightHeader },
    // Props for this component
    props: ['theThing', 'numberOfThings', 'showAsList', 'totalNumber', 'isLoading', 'grabIsLoading'],
    // Data for this component
    data () {
      return {
        // String to search on
        searchString: '',
        // when true, view as list
        showList: this.showAsList,
        // when true, the clear button is loading
        showLoading: this.isLoading,
        // when true, the graball button is loading
        showGrabLoading: this.grabIsLoading
      }
    },
    methods: {
      sendShowGrid () {
        this.$emit('gridOn')
        this.showList = false
      },
      sendShowList () {
        this.$emit('listOn')
        this.showList = true
      },
      sendGrabAll () {
        this.$emit('grabAll')
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
      }
    },
    /**
     * debounce some functions
     */
    created: function () {
      this.debouncedSearch = _.debounce(this.search, 500)
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
      },
      grabIsLoading: function (val, oldVal) {
        this.showGrabLoading = val
      }
    }
  }
</script>

<style>
</style>
