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
      <div class="live-stat" :class="{ connected: obsConnected }">
        {{ obsConnected ? '● OBS 已连接' : '○ OBS 未连接' }}
      </div>
      <div style="flex:1"></div>
      <el-button v-if="!streamActive" size="small" type="danger" @click="startStream" :loading="streamStarting">
        <span style="display:flex;align-items:center;gap:4px">🔴 开始直播</span>
      </el-button>
      <el-button v-else size="small" type="info" @click="stopStream" :loading="streamStopping">
        停止直播
      </el-button>
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
                <el-form-item label="声音模型"><el-select class="full" v-model="form.voiceModelId" filterable clearable placeholder="自动选择"><el-option label="自动选择 (Edge TTS)" :value="null" /><el-option v-for="v in voices" :key="v.id" :label="v.name" :value="v.id" /></el-select></el-form-item>
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
              <span v-if="sfx.key.startsWith('custom_')" @click="removeCustomSound(sfx.key)" class="sound-remove" title="删除">×</span>
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
            <div class="muted">{{ item.time }} <el-tag v-if="item.provider" size="small" :type="item.provider==='doubao'?'warning':''" effect="plain" style="margin-left:4px">{{ item.provider === 'doubao' ? '🎵豆包' : '🔊Edge' }}</el-tag></div>
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
defineOptions({ name: 'LiveView' })
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import EmptyState from '../components/EmptyState.vue'
import AudioResult from '../components/AudioResult.vue'
import request from '../api/request'
import { getVoiceList } from '../api/voice'
import { getScriptList } from '../api/scripts'
import { convertTTS } from '../api/tts'
import { startSession, endSession } from '../api/stats'
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
  { key: 'fanfare', label: '🎵 入场号角', type: '', style: 'background:var(--danger-soft);border-color:var(--danger)' },
  { key: 'alert', label: '📢 公告音', type: 'danger', style: '' },
]
const customSounds = ref(JSON.parse(localStorage.getItem('taffy_sfx') || '[]'))
const allSounds = computed(() => [...presetSounds, ...customSounds.value])

// 跟踪当前正在播放的自定义音效，支持点击停止
const currentCustomAudio = ref(null)
const currentCustomKey = ref(null)

function playSound(key) {
  const preset = presetSounds.find(s => s.key === key)
  const custom = customSounds.value.find(s => s.key === key)

  // 自定义音效：如果点击正在播放的 → 停止；否则先停旧的再播新的
  if (custom) {
    if (currentCustomKey.value === key && currentCustomAudio.value) {
      const a = currentCustomAudio.value
      a.pause()
      a.currentTime = 0
      currentCustomAudio.value = null
      currentCustomKey.value = null
      playingLabel.value = ''
      return
    }
    // 停止上一个
    if (currentCustomAudio.value) {
      currentCustomAudio.value.pause()
      currentCustomAudio.value = null
    }
    const a = new Audio(custom.data)
    currentCustomAudio.value = a
    currentCustomKey.value = key
    a.onended = () => {
      if (currentCustomAudio.value === a) {
        currentCustomAudio.value = null
        currentCustomKey.value = null
        playingLabel.value = ''
      }
    }
    playingLabel.value = custom.label || ''
    a.play().catch(() => { currentCustomAudio.value = null; currentCustomKey.value = null })
    return
  }

  // 预设音效：停止任何正在播放的自定义音效
  if (currentCustomAudio.value) {
    currentCustomAudio.value.pause()
    currentCustomAudio.value = null
    currentCustomKey.value = null
  }

  playingLabel.value = preset?.label || ''
  setTimeout(() => { playingLabel.value = '' }, 2000)

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
      type: '', style: 'background:var(--success-soft);border-color:var(--success)',
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
  segIndex.value = i
  sending.value = true
  try {
    const payload = { text: segments.value[i].text, speed: form.speed, pitch: form.pitch }
    if (form.voiceModelId) payload.voiceModelId = form.voiceModelId
    const data = await convertTTS(payload)
    const url = normalizeAudioUrl(data, data?.taskId || data?.id)
    segments.value[i].audioUrl = url
    segments.value[i].played = true
    audioUrl.value = url
    ttsCount.value++
    sendHistory.value.unshift({ text: segments.value[i].text, time: new Date().toLocaleTimeString(), audioUrl: url, provider: data?.provider || '' })
    if (audioUrl.value) { timerRunning.value = true; resetTimerTick() }
  } finally { sending.value = false }
}

