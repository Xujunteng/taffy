<template>
  <div class="page page-narrow">
    <PageHeader title="声音评价" subtitle="对声音模型提交评分与评论，并查看历史评价。">
      <el-button @click="loadAll">刷新</el-button>
    </PageHeader>
    <div class="grid grid-2">
      <div class="card">
        <h3 class="panel-title">提交评价</h3>
        <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
          <el-form-item label="声音模型" prop="voiceModelId">
            <el-select class="full" v-model="form.voiceModelId" filterable placeholder="请选择声音模型" @change="loadRating">
              <el-option v-for="v in voices" :key="v.id" :label="v.name" :value="v.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="评分" prop="rating"><el-rate v-model="form.rating" show-text /></el-form-item>
          <el-form-item label="评价内容"><el-input v-model="form.comment" type="textarea" :rows="6" maxlength="500" show-word-limit placeholder="例如：音色自然、语速合适，适合直播欢迎语。" /></el-form-item>
          <el-form-item label="公开姓名">
            <el-switch v-model="form.showName" active-text="显示姓名" inactive-text="匿名发布" />
          </el-form-item>
          <el-button type="primary" :loading="submitting" @click="submit">提交评价</el-button>
        </el-form>
        <el-divider />
        <div v-if="ratingInfo" class="grid grid-2">
          <div class="card" style="box-shadow:none;background:var(--primary-soft)"><div class="stat-label">平均评分</div><div class="stat-number">{{ Number(ratingInfo.avgRating || 0).toFixed(1) }}</div></div>
          <div class="card" style="box-shadow:none;background:var(--success-soft)"><div class="stat-label">评价数量</div><div class="stat-number">{{ ratingInfo.count || 0 }}</div></div>
        </div>
      </div>
      <div class="card">
        <h3 class="panel-title">历史评价</h3>
        <el-select v-model="filterVoiceId" clearable class="full" placeholder="按声音模型筛选" style="margin-bottom:12px" @change="loadFeedback">
          <el-option v-for="v in voices" :key="v.id" :label="v.name" :value="v.id" />
        </el-select>
        <div v-loading="loading">
          <div v-for="item in pagedFeedbacks" :key="item.id" class="list-item">
            <div class="space-between"><el-rate :model-value="item.rating" disabled /><span class="muted">{{ item.createdAt }}</span></div>
            <p style="margin:8px 0 0">{{ item.comment || '无文字评价' }}</p>
            <div class="space-between" style="margin-top:6px">
              <span class="muted">— {{ item.username || '匿名用户' }}</span>
              <el-tag v-if="item.modelName" type="primary" size="small" style="cursor:pointer" @click="openModelDialog(item)">{{ item.modelName }}</el-tag>
            </div>
          </div>
          <EmptyState v-if="!loading && feedbacks.length===0" description="暂无评价记录" />
        </div>
        <div v-if="feedbacks.length > pageSize" style="display:flex;justify-content:center;margin-top:12px">
          <el-pagination v-model:current-page="fbPage" :page-size="pageSize" layout="prev, pager, next, total" :total="feedbacks.length" />
        </div>
      </div>
    </div>

    <el-dialog v-model="modelDialog" title="声音模型" width="420px" @close="adoptedId = null">
      <div v-if="selectedModel">
        <p><strong>模型名称：</strong>{{ selectedModel.modelName }}</p>
        <p><strong>评价人：</strong>{{ selectedModel.username || '匿名用户' }}</p>
        <p><strong>评分：</strong><el-rate :model-value="selectedModel.rating" disabled style="display:inline-block;vertical-align:middle" /></p>
        <p><strong>评价：</strong>{{ selectedModel.comment || '无' }}</p>
        <el-alert v-if="adoptedId" title="已添加到你的声音管理" type="success" :closable="false" style="margin-top:12px" />
      </div>
      <template #footer>
        <el-button @click="modelDialog = false">关闭</el-button>
        <el-button v-if="!adoptedId" type="primary" :loading="adopting" @click="doAdopt(selectedModel)">添加到我的声音管理</el-button>
        <el-button v-else type="primary" @click="goTTS(adoptedId)">去 TTS 使用此模型</el-button>
      </template>
    </el-dialog>
  </div>
</template>
<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import PageHeader from '../components/PageHeader.vue'
import EmptyState from '../components/EmptyState.vue'
import { adoptVoice, getVoiceList } from '../api/voice'
import { createFeedback, getFeedbackList, getVoiceRating } from '../api/feedback'

const router = useRouter()
const voices = ref([])
const feedbacks = ref([])
const loading = ref(false)
const submitting = ref(false)
const ratingInfo = ref(null)
const filterVoiceId = ref(null)
const formRef = ref()
const modelDialog = ref(false)
const selectedModel = ref(null)
const fbPage = ref(1)
const pageSize = ref(8)
const pagedFeedbacks = computed(() => {
  const start = (fbPage.value - 1) * pageSize.value
  return feedbacks.value.slice(start, start + pageSize.value)
})
const adopting = ref(false)
const adoptedId = ref(null)
const form = reactive({ voiceModelId: null, rating: 5, comment: '', showName: true })
const rules = { voiceModelId: [{ required: true, message: '请选择声音模型', trigger: 'change' }], rating: [{ required: true, message: '请选择评分', trigger: 'change' }] }
async function loadAll() { voices.value = await getVoiceList().catch(() => []) || []; await loadFeedback() }
async function loadFeedback() { loading.value = true; try { feedbacks.value = await getFeedbackList(filterVoiceId.value ? { voiceModelId: filterVoiceId.value } : undefined).catch(() => []) || [] } finally { loading.value = false } }
async function loadRating(id) { ratingInfo.value = id ? await getVoiceRating(id).catch(() => null) : null }
async function submit() {
  await formRef.value.validate()
  submitting.value = true
  try {
    await createFeedback({ voiceModelId: form.voiceModelId, rating: form.rating, comment: form.comment, showName: form.showName })
    ElMessage.success('评价提交成功')
    form.comment = ''
    await loadRating(form.voiceModelId)
    await loadFeedback()
  } finally { submitting.value = false }
}
function openModelDialog(item) {
  selectedModel.value = item
  adoptedId.value = null
  modelDialog.value = true
}
async function doAdopt(item) {
  adopting.value = true
  try {
    const copy = await adoptVoice(item.voiceModelId)
    adoptedId.value = copy?.id
    ElMessage.success('已添加到你的声音管理')
  } catch { /* error handled by interceptor */ }
  finally { adopting.value = false }
}
function goTTS(newId) {
  modelDialog.value = false
  router.push({ path: '/tts', query: { voiceModelId: newId } })
}
onMounted(loadAll)
</script>
