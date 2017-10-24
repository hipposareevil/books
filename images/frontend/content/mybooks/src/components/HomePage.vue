<template>
  <div>
    <div class="title is-3 has-text-centered">
      My Book Library
    </div>
    <img class="boxshadow"
         src="../assets/library_medium.jpg">

    <br>
    <div class="title is-5 has-text-left"
         style="padding-top: 1em;">
      <span style="">
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
        let params = {
          offset: 0,
          limit: 1000
        }
        let url = '/user_book/' + Auth.user.id + '?last_added=5'
        this.$axios.get(url, {
          headers: { Authorization: authString },
          params: params })
          .then((response) => {
            self.userBooks = response.data.data
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
.boxshadow {
    box-shadow: 2px 2px 1px 1px rgba(0, 0, 0, .1);
}
.isclickable:hover {
    background-color: rgb(245,245,245)
}

</style>
