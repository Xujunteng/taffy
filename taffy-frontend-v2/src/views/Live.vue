<template>
  <div class="page page-narrow">
    <!-- 顶部状态栏 -->
    <div class="live-topbar">
      <div class="live-stat" :class="{ active: streamActive }">
        <span class="live-dot"></span>
        <span>{{ streamActive ? '直播中' : '未开播' }}</span>
      </div>
      <div class="live-stat"><strong>{{ timerDisplay }}</strong></div>
      <div class="live-stat">TTS <strong>{{ ttsCount }}</strong> 次</div>
      <div class="live-stat">声音: <strong>{{ currentVoiceName || '默认' }}</strong></div>
      <div class="live-stat" :style="{color: obsConnected ? '#67C23A' : '#909399'}">
        {{ obsConnected ? '● OBS 已连接' : '○ OBS 未连接' }}
      </div>
      <div style="flex:1"></div>
      <el-button v-if="!obsConnected" size="small" type="primary" @click="showObsDialog = true">连接 OBS</el-button>
      <el-button v-else size="small" type="danger" @click="disconnectObs">断开</el-button>
    </div>

    <!-- 平台集成 -->
    <div class="card platform-bar">
      <div class="space-between">
        <div style="display:flex;align-items:center;gap:12px">
          <span class="panel-title" style="margin:0">推流平台</span>
          <el-select v-model="selectedPlatform" size="small" style="width:140px" @change="onPlatformChange">
            <el-option v-for="p in platforms" :key="p.key" :label="p.name" :value="p.key" />
          </el-select>
          <el-tag v-if="streamActive" type="danger" size="small">推流中</el-tag>
        </div>
        <el-button size="small" type="primary" @click="showPlatformGuide = !showPlatformGuide">
          {{ showPlatformGuide ? '收起' : '推流指南' }}
        </el-button>
      </div>
      <div v-if="showPlatformGuide" class="platform-guide">
        <el-divider style="margin:12px 0" />
        <div class="grid grid-2">
          <div>
            <h4>{{ currentPlatform.name }} 推流配置</h4>
            <el-descriptions :column="1" border size="small">
              <el-descriptions-item label="RTMP 服务器">
                <span>{{ currentPlatform.rtmp }}</span>
                <el-button link size="small" @click="copyText(currentPlatform.rtmp)">复制</el-button>
              </el-descriptions-item>
              <el-descriptions-item label="串流密钥">
                <span>{{ streamKey || '（在平台直播设置中获取）' }}</span>
                <el-button v-if="streamKey" link size="small" @click="copyText(streamKey)">复制</el-button>
              </el-descriptions-item>
              <el-descriptions-item label="OBS 状态">{{ obsConnected ? '✅ 已连接' : '⚠️ 未连接' }}</el-descriptions-item>
            </el-descriptions>
            <el-input v-model="streamKey" size="small" placeholder="粘贴你的串流密钥" style="margin-top:8px" clearable />
          </div>
          <div>
            <h4>开播步骤</h4>
            <el-steps direction="vertical" :active="setupStep" finish-status="success" style="height:auto">
              <el-step title="连接 OBS" description="点击右上角「连接 OBS」" />
              <el-step title="配置推流" :description="`OBS → 设置 → 推流 → 填入${currentPlatform.name}的服务器和密钥`" />
              <el-step title="开始推流" description="OBS 点击「开始推流」，回到本页面点击「开始直播」" />
            </el-steps>
          </div>
        </div>
      </div>
    </div>

    <div class="grid grid-2">
      <!-- 左列：TTS + 话术 + 音效 -->
      <div>
        <!-- TTS 发送 -->
        <div class="card">
          <h3 class="panel-title">TTS 发送</h3>
          <el-form label-position="top" size="small">
            <el-row :gutter="12">
              <el-col :span="12">
                <el-form-item label="声音模型"><el-select class="full" v-model="form.voiceModelId" filterable><el-option v-for="v in voices" :key="v.id" :label="v.name" :value="v.id" /></el-select></el-form-item>
              </el-col>
              <el-col :span="6">
                <el-form-item label="语速"><el-slider v-model="form.speed" :min="0.5" :max="2" :step="0.1" :show-tooltip="false" /></el-form-item>
              </el-col>
              <el-col :span="6">
                <el-form-item label="音调"><el-slider v-model="form.pitch" :min="0.5" :max="2" :step="0.1" :show-tooltip="false" /></el-form-item>
              </el-col>
            </el-row>
            <el-form-item label="话术内容"><el-input v-model="form.text" type="textarea" :rows="4" maxlength="600" show-word-limit @keydown.ctrl.enter.prevent="send" /></el-form-item>
          </el-form>
          <el-space wrap>
            <el-button type="primary" :loading="sending" @click="send">发送 TTS</el-button>
            <el-button @click="startTimer" :disabled="timerRunning">开始计时</el-button>
            <el-button @click="form.text=''">清空</el-button>
          </el-space>
        </div>

        <!-- 场景话术面板 -->
        <div class="card" style="margin-top:12px">
          <div class="space-between"><h3 class="panel-title">场景话术</h3><el-button link size="small" @click="showCustomDialog = true">+自定义</el-button></div>
          <el-tabs v-model="phraseTab" type="card" class="phrase-tabs">
            <el-tab-pane v-for="cat in phraseCategories" :key="cat.key" :label="cat.label" :name="cat.key">
              <div class="phrase-grid">
                <el-button v-for="(txt, i) in getPhrases(cat.key)" :key="i" size="small" class="phrase-btn" @click="quick(txt)">{{ txt.length > 18 ? txt.slice(0,18)+'...' : txt }}</el-button>
              </div>
            </el-tab-pane>
          </el-tabs>
        </div>

        <!-- 弹幕 -->
        <div class="card" style="margin-top:12px">
          <div class="space-between"><h3 class="panel-title">弹幕 <span class="muted" style="font-size:12px">{{ danmakuConnected ? 'B站已连接' : '未连接B站' }}</span></h3>
            <el-space>
              <el-input v-model="biliRoomId" size="small" placeholder="B站直播间ID" style="width:140px" clearable />
              <el-button v-if="!danmakuConnected" size="small" type="primary" @click="connectBili">连接</el-button>
              <el-button v-else size="small" type="danger" @click="disconnectBili">断开</el-button>
            </el-space>
          </div>
          <div class="danmaku-box" ref="danmakuBox">
            <div v-for="(d, i) in danmakuList" :key="i" class="danmaku-item" :class="'dm-' + d.type">
              <span class="dm-user">{{ d.user }}</span>
              <span v-if="d.type==='gift'" class="dm-gift">🎁 {{ d.content }}</span>
              <span v-else-if="d.type==='enter'">👋 进入直播间</span>
              <span v-else>{{ d.content }}</span>
              <span class="dm-time">{{ d.time }}</span>
            </div>
            <div v-if="danmakuList.length===0" class="muted" style="text-align:center;padding:20px">等待弹幕中...</div>
          </div>
          <div style="margin-top:8px;display:flex;gap:6px">
            <el-input v-model="mockDanmaku" size="small" placeholder="模拟弹幕内容" style="flex:1" @keyup.enter="sendMockDanmaku" />
            <el-button size="small" @click="sendMockDanmaku">发送模拟</el-button>
          </div>
        </div>

        <!-- 音效 -->
        <div class="card" style="margin-top:12px">
          <div class="space-between"><h3 class="panel-title">音效面板</h3><el-upload :auto-upload="false" :show-file-list="false" accept=".wav,.mp3,.ogg" :on-change="uploadCustomSound"><el-button size="small" type="primary">+ 上传音效</el-button></el-upload></div>
          <div style="display:flex;flex-wrap:wrap;gap:6px">
            <span v-for="sfx in allSounds" :key="sfx.key" style="position:relative;display:inline-block">
              <el-button size="small" @click="playSound(sfx.key)" :type="sfx.type" :style="sfx.style">{{ sfx.label }}</el-button>
              <span v-if="sfx.key.startsWith('custom_')" @click="removeCustomSound(sfx.key)" style="position:absolute;top:-6px;right:-6px;width:18px;height:18px;border-radius:50%;background:#f56c6c;color:#fff;font-size:12px;line-height:18px;text-align:center;cursor:pointer" title="删除">×</span>
            </span>
          </div>
          <div v-if="customSounds.length" class="muted" style="margin-top:8px;font-size:12px">自定义音效：点击 × 可删除</div>
          <div v-if="playingLabel" class="muted" style="margin-top:4px;font-size:12px">正在播放: {{ playingLabel }}</div>
        </div>
      </div>

      <!-- 右列：脚本播放器 + 自动模式 + 历史 -->
      <div>
        <!-- 脚本分段播放器 -->
        <div class="card">
          <div class="space-between"><h3 class="panel-title">脚本播放器</h3><el-button link size="small" @click="refreshScripts">刷新</el-button></div>
          <el-select v-model="selectedScript" class="full" clearable filterable placeholder="选择脚本库中的脚本" @change="applyScript" style="margin-bottom:8px">
            <el-option v-for="s in scripts" :key="s.id" :label="`${s.title} (${s.category||'未分类'})`" :value="s.id" />
          </el-select>
          <div v-if="segments.length" class="segment-list">
            <div v-for="(seg, i) in segments" :key="i" class="segment-item" :class="{ played: seg.played, current: i === segIndex }" @click="playSegment(i)">
              <span class="seg-num">{{ seg.played ? '✓' : i + 1 }}</span>
              <span class="seg-text">{{ seg.text.slice(0, 40) }}{{ seg.text.length > 40 ? '...' : '' }}</span>
              <el-button v-if="seg.played && seg.audioUrl" link size="small" @click.stop="audioUrl = seg.audioUrl">重播</el-button>
            </div>
          </div>
          <EmptyState v-else description="选择脚本后可分段播放" />
        </div>

        <!-- 自动播报 -->
        <div class="card" style="margin-top:12px">
          <div class="space-between"><h3 class="panel-title">自动播报</h3><el-switch v-model="autoMode" @change="onAutoModeChange" /></div>
          <template v-if="autoMode">
            <div style="margin:8px 0"><span class="muted">间隔 {{ autoInterval }}s · {{ queue.length - queueIndex }}/{{ queue.length }} 条</span></div>
            <el-slider v-model="autoInterval" :min="3" :max="30" :step="1" show-input />
            <el-button :type="autoRunning ? 'danger' : 'primary'" @click="toggleAutoPlay" style="margin-top:8px">{{ autoRunning ? '停止' : '开始自动播报' }}</el-button>
          </template>
          <div v-if="queue.length" class="queue-list" style="margin-top:8px">
            <div v-for="(item, i) in queue" :key="i" class="queue-row" :class="{ done: i < queueIndex, now: i === queueIndex }">
              <span>{{ i < queueIndex ? '✓' : i === queueIndex ? '▶' : i+1 }}</span>
              <span>{{ item.slice(0, 30) }}</span>
            </div>
          </div>
        </div>

        <!-- 播放器 + 历史 -->
        <div class="card" style="margin-top:12px">
          <h3 class="panel-title">播放器</h3>
          <AudioResult :url="audioUrl" />
          <el-divider />
          <h3 class="panel-title">发送历史</h3>
          <div v-for="item in sendHistory" :key="item.time" class="list-item">
            <div class="space-between"><strong>{{ item.text.slice(0, 30) }}</strong><el-button size="small" @click="audioUrl = item.audioUrl">重播</el-button></div>
            <div class="muted">{{ item.time }}</div>
          </div>
          <EmptyState v-if="sendHistory.length===0" description="暂无发送记录" />
        </div>
      </div>
    </div>

    <!-- OBS 对话框 -->
    <el-dialog v-model="showObsDialog" title="连接 OBS Studio" width="480px" destroy-on-close>
      <el-form label-width="110px">
        <el-form-item label="WebSocket 地址"><el-input v-model="obsUrl" placeholder="ws://localhost:4455" /></el-form-item>
        <el-form-item label="密码"><el-input v-model="obsPassword" type="password" show-password placeholder="未设置则留空" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="showObsDialog=false">取消</el-button><el-button type="primary" :loading="obsConnecting" @click="connectObs">连接</el-button></template>
    </el-dialog>

    <!-- 自定义话术对话框 -->
    <el-dialog v-model="showCustomDialog" title="自定义话术" width="440px">
      <el-form label-position="top">
        <el-form-item label="话术文本"><el-input v-model="customPhrase" type="textarea" :rows="3" maxlength="100" /></el-form-item>
        <el-form-item label="分类"><el-select v-model="customCategory" class="full"><el-option v-for="c in phraseCategories" :key="c.key" :label="c.label" :value="c.key" /></el-select></el-form-item>
      </el-form>
      <template #footer><el-button @click="showCustomDialog=false">取消</el-button><el-button type="primary" @click="addCustomPhrase">添加</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import EmptyState from '../components/EmptyState.vue'
