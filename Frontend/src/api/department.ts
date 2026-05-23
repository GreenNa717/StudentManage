import request from '../utils/request'

export function getDepartmentPage(params: { page: number; size: number; keyword?: string }) {
  return request.get('/api/departments', { params })
}

export function getDepartment(id: number) {
  return request.get(`/api/departments/${id}`)
}

export function createDepartment(data: any) {
  return request.post('/api/departments', data)
}

export function updateDepartment(id: number, data: any) {
  return request.put(`/api/departments/${id}`, data)
}

export function deleteDepartment(id: number) {
  return request.delete(`/api/departments/${id}`)
}

export const page = getDepartmentPage
export const create = createDepartment
export const update = updateDepartment
export const remove = deleteDepartment
