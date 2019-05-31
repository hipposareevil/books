<template>
  <td>
    <!-- normal text -->
    <span v-if="showSpan"
          @click="mutate">{{ user.name }}</span>
    <!-- input text -->
    <input v-else
           class="input is-small"
           type="text"
           autofocus
           v-model="user.name"
           v-on:blur="mutate"
           @keyup.enter="submit"></input>
  </td>
</template>

<script>
  import auth from '../auth'

  export default {
    // Accepts the full User object instead of just name as
    // we need the user id
    props: [ 'userObject' ],
    // Data for this component
    data () {
      return { showSpan: true, user: this.userObject, originalName: this.userObject.name }
    },
    methods: {
      submit () {
        let self = this

        // get authorization string
        const authString = auth.getAuthHeader()

        // params for axios request
        let url = '/user/' + self.user.id
        // auth headers
        let ourheaders = {
          'Authorization': authString
        }
        // Data to update in database, just the name
        let data = {
          name: self.user.name
        }
        // make request
        this.$axios.put(url, data, { headers: ourheaders })
          .then((response) => {
            // save the new original name
            self.originalName = self.user.name
          })
          .catch(function (error) {
            console.log(error)
          })
      },
      // Mutate the type of element
      // change from span to input
      mutate () {
        // reset the name as save/submit was not called
        this.user.name = this.originalName
        this.showSpan = !this.showSpan
      }
    }
  }
</script>


<style>
  span {
    cursor: pointer
  }
</style>
