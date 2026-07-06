<template>
  <div class="voices-page">
    <div class="page-header">
      <h2 class="page-title">声音管理</h2>
      <el-button type="primary" @click="showUploadDialog = true">
        <el-icon><Plus /></el-icon> 上传音频
      </el-button>
    </div>

    <!-- 声音列表 -->
    <el-card>
      <el-table :data="voiceList" style="width: 100%" v-loading="loading">
        <el-table-column prop="name" label="名称" min-width="150" />
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleTrain(row)">训练</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && voiceList.length === 0" description="暂无声音模型，请上传音频" />
    </el-card>

    <!-- 上传音频对话框 -->
    <el-dialog v-model="showUploadDialog" title="上传音频文件" width="500px" destroy-on-close>
      <el-form :model="uploadForm" label-width="80px">
        <el-form-item label="名称" required>
          <el-input v-model="uploadForm.name" placeholder="为声音模型命名" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="uploadForm.description" type="textarea" placeholder="描述这个声音的特点" />
        </el-form-item>
        <el-form-item label="音频文件" required>
          <el-upload
            ref="uploadRef"
            :auto-upload="false"
            :limit="1"
            accept=".wav,.mp3,.m4a"
            :on-change="handleFileChange"
            :file-list="fileList"
          >
            <el-button type="primary">选择音频文件</el-button>
            <template #tip>
              <div class="el-upload__tip">支持 WAV、MP3、M4A 格式，建议时长 10-60 秒</div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showUploadDialog = false">取消</el-button>
        <el-button type="primary" :loading="uploading" @click="handleUpload">上传并训练</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getVoiceList, deleteVoice, uploadAudio, startVoiceTrain } from '@/api/voice'

const loading = ref(false)
const voiceList = ref([])
const showUploadDialog = ref(false)
const uploading = ref(false)
const fileList = ref([])
const selectedFile = ref(null)

const uploadForm = ref({
  name: '',
  description: ''
})

const statusType = (status) => {
  const map = { '就绪': 'success', '训练中': 'warning', '失败': 'danger' }
  return map[status] || 'info'
}

const handleFileChange = (file) => {
  selectedFile.value = file.raw
}

const handleUpload = async () => {
  if (!uploadForm.value.name || !selectedFile.value) {
    ElMessage.warning('请填写名称并选择音频文件')
    return
  }
  uploading.value = true
  try {
    const formData = new FormData()
    formData.append('file', selectedFile.value)
    formData.append('name', uploadForm.value.name)
    formData.append('description', uploadForm.value.description)
    const res = await uploadAudio(formData)
    // 上传成功后开始训练
    await startVoiceTrain({ voiceModelId: res.data.id })
    ElMessage.success('上传成功，开始训练')
    showUploadDialog.value = false
    uploadForm.value = { name: '', description: '' }
    fileList.value = []
    fetchVoices()
  } catch (error) {
    ElMessage.error('上传失败')
  } finally {
    uploading.value = false
  }
}

const handleTrain = async (row) => {
  try {
    await startVoiceTrain({ voiceModelId: row.id })
    ElMessage.success('开始训练')
    fetchVoices()
  } catch (error) {
    ElMessage.error('训练失败')
  }
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm('确定要删除该声音模型吗？', '确认删除', {
    type: 'warning'
  })
  try {
    await deleteVoice(row.id)
    ElMessage.success('删除成功')
    fetchVoices()
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

const fetchVoices = async () => {
  loading.value = true
  try {
    const res = await getVoiceList()
    voiceList.value = res.data || []
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchVoices()
})
</script>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-title {
  font-size: 22px;
  color: #333;
}
</style>