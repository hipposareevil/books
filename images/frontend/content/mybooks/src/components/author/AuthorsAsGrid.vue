<template>
  <div>
    <div class="columns is-multiline is-left is-gapless">

      <!-- authors list -->
      <div v-for="current in AuthorsJson"
           style="margin: 0.75em;"
           class="has-text-centered column is-2">

        <!-- author data -->
        <a @click="gotoAuthor(current)">
          <div class="grid-div">

            <figure class="image is-128x128 grid-figure">
              <img v-if="current.imageMedium"
                   class="grid-image"
                   :src="current.imageMedium">
            </figure>
            <br>
            {{current.name}}
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
    props: [ 'authors' ],
    // Data for this component
    data () {
      return {
        AuthorsJson: this.authors
      }
    },
    methods: {
      gotoAuthor (current) {
        this.$router.push('/authors/' + current.id)
      }
    },
    watch: {
      authors: function (val, oldVal) {
        this.AuthorsJson = val
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

.author {
   cursor: pointer
}


</style>
