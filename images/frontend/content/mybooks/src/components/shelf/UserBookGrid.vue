<!-- This represent a single userbook as a box in a grid -->
<template>
  <div class="has-text-centered">

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

  /**
   * Default data
   */
  export default {
    /**
     * Props for this userbook row:
     *
     * userBook: UserBook to be displayed
     * allTags: list of all tags (id,name,data)
     */
    props: [ 'userBook', 'allTags' ],
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
    }
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
