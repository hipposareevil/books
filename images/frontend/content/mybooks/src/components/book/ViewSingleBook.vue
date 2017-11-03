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

    <!-- data columns -->
    <div class="columns">
      <!-- left column -->
      <div class="column is-2">

        <!-- Image  -->
        <figure class="image is-square isclickable"
                style="cursor: pointer;"
                title="Double click to update."
                @dblclick="changeImage">
          <img v-if="bookData.imageMedium"
               class="boxshadow"
               :src="bookData.imageMedium">
        </figure>

      </div>

      <!--  info -->
      <div class="column is-4">

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
          </div>

          <!-- description -->
          <div class="card-content"
               @dblclick="changeDescription"
               title="Double click to update.">
            <p class="title is-6">
              Description:
            </p>
            <!-- description or ... -->
            <div class="isclickable subtitle">
              <div v-if="bookData.description"
                   v-bind:class="{ 'has-text-success' : bookDescriptionWasChangedFlag.flag }">
                {{ bookData.description }}
              </div>
              <div v-else>
                .....
              </div>
            </div>
          </div>


          <!-- Footer -->
          <footer class="card-footer">
            <p class="card-footer-item">
              <span v-if="bookData.openlibraryWorkUrl">
                View on <a :href="bookData.openlibraryWorkUrl">openlibrary.org</a>
              </span>
            </p>
            <p class="card-footer-item">
              <span>&nbsp;
              </span>
            </p>
          </footer>

        </div> <!-- end card -->

      </div> <!-- end info column -->

    </div> <!-- end columns -->

  </div> <!-- main div -->
</template>

<script>
  import Auth from '../../auth'
  import Modal from '../GetSingleValueModal.vue'

  export default {
    // Components
    components: { Modal },
    // Data for this component
    data () {
      return {
        // author data from ajax
        bookData: {},
        // Set by the route prams
        bookId: this.$route.params.id,
        // show the modal for updating image URL
        showImageChangeModal: false,
        // show the modal for updating book title
        showTitleChangeModal: false,
        // show the modal for updating book description
        showDescriptionChangeModal: false,
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
        }
      }
    },
    /**
     * When mounted, get extra info
     */
    mounted: function () {
      this.getBook()
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
        console.log('Get book from ' + url)
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
      /**
       * Set a new description
       */
      changeDescription () {
        this.showDescriptionChangeModal = true
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
          // Set the description for the author
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

.isclickable:hover {
    background-color: rgb(245,245,245)
}

a:hover {
    background-color: rgb(245,245,245)
}

/* Tooltip container */
.tooltip {
    position: relative;
    display: inline-block;
}

/* Tooltip text */
.tooltip .tooltiptext {
    visibility: hidden;
    width: 250px;
    background-color: lightgray;
    color: rgb(238,240,240);
    text-align: center;
    padding: 5px 0;
    border-radius: 6px;
 
    /* Position the tooltip text - see examples below! */
    position: absolute;
    z-index: 1;
}

.tooltiptext {
  visibility: hidden;
}

/* Show the tooltip text when you mouse over the tooltip container */
.tooltip:hover .tooltiptext {
    top: -25px;
    left: 120%;
    visibility: visible;
}

.boxshadow {
    box-shadow: 2px 2px 2px 1px rgba(0, 0, 0, .1);
}
</style>
