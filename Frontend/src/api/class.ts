import request from '../utils/request'

export function getClassPage(params: { page: number; size: number; keyword?: string; majorId?: number; grade?: number }) {
  return request.get('/api/classes', { params })
}

export function getClass(id: number) {
  return request.get(`/api/classes/${id}`)
}

export function createClass(data: any) {
  return request.post('/api/classes', data)
}

export function updateClass(id: number, data: any) {
  return request.put(`/api/classes/${id}`, data)
}

export function deleteClass(id: number) {
  return request.delete(`/api/classes/${id}`)
}

export const page = getClassPage
export const create = createClass
export const update = updateClass
export const remove = deleteClass
