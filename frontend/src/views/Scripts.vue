<template>
  <div class="scripts-page">
    <div class="page-header">
      <h2 class="page-title">脚本编辑</h2>
      <el-button type="primary" @click="handleCreate">
        <el-icon><Plus /></el-icon> 新建脚本
      </el-button>
    </div>

    <el-row :gutter="20">
      <el-col :span="8">
        <el-card>
          <template #header><span>脚本列表</span></template>
          <div class="script-list">
            <div
              v-for="script in scriptList"
              :key="script.id"
              class="script-item"
              :class="{ active: currentScript?.id === script.id }"
              @click="selectScript(script)"
            >
              <div class="script-item-title">{{ script.title }}</div>
              <div class="script-item-meta">{{ script.category }} | {{ script.updatedAt }}</div>
            </div>
            <el-empty v-if="scriptList.length === 0" description="暂无脚本" :image-size="60" />
          </div>
        </el-card>
      </el-col>

      <el-col :span="16">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>{{ editing ? '编辑脚本' : '脚本内容' }}</span>
              <div v-if="currentScript">
                <el-button type="primary" link @click="startEdit">编辑</el-button>
                <el-button type="danger" link @click="handleDelete">删除</el-button>
              </div>
            </div>
          </template>

          <div v-if="editing || !currentScript">
            <el-form :model="scriptForm" label-width="80px">
              <el-form-item label="标题">
                <el-input v-model="scriptForm.title" placeholder="脚本标题" />
              </el-form-item>
              <el-form-item label="分类">
                <el-select v-model="scriptForm.category" placeholder="选择分类">
                  <el-option label="开场白" value="开场白" />
                  <el-option label="产品介绍" value="产品介绍" />
                  <el-option label="互动话术" value="互动话术" />
                  <el-option label="结束语" value="结束语" />
                  <el-option label="其他" value="其他" />
                </el-select>
              </el-form-item>
              <el-form-item label="内容">
                <el-input
                  v-model="scriptForm.content"
                  type="textarea"
                  :rows="12"
                  placeholder="编写直播脚本内容..."
                />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
                <el-button @click="cancelEdit">取消</el-button>
              </el-form-item>
            </el-form>
          </div>

          <div v-else-if="currentScript" class="script-view">
            <div class="script-meta">
              <el-tag>{{ currentScript.category }}</el-tag>
              <span class="meta-time">更新于 {{ currentScript.updatedAt }}</span>
            </div>
            <div class="script-content">{{ currentScript.content }}</div>
          </div>

          <el-empty v-if="!currentScript && !editing" description="请选择或创建脚本" :image-size="60" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getScriptList, getScriptDetail, createScript, updateScript, deleteScript } from '@/api/scripts'

const scriptList = ref([])
const currentScript = ref(null)
const editing = ref(false)
const saving = ref(false)

const scriptForm = reactive({
  title: '',
  category: '',
  content: ''
})

const selectScript = async (script) => {
  try {
    const res = await getScriptDetail(script.id)
    currentScript.value = res.data
    editing.value = false
  } catch {}
}

const startEdit = () => {
  if (currentScript.value) {
    Object.assign(scriptForm, currentScript.value)
    editing.value = true
  }
}

const handleCreate = () => {
  currentScript.value = null
  editing.value = true
  scriptForm.title = ''
  scriptForm.category = ''
  scriptForm.content = ''
}

const cancelEdit = () => {
  editing.value = false
  if (!currentScript.value) {
    scriptForm.title = ''
    scriptForm.category = ''
    scriptForm.content = ''
  }
}

const handleSave = async () => {
  if (!scriptForm.title || !scriptForm.content) {
    ElMessage.warning('请填写标题和内容')
    return
  }
  saving.value = true
  try {
    if (currentScript.value && !editing.value === false) {
      await updateScript(currentScript.value.id, scriptForm)
      ElMessage.success('更新成功')
    } else {
      await createScript(scriptForm)
      ElMessage.success('创建成功')
    }
    editing.value = false
    fetchScripts()
  } catch {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

const handleDelete = async () => {
  if (!currentScript.value) return
  await ElMessageBox.confirm('确定删除该脚本吗？', '确认删除', { type: 'warning' })
  try {
    await deleteScript(currentScript.value.id)
    ElMessage.success('删除成功')
    currentScript.value = null
    fetchScripts()
  } catch {}
}

const fetchScripts = async () => {
  try {
    const res = await getScriptList()
    scriptList.value = res.data || []
  } catch {}
}

onMounted(() => {
  fetchScripts()
})
</script>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.page-title { font-size: 22px; color: #333; }
.script-list { max-height: 450px; overflow-y: auto; }
.script-item {
  padding: 12px 8px;
  border-bottom: 1px solid #eee;
  cursor: pointer;
  transition: background 0.2s;
}
.script-item:hover, .script-item.active { background: #ecf5ff; }
.script-item-title { font-size: 14px; font-weight: 600; color: #333; }
.script-item-meta { font-size: 12px; color: #999; margin-top: 4px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.script-view { padding: 8px 0; }
.script-meta { margin-bottom: 16px; display: flex; align-items: center; gap: 12px; }
.meta-time { font-size: 12px; color: #999; }
.script-content { white-space: pre-wrap; line-height: 1.8; color: #333; font-size: 14px; }
</style>