import request from './request'
export const login = data => request.post('/api/auth/login', data)
export const register = data => request.post('/api/auth/register', data)
export const getUserInfo = () => request.get('/api/auth/userinfo')
export const changePassword = data => request.put('/api/auth/password', data)
