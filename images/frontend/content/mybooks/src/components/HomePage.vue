<template>
  <div>
    <div class="title is-3 has-text-centered">
      My Book Library
    </div>
    <img class="boxshadow"
         src="/static/library_medium.jpg">

    <br>
    <div class="title is-5 has-text-left"
         style="padding-top: 1em;">
      <span v-if="userBooks.length > 0" >
        Last books added to
        <a class="clickable"
           @click="gotoMyBooks" >My Books</a>:
      </span>
    </div>

    <ul>

      <li v-for="current in userBooks">
        <a @click="gotoBook(current)">
          - {{ current.title }} 
        </a>
      </li>
    </ul>

  </div>
</template>

<script>
  import Auth from '../auth'
  //  number of 'last' books to get
  let NUM_BOOKS = 10

  export default {
    // Data
    data () {
      return {
        userBooks: []
      }
    },
    /**
     * When mounted, get the user books
     */
    mounted: function () {
      // Only try to get userbooks if we are logged in
      if (Auth.isAuthenticated()) {
        this.getUserBooks()
      }
    },
    /**
     * Setup when created
     */
    created () {
      Event.$on('loggedOut', () => this.loggedOut())
      Event.$on('loggedIn', () => this.getUserBooks())
    },
    /**
     * Methods
     */
    methods: {
      /**
       * The user was logged out, so refresh our data
       */
      loggedOut () {
        if (!Auth.isAuthenticated()) {
          // Not logged in, so remove the user books
          this.userBooks = []
        }
      },
      /**
       * send the router to a single book
       */
      gotoBook (current) {
        this.$router.push('/books/' + current.bookId)
      },
      /**
       * send the router to the shelves
       */
      gotoMyBooks () {
        this.$router.push('/shelves')
      },
      /**
       * Get user books to then sort by date to display to user
       */
      getUserBooks () {
        let self = this

        const authString = Auth.getAuthHeader()
        // Make query to just get the # of books
        let params = {
          limit: 0
        }
        // find number of books
        let url = '/user_book/' + Auth.user.id
        this.$axios.get(url, {
          headers: { Authorization: authString },
          params: params })
          .then((response) => {
            let numBooks = response.data.total
            let offset = (numBooks - NUM_BOOKS)

            // Get last NUM_BOOKS books
            params = {
              offset: offset
            }
            this.$axios.get(url, {
              headers: { Authorization: authString },
              params: params })
              .then((response) => {
                // Got the user books
                self.userBooks = response.data.data
              })
          })
          .catch(function (error) {
            if (error.response) {
              if (error.response.status === 401) {
                Event.$emit('got401')
              }
            } else {
              console.log(error)
            }
          })
      } // end getUserBooks
    }
  }
</script>

<style>
a {
  color: #4a4a4a;
}
a:hover {
  background-color: rgb(245,245,245)
}


.boxshadow {
    box-shadow: 2px 2px 1px 1px rgba(0, 0, 0, .1);
}
.isclickable:hover {
    background-color: rgb(245,245,245)
}

</style>
