<template>
  <div class="app-container">
    <el-form class="search-form" inline :model="query">
      <el-form-item label="状态">
        <el-select
          v-model="query.status"
          placeholder="全部"
          clearable
          style="width: 120px"
        >
          <el-option label="待审核" :value="0" /><el-option
            label="已出库"
            :value="1"
          />
          <el-option label="已取消" :value="2" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="onSearch">查询</el-button>
      </el-form-item>
    </el-form>

    <el-card class="table-card" shadow="never">
      <div class="toolbar">
        <el-button type="primary" v-hasPermi="'outbound:add'" @click="openAdd">
          <el-icon><Plus /></el-icon>新增出库单
        </el-button>
      </div>
      <el-table :data="list" v-loading="loading" border stripe>
        <el-table-column prop="outboundNo" label="出库单号" width="170" />
        <el-table-column
          prop="customerName"
          label="客户"
          min-width="150"
          show-overflow-tooltip
        />
        <el-table-column
          prop="totalQty"
          label="总数量"
          width="90"
          align="right"
        />
        <el-table-column label="总金额" width="110" align="right">
          <template #default="{ row }"
            >¥{{ Number(row.totalAmount || 0).toFixed(2) }}</template
          >
        </el-table-column>
        <el-table-column
          prop="outboundDate"
          label="出库日期"
          width="110"
          align="center"
        />
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag
              :type="
                row.status === 1
                  ? 'success'
                  : row.status === 2
                    ? 'info'
                    : 'warning'
              "
              size="small"
            >
              {{
                row.status === 1
                  ? '已出库'
                  : row.status === 2
                    ? '已取消'
                    : '待审核'
              }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="审批节点" width="130" align="center">
          <template #default="{ row }">
            <el-tag
              v-if="row.status === 0"
              type="warning"
              size="small"
              effect="plain"
              >待场长审核</el-tag
            >
            <el-tag
              v-else-if="row.status === 1"
              type="success"
              size="small"
              effect="plain"
              >审批完成</el-tag
            >
            <el-tag v-else type="info" size="small" effect="plain"
              >已取消</el-tag
            >
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="viewDetail(row)"
              >明细</el-button
            >
            <el-button
              link
              type="success"
              v-hasPermi="'outbound:audit'"
              :disabled="row.status !== 0 || row.createBy === userStore.user.username"
              :title="row.createBy === userStore.user.username ? '请由其他有审核权限的人员审核' : '审核出库'"
              @click="audit(row)"
              >审核</el-button
            >
            <el-button
              link
              type="danger"
              v-hasPermi="'outbound:remove'"
              @click="remove(row)"
              >删除</el-button
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

    <!-- 新增出库单 -->
    <el-dialog
      align-center
      v-model="addDialog"
      title="新增出库单"
      width="800px"
      :close-on-click-modal="false"
    >
      <el-form :model="form" label-width="auto">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="客户" required>
              <el-select
                v-model="form.customerId"
                placeholder="请选择客户"
                style="width: 100%"
              >
                <el-option
                  v-for="c in customers"
                  :key="c.id"
                  :label="c.customerName"
                  :value="c.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="出库日期" required>
              <el-date-picker
                v-model="form.outboundDate"
                type="date"
                value-format="YYYY-MM-DD"
                placeholder="选择日期"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <div class="detail-section">
        <div class="detail-header">
          <span class="detail-title">出库明细</span>
          <el-button type="primary" size="small" @click="addItem">
            <el-icon><Plus /></el-icon>&nbsp;添加明细
          </el-button>
        </div>
        <el-table :data="form.items" border size="default" class="detail-table">
          <el-table-column label="产品批次" min-width="240">
            <template #default="{ row }">
              <el-select
                v-model="row.productBatchId"
                placeholder="请选择"
                size="default"
                style="width: 100%"
                @change="onProductChange(row)"
              >
                <el-option
                  v-for="p in products"
                  :key="p.id"
                  :label="
                    p.productBatchNo +
                    ' ' +
                    p.productName +
                    ' ' +
                    p.spec +
                    ' (库存' +
                    Number(p.availableBottles ?? 0) +
                    ')'
                  "
                  :value="p.id"
                  :disabled="Number(p.availableBottles ?? 0) <= 0 || Number(p.qcStatus) !== 1"
                />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="单位" width="70" align="center">
            <template #default="{ row }">{{ row.unit || '-' }}</template>
          </el-table-column>
          <el-table-column label="数量" width="130" align="center">
            <template #default="{ row }">
              <el-input-number
                v-model="row.quantity"
                :min="1"
                :max="row.maxQty || 9999"
                size="default"
                controls-position="right"
                style="width: 100%"
              />
            </template>
          </el-table-column>
          <el-table-column label="单价(元)" width="140" align="center">
            <template #default="{ row }">
              <el-input-number
                v-model="row.price"
                :min="0"
                :precision="2"
                :step="0.5"
                size="default"
                controls-position="right"
                style="width: 100%"
              />
            </template>
          </el-table-column>
          <el-table-column label="小计" width="100" align="right">
            <template #default="{ row }"
              >¥{{
                ((row.quantity || 0) * (row.price || 0)).toFixed(2)
              }}</template
            >
          </el-table-column>
          <el-table-column label="操作" width="70" align="center">
            <template #default="{ $index }">
              <el-button
                link
                type="danger"
                size="default"
                @click="removeItem($index)"
                >删除</el-button
              >
            </template>
          </el-table-column>
        </el-table>
        <div class="detail-footer" v-if="form.items.length">
          <span
            >共 <b>{{ form.items.length }}</b> 项明细，合计数量
            <b>{{ totalQty }}</b
            >，合计金额 <b style="color: #f56c6c">¥{{ totalAmount }}</b></span
          >
        </div>
      </div>

      <template #footer>
        <el-button @click="addDialog = false">取 消</el-button>
        <el-button type="primary" :loading="saving" @click="submit"
          >提 交</el-button
        >
      </template>
    </el-dialog>

    <!-- 明细 -->
    <el-dialog
      align-center
      v-model="detailDialog"
      title="出库单详情"
      width="720px"
    >
      <!-- 审批流程 -->
      <div class="audit-flow" v-if="detailInfo">
        <el-steps
          :active="
            detailInfo.status === 1 ? 2 : detailInfo.status === 2 ? 0 : 1
          "
          finish-status="success"
          align-center
          size="small"
        >
          <el-step
            title="创建提交"
            :description="
              detailInfo.createBy +
              '\n' +
              (detailInfo.createTime || '').slice(0, 16)
            "
          />
          <el-step
            title="场长审核"
            :description="
              detailInfo.status === 1
                ? (detailInfo.updateBy || '') +
                  '\n' +
                  (detailInfo.updateTime || '').slice(0, 16)
                : detailInfo.status === 2
                  ? '已取消'
                  : '待处理'
            "
          />
          <el-step
            title="出库完成"
            :description="detailInfo.status === 1 ? '已扣减库存' : ''"
          />
        </el-steps>
        <div class="flow-meta">
          <span
            >单号：<b>{{ detailInfo.outboundNo }}</b></span
          >
          <span>客户：{{ detailInfo.customerName }}</span>
          <span
            >合计：<b style="color: #f56c6c"
              >¥{{ Number(detailInfo.totalAmount || 0).toFixed(2) }}</b
            ></span
          >
        </div>
      </div>
      <el-divider />
      <el-table :data="detailItems" border stripe>
        <el-table-column
          prop="productBatchNo"
          label="产品批次"
          min-width="170"
        />
        <el-table-column prop="productName" label="产品" min-width="110" />
        <el-table-column prop="spec" label="规格" width="90" align="center" />
        <el-table-column label="单位" width="70" align="center">
          <template #default="{ row }">{{ row.unit || '-' }}</template>
        </el-table-column>
        <el-table-column
          prop="quantity"
          label="数量"
          width="80"
          align="right"
        />
        <el-table-column label="单价" width="100" align="right">
          <template #default="{ row }"
            >¥{{ Number(row.price || 0).toFixed(2) }}</template
          >
        </el-table-column>
        <el-table-column label="小计" width="110" align="right">
          <template #default="{ row }"
            >¥{{
              ((row.quantity || 0) * (row.price || 0)).toFixed(2)
            }}</template
          >
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="detailDialog = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { loadAllRows } from '@/utils/pagination'
import { confirmAction } from '@/utils/interaction'
import { ref, reactive, computed, onMounted } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { useUserStore } from '@/store/user'
const userStore = useUserStore()
import { ElMessage } from 'element-plus'

const list = ref([])
const total = ref(0)
const loading = ref(false)
const customers = ref([])
const products = ref([])
const query = reactive({ pageNum: 1, pageSize: 10, status: null })
const addDialog = ref(false)
const detailDialog = ref(false)
const form = reactive({ items: [] })
const detailItems = ref([])
const detailInfo = ref(null)

const totalQty = computed(() =>
  form.items.reduce((s, i) => s + (Number(i.quantity) || 0), 0)
)
const totalAmount = computed(() =>
  form.items
    .reduce((s, i) => s + (Number(i.quantity) || 0) * (Number(i.price) || 0), 0)
    .toFixed(2)
)

async function load() {
  loading.value = true
  try {
    const res = await request.get('/outbound/list', { params: query })
    list.value = res.rows
    total.value = res.total
  } catch (error) {
    // 请求拦截器已展示错误，保留当前列表。
  } finally {
    loading.value = false
  }
}
function openAdd() {
  Object.assign(form, {
    customerId: null,
    outboundDate: [new Date().getFullYear(), String(new Date().getMonth()+1).padStart(2,'0'), String(new Date().getDate()).padStart(2,'0')].join('-'),
    items: []
  })
  addItem()
  addDialog.value = true
}
function addItem() {
  form.items.push({
    productBatchId: null,
    quantity: 1,
    price: 0,
    maxQty: 9999,
    unit: ''
  })
}
function removeItem(idx) {
  form.items.splice(idx, 1)
}
function onProductChange(row) {
  const p = products.value.find((x) => x.id === row.productBatchId)
  if (p) {
    const stock = Number(p.availableBottles ?? 0)
    row.maxQty = stock > 0 ? stock : 1
    if (row.quantity > stock && stock > 0) row.quantity = stock
    // 自动带出产品定价
    if (p.price != null && Number(p.price) > 0) {
      row.price = Number(p.price)
    }
    row.unit = '瓶'
  }
}
async function submit() {
  if (saving.value) return
  saving.value = true
  try {
    if (!form.customerId) {
      ElMessage.warning('请选择客户')
      return
    }
    if (!form.outboundDate) {
      ElMessage.warning('请选择出库日期')
      return
    }
    if (!form.items.length) {
      ElMessage.warning('请添加出库明细')
      return
    }
    for (const item of form.items) {
      if (!item.productBatchId) {
        ElMessage.warning('请选择产品批次')
        return
      }
      if (!item.quantity || item.quantity <= 0) {
        ElMessage.warning('数量必须大于0')
        return
      }
    }
    await request.post('/outbound', form)
    ElMessage.success('出库单创建成功，待审核')
    addDialog.value = false
    load()
  } catch (error) {
    // 请求失败已由拦截器提示，表单内容保留供重试。
  } finally {
    saving.value = false
  }
}
async function viewDetail(row) {
  const res = await request.get(`/outbound/${row.id}`)
  detailInfo.value = res.data
  detailItems.value = res.data.items.map(item => ({ ...item, unit: '瓶' }))
  detailDialog.value = true
}
async function audit(row) {
  if (
    !(await confirmAction('确认审核通过？审核后将扣减库存', '提示', {
      type: 'warning'
    }))
  )
    return
  await request.put(`/outbound/audit/${row.id}`)
  ElMessage.success('审核成功，库存已扣减')
  load()
}
async function remove(row) {
  if (!(await confirmAction('确定删除该出库单？', '提示', { type: 'warning' })))
    return
  await request.delete(`/outbound/${row.id}`)
  ElMessage.success('删除成功')
  load()
}

onMounted(async () => {
  try {
    customers.value = (await request.get('/customer/list')).data
  } catch (e) {}
  try {
    products.value = await loadAllRows('/product/list')
  } catch (e) {}
  load()
})

const saving = ref(false)
function onSearch() {
  query.pageNum = 1
  load()
}
</script>
<style scoped>
.toolbar {
  margin-bottom: 14px;
}
.detail-section {
  margin-top: 4px;
}
.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}
.detail-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
}
.detail-table {
  width: 100%;
}
.detail-footer {
  margin-top: 12px;
  padding: 10px 14px;
  background: var(--el-fill-color-light);
  border-radius: 6px;
  font-size: 13px;
  color: var(--text-regular);
  text-align: right;
}
.audit-flow {
  padding: 8px 0;
}
.flow-meta {
  display: flex;
  gap: 24px;
  margin-top: 16px;
  padding: 10px 14px;
  background: var(--el-fill-color-light);
  border-radius: 6px;
  font-size: 13px;
  color: var(--text-regular);
  flex-wrap: wrap;
}
</style>

<style scoped>
.audit-flow :deep(.el-step__description) {
  white-space: pre-line;
}
@media (max-width: 767px) {
  .detail-header {
    flex-wrap: wrap;
    gap: 10px;
  }
  .flow-meta {
    gap: 10px 16px;
  }
}
</style>
