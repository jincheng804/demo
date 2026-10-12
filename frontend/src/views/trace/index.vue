<template>
  <div class="app-container">
    <!-- 溯源分析 -->
    <el-row :gutter="16" class="trace-charts">
      <el-col :xs="24" :lg="14">
        <el-card shadow="never" class="chart-card">
          <template #header
            ><div class="card-header">
              <span>近 30 天扫码趋势</span>
            </div></template
          >
          <div ref="scanTrendRef" class="chart-box"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="10">
        <el-card shadow="never" class="chart-card">
          <template #header
            ><div class="card-header"><span>扫码设备分布</span></div></template
          >
          <div ref="deviceRef" class="chart-box"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 溯源码管理 -->
    <el-card shadow="never" class="table-card">
      <template #header
        ><div class="card-header"><span>溯源码管理</span></div></template
      >
      <el-form class="search-form" inline :model="query">
        <el-form-item label="溯源码">
          <el-input
            v-model="query.traceCode"
            clearable
            @keyup.enter="onSearch"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="onSearch">查询</el-button>
        </el-form-item>
      </el-form>
      <div class="toolbar">
        <el-button
          type="primary"
          v-hasPermi="'trace:add'"
          @click="openGenerate"
        >
          <el-icon><Plus /></el-icon>生成溯源码
        </el-button>
      </div>
      <el-table :data="list" v-loading="loading" border stripe>
        <el-table-column prop="traceCode" label="溯源码" width="190" />
        <el-table-column prop="productBatchNo" label="产品批次" width="160" />
        <el-table-column prop="productName" label="产品名称" min-width="130" />
        <el-table-column prop="spec" label="规格" width="90" />
        <el-table-column prop="scanCount" width="100">
          <template #header
            ><GlossaryTip
              term="查看次数"
              explanation="消费者扫码访问该溯源码页面的累计次数，反映产品被关注程度"
          /></template>
        </el-table-column>
        <el-table-column prop="lastScanTime" min-width="160">
          <template #header
            ><GlossaryTip
              term="最后查看时间"
              explanation="最近一次消费者扫码访问该溯源码的时间"
          /></template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="showQr(row)"
              >二维码</el-button
            >
            <el-button link type="success" @click="goTrace(row)"
              >溯源详情</el-button
            >
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrapper">
        <el-pagination
          background
          layout="total, prev, pager, next"
          :total="total"
          v-model:current-page="query.pageNum"
          v-model:page-size="query.pageSize"
          @current-change="load"
        />
      </div>
    </el-card>

    <!-- 扫码访问明细 -->
    <el-card shadow="never" class="table-card analysis-card">
      <template #header
        ><div class="card-header"><span>扫码访问明细</span></div></template
      >
      <el-form inline :model="scanQuery">
        <el-form-item label="溯源码">
          <el-input
            v-model="scanQuery.traceCode"
            clearable
            placeholder="溯源码"
            @keyup.enter="onScanSearch"
          />
        </el-form-item>
        <el-form-item label="设备类型">
          <el-select
            v-model="scanQuery.deviceType"
            placeholder="全部"
            clearable
            style="width: 130px"
          >
            <el-option
              v-for="d in deviceTypes"
              :key="d"
              :label="d"
              :value="d"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="扫码时间">
          <el-date-picker
            v-model="scanRange"
            type="daterange"
            value-format="YYYY-MM-DD"
            range-separator="至"
            start-placeholder="开始"
            end-placeholder="结束"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="onScanSearch">查询</el-button>
          <el-button @click="resetScan">重置</el-button>
        </el-form-item>
      </el-form>
      <el-table
        :data="scanList"
        v-loading="scanLoading"
        border
        stripe
        size="small"
      >
        <el-table-column prop="traceCode" label="溯源码" width="180" />
        <el-table-column prop="deviceType" label="设备类型" width="110">
          <template #default="{ row }">
            <el-tag size="small" effect="plain">{{
              row.deviceType || '未知'
            }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="location" label="扫码地区" min-width="120">
          <template #default="{ row }">{{
            row.location || row.ip || '—'
          }}</template>
        </el-table-column>
        <el-table-column prop="ip" label="IP 地址" width="140" />
        <el-table-column prop="scanTime" label="扫码时间" width="170" />
      </el-table>
      <div class="pagination-wrapper">
        <el-pagination
          background
          layout="total, prev, pager, next"
          :total="scanTotal"
          v-model:current-page="scanQuery.pageNum"
          v-model:page-size="scanQuery.pageSize"
          @current-change="loadScan"
        />
      </div>
    </el-card>

    <!-- 选择产品批次 -->
    <el-dialog
      align-center
      v-model="genDialog"
      title="生成溯源码"
      width="480px"
    >
      <el-form label-width="auto">
        <el-form-item label="产品批次">
          <el-select
            v-model="selectedBatch"
            style="width: 100%"
            filterable
            placeholder="请选择未生成溯源码的产品批次"
          >
            <el-option
              v-for="p in availableProducts"
              :key="p.id"
              :label="p.productBatchNo + ' ' + p.productName + ' ' + p.spec"
              :value="p.id"
            />
          </el-select>
          <div v-if="generatedCount > 0" class="gen-tip">
            另有 {{ generatedCount }} 个产品批次已生成溯源码，不在下拉列表中显示
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="genDialog = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="doGenerate"
          >生成</el-button
        >
      </template>
    </el-dialog>

    <!-- 二维码预览 -->
    <el-dialog
      align-center
      v-model="qrDialog"
      title="溯源二维码"
      width="360px"
      @closed="clearQr"
    >
      <div style="text-align: center">
        <img
          alt="产品溯源二维码"
          :src="qrSrc"
          style="width: 280px; height: 280px"
        />
        <p style="margin-top: 10px; color: #909399">扫码可查看完整溯源信息</p>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { loadAllRows } from '@/utils/pagination'
