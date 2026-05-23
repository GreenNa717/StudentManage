<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'
import type { ColumnDef, FormField, SearchField } from '@/types/crud'

const props = defineProps<{
  title: string
  api: {
    page: (params: any) => Promise<any>
    create?: (data: any) => Promise<any>
    update?: (id: number, data: any) => Promise<any>
    remove?: (id: number) => Promise<any>
  }
  columns: ColumnDef[]
  searchFields?: SearchField[]
  formFields: FormField[]
  formDefault: Record<string, any>
  formRules?: Record<string, any[]>
  formLabelWidth?: string
  dialogWidth?: string
  searchParams?: Record<string, any>
  searchModel?: Record<string, any>
  showAdd?: boolean
  showEdit?: boolean
  showDelete?: boolean
  editFields?: string[]
}>()

const tableData = ref<any[]>([])
const total = ref(0)
const loading = ref(false)

const searchForm = reactive<Record<string, any>>({})
const pagination = reactive({ page: 1, size: 10 })

const dialogVisible = ref(false)
const dialogTitle = ref('')
const editingId = ref<number | null>(null)
const formRef = ref<FormInstance>()

const form = reactive<Record<string, any>>({})
const rules = computed(() => {
  const r: Record<string, any[]> = {}
  for (const field of props.formFields) {
    if (field.required && !(editingId.value !== null && field.hideOnEdit)) {
      r[field.key] = [{ required: true, message: `${field.label}不能为空`, trigger: field.type === 'select' || field.type === 'radio' ? 'change' : 'blur' }]
    }
  }
  if (props.formRules) {
    Object.assign(r, props.formRules)
  }
  return r
})

onMounted(() => {
  const source = props.searchModel || searchForm
  if (props.searchFields) {
    for (const f of props.searchFields) {
      if (!(f.key in source)) {
        source[f.key] = undefined
      }
    }
  }
  loadData()
})

