<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { getUserInfo, changePassword } from '../../api/auth'
import { useUserStore } from '../../stores/user'

const userStore = useUserStore()
const userInfo = ref<any>({})
const loading = ref(false)

const roleMap: Record<string, string> = { ADMIN: '管理员', TEACHER: '教师', STUDENT: '学生' }
const genderMap: Record<number, string> = { 0: '女', 1: '男', 2: '未知' }

const infoItems = computed(() => {
  const u = userInfo.value
  const role = u.role
  const items = [
    { label: '用户名', value: u.username },
    { label: '角色', value: roleMap[role] || role },
    { label: '姓名', value: u.name || '-' },
  ]
  if (role === 'STUDENT') {
    items.push(
      { label: '学号', value: u.studentNo || '-' },
      { label: '院系', value: u.departmentName || '-' },
      { label: '班级', value: u.className || '-' },
    )
  } else if (role === 'TEACHER') {
    items.push(
      { label: '工号', value: u.teacherNo || '-' },
      { label: '院系', value: u.departmentName || '-' },
      { label: '职称', value: u.title || '-' },
    )
  }
  items.push(
    { label: '性别', value: u.gender != null ? genderMap[u.gender] || '-' : '-' },
    { label: '联系电话', value: u.phone || '-' },
    { label: '上次登录', value: u.lastLoginTime || '-' },
  )
  return items
})

async function loadUserInfo() {
  loading.value = true
  try {
    const res: any = await getUserInfo()
    userInfo.value = res.data
  } catch {
    ElMessage.error('获取用户信息失败')
  } finally {
    loading.value = false
  }
}

const pwdDialogVisible = ref(false)
const pwdFormRef = ref<FormInstance>()
const pwdForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })
const pwdRules = {
  oldPassword: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 30, message: '密码长度6-30位', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (_rule: any, value: string, callback: any) => {
        if (value !== pwdForm.newPassword) callback(new Error('两次密码不一致'))
        else callback()
      },
      trigger: 'blur',
    },
  ],
}
const pwdLoading = ref(false)

function openPwdDialog() {
  pwdForm.oldPassword = ''
  pwdForm.newPassword = ''
  pwdForm.confirmPassword = ''
  pwdDialogVisible.value = true
}

async function handlePwdSubmit() {
  const valid = await pwdFormRef.value?.validate().catch(() => false)
  if (!valid) return
  pwdLoading.value = true
  try {
    await changePassword({ oldPassword: pwdForm.oldPassword, newPassword: pwdForm.newPassword })
    ElMessage.success('密码修改成功，请重新登录')
    pwdDialogVisible.value = false
    userStore.logout()
    location.href = '/login'
  } catch {
  } finally {
    pwdLoading.value = false
  }
}

onMounted(() => {
  loadUserInfo()
})
</script>

<template>
  <div class="profile-page" v-loading="loading">
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card class="profile-card">
          <div class="profile-avatar">
            <el-avatar :size="80" class="avatar-large">
              <el-icon :size="40"><UserFilled /></el-icon>
            </el-avatar>
          </div>
          <div class="profile-name">{{ userInfo.name || userInfo.username }}</div>
          <div class="profile-role">
            <el-tag :type="userInfo.role === 'ADMIN' ? 'danger' : userInfo.role === 'TEACHER' ? 'warning' : ''">
              {{ roleMap[userInfo.role] || userInfo.role }}
            </el-tag>
          </div>
          <el-divider />
          <div class="profile-quick">
            <div class="quick-item">
              <el-icon><User /></el-icon>
              <span>{{ userInfo.username }}</span>
            </div>
            <div class="quick-item" v-if="userInfo.departmentName">
              <el-icon><OfficeBuilding /></el-icon>
              <span>{{ userInfo.departmentName }}</span>
            </div>
            <div class="quick-item" v-if="userInfo.phone">
              <el-icon><Phone /></el-icon>
              <span>{{ userInfo.phone }}</span>
            </div>
          </div>
          <el-divider />
          <el-button type="primary" plain style="width: 100%" @click="openPwdDialog">
            <el-icon><Lock /></el-icon>修改密码
          </el-button>
        </el-card>
      </el-col>

      <el-col :span="16">
        <el-card>
          <template #header>
            <div class="card-header">
              <span class="card-title">个人资料</span>
              <el-tag type="info" size="small">只读</el-tag>
            </div>
          </template>
          <el-descriptions :column="2" border>
            <el-descriptions-item
              v-for="item in infoItems"
              :key="item.label"
              :label="item.label"
            >
              {{ item.value }}
            </el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="pwdDialogVisible" title="修改密码" width="440px" destroy-on-close>
      <el-form ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" label-width="90px">
        <el-form-item label="旧密码" prop="oldPassword">
          <el-input v-model="pwdForm.oldPassword" type="password" show-password placeholder="请输入旧密码" />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="pwdForm.newPassword" type="password" show-password placeholder="6-30位密码" />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="pwdForm.confirmPassword" type="password" show-password placeholder="再次输入新密码" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="pwdDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="pwdLoading" @click="handlePwdSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.profile-card {
  text-align: center;
}

.profile-avatar {
  padding: 20px 0 12px;
}

.avatar-large {
  background: var(--primary);
  color: #fff;
}

.profile-name {
  font-size: 20px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 8px;
}

.profile-role {
  margin-bottom: 4px;
}

.profile-quick {
  text-align: left;
}

.quick-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 0;
  color: var(--text-regular);
  font-size: 14px;
}

.quick-item .el-icon {
  color: var(--text-secondary);
  font-size: 16px;
  width: 16px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.card-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
}
</style>
