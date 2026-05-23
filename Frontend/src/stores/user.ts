import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getToken, setToken, removeToken } from '../utils/auth'
import { getMenuByRole } from '../utils/permission'
import { login as loginApi, getUserInfo as getUserInfoApi } from '../api/auth'

export const useUserStore = defineStore('user', () => {
  const token = ref(getToken())
  const username = ref('')
  const role = ref('')
  const refId = ref<number | null>(null)
  const menus = ref<{ path: string; title: string; icon: string }[]>([])

  async function login(loginForm: { username: string; password: string }) {
    const res: any = await loginApi(loginForm)
    token.value = res.data.token
    username.value = res.data.username
    role.value = res.data.role
    refId.value = res.data.refId
    setToken(res.data.token)
    menus.value = getMenuByRole(res.data.role)
  }

  async function fetchUserInfo() {
    const res: any = await getUserInfoApi()
    username.value = res.data.username
    role.value = res.data.role
    refId.value = res.data.refId
    menus.value = getMenuByRole(res.data.role)
  }

  function logout() {
    token.value = ''
    username.value = ''
    role.value = ''
    refId.value = null
    menus.value = []
    removeToken()
  }

  return { token, username, role, refId, menus, login, fetchUserInfo, logout }
})
