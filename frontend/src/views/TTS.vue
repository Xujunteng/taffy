<template>
  <div class="tts-page">
    <h2 class="page-title">TTS 语音转换</h2>

    <el-row :gutter="20">
      <!-- 左侧：输入区域 -->
      <el-col :span="14">
        <el-card>
          <template #header>
            <span>文本输入</span>
          </template>
          <el-form :model="ttsForm" label-width="80px">
            <el-form-item label="声音模型">
              <el-select v-model="ttsForm.voiceModelId" placeholder="选择已训练的声音模型" style="width: 100%">
                <el-option
                  v-for="voice in readyVoices"
                  :key="voice.id"
                  :label="voice.name"
                  :value="voice.id"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="文本内容">
              <el-input
                v-model="ttsForm.text"
                type="textarea"
                :rows="8"
                placeholder="请输入要转换为语音的文本内容..."
                maxlength="1000"
                show-word-limit
              />
            </el-form-item>
            <el-form-item label="语速">
              <el-slider v-model="ttsForm.speed" :min="0.5" :max="2.0" :step="0.1" show-input />
            </el-form-item>
            <el-form-item label="音调">
              <el-slider v-model="ttsForm.pitch" :min="0.5" :max="2.0" :step="0.1" show-input />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="converting" @click="handleConvert" style="width: 100%">
                <el-icon><Headset /></el-icon> 开始合成
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <!-- 右侧：播放预览和历史 -->
      <el-col :span="10">
        <el-card class="preview-card">
          <template #header>
            <span>音频预览</span>
          </template>
          <div v-if="currentAudio" class="audio-player">
            <audio ref="audioPlayer" :src="currentAudio" controls style="width: 100%"></audio>
            <el-button type="primary" style="margin-top: 12px; width: 100%" @click="handleDownload">
              <el-icon><Download /></el-icon> 下载音频
            </el-button>
          </div>
          <el-empty v-else description="暂无合成结果" />
        </el-card>

        <el-card class="history-card" style="margin-top: 20px">
          <template #header>
            <span>合成历史</span>
          </template>
          <div class="history-list">
            <div
              v-for="item in historyList"
              :key="item.id"
              class="history-item"
              @click="handlePlayHistory(item)"
            >
              <div class="history-text">{{ item.textContent?.substring(0, 30) }}...</div>
              <div class="history-time">{{ item.createdAt }}</div>
            </div>
            <el-empty v-if="historyList.length === 0" description="暂无历史记录" :image-size="60" />
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { convertTTS, getTTSHistory, downloadTTS } from '@/api/tts'
import { getVoiceList } from '@/api/voice'

const converting = ref(false)
const readyVoices = ref([])
const historyList = ref([])
const currentAudio = ref('')
const currentTaskId = ref('')
const audioPlayer = ref(null)

const ttsForm = reactive({
  voiceModelId: '',
  text: '',
  speed: 1.0,
  pitch: 1.0
})

const handleConvert = async () => {
  if (!ttsForm.voiceModelId || !ttsForm.text) {
    ElMessage.warning('请选择声音模型并输入文本')
    return
  }
  converting.value = true
  try {
    const res = await convertTTS(ttsForm)
    currentAudio.value = URL.createObjectURL(new Blob([res], { type: 'audio/wav' }))
    currentTaskId.value = res.taskId
    ElMessage.success('合成成功')
    fetchHistory()
  } catch (error) {
    ElMessage.error('合成失败')
  } finally {
    converting.value = false
  }
}

const handleDownload = () => {
  if (!currentTaskId.value) return
  downloadTTS(currentTaskId.value).then((res) => {
    const url = URL.createObjectURL(new Blob([res], { type: 'audio/wav' }))
    const link = document.createElement('a')
    link.href = url
    link.download = 'tts-output.wav'
    link.click()
    URL.revokeObjectURL(url)
  })
}

const handlePlayHistory = (item) => {
  if (item.audioOutputPath) {
    currentAudio.value = item.audioOutputPath
    currentTaskId.value = item.id
  }
}

const fetchHistory = async () => {
  try {
    const res = await getTTSHistory()
    historyList.value = res.data || []
  } catch {}
}

const fetchVoices = async () => {
  try {
    const res = await getVoiceList()
    readyVoices.value = (res.data || []).filter(v => v.status === '就绪')
  } catch {}
}

onMounted(() => {
  fetchVoices()
  fetchHistory()
})
</script>

<style scoped>
.page-title {
  margin-bottom: 20px;
  font-size: 22px;
  color: #333;
}

.preview-card,
.history-card {
  min-height: 200px;
}

.audio-player {
  text-align: center;
}

.history-list {
  max-height: 250px;
  overflow-y: auto;
}

.history-item {
  padding: 10px 8px;
  border-bottom: 1px solid #eee;
  cursor: pointer;
  transition: background 0.2s;
}

.history-item:hover {
  background: #f5f7fa;
}

.history-text {
  font-size: 14px;
  color: #333;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.history-time {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}
</style>