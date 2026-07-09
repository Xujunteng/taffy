<template>
  <div class="page page-narrow">
    <PageHeader title="直播统计" subtitle="查看 TTS 使用趋势、直播概览和会话记录。">
      <el-button @click="load">刷新</el-button>
    </PageHeader>
    <div class="grid grid-4" v-loading="loading">
      <StatCard label="声音模型" :value="overview.voiceCount || 0" icon="Microphone" />
      <StatCard label="TTS 合成" :value="overview.ttsCount || 0" icon="Headset" />
      <StatCard label="脚本数量" :value="overview.scriptCount || 0" icon="Document" />
      <StatCard label="直播会话" :value="overview.liveCount || sessions.length || 0" icon="VideoPlay" />
    </div>

    <div class="grid grid-2" style="margin-top:16px">
      <!-- TTS 趋势图 -->
      <div class="card">
        <h3 class="panel-title">TTS 近 7 天趋势</h3>
        <div v-if="trend.length" class="svg-chart">
          <svg :viewBox="`0 0 ${trend.length * 80 + 40} 180`" width="100%" height="180" style="display:block">
            <!-- 网格线 -->
            <line v-for="y in 4" :key="'g'+y" :x1="40" :y1="y*35" :x2="trend.length*80+20" :y2="y*35" stroke="#e5e7eb" stroke-dasharray="4,4" />
            <!-- 柱状条 -->
            <g v-for="(item, i) in trend" :key="i">
              <rect :x="i*80+55" :y="160 - item.height" width="44" :height="item.height" rx="4" :fill="item.color || '#409EFF'" />
              <text :x="i*80+77" :y="158 - item.height" text-anchor="middle" font-size="12" fill="#666">{{ item.count }}</text>
              <text :x="i*80+77" y="178" text-anchor="middle" font-size="11" fill="#909399">{{ item.label }}</text>
            </g>
          </svg>
        </div>
        <EmptyState v-else description="暂无趋势数据" />
      </div>

      <!-- 统计明细 -->
      <div class="card">
        <h3 class="panel-title">统计明细</h3>
        <el-table :data="liveStats" empty-text="暂无统计数据" height="240">
          <el-table-column prop="date" label="日期" width="110" />
          <el-table-column prop="platform" label="平台" />
          <el-table-column prop="viewerCount" label="观看" width="80" />
          <el-table-column prop="ttsCount" label="TTS" width="80" />
        </el-table>
      </div>
    </div>

    <!-- 最近活动 -->
    <div class="card" style="margin-top:16px">
      <h3 class="panel-title">最近活动</h3>
      <div v-if="recent.length" class="activity-list">
        <div v-for="(item, i) in recent" :key="i" class="activity-item">
          <el-tag :type="item.type === 'tts' ? 'primary' : 'success'" size="small" style="margin-right:10px">{{ item.type === 'tts' ? 'TTS' : '评价' }}</el-tag>
          <span style="flex:1;min-width:0;overflow:hidden;text-overflow:ellipsis;white-space:nowrap">{{ item.detail || '无详情' }}</span>
          <span class="muted" style="margin-left:12px;white-space:nowrap">{{ item.time }}</span>
        </div>
      </div>
      <EmptyState v-else description="暂无活动记录" />
    </div>

    <!-- 直播会话 -->
    <div class="card" style="margin-top:16px">
      <h3 class="panel-title">直播会话记录</h3>
      <el-table :data="sessions" v-loading="loading" empty-text="暂无直播会话">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="platform" label="平台" width="120" />
        <el-table-column prop="scriptId" label="脚本ID" width="100" />
        <el-table-column prop="startTime" label="开始时间" width="170" />
        <el-table-column prop="endTime" label="结束时间" width="170" />
        <el-table-column prop="statsData" label="统计数据" show-overflow-tooltip />
      </el-table>
    </div>
  </div>
</template>
<script setup>
import { onMounted, ref } from 'vue'
import PageHeader from '../components/PageHeader.vue'
import StatCard from '../components/StatCard.vue'
import EmptyState from '../components/EmptyState.vue'
import { getLiveSessions, getLiveStats, getStatsOverview, getTtsTrend, getRecentActivity } from '../api/stats'

const loading = ref(false)
const overview = ref({})
const sessions = ref([])
const liveStats = ref([])
const trend = ref([])
const recent = ref([])

async function load() {
  loading.value = true
  try {
    overview.value = await getStatsOverview().catch(() => ({})) || {}
    sessions.value = await getLiveSessions().catch(() => []) || []
    liveStats.value = await getLiveStats().catch(() => []) || []
    const rawTrend = await getTtsTrend().catch(() => []) || []
    recent.value = await getRecentActivity().catch(() => []) || []

    // 处理趋势数据为 SVG 柱状图
    const colors = ['#409EFF', '#67C23A', '#E6A23C', '#F56C6C', '#909399', '#409EFF', '#67C23A']
    const maxCount = Math.max(1, ...rawTrend.map(t => t.count || 0))
    trend.value = rawTrend.map((t, i) => ({
      label: (t.date || '').slice(5), // MM-DD
      count: t.count || 0,
      height: Math.max(8, Math.round((t.count || 0) / maxCount * 130)),
      color: colors[i % colors.length]
    }))
  } finally { loading.value = false }
}
onMounted(load)
</script>
<style scoped>
.svg-chart text { font-family: inherit; }
.activity-item { display:flex; align-items:center; padding:8px 0; border-bottom:1px solid #f0f0f0; }
.activity-item:last-child { border-bottom:none; }
</style>
