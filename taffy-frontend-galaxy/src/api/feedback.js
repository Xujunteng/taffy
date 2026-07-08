import request from './request'
export const getFeedbackList = params => request.get('/api/feedback', { params })
export const createFeedback = data => request.post('/api/feedback', data)
export const getVoiceRating = voiceModelId => request.get(`/api/feedback/rating/${voiceModelId}`)
