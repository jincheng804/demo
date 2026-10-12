<template>
  <div class="app-container">
    <el-card shadow="never" class="report-filter">
      <el-form inline>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            value-format="YYYY-MM-DD"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="reload">
            <el-icon><Search /></el-icon>查询
          </el-button>
          <el-button @click="resetRange">重置</el-button>
          <el-button
            type="success"
            v-hasPermi="'report:export'"
            :loading="exporting"
            @click="doExport"
          >
            <el-icon><Download /></el-icon>导出
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="table-card">
      <el-tabs v-model="activeTab" @tab-change="onTabChange">
        <!-- 产量 -->
        <el-tab-pane label="产量报表" name="production">
          <div v-if="activeTab === 'production'">
            <el-radio-group
              v-model="prodDim"
              @change="reload"
              style="margin-bottom: 12px"
            >
              <el-radio-button value="month">按月</el-radio-button>
              <el-radio-button value="hive">按蜂箱</el-radio-button>
              <el-radio-button value="source">按蜜源</el-radio-button>
            </el-radio-group>
            <div ref="prodChartRef" class="chart-box"></div>
            <el-table
              :data="prodRows"
              border
              stripe
              size="small"
              style="margin-top: 12px"
            >
              <el-table-column
                prop="dim"
                label="维度"
                min-width="140"
                show-overflow-tooltip
              />
              <el-table-column
                prop="weight"
                label="采收重量 (kg)"
                align="right"
              />
              <el-table-column prop="batches" label="采收批次" align="right" />
            </el-table>
          </div>
        </el-tab-pane>

        <!-- 销售 -->
        <el-tab-pane label="销售报表" name="sales">
          <div v-if="activeTab === 'sales'">
            <el-radio-group
              v-model="salesDim"
              @change="reload"
              style="margin-bottom: 12px"
            >
              <el-radio-button value="month">按月</el-radio-button>
              <el-radio-button value="customer">按客户</el-radio-button>
              <el-radio-button value="product">按产品</el-radio-button>
            </el-radio-group>
            <div ref="salesChartRef" class="chart-box"></div>
            <el-table
              :data="salesRows"
              border
              stripe
              size="small"
              style="margin-top: 12px"
            >
              <el-table-column
                prop="dim"
                label="维度"
                min-width="140"
                show-overflow-tooltip
              />
              <el-table-column prop="qty" label="销售数量 (瓶)" align="right" />
              <el-table-column
                prop="amount"
                label="销售金额 (元)"
                align="right"
              />
            </el-table>
          </div>
        </el-tab-pane>

        <!-- 库存 -->
        <el-tab-pane label="库存报表" name="inventory">
          <div v-if="activeTab === 'inventory'">
            <el-row
              :gutter="16"
              class="summary-row"
              style="margin-bottom: 12px"
            >
              <el-col :xs="24" :md="8">
                <el-stat
                  label="库存总数量"
                  :value="invSummary.totalStock"
                  suffix="件"
                  color="#487c5b"
                />
              </el-col>
              <el-col :xs="24" :md="8">
                <el-stat
                  label="库存总金额"
                  :value="invSummary.totalValue"
                  suffix="元"
                  color="#6c9566"
                />
              </el-col>
              <el-col :xs="24" :md="8">
                <el-stat
                  label="物料种类"
                  :value="invRows.length"
                  suffix="种"
                  color="#b78429"
                />
              </el-col>
            </el-row>
            <el-table
              :data="invRows"
              border
              stripe
              size="small"
              max-height="420"
            >
              <el-table-column prop="warehouse" label="仓库" width="110" />
              <el-table-column
                prop="itemName"
                label="物料名称"
                min-width="140"
                show-overflow-tooltip
              />
              <el-table-column prop="spec" label="规格" width="100" />
              <el-table-column prop="stock" label="库存数量" align="right" />
              <el-table-column
                prop="amount"
                label="库存金额 (元)"
                align="right"
              />
            </el-table>
          </div>
        </el-tab-pane>

        <!-- 成本毛利 -->
        <el-tab-pane label="成本毛利" name="profit">
          <div v-if="activeTab === 'profit'">
            <el-row
              :gutter="16"
              class="summary-row"
              style="margin-bottom: 12px"
            >
              <el-col :xs="24" :md="8"
                ><el-stat
                  label="总成本"
                  :value="profitData.totalCost"
                  suffix="元"
                  color="#c56e62"
              /></el-col>
              <el-col :xs="24" :md="8"
                ><el-stat
                  label="总产值"
                  :value="profitData.totalOutput"
                  suffix="元"
                  color="#6c9566"
              /></el-col>
              <el-col :xs="24" :md="8"
                ><el-stat
                  label="毛利润"
                  :value="profitData.grossProfit"
                  suffix="元"
                  color="#487c5b"
              /></el-col>
            </el-row>
            <el-row :gutter="16">
              <el-col :xs="24" :md="12"
                ><div ref="costChartRef" class="chart-box"></div
              ></el-col>
              <el-col :xs="24" :md="12"
                ><div ref="outputChartRef" class="chart-box"></div
              ></el-col>
            </el-row>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { useCharts, chartPalette } from '@/composables/useCharts'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

