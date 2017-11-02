<template>
  <div>
    <!-- users table -->
    <table cellpadding="10"
           class="table main-width is-narrow is-stripped">
      <thead>
        <tr>
          <th style="width: 13%;"
              title="Cover">Cover</th>

          <th style="width: 30%;"
              class="clickable"
              @click="sortBy('title')"
              title="Title">Title</th>

          <th style="width: 18%;"
              class="clickable"
              @click="sortBy('authorName')"
              title="Author">Author</th>

          <th style="width: 5%;"
              class="clickable"
              @click="sortBy('firstPublishedYear')"
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
          v-for="currentUserBook in bookList"
          v-bind:allTags="allTags"
          v-bind:userBook="currentUserBook"
          :key="currentUserBook.userBookId"
          >
        </UserBookRow>

      </tbody>
    </table>

  </div>

</template>

<script>
  import UserBookRow from './UserBookRow.vue'
  import _ from 'lodash'

  /**
   * Default data
   */
  export default {
    /**
     * Props for this component
     *
     * userBooks: Json of all userbooks
     * allTags: list of all tags (id,name,data)
     */
    props: [ 'userBooks', 'allTags' ],
    // Components we depend on
    components: { UserBookRow },
    // Data for this component
    data () {
      return {
        // Books to display
        UserBooksJson: this.userBooks,
        // How to sort the columns
        SortColumns: {
          order: 'asc',
          columnName: ''
        }
      }
    },
    /**
     * Methods
     */
    methods: {
      /**
       * Sort the books by the incoming column
       *
       */
      sortBy (column) {
        // flip order or sorting
        if (this.SortColumns.order === 'asc') {
          this.SortColumns.order = 'desc'
        } else {
          this.SortColumns.order = 'asc'
        }

        this.SortColumns.columnName = column
      }
    },
    /**
     * Watch a property and update our model accordingly
     */
    watch: {
      userBooks: function (val, oldVal) {
        this.UserBooksJson = val
      }
    },
    /**
     * Compute values
     */
    computed: {
      /**
       * Compute the book list
       */
      bookList: function () {
        let newlist = _.orderBy(this.UserBooksJson, [this.SortColumns.columnName], [this.SortColumns.order])
        return newlist
      }
    }
  }
</script>

<style>
.fade-enter-active, .fade-leave-active {
  transition: opacity 5s
}

.main-width {
  width: 80%
}

.book {
   cursor: pointer
}
.clickable  {
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
