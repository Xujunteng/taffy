<template>
  <div class="page page-narrow">
    <PageHeader title="仪表盘" subtitle="欢迎回来！查看你的声音模型、合成历史和最近活动。">
      <el-button type="primary" @click="$router.push('/tts')">开始合成</el-button>
    </PageHeader>

    <!-- 统计卡片 -->
    <div class="grid grid-4" v-loading="loading">
      <div class="card stat-card" @click="$router.push('/voices')" style="cursor:pointer">
        <div class="stat-icon" style="background:#ecf5ff;color:#409EFF"><el-icon :size="28"><Microphone /></el-icon></div>
        <div><div class="stat-number">{{ overview.voiceCount || 0 }}</div><div class="stat-label">声音模型</div></div>
      </div>
      <div class="card stat-card" @click="$router.push('/tts')" style="cursor:pointer">
        <div class="stat-icon" style="background:#f0f9eb;color:#67C23A"><el-icon :size="28"><Headset /></el-icon></div>
        <div><div class="stat-number">{{ overview.ttsCount || 0 }}</div><div class="stat-label">TTS 合成</div></div>
      </div>
      <div class="card stat-card" @click="$router.push('/scripts')" style="cursor:pointer">
        <div class="stat-icon" style="background:#fdf6ec;color:#E6A23C"><el-icon :size="28"><Document /></el-icon></div>
        <div><div class="stat-number">{{ overview.scriptCount || 0 }}</div><div class="stat-label">直播脚本</div></div>
      </div>
      <div class="card stat-card" @click="$router.push('/stats')" style="cursor:pointer">
        <div class="stat-icon" style="background:#fef0f0;color:#F56C6C"><el-icon :size="28"><VideoPlay /></el-icon></div>
        <div><div class="stat-number">{{ overview.liveCount || 0 }}</div><div class="stat-label">直播场次</div></div>
      </div>
    </div>

    <div class="grid grid-3" style="margin-top:16px">
      <!-- 快速操作 -->
      <div class="card">
        <h3 class="panel-title">快速操作</h3>
        <el-menu :default-active="''" class="quick-menu">
          <el-menu-item @click="$router.push('/voices')">
            <el-icon><Upload /></el-icon><span>上传声音 & 训练模型</span>
          </el-menu-item>
          <el-menu-item @click="$router.push('/tts')">
            <el-icon><Microphone /></el-icon><span>文本转语音合成</span>
          </el-menu-item>
          <el-menu-item @click="$router.push('/scripts')">
            <el-icon><Edit /></el-icon><span>编辑直播脚本</span>
          </el-menu-item>
          <el-menu-item @click="$router.push('/live')">
            <el-icon><VideoCamera /></el-icon><span>启动直播面板</span>
          </el-menu-item>
          <el-menu-item @click="$router.push('/feedback')">
            <el-icon><Star /></el-icon><span>浏览声音评价</span>
          </el-menu-item>
          <el-menu-item @click="$router.push('/help')">
            <el-icon><QuestionFilled /></el-icon><span>查看帮助文档</span>
          </el-menu-item>
        </el-menu>
      </div>

      <!-- 最近活动 -->
      <div class="card" style="grid-column:span 2">
        <div class="space-between">
          <h3 class="panel-title">最近活动</h3>
          <el-button link @click="$router.push('/stats')">查看全部</el-button>
        </div>
        <div v-if="recent.length" class="timeline">
          <div v-for="(item, i) in recent" :key="i" class="timeline-item">
            <div class="tl-dot" :style="{background: item.type === 'tts' ? '#409EFF' : '#67C23A'}"></div>
            <div class="tl-content">
              <el-tag :type="item.type === 'tts' ? '' : 'success'" size="small">{{ item.type === 'tts' ? 'TTS 合成' : '声音评价' }}</el-tag>
              <p>{{ (item.detail || '无详情').slice(0, 60) }}{{ (item.detail || '').length > 60 ? '...' : '' }}</p>
              <span class="muted" style="font-size:12px">{{ item.time }}</span>
            </div>
          </div>
        </div>
        <EmptyState v-else description="暂无活动，去合成一段语音吧！">
          <el-button type="primary" @click="$router.push('/tts')">开始使用</el-button>
        </EmptyState>
      </div>
    </div>

    <!-- 直播会话 -->
    <div class="card" style="margin-top:16px">
      <div class="space-between">
        <h3 class="panel-title">最近直播会话</h3>
        <el-button link @click="$router.push('/stats')">查看全部</el-button>
      </div>
      <el-table :data="sessions.slice(0,5)" empty-text="暂无直播会话">
        <el-table-column prop="platform" label="平台" width="120" />
        <el-table-column prop="scriptId" label="脚本" width="100" />
        <el-table-column prop="startTime" label="开始时间" width="180" />
        <el-table-column prop="endTime" label="结束时间" width="180" />
        <el-table-column prop="statsData" label="统计数据" show-overflow-tooltip />
      </el-table>
    </div>
  </div>
</template>
<script setup>
import { onMounted, ref } from 'vue'
import PageHeader from '../components/PageHeader.vue'
import EmptyState from '../components/EmptyState.vue'
import { getStatsOverview, getLiveSessions, getRecentActivity } from '../api/stats'

const loading = ref(false)
const overview = ref({})
const sessions = ref([])
const recent = ref([])

async function load() {
  loading.value = true
  try {
    overview.value = await getStatsOverview().catch(() => ({})) || {}
    sessions.value = await getLiveSessions().catch(() => []) || []
    recent.value = await getRecentActivity().catch(() => []) || []
  } finally { loading.value = false }
}
onMounted(load)
</script>
<style scoped>
.stat-card { display:flex; align-items:center; gap:16px; padding:20px; transition:transform .15s,box-shadow .15s; }
.stat-card:hover { transform:translateY(-2px); box-shadow:0 4px 12px rgba(0,0,0,.1); }
.stat-icon { width:56px; height:56px; border-radius:12px; display:flex; align-items:center; justify-content:center; }
.stat-number { font-size:28px; font-weight:700; line-height:1.2; }
.stat-label { color:#909399; font-size:13px; margin-top:2px; }
.quick-menu { border:none; }
.quick-menu .el-menu-item { height:44px; line-height:44px; }
.timeline { position:relative; padding-left:8px; }
.timeline-item { display:flex; gap:12px; padding:10px 0; border-bottom:1px solid #f5f5f5; }
.timeline-item:last-child { border-bottom:none; }
.tl-dot { width:10px; height:10px; border-radius:50%; margin-top:6px; flex-shrink:0; }
.tl-content p { margin:4px 0 2px; color:#555; }
</style>
