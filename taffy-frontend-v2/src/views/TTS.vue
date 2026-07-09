<template>
  <div class="page page-narrow">
    <PageHeader title="TTS 转换" subtitle="选择已训练声音模型，将直播文本合成为音频。">
      <el-button @click="loadAll">刷新历史</el-button>
    </PageHeader>
    <div class="grid grid-2">
      <div class="card">
        <h3 class="panel-title">合成参数</h3>
        <el-form label-position="top">
          <el-form-item label="声音模型（可选）">
            <el-select v-model="form.voiceModelId" filterable clearable class="full" placeholder="不选则使用默认声音">
              <el-option v-for="v in readyVoices" :key="v.id" :label="`${v.name}（${v.status}）`" :value="v.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="文本内容">
            <el-input v-model="form.text" class="editor-textarea" type="textarea" :rows="10" maxlength="800" show-word-limit placeholder="请输入要合成的直播话术" />
          </el-form-item>
          <el-form-item label="语速 speed：0.5～2.0"><el-slider v-model="form.speed" :min="0.5" :max="2" :step="0.1" show-input /></el-form-item>
          <el-form-item label="音调 pitch：0.5～2.0"><el-slider v-model="form.pitch" :min="0.5" :max="2" :step="0.1" show-input /></el-form-item>
          <el-space wrap>
            <el-button type="primary" :loading="converting" @click="convert">合成语音</el-button>
            <el-button @click="useTemplate">填入示例话术</el-button>
            <el-button @click="clearText">清空</el-button>
          </el-space>
        </el-form>
      </div>
      <div class="card">
        <div class="space-between"><h3 class="panel-title">播放结果</h3><StatusTag v-if="currentTask.status" :value="currentTask.status" /></div>
        <AudioResult :url="audioUrl" />
        <el-progress v-if="polling" :percentage="progress" :indeterminate="true" style="margin:14px 0" />
        <el-space v-if="currentTask.taskId" wrap>
          <el-button type="success" @click="download(currentTask.taskId)">下载音频</el-button>
          <el-button @click="queryStatus(currentTask.taskId)">查询状态</el-button>
        </el-space>
        <el-divider />
        <h3 class="panel-title">合成历史</h3>
        <div v-loading="historyLoading">
          <div v-for="item in history" :key="item.id || item.taskId" class="list-item">
            <div class="space-between">
              <div style="min-width:0">
                <strong>{{ previewText(item) }}</strong>
                <div class="muted">{{ item.createdAt || item.status || '历史任务' }}</div>
              </div>
              <el-space>
                <StatusTag :value="item.status" />
                <el-button size="small" @click="download(item.id || item.taskId)">下载</el-button>
              </el-space>
            </div>
          </div>
          <EmptyState v-if="!historyLoading && history.length===0" description="暂无 TTS 历史" />
        </div>
      </div>
    </div>
  </div>
</template>
<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import PageHeader from '../components/PageHeader.vue'
import EmptyState from '../components/EmptyState.vue'
import StatusTag from '../components/StatusTag.vue'
import AudioResult from '../components/AudioResult.vue'
import { getVoiceList } from '../api/voice'
import { convertTTS, downloadTTS, getTTSHistory, getTTSStatus } from '../api/tts'
import { normalizeAudioUrl, saveBlob } from '../utils/download'
const voices = ref([])
const history = ref([])
const historyLoading = ref(false)
const converting = ref(false)
const polling = ref(false)
const progress = ref(35)
const audioUrl = ref('')
const timer = ref(null)
const currentTask = reactive({ taskId: null, status: '' })
const route = useRoute()
const form = reactive({ voiceModelId: null, text: '大家好，欢迎来到我的直播间。今天我们会介绍几款适合日常使用的好物。', speed: 1, pitch: 1 })
const readyVoices = computed(() => voices.value.filter(v => {
  const status = String(v.status || '').toLowerCase()
  return !v.status || ['就绪'].includes(v.status) || ['success', 'completed'].includes(status)
}))
function previewText(item) { return (item.textContent || item.text || 'TTS 合成任务').slice(0, 50) }
function useTemplate() { form.text = '欢迎来到直播间，喜欢的朋友可以点点关注。今天的福利马上开始，大家不要错过。' }
function clearText() { form.text = '' }
async function loadAll() {
  voices.value = await getVoiceList().catch(() => []) || []
  // 从 URL 参数预选模型（从评价页跳转过来）
  const qId = route.query.voiceModelId
  if (qId) {
    const num = Number(qId)
    if (!isNaN(num) && voices.value.some(v => v.id === num)) {
      form.voiceModelId = num
    }
  }
  historyLoading.value = true
  try { history.value = await getTTSHistory().catch(() => []) || [] }
  finally { historyLoading.value = false }
}
async function convert() {
  if (!form.text.trim()) return ElMessage.warning('请输入文本内容')
  converting.value = true
  try {
    const data = await convertTTS({ ...form })
    currentTask.taskId = data?.taskId || data?.id
    currentTask.status = data?.status || 'processing'
    audioUrl.value = normalizeAudioUrl(data, currentTask.taskId)
    ElMessage.success('合成任务已提交')
    if (currentTask.taskId) startPolling(currentTask.taskId)
    await loadAll()
  } finally { converting.value = false }
}
function startPolling(taskId) {
  polling.value = true
  clearInterval(timer.value)
  timer.value = setInterval(async () => {
    const done = await queryStatus(taskId, true)
    if (done) {
      clearInterval(timer.value)
      polling.value = false
      await loadAll()
    }
  }, 2500)
}
async function queryStatus(taskId, silent = false) {
  const data = await getTTSStatus(taskId)
  currentTask.status = data?.status || currentTask.status
  if (!silent) ElMessage.success(`当前状态：${currentTask.status}`)
  const normalized = String(currentTask.status || '').toLowerCase()
  return ['completed', 'success', 'failed'].includes(normalized) || ['已完成', '失败'].includes(currentTask.status)
}
async function download(taskId) {
  const blob = await downloadTTS(taskId)
  saveBlob(blob, `tts_${taskId}.wav`)
}
onMounted(loadAll)
onBeforeUnmount(() => clearInterval(timer.value))
</script>
