import request from '../utils/request'

export function getTeacherPage(params: { page: number; size: number; keyword?: string; departmentId?: number }) {
  return request.get('/api/teachers', { params })
}

export function getTeacher(id: number) {
  return request.get(`/api/teachers/${id}`)
}

export function createTeacher(data: any) {
  return request.post('/api/teachers', data)
}

export function updateTeacher(id: number, data: any) {
  return request.put(`/api/teachers/${id}`, data)
}

export function deleteTeacher(id: number) {
  return request.delete(`/api/teachers/${id}`)
}

export const page = getTeacherPage
export const create = createTeacher
export const update = updateTeacher
export const remove = deleteTeacher
