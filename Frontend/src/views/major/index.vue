<script setup lang="ts">
import { ref, onMounted } from 'vue'
import CrudPage from '@/components/CrudPage.vue'
import * as api from '@/api/major'
import { getDepartmentPage } from '@/api/department'

const departments = ref<any[]>([])

onMounted(async () => {
  const res: any = await getDepartmentPage({ page: 1, size: 100 })
  departments.value = res.data.records
})

const deptOptions = () => departments.value.map((d: any) => ({ label: d.name, value: d.id }))
</script>

<template>
  <CrudPage
    title="专业"
    :api="api"
    :columns="[
      { prop: 'id', label: 'ID', width: 80 },
      { prop: 'name', label: '专业名称' },
      { prop: 'departmentName', label: '所属院系' },
      { prop: 'duration', label: '学制(年)', width: 100 },
      { prop: 'createTime', label: '创建时间', width: 180 },
    ]"
    :searchFields="[
      { key: 'keyword', placeholder: '搜索专业名称', type: 'input' },
      { key: 'departmentId', placeholder: '选择院系', type: 'select', options: deptOptions(), clearable: true },
    ]"
    :formFields="[
      { key: 'name', label: '专业名称', type: 'input', required: true },
      { key: 'departmentId', label: '所属院系', type: 'select', required: true, options: deptOptions(), placeholder: '请选择院系' },
      { key: 'duration', label: '学制(年)', type: 'number', required: true, min: 1, max: 8 },
    ]"
    :formDefault="{ name: '', departmentId: null, duration: 4 }"
    :editFields="['name', 'departmentId', 'duration']"
  />
</template>
