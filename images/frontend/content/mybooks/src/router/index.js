import Vue from 'vue'
import Router from 'vue-router'
import HomePage from '@/components/HomePage'
import AddAuthor from '@/components/AddAuthor'
import AddBook from '@/components/AddBook'
import AddTag from '@/components/AddTag'
import Users from '@/components/Users'
import ViewShelf from '@/components/shelf/ViewShelf'
import ViewAuthors from '@/components/author/ViewAuthors'
import ViewSingleAuthor from '@/components/author/ViewSingleAuthor'
import ViewBooks from '@/components/book/ViewBooks'
import ViewSingleBook from '@/components/book/ViewSingleBook'

Vue.use(Router)

const router = new Router({
  routes: [
    {
      path: '/',
      name: 'HomePage',
      component: HomePage
    },
    {
      path: '/shelves',
      name: 'ViewShelf',
      component: ViewShelf
    },
    {
      path: '/addauthor',
      name: 'AddAuthor',
      component: AddAuthor
    },
    {
      path: '/addbook',
      name: 'AddBook',
      component: AddBook
    },
    {
      path: '/users',
      name: 'Users',
      component: Users
    },
    {
      path: '/authors',
      name: 'ViewAuthors',
      component: ViewAuthors
    },
    {
      path: '/authors/:id',
      name: 'SingleAuthor',
      component: ViewSingleAuthor
    },
    {
      path: '/books',
      name: 'ViewBooks',
      component: ViewBooks
    },
    {
      path: '/books/:id',
      name: 'SingleBook',
      component: ViewSingleBook
    },
    {
      path: '/addtag',
      name: 'AddTag',
      component: AddTag
    }
  ],
  linkActiveClass: 'is-active'
})

export default router
