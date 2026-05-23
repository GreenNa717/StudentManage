<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '../stores/user'
import { useAppStore, type Theme } from '../stores/app'
import { useRouter } from 'vue-router'
import { getMenuByRole } from '../utils/permission'
import { ElMessageBox } from 'element-plus'

const route = useRoute()
const userStore = useUserStore()
const appStore = useAppStore()
const router = useRouter()
const dynamicRoutesAdded = ref(false)

const menus = ref<{ path: string; title: string; icon: string }[]>([])

const sidebarWidth = computed(() => {
  const collapsed = appStore.sidebarCollapsed
  if (appStore.theme === 'premium') return collapsed ? 72 : 260
  return collapsed ? 64 : 220
})

const breadcrumbItems = computed(() => {
  const matched = route.matched.filter((item) => item.meta?.title)
  return matched.map((item) => ({ title: item.meta.title as string, path: item.path }))
})

const themeOptions: { label: string; value: Theme }[] = [
  { label: '现代扁平', value: 'modern' },
  { label: '经典后台', value: 'classic' },
  { label: '简约高级', value: 'premium' },
]

const themeLabel = computed(() => themeOptions.find((t) => t.value === appStore.theme)?.label || '现代扁平')

watch(() => appStore.theme, () => {
  const root = document.documentElement
  root.style.setProperty('--sidebar-width', '220px')
  root.style.setProperty('--sidebar-collapsed-width', '64px')
  if (appStore.theme === 'premium') {
    root.style.setProperty('--sidebar-width', '260px')
    root.style.setProperty('--sidebar-collapsed-width', '72px')
  }
}, { immediate: true })

onMounted(async () => {
  appStore.initTheme()
  if (!dynamicRoutesAdded.value && userStore.token) {
    await userStore.fetchUserInfo()
    addDynamicRoutes()
    menus.value = getMenuByRole(userStore.role)
    dynamicRoutesAdded.value = true
  }
})

function addDynamicRoutes() {
  const role = userStore.role
  const routeMap: Record<string, { path: string; name: string; component: () => Promise<any>; meta: { title: string } }[]> = {
    ADMIN: [
      { path: 'department', name: 'Department', component: () => import('../views/department/index.vue'), meta: { title: '院系管理' } },
      { path: 'major', name: 'Major', component: () => import('../views/major/index.vue'), meta: { title: '专业管理' } },
      { path: 'class', name: 'Class', component: () => import('../views/class/index.vue'), meta: { title: '班级管理' } },
      { path: 'teacher', name: 'Teacher', component: () => import('../views/teacher/index.vue'), meta: { title: '教师管理' } },
      { path: 'student', name: 'Student', component: () => import('../views/student/index.vue'), meta: { title: '学生管理' } },
      { path: 'course', name: 'Course', component: () => import('../views/course/index.vue'), meta: { title: '课程管理' } },
      { path: 'score', name: 'Score', component: () => import('../views/score/index.vue'), meta: { title: '成绩管理' } },
      { path: 'user', name: 'UserManage', component: () => import('../views/user/index.vue'), meta: { title: '用户管理' } },
    ],
    TEACHER: [
      { path: 'student', name: 'Student', component: () => import('../views/student/index.vue'), meta: { title: '学生信息' } },
      { path: 'course', name: 'Course', component: () => import('../views/course/index.vue'), meta: { title: '课程信息' } },
      { path: 'score', name: 'Score', component: () => import('../views/score/index.vue'), meta: { title: '成绩管理' } },
    ],
    STUDENT: [
      { path: 'student', name: 'Student', component: () => import('../views/student/index.vue'), meta: { title: '我的信息' } },
      { path: 'score', name: 'Score', component: () => import('../views/score/index.vue'), meta: { title: '我的成绩' } },
    ],
  }
  const routes = routeMap[role] || []
  routes.forEach((r) => {
    if (!router.hasRoute(r.name)) {
      router.addRoute('MainLayout', r)
    }
  })
}

async function handleCommand(command: string) {
  if (command === 'logout') {
    await ElMessageBox.confirm('确定退出登录？', '提示', { type: 'warning' })
    userStore.logout()
    dynamicRoutesAdded.value = false
    router.push('/login')
  } else if (command === 'profile') {
    router.push('/profile')
  }
}
</script>

