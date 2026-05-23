import axios from 'axios'
import request from '../utils/request'
import { getToken } from '../utils/auth'

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

export function importScores(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/api/scores/import', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  })
}

async function downloadFile(url: string, params?: Record<string, unknown>) {
  const response = await axios.get(url, {
    params,
    responseType: 'blob',
    headers: getToken() ? { Authorization: `Bearer ${getToken()}` } : undefined,
  })
  return response.data as Blob
}

export function exportScores(params: { studentId?: number; courseId?: number; semester?: string }) {
  return downloadFile('/api/scores/export', params)
}

export function downloadScoreTemplate() {
  return downloadFile('/api/scores/template')
}

export const page = getScorePage
export const create = createScore
export const update = updateScore
export const remove = deleteScore
