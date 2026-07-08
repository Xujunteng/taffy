import request from './request'
export const convertTTS = data => request.post('/api/tts/convert', data)
export const getTTSStatus = taskId => request.get(`/api/tts/status/${taskId}`)
export const getTTSHistory = () => request.get('/api/tts/history')
export const downloadTTS = taskId => request.get(`/api/tts/download/${taskId}`, { responseType: 'blob' })
