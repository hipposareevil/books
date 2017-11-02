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
      <div v-if="currentBookIsOnAShelf">
        <!-- book already on shelf -->
        <button class="button is-info is-small is-static">
          <span>Add To My Books</span>
        </button>
      </div>
      <div v-else>
        <!-- book not on a shelf -->
        <button class="button is-info is-small"
                v-bind:class="{'is-success' : userBookWasAdded.flag, 'is-danger' : userBookWasAddedError.flag}" 
                @click="addBookToList(currentBook)">
          <span>Add To My Books</span>
        </button>
      </div>
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
        userBookWasAdded: {
          flag: false
        },
        // Flag showing error for userbook being added
        userBookWasAddedError: {
          flag: false
        },
        // Flag signalling if current book (row) is in the user's shelf
        currentBookIsOnAShelf: true
      }
    },
    /**
     * See if there is a matching user_book for this book
     */
    mounted: function () {
      this.getMatchingUserBook()
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
       * Find a matching user book, denoting if this book
       * can be added or not.
       */
      getMatchingUserBook () {
        let self = this

        // Get user id from Auth
        let userId = Auth.user.id
        let ourHeaders = {
          'Authorization': Auth.getAuthHeader()
        }
        let ourParams = {
          'book_id': this.currentBook.id
        }

        // Try to create a new user_book
        let url = '/user_book/' + userId
        this.$axios.get(url, {
          headers: ourHeaders,
          params: ourParams
        })
          .then((response) => {
            let length = response.data.total
            if (length === 1) {
              self.currentBookIsOnAShelf = true
            } else {
              self.currentBookIsOnAShelf = false
            }
          })
          .catch(function () {
            // Do nothing if we can't get the user_book
            console.log('Unable to find user_book for book: ' + self.currentBook.title)
          })
      },
      /**
       * Add book to a shelf. Notify the ViewBook component
       */
      addBookToList (book) {
        // Get user id from Auth
        let userId = Auth.user.id

        let self = this
        let ourHeaders = {
          'Authorization': Auth.getAuthHeader()
        }

        // Data to update in database
        let data = {
          bookId: book.id
        }

        // Try to create a new user_book
        let url = '/user_book/' + userId
        this.$axios.post(url, data, { headers: ourHeaders })
          .then((response) => {
            // Flag that the book was added
            self.userBookWasAdded.flag = true

            setTimeout(function () {
              self.userBookWasAdded.flag = false
            }, 1200)
          })
          .catch(function (error) {
            if (error.response) {
              let statusCode = error.response.status
              if (statusCode === 409) {
                // Flag that the book was NOT added

                self.userBookWasAddedError.flag = true
                setTimeout(function () {
                  self.userBookWasAddedError.flag = false
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


    
