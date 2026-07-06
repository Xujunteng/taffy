<template>
  <div class="stats-page">
    <h2 class="page-title">直播统计</h2>

    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-value">{{ overview.sessionCount }}</div>
          <div class="stat-label">直播场次</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-value">{{ overview.totalDuration }}</div>
          <div class="stat-label">总时长 (分钟)</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-value">{{ overview.ttsUsed }}</div>
          <div class="stat-label">TTS 使用次数</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-value">{{ overview.avgRating }}</div>
          <div class="stat-label">平均评分</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card style="margin-top: 20px">
      <template #header><span>直播会话记录</span></template>
      <el-table :data="sessions" style="width: 100%" v-loading="loading">
        <el-table-column prop="platform" label="平台" width="120" />
        <el-table-column prop="scriptTitle" label="使用脚本" min-width="180" show-overflow-tooltip />
        <el-table-column prop="startTime" label="开始时间" width="180" />
        <el-table-column prop="endTime" label="结束时间" width="180" />
        <el-table-column prop="duration" label="时长(分钟)" width="100" />
      </el-table>
      <el-empty v-if="!loading && sessions.length === 0" description="暂无直播记录" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getTodayOverview, getLiveSessions } from '@/api/stats'

const loading = ref(false)
const sessions = ref([])

const overview = reactive({
  sessionCount: 0,
  totalDuration: 0,
  ttsUsed: 0,
  avgRating: 0
})

onMounted(async () => {
  loading.value = true
  try {
    const [overviewRes, sessionsRes] = await Promise.all([
      getTodayOverview().catch(() => ({ data: {} })),
      getLiveSessions().catch(() => ({ data: [] }))
    ])
    Object.assign(overview, overviewRes.data || {})
    sessions.value = sessionsRes.data || []
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.page-title { margin-bottom: 20px; font-size: 22px; color: #333; }
.stat-card { text-align: center; padding: 10px 0; }
.stat-value { font-size: 32px; font-weight: 700; color: #409EFF; }
.stat-label { font-size: 14px; color: #999; margin-top: 8px; }
</style>