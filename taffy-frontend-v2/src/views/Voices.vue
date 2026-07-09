<template>
  <div class="page page-narrow">
    <PageHeader title="声音管理" subtitle="上传音频、提交训练、查看声音模型状态。">
      <el-button type="primary" @click="openUpload">上传声音</el-button>
      <el-button @click="load">刷新</el-button>
    </PageHeader>

    <div class="card">
      <el-table :data="pagedVoices" v-loading="loading" empty-text="暂无声音模型">
        <el-table-column type="index" width="50" label="#" />
        <el-table-column label="名称" min-width="150">
          <template #default="{ row }"><strong>{{ row.name }}</strong><div class="muted">{{ row.description || '无描述' }}</div></template>
        </el-table-column>
        <el-table-column label="状态" width="130"><template #default="{ row }"><StatusTag :value="row.status" /></template></el-table-column>
        <el-table-column prop="audioFilePath" label="音频路径" min-width="180" show-overflow-tooltip />
        <el-table-column prop="createdAt" label="创建时间" min-width="170" />
        <el-table-column label="操作" width="340" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="playAudio(row)" :type="playingId === row.id ? 'warning' : ''">
              {{ playingId === row.id ? '停止' : '播放' }}
            </el-button>
            <el-button size="small" @click="openEdit(row)">编辑</el-button>
            <el-button size="small" type="primary" :loading="trainingIds.includes(row.id)" @click="train(row)">训练</el-button>
            <el-button size="small" type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
        <!-- 隐藏的音频元素 -->
        <audio ref="audioRef" style="display:none" @ended="onAudioEnded" @error="onAudioError"></audio>
      </el-table>
      <EmptyState v-if="!loading && voices.length === 0" description="还没有声音模型，先上传一段音频吧。">
        <el-button type="primary" @click="openUpload">上传声音</el-button>
      </EmptyState>
    </div>
    <div v-if="voices.length > pageSize" style="display:flex;justify-content:center;margin-top:16px">
      <el-pagination v-model:current-page="currentPage" :page-size="pageSize" layout="prev, pager, next, total" :total="voices.length" />
    </div>

    <el-dialog v-model="uploadDialog" title="上传声音样本" width="560px" destroy-on-close>
      <el-form ref="uploadRef" :model="uploadForm" :rules="uploadRules" label-width="92px">
        <el-form-item label="模型名称" prop="name"><el-input v-model.trim="uploadForm.name" placeholder="例如：主播声音A" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="uploadForm.description" type="textarea" :rows="3" placeholder="说明使用场景，可选" /></el-form-item>
        <el-form-item label="音频文件" prop="file">
          <el-upload drag :auto-upload="false" :limit="1" :on-change="onFileChange" :on-remove="onFileRemove" accept=".wav,.mp3,.m4a">
            <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
            <div class="el-upload__text">拖拽文件到这里，或 <em>点击选择</em></div>
            <template #tip><div class="el-upload__tip">支持 .wav / .mp3 / .m4a，上传后会自动提交训练。</div></template>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="uploadDialog=false">取消</el-button>
        <el-button type="primary" :loading="uploading" @click="submitUpload">上传并训练</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="editDialog" title="编辑声音模型" width="520px">
      <el-form :model="editForm" label-width="92px">
        <el-form-item label="名称"><el-input v-model.trim="editForm.name" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="editForm.description" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="状态"><el-select v-model="editForm.status" class="full"><el-option label="训练中" value="训练中"/><el-option label="就绪" value="就绪"/><el-option label="失败" value="失败"/></el-select></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialog=false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveEdit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageHeader from '../components/PageHeader.vue'
import EmptyState from '../components/EmptyState.vue'
import StatusTag from '../components/StatusTag.vue'
import { deleteVoice, getTrainStatus, getVoiceList, startVoiceTrain, updateVoice, uploadAudio } from '../api/voice'

