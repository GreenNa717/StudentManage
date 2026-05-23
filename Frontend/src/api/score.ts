import request from '../utils/request'

export function getScorePage(params: { page: number; size: number; studentId?: number; courseId?: number; semester?: string }) {
  return request.get('/api/scores', { params })
}

export function getScore(id: number) {
  return request.get(`/api/scores/${id}`)
}

export function createScore(data: any) {
  return request.post('/api/scores', data)
}

export function updateScore(id: number, data: any) {
  return request.put(`/api/scores/${id}`, data)
}

export function deleteScore(id: number) {
  return request.delete(`/api/scores/${id}`)
}

export function getScoreStatistics(params: { courseId?: number; semester?: string }) {
  return request.get('/api/scores/statistics', { params })
}

export const page = getScorePage
export const create = createScore
export const update = updateScore
export const remove = deleteScore
