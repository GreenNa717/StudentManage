<script setup lang="ts">
import CrudPage from '@/components/CrudPage.vue'
import * as api from '@/api/department'
</script>

<template>
  <CrudPage
    title="院系"
    :api="api"
    :columns="[
      { prop: 'id', label: 'ID', width: 80 },
      { prop: 'name', label: '院系名称' },
      { prop: 'dean', label: '院长' },
      { prop: 'phone', label: '联系电话' },
      { prop: 'createTime', label: '创建时间', width: 180 },
    ]"
    :searchFields="[
      { key: 'keyword', placeholder: '搜索院系名称', type: 'input' },
    ]"
    :formFields="[
      { key: 'name', label: '院系名称', type: 'input', required: true },
      { key: 'dean', label: '院长', type: 'input' },
      { key: 'phone', label: '联系电话', type: 'input' },
    ]"
    :formDefault="{ name: '', dean: '', phone: '' }"
  />
</template>
