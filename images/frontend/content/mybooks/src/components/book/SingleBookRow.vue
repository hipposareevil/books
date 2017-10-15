<template>
  <tr>
    <td>
      <a @click="gotoBook(currentBook)">
        <figure class="image is-128x128">
          <img v-if="currentBook.imageMedium"
               class="boxshadow"
               style="width: auto; max-height: 128px;"
               :src="currentBook.imageMedium">
        </figure>
      </a>
    </td>

    <th>
      <a @click="gotoBook(currentBook)">
        {{currentBook.title}}
      </a>
    </th>

    <td>
      <a @click="gotoAuthor(currentBook)">
        {{ currentBook.authorName }}
      </a>
    </td>

    <td>
      {{ currentBook.firstPublishedYear }}
    </td>

    <!-- actions -->
    <td>
      <button class="button is-info is-small"
              v-bind:class="{'is-success' : userbookWasAdded.flag, 'is-danger' : userbookWasAddedError.flag}" 
              @click="addBookToList(currentBook)">
        <span>Add To My Books</span>
      </button>
    </td> <!-- end action -->
  </tr>
</template>

<script>
  import Auth from '../../auth'
  /**
   * Default data
   */
  export default {
    /**
     * Props for this Book row:
     *
     */
    props: [ 'currentBook' ],
    /**
     * Data for this user book row
     */
    data () {
      return {
        // Flag denoting the book was added
        userbookWasAdded: {
          flag: false
        },
        // Flag showing error for userbook being added
        userbookWasAddedError: {
          flag: false
        }
      }
    },
    /**
     * Method
     */
    methods: {
      /**
       * send the router to a single book
       *
       */
      gotoBook (current) {
        this.$router.push('/books/' + current.id)
      },
      /**
       * send the router to a single author
       *
       */
      gotoAuthor (current) {
        this.$router.push('/authors/' + current.authorId)
      },
      /**
       * Add book to a shelf. Notify the ViewBook component
       */
      addBookToList (book) {
        console.log('ViewBooks.bookWasAddedForUser: book title ' + book.title)
        // Get user id from Auth
        let userId = Auth.user.id

        let self = this
        let ourheaders = {
          'Authorization': Auth.getAuthHeader()
        }

        // Data to update in database
        let data = {
          bookId: book.id
        }

        // Try to create a new user_book
        let url = '/user_book/' + userId
        this.$axios.post(url, data, { headers: ourheaders })
          .then((response) => {
            self.userbookWasAdded.flag = true
            setTimeout(function () {
              self.userbookWasAdded.flag = false
            }, 1200)
          })
          .catch(function (error) {
            if (error.response) {
              let statusCode = error.response.status
              if (statusCode === 409) {
                self.userbookWasAddedError.flag = true
                setTimeout(function () {
                  self.userbookWasAddedError.flag = false
                }, 1200)
              }
            }
            console.log(error)
          })
      }
    }
  }
</script>

<style>
</style>


    
