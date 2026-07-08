<template>
  <div class="auth-page">
    <div class="auth-orbit"></div>
    <div class="card auth-card">
      <div class="auth-logo">🚀</div>
      <h1 class="auth-title glow-title">创建星河账号</h1>
      <p class="auth-subtitle">注册后即可上传音频、训练声音模型并生成直播语音。</p>
      <ErrorBlock :message="error" />
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="用户名" prop="username"><el-input v-model.trim="form.username" size="large" /></el-form-item>
        <el-form-item label="邮箱" prop="email"><el-input v-model.trim="form.email" size="large" /></el-form-item>
        <el-form-item label="密码" prop="password"><el-input v-model="form.password" size="large" type="password" show-password /></el-form-item>
        <el-form-item label="确认密码" prop="confirm"><el-input v-model="form.confirm" size="large" type="password" show-password /></el-form-item>
        <el-button class="full" size="large" type="primary" :loading="loading" @click="submit">注册账号</el-button>
      </el-form>
      <div style="margin-top:18px" class="muted">已有账号？<router-link to="/login">返回登录</router-link></div>
    </div>
  </div>
</template>
<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../stores/user'
import ErrorBlock from '../components/ErrorBlock.vue'
const router = useRouter()
const userStore = useUserStore()
const formRef = ref()
const loading = ref(false)
const error = ref('')
const form = reactive({ username: '', email: '', password: '', confirm: '' })
const validateConfirm = (_, value, cb) => value === form.password ? cb() : cb(new Error('两次密码不一致'))
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }, { min: 3, message: '用户名至少3位', trigger: 'blur' }],
  email: [{ type: 'email', message: '邮箱格式不正确', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }, { min: 6, message: '密码至少6位', trigger: 'blur' }],
  confirm: [{ required: true, message: '请确认密码', trigger: 'blur' }, { validator: validateConfirm, trigger: 'blur' }]
}
async function submit() {
  error.value = ''
  await formRef.value.validate()
  loading.value = true
  try {
    await userStore.doRegister({ username: form.username, email: form.email, password: form.password })
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } catch (e) { error.value = e.message || '注册失败' }
  finally { loading.value = false }
}
</script>
