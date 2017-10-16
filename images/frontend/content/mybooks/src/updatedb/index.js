/**
 * Methods to help add Authors and Books to database
 *
 */
import Auth from '../auth'

// URL of API
const HOSTNAME = location.hostname
const API_URL = 'http://' + HOSTNAME + ':8080'
// const API_URL = 'http://localhost:8080'

export default {
  /**
   * Try and add an author to the database.
   * If the author already exists, do nothing
   *
   * params:
   * context- Vue component that contains $axios
   * author- Author bean
   */
  tryAddAuthor (context, author) {
    console.log('add author ' + author.name)
    let self = this

    // Check for author existing
    const authString = Auth.getAuthHeader()
    const url = API_URL + '/author?name=' + author.name

    // Make GET call to /author?name=foo to see if author exists
    context.$axios.get(url, { headers: { Authorization: authString } })
      .then((response) => {
        let listOfAuthors = response.data.data
        if (listOfAuthors.length <= 0) {
          self.addAuthor(context, author)
        } else {
          window.Event.$emit('updatedb.authorcreated', 'Author ' + author.name + ' already exists. Continuing.')
        }
      })
      .catch(function (error) {
        console.log(error)
      })
  },
  /**
   * Add the author to the database
   *
   * params:
   * context- Vue component that contains $axios
   * author- Author bean
   */
  addAuthor (context, author) {
    console.log('really add author: ' + author.name)

    // Add author
    const authString = Auth.getAuthHeader()
    const url = API_URL + '/author'

    // Make POST call to /author
    context.$axios.post(url, author, { headers: { Authorization: authString } })
      .then((response) => {
        window.Event.$emit('updatedb.authorcreated', 'Author ' + author.name + ' created!')
      })
      .catch(function (error) {
        window.Event.$emit('updatedb.error', 'Unable to create author "' + author.name + '": ' + error)
        console.log(error)
      })
  },
  /**
   * Add a book to the database.
   *
   * params:
   * context- Vue component that contains $axios
   * book- Book bean
   */
  addBook (context, book) {
    console.log('add book ' + book.title)
    console.log('add book ' + book.authorName)
    console.log('add book ' + book.authorKey)

    let self = this
    const authString = Auth.getAuthHeader()

    // Make GET call to /author?name=foo to see if author exists
    const url = API_URL + '/author?name=' + book.authorName
    console.log('making call to ' + url)
    context.$axios.get(url, { headers: { Authorization: authString } })
      .then((response) => {
        let listOfAuthors = response.data.data
        if (listOfAuthors.length <= 0) {
          // no author
          self.addBook_noauthor(context, book)
        } else {
          // author exists
          // Get the first author from array.
          let authorId = listOfAuthors[0].id
          console.log('author already exists: ' + authorId)
          self.addBook_authorcreated(context, book, authorId)
        }
      })
      .catch(function (error) {
        console.log(error)
      })
  },
  /**
   * No author exists so add one. Then create the book.
   * To create an author, we make a call to /query/author
   * and then post that to /author to create.
   *
   */
  addBook_noauthor (context, book) {
    let self = this
    let authString = Auth.getAuthHeader()

    console.log('addbook_noauthor: ' + book.authorKey)

    // Get Author information from /query into 'authorJson'
    let url = API_URL + '/query/author?author=' + book.authorKey
    console.log('addBook_noauthor. do a get on: ' + url)
    context.$axios.get(url, { headers: { Authorization: authString } })
      .then((response) => {
        // We expect the response to be an array of 1
        let authorJson = response.data.data[0]
        console.log(authorJson)

        // Add new author with 'authorJson'
        const posturl = API_URL + '/author'
        console.log('addBook. do a POST to ' + posturl)
        // Make ajax call to /author
        context.$axios.post(posturl, authorJson, { headers: { Authorization: authString } })
          .then((response) => {
            let authorId = response.data.id
            console.log('AUTHOR: ' + response.data)
            console.log('Created author: ' + authorId)
            self.addBook_authorcreated(context, book, authorId)
          })
          .catch(function (error) {
            // Couldn't create author. error out
            window.Event.$emit('updatedb.error', 'Unable to create author "' + book.authorName + '": ' + error)
            console.log(error)
          })
      })
      .catch(function (error) {
        // Unable to get author info from /query endpoint
        console.log(error)
      })
  },
  /**
   * Create the actual book
   *
   * params:
   * context - Vue component
   * book- Book bean
   * authorId- ID of author
   */
  addBook_authorcreated (context, book, authorid) {
    console.log('create book for existing author ' + authorid)

    const authString = Auth.getAuthHeader()

    // Create a subset of the openlibrary data for the POST
    let data = {
      imageLarge: book.imageLarge,
      imageMedium: book.imageMedium,
      imageSmall: book.imageSmall,
      authorId: authorid,
      description: book.description,
      firstPublishedYear: book.firstPublishedYear,
      openlibraryWorkUrl: book.openlibraryWorkUrl,
      subjects: book.subjects,
      isbns: book.isbns,
      title: book.title
    }

    // Make POST call to /book
    const url = API_URL + '/book'
    console.log('create book at url: ' + url)
    console.log('create with workurl: ' + book.openlibraryWorkUrl)
    context.$axios.post(url, data, { headers: { Authorization: authString } })
      .then((response) => {
        console.log('Created book!: ' + response.data)
        window.Event.$emit('updatedb.bookcreated', 'Book ' + book.title + ' saved!')
      })
      .catch(function (error) {
        if (error.response) {
          if (error.response.status === 409) {
            // duplidate book
            window.Event.$emit('updatedb.book.409', 'Book "' + book.title + '" already exists')
          } else {
            window.Event.$emit('updatedb.error', 'Unable to create book "' + book.title + '": ' + error)
          }
        } else {
          window.Event.$emit('updatedb.error', 'Unable to create book "' + book.title + '": ' + error)
          console.log(error)
        }
      })
  }
}
