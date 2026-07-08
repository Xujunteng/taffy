import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'

const routes = [
  { path: '/', redirect: '/dashboard' },
  { path: '/login', name: 'login', component: () => import('../views/Login.vue'), meta: { guest: true, title: '登录' } },
  { path: '/register', name: 'register', component: () => import('../views/Register.vue'), meta: { guest: true, title: '注册' } },
  { path: '/dashboard', name: 'dashboard', component: () => import('../views/Dashboard.vue'), meta: { title: '星河主页' } },
  { path: '/voices', name: 'voices', component: () => import('../views/Voices.vue'), meta: { title: '声音管理' } },
  { path: '/tts', name: 'tts', component: () => import('../views/TTS.vue'), meta: { title: 'TTS 转换' } },
  { path: '/live', name: 'live', component: () => import('../views/Live.vue'), meta: { title: '直播面板' } },
  { path: '/scripts', name: 'scripts', component: () => import('../views/Scripts.vue'), meta: { title: '脚本编辑' } },
  { path: '/feedback', name: 'feedback', component: () => import('../views/Feedback.vue'), meta: { title: '声音评价' } },
  { path: '/stats', name: 'stats', component: () => import('../views/Stats.vue'), meta: { title: '直播统计' } },
  { path: '/help', name: 'help', component: () => import('../views/Help.vue'), meta: { title: '帮助中心' } },
  { path: '/:pathMatch(.*)*', redirect: '/dashboard' }
]

const router = createRouter({ history: createWebHistory(), routes, scrollBehavior: () => ({ top: 0 }) })

router.beforeEach(to => {
  document.title = `${to.meta.title || '首页'} - Taffy 星河直播助手`
  const store = useUserStore()
  if (!to.meta.guest && !store.isLogin) return '/login'
  if (to.meta.guest && store.isLogin) return '/dashboard'
  return true
})
export default router
