import { defineStore } from 'pinia'
import { ref } from 'vue'

export type Theme = 'modern' | 'classic' | 'premium'

export const useAppStore = defineStore('app', () => {
  const sidebarCollapsed = ref(false)
  const theme = ref<Theme>((localStorage.getItem('theme') as Theme) || 'modern')
  const darkMode = ref(localStorage.getItem('darkMode') === 'true')

  function initTheme() {
    document.documentElement.setAttribute('data-theme', theme.value)
    if (darkMode.value) {
      document.documentElement.classList.add('dark')
    }
  }

  function toggleSidebar() {
    sidebarCollapsed.value = !sidebarCollapsed.value
  }

  function setTheme(t: Theme) {
    theme.value = t
    localStorage.setItem('theme', t)
    document.documentElement.setAttribute('data-theme', t)
  }

  function toggleDark(val?: boolean) {
    darkMode.value = val ?? !darkMode.value
    document.documentElement.classList.toggle('dark', darkMode.value)
    localStorage.setItem('darkMode', String(darkMode.value))
  }

  return { sidebarCollapsed, theme, darkMode, toggleSidebar, setTheme, toggleDark, initTheme }
})
