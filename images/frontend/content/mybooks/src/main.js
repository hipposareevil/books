// The Vue build version to load with the `import` command
// (runtime-only or standalone) has been set in webpack.base.conf with an alias.
import Vue from 'vue'
import App from './App'
import router from './router'
import axios from 'axios'
import Vuex from 'vuex'

Vue.use(Vuex)
Vue.prototype.$axios = axios
Vue.config.productionTip = false

// Event queue for everyone
window.Event = new Vue()

// Storage
// Access via "this.$store.state.count"
const store = new Vuex.Store({
  state: {
    allBooks: {},
    booksView: {},
    allAuthors: {},
    authorsView: {},
    userBooks: {},
    userBooksView: {}
  },
  mutations: {
    // Clear everything from this store
    clearEverything (state) {
      console.log('$store. clear everything')

      state.allBooks = {}
      state.booksView = {}
      state.allAuthors = {}
      state.authorsView = {}
      state.userBooks = {}
      state.userBooksView = {}
    },
    // The currently loaded set of 'books'
    // Note this is an object containing the books JSON and
    // state of the query (start, length, total, end)
    setAllBooks (state, newBooks) {
      state.allBooks = newBooks
    },
    // Set the view options for the BookView
    // This includes grid vs list, etc
    setBooksView (state, view) {
      state.booksView = view
    },
    // The currently loaded set of 'authors'
    // Note this is an object containing the authors JSON and
    // state of the query (start, length, total, end)
    setAllAuthors (state, newAuthors) {
      state.allAuthors = newAuthors
    },
    // Set the view options for the AuthorView
    // This includes grid vs list, etc
    setAuthorsView (state, view) {
      state.authorsView = view
    },
    // The currently loaded set of 'user books'
    // Note this is an object containing the userbooks JSON and
    // state of the query (start, length, total, end)
    setUserBooks (state, newUserBooks) {
      state.userBooks = newUserBooks
    },
    // Set the view options for the UserBooksVue
    // This includes grid vs list, etc
    setUserBooksView (state, view) {
      state.userBooksView = view
    }
  }
})

/* eslint-disable no-new */
new Vue({
  el: '#app',
  router,
  store,
  template: '<App/>',
  components: { App }
})
