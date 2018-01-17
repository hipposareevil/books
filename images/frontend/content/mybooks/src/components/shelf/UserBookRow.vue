<!-- This represent a single userbook as a row in a table -->

<template>
  <tr>

    <!-- cover -->
    <td style=" outline-style: none; background-transparent;">
      <a @click="gotoBook">
      <figure class="image is-128x128">
        <img v-if="userBook.imageMedium"
             class="boxshadow"
             style="width: auto; max-height: 128px;"
             :src="userBook.imageMedium">
      </figure>
      </a>
    </td>

    <!-- title -->
    <th>
      <a @click="gotoBook">
        {{ userBook.title }} 
      </a>
    </th>

    <!-- author  -->
    <td>
      <a @click="gotoAuthor">
      {{ userBook.authorName }}
      </a>
    </td>

    <!-- year -->
    <td>
      {{ userBook.firstPublishedYear }}
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
     * allTags: list of all tags (id,name,data)
     */
    props: [ 'userBook', 'allTags' ],
    /**
     * Data for this user book row
     */
    data () {
      return {
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
     * Methods
     */
    methods: {
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
        this.$router.push('/books/' + this.userBook.bookId)
      },
      /**
       * send the router to a single author
       *
       */
      gotoAuthor () {
        this.$router.push('/authors/' + this.userBook.authorId)
      }
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
.clickable {
  cursor: pointer
}

.boxshadow {
  box-shadow: 1px 1px 1px 1px rgba(0, 0, 0, .1);
}

</style>
