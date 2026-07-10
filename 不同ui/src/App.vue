<template>
  <div v-if="isAuthPage" class="guest-shell">
    <router-view />
    <div class="guest-theme-switcher"><ThemeSwitcher /></div>
  </div>
  <el-container v-else class="app-layout">
    <el-aside :width="collapsed ? '72px' : '246px'" class="side">
      <div class="brand" :class="{ collapsed }">
        <span class="brand-icon">🎙️</span>
        <span v-if="!collapsed">Taffy 直播助手</span>
      </div>
      <el-menu router :default-active="$route.path" :collapse="collapsed" class="nav-menu">
        <el-menu-item v-for="item in menu" :key="item.path" :index="item.path">
          <el-icon><component :is="item.icon" /></el-icon>
          <template #title>{{ item.title }}</template>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="topbar">
        <div class="flex">
          <el-button text circle @click="collapsed = !collapsed"><el-icon><Fold v-if="!collapsed" /><Expand v-else /></el-icon></el-button>
          <strong>{{ $route.meta.title }}</strong>
        </div>
        <div class="flex topbar-actions">
          <ThemeSwitcher class="hide-theme-label" />
          <el-tag type="success" effect="plain"><span class="badge-dot"></span>开发环境</el-tag>
          <el-dropdown>
            <span class="flex user-entry">
              <el-avatar :size="32">{{ userInitial }}</el-avatar>
              <span class="hide-sm">{{ userStore.username }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="refreshUser">刷新用户信息</el-dropdown-item>
                <el-dropdown-item divided @click="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-main class="main"><router-view v-slot="{ Component }"><keep-alive include="LiveView"><component :is="Component" /></keep-alive></router-view></el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from './stores/user'
import ThemeSwitcher from './components/ThemeSwitcher.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const collapsed = ref(false)
const isAuthPage = computed(() => Boolean(route.meta.guest))
const userInitial = computed(() => (userStore.username || '用').slice(0, 1).toUpperCase())

const menu = [
  { title: '仪表盘', path: '/dashboard', icon: 'DataBoard' },
  { title: '声音管理', path: '/voices', icon: 'Microphone' },
  { title: 'TTS 转换', path: '/tts', icon: 'Headset' },
  { title: '直播面板', path: '/live', icon: 'VideoPlay' },
  { title: '脚本编辑', path: '/scripts', icon: 'Document' },
  { title: '声音评价', path: '/feedback', icon: 'Star' },
  { title: '直播统计', path: '/stats', icon: 'TrendCharts' },
  { title: '帮助中心', path: '/help', icon: 'QuestionFilled' },
  { title: '个人中心', path: '/profile', icon: 'User' }
]

async function refreshUser() {
  await userStore.loadUserInfo()
  ElMessage.success('用户信息已刷新')
}
function logout() {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.app-layout { min-height: 100vh; }
.side { background: var(--sidebar-bg); border-right: 1px solid var(--border); transition: width .2s ease; overflow: hidden; backdrop-filter: var(--backdrop); }
.brand { height: 64px; display: flex; align-items: center; justify-content: center; gap: 8px; font-weight: 900; color: var(--primary); border-bottom: 1px solid var(--border); white-space: nowrap; }
.brand.collapsed { gap: 0; }
.brand-icon { font-size: 24px; }
.nav-menu { border-right: 0; padding-top: 8px; }
.topbar { background: var(--topbar-bg); border-bottom: 1px solid var(--border); display: flex; align-items: center; justify-content: space-between; backdrop-filter: var(--backdrop); }
.main { padding: 0; background: transparent; }
.guest-shell { min-height:100vh; }
.guest-theme-switcher { position:fixed; z-index:50; top:18px; right:18px; }
.user-entry { cursor: pointer; outline: none; }
@media (max-width: 760px) {
  .side { position: fixed; z-index: 20; height: 100vh; transform: translateX(-100%); }
  .side:hover { transform: translateX(0); }
  .hide-sm { display: none; }
  .topbar-actions .theme-trigger span { display:none; }
  .guest-theme-switcher { top:10px; right:10px; }
}
</style>
