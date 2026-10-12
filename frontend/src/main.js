import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

import App from './App.vue'
import router from './router'
import './router/permission'
import './styles/index.css'
import GlossaryTip from './components/GlossaryTip/index.vue'

const app = createApp(App)

// 注册专业词汇提示组件
app.component('GlossaryTip', GlossaryTip)

// 注册所有图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 权限指令 v-hasPermi
function applyPermission(el, binding) {
  const store = useUserStoreLocal()
  const required = Array.isArray(binding.value) ? binding.value : [binding.value]
  const allowed = store.permissions.includes('*:*:*') || required.some(p => store.permissions.includes(p))
  el.hidden = !allowed
  if (!allowed) el.style.setProperty('display', 'none', 'important')
  else el.style.removeProperty('display')
}
app.directive('hasPermi', { mounted: applyPermission, updated: applyPermission })

import { useUserStore } from './store/user'
function useUserStoreLocal() {
  return useUserStore()
}

app.use(createPinia())
app.use(router)
app.use(ElementPlus, { locale: zhCn })
app.mount('#app')
