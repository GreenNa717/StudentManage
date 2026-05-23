<script setup lang="ts">
import { ref, onMounted } from 'vue'
import CrudPage from '@/components/CrudPage.vue'
import * as api from '@/api/teacher'
import { getDepartmentPage } from '@/api/department'

const departments = ref<any[]>([])

onMounted(async () => {
  const res: any = await getDepartmentPage({ page: 1, size: 100 })
  departments.value = res.data.records
})

const deptOptions = () => departments.value.map((d: any) => ({ label: d.name, value: d.id }))

const genderMap: Record<number, string> = { 0: '女', 1: '男', 2: '未知' }
</script>

<template>
  <CrudPage
    title="教师"
    :api="api"
    :columns="[
      { prop: 'id', label: 'ID', width: 80 },
      { prop: 'teacherNo', label: '工号', width: 120 },
      { prop: 'name', label: '姓名', width: 100 },
      { prop: 'gender', label: '性别', width: 80, slot: 'gender' },
      { prop: 'departmentName', label: '所属院系' },
      { prop: 'title', label: '职称' },
      { prop: 'phone', label: '联系方式' },
      { prop: 'createTime', label: '创建时间', width: 180 },
    ]"
    :searchFields="[
      { key: 'keyword', placeholder: '搜索姓名/工号', type: 'input' },
      { key: 'departmentId', placeholder: '选择院系', type: 'select', options: deptOptions(), clearable: true },
    ]"
    :formFields="[
      { key: 'teacherNo', label: '工号', type: 'input', required: true },
      { key: 'name', label: '姓名', type: 'input', required: true },
      { key: 'gender', label: '性别', type: 'radio', required: true, options: [{ label: '男', value: 1 }, { label: '女', value: 0 }] },
      { key: 'departmentId', label: '所属院系', type: 'select', required: true, options: deptOptions(), placeholder: '请选择院系' },
      { key: 'title', label: '职称', type: 'input' },
      { key: 'phone', label: '联系方式', type: 'input' },
    ]"
    :formDefault="{ teacherNo: '', name: '', gender: 1, departmentId: null, title: '', phone: '' }"
    :editFields="['teacherNo', 'name', 'gender', 'departmentId', 'title', 'phone']"
  >
    <template #gender="{ row }">{{ genderMap[row.gender] }}</template>
  </CrudPage>
</template>
