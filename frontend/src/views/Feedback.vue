<template>
  <div class="feedback-page">
    <h2 class="page-title">声音评价</h2>

    <el-row :gutter="20">
      <el-col :span="10">
        <el-card>
          <template #header><span>提交评价</span></template>
          <el-form :model="feedbackForm" label-width="80px">
            <el-form-item label="声音模型">
              <el-select v-model="feedbackForm.voiceModelId" placeholder="选择声音模型" style="width: 100%">
                <el-option v-for="v in voices" :key="v.id" :label="v.name" :value="v.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="评分">
              <el-rate v-model="feedbackForm.rating" :max="5" show-score />
            </el-form-item>
            <el-form-item label="评价内容">
              <el-input v-model="feedbackForm.comment" type="textarea" :rows="4" placeholder="分享你的使用感受..." />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="submitting" @click="handleSubmit" style="width: 100%">
                提交评价
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <el-col :span="14">
        <el-card>
          <template #header><span>评价列表</span></template>
          <div class="feedback-list">
            <div v-for="item in feedbackList" :key="item.id" class="feedback-item">
              <div class="feedback-header">
                <span class="feedback-voice">{{ item.voiceName || '声音模型' }}</span>
                <el-rate :model-value="item.rating" :max="5" disabled show-score size="small" />
              </div>
              <div class="feedback-comment">{{ item.comment }}</div>
              <div class="feedback-time">{{ item.createdAt }}</div>
            </div>
            <el-empty v-if="feedbackList.length === 0" description="暂无评价" :image-size="60" />
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getFeedbackList, submitFeedback } from '@/api/feedback'
import { getVoiceList } from '@/api/voice'

const voices = ref([])
const feedbackList = ref([])
const submitting = ref(false)

const feedbackForm = reactive({
  voiceModelId: '',
  rating: 0,
  comment: ''
})

const handleSubmit = async () => {
  if (!feedbackForm.voiceModelId || feedbackForm.rating === 0) {
    ElMessage.warning('请选择声音模型并评分')
    return
  }
  submitting.value = true
  try {
    await submitFeedback(feedbackForm)
    ElMessage.success('评价提交成功')
    feedbackForm.voiceModelId = ''
    feedbackForm.rating = 0
    feedbackForm.comment = ''
    fetchFeedbackList()
  } catch {
    ElMessage.error('提交失败')
  } finally {
    submitting.value = false
  }
}

const fetchFeedbackList = async () => {
  try {
    const res = await getFeedbackList()
    feedbackList.value = res.data || []
  } catch {}
}

onMounted(async () => {
  try {
    const res = await getVoiceList()
    voices.value = (res.data || []).filter(v => v.status === '就绪')
  } catch {}
  fetchFeedbackList()
})
</script>

<style scoped>
.page-title { margin-bottom: 20px; font-size: 22px; color: #333; }
.feedback-list { max-height: 450px; overflow-y: auto; }
.feedback-item {
  padding: 12px 8px;
  border-bottom: 1px solid #eee;
}
.feedback-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}
.feedback-voice { font-size: 14px; font-weight: 600; color: #333; }
.feedback-comment { font-size: 13px; color: #666; line-height: 1.6; }
.feedback-time { font-size: 12px; color: #999; margin-top: 6px; }
</style>