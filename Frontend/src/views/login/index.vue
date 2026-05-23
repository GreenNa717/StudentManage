<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../../stores/user'
import { ElMessage } from 'element-plus'
import type { FormInstance } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref<FormInstance>()
const loading = ref(false)

const loginForm = reactive({
  username: '',
  password: '',
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

async function handleLogin() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await userStore.login(loginForm)
    ElMessage.success('登录成功')
    router.push('/dashboard')
  } catch {
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-page">
    <div class="login-bg">
      <div class="bg-shape shape-1"></div>
      <div class="bg-shape shape-2"></div>
      <div class="bg-shape shape-3"></div>
    </div>
    <div class="login-card">
      <div class="card-header">
        <div class="card-icon">
          <el-icon :size="28"><HomeFilled /></el-icon>
        </div>
        <h1 class="card-title">学生管理系统</h1>
        <p class="card-subtitle">Student Management System</p>
      </div>
      <el-form ref="formRef" :model="loginForm" :rules="rules" class="login-form" @keyup.enter="handleLogin">
        <el-form-item prop="username">
          <el-input v-model="loginForm.username" placeholder="用户名" prefix-icon="User" size="large" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="loginForm.password" type="password" placeholder="密码" prefix-icon="Lock" size="large" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" :loading="loading" class="login-btn" @click="handleLogin">
            登 录
          </el-button>
        </el-form-item>
      </el-form>
      <div class="card-footer">
        <span>默认账号: admin / 123456</span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.login-page {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
  background: var(--main-bg);
}

.login-bg {
  position: absolute;
  inset: 0;
  z-index: 0;
  background: linear-gradient(135deg, var(--primary) 0%, color-mix(in srgb, var(--primary) 60%, #6366f1) 100%);
}

.bg-shape {
  position: absolute;
  border-radius: 50%;
  opacity: 0.12;
  animation: float 8s ease-in-out infinite;
}

.shape-1 {
  width: 500px;
  height: 500px;
  background: #fff;
  top: -100px;
  right: -100px;
  animation-delay: 0s;
}

.shape-2 {
  width: 350px;
  height: 350px;
  background: #fff;
  bottom: -80px;
  left: -80px;
  animation-delay: -3s;
}

.shape-3 {
  width: 200px;
  height: 200px;
  background: #fff;
  top: 50%;
  left: 20%;
  animation-delay: -5s;
}

@keyframes float {
  0%, 100% { transform: translateY(0) rotate(0deg); }
  33% { transform: translateY(-20px) rotate(5deg); }
  66% { transform: translateY(10px) rotate(-3deg); }
}

.login-card {
  position: relative;
  z-index: 1;
  width: 420px;
  padding: 48px 40px 36px;
  background: var(--card-bg);
  border-radius: var(--card-radius);
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
  backdrop-filter: blur(20px);
}

[data-theme="classic"] .login-card {
  border-radius: 8px;
  box-shadow: 0 8px 40px rgba(0, 0, 0, 0.12);
  backdrop-filter: none;
}

[data-theme="premium"] .login-card {
  width: 440px;
  padding: 56px 48px 40px;
  border-radius: 20px;
  box-shadow: 0 24px 80px rgba(0, 0, 0, 0.08);
}

.card-header {
  text-align: center;
  margin-bottom: 36px;
}

.card-icon {
  width: 56px;
  height: 56px;
  margin: 0 auto 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--primary);
  color: #fff;
  border-radius: 16px;
  box-shadow: 0 8px 24px color-mix(in srgb, var(--primary) 40%, transparent);
}

[data-theme="classic"] .card-icon {
  border-radius: 50%;
}

.card-title {
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 4px;
}

.card-subtitle {
  font-size: 13px;
  color: var(--text-secondary);
  letter-spacing: 0.5px;
}

.login-form {
  margin-top: 8px;
}

.login-btn {
  width: 100%;
  height: 44px;
  font-size: 15px;
  font-weight: 600;
  border-radius: var(--input-radius);
  letter-spacing: 2px;
}

.card-footer {
  text-align: center;
  margin-top: 20px;
  font-size: 12px;
  color: var(--text-placeholder);
}
</style>
