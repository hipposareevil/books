<!-- This represent a single userbook as a row in a table -->

<template>
  <tr v-if="showRow">

    <!-- cover -->
    <td style=" outline-style: none; background-transparent;">
      <a @click="gotoBook">
      <figure class="image is-128x128">
        <img v-if="book.imageMedium"
             class="boxshadow"
             style="width: auto; max-height: 128px;"
             :src="book.imageMedium">
      </figure>
      </a>
    </td>

    <!-- title -->
    <th>
      <a @click="gotoBook">
        {{ book.title }} 
      </a>
    </th>

    <!-- author  -->
    <td>
      <a @click="gotoAuthor">
      {{ book.authorName }}
      </a>
    </td>

    <!-- year -->
    <td>
      {{ book.firstPublishedYear }}
    </td>

    <!-- rating -->
    <td title="double-click to change rating">

      <span class="icon is-medium clickable"
            v-bind:class="{'has-text-success' : ratingWasChanged.flag }" 
            v-on:dblclick="ratingClicked">

        <!-- thumbs up or down -->
        <i v-if="userBook.rating === true"
           class="fa fa-thumbs-up"></i>
        <i v-if="userBook.rating === false"
           class="fa fa-thumbs-down"></i>

      </span>
    </td>
    <!-- end rating -->

    <!-- tags -->
    <td title="double-click to edit tags. "
        class="clickable"
        v-on:dblclick="tagClicked">
      <div class="field is-grouped is-grouped-multiline">

        <div v-for="current in userBook.tags"
             class="control">

          <!-- single tag -->
          <div class="tags has-addons">
            <span
              v-bind:class="{ 'is-success' : tagsWereChanged.flag }" 
              class="tag is-light">
              {{ current }}
            </span>
          </div>
          <!-- end tag -->

        </div> <!-- end v-for -->

      </div> <!-- end field -->

      <!-- modal to update tags -->
      <UpdateTagsModal v-if="showTagModal"
                       :active="showTagModal"
                       :allTags="allTags"
                       :book="book"
                       :userBook="userBook"
                       @savedCalled="tagsUpdatedByModal"
                       @cancelCalled="tagsCancelledByModal"
                       >
      </UpdateTagsModal>

    </td>
    <!-- end tags -->


  </tr>
</template>

<script>
  import Auth from '../../auth'
  import _ from 'lodash'
  import UpdateTagsModal from './UpdateTagsModal.vue'

  /**
   * Default data
   */
  export default {
    // Components
    components: { UpdateTagsModal },
    /**
     * Props for this userbook row:
     *
     * userBook: UserBook to be displayed
     * filter: String to filter titles/authors by
     * tagFilter: Currently selected tag in left column
     * allTags: list of all tags (id,name,data)
     */
    props: [ 'userBook', 'filter', 'tagFilter', 'allTags' ],
    /**
     * Data for this user book row
     */
    data () {
      return {
        // Data for the current book, this is retrieved once
        // we are mounted via this.getFullBook
        book: {},
        // Flag denoting the thumbs up was clicked & changed
        ratingWasChanged: {
          flag: false
        },
        // Flag denoting the tags were updated
        tagsWereChanged: {
          flag: false
        },
        // Flag to bring up the tags modal
        showTagModal: false
      }
    },
    /**
     * When mounted, get the user books
     *
     */
    mounted: function () {
      this.getFullBook()
    },
    /**
     * Methods
     */
    methods: {
      /**
       * Get get book for id
       */
      getFullBook () {
        const authString = Auth.getAuthHeader()
        let self = this
        let url = '/book/' + this.userBook.bookId
        this.$axios.get(url, { headers: { Authorization: authString } })
          .then((response) => {
            self.book = response.data
          })
          .catch(function (error) {
            if (error.response.status === 401) {
              this.$emit('got401')
            } else {
              console.log(error)
            }
          })
      },
      /**
       * The tag section was double clicked
       */
      tagClicked () {
        this.showTagModal = true
      },
     /**
      * Tags were updated via the modal
      */
      tagsUpdatedByModal (newtags) {
        this.showTagModal = false
        console.log('Tags were updated: ' + newtags)
        // Update the user book. this is just done locally
        this.userBook.tags = newtags

        // Update the database with a new rating
        let data = {
          tags: this.userBook.tags
        }
        this.makePutCall(data, this.tagsWereChanged)
      },
     /**
      * Tags were NOT updated via the modal
      */
      tagsCancelledByModal () {
        console.log('Tags were CANCELD')
        this.showTagModal = false
      },
      /**
       * The thumbs up/down was double clicked
       */
      ratingClicked () {
        // Switch rating
        this.userBook.rating = !this.userBook.rating

        // Update the database with a new rating
        let data = {
          rating: this.userBook.rating
        }
        this.makePutCall(data, this.ratingWasChanged)
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
        console.log('making PUT request to /user_book/')

        // params for axios request
        let url = '/user_book/' + self.userBook.userId + '/' + self.userBook.userBookId
        // auth headers
        let ourheaders = {
          'Authorization': authString
        }
        // make request
        this.$axios.put(url, data, { headers: ourheaders })
          .then((response) => {
            // Mark the rating as changed, and then set timeout to remove that change.
            if (flagMe) {
              flagMe.flag = true
              setTimeout(function () {
                flagMe.flag = false
              }, 1200)
            }
          })
          .catch(function (error) {
            console.log(error)
          })
      },
      /**
       * send the router to a single book
       *
       */
      gotoBook () {
        this.$router.push('/books/' + this.book.id)
      },
      /**
       * send the router to a single author
       *
       */
      gotoAuthor () {
        this.$router.push('/authors/' + this.book.authorId)
      }
    },
    /**
     * Computed value for 'showRow'.
     * This returns true if the row should be shown.
     * If the filter or tagFilter strings are non-null, we check
     * those against the title, author, date and the tags
     */
    computed: {
      showRow: function () {
        let firstFilter = true
        let secondFilter = true

        // Check filter
        if (this.filter) {
          // lower case the filter
          let filterString = _.lowerCase(this.filter)

          // Check the filter string against:
          // title, author, and date
          let titleString = _.lowerCase(this.book.title)
          let authorString = _.lowerCase(this.book.authorName)
          let dateString = String(this.book.firstPublishedYear)

          firstFilter = _.includes(authorString, filterString) ||
              _.includes(titleString, filterString) ||
              _.includes(dateString, filterString)
        }

        // Check tag filter
        if (this.tagFilter) {
          secondFilter = _.includes(this.userBook.tags, this.tagFilter)
        }

        // If both filters say show, then show it
        if (firstFilter && secondFilter) {
          return true
        } else {
          return false
        }
      }
    }, // end computed
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
.clickable {
  cursor: pointer
}

.boxshadow {
  box-shadow: 1px 1px 1px 1px rgba(0, 0, 0, .1);
}

</style>