import {
  ref,
  reactive,
  computed,
  onMounted,
  onBeforeUnmount,
  nextTick
} from 'vue'
import { useRouter } from 'vue-router'
import echarts from '@/utils/charts'
import request from '@/utils/request'
import { useCharts, chartPalette } from '@/composables/useCharts'
import { ElMessage } from 'element-plus'

const router = useRouter()
const PALETTE = chartPalette
const { renderChart } = useCharts()

// ===== 溯源码管理（原有） =====
const list = ref([])
const total = ref(0)
const loading = ref(false)
const products = ref([])
const generatedBatchIds = ref(new Set())
const query = reactive({ pageNum: 1, pageSize: 10, traceCode: '' })
const genDialog = ref(false)
const qrDialog = ref(false)
const selectedBatch = ref(null)
const qrSrc = ref('')

// 未生成溯源码的产品批次
const availableProducts = computed(() =>
  products.value.filter((p) => !generatedBatchIds.value.has(p.id))
)
// 已生成溯源码的产品批次数量
const generatedCount = computed(
  () => products.value.filter((p) => generatedBatchIds.value.has(p.id)).length
)

async function loadProducts() {
  products.value = await loadAllRows('/product/list')
  // 获取所有已生成溯源码的产品批次ID
  const allTraces = await loadAllRows('/trace/list')
  const ids = new Set()
  allTraces.forEach((t) => {
    if (t.productBatchId) ids.add(t.productBatchId)
  })
  generatedBatchIds.value = ids
}

async function load() {
  loading.value = true
  try {
    const res = await request.get('/trace/list', { params: query })
    list.value = res.rows
    total.value = res.total
  } catch (error) {
    // 请求拦截器已展示错误，保留当前列表。
  } finally {
    loading.value = false
  }
}
function openGenerate() {
  selectedBatch.value = null
  genDialog.value = true
}
async function doGenerate() {
  if (saving.value) return
  saving.value = true
  try {
    if (!selectedBatch.value) {
      ElMessage.warning('请选择产品批次')
      return
    }
    await request.post(`/trace/generate/${selectedBatch.value}`)
    ElMessage.success('溯源码生成成功')
    genDialog.value = false
    load()
    loadProducts()
  } catch (error) {
    // 请求失败已由拦截器提示，表单内容保留供重试。
  } finally {
    saving.value = false
  }
}
async function showQr(row) {
  const blob = await request.get(`/trace/qr/${row.traceCode}`, {
    responseType: 'blob'
  })
  clearQr()
  qrSrc.value = URL.createObjectURL(blob)
  qrDialog.value = true
}
function clearQr() {
  if (qrSrc.value) URL.revokeObjectURL(qrSrc.value)
  qrSrc.value = ''
}
function goTrace(row) {
  window.open(
    `/trace/${encodeURIComponent(row.traceCode)}`,
    '_blank',
    'noopener,noreferrer'
  )
}

