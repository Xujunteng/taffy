import { computed, ref } from 'vue'

export const themes = [
  {
    id: 'ocean',
    name: '云海蓝',
    description: '清爽、专业、适合日常后台',
    colors: ['#3366ff', '#dce7ff', '#ffffff']
  },
  {
    id: 'midnight',
    name: '深夜直播',
    description: '深色低眩光，适合直播控制台',
    colors: ['#7c8cff', '#151b2d', '#0b1020']
  },
  {
    id: 'sakura',
    name: '樱花玻璃',
    description: '柔和粉紫、半透明玻璃质感',
    colors: ['#db5f93', '#f8d5e4', '#fff8fb']
  },
  {
    id: 'matcha',
    name: '抹茶米色',
    description: '自然温和、低饱和舒适风格',
    colors: ['#4f7a63', '#dce6d9', '#f7f3e8']
  },
  {
    id: 'cyber',
    name: '赛博霓虹',
    description: '高对比、霓虹描边、科技感',
    colors: ['#00e5ff', '#ff3dbf', '#07090f']
  }
]

const storedTheme = localStorage.getItem('taffy-ui-theme')
const currentTheme = ref(themes.some(item => item.id === storedTheme) ? storedTheme : 'ocean')

function applyTheme(themeId) {
  const safeTheme = themes.some(item => item.id === themeId) ? themeId : 'ocean'
  currentTheme.value = safeTheme
  document.documentElement.dataset.theme = safeTheme
  document.documentElement.style.colorScheme = ['midnight', 'cyber'].includes(safeTheme) ? 'dark' : 'light'
  localStorage.setItem('taffy-ui-theme', safeTheme)
}

applyTheme(currentTheme.value)

export function useTheme() {
  return {
    themes,
    currentTheme: computed(() => currentTheme.value),
    currentThemeInfo: computed(() => themes.find(item => item.id === currentTheme.value) || themes[0]),
    setTheme: applyTheme
  }
}
