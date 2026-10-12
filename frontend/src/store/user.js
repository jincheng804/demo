import { defineStore } from 'pinia'
import request from '@/utils/request'

// Vue Router 返回的移除函数不参与持久化。
let routeRemovers = []

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('bee_token') || '',
    roles: [],
    permissions: [],
    user: {},
    // 用于侧边栏渲染的完整路由（静态 + 动态）
    sidebarRoutes: [],
    routesLoaded: false
  }),
  actions: {
    async login(loginForm) {
      const res = await request.post('/auth/login', loginForm)
      this.token = res.data.token
      localStorage.setItem('bee_token', this.token)
      return res
    },
    async getInfo() {
      const res = await request.get('/auth/info')
      this.roles = res.data.roles
      this.permissions = res.data.permissions
      this.user = res.data.user
      return res.data
    },
    async getRouters() {
      const res = await request.get('/auth/routers')
      return res.data
    },
    setSidebarRoutes(routes) {
      this.sidebarRoutes = routes
    },
    setRouteRemovers(removers) {
      routeRemovers.forEach((remove) => remove())
      routeRemovers = removers
      this.routesLoaded = true
    },
    async signOut() {
      try { await request.post('/auth/logout') } finally { this.logout() }
    },
    logout() {
      routeRemovers.forEach((remove) => remove())
      routeRemovers = []
      this.routesLoaded = false
      this.token = ''
      this.roles = []
      this.permissions = []
      this.user = {}
      this.sidebarRoutes = []
      localStorage.removeItem('bee_token')
    },
    hasPermi(perm) {
      return (
        this.permissions.includes('*:*:*') || this.permissions.includes(perm)
      )
    }
  }
})
