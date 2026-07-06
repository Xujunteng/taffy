import request from './request'

// 获取评价列表
export function getFeedbackList(params) {
  return request({
    url: '/api/feedback',
    method: 'get',
    params
  })
}

// 提交评价
export function submitFeedback(data) {
  return request({
    url: '/api/feedback',
    method: 'post',
    data
  })
}

// 获取某个声音模型的评价统计
export function getVoiceRating(voiceModelId) {
  return request({
    url: `/api/feedback/rating/${voiceModelId}`,
    method: 'get'
  })
}