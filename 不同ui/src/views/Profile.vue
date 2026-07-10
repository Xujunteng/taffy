<template>
  <div class="page page-narrow">
    <PageHeader title="个人中心" subtitle="查看账户信息和修改密码。" />

    <div class="grid grid-2">
      <div class="card">
        <h3 class="panel-title">账户信息</h3>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="用户 ID">{{ user.id || '-' }}</el-descriptions-item>
          <el-descriptions-item label="用户名">{{ user.username || '-' }}</el-descriptions-item>
          <el-descriptions-item label="邮箱">{{ user.email || '未设置' }}</el-descriptions-item>
          <el-descriptions-item label="角色">{{ user.role || 'user' }}</el-descriptions-item>
          <el-descriptions-item label="注册时间">{{ user.createdAt || '-' }}</el-descriptions-item>
        </el-descriptions>
      </div>

      <div class="card">
        <h3 class="panel-title">修改密码</h3>
        <el-form ref="pwdRef" :model="pwdForm" :rules="pwdRules" label-position="top">
          <el-form-item label="原密码" prop="oldPassword">
            <el-input v-model="pwdForm.oldPassword" type="password" show-password placeholder="请输入原密码" />
          </el-form-item>
          <el-form-item label="新密码" prop="newPassword">
            <el-input v-model="pwdForm.newPassword" type="password" show-password placeholder="至少6位" />
          </el-form-item>
          <el-form-item label="确认新密码" prop="confirmPwd">
            <el-input v-model="pwdForm.confirmPwd" type="password" show-password placeholder="再次输入新密码" />
          </el-form-item>
          <el-button type="primary" :loading="changing" @click="changePwd">修改密码</el-button>
        </el-form>
      </div>
    </div>

    <!-- API Key -->
    <div class="card" style="margin-top:16px">
      <div class="space-between"><h3 class="panel-title">API 密钥</h3><el-button size="small" @click="fetchApiKey" :loading="keyLoading">生成/刷新</el-button></div>
      <p class="muted" style="margin-bottom:12px">用于调用开放 API 推送接口，永久有效。请妥善保管。</p>
      <div v-if="apiKey" class="api-key-box">
        <code style="flex:1;word-break:break-all;font-size:13px">{{ apiKey }}</code>
        <el-button size="small" @click="copyKey">复制</el-button>
      </div>
      <div v-if="apiKey" style="margin-top:12px">
        <el-collapse>
          <el-collapse-item title="curl 示例">
            <pre class="api-code">curl -X POST http://localhost:8083/api/live/push \
  -H "Content-Type: application/json" \
  -H "X-API-Key: {{ apiKey }}" \
  -d '{"text":"欢迎来到直播间！","voiceModelId":5,"speed":1.0}'</pre>
          </el-collapse-item>
          <el-collapse-item title="Python 示例">
            <pre class="api-code">import requests
r = requests.post("http://localhost:8083/api/live/push",
    json={"text":"欢迎来到直播间！","voiceModelId":5},
    headers={"X-API-Key":"{{ apiKey }}"})
print(r.json())</pre>
          </el-collapse-item>
        </el-collapse>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import PageHeader from '../components/PageHeader.vue'
import request from '../api/request'
import { changePassword, getUserInfo } from '../api/auth'

const user = ref({})
const changing = ref(false)
const apiKey = ref('')
const keyLoading = ref(false)
const pwdRef = ref()
const pwdForm = reactive({ oldPassword: '', newPassword: '', confirmPwd: '' })
const validateConfirm = (_rule, value, cb) => {
  if (value !== pwdForm.newPassword) cb(new Error('两次输入的新密码不一致'))
  else cb()
}
const pwdRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [{ required: true, min: 6, message: '新密码至少6位', trigger: 'blur' }],
  confirmPwd: [{ required: true, message: '请确认新密码', trigger: 'blur' }, { validator: validateConfirm, trigger: 'blur' }],
}

async function load() {
  user.value = await getUserInfo().catch(() => ({})) || {}
  fetchApiKey()
}
async function fetchApiKey() {
  keyLoading.value = true
  try {
    const data = await request.get('/api/auth/apikey')
    apiKey.value = data?.apiKey || ''
  } catch { /* handled */ }
  finally { keyLoading.value = false }
}
function copyKey() { navigator.clipboard.writeText(apiKey.value); ElMessage.success('已复制') }
async function changePwd() {
  await pwdRef.value.validate()
  changing.value = true
  try {
    await changePassword({ oldPassword: pwdForm.oldPassword, newPassword: pwdForm.newPassword })
    ElMessage.success('密码修改成功')
    Object.assign(pwdForm, { oldPassword: '', newPassword: '', confirmPwd: '' })
  } finally { changing.value = false }
}
onMounted(load)
</script>
<style scoped>
.api-key-box { display:flex; align-items:center; gap:8px; background:var(--surface-soft); padding:12px; border:1px solid var(--border); border-radius:var(--radius-small); }
.api-code { background:var(--code-bg); color:var(--code-text); padding:16px; border-radius:var(--radius-small); font-size:13px; line-height:1.6; overflow-x:auto; white-space:pre; }
</style>
