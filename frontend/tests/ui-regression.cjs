const fs = require('fs'),
  path = require('path'),
  assert = require('assert/strict'),
  { createRequire } = require('module')
const root = path.resolve(__dirname, '..')
const req = createRequire(root + '/package.json')
const { JSDOM } = req('jsdom')
const dom = new JSDOM('<!doctype html><html><body></body></html>', {
  url: 'http://localhost:5173',
  pretendToBeVisual: true
})
for (const key of [
  'window',
  'document',
  'navigator',
  'Element',
  'HTMLElement',
  'SVGElement',
  'Node',
  'Document',
  'MutationObserver',
  'localStorage',
  'getComputedStyle',
  'Event',
  'MouseEvent',
  'KeyboardEvent'
])
  global[key] =
    key === 'getComputedStyle'
      ? dom.window.getComputedStyle.bind(dom.window)
      : dom.window[key]
for (const key of Object.getOwnPropertyNames(dom.window)) {
  if (!(key in global)) {
    try {
      global[key] = dom.window[key]
    } catch {}
  }
}
global.requestAnimationFrame = dom.window.requestAnimationFrame.bind(dom.window)
global.cancelAnimationFrame = dom.window.cancelAnimationFrame.bind(dom.window)
class ResizeObserver {
  constructor(callback) {
    this.callback = callback
    this.targets = new Set()
  }
  observe(el) {
    this.targets.add(el)
  }
  unobserve(el) {
    this.targets.delete(el)
  }
  disconnect() {
    this.targets.clear()
  }
}
global.ResizeObserver = ResizeObserver
window.matchMedia = (query) => ({
  matches: false,
  media: query,
  addEventListener() {},
  removeEventListener() {},
  addListener() {},
  removeListener() {}
})
window.scrollTo = () => {}
const objectUrls = [],
  revoked = []
URL.createObjectURL = () => {
  const value = 'blob:qa-' + objectUrls.length
  objectUrls.push(value)
  return value
}
URL.revokeObjectURL = (url) => revoked.push(url)
const Vue = req('vue'),
  Pinia = req('pinia'),
  ElementPlus = req('element-plus'),
  Icons = req('@element-plus/icons-vue')
const { parse } = req('@babel/parser'),
  { parse: parseSfc, compileScript, compileTemplate } = req('@vue/compiler-sfc')
