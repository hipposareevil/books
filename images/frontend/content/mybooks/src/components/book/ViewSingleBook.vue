<template>
  <div>

    <!-- Image GetValue Modal -->
    <modal @okclicked="saveImageCalled" 
           @cancelclicked="cancelImageCalled" 
           v-bind:active="showImageChangeModal"
           v-bind:message="createImageMessage()"></modal>

    <!-- Book Title GetValue Modal -->
    <modal @okclicked="saveTitleCalled" 
           @cancelclicked="cancelTitleCalled" 
           :initialValue="bookData.title"
           v-bind:active="showTitleChangeModal"
           v-bind:message="createTitleMessage()"></modal>

    <!-- Book Description GetValue Modal -->
    <modal @okclicked="saveDescriptionCalled"
           @cancelclicked="cancelDescriptionCalled"
           inputType="textarea"
           :initialValue="bookData.description"
           v-bind:active="showDescriptionChangeModal"
           v-bind:message="createDescriptionMessage()"></modal>

    <!-- UserBook Review GetValue Modal -->
    <modal @okclicked="saveReviewCalled"
           @cancelclicked="cancelReviewCalled"
           inputType="textarea"
           :initialValue="userBookData.review"
           v-bind:active="showReviewChangeModal"
           v-bind:message="createReviewMessage()"></modal>


    <!-- modal to update tags -->
    <UpdateTagsModal v-if="showTagModal"
                     :active="showTagModal"
                     :allTags="allTags"
                     :userBook="userBookData"
                     @savedCalled="tagsUpdatedByModal"
                     @cancelCalled="tagsCancelledByModal"
                     >
    </UpdateTagsModal>


    <!-- data columns -->
    <div class="columns">
      <!-- left column -->
      <div class="column is-3">

        <!-- Image  -->
        <figure class="image "
                style="cursor: pointer; min-height: 10em; margin-bottom: 2em;"
                title="Double click to update."
                @dblclick="changeImage">
          <img v-if="bookData.imageMedium"
               class="boxshadow"
               :src="bookData.imageMedium">
        </figure>

        <!-- user book information -->


        <!-- not in userbooks -->
        <div v-if="isEmpty(userBookData)">

          <div class="isclickable"
               v-on:dblclick="addBookToList">
            <a class=" hoverme">
              (Add to mybooks)
              </a>
          </div>
        </div>

        
        <!-- is in userbooks -->
        <div v-else>
          <div class="columns">

            <!-- rating -->
            <div class="column is-one-quarter">
            Rating:<br>
            <span style="padding-top: 1em;"
                  class="icon is-medium isclickable"
                  v-on:dblclick="ratingClicked">
              <!-- thumbs up or down -->
              <i v-if="userBookData.rating === true"
                 class="fas fa-thumbs-up fa-2x has-text-link"></i>
              <i v-if="userBookData.rating === false"
                 class="fas fa-thumbs-down fa-2x has-text-danger"></i>
            </span>
            </div>
            <!-- end rating -->

            <!-- tags -->
            <div class="column">
              Tags:<br>
              <div style="padding-top: 1em;"
                   v-on:dblclick="tagClicked"
                   class="field is-grouped is-grouped-multiline isclickable">

                <div v-if="isEmpty(userBookData.tags)"
                     class="hoverable">
                  <a>
                    (add tags)
                  </a>
                </div>

                <div v-for="current in userBookData.tags"
                     class="control">

                  <!-- single tag -->
                  <div class="tags has-addons">
                    <span class="tag is-light">
                      {{ current }}
                    </span>
                  </div>
                  <!-- end tag -->

                </div> <!-- end v-for -->

              </div> <!-- end field -->

            </div>
            <!-- end tags -->

          </div>
          <!-- end columns -->

        </div>
        <!-- end user book -->

      </div>
      <!-- end left column/image -->

      <!--  info -->
      <div class="column is-6">

        <div class="card">
          <div class="card-content">
            <!-- Title  -->
            <p class="title isclickable has-text-gray"
               title="Double click to update."
               @dblclick="changeTitle"
               v-bind:class="{ 'has-text-success' : bookTitleWasChangedFlag.flag }">
              {{ bookData.title }}
            </p>

            <!-- Author -->
            <p class="subtitle">
              by <a @click="gotoAuthor()">{{ bookData.authorName }}</a>
              <br>
              <span class="is-size-7">
                Published: <time>{{ bookData.firstPublishedYear }}</time>
              </span>
            </p>


            <!-- description or ... -->
            <div class="isclickable subtitle"
                 @dblclick="changeDescription"
                 title="Double click to update.">
              <div v-if="bookData.description"
                   v-bind:class="{ 'has-text-success' : bookDescriptionWasChangedFlag.flag }">
                <!-- expand section -->
                <div v-bind:class="{ 'expanded': descriptionExpanded, 'notExpanded': !descriptionExpanded }">
                  <span v-html="prettyDescription"></span>
                </div>
                <span v-if="descriptionExpanded">
                  <a class="expanderText"
                     @click="toggleDescription">(less)</a>
                  </span>
                  <span v-else>
                    <a class="expanderText"
                       @click="toggleDescription">...more</a>
                  </span>
                <!-- end expand section -->
              </div>
              <div v-else>
                ...
              </div>

            </div>
          </div>

          <!-- Footer -->
          <footer class="card-footer">
            <p class="card-footer-item">
              <span v-if="bookData.openLibraryWorkUrl">
                <a :href="bookData.openLibraryWorkUrl">openlibrary.org</a>
              </span>
            </p>
            <p class="card-footer-item">
              <span v-if="bookData.goodreadsUrl">
                <a :href="bookData.goodreadsUrl">goodreads.com</a>
              </span>
            </p>
          </footer>

        </div> <!-- end card -->

      </div> <!-- end info column -->


    </div> <!-- end columns -->


    <!-- more userbook info -->
    <div v-if="! isEmpty(userBookData)">
      <div class="columns">
        <div class="column is-9">
          <hr>
          <h1 class="title is-4">My Review:</h1>

          <div class="isclickable subtitle"
               style="padding-top: 1em;"
               @dblclick="changeReview"
               title="Double click to update.">
            <div v-if="userBookData.review">
              <span v-html="prettyReview"></span>
            </div>
            <div v-else
                 style="font-weight: lighter">
              (none)
            </div>
          </div>
        </div>
        
      </div>
    </div>


    </div>
    <!-- end userbook info -->

  </div> <!-- main div -->
