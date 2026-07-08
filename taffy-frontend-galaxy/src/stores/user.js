import { defineStore } from 'pinia'
import { login, register, getUserInfo } from '../api/auth'

function safeParseUser() {
  try { return JSON.parse(localStorage.getItem('taffy_user') || 'null') }
  catch { return null }
}

export const useUserStore = defineStore('user', {
  state: () => ({ token: localStorage.getItem('taffy_token') || '', user: safeParseUser() }),
  getters: { isLogin: state => Boolean(state.token), username: state => state.user?.username || '星河用户' },
  actions: {
    async doLogin(form) {
      const data = await login(form)
      this.token = data.token
      this.user = { id: data.userId || data.id, username: data.username || form.username }
      localStorage.setItem('taffy_token', data.token)
      localStorage.setItem('taffy_user', JSON.stringify(this.user))
      return data
    },
    async doRegister(form) { return register(form) },
    async loadUserInfo() {
      if (!this.token) return null
      const data = await getUserInfo()
      this.user = data || this.user
      localStorage.setItem('taffy_user', JSON.stringify(this.user))
      return this.user
    },
    logout() {
      this.token = ''
      this.user = null
      localStorage.removeItem('taffy_token')
      localStorage.removeItem('taffy_user')
    }
  }
})
