import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes = [
  {
    path: '/',
    redirect: '/dashboard'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { noAuth: true }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
    meta: { noAuth: true }
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('@/views/Dashboard.vue'),
    meta: { title: '仪表盘' }
  },
  {
    path: '/voices',
    name: 'Voices',
    component: () => import('@/views/Voices.vue'),
    meta: { title: '声音管理' }
  },
  {
    path: '/tts',
    name: 'TTS',
    component: () => import('@/views/TTS.vue'),
    meta: { title: 'TTS 转换' }
  },
  {
    path: '/live',
    name: 'Live',
    component: () => import('@/views/Live.vue'),
    meta: { title: '直播面板' }
  },
  {
    path: '/scripts',
    name: 'Scripts',
    component: () => import('@/views/Scripts.vue'),
    meta: { title: '脚本编辑' }
  },
  {
    path: '/feedback',
    name: 'Feedback',
    component: () => import('@/views/Feedback.vue'),
    meta: { title: '声音评价' }
  },
  {
    path: '/stats',
    name: 'Stats',
    component: () => import('@/views/Stats.vue'),
    meta: { title: '直播统计' }
  },
  {
    path: '/help',
    name: 'Help',
    component: () => import('@/views/Help.vue'),
    meta: { title: '帮助中心' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫 - 检查登录状态
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  if (!to.meta.noAuth && !userStore.token) {
    next('/login')
  } else if (to.meta.noAuth && userStore.token) {
    next('/dashboard')
  } else {
    next()
  }
})

export default router