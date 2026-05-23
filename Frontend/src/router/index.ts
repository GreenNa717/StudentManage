import { createRouter, createWebHistory } from 'vue-router'
import { getToken } from '../utils/auth'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import('../views/login/index.vue'),
      meta: { title: '登录', public: true },
    },
    {
      path: '/',
      name: 'MainLayout',
      component: () => import('../layouts/BasicLayout.vue'),
      redirect: '/dashboard',
      children: [
        {
          path: 'dashboard',
          name: 'Dashboard',
          component: () => import('../views/dashboard/index.vue'),
          meta: { title: '首页' },
        },
        {
          path: 'profile',
          name: 'Profile',
          component: () => import('../views/profile/index.vue'),
          meta: { title: '个人资料' },
        },
      ],
    },
  ],
})

router.beforeEach((to) => {
  document.title = `${to.meta.title || '学生管理系统'} - 学生管理系统`
  const token = getToken()

  if (to.meta.public) {
    return token && to.name === 'Login' ? '/dashboard' : true
  }

  if (token) {
    return true
  } else {
    return '/login'
  }
})

export default router