import AudioResult from '../components/AudioResult.vue'
import request from '../api/request'
import { getVoiceList } from '../api/voice'
import { getScriptList } from '../api/scripts'
import { convertTTS } from '../api/tts'
import { normalizeAudioUrl } from '../utils/download'

// 平台集成
const platforms = [
  { key: 'douyin', name: '抖音', rtmp: 'rtmp://push-rtmp.douyin.com/live/', icon: '🎵' },
  { key: 'kuaishou', name: '快手', rtmp: 'rtmp://live-push.kuaishou.com/live/', icon: '📱' },
  { key: 'bilibili', name: 'B站', rtmp: 'rtmp://live-push.bilivideo.com/live-bvc/', icon: '📺' },
  { key: 'youtube', name: 'YouTube', rtmp: 'rtmp://a.rtmp.youtube.com/live2/', icon: '▶️' },
  { key: 'custom', name: '自定义 RTMP', rtmp: '', icon: '🔧' },
]
const selectedPlatform = ref('douyin')
const showPlatformGuide = ref(true)
const streamKey = ref('')
const currentPlatform = computed(() => platforms.find(p => p.key === selectedPlatform.value) || platforms[0])
const setupStep = computed(() => streamActive.value ? 3 : obsConnected.value && streamKey.value ? 2 : obsConnected.value ? 1 : 0)
function onPlatformChange() { streamKey.value = '' }
function copyText(text) { if (text) { navigator.clipboard.writeText(text); ElMessage.success('已复制') } }

