import request from '../utils/request'

export function getUserPage(params: { page: number; size: number; keyword?: string; role?: string; status?: number }) {
  return request.get('/api/users', { params })
}

export function getUser(id: number) {
  return request.get(`/api/users/${id}`)
}

export function createUser(data: any) {
  return request.post('/api/users', data)
}

export function updateUser(id: number, data: any) {
  return request.put(`/api/users/${id}`, data)
}

export function deleteUser(id: number) {
  return request.delete(`/api/users/${id}`)
}

export function resetUserPassword(id: number) {
  return request.put(`/api/users/${id}/reset-password`)
}

export const page = getUserPage
export const create = createUser
export const update = updateUser
export const remove = deleteUser
