import request from './request'
export const getScriptList = params => request.get('/api/scripts', { params })
export const getScriptDetail = id => request.get(`/api/scripts/${id}`)
export const createScript = data => request.post('/api/scripts', data)
export const updateScript = (id, data) => request.put(`/api/scripts/${id}`, data)
export const deleteScript = id => request.delete(`/api/scripts/${id}`)