// 简易统计展示组件（局部）
import { defineComponent, h } from 'vue'
const ElStat = defineComponent({
  props: {
    label: String,
    value: [Number, String],
    suffix: String,
    color: String
  },
  setup(p) {
    return () =>
      h(
        'div',
        { class: 'mini-stat', style: { borderLeft: `4px solid ${p.color}` } },
        [
          h('div', { class: 'mini-stat-label' }, p.label),
          h(
            'div',
            { class: 'mini-stat-value', style: { color: p.color } },
            (p.value ?? 0) + ' ' + (p.suffix || '')
          )
        ]
      )
  }
})

const PALETTE = chartPalette
const { renderChart: makeChart, clearCharts: disposeCharts } = useCharts()
let requestVersion = 0
let alive = true
const activeTab = ref('production')
const dateRange = ref([])
const exporting = ref(false)

const prodDim = ref('month')
const prodRows = ref([])
const salesDim = ref('month')
const salesRows = ref([])
const invRows = ref([])
const invSummary = reactive({ totalStock: 0, totalValue: 0 })
const profitData = reactive({
  costByType: [],
  outputByMonth: [],
  totalCost: 0,
  totalOutput: 0,
  grossProfit: 0
})

const prodChartRef = ref()
const salesChartRef = ref()
const costChartRef = ref()
const outputChartRef = ref()

function params() {
  return {
    beginTime:
      dateRange.value && dateRange.value[0] ? dateRange.value[0] : undefined,
    endTime:
      dateRange.value && dateRange.value[1] ? dateRange.value[1] : undefined
  }
}
function axisTip() {
  return { tooltip: { trigger: 'axis', confine: true } }
}

async function loadProduction() {
  const version = ++requestVersion
  disposeCharts()
  const res = await request.get('/report/production', {
    params: { ...params(), dim: prodDim.value }
  })
  if (!alive || version !== requestVersion) return
  prodRows.value = (res.data && res.data.rows) || []
  await nextTick()
  makeChart(prodChartRef.value, {
    ...axisTip(),
    color: PALETTE,
    grid: { left: 50, right: 20, top: 30, bottom: 40 },
    xAxis: {
      type: 'category',
      data: prodRows.value.map((d) => d.dim),
      axisLabel: { rotate: prodDim.value === 'month' ? 0 : 30 }
    },
    yAxis: { type: 'value' },
    series: [
      {
        name: '采收重量',
        type: 'bar',
        barWidth: 22,
        data: prodRows.value.map((d) => d.weight),
        itemStyle: { color: '#487c5b', borderRadius: [4, 4, 0, 0] }
      }
    ]
  })
}

async function loadSales() {
  const version = ++requestVersion
  disposeCharts()
  const res = await request.get('/report/sales', {
    params: { ...params(), dim: salesDim.value }
  })
  if (!alive || version !== requestVersion) return
  salesRows.value = (res.data && res.data.rows) || []
  await nextTick()
  makeChart(salesChartRef.value, {
    ...axisTip(),
    color: PALETTE,
    grid: { left: 50, right: 50, top: 40, bottom: 40 },
    legend: { top: 0 },
    xAxis: {
      type: 'category',
      data: salesRows.value.map((d) => d.dim),
      axisLabel: { rotate: salesDim.value === 'month' ? 0 : 30 }
    },
    yAxis: [
      { type: 'value', name: '数量(瓶)' },
      { type: 'value', name: '金额(元)' }
    ],
    series: [
      {
        name: '销售数量',
        type: 'bar',
        barWidth: 18,
        data: salesRows.value.map((d) => d.qty),
        itemStyle: { color: '#7c9890', borderRadius: [4, 4, 0, 0] }
      },
      {
        name: '销售金额',
        type: 'line',
        smooth: true,
        yAxisIndex: 1,
        data: salesRows.value.map((d) => d.amount),
        itemStyle: { color: '#b97845' },
        lineStyle: { width: 3 }
      }
    ]
  })
}

