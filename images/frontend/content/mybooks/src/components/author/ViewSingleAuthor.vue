<template>
  <div>
    <!-- header -->
    <p class="subtitle is-4"
       style="border-bottom: solid lightgray 1px;">
      Author: {{ authorData.name }}
    </p>


    <!-- data columns -->
    <div class="columns">
      <div class="column is-one-quarter">

        <figure class="image is-square"
                style="cursor: pointer;"
                @dblclick="changeImage">
          <img v-if="authorData.imageLarge"
               :src="authorData.imageLarge">
          <img v-else
               src="../../assets/kokopelli.jpg">
        </figure>

      </div>

      <!-- books and stats -->
      <div class="column">
        <p class="subtitle is-5"
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
  import auth from '../../auth'

  export default {
    // Data for this component
    data () {
      return {
        // author data from ajax
        authorData: {},
        // Set by the route prams
        authorId: this.$route.params.id,
        // book list for this author
        bookData: {}
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
        const authString = auth.getAuthHeader()
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
        console.log('get books for ' + this.authorId)
        const authString = auth.getAuthHeader()
        let self = this
        this.authorData = {}
        this.$axios.get('/book?authorId=' + this.authorId, { headers: { Authorization: authString } })
          .then((response) => {
            self.bookData = response.data.data
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
       *
       */
      makeHover (current) {
        let listvalue = ''
        if (current.imageMedium) {
          listvalue += '<img src="' + current.imageMedium + '"><br>'
        }
        return listvalue
      },
      /**
       *
       */
      changeImage () {
        console.log('change image for author ' + this.authorData.name)
      }
    },
    /**
     * When mounted, get list of users from database
     */
    mounted: function () {
      this.getAuthor()
      this.getBooks()
    }
  }
</script>

<style>
/* Tooltip container */
.tooltip {
    position: relative;
    display: inline-block;
    border-bottom: 1px dotted black; /* If you want dots under the hoverable text */
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


</style>
