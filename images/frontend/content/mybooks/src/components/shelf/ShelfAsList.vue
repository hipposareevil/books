<template>
  <div>
    <!-- users table -->
    <table cellpadding="10"
           class="table main-width is-narrow is-stripped">
      <thead>
        <tr>
          <th style="width: 15%;"
              title="Cover">Cover</th>
          <th style="width: 25%;"
              title="Title">Title</th>
          <th style="width: 15%;"
              title="Author">Author</th>
          <th style="width: 5%;"
              title="Year">Year</th>
          <th style="width: 4%;"
              title="Year">Rating</th>
          <th style="width: 18%;"
              class="has-text-centered"
              title="Actions">Tags</th>
        </tr>
      </thead>
      
      <tbody>

        <!-- userbookrow creates a tr -->
        <UserBookRow
          v-for="currentUserBook in UserBooksJson"
          v-bind:allTags="allTags"
          v-bind:userBook="currentUserBook"
          v-bind:filter="filter"
          v-bind:tagFilter="tagFilter"
          :key="currentUserBook.userBookId"
          >
        </UserBookRow>

      </tbody>
    </table>

  </div>

</template>

<script>
  import UserBookRow from './UserBookRow.vue'

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
    components: { UserBookRow },
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
.fade-enter-active, .fade-leave-active {
  transition: opacity 5s
}

.main-width {
  width: 65%
}

.book {
   cursor: pointer
}

.taglist {
    border: solid lightgray 1px;
    margin-left: 1em;
    width: 80%;
    height: 100px;
    overflow: auto;
}

</style>
