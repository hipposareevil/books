<template>
  <div class="field has-addons">

    <!-- list the number of things -->
    <p class="control"
       style="margin-right: 1em;"
       title="Click to get all"
       v-if="numberOfThings">

      <button class="button"
              v-bind:class="{'is-loading' : showGrabLoading }"
              @click="grabAll()">
        <span class="is-size-7">
          ({{ numberOfThings }}
          <span v-if="totalNumber">&nbsp;of {{ totalNumber }})</span>
        </span>
      </button>
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
</template>

<script>
  /**
   * Data and methods
   */
  export default {
    // Props for this component
    props: ['numberOfThings', 'showAsList', 'totalNumber', 'isLoading', 'grabIsLoading'],
    // Data for this component
    data () {
      return {
        // when true, view as list
        showList: this.showAsList,
        // when true, the clear button is loading
        showLoading: this.isLoading,
        // when true, the graball button is loading
        showGrabLoading: this.grabIsLoading
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
       * Signal to grab everything
       */
      grabAll () {
        this.$emit('grabAll')
      }
    },
   /**
    * What things to watch for
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
