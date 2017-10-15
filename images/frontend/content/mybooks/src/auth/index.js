/**
 * Authentication related variables and functions.
 *
 * Used to obtain a Bearer auth token from webservice.
 *
 * This stores the following keys in localStorage:
 * - 'id_token'   -> The Bearer token
 * - 'user_id'    -> ID of user in the database
 * - 'user_name'  -> Name of user
 * - 'user_grup'  -> Group of user, e.g. admin
 */

// URL of API
const HOSTNAME = location.hostname
const API_URL = 'http://' + HOSTNAME + ':8080/'
// URL of token generation
const LOGIN_URL = API_URL + 'authorize/token'
// URL of validate token
const VALIDATE_URL = API_URL + 'authorize/validate'

export default {

  /**
   * User's ID, group, name, and authenticated flag
   *
   */
  user: {
    authenticated: false,
    id: '',
    name: '',
    group: ''
  },
  /**
   * Check if this user is authenticated.
   * Optionally takes 'groupToTest' to validate the group of the user.
   */
  isAuthenticated (groupToTest) {
    if (groupToTest) {
      // Check the incoming group against the users' group
      if (groupToTest === this.user.group) {
        return true
      } else {
        // Doesn't match group, return false
        return false
      }
    } else {
      // no group to check, reply with authenticated flag
      return this.user.authenticated
    }
  },
  /**
   * Validate the locally stored tokens against the server.
   * This should be used on startup of the application as the local creds
   * could be stored, but the server version could be revoked
   */
  validateTokens (context) {
    const authString = this.getAuthHeader()
    let self = this
    context.$axios.get(VALIDATE_URL, { headers: { Authorization: authString } })
      .then((response) => {
        console.log('User validated')
      })
      .catch(function () {
        console.log('Auth.validatetokens: Unable to validate user authtoken. Logging our selves out internally.')
        self.logout(context)
      })
  },
  /**
   * Log into the application.
   *
   * params:
   * context: Calling object, used to obtain the $axios and $router variables.
   * creds: Credentials object, must container 'name' and 'password'
   * redirect: Location to redirect to after login. Uses the $router.
   */
  login (context, creds, redirect) {
    console.log('log in! ')
    context.$axios.post(LOGIN_URL, {
      name: creds.name,
      password: creds.password
    })
    .then((response) => {
      // Set local storage for later
      localStorage.setItem('id_token', response.data.token)
      localStorage.setItem('user_id', response.data.userId)
      localStorage.setItem('user_name', creds.name)
      localStorage.setItem('user_group', response.data.groupName)

      // Save user information in our object
      this.user.authenticated = true
      this.user.id = response.data.userId
      this.user.name = creds.name
      this.user.group = response.data.groupName

      console.log('Auth.login: finished processing login information')

      // Redirect to some page/tab
      if (redirect) {
        console.log('Auth.login: route to location: ' + redirect)
        context.$router.push(redirect)
      }
    })
    // Process errors
    .catch(function (error) {
      if (error.response) {
        // got a response from server

        console.log('login: ' + error.response.status)

        if (error.response.status === 500) {
          // Send message to Event
          context.emitMessage('got500')
        }
      } else if (error.request) {
        console.log('no response, sending message')
        // no response from server
        context.emitMessage('got500', 'Unable to contact the server (500).')
      } else {
        console.log('Auth.login: unknown error - ' + error)
      }
    })
  },
  /**
   * Zero out the local storage and 'user'
   */
  zeroOut () {
    // zero out local storage
    localStorage.removeItem('id_token')
    localStorage.removeItem('user_id')
    localStorage.removeItem('user_name')
    localStorage.removeItem('user_group')

    // zero out the user
    this.user.authenticated = false
    this.user.group = ''
    this.user.id = ''
    this.user.name = ''
  },
  /**
   * Logout
   *
   * Remove the local storage and set authenticated to false
   */
  logout (context) {
    this.zeroOut()

    // Go back to main page
    context.$router.push('/')
  },
  /**
   * Check local storage for the user information.
   * If it exists, the token and user id will be set.
   */
  checkAuth () {
    var jwt = localStorage.getItem('id_token')
    if (jwt) {
      this.user.authenticated = true
      this.user.id = localStorage.getItem('user_id')
      this.user.name = localStorage.getItem('user_name')
      this.user.group = localStorage.getItem('user_group')
    } else {
      this.zeroOut()
    }
  },
  /**
   * Return the header used in authentication
   */
  getAuthHeader () {
    return localStorage.getItem('id_token')
  }
}