<template>
  <el-container class="layout-container">
    <el-aside :width="sidebarWidth" class="layout-aside">
      <div class="sidebar-logo">
        <div class="logo-icon">
          <el-icon :size="22"><HomeFilled /></el-icon>
        </div>
        <transition name="fade">
          <span v-if="!appStore.sidebarCollapsed" class="logo-text">学生管理系统</span>
        </transition>
      </div>
      <el-menu
        :default-active="$route.path"
        :collapse="appStore.sidebarCollapsed"
        :collapse-transition="false"
        router
        class="sidebar-menu"
      >
        <el-menu-item index="/dashboard">
          <el-icon><HomeFilled /></el-icon>
          <template #title>首页</template>
        </el-menu-item>
        <el-menu-item v-for="menu in menus" :key="menu.path" :index="`/${menu.path}`">
          <el-icon><component :is="menu.icon" /></el-icon>
          <template #title>{{ menu.title }}</template>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container class="layout-right">
      <el-header class="layout-header">
        <div class="header-left">
          <el-icon class="collapse-btn" @click="appStore.toggleSidebar">
            <Fold v-if="!appStore.sidebarCollapsed" />
            <Expand v-else />
          </el-icon>
          <el-breadcrumb separator="/" class="breadcrumb">
            <el-breadcrumb-item v-for="item in breadcrumbItems" :key="item.path" :to="item.path">
              {{ item.title }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-dropdown trigger="click" @command="appStore.setTheme">
            <span class="theme-switch">
              <el-icon><Brush /></el-icon>
              <span class="theme-label">{{ themeLabel }}</span>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item v-for="opt in themeOptions" :key="opt.value" :command="opt.value" :class="{ 'is-active': appStore.theme === opt.value }">
                  {{ opt.label }}
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
          <el-switch
            :model-value="appStore.darkMode"
            class="dark-switch"
            inline-prompt
            active-text="🌙"
            inactive-text="☀️"
            @change="(v: boolean) => appStore.toggleDark(v)"
          />
          <el-dropdown @command="handleCommand" class="user-dropdown">
            <span class="user-info">
              <el-avatar :size="28" class="user-avatar">
                <el-icon><UserFilled /></el-icon>
              </el-avatar>
              <span class="user-name">{{ userStore.username }}</span>
              <el-icon class="arrow"><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>个人资料
                </el-dropdown-item>
                <el-dropdown-item command="logout" divided>
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="layout-main">
        <router-view v-slot="{ Component, route: viewRoute }">
          <transition name="slide-fade" mode="out-in">
            <component :is="Component" :key="viewRoute.path" v-if="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
.layout-container {
  height: 100vh;
}

.layout-aside {
  background: var(--sidebar-bg);
  border-right: 1px solid var(--sidebar-border);
  transition: width var(--transition-duration) ease;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.sidebar-logo {
  height: var(--header-height);
  display: flex;
  align-items: center;
  padding: 0 16px;
  gap: 10px;
  background: var(--sidebar-logo-bg);
  overflow: hidden;
  flex-shrink: 0;
}

.logo-icon {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--sidebar-logo-text);
  flex-shrink: 0;
  border-radius: 8px;
}

.logo-text {
  color: var(--sidebar-logo-text);
  font-size: 16px;
  font-weight: 600;
  white-space: nowrap;
}

.sidebar-menu {
  flex: 1;
  overflow-y: auto;
  background: transparent !important;
  --el-menu-bg-color: transparent;
  --el-menu-text-color: var(--sidebar-text);
  --el-menu-active-color: var(--sidebar-active);
  --el-menu-hover-bg-color: var(--sidebar-hover-bg);
  --el-menu-hover-text-color: var(--sidebar-active);
}

.sidebar-menu :deep(.el-menu-item) {
  transition: background 0.2s, color 0.2s;
}

.sidebar-menu :deep(.el-menu-item.is-active) {
  background: var(--sidebar-active-bg) !important;
  color: var(--sidebar-active) !important;
  font-weight: 600;
}

.sidebar-menu :deep(.el-menu-item:hover) {
  color: var(--sidebar-active) !important;
}

.layout-right {
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.layout-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: var(--header-bg);
  border-bottom: 1px solid var(--header-border);
  box-shadow: var(--header-shadow);
  padding: 0 20px;
  height: var(--header-height);
  flex-shrink: 0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.collapse-btn {
  font-size: 20px;
  cursor: pointer;
  color: var(--text-regular);
  transition: color 0.2s;
}
.collapse-btn:hover {
  color: var(--primary);
}

.breadcrumb {
  line-height: var(--header-height);
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.theme-switch {
  display: flex;
  align-items: center;
  gap: 4px;
  cursor: pointer;
  color: var(--text-regular);
  font-size: 13px;
  padding: 4px 8px;
  border-radius: var(--input-radius);
  transition: background 0.2s;
}
.theme-switch:hover {
  background: var(--sidebar-hover-bg);
}
.theme-label {
  font-size: 13px;
}

.dark-switch {
  --el-switch-on-color: #6366f1;
}

.user-dropdown {
  cursor: pointer;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.user-avatar {
  background: var(--primary);
  color: #fff;
}

.user-name {
  color: var(--text-primary);
  font-size: 14px;
  font-weight: 500;
}

.arrow {
  color: var(--text-secondary);
  font-size: 12px;
}

.layout-main {
  background: var(--main-bg);
  padding: var(--page-padding);
  overflow-y: auto;
  flex: 1;
}
</style>
