import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'
import router from '@/router'

const service = axios.create({ baseURL: '/api', timeout: 15000 })
const isPublic = (config) => /^\/trace\/public\//.test(config?.url || '')
const isLogin = (config) => config?.url === '/auth/login'
service.interceptors.request.use((config) => {
  const userStore = useUserStore()
  if (userStore.token && !isPublic(config) && !isLogin(config))
    config.headers.Authorization = 'Bearer ' + userStore.token
  return config
})

function rejectResponse(res, config) {
  const message = res.msg || '请求失败'
  if (res.code === 401 && !isPublic(config) && !isLogin(config))
    handleUnauthorized()
  else ElMessage.error(message)
  return Promise.reject(new Error(message))
}
service.interceptors.response.use(
  async (response) => {
    if (response.config.responseType === 'blob') {
      // 导出/二维码接口也可能返回业务错误 JSON，不能当作文件下载。
      if ((response.headers['content-type'] || '').includes('json')) {
        let res
        try {
          res = JSON.parse(await response.data.text())
        } catch {
          ElMessage.error('文件响应异常，请重试')
          throw new Error('文件响应异常，请重试')
        }
        if (res.code !== 200) return rejectResponse(res, response.config)
      }
      return response.data
    }
    const res = response.data
    if (res.code === 200) return res
    return rejectResponse(res, response.config)
  },
  async (error) => {
    const status = error.response?.status
    let responseData = error.response?.data
    if (responseData instanceof Blob && responseData.type.includes('json')) {
      try {
        responseData = JSON.parse(await responseData.text())
      } catch {
        /* 保留默认错误提示 */
      }
    }
    if (status === 401 && !isPublic(error.config) && !isLogin(error.config))
      handleUnauthorized()
    else if (status === 403) ElMessage.error('没有访问权限')
    else
      ElMessage.error(
        responseData?.msg ||
          (error.code === 'ECONNABORTED'
            ? '请求超时，请稍后重试'
            : error.message) ||
          '网络异常'
      )
    return Promise.reject(error)
  }
)
let redirecting = false
function handleUnauthorized() {
  if (redirecting) return
  redirecting = true
  ElMessage.error('登录已过期，请重新登录')
  useUserStore().logout()
  router.replace({ path: '/login', query: { redirect: router.currentRoute.value.fullPath } })
  setTimeout(() => {
    redirecting = false
  }, 1000)
}
export default service
