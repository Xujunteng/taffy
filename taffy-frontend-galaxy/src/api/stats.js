import request from './request'
export const getLiveStats = params => request.get('/api/stats/live', { params })
export const getStatsOverview = () => request.get('/api/stats/overview')
export const getLiveSessions = () => request.get('/api/stats/sessions')
