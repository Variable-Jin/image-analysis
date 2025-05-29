import { createRouter, createWebHistory } from 'vue-router'
import LoginPage from '../components/Login.vue'
import SignUpPage from '../components/signUp.vue'


const routes = [
  {
    path: '/login',
    name: 'Login',
    component: LoginPage
  },
  {
    path: '/signup',
    name: 'SignUp',
    component: SignUpPage
  }

]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