const voices = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(8)
const pagedVoices = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return voices.value.slice(start, start + pageSize.value)
})
const uploadDialog = ref(false)
const editDialog = ref(false)
const uploading = ref(false)
const saving = ref(false)
const trainingIds = ref([])
const audioRef = ref(null)
const playingId = ref(null)
const uploadRef = ref()
const uploadForm = reactive({ name: '', description: '', file: null })
const editForm = reactive({ id: null, name: '', description: '', status: '' })
const timers = new Map()
const uploadRules = {
  name: [{ required: true, message: '请输入模型名称', trigger: 'blur' }],
  file: [{ required: true, message: '请选择音频文件', trigger: 'change' }]
}

function openUpload() {
  Object.assign(uploadForm, { name: '', description: '', file: null })
  uploadDialog.value = true
}
function onFileChange(file) { uploadForm.file = file.raw }
function onFileRemove() { uploadForm.file = null }
async function load() {
  loading.value = true
  try { voices.value = await getVoiceList() || [] }
  finally { loading.value = false }
}
async function submitUpload() {
  await uploadRef.value.validate()
  const fd = new FormData()
  fd.append('file', uploadForm.file)
  fd.append('name', uploadForm.name)
  fd.append('description', uploadForm.description || '')
  uploading.value = true
  try {
    const voice = await uploadAudio(fd)
    uploadDialog.value = false
    ElMessage.success('上传成功，正在提交训练')
    if (voice?.id) await train(voice)
    await load()
  } finally { uploading.value = false }
}
async function train(row) {
  if (!trainingIds.value.includes(row.id)) trainingIds.value.push(row.id)
  try {
    const res = await startVoiceTrain(row.id)
    const taskId = res?.taskId || res?.voiceModelId || row.id
    ElMessage.success('训练任务已提交')
    pollTraining(row.id, taskId)
  } catch {
    trainingIds.value = trainingIds.value.filter(id => id !== row.id)
  }
}
function pollTraining(voiceId, taskId) {
  clearInterval(timers.get(voiceId))
  const timer = setInterval(async () => {
    try {
      const data = await getTrainStatus(taskId)
      const status = data?.status
      const item = voices.value.find(v => v.id === voiceId)
      if (item && status) item.status = status
      if (['就绪', '失败', 'completed', 'failed', 'success'].includes(status)) {
        clearInterval(timer)
        timers.delete(voiceId)
        trainingIds.value = trainingIds.value.filter(id => id !== voiceId)
        await load()
      }
    } catch {
      clearInterval(timer)
      timers.delete(voiceId)
      trainingIds.value = trainingIds.value.filter(id => id !== voiceId)
    }
  }, 3000)
  timers.set(voiceId, timer)
}
function openEdit(row) { Object.assign(editForm, row); editDialog.value = true }
async function saveEdit() {
  saving.value = true
  try {
    await updateVoice(editForm.id, { name: editForm.name, description: editForm.description, status: editForm.status })
    ElMessage.success('保存成功')
    editDialog.value = false
    await load()
  } finally { saving.value = false }
}
async function remove(row) {
  await ElMessageBox.confirm(`确认删除声音模型「${row.name}」？`, '删除确认', { type: 'warning' })
  await deleteVoice(row.id)
  ElMessage.success('删除成功')
  await load()
}
function playAudio(row) {
  if (playingId.value === row.id) {
    audioRef.value?.pause()
    playingId.value = null
    return
  }
  playingId.value = row.id
  const url = `/api/audio/file/${row.id}`
  if (audioRef.value) {
    audioRef.value.src = url
    audioRef.value.play().catch(() => ElMessage.warning('无法播放该音频文件'))
  }
}
function onAudioEnded() { playingId.value = null }
function onAudioError() { playingId.value = null; ElMessage.warning('音频文件不可用') }
onMounted(load)
onBeforeUnmount(() => { timers.forEach(timer => clearInterval(timer)); audioRef.value?.pause() })
</script>
