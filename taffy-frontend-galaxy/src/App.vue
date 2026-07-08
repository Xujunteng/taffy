<template>
  <router-view v-if="isAuthPage" />
  <el-container v-else class="app-layout">
    <el-aside :width="collapsed ? '76px' : '260px'" class="side">
      <div class="brand" :class="{ collapsed }">
        <span class="brand-mark">✦</span>
        <span v-if="!collapsed" class="brand-name">Taffy Galaxy</span>
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
        <div class="flex">
          <el-tag type="success" effect="plain"><span class="badge-dot"></span>星河控制台</el-tag>
          <el-dropdown>
            <span class="flex user-entry">
              <el-avatar :size="34" class="avatar">{{ userInitial }}</el-avatar>
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
      <el-main class="main"><router-view /></el-main>
    </el-container>
  </el-container>
</template>
<script setup>
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from './stores/user'
const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const collapsed = ref(false)
const isAuthPage = computed(() => Boolean(route.meta.guest))
const userInitial = computed(() => (userStore.username || '星').slice(0, 1).toUpperCase())
const menu = [
  { title: '星河主页', path: '/dashboard', icon: 'DataBoard' },
  { title: '声音管理', path: '/voices', icon: 'Microphone' },
  { title: 'TTS 转换', path: '/tts', icon: 'Headset' },
  { title: '直播面板', path: '/live', icon: 'VideoPlay' },
  { title: '脚本编辑', path: '/scripts', icon: 'Document' },
  { title: '声音评价', path: '/feedback', icon: 'Star' },
  { title: '直播统计', path: '/stats', icon: 'TrendCharts' },
  { title: '帮助中心', path: '/help', icon: 'QuestionFilled' }
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
.app-layout { min-height: 100vh; position:relative; z-index:1; }
.side { background: rgba(5, 8, 22, .74); border-right: 1px solid rgba(255,255,255,.14); transition: width .2s ease; overflow: hidden; backdrop-filter: blur(22px); }
.brand { height: 70px; display:flex; align-items:center; justify-content:center; gap:10px; border-bottom: 1px solid rgba(255,255,255,.12); }
.brand-mark { width: 38px; height:38px; border-radius:14px; display:grid; place-items:center; background:linear-gradient(135deg,#38bdf8,#8b5cf6); box-shadow:0 0 34px rgba(125,211,252,.34); font-weight:900; }
.brand-name { font-size: 18px; font-weight:950; letter-spacing:-.03em; }
.nav-menu { border-right:0; padding-top:10px; }
.topbar { background: rgba(5, 8, 22, .58); border-bottom:1px solid rgba(255,255,255,.14); display:flex; align-items:center; justify-content:space-between; backdrop-filter: blur(20px); }
.main { padding:0; min-width:0; }
.user-entry { cursor:pointer; outline:none; color:var(--text); }
.avatar { background:linear-gradient(135deg,#38bdf8,#8b5cf6); }
@media (max-width: 760px) { .side { display:none; } .hide-sm { display:none; } }
</style>