const route = {
  path: '/dashboard',
  fullPath: '/dashboard',
  params: { code: 'TEST' },
  meta: { title: '数据看板' },
  matched: []
}
const navigations = []
const fixture = {
  id: 1,
  status: 1,
  hiveId: 1,
  hiveCode: 'BX001',
  hiveName: '一号蜂箱',
  apiaryName: '东区蜂场',
  location: '示例蜜源区',
  manager: '张三',
  sourceName: '槐花蜜源地',
  plant: '槐树',
  bloomStart: '2026-05-01',
  bloomEnd: '2026-06-01',
  username: 'admin',
  realName: '管理员',
  roleName: '管理员',
  roleCode: 'admin',
  deptName: '蜂场管理部',
  menuName: '基础档案',
  menuType: 'M',
  parentId: 0,
  path: '/base',
  icon: 'Files',
  children: [],
  dictName: '蜂箱状态',
  dictType: 'hive_status',
  dictLabel: '活跃',
  dictValue: '1',
  phone: '13800138000',
  gender: 0,
  email: 'test@example.com',
  productName: '槐花蜂蜜',
  productBatchNo: 'CP202605010001',
  productBatchId: 1,
  harvestId: 1,
  batchNo: 'HS202605010001',
  bottleCount: 240,
  produceDate: '2026-05-01',
  shelfLife: 18,
  spec: '500g',
  weight: 80,
  honeyType: '槐花蜜',
  harvestDate: '2026-05-01',
  moisture: 42,
  price: 60,
  itemName: '槐花蜂蜜',
  itemType: 2,
  stock: 240,
  warnStock: 20,
  unit: '瓶',
  amount: 14400,
  warehouseId: 2,
  whName: '成品库',
  whType: '成品库',
  outboundNo: 'CK202605010001',
  outboundDate: '2026-05-01',
  customerName: '示例客户',
  customerId: 1,
  items: [],
  totalQty: 24,
  totalAmount: 1440,
  purchaseNo: 'CG202605010001',
  purchaseDate: '2026-05-01',
  supplierName: '示例供应商',
  supplierType: '蜂具',
  supplierId: 1,
  expenseType: '人工',
  expenseDate: '2026-05-01',
  summary: '示例费用',
  traceCode: 'TRTEST',
  scanTime: '2026-05-01 10:00:00',
  createTime: '2026-05-01 10:00:00',
  lastScanTime: '2026-05-01 10:00:00',
  scanCount: 12,
  deviceType: 'Android',
  ip: '127.0.0.1',
  qcResult: 1,
  qcDate: '2026-05-01 10:00:00',
  qcItems: '水分',
  inspector: '张三',
  qcValue: '18%',
  warningType: '库存不足',
  level: 2
}
const calls = []
let customGet, customPost
function response(url) {
  if (url === '/auth/info')
    return {
      code: 200,
      data: { roles: ['admin'], permissions: ['*:*:*'], user: fixture }
    }
  if (url === '/user/profile') return { code: 200, data: fixture }
  if (url.startsWith('/trace/public/'))
    return {
      code: 200,
      data: {
        traceCode: 'TRTEST',
        product: fixture,
        harvest: fixture,
        source: fixture,
        hive: fixture,
        qcRecords: [fixture],
        medications: [],
        scanCount: 12
      }
    }
  if (url === '/dashboard/stats')
    return {
      code: 200,
      data: {
        hiveTotal: 128,
        activeHive: 116,
        monthHarvestWeight: 2860,
        monthOutboundBottles: 1200,
        warnCount: 4,
        todoCount: 8,
        scanTotal: 2380,
        totalWeight: 12980
      }
    }
  if (url.startsWith('/dashboard/')) return { code: 200, data: [] }
  if (url.startsWith('/report/'))
    return {
      code: 200,
      data: {
        rows: [
          { dim: '2026-05', weight: 80, batches: 1, qty: 24, amount: 1440 }
        ],
        totalStock: 240,
        totalValue: 14400,
        totalCost: 100,
        totalOutput: 1000,
        grossProfit: 900,
        costByType: [],
        outputByMonth: []
      }
    }
  if (url.includes('/safe'))
    return { code: 200, data: { safe: true, safeDate: '2026-05-01' } }
  if (url.includes('/export') || url.startsWith('/trace/qr/'))
    return new Blob(['test'])
  return { code: 200, data: [fixture], rows: [fixture], total: 1 }
}
const request = {
  get: async (...args) => {
    calls.push(['get', ...args])
    return customGet ? customGet(...args) : response(args[0])
  },
  post: async (...args) => {
    calls.push(['post', ...args])
    return customPost
      ? customPost(...args)
      : { code: 200, data: { token: 'test' } }
  },
  put: async (...args) => {
    calls.push(['put', ...args])
    return { code: 200 }
  },
  delete: async (...args) => {
    calls.push(['delete', ...args])
    return { code: 200 }
  }
}
const dependencies = {
  vue: Vue,
  pinia: Pinia,
  'element-plus': ElementPlus,
  '@element-plus/icons-vue': Icons,
  'vue-router': {
    useRoute: () => route,
    useRouter: () => ({ push: (path) => navigations.push(path) })
  },
  '@/utils/request': { default: request },
  echarts: { graphic: { LinearGradient: class {} } },
  '@/composables/useCharts': {
    useCharts: () => ({ renderChart() {}, clearCharts() {} }),
    chartPalette: ['#487c5b']
  }
}
function transform(source) {
  const ast = parse(source, { sourceType: 'module' })
  const edits = []
  for (const node of ast.program.body) {
    if (node.type === 'ImportDeclaration') {
      const id = node.source.value
      let text = ''
      for (const s of node.specifiers) {
        if (s.type === 'ImportNamespaceSpecifier')
          text += `const ${s.local.name}=__deps[${JSON.stringify(id)}];`
        else {
          const key =
            s.type === 'ImportDefaultSpecifier' ? 'default' : s.imported.name
          text += `const ${s.local.name}=__deps[${JSON.stringify(id)}][${JSON.stringify(key)}];`
        }
      }
      edits.push({ start: node.start, end: node.end, text })
    } else if (node.type === 'ExportNamedDeclaration')
      edits.push({ start: node.start, end: node.declaration.start, text: '' })
    else if (node.type === 'ExportDefaultDeclaration')
      edits.push({
        start: node.start,
        end: node.declaration.start,
        text: 'const __default = '
      })
  }
  for (const e of edits.sort((a, b) => b.start - a.start))
    source = source.slice(0, e.start) + e.text + source.slice(e.end)
  return source
}
function loadJs(relative, names, deps = dependencies) {
  const code = transform(fs.readFileSync(root + '/src/' + relative, 'utf8'))
  return new Function('__deps', code + ';return {' + names.join(',') + '}')(
    deps
  )
}
dependencies['@/utils/interaction'] = loadJs('utils/interaction.js', [
  'confirmAction',
  'validateForm'
])
dependencies['@/utils/menu'] = loadJs('utils/menu.js', [
  'resolveMenuPath',
  'collectMenuPaths'
])
dependencies['@/store/user'] = loadJs('store/user.js', ['useUserStore'])
dependencies['@/composables/useResponsiveLayout'] = loadJs(
  'composables/useResponsiveLayout.js',
  ['useResponsiveLayout']
)
const cache = new Map()
function loadSfc(relative) {
  if (cache.has(relative)) return cache.get(relative)
  const filename = root + '/src/' + relative
  const { descriptor } = parseSfc(fs.readFileSync(filename, 'utf8'), {
    filename
  })
  const script =
    descriptor.scriptSetup || descriptor.script
      ? compileScript(descriptor, { id: relative, genDefaultAs: '__component' })
          .content
      : 'const __component={}'
  const template = compileTemplate({
    source: descriptor.template.content,
    filename,
    id: relative,
    compilerOptions: {
      bindingMetadata: descriptor.scriptSetup
        ? compileScript(descriptor, { id: relative }).bindings
        : {}
    }
  })
  if (template.errors.length) throw Error(template.errors.join('\n'))
  const deps = { ...dependencies }
  for (const m of script.matchAll(/from\s+['"]([^'"]+\.vue)['"]/g)) {
    const name = m[1]
    const rel = name.startsWith('@/')
      ? name.slice(2)
      : path.posix.normalize(
          path.posix.join(path.posix.dirname(relative), name)
        )
    deps[name] = { default: loadSfc(rel) }
  }
  const component = new Function(
    '__deps',
    transform(script) +
      '\n' +
      transform(template.code) +
      '\nreturn {...__component,render}'
  )(deps)
  cache.set(relative, component)
  return component
}
let mounted = []
async function flush() {
  for (let i = 0; i < 5; i++) {
    await Promise.resolve()
    await Vue.nextTick()
  }
  await new Promise((r) => setTimeout(r, 20))
}
async function mount(relative) {
  const pinia = Pinia.createPinia()
  Pinia.setActivePinia(pinia)
  const host = document.createElement('div')
  document.body.append(host)
  const app = Vue.createApp(loadSfc(relative))
  app.use(pinia)
  app.use(ElementPlus)
  for (const [name, c] of Object.entries(Icons)) app.component(name, c)
  app.component('GlossaryTip', loadSfc('components/GlossaryTip/index.vue'))
  app.component('RouterLink', {
    setup(p, { slots }) {
      return () => Vue.h('a', {}, slots.default?.())
    }
  })
  app.component('RouterView', {
    setup(p, { slots }) {
      return () =>
        slots.default?.({ Component: { render: () => Vue.h('div') } })
    }
  })
  app.directive('hasPermi', () => {})
  const errors = []
  app.config.errorHandler = (e) => errors.push(e)
  app.config.warnHandler = () => {}
  app.mount(host)
  const state = app._instance.setupState
  const item = { app, host, state, errors }
  mounted.push(item)
  await flush()
  return item
}
function reset() {
  for (const { app, host } of mounted) {
    app.unmount()
    host.remove()
  }
  mounted = []
  customGet = customPost = null
  calls.length = 0
  navigations.length = 0
  localStorage.clear()
  document
    .querySelectorAll('.el-overlay,.el-message')
    .forEach((el) => el.remove())
}
function walk(dir) {
  return fs.readdirSync(dir, { withFileTypes: true }).flatMap((e) =>
    e.isDirectory()
      ? walk(path.join(dir, e.name))
      : e.name.endsWith('.vue')
        ? [
            path
              .relative(root + '/src', path.join(dir, e.name))
              .split(path.sep)
              .join('/')
          ]
        : []
  )
}
let passed = 0
const results = []
async function test(name, fn) {
  reset()
  await fn()
  passed++
  results.push(name)
  console.log('PASS', name)
}
;(async () => {
  for (const file of walk(root + '/src/views'))
    await test('页面挂载 ' + file, async () => {
      const x = await mount(file)
      assert.deepEqual(
        x.errors.map((e) => e.message),
        []
      )
      assert.ok(x.host.childNodes.length)
    })
  await test('列表失败结束加载；取消删除不发送请求', async () => {
    customGet = async () => {
      throw Error('network')
    }
    const { state } = await mount('views/base/apiary/index.vue')
    assert.equal(state.loading, false)
    const original = ElementPlus.ElMessageBox.confirm
    ElementPlus.ElMessageBox.confirm = async () => {
      throw 'cancel'
    }
    try {
      await state.remove(fixture)
      assert.equal(calls.filter((c) => c[0] === 'delete').length, 0)
    } finally {
      ElementPlus.ElMessageBox.confirm = original
    }
  })
  await test('无效表单不提交；重复提交只保存一次；失败保留输入', async () => {
    const { state } = await mount('views/base/apiary/index.vue')
    state.openAdd()
    await flush()
    await state.submit()
    assert.equal(calls.filter((c) => c[0] === 'post').length, 0)
    state.form.apiaryName = '新分区'
    let reject
    customPost = () => new Promise((_, r) => (reject = r))
    const saving = state.submit()
    await flush()
    await state.submit()
    assert.equal(calls.filter((c) => c[0] === 'post').length, 1)
    reject(Error('失败'))
    await saving
    assert.equal(state.form.apiaryName, '新分区')
    assert.equal(state.dialog.visible, true)
    assert.equal(state.saving, false)
  })
  await test('查询从第一页开始，保留查询字段', async () => {
    const { state } = await mount('views/base/hive/index.vue')
    state.query.pageNum = 8
    state.query.hiveCode = 'BX001'
    state.onSearch()
    await flush()
    assert.equal(state.query.pageNum, 1)
    assert.equal(state.query.hiveCode, 'BX001')
  })
  await test('字典编辑后新增使用 POST', async () => {
    const { state } = await mount('views/system/dict/index.vue')
    state.openType({ id: 42, dictName: 'old' })
    state.openType()
    assert.equal(state.typeForm.id, null)
    await state.saveType()
    assert.ok(calls.some((c) => c[0] === 'post' && c[1] === '/dict/type'))
    assert.equal(calls.filter((c) => c[0] === 'put').length, 0)
  })
  await test('登录校验、记住用户名和登录跳转保持原流程', async () => {
    const { state } = await mount('views/login/index.vue')
    await state.handleLogin()
    assert.equal(calls.filter((c) => c[0] === 'post').length, 0)
    state.loginForm.username = 'admin'
    state.loginForm.password = 'test'
    state.rememberMe = true
    await state.handleLogin()
    assert.equal(localStorage.getItem('bee_remember_user'), 'admin')
    assert.ok(navigations.includes('/'))
  })
  await test('溯源筛选读取 productBatchId；二维码 URL 释放', async () => {
    const { state, app } = await mount('views/trace/index.vue')
    assert.equal(state.availableProducts.length, 0)
    assert.equal(state.generatedCount, 1)
    const before = revoked.length
    await state.showQr(fixture)
    await state.showQr(fixture)
    assert.equal(revoked.length, before + 1)
    app.unmount()
    mounted = []
    assert.equal(revoked.length, before + 2)
  })
  await test('公开溯源缺少关联记录仍可展示', async () => {
    customGet = async () => ({
      code: 200,
      data: {
        product: fixture,
        harvest: null,
        source: null,
        hive: null,
        qcRecords: null,
        medications: null
      }
    })
    const { errors, host } = await mount('views/trace/public.vue')
    assert.deepEqual(errors, [])
    assert.ok(host.textContent.includes('该批次暂无用药记录'))
  })
  await test('报表旧响应不能覆盖最新维度数据', async () => {
    const { state } = await mount('views/report/index.vue')
    let resolveOld,
      i = 0
    customGet = () =>
      ++i === 1
        ? new Promise((r) => (resolveOld = r))
        : Promise.resolve({ data: { rows: [{ dim: '新蜜源', weight: 200 }] } })
    state.prodDim = 'hive'
    const old = state.loadProduction()
    state.prodDim = 'source'
    await state.loadProduction()
    resolveOld({ data: { rows: [{ dim: '旧蜂箱', weight: 100 }] } })
    await old
    assert.equal(state.prodRows[0].dim, '新蜜源')
  })
  await test('多级导航拼接与隐藏菜单过滤', async () => {
    const { resolveMenuPath, collectMenuPaths } = dependencies['@/utils/menu']
    assert.equal(resolveMenuPath('/base/hive', 'history'), '/base/hive/history')
    assert.equal(resolveMenuPath('/base', '/dashboard'), '/dashboard')
    assert.deepEqual(
      collectMenuPaths([
        {
          path: '/base',
          children: [{ path: 'hive' }, { path: 'hidden', hidden: true }]
        }
      ]),
      ['/base', '/base/hive']
    )
  })
  await test('退出登录移除动态路由和菜单', async () => {
    Pinia.setActivePinia(Pinia.createPinia())
    const store = dependencies['@/store/user'].useUserStore()
    let removed = 0
    store.setRouteRemovers([() => removed++])
    store.sidebarRoutes = [{ path: '/base' }]
    store.token = 'test'
    store.logout()
    assert.equal(removed, 1)
    assert.equal(store.routesLoaded, false)
    assert.equal(store.token, '')
    assert.deepEqual(store.sidebarRoutes, [])
  })

  await test('移动导航按钮与 Escape 键关闭菜单', async () => {
    const original = window.matchMedia
    window.matchMedia = (query) => ({
      matches: query.includes('767px'),
      addEventListener() {},
      removeEventListener() {}
    })
    try {
      const { state, host } = await mount('layout/index.vue')
      assert.equal(state.isMobile, true)
      assert.equal(state.mobileMenuOpen, false)
      state.toggleMenu()
      await flush()
      assert.ok(host.querySelector('.sidebar-mask'))
      window.dispatchEvent(
        new window.KeyboardEvent('keydown', { key: 'Escape' })
      )
      await flush()
      assert.equal(state.mobileMenuOpen, false)
      assert.equal(host.querySelector('.sidebar-mask'), null)
    } finally {
      window.matchMedia = original
    }
  })
  await test('已找到公开批次与无效码都有明确页面状态', async () => {
    customGet = async () => {
      throw Error('溯源码无效或不存在')
    }
    const { host, errors } = await mount('views/trace/public.vue')
    assert.deepEqual(errors, [])
    assert.ok(host.textContent.includes('溯源码无效或不存在'))
    assert.ok(host.querySelector('.trace-empty'))
  })
  await test('多级侧栏递归保留父级路径', async () => {
    const host = document.createElement('div')
    document.body.append(host)
    const pinia = Pinia.createPinia()
    Pinia.setActivePinia(pinia)
    const store = dependencies['@/store/user'].useUserStore()
    store.sidebarRoutes = [
      {
        path: '/base',
        meta: { title: '基础档案', icon: 'Files' },
        children: [
          {
            path: 'hive',
            meta: { title: '蜂箱档案' },
            children: [{ path: 'history', meta: { title: '历史记录' } }]
          }
        ]
      }
    ]
    const app = Vue.createApp(loadSfc('layout/index.vue'))
    app.use(pinia)
    app.use(ElementPlus)
    for (const [name, c] of Object.entries(Icons)) app.component(name, c)
    app.component('RouterLink', {
      setup(p, { slots }) {
        return () => Vue.h('a', {}, slots.default?.())
      }
    })
    app.component('RouterView', {
      setup(p, { slots }) {
        return () =>
          slots.default?.({ Component: { render: () => Vue.h('div') } })
      }
    })
    app.config.warnHandler = () => {}
    const errors = []
    app.config.errorHandler = (e) => errors.push(e)
    app.mount(host)
    mounted.push({ app, host })
    await flush()
    assert.deepEqual(errors, [])
    const sidebar = app._instance.subTree
    assert.ok(host.textContent.includes('基础档案'))
    const paths = dependencies['@/utils/menu'].collectMenuPaths(
      store.sidebarRoutes
    )
    assert.ok(paths.includes('/base/hive/history'))
  })
  await test('图表随容器变化缩放；离页清理并拒绝迟到响应', async () => {
    let observer,
      api,
      initCount = 0
    const chart = {
      setOption() {},
      resize() {
        this.resized = (this.resized || 0) + 1
      },
      dispose() {
        this.disposed = true
      },
      isDisposed() {
        return !!this.disposed
      }
    }
    const chartDeps = {
      ...dependencies,
      echarts: {
        init() {
          initCount++
          return chart
        }
      }
    }
    const original = global.ResizeObserver
    global.ResizeObserver = class extends original {
      constructor(cb) {
        super(cb)
        observer = this
      }
    }
    try {
      const { useCharts } = loadJs(
        'composables/useCharts.js',
        ['useCharts'],
        chartDeps
      )
      const host = document.createElement('div')
      document.body.append(host)
      const app = Vue.createApp({
        setup() {
          api = useCharts()
          return () => Vue.h('div')
        }
      })
      app.mount(host)
      const el = host.firstChild
      api.renderChart(el, { series: [{ data: [1] }] })
      api.renderChart(el, { series: [{ data: [2] }] })
      assert.equal(initCount, 1)
      observer.callback([
        { target: el, contentRect: { width: 600, height: 300 } }
      ])
      assert.equal(chart.resized, 1)
      app.unmount()
      assert.equal(chart.disposed, true)
      api.renderChart(el, { series: [{ data: [3] }] })
      assert.equal(initCount, 1)
      host.remove()
    } finally {
      global.ResizeObserver = original
    }
  })
  await test('公开请求不携带旧令牌，私有请求保留身份', async () => {
    const record = {},
      messages = [],
      redirects = [],
      logout = []
    const deps = {
      ...dependencies,
      axios: {
        default: {
          create() {
            return {
              interceptors: {
                request: {
                  use(fn) {
                    record.request = fn
                  }
                },
                response: {
                  use(fn, error) {
                    record.response = fn
                    record.error = error
                  }
                }
              }
            }
          }
        }
      },
      'element-plus': { ElMessage: { error: (m) => messages.push(m) } },
      '@/store/user': {
        useUserStore: () => ({
          token: 'expired',
          logout: () => logout.push(true)
        })
      },
      '@/router': { default: { push: (p) => redirects.push(p) } }
    }
    loadJs('utils/request.js', ['service'], deps)
    const pub = record.request({ url: '/trace/public/TR', headers: {} })
    const privateReq = record.request({ url: '/hive/list', headers: {} })
    assert.equal(pub.headers.Authorization, undefined)
    assert.equal(privateReq.headers.Authorization, 'Bearer expired')
    dependencies.__requestRecord = { record, messages, redirects, logout }
  })
  await test('登录错误保留原提示；公开错误不跳转管理端', async () => {
    const { record, messages, redirects, logout } = dependencies.__requestRecord
    await assert.rejects(
      record.response({
        data: { code: 401, msg: '用户名或密码错误' },
        config: { url: '/auth/login' }
      }),
      /用户名或密码错误/
    )
    await assert.rejects(
      record.response({
        data: { code: 401, msg: '无法访问' },
        config: { url: '/trace/public/TR' }
      })
    )
    assert.ok(messages.includes('用户名或密码错误'))
    assert.deepEqual(redirects, [])
    assert.deepEqual(logout, [])
  })
  await test('JSON 下载错误不会伪装成二维码或 Excel 文件', async () => {
    const { record, messages } = dependencies.__requestRecord
    await assert.rejects(
      record.response({
        data: new Blob([JSON.stringify({ code: 500, msg: '导出失败' })], {
          type: 'application/json'
        }),
        headers: { 'content-type': 'application/json' },
        config: { url: '/export/inventory', responseType: 'blob' }
      }),
      /导出失败/
    )
    assert.ok(messages.includes('导出失败'))
    const blob = new Blob(['test'], { type: 'application/octet-stream' })
    assert.equal(
      await record.response({
        data: blob,
        headers: { 'content-type': blob.type },
        config: { responseType: 'blob' }
      }),
      blob
    )
  })
  await test('空角色不反复初始化路由；公开溯源跳过身份初始化', async () => {
    Pinia.setActivePinia(Pinia.createPinia())
    const store = dependencies['@/store/user'].useUserStore()
    store.token = 'test'
    let guard
    const router = {
      beforeEach(fn) {
        guard = fn
      },
      addRoute() {
        return () => {}
      }
    }
    const guardDeps = {
      ...dependencies,
      '@/router': { default: router, constantRoutes: [] },
      '@/layout/index.vue': { default: {} },
      '@/components/ParentView.vue': { default: {} }
    }
    let source = fs
      .readFileSync(root + '/src/router/permission.js', 'utf8')
      .replace(/import.meta.glob\([^\n]+\)/, '({})')
    new Function('__deps', transform(source))(guardDeps)
    customGet = async (url) =>
      url === '/auth/info'
        ? { data: { roles: [], permissions: [], user: {} } }
        : { data: [] }
    const next = []
    await guard({ path: '/dashboard' }, {}, (x) => next.push(x))
    assert.equal(store.routesLoaded, true)
    assert.equal(calls.filter((c) => c[1] === '/auth/info').length, 1)
    await guard({ path: '/profile' }, {}, (x) => next.push(x))
    assert.equal(calls.filter((c) => c[1] === '/auth/info').length, 1)
    store.routesLoaded = false
    await guard({ path: '/trace/TR', name: 'PublicTrace' }, {}, (x) =>
      next.push(x)
    )
    assert.equal(calls.filter((c) => c[1] === '/auth/info').length, 1)
    store.logout()
  })
  await test('管理员通配权限也适用于数组指令', async () => {
    const source = fs.readFileSync(root + '/src/main.js', 'utf8')
    const ast = parse(source, { sourceType: 'module' })
    const stmt = ast.program.body.find(
      (n) =>
        n.type === 'ExpressionStatement' &&
        n.expression.type === 'CallExpression' &&
        n.expression.callee.type === 'MemberExpression' &&
        n.expression.callee.property.name === 'directive'
    )
    const method = stmt.expression.arguments[1].properties.find(
      (n) => n.key.name === 'mounted'
    )
    const mounted = new Function(
      'useUserStoreLocal',
      'return ({' + source.slice(method.start, method.end) + '}).mounted'
    )(() => ({ permissions: ['*:*:*'] }))
    const host = document.createElement('div'),
      button = document.createElement('button')
    host.append(button)
    mounted(button, { value: ['hive:add', 'hive:edit'] })
    assert.equal(host.children.length, 1)
  })

  for (const file of walk(root + '/src/views')) {
    const source = fs.readFileSync(root + '/src/' + file, 'utf8')
    for (const match of source.matchAll(/<el-dialog\b([\s\S]*?)>/g)) {
      const model = match[1].match(/v-model="([^"]+)"/)[1]
      await test('弹窗挂载与标签配置 ' + file + ' ' + model, async () => {
        assert.ok(match[1].includes('align-center'))
        const { state, errors } = await mount(file)
        if (
          state.openAdd &&
          ['dialog.visible', 'addDialog', 'editDialog'].includes(model)
        )
          await state.openAdd()
        if (state.detail !== undefined)
          state.detail = { ...fixture, flowNodes: [] }
        if (state.detailInfo !== undefined) state.detailInfo = { ...fixture }
        if (state.detailItems !== undefined)
          state.detailItems = [{ ...fixture }]
        if (state.currentType !== undefined) state.currentType = { ...fixture }
        if (model.includes('.')) {
          const [key, prop] = model.split('.')
          state[key].row = {
            ...fixture,
            content: '库存低于预警值，请安排补货。'
          }
          state[key].title = '布局检查'
          state[key][prop] = true
        } else state[model] = true
        await flush()
        const visible = [...document.querySelectorAll('.el-dialog')].filter(
          (el) => el.closest('.el-overlay').style.display !== 'none'
        )
        assert.equal(visible.length, 1)
        assert.ok(visible[0].classList.contains('is-align-center'))
        assert.ok(!source.match(/label-width="\d+px"/))
        assert.deepEqual(
          errors.map((e) => e.message),
          []
        )
      })
    }
  }
  await test('确认框保留420px上限；标签单行；弹窗正文滚动', async () => {
    const postcss = req('postcss')
    const tree = postcss.parse(
      fs.readFileSync(root + '/src/styles/index.css', 'utf8')
    )
    const declarations = (selector) => {
      const values = {}
      tree.walkRules((rule) => {
        if (rule.selector === selector && rule.parent.type === 'root')
          rule.walkDecls((d) => (values[d.prop] = d.value))
      })
      return values
    }
    assert.equal(
      declarations('.el-message-box')['max-width'],
      'var(--el-messagebox-width, 420px)'
    )
    assert.equal(
      declarations('.el-message-box').width,
      'min(var(--el-messagebox-width, 420px), calc(100vw - 32px))'
    )
    assert.equal(declarations('.el-form-item__label')['white-space'], 'nowrap')
    assert.equal(declarations('.el-dialog__body').overflow, 'auto')
    assert.equal(declarations('.el-dialog__body')['overflow-wrap'], 'normal')
    assert.equal(declarations('.el-dialog__footer').flex, 'none')
  })
  await test('窄屏详情切换单列并清理尺寸监听', async () => {
    const original = window.matchMedia
    let handler,
      removed = false
    const media = {
      matches: true,
      addEventListener(type, fn) {
        handler = fn
      },
      removeEventListener(type, fn) {
        removed = fn === handler
      }
    }
    window.matchMedia = () => media
    try {
      const item = await mount('views/stock/record/index.vue')
      assert.equal(item.state.compact, true)
      media.matches = false
      handler()
      await flush()
      assert.equal(item.state.compact, false)
      reset()
      assert.equal(removed, true)
    } finally {
      window.matchMedia = original
    }
  })
  await test('真实退出确认框显示明确按钮，取消后保留登录', async () => {
    const { state } = await mount('layout/index.vue')
    const pending = state.handleCommand('logout')
    await flush()
    const message = document.querySelector('.el-message-box')
    assert.ok(message)
    assert.ok(message.textContent.includes('退出登录'))
    const cancel = [...message.querySelectorAll('button')].find(
      (b) => b.textContent.trim() === '取消'
    )
    cancel.click()
    await pending
    assert.deepEqual(navigations, [])
    ElementPlus.ElMessageBox.close()
  })

  await test('密码重置提示框保留密码输入和取消行为', async () => {
    const pending = ElementPlus.ElMessageBox.prompt(
      '请输入新密码',
      '重置密码',
      {
        inputType: 'password',
        inputPattern: /.{6,}/,
        inputErrorMessage: '密码至少6位'
      }
    ).catch((error) => error)
    await flush()
    const box = document.querySelector('.el-message-box')
    assert.ok(box)
    assert.equal(box.querySelector('input').type, 'password')
    box
      .querySelector(
        '.el-message-box__btns .el-button:not(.el-button--primary)'
      )
      .click()
    assert.equal(await pending, 'cancel')
    ElementPlus.ElMessageBox.close()
  })

  reset()
  fs.writeFileSync(
    path.join(__dirname, 'regression-results.json'),
    JSON.stringify({ passed, tests: results }, null, 2)
  )
  console.log('TOTAL PASS', passed)
  dom.window.close()
  process.exit(0)
})().catch((error) => {
  console.error('FAIL', error.stack)
  reset()
  dom.window.close()
  process.exit(1)
})
