<!-- component for listing and choosing tags -->
<template>
  <div v-bind:class="{ 'is-warning' : !checked }"
       class="notification"
       style="padding: 3px; min-height: 9em;">

    <div class="title is-5">Current tags</div>
    <div class="tags">
      <span v-for="current in tagsToAdd"
            class="tag is-info">
        {{current}}
        <button class="delete is-small"
                @click="deleteTag(current)">
        </button>
      </span>
    </div>


    <!-- All Tags in system -->
    <div class="title is-5">All tags</div>
    <div class="tags">
      <span v-for="current in currentTagList"
            title="click to add to tags"
            @click="addTag(current)"
            class="tag clickable">
        {{current}}
      </span>
    </div>
    <!-- end of tag list -->
  </div>

</div>
</template>

<script>
   import Auth from '../auth'
   import _ from 'lodash'

   export default {
     created: function () {
       Event.$on('resetTags', () => this.clearTags())
     },
     destroyed: function () {
       Event.$off('resetTags')
     },
    /**
     * When mounted
     */
     mounted: function () {
       // Get tags
       this.getTags()
     },
    /**
     * checked
     */
     props: [ 'checked', 'resetTags' ],
     /**
      * Data
      */
     data () {
       return {
         // All Tags
         TagJson: {},
         /**
          * tags
          */
         tagsToAdd: [],
         /**
          * all incoming tag names
          */
         allTagsAsList: []
       }
     },
     /**
      * Methods
      */
     methods: {
       /**
        * Get tags from database
        */
       getTags () {
         const authString = Auth.getAuthHeader()
         let self = this
         this.TagJson = {}
         let params = {
           offset: 0,
           limit: 200
         }

         this.$axios.get('/tag/', {
           headers: { Authorization: authString },
           params: params })
           .then((response) => {
             self.TagJson = response.data.data
             // Sort
             self.TagJson = _.sortBy(self.TagJson, ['name'])

             // Set all tags as a list of just tag names
             self.allTagsAsList = this.TagJson.map(function (o) { return o.name })
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
        * Add tag to the currentUserBook
        */
       addTag (tagName) {
         // only add if it isn't already in list
         let result = _.includes(this.tagsToAdd, tagName)
         if (!result) {
           this.tagsToAdd.push(tagName)
         }
         this.$emit('tagListUpdated', this.tagsToAdd)
       },
       /**
        * Got a clearTags signal from parent
        */
       clearTags () {
         this.tagsToAdd = []
       },
       /**
        * Delete tag from the currentUserBook
        */
       deleteTag (tagName) {
         let index = this.tagsToAdd.indexOf(tagName)
         this.tagsToAdd.splice(index, 1)
         this.$emit('tagListUpdated', this.tagsToAdd)
       }
     },
     /**
      * Computed values
      */
     computed: {
       currentTagList: function () {
         let result = _.difference(this.allTagsAsList, this.tagsToAdd)
         return result
       }
     }
   }
</script>

<style scoped>
div {
cursor: auto
}
.clickable {
cursor: pointer
}
</style>
