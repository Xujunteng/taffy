import request from './request'

// 获取帮助文章列表
export function getHelpArticles(params) {
  return request({
    url: '/api/help/articles',
    method: 'get',
    params
  })
}

// 获取帮助文章详情
export function getHelpArticleDetail(id) {
  return request({
    url: `/api/help/articles/${id}`,
    method: 'get'
  })
}

// 搜索帮助文章
export function searchHelpArticles(keyword) {
  return request({
    url: '/api/help/search',
    method: 'get',
    params: { keyword }
  })
}