<template>
  <div class="dashboard">
    <h2 class="page-title">仪表盘</h2>

    <!-- 快速入口 -->
    <el-row :gutter="20" class="quick-actions">
      <el-col :span="6" v-for="action in quickActions" :key="action.title">
        <el-card shadow="hover" class="action-card" @click="$router.push(action.path)">
          <div class="action-icon">
            <el-icon :size="32"><component :is="action.icon" /></el-icon>
          </div>
          <div class="action-title">{{ action.title }}</div>
          <div class="action-desc">{{ action.desc }}</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 最近声音模型 & 统计概览 -->
    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="14">
        <el-card class="section-card">
          <template #header>
            <div class="card-header">
              <span>最近的声音模型</span>
              <el-button type="primary" link @click="$router.push('/voices')">查看全部</el-button>
            </div>
          </template>
          <el-table :data="recentVoices" style="width: 100%" v-loading="loading">
            <el-table-column prop="name" label="名称" />
            <el-table-column prop="status" label="状态">
              <template #default="{ row }">
                <el-tag :type="statusType(row.status)">{{ row.status }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createdAt" label="创建时间" />
          </el-table>
          <el-empty v-if="!loading && recentVoices.length === 0" description="暂无声音模型" />
        </el-card>
      </el-col>

      <el-col :span="10">
        <el-card class="section-card">
          <template #header>
            <span>使用统计概览</span>
          </template>
          <div class="stats-grid">
            <div class="stat-item">
              <div class="stat-value">{{ stats.voiceCount }}</div>
              <div class="stat-label">声音模型</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ stats.ttsCount }}</div>
              <div class="stat-label">TTS 转换次数</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ stats.scriptCount }}</div>
              <div class="stat-label">直播脚本</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ stats.liveCount }}</div>
              <div class="stat-label">直播场次</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getVoiceList } from '@/api/voice'
import { getTodayOverview } from '@/api/stats'

const loading = ref(false)
const recentVoices = ref([])

const stats = reactive({
  voiceCount: 0,
  ttsCount: 0,
  scriptCount: 0,
  liveCount: 0
})

const quickActions = [
  { title: '声音管理', desc: '上传音频训练声音', icon: 'Microphone', path: '/voices' },
  { title: 'TTS 转换', desc: '文本转语音合成', icon: 'Headset', path: '/tts' },
  { title: '直播面板', desc: '实时直播辅助', icon: 'VideoCamera', path: '/live' },
  { title: '脚本编辑', desc: '编辑直播脚本', icon: 'Document', path: '/scripts' }
]

const statusType = (status) => {
  const map = { '就绪': 'success', '训练中': 'warning', '失败': 'danger' }
  return map[status] || 'info'
}

onMounted(async () => {
  loading.value = true
  try {
    const [voiceRes, overviewRes] = await Promise.all([
      getVoiceList().catch(() => ({ data: [] })),
      getTodayOverview().catch(() => ({ data: {} }))
    ])
    recentVoices.value = (voiceRes.data || []).slice(0, 5)
    Object.assign(stats, overviewRes.data || {})
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.page-title {
  margin-bottom: 20px;
  font-size: 22px;
  color: #333;
}

.action-card {
  cursor: pointer;
  text-align: center;
  padding: 16px 0;
  transition: transform 0.3s;
}

.action-card:hover {
  transform: translateY(-4px);
}

.action-icon {
  color: #409EFF;
  margin-bottom: 12px;
}

.action-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 4px;
}

.action-desc {
  font-size: 12px;
  color: #999;
}

.section-card {
  min-height: 300px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.stats-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  padding: 10px 0;
}

.stat-item {
  text-align: center;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #409EFF;
}

.stat-label {
  font-size: 13px;
  color: #999;
  margin-top: 4px;
}
</style>