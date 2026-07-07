import request from './request'
export const getHelpArticles=params=>request.get('/api/help/articles',{params})
export const getHelpArticle=id=>request.get(`/api/help/articles/${id}`)
export const searchHelp=keyword=>request.get('/api/help/search',{params:{keyword}})
