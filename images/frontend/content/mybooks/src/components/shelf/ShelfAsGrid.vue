<template>
  <div>
    <div class="columns is-multiline is-left is-gapless">

      <!-- user books list -->
      <UserBookGrid
        class="column is-2"
        style="margin: 0.75em;"
        v-for="currentUserBook in UserBooksJson"
        v-bind:allTags="allTags"
        v-bind:userBook="currentUserBook"
        v-bind:filter="filter"
        v-bind:tagFilter="tagFilter"
        :key="currentUserBook.userBookId"
        >
      </UserBookGrid>

    </div> <!-- Main colums -->
  </div> <!-- main div -->
</template>


<script>
  import UserBookGrid from './UserBookGrid.vue'

  /**
   * Default data
   */
  export default {
    /**
     * Props for this component
     *
     * userBooks: Json of all userbooks
     * filter: String to filter titles/authors by
     * tagFilter: Currently selected tag in left column
     * allTags: list of all tags (id,name,data)
     */
    props: [ 'userBooks', 'filter', 'tagFilter', 'allTags' ],
    // Components we depend on
    components: { UserBookGrid },
    // Data for this component
    data () {
      return {
        // books to display
        UserBooksJson: this.userBooks
      }
    },
    methods: {
    },
   /**
    * Watch a property and update our model accordingly
    */
    watch: {
      userBooks: function (val, oldVal) {
        this.UserBooksJson = val
      }
    }
  }
</script>

<style>
.book {
   cursor: pointer
}

</style>
