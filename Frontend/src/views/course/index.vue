<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import CrudPage from '@/components/CrudPage.vue'
import * as api from '@/api/course'
import { getTeacherPage } from '@/api/teacher'
import { useUserStore } from '@/stores/user'
import type { SearchField } from '@/types/crud'

const teachers = ref<any[]>([])
const userStore = useUserStore()
const isAdmin = computed(() => userStore.role === 'ADMIN')

onMounted(async () => {
  if (!isAdmin.value) {
    return
  }
  const res: any = await getTeacherPage({ page: 1, size: 200 })
  teachers.value = res.data.records
})

const teacherOptions = () => teachers.value.map((t: any) => ({ label: `${t.name}(${t.teacherNo})`, value: t.id }))
const searchFields = computed(() => {
  const fields: SearchField[] = [{ key: 'keyword', placeholder: '搜索课程号/名称', type: 'input' }]
  if (isAdmin.value) {
    fields.push({
      key: 'teacherId',
      placeholder: '选择教师',
      type: 'select',
      options: teacherOptions(),
      clearable: true,
      filterable: true,
    })
  }
  return fields
})
</script>

<template>
  <CrudPage
    title="课程"
    :api="api"
    :showAdd="isAdmin"
    :showEdit="isAdmin"
    :showDelete="isAdmin"
    :columns="[
      { prop: 'id', label: 'ID', width: 80 },
      { prop: 'courseNo', label: '课程号', width: 120 },
      { prop: 'name', label: '课程名称' },
      { prop: 'credit', label: '学分', width: 80 },
      { prop: 'hours', label: '学时', width: 80 },
      { prop: 'teacherName', label: '授课教师', width: 100 },
      { prop: 'createTime', label: '创建时间', width: 180 },
    ]"
    :searchFields="searchFields"
    :formFields="[
      { key: 'courseNo', label: '课程号', type: 'input', required: true },
      { key: 'name', label: '课程名称', type: 'input', required: true },
      { key: 'credit', label: '学分', type: 'number', required: true, min: 0.5, max: 10, step: 0.5, precision: 1 },
      { key: 'hours', label: '学时', type: 'number', required: true, min: 8, max: 200 },
      { key: 'teacherId', label: '授课教师', type: 'select', required: true, options: teacherOptions(), placeholder: '请选择教师', filterable: true },
    ]"
    :formDefault="{ courseNo: '', name: '', credit: 3, hours: 48, teacherId: null }"
    :editFields="['courseNo', 'name', 'credit', 'hours', 'teacherId']"
  />
</template>
