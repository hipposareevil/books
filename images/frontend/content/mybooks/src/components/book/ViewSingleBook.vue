<template>
  <div>

    <!-- data columns -->
    <div class="columns">
      <!-- left column -->
      <div class="column is-2">
        <img class="boxshadow"
             v-if="bookData.imageLarge"
             :src="bookData.imageLarge">
      </div>

      <!--  info -->
      <div class="column is-4">

        <div class="card">
          <div class="card-content">
            <!-- title, by -->
            <p class="title">
              {{ bookData.title }}
            </p>
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
               v-if="bookData.description">
            <p class="title is-6">
              Description:
            </p>
            <p class="subtitle">
              {{ bookData.description }}
            </p>
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
  import auth from '../../auth'

  export default {
    // Data for this component
    data () {
      return {
        // author data from ajax
        bookData: {},
        // Set by the route prams
        bookId: this.$route.params.id
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
        const authString = auth.getAuthHeader()
        let self = this
        this.bookData = {}

        let url = '/book/' + this.bookId
        console.log('getbook. url: ' + url)

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
      }
    },
    /**
     * When mounted, get list of users from database
     */
    mounted: function () {
      this.getBook()
    }
  }
</script>

<style>
a:hover {
    color: black;
    border-bottom: gray solid 1px;
}

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

.boxshadow {
    box-shadow: 2px 2px 2px 1px rgba(0, 0, 0, .1);
}
</style>