async function loadInventory() {
  const version = ++requestVersion
  const res = await request.get('/report/inventory', { params: params() })
  if (!alive || version !== requestVersion) return
  invRows.value = (res.data && res.data.rows) || []
  invSummary.totalStock = (res.data && res.data.totalStock) || 0
  invSummary.totalValue = (res.data && res.data.totalValue) || 0
}

async function loadProfit() {
  const version = ++requestVersion
  disposeCharts()
  const res = await request.get('/report/profit', { params: params() })
  if (!alive || version !== requestVersion) return
  Object.assign(profitData, res.data || {})
  await nextTick()
  makeChart(costChartRef.value, {
    title: { text: '成本构成', left: 'center', textStyle: { fontSize: 14 } },
    tooltip: { trigger: 'item', formatter: '{b}: {c} 元 ({d}%)' },
    legend: { bottom: 0 },
    color: PALETTE,
    series: [
      {
        type: 'pie',
        radius: ['38%', '62%'],
        center: ['50%', '52%'],
        itemStyle: { borderColor: '#fff', borderWidth: 2 },
        data: profitData.costByType || []
      }
    ]
  })
  const om = profitData.outputByMonth || []
  makeChart(outputChartRef.value, {
    title: { text: '月度产值', left: 'center', textStyle: { fontSize: 14 } },
    ...axisTip(),
    grid: { left: 50, right: 20, top: 50, bottom: 30 },
    xAxis: { type: 'category', data: om.map((d) => d.month) },
    yAxis: { type: 'value' },
    series: [
      {
        type: 'bar',
        barWidth: 18,
        data: om.map((d) => d.amount),
        itemStyle: { color: '#6c9566', borderRadius: [4, 4, 0, 0] }
      }
    ]
  })
}

function onTabChange(tab) {
  loadTab(tab)
}
async function loadTab(tab) {
  disposeCharts()
  try {
    if (tab === 'production') await loadProduction()
    else if (tab === 'sales') await loadSales()
    else if (tab === 'inventory') await loadInventory()
    else if (tab === 'profit') await loadProfit()
  } catch (error) {
    /* 请求拦截器已提示 */
  }
}
function reload() {
  loadTab(activeTab.value)
}
function resetRange() {
  dateRange.value = []
  reload()
}

const exportMap = {
  production: { url: '/export/harvest', name: '产量报表.xlsx' },
  sales: { url: '/export/outbound', name: '销售报表.xlsx' },
  inventory: { url: '/export/inventory', name: '库存报表.xlsx' },
  profit: { url: '/expense/export', name: '成本费用.xlsx' }
}
async function doExport() {
  exporting.value = true
  try {
    const cfg = exportMap[activeTab.value]
    const res = await request.get(cfg.url, {
      params: params(),
      responseType: 'blob'
    })
    const blob = new Blob([res], {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
    })
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = cfg.name
    a.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } finally {
    exporting.value = false
  }
}

onMounted(() => loadTab('production'))
onBeforeUnmount(() => {
  alive = false
  requestVersion++
})
</script>

<style scoped>
.chart-box {
  height: 300px;
}
.summary-row {
  row-gap: 12px;
}
.mini-stat {
  background: #f7f9fc;
  border-radius: 6px;
  padding: 12px 16px;
}
.mini-stat-label {
  font-size: 13px;
  color: #909399;
}
.mini-stat-value {
  font-size: 22px;
  font-weight: 700;
  margin-top: 4px;
}
.report-filter {
  margin-bottom: 18px;
}
.report-filter :deep(.el-form-item) {
  margin-bottom: 0;
}
@media (max-width: 767px) {
  .report-filter :deep(.el-form-item) {
    margin-bottom: 12px;
  }
}
</style>
