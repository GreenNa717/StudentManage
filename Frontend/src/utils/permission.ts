export function getMenuByRole(role: string) {
  const menus: Record<string, { path: string; title: string; icon: string }[]> = {
    ADMIN: [
      { path: 'department', title: '院系管理', icon: 'OfficeBuilding' },
      { path: 'major', title: '专业管理', icon: 'Reading' },
      { path: 'class', title: '班级管理', icon: 'Collection' },
      { path: 'teacher', title: '教师管理', icon: 'User' },
      { path: 'student', title: '学生管理', icon: 'Avatar' },
      { path: 'course', title: '课程管理', icon: 'Notebook' },
      { path: 'score', title: '成绩管理', icon: 'DataAnalysis' },
    ],
    TEACHER: [
      { path: 'student', title: '学生信息', icon: 'Avatar' },
      { path: 'course', title: '课程信息', icon: 'Notebook' },
      { path: 'score', title: '成绩管理', icon: 'DataAnalysis' },
    ],
    STUDENT: [
      { path: 'student', title: '我的信息', icon: 'Avatar' },
      { path: 'score', title: '我的成绩', icon: 'DataAnalysis' },
    ],
  }
  return menus[role] || []
}
