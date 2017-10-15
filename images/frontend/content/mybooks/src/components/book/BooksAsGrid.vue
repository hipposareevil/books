<template>
  <div>
    <div class="columns is-multiline is-left is-gapless">

      <!-- books list -->
      <div v-for="current in BooksJson"
           style="margin: 0.75.em"
           class="has-text-centered column is-2">

        <!-- book data -->
        <a @click="gotoBook(current)">
          <div class="grid-div">

            <figure class="image is-128x128 grid-figure">
              <img v-if="current.imageMedium"
                   class="grid-image"
                   :src="current.imageMedium">
            </figure>
            <br>
            {{current.title}}
          </div>
        </a>

      </div> <!-- end of v-for -->

    </div> <!-- Main colums -->

  </div> <!-- main div -->
</template>

<script>
 /**
  * Default data
  */
  export default {
    props: [ 'books' ],
    // Data for this component
    data () {
      return {
        BooksJson: this.books
      }
    },
    methods: {
      gotoBook (current) {
        this.$router.push('/books/' + current.id)
      }
    },
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
</style>
