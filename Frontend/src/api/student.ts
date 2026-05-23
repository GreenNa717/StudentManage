import request from '../utils/request'

export function getStudentPage(params: { page: number; size: number; keyword?: string; classId?: number }) {
  return request.get('/api/students', { params })
}

export function getStudent(id: number) {
  return request.get(`/api/students/${id}`)
}

export function createStudent(data: any) {
  return request.post('/api/students', data)
}

export function updateStudent(id: number, data: any) {
  return request.put(`/api/students/${id}`, data)
}

export function deleteStudent(id: number) {
  return request.delete(`/api/students/${id}`)
}

export const page = getStudentPage
export const create = createStudent
export const update = updateStudent
export const remove = deleteStudent
