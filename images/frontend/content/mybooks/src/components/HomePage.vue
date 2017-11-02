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
  let NUM_BOOKS = 5

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
      this.getUserBooks()
    },
    /**
     * Methods
     */
    methods: {
      /**
       * send the router to a single book
       *
       */
      gotoBook (current) {
        this.$router.push('/books/' + current.bookId)
      },
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
