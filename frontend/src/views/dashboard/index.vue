<template>
  <div class="app-container dashboard-page">
    <el-alert
      v-if="loadFailed"
      title="部分数据加载失败，请刷新页面重试"
      type="warning"
      :closable="false"
      show-icon
      class="load-alert"
    />
    <section class="overview-banner">
      <div class="overview-copy">
        <span class="overview-kicker">BEE FARM / 工作概览</span>
        <h2>{{ greeting }}，{{ userStore.user.realName || userStore.user.username || '蜂场伙伴' }}</h2>
        <p>从蜂群养护到产品流转，今日经营尽在掌握。</p>
        <div class="overview-status"><span :class="{ 'status-offline': loadFailed }" />{{ lastUpdated ? `最近更新 ${lastUpdated}` : '正在获取蜂场数据' }}</div>
      </div>
      <div class="overview-action">
        <span class="overview-mark" aria-hidden="true">蜂</span>
        <el-button :loading="refreshing" @click="loadAll"><el-icon><Refresh /></el-icon><span>刷新数据</span></el-button>
      </div>
    </section>
    <div class="section-heading"><h2>核心经营指标</h2><span>数据随业务记录更新</span></div>
    <!-- 顶部统计卡片 -->
    <div class="stat-row">
      <div class="stat-card" v-for="c in primaryCards" :key="c.key">
        <div class="stat-icon" :style="{ background: c.bg }">
          <el-icon :size="30" :color="c.color"
            ><component :is="c.icon"
          /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ formatNum(stats[c.key]) }}</div>
          <div class="stat-label">{{ c.label }}</div>
        </div>
      </div>
    </div>

    <div class="secondary-metrics">
      <div v-for="c in secondaryCards" :key="c.key" class="secondary-metric">
        <span class="secondary-label"><el-icon><component :is="c.icon" /></el-icon>{{ c.label }}</span>
        <strong>{{ formatNum(stats[c.key]) }}</strong>
      </div>
    </div>
    <div class="section-heading trend-heading"><h2>生产与流转趋势</h2><span>多维数据，辅助日常决策</span></div>
    <!-- 第一行图表：产量趋势 + 蜜种环形 -->
    <el-row :gutter="16" class="chart-row">
      <el-col :xs="24" :lg="15">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <div class="card-header">
              <span>近 12 月采收产量趋势 (kg)</span>
            </div>
          </template>
          <div ref="trendRef" class="chart-box"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="9">
        <el-card shadow="never" class="chart-card">
          <template #header
            ><div class="card-header"><span>蜜种产量占比</span></div></template
          >
          <div ref="honeyRef" class="chart-box"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 第二行：蜂箱状态 + 出库趋势 -->
    <el-row :gutter="16" class="chart-row">
      <el-col :xs="24" :lg="9">
        <el-card shadow="never" class="chart-card">
          <template #header
            ><div class="card-header"><span>蜂箱状态分布</span></div></template
          >
          <div ref="hiveRef" class="chart-box"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="15">
        <el-card shadow="never" class="chart-card">
          <template #header
            ><div class="card-header">
              <span>近 12 月出库趋势 (瓶 / 元)</span>
            </div></template
          >
          <div ref="outputRef" class="chart-box"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 第三行：扫码趋势 + 预警/待办 -->
    <el-row :gutter="16" class="chart-row">
      <el-col :xs="24" :lg="12">
        <el-card shadow="never" class="chart-card">
          <template #header
            ><div class="card-header">
              <span>近 30 天查看趋势</span>
            </div></template
          >
          <div ref="scanRef" class="chart-box"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="12">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <div class="card-header">
              <span>库存预警</span>
              <el-button
                v-if="canVisit('/warning')"
                link
                type="primary"
                @click="goWarning"
                >查看全部</el-button
              >
            </div>
          </template>
          <el-table :data="warnTop" size="small" border>
            <el-table-column
              prop="itemName"
              label="物料名称"
              min-width="120"
              show-overflow-tooltip
            />
            <el-table-column prop="spec" label="规格" width="90" />
            <el-table-column
              prop="stock"
              label="当前库存"
              width="90"
              align="right"
            />
            <el-table-column
              prop="warnStock"
              label="预警库存"
              width="90"
              align="right"
            />
            <el-table-column label="状态" width="90">
              <template #default="{ row }">
                <el-tag
                  :type="
                    Number(row.stock) <= Number(row.warnStock)
                      ? 'danger'
                      : 'warning'
                  "
                  size="small"
                >
                  {{
                    Number(row.stock) <= Number(row.warnStock) ? '短缺' : '偏低'
                  }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <!-- 快捷入口区块 -->
    <el-row :gutter="16" class="chart-row">
      <el-col :span="24">
        <el-card shadow="never">
          <template #header
            ><div class="card-header"><span>快捷入口</span></div></template
          >
          <div class="todo-grid">
            <button
              type="button"
              class="todo-item"
              v-for="t in todos"
              :key="t.label"
              :disabled="!canVisit(t.path)"
              :title="canVisit(t.path) ? t.desc : '当前角色无此菜单权限'"
              @click="router.push(t.path)"
            >
              <el-icon :size="26" :color="t.color"
                ><component :is="t.icon"
              /></el-icon>
              <div class="todo-body">
                <div class="todo-num">{{ t.value }}</div>
                <div class="todo-label">{{ t.label }}</div>
                <div class="todo-desc">{{ t.desc }}</div>
              </div>
              <el-icon class="todo-arrow"><ArrowRight /></el-icon>
            </button>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import echarts from '@/utils/charts'
import request from '@/utils/request'
import { useCharts, chartPalette } from '@/composables/useCharts'
import { useUserStore } from '@/store/user'
import { collectMenuPaths } from '@/utils/menu'

const router = useRouter()
const PALETTE = chartPalette
const { renderChart: reg } = useCharts()
const userStore = useUserStore()
const allowedPaths = computed(
  () => new Set(collectMenuPaths(userStore.sidebarRoutes))
)
const canVisit = (path) => allowedPaths.value.has(path)
const loadFailed = ref(false)
const refreshing = ref(false)
const lastUpdated = ref('')
const hour = new Date().getHours()
const greeting = hour < 12 ? '上午好' : hour < 18 ? '下午好' : '晚上好'

const stats = reactive({})
const warnTop = ref([])

const cards = [
  {
    key: 'hiveTotal',
    label: '蜂箱总数 (群)',
    icon: 'Grid',
    color: '#487c5b',
    bg: '#edf4ee'
  },
  {
    key: 'activeHive',
    label: '活跃蜂箱 (群)',
    icon: 'Sunny',
    color: '#6c9566',
    bg: '#eff5eb'
  },
  {
    key: 'monthHarvestWeight',
    label: '本月采收 (kg)',
    icon: 'Histogram',
    color: '#b78429',
    bg: '#fdf6ec'
  },
  {
    key: 'monthOutboundBottles',
    label: '本月出库 (瓶)',
    icon: 'Van',
    color: '#7c9890',
    bg: '#eef3f1'
  },
  {
    key: 'warnCount',
    label: '待处理预警',
    icon: 'Bell',
    color: '#c56e62',
    bg: '#fef0f0'
  },
  {
    key: 'todoCount',
    label: '待审批单据',
    icon: 'List',
    color: '#b97845',
    bg: '#fdf2ec'
  },
  {
    key: 'scanTotal',
    label: '溯源码查看总次数',
    icon: 'Cellphone',
    color: '#8b839e',
    bg: '#f5f0fa'
  },
  {
    key: 'totalWeight',
    label: '累计采收 (kg)',
    icon: 'Coin',
    color: '#487c5b',
    bg: '#e9f7f1'
  }
]

const primaryCards = cards.filter(c => ['hiveTotal', 'monthHarvestWeight', 'monthOutboundBottles', 'warnCount'].includes(c.key))
const secondaryCards = cards.filter(c => !primaryCards.includes(c))
const todos = ref([])

function formatNum(v) {
  if (v === null || v === undefined || v === '') return '—'
  return new Intl.NumberFormat('zh-CN', { maximumFractionDigits: 2 }).format(Number(v))
}

const trendRef = ref()
const honeyRef = ref()
const hiveRef = ref()
const outputRef = ref()
const scanRef = ref()

function baseAxis() {
  return {
    tooltip: { trigger: 'axis', confine: true },
    grid: { left: 50, right: 24, top: 30, bottom: 36 },
    color: PALETTE
  }
}

async function loadAll() {
  if (refreshing.value) return
  refreshing.value = true
  try {
  const results = await Promise.allSettled([
    request.get('/dashboard/stats'),
    request.get('/dashboard/trend'),
    request.get('/dashboard/honeyPie'),
    request.get('/dashboard/hiveStatus'),
    request.get('/dashboard/outputTrend'),
    request.get('/dashboard/scanTrend'),
    request.get('/dashboard/warnTop')
  ])
  loadFailed.value = results.some((result) => result.status === 'rejected')
  const [s, trend, honey, hive, output, scan, warn] = results.map((result) =>
    result.status === 'fulfilled' ? result.value : { data: [] }
  )
  Object.keys(stats).forEach(key => delete stats[key])
  Object.assign(stats, s.data || {})
  if (!loadFailed.value) lastUpdated.value = new Date().toLocaleTimeString('zh-CN', { hour12: false })
  warnTop.value = (warn.data || []).slice(0, 6)

  todos.value = [
    {
      label: '待处理预警',
      value: stats.warnCount ?? '—',
      icon: 'Bell',
      color: '#c56e62',
      path: '/warning',
      desc: '库存短缺/偏低预警'
    },
    {
      label: '待审批单据',
      value: stats.todoCount ?? '—',
      icon: 'List',
      color: '#b97845',
      path: '/sale/outbound',
      desc: '出库/采购/质检待审'
    },
    {
      label: '本月采收',
      value: stats.monthHarvestWeight ?? '—',
      icon: 'Histogram',
      color: '#b78429',
      path: '/produce/harvest',
      desc: '当月摇蜜采收重量(kg)'
    },
    {
      label: '本月出库',
      value: stats.monthOutboundBottles ?? '—',
      icon: 'Van',
      color: '#7c9890',
      path: '/sale/outbound',
      desc: '当月销售出库瓶数'
    },
    {
      label: '溯源码累计查看',
      value: stats.scanTotal ?? '—',
      icon: 'Cellphone',
      color: '#8b839e',
      path: '/tracecode/scanlog',
      desc: '消费者扫码查看溯源页总次数'
    }
  ]

  await nextTick()

  // 产量趋势 折线
  reg(trendRef.value, {
    ...baseAxis(),
    xAxis: {
      type: 'category',
      data: (trend.data || []).map((d) => d.month),
      boundaryGap: false,
      axisLine: { lineStyle: { color: '#dcdfe6' } }
    },
    yAxis: { type: 'value', splitLine: { lineStyle: { color: '#f0f2f5' } } },
    series: [
      {
        name: '采收重量',
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 7,
        data: (trend.data || []).map((d) => d.weight),
        lineStyle: { width: 3, color: '#487c5b' },
        itemStyle: { color: '#487c5b' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(72,124,91,0.25)' },
            { offset: 1, color: 'rgba(72,124,91,0.02)' }
          ])
        }
      }
    ]
  })

  // 蜜种环形
  reg(honeyRef.value, {
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { bottom: 0, icon: 'circle' },
    color: PALETTE,
    series: [
      {
        type: 'pie',
        radius: ['42%', '68%'],
        center: ['50%', '46%'],
        avoidLabelOverlap: true,
        itemStyle: { borderColor: '#fff', borderWidth: 2 },
        label: { show: true, position: 'inside', color: '#fff', formatter: p => `${Math.round(p.percent)}%` },
        data: honey.data || []
      }
    ]
  })

  // 蜂箱状态饼
  reg(hiveRef.value, {
    tooltip: { trigger: 'item', formatter: '{b}: {c} 群 ({d}%)' },
    legend: { bottom: 0, icon: 'circle' },
    color: ['#6c9566', '#b78429', '#c56e62', '#7c9890', '#8b839e'],
    series: [
      {
        type: 'pie',
        radius: '65%',
        center: ['50%', '46%'],
        itemStyle: { borderColor: '#fff', borderWidth: 2 },
        label: { formatter: '{b}\n{d}%' },
        data: hive.data || []
      }
    ]
  })

  // 出库趋势 柱+线 双轴
  const out = output.data || []
  reg(outputRef.value, {
    ...baseAxis(),
    xAxis: {
      type: 'category',
      data: out.map((d) => d.month),
      axisLine: { lineStyle: { color: '#dcdfe6' } }
    },
    yAxis: [
      {
        type: 'value',
        name: '出库(瓶)',
        splitLine: { lineStyle: { color: '#f0f2f5' } }
      },
      { type: 'value', name: '金额(元)', splitLine: { show: false } }
    ],
    series: [
      {
        name: '出库量',
        type: 'bar',
        barWidth: 18,
        data: out.map((d) => d.qty),
        itemStyle: { color: '#7c9890', borderRadius: [4, 4, 0, 0] }
      },
      {
        name: '出库金额',
        type: 'line',
        smooth: true,
        yAxisIndex: 1,
        data: out.map((d) => d.amount),
        itemStyle: { color: '#b97845' },
        lineStyle: { width: 3 }
      }
    ]
  })

  // 扫码趋势 面积
  const sc = scan.data || []
  reg(scanRef.value, {
    ...baseAxis(),
    xAxis: {
      type: 'category',
      data: sc.map((d) => d.date),
      boundaryGap: false,
      axisLine: { lineStyle: { color: '#dcdfe6' } }
    },
    yAxis: { type: 'value', splitLine: { lineStyle: { color: '#f0f2f5' } } },
    series: [
      {
        name: '查看次数',
        type: 'line',
        smooth: true,
        symbol: 'none',
        data: sc.map((d) => d.count),
        lineStyle: { width: 2, color: '#8b839e' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(139,131,158,0.3)' },
            { offset: 1, color: 'rgba(139,131,158,0.02)' }
          ])
        }
      }
    ]
  })
  } finally { refreshing.value = false }
}

