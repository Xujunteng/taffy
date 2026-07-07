import axios from 'axios'
import { ElMessage } from 'element-plus'
const request=axios.create({baseURL:'',timeout:30000})
request.interceptors.request.use(config=>{const token=localStorage.getItem('token'); if(token) config.headers.Authorization=`Bearer ${token}`; return config})
request.interceptors.response.use(res=>{const data=res.data; if(data instanceof Blob) return data; if(data && typeof data==='object' && 'code' in data){ if(data.code===200) return data.data; ElMessage.error(data.message||'请求失败'); return Promise.reject(data)} return data}, err=>{const msg=err.response?.data?.message||err.message||'网络错误'; ElMessage.error(msg); if(err.response?.status===401){localStorage.removeItem('token'); location.href='/login'} return Promise.reject(err)})
export default request
