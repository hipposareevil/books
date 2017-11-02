<template>
  <div>
    <!-- book table -->
    <table cellpadding="10"
           class="table main-width is-narrow is-stripped">
      <thead>
        <tr>
          <th style="width: 15%;"
              title="Cover">Cover</th>

          <th style="width: 30%;"
              class="clickable"
              @click="sortBy('title')"
              title="Title">Title</th>

          <th style="width: 30%;"
              class="clickable"
              @click="sortBy('authorName')"
              title="Author">Author</th>

          <th style="width: 7%;"
              class="clickable"
              @click="sortBy('firstPublishedYear')"
              title="Year">Year</th>

          <th style="width: 10%;"
              title="Actions">&nbsp;Actions</th>
        </tr>
      </thead>
      
      <tbody>

        <!-- SingleBookRow creates a tr -->
        <SingleBookRow
          v-for="currentBook in bookList"
          v-bind:currentBook="currentBook"
          :key="currentBook.id">
        </SingleBookRow>

      </tbody>
    </table>

  </div>

</template>

<script>
  import SingleBookRow from './SingleBookRow.vue'
  import _ from 'lodash'

  /**
   * Default data
   */
  export default {
    components: { SingleBookRow },
    // Props for this component
    props: [ 'books' ],
    // Data for this component
    data () {
      return {
        // books to display
        BooksJson: this.books,
        // How to sort the columns
        SortColumns: {
          order: 'asc',
          columnName: ''
        }
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
        let newlist = _.orderBy(this.BooksJson, [this.SortColumns.columnName], [this.SortColumns.order])
        return newlist
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
      books: function (val, oldVal) {
        this.BooksJson = val
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

.clickable  {
    cursor: pointer
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
.boxshadow {
    box-shadow: 1px 1px 1px 1px rgba(0, 0, 0, .1);
}

</style>
