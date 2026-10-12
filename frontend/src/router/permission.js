import router, { constantRoutes } from '@/router'
import { useUserStore } from '@/store/user'
import Layout from '@/layout/index.vue'
import ParentView from '@/components/ParentView.vue'
import { h, defineComponent } from 'vue'

// 自动收集 views 下所有 .vue
const modules = import.meta.glob('@/views/**/*.vue')

// 内链占位（本系统无内链，给一个渲染子路由的空壳）
const InnerLink = defineComponent({
  name: 'InnerLink',
  render() {
    return h('router-view')
  }
})

// 后端路由 -> 真实路由
function loadComponent(component) {
  if (component === 'Layout') return Layout
  if (component === 'ParentView') return ParentView
  if (component === 'InnerLink') return InnerLink
  const key = `/src/views/${component}.vue`
  if (modules[key]) return modules[key]
  return modules['/src/views/error/404.vue']
}

function filterAsyncRoutes(routes) {
  const res = []
  routes.forEach((route) => {
    const tmp = { ...route }
    // 后端占位的 "noRedirect" 并非真实地址，删除以免 vue-router 误重定向；真实 redirect 保留
    if (tmp.redirect === 'noRedirect') {
      delete tmp.redirect
    }
    if (tmp.component) {
      tmp.component = loadComponent(tmp.component)
    }
    if (tmp.children) {
      tmp.children = filterAsyncRoutes(tmp.children)
    }
    res.push(tmp)
  })
  return res
}

// 白名单
const whiteList = ['/login']

router.beforeEach(async (to, from, next) => {
  const userStore = useUserStore()
  // 公开溯源不依赖管理端登录状态，旧令牌也不应阻止消费者查看。
  if (to.name === 'PublicTrace') {
    next()
    return
  }
  if (userStore.token) {
    if (to.path === '/login') {
      next('/')
    } else if (!userStore.routesLoaded) {
      try {
        await userStore.getInfo()
        const routers = await userStore.getRouters()
        // 数据看板已在静态路由中，动态路由里剔除避免重复
        const dynamic = routers.filter((r) => r.path !== '/dashboard')
        const accessRoutes = filterAsyncRoutes(dynamic)
        // 注册动态路由
        userStore.setRouteRemovers(accessRoutes.map((r) => router.addRoute(r)))
        // 侧边栏 = 静态路由 + 动态路由（过滤隐藏/登录/公开页/404）
        const sidebar = [...constantRoutes, ...accessRoutes].filter(
          (r) =>
            !r.hidden &&
            r.path !== '/login' &&
            r.path !== '/trace/:code' &&
            r.path !== '/:pathMatch(.*)*'
        )
        userStore.setSidebarRoutes(sidebar)
        // 重新进入目标地址，确保命中新注册路由
        next({ ...to, replace: true })
      } catch (err) {
        console.error('GUARD_ERROR', err)
        userStore.logout()
        next({ path: '/login', query: { redirect: to.fullPath } })
      }
    } else {
      next()
    }
  } else {
    if (whiteList.includes(to.path) || to.path.startsWith('/trace/')) {
      next()
    } else {
      next({ path: '/login', query: { redirect: to.fullPath } })
    }
  }
})
