<template>
  <div>

    <p class="subtitle is-4"
       style="border-bottom: solid lightgray 1px;">
      Users
    </p>



    <!-- add new user -->
    <nav class="level">
      <!-- Left side -->
        <div class="level-left">
          <div class="level-item">
            <p class="subtitle is-5">
              Add new user: 
            </p>
          </div>


          <div class="level-item">
            <input class="input"
                   type="text"
                   placeholder="Username"
                   v-model="newUser.name">
          </div>

          <div class="level-item">
            <input class="input"
                   type="password"
                   placeholder="Password"
                   @keyup.enter="createNewUser"
                   v-model="newUser.password">
          </div>

          <div class="level-item">
            <input class="input"
                   type="text"
                   placeholder="Group"
                   v-model="newUser.group">
          </div>

          <div class="level-item">
            <p class="control">
              <button @click="createNewUser" class="button is-info">
                Create
              </button>
            </p>
          </div>
        </div>
    </nav>

    <br>

    <!-- users table -->
    <table cellpadding="10"
           class="table main-width is-narrow is-bordered">
      <thead>
        <tr>
          <th style="width: 30%;" title="Username">Username</th>
          <th style="width: 10%;" title="User ID">User ID</th>
          <th style="width: 30%;" title="Password">Password</th>
          <th style="width: 30%;" title="Group">Group</th>
          <th style="width: 10%;" title="Actions">Actions</th>
        </tr>
      </thead>
      
      <tbody>
        <tr v-for="current in UsersJson">
          <!-- clickable creates a td -->
          <clickable :userObject="current">
          </clickable>

          <td>{{current.id}}</td>

          <clickablepassword :value="current.name">
          </clickablepassword>

          <td>{{current.userGroup}}</td>
          
          <td>
            <deleteuserbutton :userObject="current"
                              @userDeleted="userWasDeleted(current)">
            </deleteuserbutton>
          </td>

        </tr>
      </tbody>
    </table>

    <!-- longer error message to user -->
    <article v-if="errorMessage"
             class="main-width message is-danger">
      <div class="message-body">
        {{ errorMessage }}
      </div>
    </article>

    <!-- Message to user -->
    <div class="animated fadeOutDown is-size-2"
         v-if="messageToUser">
      {{ messageToUser }}
    </div>

  </div>
</template>


<script>
  import auth from '../auth'
  import Clickable from './Clickable.vue'
  import deleteuserbutton from './buttons/DeleteUserButton.vue'
  import Clickablepassword from './Clickablepassword.vue'

  /**
   * Class to hold user data and perform some basic tasks
   *
   */
  class UserData {
    constructor () {
      this.name = ''
      this.password = ''
      this.group = ''
    }

    /**
     * Validate username & password
     */
    validate () {
      if (this.name && this.password) {
        return null
      } else {
        let message = 'Must supply '
        if (!this.name) {
          message += 'username'
        }
        if (!this.password) {
          if (!this.name) {
            message += ' and '
          }
          message += 'password'
        }
        message += '.'
        return message
      }
    }

    clear () {
      this.name = ''
      this.password = ''
      this.group = ''
    }
  }
  
 /**
  * Default data
  */
  export default {
  // Components we depend on
    components: { Clickable, Clickablepassword, deleteuserbutton },
    // Data for this component
    data () {
      return {
        // data from ajax
        UsersJson: {},
        // animated message content
        messageToUser: '',
        // bean to hold new user information
        newUser: new UserData(),
        // Error message
        errorMessage: ''
      }
    },
    /**
     * Called before this component would be loaded.   This halts the rendering if the current
     * user is not authenticated as being in the 'admin' group.
     */
    beforeRouteEnter (to, from, next) {
      if (auth.isAuthenticated('admin')) {
        next()
      } else {
        next(false)
      }
    },
    /**
     * When mounted, get list of users from database
     */
    mounted: function () {
      this.getUsers()
    },
    /**
     * Methods
     */
    methods: {
      /**
       * Get list of users
       */
      getUsers () {
        const authString = auth.getAuthHeader()
        let self = this
        this.UsersJson = {}
        this.$axios.get('/user', { headers: { Authorization: authString } })
          .then((response) => {
            self.UsersJson = response.data.data
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
       * A user was deleted. Send message to screen and then refresh our list of users.
       */
      userWasDeleted (whichUser) {
        this.printMessage('User ' + whichUser.name + ' was deleted')
        this.getUsers()
      },
      /**
       * Create a new user from the input fields on top
       *
       */
      createNewUser () {
        let validateMessage = this.newUser.validate()
        if (validateMessage) {
          this.printError(validateMessage)
          return
        }
        let self = this
        let ourheaders = {
          'Authorization': auth.getAuthHeader()
        }

        // Data to update in database
        let data = {
          name: self.newUser.name,
          userGroup: self.newUser.group,
          password: self.newUser.password
        }

        // Try to create a new user
        this.$axios.post('/user/', data, { headers: ourheaders })
          .then((response) => {
            self.printMessage('Created new user ' + self.newUser.name)
            self.newUser.clear()
            self.getUsers()
          })
        // Process errors
          .catch(function (error) {
            let temp = ''
            if (error.response) {
              let statusCode = error.response.status
              if (statusCode === 409) {
                temp = 'User "' + self.newUser.name + '" already exists'
              }
            } else {
              if (error.response.status === 401) {
                Event.$emit('got401')
              } else {
                console.log(error)
              }
            }
            // Clear out user data
            self.newUser.clear()
            // Print error
            self.printError(temp)
          })
      },
      /**
       * Print an Error to the user
       */
      printError (printThis) {
        let self = this
        this.errorMessage = printThis
        // Have the modal with errorMessage go away in bit
        setTimeout(function () {
          self.errorMessage = ''
        }, 2500)
      },
      /**
       * Print a message to the user
       */
      printMessage (printThis) {
        let self = this
        this.messageToUser = printThis
        // Have the modal with userMessage go away in bit
        setTimeout(function () {
          self.messageToUser = ''
        }, 2000)
      }
    }
  }
</script>

<style>
.fade-enter-active, .fade-leave-active {
  transition: opacity 5s
}

.main-width {
  width: 65%
}
</style>
