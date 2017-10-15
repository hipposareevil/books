<!-- This represent a single userbook as a box in a grid -->
<template>
  <div v-if="showBook"
       class="has-text-centered">

    <div class="grid-div">

      <!-- cover -->
      <a @click="gotoBook">
        <figure class="image is-128x128 grid-figure">
          <img v-if="book.imageMedium"
               class="boxshadow grid-image"
               :src="book.imageMedium">
        </figure>
        <span style="overflow-wrap: break-word;">
          {{book.title}}
        </span>
      </a>

    </div>

  </div>
</template>

<script>
  import Auth from '../../auth'
  import _ from 'lodash'

  /**
   * Default data
   */
  export default {
    /**
     * Props for this userbook row:
     *
     * userBook: UserBook to be displayed
     * filter: String to filter titles/authors by
     * tagFilter: Currently selected tag in left column
     * allTags: list of all tags (id,name,data)
     */
    props: [ 'userBook', 'filter', 'tagFilter', 'allTags' ],
    /**
     * Data for this user book row
     */
    data () {
      return {
        // Data for the current book, this is retrieved once
        // we are mounted via this.getFullBook
        book: {}
      }
    },
    /**
     * When mounted, get the user books
     *
     */
    mounted: function () {
      this.getFullBook()
    },
    /**
     * Methods
     */
    methods: {
      /**
       * Get get book for id
       */
      getFullBook () {
        const authString = Auth.getAuthHeader()
        let self = this
        let url = '/book/' + this.userBook.bookId
        this.$axios.get(url, { headers: { Authorization: authString } })
          .then((response) => {
            self.book = response.data
          })
          .catch(function (error) {
            if (error.response.status === 401) {
              this.$emit('got401')
            } else {
              console.log(error)
            }
          })
      },
      /**
       * send the router to a single book
       *
       */
      gotoBook () {
        this.$router.push('/books/' + this.book.id)
      },
      /**
       * send the router to a single author
       *
       */
      gotoAuthor () {
        this.$router.push('/authors/' + this.userBook.authorId)
      }
    },
    /**
     * Computed value for 'showBook'.
     * This returns true if the row should be shown.
     * If the filter or tagFilter strings are non-null, we check
     * those against the title, author, date and the tags
     */
    computed: {
      showBook: function () {
        let firstFilter = true
        let secondFilter = true

        // Check filter
        if (this.filter) {
          // lower case the filter
          let filterString = _.lowerCase(this.filter)

          // Check the filter string against:
          // title, author, and date
          let titleString = _.lowerCase(this.book.title)
          let authorString = _.lowerCase(this.book.authorName)
          let dateString = String(this.book.firstPublishedYear)

          firstFilter = _.includes(authorString, filterString) ||
              _.includes(titleString, filterString) ||
              _.includes(dateString, filterString)
        }

        // Check tag filter
        if (this.tagFilter) {
          secondFilter = _.includes(this.userBook.tags, this.tagFilter)
        }

        // If both filters say show, then show it
        if (firstFilter && secondFilter) {
          return true
        } else {
          return false
        }
      }
    } // end computed
  }
</script>

<style>
.clickable {
  cursor: pointer
}

.boxshadow {
  box-shadow: 1px 1px 1px 1px rgba(0, 0, 0, .1);
}

</style>
