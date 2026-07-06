<template>
  <div id="app">
    <el-container class="layout-container">
      <!-- 侧边栏 -->
      <el-aside v-if="showSidebar" width="220px" class="sidebar">
        <div class="logo">
          <h2>🎙️ 直播助手</h2>
        </div>
        <el-menu
          :default-active="activeMenu"
          router
          background-color="#304156"
          text-color="#bfcbd9"
          active-text-color="#409EFF"
        >
          <el-menu-item index="/dashboard">
            <el-icon><HomeFilled /></el-icon>
            <span>仪表盘</span>
          </el-menu-item>
          <el-menu-item index="/voices">
            <el-icon><Microphone /></el-icon>
            <span>声音管理</span>
          </el-menu-item>
          <el-menu-item index="/tts">
            <el-icon><Headset /></el-icon>
            <span>TTS 转换</span>
          </el-menu-item>
          <el-menu-item index="/live">
            <el-icon><VideoCamera /></el-icon>
            <span>直播面板</span>
          </el-menu-item>
          <el-menu-item index="/scripts">
            <el-icon><Document /></el-icon>
            <span>脚本编辑</span>
          </el-menu-item>
          <el-menu-item index="/feedback">
            <el-icon><Star /></el-icon>
            <span>声音评价</span>
          </el-menu-item>
          <el-menu-item index="/stats">
            <el-icon><DataLine /></el-icon>
            <span>直播统计</span>
          </el-menu-item>
          <el-menu-item index="/help">
            <el-icon><QuestionFilled /></el-icon>
            <span>帮助中心</span>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <el-container>
        <!-- 顶部导航 -->
        <el-header v-if="showSidebar" class="header">
          <div class="header-right">
            <el-dropdown>
              <span class="user-info">
                {{ userStore.username || '用户' }}
                <el-icon><ArrowDown /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="handleLogout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </el-header>

        <!-- 主内容区 -->
        <el-main class="main-content">
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const showSidebar = computed(() => {
  return !['/login', '/register'].includes(route.path)
})

const activeMenu = computed(() => {
  return route.path
})

const handleLogout = () => {
  userStore.logout()
  router.push('/login')
}
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body, #app {
  height: 100%;
}

.layout-container {
  height: 100%;
}

.sidebar {
  background-color: #304156;
  overflow-y: auto;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.logo h2 {
  font-size: 18px;
  font-weight: 600;
}

.header {
  background: #fff;
  border-bottom: 1px solid #e6e6e6;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  padding: 0 20px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-info {
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 4px;
  color: #333;
}

.main-content {
  background-color: #f0f2f5;
  min-height: calc(100vh - 60px);
  padding: 20px;
}
</style>