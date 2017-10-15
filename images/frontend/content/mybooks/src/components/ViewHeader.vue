<template>
  <div style="padding-bottom: 0.5em; margin-bottom: 1em; border-bottom: solid gray 1px;">

    <nav class="level">

      <div class="level-left">
        <div class="level-item has-text-centered">
          <div class="field has-addons">
            <input class="input"
                   style="width: 7em;"
                   type="text"
                   placeholder="filter"
                   v-model="filterString">
            <p class="control">
              <button class="button"
                      @click="filterString = ''">
                Clear
              </button>
            </p>
            <p class="control"
              v-if="numberOfThings">
              <a class="button is-static">
                ({{ numberOfThings }})
              </a>
            </p>

          </div>
        </div>
      </div>


      <div class="level-right">

        <div class="level-item has-text-centered">

          <div class="level-item">

            <!-- search and clear -->
            <div class="field has-addons">
              <p class="control">
                <input class="input" type="text"
                       v-bind:placeholder="getPlaceHolder()"
                       v-model="searchString">
              </p>
              <p class="control">
                <button class="button"
                        @click="searchString = ''">
                  Clear
                </button>
              </p>
            </div>
          </div>
          <div class="level-item">
            <div class="field has-addons">

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
  /**
   * Data and methods
   */
  export default {
    // Props for this component
    props: [ 'theThing', 'numberOfThings' ],
    // Data for this component
    data () {
      return {
        // String to search on
        searchString: '',
        // String to filter on
        filterString: '',
        // when true, view as list
        showList: true
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
      }
    },
    watch: {
      filterString: function (val, oldVal) {
        this.$emit('filterString', val)
      },
      searchString: function (val, oldVal) {
        this.$emit('searchString', val)
      }
    }
  }
</script>

<style>
</style>
