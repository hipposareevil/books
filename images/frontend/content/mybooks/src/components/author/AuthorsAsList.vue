<template>
  <div>

    <!-- authors list -->
    <ul v-for="current in AuthorsJson">

      <!-- single author -->
      <li style="margin-top: 1em; padding-bottom: 1em; border-bottom: 1px lightgray solid;" >

        <a @click="gotoAuthor(current)">
          <article class="media"
                   style="width: 22%;">
            <figure class="media-left">
              <p class="image is-128x128">
                <img v-if="current.imageMedium"
                     class="boxshadow"
                     style="width: auto; max-height: 128px;"
                     :src="current.imageMedium">
              </p>
            </figure>
            <div class="media-content">
              <div class="content">
                <p>
                  <strong>{{current.name}}</strong>
                </p>
              </div>
            </div>
<!-- TODO Delete
            <div class="media-right">
              <button v-if="is_authenticated('admin')" class="delete is-small"></button>
            </div>
-->
          </article>
        </a>

      </li> <!-- end of author -->

    </ul> <!-- end author list -->


  </div>
</template>

<script>
  import Auth from '../../auth'

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
      /**
       * Check if the current user is authenticated
       * for the desired group
       */
      is_authenticated (groupToCheck) {
        // Refresh from local storage
        Auth.checkAuth()
        // If user has been set
        return Auth.isAuthenticated(groupToCheck)
      },
      /**
       * Re route to the author webpage
       *
       */
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

.boxshadow {
    box-shadow: 1px 1px 1px 1px rgba(0, 0, 0, .1);
}

</style>
