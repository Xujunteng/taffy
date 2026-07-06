import request from './request'

// 获取直播统计数据
export function getLiveStats(params) {
  return request({
    url: '/api/stats/live',
    method: 'get',
    params
  })
}

// 获取今日统计概览
export function getTodayOverview() {
  return request({
    url: '/api/stats/overview',
    method: 'get'
  })
}

// 获取直播会话列表
export function getLiveSessions(params) {
  return request({
    url: '/api/stats/sessions',
    method: 'get',
    params
  })
}