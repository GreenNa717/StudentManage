<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import CrudPage from '@/components/CrudPage.vue'
import * as api from '@/api/score'
import { getStudentPage } from '@/api/student'
import { getCoursePage } from '@/api/course'
import { getScoreStatistics } from '@/api/score'
import { useUserStore } from '@/stores/user'
import type { FormField, SearchField } from '@/types/crud'

const userStore = useUserStore()
const isAdmin = computed(() => userStore.role === 'ADMIN')
const isTeacher = computed(() => userStore.role === 'TEACHER')
const isStudent = computed(() => userStore.role === 'STUDENT')

const students = ref<any[]>([])
const courses = ref<any[]>([])
const statistics = ref<any[]>([])
const showStats = ref(false)

onMounted(async () => {
  const [sRes, cRes]: any[] = await Promise.all([
    getStudentPage({ page: 1, size: 200 }),
    getCoursePage({ page: 1, size: 200 }),
  ])
  students.value = sRes.data.records
  courses.value = cRes.data.records
})

const studentOptions = () => students.value.map((s: any) => ({ label: `${s.name}(${s.studentNo})`, value: s.id }))
const courseOptions = () => courses.value.map((c: any) => ({ label: c.name, value: c.id }))
const searchFields = computed(() => {
  const fields: SearchField[] = [
    { key: 'courseId', placeholder: '选择课程', type: 'select', options: courseOptions(), clearable: true },
    { key: 'semester', placeholder: '学期 如2024-2025-1', type: 'input', width: '180px' },
  ]
  if (!isStudent.value) {
    fields.unshift({
      key: 'studentId',
      placeholder: '选择学生',
      type: 'select',
      options: studentOptions(),
      clearable: true,
      filterable: true,
    })
  }
  return fields
})
const formFields = computed(() => {
  if (isStudent.value) {
    return [] as FormField[]
  }
  return [
    { key: 'studentId', label: '学生', type: 'select', required: true, options: studentOptions(), placeholder: '请选择学生', filterable: true },
    { key: 'courseId', label: '课程', type: 'select', required: true, options: courseOptions(), placeholder: '请选择课程', filterable: true },
    { key: 'score', label: '成绩', type: 'number', required: true, min: 0, max: 100, precision: 2 },
    { key: 'semester', label: '学期', type: 'input', required: true, placeholder: '如 2024-2025-1' },
  ] as FormField[]
})
const formDefault = computed(() => (isStudent.value ? {} : { studentId: null, courseId: null, score: null, semester: '' }))

async function loadStatistics() {
  const res: any = await getScoreStatistics({})
  statistics.value = res.data
  showStats.value = true
}
</script>

<template>
  <CrudPage
    title="成绩"
    :api="api"
    :showAdd="isAdmin || isTeacher"
    :showEdit="isAdmin || isTeacher"
    :showDelete="isAdmin"
    :columns="[
      { prop: 'id', label: 'ID', width: 80 },
      { prop: 'studentNo', label: '学号', width: 130 },
      { prop: 'studentName', label: '学生姓名', width: 100 },
      { prop: 'courseName', label: '课程名称' },
      { prop: 'score', label: '成绩', width: 80 },
      { prop: 'semester', label: '学期', width: 130 },
      { prop: 'createTime', label: '创建时间', width: 180 },
    ]"
    :searchFields="searchFields"
    :formFields="formFields"
    :formDefault="formDefault"
    :editFields="['studentId', 'courseId', 'score', 'semester']"
  >
    <template #toolbar-extra>
      <el-button type="info" @click="loadStatistics">
        <el-icon><DataAnalysis /></el-icon>查看统计
      </el-button>
    </template>
  </CrudPage>

  <el-dialog v-model="showStats" title="成绩统计" width="700px">
     <el-table :data="statistics">
      <el-table-column prop="courseName" label="课程" />
      <el-table-column prop="totalCount" label="人数" width="80" />
      <el-table-column prop="avgScore" label="平均分" width="80" />
      <el-table-column prop="maxScore" label="最高分" width="80" />
      <el-table-column prop="minScore" label="最低分" width="80" />
      <el-table-column prop="passRate" label="及格率%" width="100" />
    </el-table>
  </el-dialog>
</template>