// ===== 溯源分析 =====
const scanList = ref([])
const scanTotal = ref(0)
const scanLoading = ref(false)
const scanQuery = reactive({
  pageNum: 1,
  pageSize: 10,
  traceCode: '',
  deviceType: ''
})
const scanRange = ref([])
const deviceTypes = ['Android', 'iPhone', 'iPad', '手机', '微信', '浏览器']

const scanTrendRef = ref()
const deviceRef = ref()

function scanParams() {
  return {
    ...scanQuery,
    beginTime:
      scanRange.value && scanRange.value[0] ? scanRange.value[0] : undefined,
    endTime:
      scanRange.value && scanRange.value[1] ? scanRange.value[1] : undefined
  }
}
async function loadScan() {
  scanLoading.value = true
  try {
    const res = await request.get('/scanlog/list', { params: scanParams() })
    scanList.value = res.rows || []
    scanTotal.value = res.total || 0
  } catch (error) {
    /* 请求拦截器已提示 */
  } finally {
    scanLoading.value = false
  }
}
function onScanSearch() {
  scanQuery.pageNum = 1
  loadScan()
}
function resetScan() {
  Object.assign(scanQuery, { pageNum: 1, traceCode: '', deviceType: '' })
  scanRange.value = []
  loadScan()
}

async function loadAnalysis() {
  // 扫码趋势（复用看板近30天数据）
  const [trendRes, allRes] = await Promise.all([
    request.get('/dashboard/scanTrend'),
    request.get('/scanlog/deviceStats')
  ])
  const trend = trendRes.data || []
  // 在数据库汇总完整日志，避免按第一页推断整体比例。
  const deviceData = allRes.data || []

  await nextTick()
  renderChart(scanTrendRef.value, {
    tooltip: { trigger: 'axis' },
    grid: { left: 40, right: 20, top: 20, bottom: 30 },
    xAxis: {
      type: 'category',
      data: trend.map((d) => d.date),
      boundaryGap: false
    },
    yAxis: { type: 'value', splitLine: { lineStyle: { color: '#f0f2f5' } } },
    series: [
      {
        name: '查看次数',
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 5,
        data: trend.map((d) => d.count),
        lineStyle: { width: 2, color: '#487c5b' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(72,124,91,0.25)' },
            { offset: 1, color: 'rgba(72,124,91,0.02)' }
          ])
        }
      }
    ]
  })
  renderChart(deviceRef.value, {
    tooltip: { trigger: 'item', formatter: '{b}: {c} 次 ({d}%)' },
    legend: { bottom: 0, icon: 'circle' },
    color: PALETTE,
    series: [
      {
        type: 'pie',
        radius: ['40%', '65%'],
        center: ['50%', '46%'],
        itemStyle: { borderColor: '#fff', borderWidth: 2 },
        label: { formatter: '{b} {d}%' },
        data: deviceData
      }
    ]
  })
}

onMounted(() => {
  loadProducts().catch(() => {})
  load()
  loadScan().catch(() => {})
  loadAnalysis().catch(() => {})
})
onBeforeUnmount(clearQr)

const saving = ref(false)
function onSearch() {
  query.pageNum = 1
  load()
}
</script>
<style scoped>
.chart-box {
  height: 280px;
}
.trace-charts {
  row-gap: 18px;
  margin-bottom: 18px;
}
.analysis-card {
  margin-top: 16px;
}
.toolbar {
  margin-bottom: 12px;
}
.card-header {
  font-weight: 600;
}
.gen-tip {
  font-size: 12px;
  color: #e6a23c;
  margin-top: 6px;
  line-height: 1.5;
}
</style>
