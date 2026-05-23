<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import CrudPage from '@/components/CrudPage.vue'
import * as api from '@/api/user'
import { getStudentPage } from '@/api/student'
import { getTeacherPage } from '@/api/teacher'
import type { FormField, SearchField } from '@/types/crud'

const teachers = ref<any[]>([])
const students = ref<any[]>([])

const roleOptions = [
  { label: '管理员', value: 'ADMIN' },
  { label: '教师', value: 'TEACHER' },
  { label: '学生', value: 'STUDENT' },
]
const statusOptions = [
  { label: '启用', value: 1 },
  { label: '禁用', value: 0 },
]
const roleMap: Record<string, string> = {
  ADMIN: '管理员',
  TEACHER: '教师',
  STUDENT: '学生',
}
const statusMap: Record<number, string> = {
  0: '禁用',
  1: '启用',
}

onMounted(async () => {
  const [teacherRes, studentRes]: any[] = await Promise.all([
    getTeacherPage({ page: 1, size: 200 }),
    getStudentPage({ page: 1, size: 200 }),
  ])
  teachers.value = teacherRes.data.records
  students.value = studentRes.data.records
})

const searchFields: SearchField[] = [
  { key: 'keyword', placeholder: '搜索用户名', type: 'input' },
  { key: 'role', placeholder: '选择角色', type: 'select', options: roleOptions, clearable: true },
  { key: 'status', placeholder: '选择状态', type: 'select', options: statusOptions, clearable: true },
]

const formFields: FormField[] = [
  { key: 'username', label: '用户名', type: 'input', required: true },
  { key: 'password', label: '初始密码', type: 'input', inputType: 'password', required: true, hideOnEdit: true, placeholder: '请输入6-30位密码' },
  { key: 'role', label: '角色', type: 'select', required: true, options: roleOptions, placeholder: '请选择角色' },
  { key: 'status', label: '状态', type: 'radio', required: true, options: statusOptions },
]

const formRules = computed(() => ({
  refId: [
    {
      validator: (_rule: unknown, value: number | null, callback: (error?: Error) => void, source: Record<string, any>) => {
        if (source.role !== 'ADMIN' && !value) {
          callback(new Error('请选择关联档案'))
          return
        }
        callback()
      },
      trigger: 'change',
    },
  ],
}))

function getRelationOptions(role: string) {
  if (role === 'TEACHER') {
    return teachers.value.map((teacher: any) => ({
      label: `${teacher.name}(${teacher.teacherNo})`,
      value: teacher.id,
    }))
  }
  if (role === 'STUDENT') {
    return students.value.map((student: any) => ({
      label: `${student.name}(${student.studentNo})`,
      value: student.id,
    }))
  }
  return []
}

async function handleResetPassword(id: number) {
  await ElMessageBox.confirm('确定将该账号密码重置为 123456 吗？', '提示', { type: 'warning' })
  await api.resetUserPassword(id)
  ElMessage.success('密码已重置为 123456')
}
</script>

<template>
  <CrudPage
    title="用户"
    :api="api"
    :columns="[
      { prop: 'id', label: 'ID', width: 80 },
      { prop: 'username', label: '用户名', width: 160 },
      { prop: 'role', label: '角色', width: 100, slot: 'role' },
      { prop: 'refName', label: '关联档案', width: 220 },
      { prop: 'status', label: '状态', width: 100, slot: 'status' },
      { prop: 'lastLoginTime', label: '上次登录', width: 180 },
      { prop: 'createTime', label: '创建时间', width: 180 },
    ]"
    :searchFields="searchFields"
    :formFields="formFields"
    :formRules="formRules"
    :formDefault="{ username: '', password: '', role: 'STUDENT', refId: null, status: 1 }"
    :editFields="['username', 'role', 'refId', 'status']"
    dialogWidth="560px"
  >
    <template #role="{ row }">{{ roleMap[row.role] || row.role }}</template>
    <template #status="{ row }">
      <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ statusMap[row.status] || row.status }}</el-tag>
    </template>

    <template #form-extra="{ form }">
      <el-form-item label="关联档案" prop="refId">
        <el-select
          v-if="form.role !== 'ADMIN'"
          v-model="form.refId"
          placeholder="请选择关联档案"
          filterable
          clearable
          style="width: 100%"
        >
          <el-option
            v-for="option in getRelationOptions(form.role)"
            :key="option.value"
            :label="option.label"
            :value="option.value"
          />
        </el-select>
        <el-input v-else value="管理员无需关联档案" disabled />
      </el-form-item>
    </template>

    <template #action="{ row, edit, del }">
      <el-button type="primary" link @click="edit(row)">编辑</el-button>
      <el-button type="warning" link @click="handleResetPassword(row.id)">重置密码</el-button>
      <el-button type="danger" link @click="del(row.id)">删除</el-button>
    </template>
  </CrudPage>
</template>
