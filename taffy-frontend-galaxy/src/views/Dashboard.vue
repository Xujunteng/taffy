<template>
  <div class="page page-narrow">
    <GalaxyHero />
    <div class="grid grid-4 dashboard-stats" v-loading="loading">
      <StatCard label="声音模型" :value="overview.voiceCount || voices.length || 0" desc="已创建模型" icon="Microphone" />
      <StatCard label="TTS 次数" :value="overview.ttsCount || history.length || 0" desc="累计合成任务" icon="Headset" />
      <StatCard label="脚本数量" :value="overview.scriptCount || scripts.length || 0" desc="可用于直播" icon="Document" />
      <StatCard label="直播场次" :value="overview.liveCount || sessions.length || 0" desc="历史会话" icon="VideoPlay" />
    </div>
    <div class="grid grid-2" style="margin-top:16px">
      <div class="card quick-card">
        <h3 class="panel-title">星舰快捷入口</h3>
        <div class="quick-grid">
          <button @click="$router.push('/voices')"><span>🎙️</span><strong>上传声音</strong><em>训练专属模型</em></button>
          <button @click="$router.push('/tts')"><span>🎧</span><strong>文本转语音</strong><em>实时试听下载</em></button>
          <button @click="$router.push('/scripts')"><span>📜</span><strong>直播脚本</strong><em>管理话术库</em></button>
          <button @click="$router.push('/live')"><span>🌊</span><strong>直播控制</strong><em>星河面板发送</em></button>
        </div>
      </div>
      <div class="card">
        <h3 class="panel-title">开发指南完成度</h3>
        <el-steps direction="vertical" :active="6" finish-status="success" style="height:260px">
          <el-step title="工程基础" description="Vue3 / Vite / Element Plus / Pinia / Axios / Router" />
          <el-step title="认证与路由" description="登录注册、JWT、路由守卫、401处理" />
          <el-step title="核心业务" description="声音管理、训练轮询、TTS合成、脚本CRUD" />
          <el-step title="直播功能" description="直播控制台、快捷话术、脚本填入、发送历史" />
          <el-step title="扩展页面" description="反馈评价、直播统计、帮助中心" />
          <el-step title="体验完善" description="星河主题、加载状态、空状态、错误提示、响应式" />
        </el-steps>
      </div>
    </div>
    <div class="card" style="margin-top:16px">
      <div class="space-between"><h3 class="panel-title">最近直播会话</h3><el-button link @click="$router.push('/stats')">查看全部</el-button></div>
      <el-table :data="sessions.slice(0,5)" empty-text="暂无直播会话">
        <el-table-column prop="platform" label="平台" width="120" />
        <el-table-column prop="startTime" label="开始时间" />
        <el-table-column prop="endTime" label="结束时间" />
        <el-table-column prop="statsData" label="统计数据" show-overflow-tooltip />
      </el-table>
    </div>
  </div>
</template>
<script setup>
import { onMounted, ref } from 'vue'
import GalaxyHero from '../components/GalaxyHero.vue'
import StatCard from '../components/StatCard.vue'
import { getStatsOverview, getLiveSessions } from '../api/stats'
import { getVoiceList } from '../api/voice'
import { getTTSHistory } from '../api/tts'
import { getScriptList } from '../api/scripts'
import { asArray } from '../utils/download'
const loading = ref(false)
const overview = ref({})
const sessions = ref([])
const voices = ref([])
const history = ref([])
const scripts = ref([])
async function load() {
  loading.value = true
  try {
    const [overviewRes, sessionRes, voiceRes, historyRes, scriptRes] = await Promise.allSettled([getStatsOverview(), getLiveSessions(), getVoiceList(), getTTSHistory(), getScriptList()])
    overview.value = overviewRes.status === 'fulfilled' ? overviewRes.value || {} : {}
    sessions.value = sessionRes.status === 'fulfilled' ? asArray(sessionRes.value) : []
    voices.value = voiceRes.status === 'fulfilled' ? asArray(voiceRes.value) : []
    history.value = historyRes.status === 'fulfilled' ? asArray(historyRes.value) : []
    scripts.value = scriptRes.status === 'fulfilled' ? asArray(scriptRes.value) : []
  } finally { loading.value = false }
}
onMounted(load)
</script>
<style scoped>
.dashboard-stats { margin-top: 16px; }
.quick-grid { display:grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 12px; }
.quick-grid button { text-align:left; border:1px solid rgba(255,255,255,.16); border-radius:20px; padding:18px; color:#fff; background:linear-gradient(135deg, rgba(125,211,252,.12), rgba(167,139,250,.10)); cursor:pointer; transition:.2s; }
.quick-grid button:hover { transform: translateY(-3px); border-color: rgba(125,211,252,.55); box-shadow:0 18px 44px rgba(0,0,0,.24); }
.quick-grid span { display:block; font-size:28px; margin-bottom:10px; }
.quick-grid strong, .quick-grid em { display:block; }
.quick-grid em { color:var(--muted); font-style:normal; margin-top:4px; font-size:13px; }
@media (max-width: 720px) { .quick-grid { grid-template-columns: 1fr; } }
</style>
