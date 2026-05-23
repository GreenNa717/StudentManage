<script setup lang="ts">
import { ref, onMounted } from 'vue'
import CrudPage from '@/components/CrudPage.vue'
import * as api from '@/api/class'
import { getMajorPage } from '@/api/major'

const majors = ref<any[]>([])

onMounted(async () => {
  const res: any = await getMajorPage({ page: 1, size: 200 })
  majors.value = res.data.records
})

const majorOptions = () => majors.value.map((m: any) => ({ label: m.name, value: m.id }))
</script>

<template>
  <CrudPage
    title="班级"
    :api="api"
    :columns="[
      { prop: 'id', label: 'ID', width: 80 },
      { prop: 'name', label: '班级名称' },
      { prop: 'majorName', label: '所属专业' },
      { prop: 'grade', label: '年级', width: 100 },
      { prop: 'advisor', label: '班主任' },
      { prop: 'createTime', label: '创建时间', width: 180 },
    ]"
    :searchFields="[
      { key: 'keyword', placeholder: '搜索班级名称', type: 'input' },
      { key: 'majorId', placeholder: '选择专业', type: 'select', options: majorOptions(), clearable: true },
      { key: 'grade', placeholder: '年级', type: 'number', min: 2000, max: 2099, width: '150px' },
    ]"
    :formFields="[
      { key: 'name', label: '班级名称', type: 'input', required: true },
      { key: 'majorId', label: '所属专业', type: 'select', required: true, options: majorOptions(), placeholder: '请选择专业' },
      { key: 'grade', label: '年级', type: 'number', required: true, min: 2000, max: 2099 },
      { key: 'advisor', label: '班主任', type: 'input' },
    ]"
    :formDefault="{ name: '', majorId: null, grade: new Date().getFullYear(), advisor: '' }"
    :editFields="['name', 'majorId', 'grade', 'advisor']"
  />
</template>
