import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({ baseURL: '', timeout: 45000 })

request.interceptors.request.use(config => {
  const token = localStorage.getItem('taffy_token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

request.interceptors.response.use(
  response => {
    if (response.data instanceof Blob) return response.data
    const body = response.data
    if (body && typeof body === 'object' && Object.prototype.hasOwnProperty.call(body, 'code')) {
      if (body.code === 200 || body.code === 0) return body.data
      const message = body.message || '请求失败'
      ElMessage.error(message)
      return Promise.reject(new Error(message))
    }
    return body
  },
  error => {
    const status = error.response?.status
    const message = error.response?.data?.message || error.message || '网络连接失败'
    if (status === 401) {
      localStorage.removeItem('taffy_token')
      localStorage.removeItem('taffy_user')
      if (!location.pathname.includes('/login')) location.href = '/login'
      return Promise.reject(error)
    }
    ElMessage.error(message)
    return Promise.reject(error)
  }
)
export default request
