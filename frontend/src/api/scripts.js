import request from './request'

// 获取脚本列表
export function getScriptList(params) {
  return request({
    url: '/api/scripts',
    method: 'get',
    params
  })
}

// 获取单个脚本
export function getScriptDetail(id) {
  return request({
    url: `/api/scripts/${id}`,
    method: 'get'
  })
}

// 创建脚本
export function createScript(data) {
  return request({
    url: '/api/scripts',
    method: 'post',
    data
  })
}

// 更新脚本
export function updateScript(id, data) {
  return request({
    url: `/api/scripts/${id}`,
    method: 'put',
    data
  })
}

// 删除脚本
export function deleteScript(id) {
  return request({
    url: `/api/scripts/${id}`,
    method: 'delete'
  })
}