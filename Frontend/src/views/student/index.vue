<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import CrudPage from '@/components/CrudPage.vue'
import * as api from '@/api/student'
import { getClassPage } from '@/api/class'
import { useUserStore } from '@/stores/user'

const classes = ref<any[]>([])
const userStore = useUserStore()
const isAdmin = computed(() => userStore.role === 'ADMIN')
const isStudent = computed(() => userStore.role === 'STUDENT')

onMounted(async () => {
  if (isStudent.value) {
    return
  }
  const res: any = await getClassPage({ page: 1, size: 200 })
  classes.value = res.data.records
})

const classOptions = () => classes.value.map((c: any) => ({ label: c.name, value: c.id }))
const searchFields = computed(() => {
  if (isStudent.value) {
    return undefined
  }
  return [
    { key: 'keyword', placeholder: '搜索姓名/学号', type: 'input' as const },
    { key: 'classId', placeholder: '选择班级', type: 'select' as const, options: classOptions(), clearable: true },
  ]
})

const genderMap: Record<number, string> = { 0: '女', 1: '男', 2: '未知' }
</script>

<template>
  <CrudPage
    title="学生"
    :api="api"
    :showAdd="isAdmin"
    :showEdit="isAdmin"
    :showDelete="isAdmin"
    :columns="[
      { prop: 'id', label: 'ID', width: 80 },
      { prop: 'studentNo', label: '学号', width: 130 },
      { prop: 'name', label: '姓名', width: 100 },
      { prop: 'gender', label: '性别', width: 80, slot: 'gender' },
      { prop: 'birthDate', label: '出生日期', width: 120 },
      { prop: 'className', label: '班级' },
      { prop: 'phone', label: '联系方式' },
      { prop: 'createTime', label: '创建时间', width: 180 },
    ]"
    :searchFields="searchFields"
    :formFields="[
      { key: 'studentNo', label: '学号', type: 'input', required: true },
      { key: 'name', label: '姓名', type: 'input', required: true },
      { key: 'gender', label: '性别', type: 'radio', required: true, options: [{ label: '男', value: 1 }, { label: '女', value: 0 }] },
      { key: 'birthDate', label: '出生日期', type: 'date' },
      { key: 'classId', label: '班级', type: 'select', required: true, options: classOptions(), placeholder: '请选择班级' },
      { key: 'phone', label: '联系方式', type: 'input' },
    ]"
    :formDefault="{ studentNo: '', name: '', gender: 1, birthDate: '', classId: null, phone: '' }"
    :editFields="['studentNo', 'name', 'gender', 'birthDate', 'classId', 'phone']"
  >
    <template #gender="{ row }">{{ genderMap[row.gender] }}</template>
  </CrudPage>
</template>
