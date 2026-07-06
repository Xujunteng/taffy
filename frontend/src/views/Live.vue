<template>
  <div class="live-page">
    <h2 class="page-title">直播面板</h2>

    <el-row :gutter="20">
      <el-col :span="14">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>实时 TTS 控制台</span>
              <el-tag :type="obsConnected ? 'success' : 'danger'">
                OBS {{ obsConnected ? '已连接' : '未连接' }}
              </el-tag>
            </div>
          </template>

          <!-- 脚本选择 -->
          <el-form-item label="选择脚本" style="margin-bottom: 16px">
            <el-select v-model="selectedScript" placeholder="选择直播脚本" style="width: 100%">
              <el-option v-for="s in scripts" :key="s.id" :label="s.title" :value="s.id" />
            </el-select>
          </el-form-item>

          <!-- 声音选择 -->
          <el-form-item label="声音模型" style="margin-bottom: 16px">
            <el-select v-model="selectedVoice" placeholder="选择声音模型" style="width: 100%">
              <el-option v-for="v in voices" :key="v.id" :label="v.name" :value="v.id" />
            </el-select>
          </el-form-item>

          <!-- 快速发送区域 -->
          <div class="quick-send">
            <el-input
              v-model="ttsText"
              type="textarea"
              :rows="3"
              placeholder="输入要朗读的文本..."
              @keyup.enter.ctrl="handleSend"
            />
            <el-button
              type="primary"
              :loading="sending"
              @click="handleSend"
              style="margin-top: 8px; width: 100%"
            >
              <el-icon><Promotion /></el-icon> 发送到 OBS (Ctrl+Enter)
            </el-button>
          </div>

          <!-- 发送历史 -->
          <div class="send-history" style="margin-top: 16px">
            <h4>发送历史</h4>
            <div v-for="item in sendHistory" :key="item.time" class="history-row">
              <span class="history-text">{{ item.text.substring(0, 50) }}...</span>
              <span class="history-time">{{ item.time }}</span>
            </div>
            <el-empty v-if="sendHistory.length === 0" description="暂无记录" :image-size="40" />
          </div>
        </el-card>
      </el-col>

      <el-col :span="10">
        <!-- OBS 连接状态 -->
        <el-card style="margin-bottom: 20px">
          <template #header><span>OBS 连接设置</span></template>
          <el-form label-width="80px">
            <el-form-item label="地址">
              <el-input v-model="obsConfig.host" placeholder="localhost:4455" />
            </el-form-item>
            <el-form-item label="密码">
              <el-input v-model="obsConfig.password" type="password" placeholder="OBS WebSocket 密码" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleOBSConnect">
                {{ obsConnected ? '重新连接' : '连接 OBS' }}
              </el-button>
              <el-button v-if="obsConnected" type="danger" @click="handleOBSDisconnect">断开</el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <!-- 当前脚本预览 -->
        <el-card>
          <template #header><span>当前脚本内容</span></template>
          <div class="script-preview">
            <div v-if="currentScript">
              <h4>{{ currentScript.title }}</h4>
              <p class="script-content">{{ currentScript.content }}</p>
            </div>
            <el-empty v-else description="未选择脚本" :image-size="60" />
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { convertTTS } from '@/api/tts'
import { getVoiceList } from '@/api/voice'
import { getScriptList, getScriptDetail } from '@/api/scripts'

const sending = ref(false)
const ttsText = ref('')
const obsConnected = ref(false)
const selectedScript = ref('')
const selectedVoice = ref('')
const scripts = ref([])
const voices = ref([])
const currentScript = ref(null)
const sendHistory = ref([])

const obsConfig = ref({
  host: 'localhost:4455',
  password: ''
})

const handleSend = async () => {
  if (!ttsText.value || !selectedVoice.value) {
    ElMessage.warning('请选择声音模型并输入文本')
    return
  }
  sending.value = true
  try {
    await convertTTS({
      voiceModelId: selectedVoice.value,
      text: ttsText.value,
      speed: 1.0,
      pitch: 1.0
    })
    sendHistory.value.unshift({
      text: ttsText.value,
      time: new Date().toLocaleTimeString()
    })
    ttsText.value = ''
    ElMessage.success('已发送')
  } catch {
    ElMessage.error('发送失败')
  } finally {
    sending.value = false
  }
}

const handleOBSConnect = () => {
  // TODO: 实现真实的 OBS WebSocket 连接
  obsConnected.value = !obsConnected.value
  ElMessage.success(obsConnected.value ? 'OBS 连接成功' : 'OBS 已断开')
}

const handleOBSDisconnect = () => {
  obsConnected.value = false
}

onMounted(async () => {
  try {
    const [voiceRes, scriptRes] = await Promise.all([
      getVoiceList().catch(() => ({ data: [] })),
      getScriptList().catch(() => ({ data: [] }))
    ])
    voices.value = (voiceRes.data || []).filter(v => v.status === '就绪')
    scripts.value = scriptRes.data || []
  } catch {}
})
</script>

<style scoped>
.page-title {
  margin-bottom: 20px;
  font-size: 22px;
  color: #333;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.quick-send {
  margin-top: 8px;
}

.send-history {
  max-height: 200px;
  overflow-y: auto;
}

.history-row {
  display: flex;
  justify-content: space-between;
  padding: 6px 0;
  border-bottom: 1px solid #f5f5f5;
}

.history-text {
  font-size: 13px;
  color: #333;
}

.history-time {
  font-size: 12px;
  color: #999;
}

.script-preview {
  max-height: 250px;
  overflow-y: auto;
}

.script-content {
  white-space: pre-wrap;
  font-size: 13px;
  color: #666;
  line-height: 1.6;
}
</style>