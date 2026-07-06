import request from './request'

// 获取声音模型列表
export function getVoiceList() {
  return request({
    url: '/api/voices',
    method: 'get'
  })
}

// 获取单个声音模型详情
export function getVoiceDetail(id) {
  return request({
    url: `/api/voices/${id}`,
    method: 'get'
  })
}

// 删除声音模型
export function deleteVoice(id) {
  return request({
    url: `/api/voices/${id}`,
    method: 'delete'
  })
}

// 更新声音模型信息
export function updateVoice(id, data) {
  return request({
    url: `/api/voices/${id}`,
    method: 'put',
    data
  })
}

// 上传音频文件
export function uploadAudio(formData) {
  return request({
    url: '/api/audio/upload',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 开始声音训练
export function startVoiceTrain(data) {
  return request({
    url: '/api/voice/train',
    method: 'post',
    data
  })
}

// 获取训练状态
export function getTrainStatus(taskId) {
  return request({
    url: `/api/voice/train/status/${taskId}`,
    method: 'get'
  })
}