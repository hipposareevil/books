<template>
  <td>
    <!-- normal text -->
    <span v-if="showSpan"
          @click="mutate" >••••••••</span>
    <!-- input text -->
    <input v-else
           class="input is-small"
           type="password"
           v-model="formValue"
           @keyup.enter="submit"
           v-on:blur="mutate"></input>
  </td>
</template>

<script>
  import Auth from '../auth'

  export default {
    props: [ 'userObject' ],
    // Data for this component
    data () {
      return { showSpan: true, formValue: this.value }
    },
    methods: {
      /**
       * A new password was created, submit a PUT
       * to update in database.
       */
      submit () {
        let self = this
        console.log('submit! ' + self.formValue)

        // get authorization string
        const authString = Auth.getAuthHeader()

        // params for axios request
        let url = '/user/' + self.userObject.id

        console.log('making PUT request to ' + url)

        // auth headers
        let ourheaders = {
          'Authorization': authString
        }
        // Data to update in database, just the name
        let data = {
          password: self.formValue
        }
        // make request
        this.$axios.put(url, data, { headers: ourheaders })
          .then((response) => {
            // save the new original name
            console.log('updated password!')
          })
          .catch(function (error) {
            console.log(error)
          })
      },
      // Mutate the type of element
      // change from span to input
      mutate () {
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
