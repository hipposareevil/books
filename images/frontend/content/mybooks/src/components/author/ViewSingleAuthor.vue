<template>
  <div>

    <!-- Image GetValue Modal -->
    <modal @okclicked="saveImageCalled" 
           @cancelclicked="cancelImageCalled" 
           v-bind:active="showImageChangeModal"
           v-bind:message="createImageMessage()"></modal>

    <!-- Author Name GetValue Modal -->
    <modal @okclicked="saveAuthorCalled" 
           @cancelclicked="cancelAuthorCalled" 
           v-bind:active="showAuthorChangeModal"
           v-bind:message="createNameMessage()"></modal>

    <!-- header -->
    <p class="subtitle is-4"
       style="border-bottom: solid lightgray 1px;">

      <!-- Author Name -->
      <span class="has-text-black">
        Author:
      </span>
      <span class="isclickable"
            title="Double click to update."
            @dblclick="changeName"
            v-bind:class="{ 'has-text-success' : authorNameWasChangedFlag.flag }">
        {{ authorData.name }}
      </span>
    </p>

    <!-- data columns -->
    <div class="columns">
      <div class="column is-one-quarter">

        <!-- Image -->

        <figure class="image isclickable"
                title="Double click to update."
                v-bind:class="{ imagechanged : authorImageWasChangedFlag.flag }"
                @dblclick="changeImage">
          <img v-if="authorData.imageLarge"
               class="boxshadow"
               v-bind:src="authorData.imageLarge">
        </figure>

      </div>

      <!-- books and stats -->
      <div class="column">
        <p class="subtitle is-4"
           style="width: 30%; ">
          Books:
        </p>

        <!-- Books list -->
        <ul v-for="current in bookData">

          <!-- single book -->
          <li style="margin-top: 1em;">
            <a @click="gotoBook(current)">
              <div class="tooltip">
                <span class="tooltiptext" v-html="makeHover(current)"></span>
                {{ current.title }}
              </div>
            </a>
          </li>
        </ul>
        <!-- end books list -->

      </div>
    </div>


  </div> <!-- main div -->
</template>

<script>
  import Auth from '../../auth'
  import Modal from '../GetSingleValueModal.vue'
  import _ from 'lodash'

  export default {
    // Components
    components: { Modal },
    // Data for this component
    data () {
      return {
        // author data from ajax
        authorData: {},
        // Set by the route prams
        authorId: this.$route.params.id,
        // book list for this author
        bookData: {},
        // show the modal for updating image URL
        showImageChangeModal: false,
        // show the modal for updating author name
        showAuthorChangeModal: false,
        // Flag denoting the author image was changed
        authorImageWasChangedFlag: {
          flag: false
        },
        // Flag denoting the author name was changed
        authorNameWasChangedFlag: {
          flag: false
        }
      }
    },
    /**
     * Methods
     */
    methods: {
      /**
       * Get Single author
       */
      getAuthor () {
        const authString = Auth.getAuthHeader()
        let self = this
        this.authorData = {}
        this.$axios.get('/author/' + this.authorId, { headers: { Authorization: authString } })
          .then((response) => {
            self.authorData = response.data
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
       * Get list of books for the author
       *
       */
      getBooks () {
        let self = this
        self.authorData = {}

        let url = '/book?author_id=' + self.authorId

        // get authorization string
        const authString = Auth.getAuthHeader()

        // Get 1000 books
        let params = {
          offset: 0,
          limit: 1000
        }

        console.log('Sending header: ' + authString)
        this.$axios.get(url, {
          headers: { Authorization: authString },
          params: params })
          .then((response) => {
            self.bookData = response.data.data
            // Sort
            self.bookData = _.sortBy(self.bookData, ['title'])
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
       * Go to the currently hovered book
       */
      gotoBook (currentBook) {
        this.$router.push('/books/' + currentBook.id)
      },
      /**
       * Make the hover for a given book
       */
      makeHover (current) {
        let listvalue = ''
        if (current.imageMedium) {
          listvalue += '<img src="' + current.imageMedium + '"><br>'
        }
        return listvalue
      },
      /**
       * Create a message when changing the author's image
       */
      createImageMessage () {
        return 'Set new image URL for ' + this.authorData.name + '.'
      },
      /**
       * Create a message when changing the author's name
       */
      createNameMessage () {
        return 'Set ' + this.authorData.name + '\'s new name.'
      },
      /**
       * Change name of author
       */
      changeName () {
        this.showAuthorChangeModal = true
      },
      /**
       * Change the URL image for the author
       */
      changeImage () {
        this.showImageChangeModal = true
      },
      /**
       * Save was called via the popup modal.
       * Try to save the author to the DB
       */
      saveImageCalled (value) {
        this.showImageChangeModal = false
        if (value) {
          // Set the image for author
          let data = {
            imageSmall: value,
            imageMedium: value,
            imageLarge: value
          }
          this.authorData.imageLarge = value
          this.makePutCall(data, this.authorImageWasChangedFlag)
        }
      },
      /**
       * Cancel was called via the popup modal.
       */
      cancelImageCalled (value) {
        this.showImageChangeModal = false
      },
      /**
       * Save was called via the Name popup modal.
       * Try to save the author name to the DB
       */
      saveAuthorCalled (value) {
        this.showAuthorChangeModal = false
        if (value) {
          // Set the name for author
          let data = {
            name: value
          }
          this.authorData.name = value
          this.makePutCall(data, this.authorNameWasChangedFlag)
        }
      },
      /**
       * Cancel for author name was called via the popup modal.
       */
      cancelAuthorCalled (value) {
        this.showAuthorChangeModal = false
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
        let url = '/author/' + self.authorData.id
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
    },
    /**
     * When mounted, get list of users from database
     */
    mounted: function () {
      this.getAuthor()
      this.getBooks()
    },
    /**
     * Prevent double clicking from highlighting the next table item
     */
    created: function () {
      window.addEventListener('mousedown', function (event) {
        if (event.detail > 1) {
          event.preventDefault()
        }
      }, false)
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

.imagechanged {
    border: solid rgb(49,207,178) 2px;
    margin: 2px;
    padding: 3px;
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
    box-shadow: 1px 1px 1px 1px rgba(0, 0, 0, .1);
}


</style>
