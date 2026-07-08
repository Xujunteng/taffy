<template>
  <div class="page page-narrow">
    <PageHeader title="脚本编辑" subtitle="维护直播话术脚本，可在直播面板中快速调用。"><el-button type="primary" @click="newScript">新建脚本</el-button></PageHeader>
    <div class="toolbar"><el-input v-model="keyword" style="width:260px" placeholder="本地筛选标题/内容" clearable /><el-select v-model="category" style="width:180px" clearable placeholder="分类筛选" @change="load"><el-option v-for="c in categories" :key="c" :label="c" :value="c" /></el-select><el-button @click="load">刷新</el-button></div>
    <div class="grid grid-2"><div class="card"><h3 class="panel-title">脚本列表</h3><el-table :data="filtered" v-loading="loading" highlight-current-row @row-click="selectScript" empty-text="暂无脚本"><el-table-column prop="title" label="标题" min-width="160" /><el-table-column prop="category" label="分类" width="110" /><el-table-column label="操作" width="96"><template #default="{ row }"><el-button link type="danger" @click.stop="remove(row)">删除</el-button></template></el-table-column></el-table><EmptyState v-if="!loading && filtered.length===0" description="暂无脚本，点击右上角新建。" /></div><div class="card"><h3 class="panel-title">{{ form.id ? '编辑脚本' : '新建脚本' }}</h3><el-form ref="formRef" :model="form" :rules="rules" label-position="top"><el-form-item label="标题" prop="title"><el-input v-model.trim="form.title" /></el-form-item><el-form-item label="分类"><el-input v-model.trim="form.category" placeholder="例如：欢迎、带货、互动" /></el-form-item><el-form-item label="内容" prop="content"><el-input v-model="form.content" class="editor-textarea" type="textarea" :rows="15" maxlength="2000" show-word-limit /></el-form-item><el-space><el-button type="primary" :loading="saving" @click="save">保存</el-button><el-button @click="newScript">重置</el-button><el-button @click="copyContent">复制内容</el-button></el-space></el-form></div></div>
  </div>
</template>
<script setup>
import { computed, onMounted, reactive, ref } from 'vue'; import { ElMessage, ElMessageBox } from 'element-plus'; import PageHeader from '../components/PageHeader.vue'; import EmptyState from '../components/EmptyState.vue'; import { createScript, deleteScript, getScriptList, updateScript } from '../api/scripts'; import { asArray } from '../utils/download'
const list = ref([]), loading = ref(false), saving = ref(false), keyword = ref(''), category = ref(''), formRef = ref()
const form = reactive({ id: null, title: '', category: '', content: '' })
const categories = computed(() => Array.from(new Set(list.value.map(i => i.category).filter(Boolean))))
const filtered = computed(() => { const k = keyword.value.trim().toLowerCase(); if (!k) return list.value; return list.value.filter(i => `${i.title} ${i.content} ${i.category}`.toLowerCase().includes(k)) })
const rules = { title: [{ required: true, message: '请输入标题', trigger: 'blur' }], content: [{ required: true, message: '请输入内容', trigger: 'blur' }] }
function newScript() { Object.assign(form, { id: null, title: '', category: '', content: '' }) }
function selectScript(row) { Object.assign(form, row) }
async function load() { loading.value = true; try { list.value = asArray(await getScriptList(category.value ? { category: category.value } : undefined)) } finally { loading.value = false } }
async function save() { await formRef.value.validate(); saving.value = true; try { const data = { title: form.title, category: form.category, content: form.content }; form.id ? await updateScript(form.id, data) : await createScript(data); ElMessage.success('保存成功'); newScript(); await load() } finally { saving.value = false } }
async function remove(row) { await ElMessageBox.confirm(`确认删除脚本「${row.title}」？`, '删除确认', { type: 'warning' }); await deleteScript(row.id); ElMessage.success('删除成功'); if (form.id === row.id) newScript(); await load() }
async function copyContent() { await navigator.clipboard.writeText(form.content || ''); ElMessage.success('内容已复制') }
onMounted(load)
</script>