// === 状态 ===
const voices = ref([])
const scripts = ref([])
const sending = ref(false)
const audioUrl = ref('')
const sendHistory = ref([])
const form = reactive({ voiceModelId: null, text: '', speed: 1, pitch: 1 })
const selectedScript = ref(null)

// 计时器
const timerRunning = ref(false)
const timerSeconds = ref(0)
const ttsCount = ref(0)
const timerRef = ref(null)
const timerDisplay = computed(() => {
  const h = Math.floor(timerSeconds.value / 3600)
  const m = Math.floor((timerSeconds.value % 3600) / 60)
  const s = timerSeconds.value % 60
  return `${String(h).padStart(2, '0')}:${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
})
const currentVoiceName = computed(() => {
  const v = voices.value.find(x => x.id === form.voiceModelId)
  return v ? v.name : null
})

// 话术面板
const phraseTab = ref('welcome')
const showCustomDialog = ref(false)
const customPhrase = ref('')
const customCategory = ref('welcome')
const phraseCategories = [
  { key: 'welcome', label: '欢迎', phrases: ['欢迎新朋友进入直播间！', '欢迎各位宝子们，今天福利多多！', '刚进来的朋友记得点个关注哦～'] },
  { key: 'thanks', label: '感谢', phrases: ['感谢大家的支持！', '谢谢老板的礼物！', '感谢每一位在直播间陪伴的朋友～'] },
  { key: 'promo', label: '促单', phrases: ['喜欢可以直接拍，库存不多了！', '这个价格真的很划算，手慢无！', '还有最后5单，拍完就没了！'] },
  { key: 'interact', label: '互动', phrases: ['大家觉得怎么样？弹幕告诉我！', '想看什么打在公屏上～', '点赞过万马上抽奖！'] },
  { key: 'bye', label: '告别', phrases: ['今天的直播就到这里，明天见！', '感谢陪伴，晚安各位～', '别忘了关注，下次开播第一时间通知！'] },
]
const customPhrases = reactive({})
function getPhrases(key) {
  const builtin = phraseCategories.find(c => c.key === key)?.phrases || []
  const extra = customPhrases[key] || []
  return [...builtin, ...extra]
}
function addCustomPhrase() {
  if (!customPhrase.value.trim()) return ElMessage.warning('请输入话术')
  const key = customCategory.value
  if (!customPhrases[key]) customPhrases[key] = []
  customPhrases[key].push(customPhrase.value.trim())
  ElMessage.success('已添加')
  customPhrase.value = ''
  showCustomDialog.value = false
}
function quick(text) { form.text = text }

// 音效系统
const audioCtx = ref(null)
const playingLabel = ref('')
function getAudioCtx() {
  if (!audioCtx.value) audioCtx.value = new (window.AudioContext || window.webkitAudioContext)()
  if (audioCtx.value.state === 'suspended') audioCtx.value.resume()
  return audioCtx.value
}
function playTone(freq, duration, type = 'sine', vol = 0.25, delay = 0) {
  const ctx = getAudioCtx()
  const osc = ctx.createOscillator()
  const gain = ctx.createGain()
  osc.type = type; osc.frequency.value = freq
  const t = ctx.currentTime + delay
  gain.gain.setValueAtTime(vol, t)
  gain.gain.exponentialRampToValueAtTime(0.001, t + duration)
  osc.connect(gain); gain.connect(ctx.destination)
  osc.start(t); osc.stop(t + duration)
}
function playNoise(duration, vol = 0.12, delay = 0) {
  const ctx = getAudioCtx()
  const len = Math.floor(ctx.sampleRate * duration)
  const buf = ctx.createBuffer(1, len, ctx.sampleRate)
  const data = buf.getChannelData(0)
  const decaySamples = ctx.sampleRate * 0.1
  for (let i = 0; i < len; i++) {
    const env = i > len - decaySamples ? (len - i) / decaySamples : 1
    data[i] = (Math.random() * 2 - 1) * vol * env
  }
  const src = ctx.createBufferSource(); src.buffer = buf
  const gain = ctx.createGain(); gain.gain.value = 1
  const filter = ctx.createBiquadFilter(); filter.type = 'bandpass'; filter.frequency.value = 1000; filter.Q.value = 0.6
  src.connect(filter); filter.connect(gain); gain.connect(ctx.destination)
  src.start(ctx.currentTime + delay)
}

const presetSounds = [
  { key: 'ding', label: '🔔 提示音', type: '', style: '' },
  { key: 'success', label: '✨ 完成音', type: 'success', style: '' },
  { key: 'emphasis', label: '⚡ 强调音', type: 'danger', style: '' },
  { key: 'countdown', label: '⏰ 倒计时', type: 'info', style: '' },
  { key: 'coin', label: '💰 金币音', type: 'warning', style: '' },
  { key: 'cheer', label: '🎉 欢呼声', type: 'success', style: '' },
  { key: 'fanfare', label: '🎵 入场号角', type: '', style: 'background:#fef0f0;border-color:#fab6b6' },
  { key: 'alert', label: '📢 公告音', type: 'danger', style: '' },
]
const customSounds = ref(JSON.parse(localStorage.getItem('taffy_sfx') || '[]'))
const allSounds = computed(() => [...presetSounds, ...customSounds.value])

function playSound(key) {
  const preset = presetSounds.find(s => s.key === key)
  playingLabel.value = preset?.label || customSounds.value.find(s => s.key === key)?.label || ''
  setTimeout(() => { playingLabel.value = '' }, 2000)

  // 自定义音效
  const custom = customSounds.value.find(s => s.key === key)
  if (custom) { const a = new Audio(custom.data); a.play().catch(() => {}); return }

  // 预设音效
  getAudioCtx()
  if (key === 'ding') {
    playTone(880, 0.1, 'sine', 0.2)
    playTone(1100, 0.07, 'sine', 0.16, 0.08)
  } else if (key === 'success') {
    playTone(523, 0.1, 'sine', 0.18)
    playTone(659, 0.1, 'sine', 0.18, 0.1)
    playTone(784, 0.18, 'sine', 0.2, 0.2)
  } else if (key === 'emphasis') {
    playTone(220, 0.15, 'sawtooth', 0.12)
    playTone(440, 0.12, 'sawtooth', 0.07, 0.04)
  } else if (key === 'countdown') {
    playTone(660, 0.08, 'sine', 0.16)
    playTone(660, 0.08, 'sine', 0.16, 0.25)
    playTone(660, 0.08, 'sine', 0.16, 0.5)
    playTone(880, 0.2, 'sine', 0.2, 0.75)
  } else if (key === 'coin') {
    playTone(1318, 0.05, 'sine', 0.1)
    playTone(1568, 0.05, 'sine', 0.08, 0.05)
    playTone(2093, 0.1, 'sine', 0.08, 0.1)
  } else if (key === 'cheer') {
    playNoise(0.4, 0.1)
    playNoise(0.25, 0.07, 0.2)
    playTone(350, 0.2, 'sawtooth', 0.03, 0.05)
    playTone(450, 0.15, 'sawtooth', 0.03, 0.18)
  } else if (key === 'fanfare') {
    playTone(392, 0.1, 'sawtooth', 0.08)
    playTone(523, 0.1, 'sawtooth', 0.08, 0.1)
    playTone(659, 0.1, 'sawtooth', 0.08, 0.2)
    playTone(784, 0.25, 'sawtooth', 0.1, 0.3)
  } else if (key === 'alert') {
    playTone(800, 0.08, 'sine', 0.13)
    playTone(600, 0.08, 'sine', 0.13, 0.12)
    playTone(800, 0.08, 'sine', 0.13, 0.24)
    playTone(600, 0.15, 'sine', 0.13, 0.36)
  }
}

function uploadCustomSound(file) {
  if (!file?.raw) return
  if (file.raw.size > 2 * 1024 * 1024) return ElMessage.warning('音效文件不能超过 2MB')
  const reader = new FileReader()
  reader.onload = () => {
    const item = {
      key: 'custom_' + Date.now(),
      label: '🎧 ' + (file.name.length > 12 ? file.name.slice(0, 12) + '...' : file.name),
      type: '', style: 'background:#f0f9eb;border-color:#b3e19d',
      data: reader.result
    }
    customSounds.value.push(item)
    localStorage.setItem('taffy_sfx', JSON.stringify(customSounds.value))
    ElMessage.success('音效已添加')
  }
  reader.readAsDataURL(file.raw)
}
// 删除自定义音效
function removeCustomSound(key) {
  customSounds.value = customSounds.value.filter(s => s.key !== key)
  localStorage.setItem('taffy_sfx', JSON.stringify(customSounds.value))
}

// 脚本分段
const segments = ref([])
const segIndex = ref(0)
function applyScript(id) {
  const s = scripts.value.find(x => x.id === id)
  if (!s) return
  const lines = (s.content || '').split('\n').map(l => l.trim()).filter(Boolean)
  segments.value = lines.map(text => ({ text, played: false, audioUrl: null }))
  segIndex.value = 0
}
async function playSegment(i) {
  if (!form.voiceModelId) return ElMessage.warning('请先选择声音模型')
  segIndex.value = i
  sending.value = true
  try {
    const data = await convertTTS({ voiceModelId: form.voiceModelId, text: segments.value[i].text, speed: form.speed, pitch: form.pitch })
    const url = normalizeAudioUrl(data, data?.taskId || data?.id)
    segments.value[i].audioUrl = url
    segments.value[i].played = true
    audioUrl.value = url
    ttsCount.value++
    sendHistory.value.unshift({ text: segments.value[i].text, time: new Date().toLocaleTimeString(), audioUrl: url })
    if (audioUrl.value) { timerRunning.value = true; resetTimerTick() }
  } finally { sending.value = false }
}

// TTS
async function send() {
  if (!form.voiceModelId) return ElMessage.warning('请选择声音模型')
  if (!form.text.trim()) return ElMessage.warning('请输入话术')
  sending.value = true
  try {
    const data = await convertTTS({ ...form })
    const url = normalizeAudioUrl(data, data?.taskId || data?.id)
    audioUrl.value = url
    ttsCount.value++
    sendHistory.value.unshift({ text: form.text, time: new Date().toLocaleTimeString(), audioUrl: url })
    if (sendHistory.value.length > 50) sendHistory.value.length = 50
  } finally { sending.value = false }
}

// 计时器
function startTimer() { timerRunning.value = true; resetTimerTick() }
function resetTimerTick() { clearInterval(timerRef.value); timerRef.value = setInterval(() => { if (timerRunning.value) timerSeconds.value++ }, 1000) }

// 自动播报
const autoMode = ref(false)
const autoInterval = ref(8)
const autoRunning = ref(false)
const queue = ref([])
const queueIndex = ref(0)
const autoTimer = ref(null)
function onAutoModeChange(val) {
  if (val) { queue.value = form.text.split('\n').map(s => s.trim()).filter(Boolean); if (!queue.value.length) queue.value = [form.text.trim()].filter(Boolean); queueIndex.value = 0 }
  else stopAutoPlay()
}
function toggleAutoPlay() {
  if (autoRunning.value) { stopAutoPlay(); return }
  if (!queue.value.length) { const lines = form.text.split('\n').map(s => s.trim()).filter(Boolean); if (!lines.length) return ElMessage.warning('请输入话术'); queue.value = lines; queueIndex.value = 0 }
  autoRunning.value = true; playNextInQueue()
}
function stopAutoPlay() { autoRunning.value = false; clearTimeout(autoTimer.value) }
function playNextInQueue() {
  if (!autoRunning.value || queueIndex.value >= queue.value.length) { stopAutoPlay(); if (queueIndex.value >= queue.value.length) ElMessage.success('队列完成'); return }
  form.text = queue.value[queueIndex.value]
  send().finally(() => { queueIndex.value++; autoTimer.value = setTimeout(playNextInQueue, autoInterval.value * 1000) })
}

// 弹幕
const danmakuConnected = ref(false)
const biliRoomId = ref('')
const danmakuList = ref([])
const mockDanmaku = ref('')
const danmakuBox = ref(null)
let eventSource = null

function connectBili() {
  if (!biliRoomId.value) return ElMessage.warning('请输入B站直播间ID')
  request.post('/api/live/danmaku/connect', { roomId: biliRoomId.value }).then(() => {
    danmakuConnected.value = true
    ElMessage.success('已连接B站弹幕')
  })
  startSSE()
}
function disconnectBili() {
  request.post('/api/live/danmaku/disconnect')
  danmakuConnected.value = false
  if (eventSource) { eventSource.close(); eventSource = null }
}
function startSSE() {
  if (eventSource) { eventSource.close(); eventSource = null }
  const token = localStorage.getItem('taffy_token')
  if (!token) { console.warn('SSE: no token, skip'); return }
  const url = `/api/live/danmaku/stream?token=${encodeURIComponent(token)}`
  console.log('SSE connecting:', url)
  eventSource = new EventSource(url)
  eventSource.onopen = () => console.log('SSE connected')
  eventSource.addEventListener('danmaku', e => {
    try {
      const d = JSON.parse(e.data)
      danmakuList.value.unshift(d)
      if (danmakuList.value.length > 100) danmakuList.value.length = 100
    } catch (ex) { console.warn('SSE parse error:', ex) }
  })
  eventSource.addEventListener('push', e => {
    try {
      const d = JSON.parse(e.data)
      if (d.text) {
        form.text = d.text
        if (d.voiceModelId) form.voiceModelId = d.voiceModelId
        send()
        ElMessage.info('收到API推送: ' + d.text.slice(0, 30))
      }
    } catch (ex) { console.warn('SSE push parse error:', ex) }
  })
  eventSource.onerror = (err) => { console.warn('SSE error, will retry'); danmakuConnected.value = false }
}
async function sendMockDanmaku() {
  if (!mockDanmaku.value.trim()) return
  await request.post('/api/live/danmaku/mock', { user: '模拟观众', content: mockDanmaku.value })
  mockDanmaku.value = ''
}

// OBS（简化实现，保留之前逻辑）
const obsSocket = ref(null); const obsConnected = ref(false); const obsConnecting = ref(false)
const showObsDialog = ref(false); const obsUrl = ref('ws://localhost:4455'); const obsPassword = ref('')
const streamActive = ref(false)
let obsRequestId = 0; const obsPending = {}
function connectObs() {
  obsConnecting.value = true
  try {
    const ws = new WebSocket(obsUrl.value)
    ws.onopen = () => { obsConnected.value = true; obsConnecting.value = false; showObsDialog.value = false; ElMessage.success('OBS 已连接') }
    ws.onclose = () => { obsConnected.value = false }
    ws.onerror = () => { obsConnecting.value = false; ElMessage.error('OBS 连接失败') }
    obsSocket.value = ws
  } catch { obsConnecting.value = false; ElMessage.error('OBS 连接失败') }
}
function disconnectObs() { obsSocket.value?.close(); obsConnected.value = false }

async function refreshScripts() { scripts.value = await getScriptList().catch(() => []) || [] }
onMounted(async () => {
  voices.value = await getVoiceList().catch(() => []) || []
  await refreshScripts()
  startSSE()
})
onBeforeUnmount(() => { stopAutoPlay(); clearInterval(timerRef.value); obsSocket.value?.close(); if (eventSource) eventSource.close() })
</script>

<style scoped>
.live-topbar { display:flex; align-items:center; gap:20px; padding:12px 20px; background:#fff; border-radius:12px; border:1px solid #e6eaf2; margin-bottom:16px; flex-wrap:wrap; }
.live-stat { display:flex; align-items:center; gap:6px; font-size:13px; color:#606266; }
.live-stat.active { color:#f56c6c; font-weight:600; }
.live-dot { width:8px; height:8px; border-radius:50%; background:#f56c6c; animation:pulse 1.5s infinite; }
@keyframes pulse { 0%,100%{opacity:1} 50%{opacity:.3} }
.phrase-tabs :deep(.el-tabs__header) { margin-bottom:8px; }
.phrase-grid { display:flex; flex-wrap:wrap; gap:6px; }
.phrase-btn { white-space:normal; text-align:left; height:auto; min-height:32px; }
.segment-list { max-height:300px; overflow-y:auto; }
.segment-item { display:flex; align-items:center; gap:8px; padding:6px 8px; border-radius:6px; cursor:pointer; font-size:13px; transition:background .15s; }
.segment-item:hover { background:#f5f7fa; }
.segment-item.current { background:#ecf5ff; color:#409EFF; }
.segment-item.played { color:#c0c4cc; }
.seg-num { width:22px; text-align:center; font-weight:bold; flex-shrink:0; }
.seg-text { flex:1; min-width:0; overflow:hidden; text-overflow:ellipsis; white-space:nowrap; }
.queue-list { max-height:180px; overflow-y:auto; }
.queue-row { display:flex; gap:6px; padding:2px 0; font-size:12px; color:#909399; }
.queue-row.now { color:#409EFF; font-weight:500; }
.queue-row.done { color:#c0c4cc; text-decoration:line-through; }
.danmaku-box { max-height:200px; overflow-y:auto; background:#fafbfc; border-radius:8px; padding:6px 10px; }
.danmaku-item { display:flex; gap:8px; padding:4px 0; font-size:13px; border-bottom:1px solid #f0f0f0; align-items:baseline; }
.danmaku-item:last-child { border-bottom:none; }
.dm-user { color:#409EFF; font-weight:600; white-space:nowrap; }
.dm-time { color:#c0c4cc; font-size:11px; margin-left:auto; white-space:nowrap; }
.dm-gift { color:#E6A23C; }
.dm-gift .dm-user { color:#E6A23C; }
.platform-bar { margin-bottom:16px; }
.platform-guide h4 { margin:0 0 8px; font-size:15px; }
</style>
