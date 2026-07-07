import request from './request'
export const getVoiceList=()=>request.get('/api/voices')
export const getVoiceDetail=id=>request.get(`/api/voices/${id}`)
export const updateVoice=(id,data)=>request.put(`/api/voices/${id}`,data)
export const deleteVoice=id=>request.delete(`/api/voices/${id}`)
export const uploadAudio=formData=>request.post('/api/audio/upload',formData,{headers:{'Content-Type':'multipart/form-data'}})
export const startVoiceTrain=voiceModelId=>request.post('/api/voice/train',{voiceModelId})
export const getTrainStatus=taskId=>request.get(`/api/voice/train/status/${taskId}`)
