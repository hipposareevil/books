<!-- This represent a single userbook as a box in a grid -->
<template>
  <div class="has-text-centered">

    <div class="grid-div">

      <!-- cover -->
      <a @click="gotoBook">
        <figure class="image is-128x128 grid-figure">
          <img v-if="userBook.imageMedium"
               class="boxshadow grid-image"
               :src="userBook.imageMedium">
        </figure>
        <span style="overflow-wrap: break-word;">
          {{userBook.title}}
        </span>
      </a>

    </div>

  </div>
</template>

<script>
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
      }
    },
    /**
     * Methods
     */
    methods: {
      /**
       * send the router to a single book
       *
       */
      gotoBook () {
        this.$router.push('/books/' + this.userBook.bookId)
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