function goWarning() {
  router.push('/warning')
}

onMounted(loadAll)
</script>

<style scoped>
.overview-banner { position:relative; display:flex; align-items:center; justify-content:space-between; overflow:hidden; padding:28px 32px; border-radius:16px; background:#1e4939; color:#fff; margin-bottom:26px; }
.overview-kicker { color:#c6d8c5; font-size:10px; letter-spacing:2px; }
.overview-copy h2 { font-size:26px; font-weight:600; margin:12px 0 10px; letter-spacing:0.2px; }
.overview-copy p { color:#d0dfd3; font-size:13px; line-height:1.7; margin:0; }
.overview-status { display:flex; align-items:center; gap:7px; color:#adc7b5; font-size:11px; margin-top:21px; }
.overview-status > span { width:5px; height:5px; border-radius:50%; background:#d7b669; }
.overview-status > .status-offline { background:#eda58a; }
.overview-action { display:flex; flex-direction:column; align-items:flex-end; gap:16px; }
.overview-action .el-button { color:#e7efea; border-color:#6a8978; background:#ffffff0a; }
.overview-action .el-button:hover { background:#ffffff16; }
.overview-mark { display:grid; place-items:center; width:88px; height:82px; clip-path:polygon(25% 0,75% 0,100% 50%,75% 100%,25% 100%,0 50%); background:#ffffff09; font-family:serif; font-size:40px; color:#e7cf94; }
.section-heading { display:flex; align-items:baseline; justify-content:space-between; gap:12px; margin-bottom:14px; }
.section-heading h2 { font-size:15px; font-weight:600; margin:0; }
.section-heading > span { font-size:11px; color:var(--text-secondary); }
.trend-heading { margin-top:26px; margin-bottom:-4px; }
.secondary-metrics { display:grid; grid-template-columns:repeat(4,minmax(0,1fr)); background:#fff; border:1px solid var(--border-light); border-radius:12px; padding:18px 0; margin-top:14px; }
.secondary-metric { padding:0 22px; border-right:1px solid var(--border-light); display:flex; align-items:center; justify-content:space-between; gap:12px; }
.secondary-metric:last-child { border:0; }
.secondary-label { color:var(--text-secondary); font-size:12px; display:flex; align-items:center; gap:8px; line-height:1.5; }
.secondary-metric strong { font-size:20px; font-weight:600; font-variant-numeric:tabular-nums; }
@media(max-width:1200px) { .secondary-metric { flex-direction:column; align-items:flex-start; padding:0 16px; gap:8px; } }
@media(max-width:767px) { .overview-banner { padding:22px; } .overview-copy h2 { font-size:21px; } .overview-copy p { max-width:190px; } .overview-mark { display:none; } .overview-action { align-self:flex-start; margin-top:22px; } .overview-action .el-button { padding:8px; } .secondary-metrics { grid-template-columns:repeat(2,minmax(0,1fr)); gap:20px 0; } .secondary-metric:nth-child(2) { border:0; } .section-heading > span { display:none; } }

.load-alert {
  margin-bottom: 18px;
}
.stat-row {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}
.stat-card {
  background: #fff;
  border: 1px solid var(--border-light);
  border-radius: 14px;
  padding: 22px 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: var(--shadow-sm);
  min-width: 0;
}
.stat-icon {
  display: grid;
  place-items: center;
  width: 46px;
  height: 46px;
  border-radius: 12px;
  flex-shrink: 0;
}
.stat-icon :deep(.el-icon) {
  font-size: 23px !important;
}
.stat-info {
  min-width: 0;
}
.stat-value {
  font-size: 26px;
  font-weight: 650;
  line-height: 1.2;
  letter-spacing: -0.6px;
  overflow-wrap: anywhere;
  font-variant-numeric: tabular-nums;
}
.stat-label {
  font-size: 12px;
  color: var(--text-secondary);
  margin-top: 8px;
  line-height: 1.5;
}
.chart-row {
  margin-top: 18px;
  row-gap: 18px;
}
.chart-card {
  height: 100%;
}
.chart-card :deep(.el-card__body) {
  padding-bottom: 12px;
}
.chart-box {
  height: 286px;
}
.card-header {
  font-size: 14px;
}
.todo-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}
.todo-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 20px;
  border: 1px solid var(--border-light);
  border-radius: 10px;
  background: #fafbf7;
  text-align: left;
  cursor: pointer;
  color: inherit;
  transition:
    border-color 0.2s,
    background 0.2s;
  min-width: 0;
}
.todo-item:not(:disabled):hover {
  border-color: #d5ab73;
  background: #faf3e7;
}
.todo-item:disabled {
  cursor: default;
}
.todo-item:disabled .todo-arrow {
  display: none;
}
.todo-body {
  min-width: 0;
}
.todo-num {
  font-size: 21px;
  font-weight: 650;
}
.todo-label {
  font-size: 12px;
  color: var(--text-regular);
  margin-top: 6px;
}
.todo-desc {
  font-size: 11px;
  color: var(--text-secondary);
  margin-top: 6px;
  line-height: 1.6;
}
.todo-arrow {
  margin-left: auto;
  color: #94a18c;
}
@media (max-width: 1250px) {
  .stat-card {
    padding: 18px 14px;
    gap: 10px;
  }
  .stat-icon {
    width: 38px;
    height: 38px;
  }
  .stat-value {
    font-size: 24px;
  }
  .todo-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
@media (max-width: 767px) {
  .stat-row {
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 10px;
  }
  .stat-card {
    align-items: flex-start;
    flex-direction: column;
    padding: 16px;
    gap: 12px;
  }
  .stat-label {
    margin-top: 6px;
  }
  .chart-box {
    height: 260px;
  }
  .todo-grid {
    grid-template-columns: 1fr;
  }
}
</style>