// TTS
async function send() {
  if (!form.text.trim()) return ElMessage.warning('请输入话术')
  sending.value = true
  try {
    // voiceModelId 为 null 时后端自动选择声音
    const payload = { text: form.text, speed: form.speed, pitch: form.pitch }
    if (form.voiceModelId) payload.voiceModelId = form.voiceModelId
    const data = await convertTTS(payload)
    const url = normalizeAudioUrl(data, data?.taskId || data?.id)
    const provider = data?.provider || ''
    audioUrl.value = url
    ttsCount.value++
    sendHistory.value.unshift({ text: form.text, time: new Date().toLocaleTimeString(), audioUrl: url, provider })
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
const biliRoomId = ref(localStorage.getItem('taffy_bili_room') || '')
const danmakuList = ref([])
const mockDanmaku = ref('')
const danmakuBox = ref(null)
let eventSource = null

// ===== B站弹幕（通过后端 HTTP API 轮询，绕过 IP 封锁和 Cloudflare 缓冲） =====
let biliPollTimer = null

function connectBili() {
  if (!biliRoomId.value) return ElMessage.warning('请输入B站直播间ID')
  localStorage.setItem('taffy_bili_room', biliRoomId.value)
  disconnectBili()
  danmakuConnected.value = true
  ElMessage.success('已连接B站弹幕（HTTP轮询）')
  biliPollTimer = setInterval(() => fetchBiliDanmaku(), 5000)
  fetchBiliDanmaku()
  startDanmakuPoll()
  startSSE()
}

function disconnectBili() {
  danmakuConnected.value = false
  clearInterval(biliPollTimer)
  biliPollTimer = null
  stopDanmakuPoll()
  if (eventSource) { eventSource.close(); eventSource = null }
}

async function fetchBiliDanmaku() {
  if (!biliRoomId.value || !danmakuConnected.value) return
  try {
    await request.get('/api/live/danmaku/fetch', { params: { roomId: biliRoomId.value } })
    // fetch 把弹幕写入后端缓冲池，poll 会取到
  } catch { /* 忽略 */ }
}

// 弹幕轮询（代替 SSE，因为 Cloudflare 隧道会缓冲 SSE 流）
let danmakuPollTimer = null
let lastDanmakuTime = 0

function startDanmakuPoll() {
  if (danmakuPollTimer) return
  danmakuPollTimer = setInterval(async () => {
    try {
      const data = await request.get('/api/live/danmaku/poll', { params: { since: lastDanmakuTime } })
      if (data?.items?.length) {
        for (const d of data.items) {
          // 去重
          const dup = danmakuList.value.find(x => x.timestamp === d.timestamp && x.content === d.content)
          if (!dup) danmakuList.value.unshift(d)
        }
        if (danmakuList.value.length > 100) danmakuList.value.length = 100
        // 处理 push 事件
        for (const d of data.items) {
          if (d.type === 'push' && d.text) {
            form.text = d.text
            if (d.voiceModelId) form.voiceModelId = d.voiceModelId
            send()
            ElMessage.info('收到API推送: ' + d.text.slice(0, 30))
          }
        }
      }
      if (data?.serverTime) lastDanmakuTime = data.serverTime
    } catch { /* 忽略轮询错误 */ }
  }, 2000)
}

function stopDanmakuPoll() {
  if (danmakuPollTimer) { clearInterval(danmakuPollTimer); danmakuPollTimer = null }
}

// SSE 作为辅助（本地开发时直接连接）
function startSSE() {
  if (eventSource) { eventSource.close(); eventSource = null }
  const token = localStorage.getItem('taffy_token')
  if (!token) return
  const url = `/api/live/danmaku/stream?token=${encodeURIComponent(token)}`
  try {
    eventSource = new EventSource(url)
    eventSource.addEventListener('danmaku', e => {
      try {
        const d = JSON.parse(e.data)
        const dup = danmakuList.value.find(x => x.timestamp === d.timestamp && x.content === d.content)
        if (!dup) danmakuList.value.unshift(d)
        if (danmakuList.value.length > 100) danmakuList.value.length = 100
      } catch (ex) {}
    })
    eventSource.addEventListener('push', e => {
      try {
        const d = JSON.parse(e.data)
        if (d.text) { form.text = d.text; if (d.voiceModelId) form.voiceModelId = d.voiceModelId; send() }
      } catch (ex) {}
    })
  } catch {}
}
async function sendMockDanmaku() {
  if (!mockDanmaku.value.trim()) return
  const msg = {
    type: 'danmaku',
    user: '模拟观众',
    content: mockDanmaku.value.trim(),
    time: new Date().toLocaleTimeString('zh-CN', { hour12: false })
  }
  // 直接添加到本地列表（Cloudflare 隧道会缓冲 SSE 导致收不到）
  danmakuList.value.unshift(msg)
  if (danmakuList.value.length > 100) danmakuList.value.length = 100
  // 同时通过后端广播给其他 SSE 客户端
  request.post('/api/live/danmaku/mock', { user: msg.user, content: msg.content }).catch(() => {})
  mockDanmaku.value = ''
}

// ===== OBS WebSocket 5.x 协议实现 =====
// 协议流程: 打开连接 → 接收 Hello (Op 0) → 发送 Identify (Op 1) → 接收 Identified (Op 2) → 连接成功
const obsSocket = ref(null)
const obsConnected = ref(false)
const obsConnecting = ref(false)
const showObsDialog = ref(false)
const obsUrl = ref(localStorage.getItem('taffy_obs_url') || 'ws://localhost:4455')
const obsPassword = ref(sessionStorage.getItem('taffy_obs_pwd') || '')
let obsRequestId = 0
const obsPending = {}

// 纯 JS SHA-256 实现 —— 当浏览器非安全上下文 (HTTP 且非 localhost) 时 crypto.subtle 不可用
function sha256Pure(bytes) {
  const K = new Uint32Array([0x428a2f98,0x71374491,0xb5c0fbcf,0xe9b5dba5,0x3956c25b,0x59f111f1,0x923f82a4,0xab1c5ed5,0xd807aa98,0x12835b01,0x243185be,0x550c7dc3,0x72be5d74,0x80deb1fe,0x9bdc06a7,0xc19bf174,0xe49b69c1,0xefbe4786,0x0fc19dc6,0x240ca1cc,0x2de92c6f,0x4a7484aa,0x5cb0a9dc,0x76f988da,0x983e5152,0xa831c66d,0xb00327c8,0xbf597fc7,0xc6e00bf3,0xd5a79147,0x06ca6351,0x14292967,0x27b70a85,0x2e1b2138,0x4d2c6dfc,0x53380d13,0x650a7354,0x766a0abb,0x81c2c92e,0x92722c85,0xa2bfe8a1,0xa81a664b,0xc24b8b70,0xc76c51a3,0xd192e819,0xd6990624,0xf40e3585,0x106aa070,0x19a4c116,0x1e376c08,0x2748774c,0x34b0bcb5,0x391c0cb3,0x4ed8aa4a,0x5b9cca4f,0x682e6ff3,0x748f82ee,0x78a5636f,0x84c87814,0x8cc70208,0x90befffa,0xa4506ceb,0xbef9a3f7,0xc67178f2])
  const rrot = (x, n) => (x >>> n) | (x << (32 - n))
  const H = new Uint32Array([0x6a09e667,0xbb67ae85,0x3c6ef372,0xa54ff53a,0x510e527f,0x9b05688c,0x1f83d9ab,0x5be0cd19])

  const len = bytes.length, bitLen = len << 3, numBlocks = ((len + 9) >>> 6) + 1
  const padded = new Uint8Array(numBlocks << 6)
  padded.set(bytes); padded[len] = 0x80
  padded[padded.length - 1] = bitLen & 0xff; padded[padded.length - 2] = (bitLen >>> 8) & 0xff
  padded[padded.length - 3] = (bitLen >>> 16) & 0xff; padded[padded.length - 4] = (bitLen >>> 24) & 0xff

  const view = new DataView(padded.buffer), w = new Uint32Array(64)
  for (let blk = 0; blk < padded.length; blk += 64) {
    for (let t = 0; t < 16; t++) w[t] = view.getUint32(blk + (t << 2))
    for (let t = 16; t < 64; t++) {
      const s0 = rrot(w[t-15], 7) ^ rrot(w[t-15], 18) ^ (w[t-15] >>> 3)
      const s1 = rrot(w[t-2], 17) ^ rrot(w[t-2], 19) ^ (w[t-2] >>> 10)
      w[t] = (w[t-16] + s0 + w[t-7] + s1) >>> 0
    }
    let a = H[0], b = H[1], c = H[2], d = H[3], e = H[4], f = H[5], g = H[6], h = H[7]
    for (let t = 0; t < 64; t++) {
      const S1 = rrot(e, 6) ^ rrot(e, 11) ^ rrot(e, 25)
      const ch = (e & f) ^ (~e & g)
      const t1 = (h + S1 + ch + K[t] + w[t]) >>> 0
      const S0 = rrot(a, 2) ^ rrot(a, 13) ^ rrot(a, 22)
      const maj = (a & b) ^ (a & c) ^ (b & c)
      const t2 = (S0 + maj) >>> 0
      h = g; g = f; f = e; e = (d + t1) >>> 0
      d = c; c = b; b = a; a = (t1 + t2) >>> 0
    }
    H[0] = (H[0] + a) >>> 0; H[1] = (H[1] + b) >>> 0
    H[2] = (H[2] + c) >>> 0; H[3] = (H[3] + d) >>> 0
    H[4] = (H[4] + e) >>> 0; H[5] = (H[5] + f) >>> 0
    H[6] = (H[6] + g) >>> 0; H[7] = (H[7] + h) >>> 0
  }
  const out = new Uint8Array(32), ov = new DataView(out.buffer)
  for (let i = 0; i < 8; i++) ov.setUint32(i << 2, H[i])
  return out
}

/** 计算 SHA-256 哈希 — 优先使用 crypto.subtle (硬件加速)，不可用时回退到纯 JS 实现 */
async function sha256Digest(data) {
  if (crypto.subtle) {
    return new Uint8Array(await crypto.subtle.digest('SHA-256', data))
  }
  return sha256Pure(new Uint8Array(data))
}

/** OBS WebSocket 5.x 认证: auth = base64(sha256(base64(sha256(password+salt)) + challenge)) */
async function buildObsAuth(password, challenge, salt) {
  const encoder = new TextEncoder()

  // Step 1: sha256(password + salt) → base64
  const step1Bytes = encoder.encode(password + salt)
  const step1Hash = await sha256Digest(step1Bytes)
  const secret = btoa(String.fromCharCode(...step1Hash))

  // Step 2: sha256(secret + challenge) → base64
  const step2Bytes = encoder.encode(secret + challenge)
  const step2Hash = await sha256Digest(step2Bytes)
  const authString = btoa(String.fromCharCode(...step2Hash))

  return authString
}

function connectObs() {
  if (obsSocket.value && obsSocket.value.readyState === WebSocket.OPEN) {
    ElMessage.warning('OBS 已连接')
    return
  }

  obsConnecting.value = true

  let ws
  try {
    ws = new WebSocket(obsUrl.value)
  } catch (e) {
    obsConnecting.value = false
    ElMessage.error('创建 WebSocket 连接失败: ' + e.message)
    return
  }

  // 超时保护: 10 秒内未完成握手则视为失败
  const handshakeTimer = setTimeout(() => {
    if (!obsConnected.value && ws.readyState === WebSocket.OPEN) {
      ws.close()
      obsConnecting.value = false
      ElMessage.error('OBS 握手超时 — 请确认 OBS WebSocket 插件版本为 5.x')
    }
  }, 10000)

  ws.onopen = () => {
    console.log('[OBS] TCP 连接已建立，等待 Hello 握手...')
    // 不要在这里标记已连接！等待 Identified 消息
  }

  ws.onmessage = async (event) => {
    let msg
    try {
      msg = JSON.parse(event.data)
    } catch {
      console.warn('[OBS] 收到非 JSON 消息:', event.data)
      return
    }

    const op = msg.op
    console.log('[OBS] 收到消息 op:', op)

    if (op === 0) {
      // ===== Hello: 服务器发送握手参数 =====
      const hello = msg.d
      console.log('[OBS] Hello:', hello)

      const identify = {
        op: 1,
        d: {
          rpcVersion: hello.rpcVersion || 1,
          // 订阅 Outputs 事件以同步推流状态
          eventSubscriptions: 64  // 1<<6 = Outputs (StreamStateChanged 等)
        }
      }

      // 如果设置了密码且服务器要求认证
      if (obsPassword.value && hello.authentication) {
        try {
          // 保存设置，刷新页面后自动恢复
          localStorage.setItem('taffy_obs_url', obsUrl.value)
          sessionStorage.setItem('taffy_obs_pwd', obsPassword.value)
          identify.d.authentication = await buildObsAuth(
            obsPassword.value,
            hello.authentication.challenge,
            hello.authentication.salt
          )
          console.log('[OBS] 已计算认证哈希')
        } catch (e) {
          console.error('[OBS] 认证哈希计算失败:', e)
          clearTimeout(handshakeTimer)
          ws.close()
          obsConnecting.value = false
          ElMessage.error('OBS 认证计算失败')
          return
        }
      } else if (hello.authentication && !obsPassword.value) {
        console.log('[OBS] 服务器要求认证但未提供密码，尝试无密码连接')
      }

      console.log('[OBS] 发送 Identify')
      ws.send(JSON.stringify(identify))
    } else if (op === 2) {
      // ===== Identified: 握手成功 =====
      clearTimeout(handshakeTimer)
      console.log('[OBS] Identified:', msg.d)
      obsConnected.value = true
      obsConnecting.value = false
      showObsDialog.value = false
      const version = msg.d?.negotiatedRpcVersion || '?'
      ElMessage.success(`OBS 已连接 (RPC v${version})`)

      // 连接后立即查询 OBS 当前推流状态，同步到网站
      syncStreamStateFromObs()
    } else if (op === 1) {
      // ===== 服务器在我们发送 Identify 前发送了 Request（不应出现） =====
      console.warn('[OBS] 握手期间收到意外 Request:', msg.d)
    } else if (op === 7) {
      // ===== RequestResponse: 响应我们的命令 =====
      const requestId = msg.d?.requestId
      const pending = obsPending[requestId]
      if (pending) {
        if (msg.d?.requestStatus?.result) {
          pending.resolve(msg.d)
        } else {
          pending.reject(new Error(msg.d?.requestStatus?.comment || 'OBS 命令执行失败'))
        }
        delete obsPending[requestId]
      }
    } else if (op === 5) {
      // ===== Event: OBS 状态变更通知 =====
      handleObsEvent(msg.d)
    }
  }

  ws.onclose = (event) => {
    clearTimeout(handshakeTimer)
    console.log('[OBS] 连接关闭, code:', event.code, 'reason:', event.reason)
    const wasConnecting = obsConnecting.value
    obsConnected.value = false
    obsConnecting.value = false
    // 清理所有待处理的请求
    Object.keys(obsPending).forEach(id => {
      obsPending[id]?.reject(new Error('OBS 连接已断开'))
      delete obsPending[id]
    })
    if (wasConnecting) {
      // 握手期间断开 — 提供诊断信息
      if (event.code === 4009) {
        ElMessage.error('OBS 认证失败 — 请检查密码是否正确')
      } else {
        ElMessage.error('OBS 连接失败 — 请确认 OBS 已启动且 WebSocket 插件已启用 (端口 ' + (obsUrl.value || '4455') + ')')
      }
    } else if (streamActive.value) {
      // 直播中 OBS 断连 — 提示但保留直播会话
      ElMessage.warning('OBS 连接已断开，直播会话仍在记录中。可重新连接 OBS 恢复控制')
    }
    obsSocket.value = null
  }

  ws.onerror = () => {
    clearTimeout(handshakeTimer)
    console.warn('[OBS] WebSocket 错误')
    // onclose 会紧随 onerror 触发，由 onclose 统一处理错误提示
  }

  obsSocket.value = ws
}

function disconnectObs() {
  if (obsSocket.value) {
    obsSocket.value.close(1000, '用户主动断开')
    obsSocket.value = null
  }
  obsConnected.value = false
  obsConnecting.value = false
}

/** 向 OBS 发送命令 (Request, Op 6)，返回 Promise */
function sendObsCommand(requestType, requestData = {}) {
  return new Promise((resolve, reject) => {
    if (!obsSocket.value || obsSocket.value.readyState !== WebSocket.OPEN) {
      reject(new Error('OBS 未连接'))
      return
    }
    const requestId = String(++obsRequestId)
    obsPending[requestId] = { resolve, reject }
    obsSocket.value.send(JSON.stringify({
      op: 6,
      d: { requestType, requestId, requestData }
    }))
    // 10 秒超时
    setTimeout(() => {
      if (obsPending[requestId]) {
        obsPending[requestId].reject(new Error('OBS 命令超时'))
        delete obsPending[requestId]
      }
    }, 10000)
  })
}

/** 连接后同步 OBS 当前推流状态到网站 */
async function syncStreamStateFromObs() {
  try {
    const resp = await sendObsCommand('GetStreamStatus')
    const obsStreaming = resp?.responseData?.outputActive || false
    console.log('[OBS] 当前推流状态:', obsStreaming ? '推流中' : '未推流')

    if (obsStreaming && !streamActive.value) {
      // OBS 在推流，网站未记录 → 同步开始
      await syncStartFromObs()
    } else if (!obsStreaming && streamActive.value) {
      // OBS 未推流，网站显示推流中 → 同步停止
      await syncStopFromObs()
    }
  } catch (e) {
    console.warn('[OBS] 查询推流状态失败:', e.message)
  }
}

/** 处理 OBS 推送的事件 */
function handleObsEvent(eventData) {
  const eventType = eventData?.eventType
  console.log('[OBS] 事件:', eventType, eventData)

  if (eventType === 'StreamStateChanged') {
    const streaming = eventData?.eventData?.outputActive
    console.log('[OBS] 推流状态变更:', streaming ? '开始推流' : '停止推流')

    if (streaming && !streamActive.value) {
      // OBS 开始推流 → 网站同步开始
      syncStartFromObs()
    } else if (!streaming && streamActive.value) {
      // OBS 停止推流 → 网站同步停止
      syncStopFromObs()
    }
  }
}

/** OBS 开始推流时同步启动网站直播会话 */
async function syncStartFromObs() {
  ElMessage.info('检测到 OBS 开始推流，同步直播状态...')
  try {
    const session = await startSession({
      platform: selectedPlatform.value,
      scriptId: selectedScript.value
    })
    if (session?.id) {
      currentSessionId.value = session.id
      streamActive.value = true
      timerRunning.value = true
      resetTimerTick()
      ElMessage.success('已同步 OBS 推流状态 — 直播开始')
    }
  } catch {
    // session API 失败不阻止本地状态更新
    streamActive.value = true
    timerRunning.value = true
    resetTimerTick()
  }
}

/** OBS 停止推流时同步停止网站直播会话 */
async function syncStopFromObs() {
  ElMessage.info('检测到 OBS 停止推流，同步直播状态...')
  if (currentSessionId.value) {
    try {
      const statsData = JSON.stringify({
        ttsCount: ttsCount.value,
        duration: timerSeconds.value,
        platform: selectedPlatform.value
      })
      await endSession(currentSessionId.value, { statsData })
    } catch { /* 忽略 */ }
  }
  forceCleanupStream()
  ElMessage.success('已同步 OBS 推流状态 — 直播结束')
}

// ===== 直播状态 & 会话管理 =====
const streamActive = ref(false)
const streamStarting = ref(false)
const streamStopping = ref(false)
const currentSessionId = ref(null)

async function startStream() {
  // 检查 OBS 连接状态，未连接时弹确认框
  if (!obsConnected.value) {
    try {
      await ElMessageBox.confirm(
        '您尚未连接 OBS Studio，未连接 OBS 将无法真正推流到直播平台。<br/>确定要仅开始计时和会话记录吗？',
        '提示',
        { confirmButtonText: '仍然开始', cancelButtonText: '先去连接', type: 'warning',
          dangerouslyUseHTMLString: true }
      )
    } catch {
      // 用户点击了取消（"先去连接"），打开 OBS 对话框
      showObsDialog.value = true
      return
    }
  }

  streamStarting.value = true
  try {
    const session = await startSession({
      platform: selectedPlatform.value,
      scriptId: selectedScript.value
    })
    if (!session || !session.id) {
      ElMessage.error('创建直播会话失败，请重试')
      return
    }
    currentSessionId.value = session.id
    streamActive.value = true
    timerRunning.value = true
    resetTimerTick()

    // 如果 OBS 已连接，同步开始推流
    if (obsConnected.value) {
      try {
        await sendObsCommand('StartStream')
        ElMessage.success('直播已开始！OBS 推流已启动')
      } catch {
        ElMessage.success('直播已开始！（OBS 推流启动失败，请手动点击 OBS 开始推流）')
      }
    } else {
      ElMessage.success('直播已开始！')
    }
  } catch {
    // 错误已在 request 拦截器中提示
  } finally {
    streamStarting.value = false
  }
}

async function stopStream() {
  if (!currentSessionId.value) {
    // 防御：session ID 丢失时直接本地复位
    forceCleanupStream()
    return
  }

  streamStopping.value = true
  try {
    const statsData = JSON.stringify({
      ttsCount: ttsCount.value,
      duration: timerSeconds.value,
      platform: selectedPlatform.value
    })
    const result = await endSession(currentSessionId.value, { statsData })
    if (!result) {
      // 后端返回 null（session 不存在），仍然允许用户本地结束
      ElMessage.warning('直播会话在服务器上未找到，已本地结束')
    } else {
      ElMessage.success('直播已结束')
    }
  } catch {
    ElMessage.error('停止直播失败，请重试')
    // 保持 streamActive 状态，允许用户重试
    return
  } finally {
    streamStopping.value = false
  }

  // API 调用成功（或 session 不存在）时清理本地状态
  forceCleanupStream()

  // 如果 OBS 已连接，同步停止推流
  if (obsConnected.value) {
    try {
      await sendObsCommand('StopStream')
    } catch { /* OBS 命令失败不阻止本地状态更新 */ }
  }
}

/** 强制清理本地直播状态（不依赖后端 API） */
function forceCleanupStream() {
  streamActive.value = false
  timerRunning.value = false
  clearInterval(timerRef.value)
  currentSessionId.value = null
}

async function refreshScripts() { scripts.value = await getScriptList().catch(() => []) || [] }

// 页面关闭/刷新时尝试结束直播会话（使用 keepalive fetch）
function handleBeforeUnload() {
  if (!streamActive.value || !currentSessionId.value) return
  const token = localStorage.getItem('taffy_token')
  const statsData = JSON.stringify({
    ttsCount: ttsCount.value,
    duration: timerSeconds.value,
    platform: selectedPlatform.value
  })
  fetch(`/api/stats/sessions/${currentSessionId.value}/end`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json', 'Authorization': token ? `Bearer ${token}` : '' },
    body: JSON.stringify({ statsData }),
    keepalive: true
  })
}

onMounted(async () => {
  window.addEventListener('beforeunload', handleBeforeUnload)
  voices.value = await getVoiceList().catch(() => []) || []
  await refreshScripts()
  startDanmakuPoll()
  startSSE()
})

onBeforeUnmount(() => {
  window.removeEventListener('beforeunload', handleBeforeUnload)
  // 尝试在路由导航离开时结束会话
  if (streamActive.value && currentSessionId.value) {
    const statsData = JSON.stringify({
      ttsCount: ttsCount.value,
      duration: timerSeconds.value,
      platform: selectedPlatform.value
    })
    // 使用 keepalive fetch 确保请求发出
    const token = localStorage.getItem('taffy_token')
    fetch(`/api/stats/sessions/${currentSessionId.value}/end`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json', 'Authorization': token ? `Bearer ${token}` : '' },
      body: JSON.stringify({ statsData }),
      keepalive: true
    })
  }
  forceCleanupStream()
  stopAutoPlay()
  stopDanmakuPoll()
  
  
  obsSocket.value?.close()
  if (eventSource) eventSource.close()
})
</script>

