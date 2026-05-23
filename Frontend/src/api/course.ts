import request from '../utils/request'

export function getCoursePage(params: { page: number; size: number; keyword?: string; teacherId?: number }) {
  return request.get('/api/courses', { params })
}

export function getCourse(id: number) {
  return request.get(`/api/courses/${id}`)
}

export function createCourse(data: any) {
  return request.post('/api/courses', data)
}

export function updateCourse(id: number, data: any) {
  return request.put(`/api/courses/${id}`, data)
}

export function deleteCourse(id: number) {
  return request.delete(`/api/courses/${id}`)
}

export const page = getCoursePage
export const create = createCourse
export const update = updateCourse
export const remove = deleteCourse
