import request from './request'

// 文本转语音
export function convertTTS(data) {
  return request({
    url: '/api/tts/convert',
    method: 'post',
    data
  })
}

// 获取 TTS 任务状态
export function getTTSStatus(taskId) {
  return request({
    url: `/api/tts/status/${taskId}`,
    method: 'get'
  })
}

// 获取 TTS 历史记录
export function getTTSHistory() {
  return request({
    url: '/api/tts/history',
    method: 'get'
  })
}

// 下载合成的音频文件
export function downloadTTS(taskId) {
  return request({
    url: `/api/tts/download/${taskId}`,
    method: 'get',
    responseType: 'blob'
  })
}