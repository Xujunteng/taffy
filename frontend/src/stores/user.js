import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login, register, getUserInfo } from '@/api/auth'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const username = ref(localStorage.getItem('username') || '')
  const userId = ref(localStorage.getItem('userId') || '')

  const isLoggedIn = computed(() => !!token.value)

  async function doLogin(loginData) {
    const res = await login(loginData)
    token.value = res.data.token
    username.value = res.data.username
    userId.value = res.data.userId
    localStorage.setItem('token', res.data.token)
    localStorage.setItem('username', res.data.username)
    localStorage.setItem('userId', res.data.userId)
    return res
  }

  async function doRegister(registerData) {
    const res = await register(registerData)
    return res
  }

  function logout() {
    token.value = ''
    username.value = ''
    userId.value = ''
    localStorage.removeItem('token')
    localStorage.removeItem('username')
    localStorage.removeItem('userId')
  }

  return {
    token,
    username,
    userId,
    isLoggedIn,
    doLogin,
    doRegister,
    logout
  }
})