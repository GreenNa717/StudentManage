import request from '../utils/request'

export function getDashboardOverview() {
  return request.get('/api/dashboard/overview')
}

export function getDepartmentStudentCount() {
  return request.get('/api/dashboard/department-students')
}

export function getScoreDistribution(semester?: string) {
  return request.get('/api/dashboard/score-distribution', { params: { semester } })
}
