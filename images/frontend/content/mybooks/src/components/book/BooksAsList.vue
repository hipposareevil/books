<template>
  <div>
    <!-- users table -->
    <table cellpadding="10"
           class="table main-width is-narrow is-stripped">
      <thead>
        <tr>
          <th style="width: 15%;" title="Cover">Cover</th>
          <th style="width: 30%;" title="Title">Title</th>
          <th style="width: 30%;" title="Author">Author</th>
          <th style="width: 7%;" title="Year">Year</th>
          <th style="width: 10%;"
              title="Actions">&nbsp;Actions</th>
        </tr>
      </thead>
      
      <tbody>

        <!-- SingleBookRow creates a tr -->
        <SingleBookRow
          v-for="currentBook in BooksJson"
          v-bind:currentBook="currentBook"
          :key="currentBook.id">
        </SingleBookRow>

      </tbody>
    </table>

  </div>

</template>

<script>
  import SingleBookRow from './SingleBookRow.vue'

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
        BooksJson: this.books
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