</template>

<script>
  import Auth from '../../auth'
  import Modal from '../GetSingleValueModal.vue'
  import UpdateTagsModal from '../shelf/UpdateTagsModal.vue'
  import _ from 'lodash'

  export default {
    // Components
    components: { Modal, UpdateTagsModal },
    // Data for this component
    data () {
      return {
        // book data from ajax
        bookData: {},
        // userbook data
        userBookData: {},
        // Set by the route prams
        bookId: this.$route.params.id,
        // show the modal for updating image URL
        showImageChangeModal: false,
        // show the modal for updating book title
        showTitleChangeModal: false,
        // show the modal for updating book description
        showDescriptionChangeModal: false,
        // show the modal for updating user book review
        showReviewChangeModal: false,
        // Flag denoting the book image was changed
        // Used for alerting user after a change went through
        bookImageWasChangedFlag: {
          flag: false
        },
        // Flag denoting the book title was changed
        // Used for alerting user after a change went through
        bookTitleWasChangedFlag: {
          flag: false
        },
        // Flag denoting the book description was changed
        // Used for alerting user after a change went through
        bookDescriptionWasChangedFlag: {
          flag: false
        },
        descriptionExpanded: false,
        // Flag to bring up the tags modal
        showTagModal: false,
        // Tags
        allTags: {}
      }
    },
    /**
     * When mounted, get extra info
     */
    created: function () {
      this.getBook()
      this.getUserBook()
      this.getTags()
    },
    computed: {
      prettyDescription: function () {
        return this.bookData.description.replace(/(?:\r\n|\r|\n)/g, '<br>')
      },
      prettyReview: function () {
        return this.userBookData.review.replace(/(?:\r\n|\r|\n)/g, '<br>')
      }
    },
    /**
     * Methods
     */
    methods: {
      /**
       * Go to the currently hovered book
       */
      gotoAuthor () {
        this.$router.push('/authors/' + this.bookData.authorId)
      },
      /**
       * Get single book
       */
      getBook () {
        const authString = Auth.getAuthHeader()
        let self = this
        this.bookData = {}

        let url = '/book/' + this.bookId
        this.$axios.get(url, { headers: { Authorization: authString } })
          .then((response) => {
            self.bookData = response.data
          })
          .catch(function (error) {
            if (error.response.status === 401) {
              Event.$emit('got401')
            } else {
              console.log(error)
            }
          })
      },
      /**
       * Get single userbook for this matching book, if it exists
       */
      getUserBook () {
        const authString = Auth.getAuthHeader()
        let self = this
        self.userBookData = {}

        let userId = Auth.user.id

        let url = '/user_book/' + userId + '?book_id=' + self.bookId

        self.$axios.get(url, { headers: { Authorization: authString } })
          .then((response) => {
            let numberResults = response.data.limit
            if (numberResults === 1) {
              var result = response.data.data[0]
              self.userBookData = result
              console.log('got user book:', result)
            }
          })
          .catch(function (error) {
            if (error.response) {
              if (error.response.status === 401) {
                Event.$emit('got401')
              } else {
                // If this was a 400 that's ok, just means no user book for this book
                console.log(error)
              }
            } else {
              console.log('error:', error)
            }
          })
      },
      /**
       * Get tags from database
       */
      getTags () {
        const authString = Auth.getAuthHeader()
        let self = this
        self.allTags = {}

        let params = {
          offset: 0,
          limit: 200
        }

        self.$axios.get('/tag', {
          headers: { Authorization: authString },
          params: params })
          .then((response) => {
            self.allTags = response.data.data
            // Sort
            self.allTags = _.sortBy(self.allTags, ['name'])
          })
          .catch(function (error) {
            if (error.response.status === 401) {
              Event.$emit('got401')
            } else {
              console.log(error)
            }
          })
      },
      /**
       * The tag section was double clicked
       */
      tagClicked () {
        console.log('tag clicked')
        this.showTagModal = true
      },
      /**
       * Tags were updated via the modal
       */
      tagsUpdatedByModal (newtags) {
        this.showTagModal = false
        console.log('Tags were updated: ' + newtags)
        // Update the user book. this is just done locally
        this.userBookData.tags = newtags

        // Update the database with a new rating
        let data = {
          tags: this.userBookData.tags
        }
        this.makeUserBookCall(data)
      },
     /**
      * Tags were NOT updated via the modal
      */
      tagsCancelledByModal () {
        this.showTagModal = false
      },
      /**
       * The thumbs up/down was double clicked
       */
      ratingClicked () {
        // Switch rating
        this.userBookData.rating = !this.userBookData.rating

        // Update the database with a new rating
        let data = {
          rating: this.userBookData.rating
        }
        console.log('Changing rating to: ', this.userBookData.rating)
        this.makeUserBookCall(data)
      },
      /**
       * Make a PUT ajax call
       *
       * params:
       * data: data to PUT
       */
      makeUserBookCall (data) {
        let self = this
        let userId = Auth.user.id

        // get authorization string
        const authString = Auth.getAuthHeader()

        // params for axios request
        let url = '/user_book/' + userId + '/' + self.userBookData.userBookId
        // auth headers
        let ourheaders = {
          'Authorization': authString
        }
        // make request
        this.$axios.put(url, data, { headers: ourheaders })
          .catch(function (error) {
            console.log(error)
          })
      },
      /**
       * Add current book to the user's list
       *
       */
      addBookToList () {
        // Get user id from Auth
        let userId = Auth.user.id

        let self = this
        let ourHeaders = {
          'Authorization': Auth.getAuthHeader()
        }

        // Data to update in database
        let data = {
          bookId: self.bookData.id,
          rating: true
        }

        // Try to create a new user_book
        let url = '/user_book/' + userId
        this.$axios.post(url, data, { headers: ourHeaders })
          .then((response) => {
            console.log('was added to user_books')
            self.getUserBook()
            self.getTags()
          })
          .catch(function (error) {
            if (error.response) {
              let statusCode = error.response.status
              if (statusCode === 409) {
                console.log('was not added to user_books')
              }
            }
            console.log(error)
          })
      },
      /**
       * Set a new image url
       */
      changeImage () {
        this.showImageChangeModal = true
      },
      /**
       * Set a new title
       */
      changeTitle () {
        this.showTitleChangeModal = true
      },
      toggleDescription () {
        this.descriptionExpanded = !this.descriptionExpanded
      },
      /**
       * Set a new description
       */
      changeDescription () {
        this.showDescriptionChangeModal = true
      },
      /**
       * Update review for book
       */
      changeReview () {
        this.showReviewChangeModal = true
      },
      /**
       * Create a message when changing the books's image
       */
      createImageMessage () {
        return 'Set new image URL for ' + this.bookData.title + '.'
      },
      /**
       * Create a message when changing the books title
       */
      createTitleMessage () {
        return 'Set ' + this.bookData.title + '\'s new title.'
      },
      /**
       * Create a message when changing the books description
       */
      createDescriptionMessage () {
        return 'Set ' + this.bookData.title + '\'s new description.'
      },
      /**
       * Create a message when changing the user book review
       */
      createReviewMessage () {
        return 'Update your review for ' + this.bookData.title
      },
      /**
       * Save was called via the popup modal.
       * Try to save the author to the DB
       */
      saveImageCalled (value) {
        this.showImageChangeModal = false
        if (value) {
          // Set the image for book
          let data = {
            imageSmall: value,
            imageMedium: value,
            imageLarge: value
          }
          this.bookData.imageLarge = value
          this.makePutCall(data, this.bookImageWasChangedFlag)
        }
      },
      /**
       * Cancel for image was called via the popup modal.
       */
      cancelImageCalled (value) {
        this.showImageChangeModal = false
      },
      /**
       * Save was called via the Name popup modal.
       * Try to save the book's title to the DB
       */
      saveTitleCalled (value) {
        this.showTitleChangeModal = false
        if (value) {
          // Set the title for the author
          let data = {
            title: value
          }
          this.bookData.title = value
          this.makePutCall(data, this.bookTitleWasChangedFlag)
        }
      },
      /**
       * Cancel for title was called via the popup modal.
       */
      cancelTitleCalled (value) {
        this.showTitleChangeModal = false
      },
      /**
       * Save was called via the description modal
       * Try to save the book's description to the DB
       */
      saveDescriptionCalled (value) {
        this.showDescriptionChangeModal = false
        if (value) {
          // Set the description for the book
          let data = {
            description: value
          }
          this.bookData.description = value
          this.makePutCall(data, this.bookDescriptionWasChangedFlag)
        }
      },
      /**
       * Cancel for description was called via the popup modal.
       */
      cancelDescriptionCalled (value) {
        this.showDescriptionChangeModal = false
      },
      /**
       * Save was called via the review modal
       * Try to save the userbook's review to the DB
       */
      saveReviewCalled (value) {
        this.showReviewChangeModal = false
        if (value) {
          // Set the review for the userbook
          let data = {
            review: value
          }
          this.userBookData.review = value
          this.makeUserBookCall(data)
        }
      },
      /**
       * Cancel for review was called via the popup modal.
       */
      cancelReviewCalled (value) {
        this.showReviewChangeModal = false
      },
      isEmpty (thing) {
        return _.isEmpty(thing)
      },
      /**
       * Make a PUT ajax call
       *
       * params:
       * data: data to PUT
       * flagMe: What variable to set to true and then after a timeout set back to false.
       *   This must be an object that contains a 'flag' member.
       */
      makePutCall (data, flagMe) {
        let self = this

        // get authorization string
        const authString = Auth.getAuthHeader()

        // params for axios request
        let url = '/book/' + self.bookData.id
        // auth headers
        let ourheaders = {
          'Authorization': authString
        }
        // make request
        this.$axios.put(url, data, { headers: ourheaders })
          .then((response) => {
            // Mark the Flagged thing as changed, and then set timeout to remove that change.
            if (flagMe) {
              flagMe.flag = true
              setTimeout(function () {
                flagMe.flag = false
              }, 1800)
            }
          })
          .catch(function (error) {
            console.log(error)
          })
      }
    }
  }
</script>

<style>
.isclickable {
    cursor: pointer;
}

.hoverme:hover {
   background-color: rgb(245,245,245)
}

/*
.isclickable:hover {
    background-color: rgb(245,245,245)
}

a:hover {
    background-color: rgb(245,245,245)
}
*/
.expanderText {
    color: green
}
.expanderText:hover {
    text-decoration: underline;
    background-color: white;
    color: green;
}

.boxshadow {
    box-shadow: 2px 2px 2px 1px rgba(0, 0, 0, .1);
}

.notExpanded {
    height: 15em;
    overflow: hidden;
}

.expanded {
}


</style>
