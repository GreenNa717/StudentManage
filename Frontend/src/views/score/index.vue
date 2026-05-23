<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import CrudPage from '@/components/CrudPage.vue'
import * as api from '@/api/score'
import { getStudentPage } from '@/api/student'
import { getCoursePage } from '@/api/course'
import { downloadScoreTemplate, exportScores, getScoreStatistics, importScores } from '@/api/score'
import { useUserStore } from '@/stores/user'
import type { FormField, SearchField } from '@/types/crud'
import { ElMessage } from 'element-plus'
import type { UploadRequestOptions } from 'element-plus'

const userStore = useUserStore()
const isAdmin = computed(() => userStore.role === 'ADMIN')
const isTeacher = computed(() => userStore.role === 'TEACHER')
const isStudent = computed(() => userStore.role === 'STUDENT')

const crudRef = ref<{ loadData: () => void } | null>(null)
const students = ref<any[]>([])
const courses = ref<any[]>([])
const statistics = ref<any[]>([])
const showStats = ref(false)
const importing = ref(false)
const scoreSearch = reactive<Record<string, any>>({
  studentId: undefined,
  courseId: undefined,
  semester: '',
})

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

function saveBlob(blob: Blob, fileName: string) {
  const url = window.URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = fileName
  document.body.appendChild(link)
  link.click()
  link.remove()
  window.URL.revokeObjectURL(url)
}

async function handleExport() {
  const blob = await exportScores({
    studentId: scoreSearch.studentId,
    courseId: scoreSearch.courseId,
    semester: scoreSearch.semester || undefined,
  })
  saveBlob(blob, 'scores.csv')
  ElMessage.success('成绩导出成功')
}

async function handleTemplateDownload() {
  const blob = await downloadScoreTemplate()
  saveBlob(blob, 'score-import-template.csv')
  ElMessage.success('模板下载成功')
}

async function handleImportUpload(options: UploadRequestOptions) {
  importing.value = true
  try {
    const res: any = await importScores(options.file)
    const summary = `总行数 ${res.data.totalRows}，新增 ${res.data.createdCount}，更新 ${res.data.updatedCount}，跳过 ${res.data.skippedCount}`
    ElMessage.success(summary)
    if (res.data.errorMessages?.length) {
      ElMessage.warning(`有 ${res.data.errorMessages.length} 条导入异常，请检查后重试`)
      console.warn('score import warnings', res.data.errorMessages)
    }
    crudRef.value?.loadData()
    options.onSuccess?.(res)
  } catch (error) {
    options.onError?.(error as any)
    throw error
  } finally {
    importing.value = false
  }
}
</script>

<template>
  <CrudPage
    ref="crudRef"
    title="成绩"
    :api="api"
    :showAdd="isAdmin || isTeacher"
    :showEdit="isAdmin || isTeacher"
    :showDelete="isAdmin"
    :searchModel="scoreSearch"
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
      <el-button v-if="isAdmin || isTeacher" @click="handleTemplateDownload">
        <el-icon><Download /></el-icon>模板
      </el-button>
      <el-upload
        v-if="isAdmin || isTeacher"
        :show-file-list="false"
        accept=".csv,text/csv"
        :http-request="handleImportUpload"
      >
        <el-button type="primary" plain :loading="importing">
          <el-icon><Upload /></el-icon>导入
        </el-button>
      </el-upload>
      <el-button v-if="isAdmin || isTeacher" type="success" plain @click="handleExport">
        <el-icon><Download /></el-icon>导出
      </el-button>
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
