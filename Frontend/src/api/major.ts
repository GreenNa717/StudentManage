import request from '../utils/request'

export function getMajorPage(params: { page: number; size: number; keyword?: string; departmentId?: number }) {
  return request.get('/api/majors', { params })
}

export function getMajor(id: number) {
  return request.get(`/api/majors/${id}`)
}

export function createMajor(data: any) {
  return request.post('/api/majors', data)
}

export function updateMajor(id: number, data: any) {
  return request.put(`/api/majors/${id}`, data)
}

export function deleteMajor(id: number) {
  return request.delete(`/api/majors/${id}`)
}

export const page = getMajorPage
export const create = createMajor
export const update = updateMajor
export const remove = deleteMajor