async function loadData() {
  loading.value = true
  try {
    const params: Record<string, any> = {
      page: pagination.page,
      size: pagination.size,
    }
    const source = props.searchModel || searchForm
    if (props.searchFields) {
      for (const f of props.searchFields) {
        const val = source[f.key]
        if (val !== undefined && val !== null && val !== '') {
          params[f.key] = val
        }
      }
    }
    if (props.searchParams) {
      Object.assign(params, props.searchParams)
    }
    const res: any = await props.api.page(params)
    tableData.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.page = 1
  loadData()
}

function handleReset() {
  const source = props.searchModel || searchForm
  if (props.searchFields) {
    for (const f of props.searchFields) {
      source[f.key] = undefined
    }
  }
  handleSearch()
}

function handleAdd() {
  dialogTitle.value = `新增${props.title}`
  editingId.value = null
  Object.assign(form, JSON.parse(JSON.stringify(props.formDefault)))
  dialogVisible.value = true
}

function handleEdit(row: any) {
  dialogTitle.value = `编辑${props.title}`
  editingId.value = row.id
  const fields = props.editFields || props.formFields.map((f) => f.key)
  const data: Record<string, any> = {}
  for (const key of fields) {
    data[key] = row[key]
  }
  Object.assign(form, { ...JSON.parse(JSON.stringify(props.formDefault)), ...data })
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  try {
    if (editingId.value) {
      await props.api.update?.(editingId.value, form)
      ElMessage.success('修改成功')
    } else {
      await props.api.create?.(form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadData()
  } catch { /* handled */ }
}

async function handleDelete(id: number) {
  await ElMessageBox.confirm(`确定删除该${props.title}？`, '提示', { type: 'warning' })
  await props.api.remove?.(id)
  ElMessage.success('删除成功')
  loadData()
}

function handlePageChange(page: number) {
  pagination.page = page
  loadData()
}

function getRadioOptions(field: FormField) {
  return field.options || []
}

function shouldRenderField(field: FormField) {
  return !(editingId.value !== null && field.hideOnEdit)
}

defineExpose({ loadData })
</script>

<template>
  <div class="crud-page">
    <el-card>
      <div class="crud-toolbar">
        <div class="toolbar-left">
          <template v-if="searchFields">
            <template v-for="field in searchFields" :key="field.key">
                <el-input
                  v-if="field.type === 'input'"
                  v-model="(props.searchModel || searchForm)[field.key]"
                  :placeholder="field.placeholder || '请输入'"
                  clearable
                  :style="{ width: field.width || '200px' }"
                @keyup.enter="handleSearch"
                @clear="handleSearch"
              >
                <template #prefix><el-icon><Search /></el-icon></template>
              </el-input>
              <el-select
                v-else-if="field.type === 'select'"
                v-model="(props.searchModel || searchForm)[field.key]"
                :placeholder="field.placeholder || '请选择'"
                :clearable="field.clearable !== false"
                :filterable="field.filterable"
                :style="{ width: field.width || '200px', marginLeft: '12px' }"
                @change="handleSearch"
              >
                <el-option v-for="opt in field.options" :key="opt.value" :label="opt.label" :value="opt.value" />
              </el-select>
              <el-input-number
                v-else-if="field.type === 'number'"
                v-model="(props.searchModel || searchForm)[field.key]"
                :placeholder="field.placeholder"
                :min="field.min"
                :max="field.max"
                :style="{ width: field.width || '160px', marginLeft: '12px' }"
                @change="handleSearch"
              />
            </template>
          </template>
          <slot name="search-extra" />
        </div>
        <div class="toolbar-right">
          <el-button v-if="searchFields" @click="handleReset">
            <el-icon><RefreshLeft /></el-icon>重置
          </el-button>
          <el-button v-if="showAdd !== false && api.create" type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>新增
          </el-button>
          <slot name="toolbar-extra" />
        </div>
      </div>

      <el-table :data="tableData" v-loading="loading" class="crud-table">
        <el-table-column
          v-for="col in columns"
          :key="col.prop"
          :prop="col.prop"
          :label="col.label"
          :width="col.width"
        >
          <template #default="{ row }">
            <slot v-if="col.slot" :name="col.slot" :row="row" />
            <template v-else-if="col.formatter">{{ col.formatter(row) }}</template>
            <template v-else>{{ row[col.prop] }}</template>
          </template>
        </el-table-column>
        <el-table-column v-if="showEdit !== false || showDelete !== false" label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <slot name="action" :row="row" :edit="handleEdit" :del="handleDelete">
              <el-button v-if="showEdit !== false && api.update" type="primary" link @click="handleEdit(row)">编辑</el-button>
              <el-button v-if="showDelete !== false && api.remove" type="danger" link @click="handleDelete(row.id)">删除</el-button>
            </slot>
          </template>
        </el-table-column>
      </el-table>

      <div class="crud-pagination">
        <el-pagination
          v-model:current-page="pagination.page"
          :page-size="pagination.size"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          :page-sizes="[10, 20, 50]"
          @current-change="handlePageChange"
          @size-change="(s: number) => { pagination.size = s; handleSearch() }"
        />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" :width="dialogWidth || '520px'" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" :label-width="formLabelWidth || '90px'" class="crud-form">
        <template v-for="field in formFields" :key="field.key">
          <el-form-item v-if="shouldRenderField(field)" :label="field.label" :prop="field.key">
            <el-input
              v-if="field.type === 'input'"
              v-model="form[field.key]"
              :type="field.inputType || 'text'"
              :show-password="field.inputType === 'password'"
              :placeholder="field.placeholder || `请输入${field.label}`"
            />
            <el-input
              v-else-if="field.type === 'textarea'"
              v-model="form[field.key]"
              type="textarea"
              :rows="field.rows || 3"
              :placeholder="field.placeholder || `请输入${field.label}`"
            />
            <el-select
              v-else-if="field.type === 'select'"
              v-model="form[field.key]"
              :placeholder="field.placeholder || `请选择${field.label}`"
              :filterable="field.filterable"
              style="width: 100%"
            >
              <el-option v-for="opt in field.options" :key="opt.value" :label="opt.label" :value="opt.value" />
            </el-select>
            <el-input-number
              v-else-if="field.type === 'number'"
              v-model="form[field.key]"
              :min="field.min"
              :max="field.max"
              :step="field.step || 1"
              :precision="field.precision"
              style="width: 100%"
            />
            <el-date-picker
              v-else-if="field.type === 'date'"
              v-model="form[field.key]"
              type="date"
              :value-format="field.valueFormat || 'YYYY-MM-DD'"
              style="width: 100%"
            />
            <el-radio-group v-else-if="field.type === 'radio'" v-model="form[field.key]">
              <el-radio v-for="opt in getRadioOptions(field)" :key="opt.value" :value="opt.value">{{ opt.label }}</el-radio>
            </el-radio-group>
          </el-form-item>
        </template>
        <slot name="form-extra" :form="form" />
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.crud-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  flex-wrap: wrap;
  gap: 12px;
}

.toolbar-left {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 4px;
}

.toolbar-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.crud-pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.crud-form {
  padding-right: 20px;
}
</style>
