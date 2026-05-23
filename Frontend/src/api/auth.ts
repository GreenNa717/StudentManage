import request from '../utils/request'

export function login(data: { username: string; password: string }) {
  return request.post('/api/auth/login', data)
}

export function getUserInfo() {
  return request.get('/api/auth/info')
}

export function changePassword(data: { oldPassword: string; newPassword: string }) {
  return request.put('/api/auth/password', data)
}