<style scoped>
.live-topbar { display:flex; align-items:center; gap:20px; padding:12px 20px; background:var(--card); border-radius:var(--radius); border:1px solid var(--border); box-shadow:var(--shadow); backdrop-filter:var(--backdrop); margin-bottom:16px; flex-wrap:wrap; }
.live-stat { display:flex; align-items:center; gap:6px; font-size:13px; color:var(--muted); }
.live-stat.active { color:var(--danger); font-weight:600; }
.live-stat.connected { color:var(--success); }
.live-dot { width:8px; height:8px; border-radius:50%; background:var(--danger); animation:pulse 1.5s infinite; }
@keyframes pulse { 0%,100%{opacity:1} 50%{opacity:.3} }
.phrase-tabs :deep(.el-tabs__header) { margin-bottom:8px; }
.phrase-grid { display:flex; flex-wrap:wrap; gap:6px; }
.phrase-btn { white-space:normal; text-align:left; height:auto; min-height:32px; }
.segment-list { max-height:300px; overflow-y:auto; }
.segment-item { display:flex; align-items:center; gap:8px; padding:6px 8px; border-radius:6px; cursor:pointer; font-size:13px; transition:background .15s; }
.segment-item:hover { background:var(--surface-soft); }
.segment-item.current { background:var(--primary-soft); color:var(--primary); }
.segment-item.played { color:color-mix(in srgb,var(--muted) 58%,transparent); }
.seg-num { width:22px; text-align:center; font-weight:bold; flex-shrink:0; }
.seg-text { flex:1; min-width:0; overflow:hidden; text-overflow:ellipsis; white-space:nowrap; }
.queue-list { max-height:180px; overflow-y:auto; }
.queue-row { display:flex; gap:6px; padding:2px 0; font-size:12px; color:var(--muted); }
.queue-row.now { color:var(--primary); font-weight:500; }
.queue-row.done { color:color-mix(in srgb,var(--muted) 58%,transparent); text-decoration:line-through; }
.danmaku-box { max-height:200px; overflow-y:auto; background:var(--surface-soft); border-radius:8px; padding:6px 10px; }
.danmaku-item { display:flex; gap:8px; padding:4px 0; font-size:13px; border-bottom:1px solid var(--border); align-items:baseline; }
.danmaku-item:last-child { border-bottom:none; }
.dm-user { color:var(--primary); font-weight:600; white-space:nowrap; }
.dm-time { color:color-mix(in srgb,var(--muted) 58%,transparent); font-size:11px; margin-left:auto; white-space:nowrap; }
.dm-gift { color:var(--warning); }
.dm-gift .dm-user { color:var(--warning); }
.sound-remove { position:absolute; top:-6px; right:-6px; width:18px; height:18px; border-radius:50%; background:var(--danger); color:#fff; font-size:12px; line-height:18px; text-align:center; cursor:pointer; }
.platform-bar { margin-bottom:16px; }
.platform-guide h4 { margin:0 0 8px; font-size:15px; }
</style>
