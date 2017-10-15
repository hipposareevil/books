<template>
  <div>
    <!-- Disallow deleting of user with id 1 and the currently logged in user -->
    <div v-if="isUserDeletable()">
      <!-- show the actual delete button -->
      <button v-if="showRealDeleteButton"
              v-on:blur="warning_averted()"
              @click="deleteUser()"
              class="button is-small is-danger">
        Delete!
      </button>
      <!-- show the first delete button, which toggles into the previous button -->      
      <button v-else
              @click="enableRealDeleteButton()"
              v-on:blur="warning_averted()"
              class="button is-warning is-small">
        Delete
      </button>
    </div>
    <!-- This user has id of 1, so make a disabled button -->
    <div v-else>
      <div class="tooltip">
        <span class="tooltiptext">Not Deletable</span>
        <button class="button is-danger is-small" disabled>
          Delete
        </button>
      </div>
    </div>
  </div>
</template>

<script>
  import auth from '../../auth'

  export default {
    // Accepts the full User object instead of just name as
    // we need the user id
    props: [ 'userObject' ],
    // Data for this component
    data () {
      return { showRealDeleteButton: false, user: this.userObject }
    },
    methods: {
      /**
       * Return true if the user id != 1 or != the logged in user
       */
      isUserDeletable () {
        let userid = Number(this.user.id)
        let authid = Number(auth.user.id)
        if (userid === authid || userid === 1) {
          return false
        } else {
          return true
        }
      },
      /**
       * The user clicked away form the first 'delete' button
       *
       */
      warning_averted () {
        this.showRealDeleteButton = false
      },
      /**
       * Delete user from database
       */
      deleteUser () {
        let self = this

        // get authorization string
        const authString = auth.getAuthHeader()

        // params for axios request
        let url = '/user/' + this.user.id
        // auth headers
        let ourheaders = {
          'Authorization': authString
        }
        // make request
        this.$axios.delete(url, { headers: ourheaders })
          .then((response) => {
            console.log('deleted user: ' + this.user.name + ' ' + this.user.id)
            // Let parent know they should refresh
            self.$emit('userDeleted')
          })
          .catch(function (error) {
            console.log(error)
          })
      },
      /**
       * Turn on the real delete button
       */
      enableRealDeleteButton () {
        this.showRealDeleteButton = true
      }
    }
  }
</script>


<style>
  span {
    cursor: pointer
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
    width: 140px;
    background-color: rgb(57, 134, 175);
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
