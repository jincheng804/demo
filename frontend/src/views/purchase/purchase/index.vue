<template>
  <div class="app-container">
    <el-form class="search-form" inline :model="query">
      <el-form-item label="采购单号">
        <el-input
          v-model="query.purchaseNo"
          placeholder="请输入"
          clearable
          @keyup.enter="onSearch"
        />
      </el-form-item>
      <el-form-item label="供应商">
        <el-select
          v-model="query.supplierId"
          placeholder="全部"
          clearable
          style="width: 160px"
        >
          <el-option
            v-for="s in suppliers"
            :key="s.id"
            :label="s.supplierName"
            :value="s.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="状态">
        <el-select
          v-model="query.status"
          placeholder="全部"
          clearable
          style="width: 120px"
        >
          <el-option label="待审核" :value="0" /><el-option
            label="已入库"
            :value="1"
          />
          <el-option label="已取消" :value="2" />
        </el-select>
      </el-form-item>
      <el-form-item label="采购日期">
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          value-format="YYYY-MM-DD"
          range-separator="至"
          start-placeholder="开始"
          end-placeholder="结束"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="onSearch">查询</el-button>
        <el-button @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-card class="table-card">
      <div class="toolbar">
        <el-button type="primary" v-hasPermi="'purchase:add'" @click="openAdd">
          <el-icon><Plus /></el-icon>新增采购单
        </el-button>
        <el-button
          type="success"
          v-hasPermi="'purchase:export'"
          @click="exportData"
        >
          <el-icon><Download /></el-icon>导出
        </el-button>
      </div>
      <el-table :data="list" v-loading="loading" border stripe>
        <el-table-column prop="purchaseNo" label="采购单号" width="160" />
        <el-table-column prop="supplierName" label="供应商" min-width="160" />
        <el-table-column prop="warehouseName" label="入库仓库" width="110" />
        <el-table-column prop="purchaseDate" label="采购日期" width="110" />
        <el-table-column
          prop="totalAmount"
          label="总金额(元)"
          width="120"
          align="right"
        >
          <template #default="{ row }">{{
            formatAmount(row.totalAmount)
          }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag
              :type="
                row.status === 1
                  ? 'success'
                  : row.status === 2
                    ? 'info'
                    : 'warning'
              "
            >
              {{
                row.status === 1
                  ? '已入库'
                  : row.status === 2
                    ? '已取消'
                    : '待审核'
              }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="230" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="viewDetail(row)"
              >明细</el-button
            >
            <el-button
              link
              type="primary"
              v-hasPermi="'purchase:edit'"
              :disabled="row.status === 1"
              @click="openEdit(row)"
              >修改</el-button
            >
            <el-button
              link
              type="success"
              v-hasPermi="'purchase:audit'"
              :disabled="row.status !== 0"
              @click="audit(row)"
              >审核</el-button
            >
            <el-button
              link
              type="danger"
              v-hasPermi="'purchase:remove'"
              @click="remove(row)"
              >删除</el-button
            >
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrapper">
        <el-pagination
          background
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          v-model:current-page="query.pageNum"
          v-model:page-size="query.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          @current-change="load"
          @size-change="onSearch"
        />
      </div>
    </el-card>

    <!-- 新增/编辑采购单 -->
    <el-dialog
      align-center
      v-model="editDialog"
      :title="editTitle"
      width="860px"
      @closed="onEditClosed"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="auto">
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="供应商" prop="supplierId">
              <el-select v-model="form.supplierId" style="width: 100%">
                <el-option
                  v-for="s in suppliers"
                  :key="s.id"
                  :label="s.supplierName"
                  :value="s.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="入库仓库" prop="warehouseId">
              <el-select v-model="form.warehouseId" style="width: 100%">
                <el-option
                  v-for="w in warehouses"
                  :key="w.id"
                  :label="w.whName"
                  :value="w.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="采购日期" prop="purchaseDate">
              <el-date-picker
                v-model="form.purchaseDate"
                type="date"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="备注">
              <el-input v-model="form.remark" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <el-divider content-position="left">采购明细</el-divider>
      <el-button type="primary" plain size="small" @click="addItem">
        <el-icon><Plus /></el-icon>添加明细
      </el-button>
      <el-table :data="form.items" border style="margin-top: 10px">
        <el-table-column label="物料名称" min-width="150">
          <template #default="{ row }">
            <el-input
              v-model="row.materialName"
              size="small"
              placeholder="如：蜂箱/白糖"
            />
          </template>
        </el-table-column>
        <el-table-column label="规格" width="120">
          <template #default="{ row }">
            <el-input
              v-model="row.spec"
              size="small"
              placeholder="如：个/50kg"
            />
          </template>
        </el-table-column>
        <el-table-column label="数量" width="120">
          <template #default="{ row }">
            <el-input-number
              v-model="row.quantity"
              :min="1"
              :precision="0"
              size="small"
              controls-position="right"
              @change="calcAmount(row)"
              style="width: 100%"
            />
          </template>
        </el-table-column>
        <el-table-column label="单位" width="90">
          <template #default="{ row }">
            <el-select v-model="row.unit" size="small">
              <el-option label="个" value="个" /><el-option
                label="kg"
                value="kg"
              />
              <el-option label="袋" value="袋" /><el-option
                label="箱"
                value="箱"
              />
              <el-option label="瓶" value="瓶" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="单价(元)" width="120">
          <template #default="{ row }">
            <el-input-number
              v-model="row.price"
              :min="0"
              :precision="2"
              size="small"
              controls-position="right"
              @change="calcAmount(row)"
              style="width: 100%"
            />
          </template>
        </el-table-column>
        <el-table-column label="金额(元)" width="110" align="right">
          <template #default="{ row }">{{ formatAmount(row.amount) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="60">
          <template #default="{ $index }">
            <el-button
              link
              type="danger"
              size="small"
              @click="form.items.splice($index, 1)"
              >删</el-button
            >
          </template>
        </el-table-column>
      </el-table>
      <div style="margin-top: 10px; text-align: right; font-weight: bold">
        合计金额：{{ formatAmount(formTotal) }} 元
      </div>

      <template #footer>
        <el-button @click="editDialog = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submit"
          >提交</el-button
        >
      </template>
    </el-dialog>

    <!-- 查看明细 -->
    <el-dialog
      align-center
      v-model="detailDialog"
      title="采购明细"
      width="720px"
    >
      <el-descriptions
        :column="compact ? 1 : 2"
        border
        size="small"
        style="margin-bottom: 12px"
      >
        <el-descriptions-item label="采购单号">{{
          detail.purchaseNo
        }}</el-descriptions-item>
        <el-descriptions-item label="供应商">{{
          detail.supplierName
        }}</el-descriptions-item>
        <el-descriptions-item label="入库仓库">{{
          detail.warehouseName
        }}</el-descriptions-item>
        <el-descriptions-item label="采购日期">{{
          detail.purchaseDate
        }}</el-descriptions-item>
      </el-descriptions>
      <el-table :data="detailItems" border>
        <el-table-column prop="materialName" label="物料名称" min-width="140" />
        <el-table-column prop="spec" label="规格" width="100" />
        <el-table-column
          prop="quantity"
          label="数量"
          width="80"
          align="right"
        />
        <el-table-column prop="unit" label="单位" width="70" />
        <el-table-column
          prop="price"
          label="单价(元)"
          width="100"
          align="right"
        />
        <el-table-column label="金额(元)" width="110" align="right">
          <template #default="{ row }">{{ formatAmount(row.amount) }}</template>
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
import { useResponsiveLayout } from '@/composables/useResponsiveLayout'
const { compact } = useResponsiveLayout()
import { validateForm, confirmAction } from '@/utils/interaction'
import { ref, reactive, computed, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const list = ref([])
const total = ref(0)
const loading = ref(false)
const suppliers = ref([])
const warehouses = ref([])
const formRef = ref()
const dateRange = ref([])
const query = reactive({
  pageNum: 1,
  pageSize: 10,
  purchaseNo: '',
  supplierId: null,
  status: null
})
const editDialog = ref(false)
const editTitle = ref('')
const form = reactive({ items: [] })
const detailDialog = ref(false)
const detail = ref({})
const detailItems = ref([])
const rules = {
  supplierId: [{ required: true, message: '请选择供应商', trigger: 'change' }],
  warehouseId: [
    { required: true, message: '请选择入库仓库', trigger: 'change' }
  ],
  purchaseDate: [
    { required: true, message: '请选择采购日期', trigger: 'change' }
  ]
}

const formatAmount = (v) =>
  v == null || v === '' ? '0.00' : Number(v).toFixed(2)
const formTotal = computed(() =>
  (form.items || []).reduce((sum, it) => sum + (Number(it.amount) || 0), 0)
)

function calcAmount(row) {
  row.amount =
    Math.round((Number(row.quantity) || 0) * (Number(row.price) || 0) * 100) /
    100
}

function buildParams() {
  return {
    ...query,
    beginTime:
      dateRange.value && dateRange.value[0] ? dateRange.value[0] : undefined,
    endTime:
      dateRange.value && dateRange.value[1] ? dateRange.value[1] : undefined
  }
}
async function load() {
  loading.value = true
  try {
    const res = await request.get('/purchase/list', { params: buildParams() })
    list.value = res.rows
    total.value = res.total
  } catch (error) {
    // 请求拦截器已展示错误，保留当前列表。
  } finally {
    loading.value = false
  }
}
function resetQuery() {
  Object.assign(query, {
    pageNum: 1,
    purchaseNo: '',
    supplierId: null,
    status: null
  })
  dateRange.value = []
  load()
}
function resetForm() {
  Object.assign(form, {
    id: null,
    purchaseNo: '',
    supplierId: null,
    warehouseId: null,
    purchaseDate: '',
    remark: '',
    status: 0,
    items: []
  })
}
function addItem() {
  form.items.push({
    materialName: '',
    spec: '',
    quantity: 1,
    unit: '个',
    price: 0,
    amount: 0
  })
}
function openAdd() {
  resetForm()
  addItem()
  editTitle.value = '新增采购单'
  editDialog.value = true
}
async function openEdit(row) {
  const res = await request.get(`/purchase/${row.id}`)
  Object.assign(form, res.data)
  editTitle.value = '修改采购单'
  editDialog.value = true
}
function onEditClosed() {
  formRef.value && formRef.value.clearValidate()
}
async function submit() {
  if (saving.value) return
  saving.value = true
  try {
    if (!(await validateForm(formRef.value))) return
    if (!form.items.length) {
      ElMessage.warning('请至少添加一条采购明细')
      return
    }
    for (const it of form.items) {
      if (!it.materialName) {
        ElMessage.warning('请填写明细物料名称')
        return
      }
      calcAmount(it)
    }
    form.totalAmount = formTotal.value
    if (form.id) await request.put('/purchase', form)
    else await request.post('/purchase', form)
    ElMessage.success('操作成功')
    editDialog.value = false
    load()
  } catch (error) {
    // 请求失败已由拦截器提示，表单内容保留供重试。
  } finally {
    saving.value = false
  }
}
async function viewDetail(row) {
  const res = await request.get(`/purchase/${row.id}`)
  detail.value = res.data
  detailItems.value = res.data.items || []
  detailDialog.value = true
}
async function audit(row) {
  if (
    !(await confirmAction(
      `确认审核通过采购单【${row.purchaseNo}】？审核后将登记入库`,
      '审核确认',
      { type: 'warning' }
    ))
  )
    return
  await request.post(`/purchase/audit/${row.id}`)
  ElMessage.success('审核成功，已登记入库')
  load()
}
async function remove(row) {
  if (
    !(await confirmAction(`确定删除采购单【${row.purchaseNo}】？`, '提示', {
      type: 'warning'
    }))
  )
    return
  await request.delete(`/purchase/${row.id}`)
  ElMessage.success('删除成功')
  load()
}
async function exportData() {
  const res = await request.get('/purchase/export', {
    params: buildParams(),
    responseType: 'blob'
  })
  const blob = new Blob([res], {
    type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
  })
  const url = window.URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = '采购单数据.xlsx'
  a.click()
  window.URL.revokeObjectURL(url)
  ElMessage.success('导出成功')
}

onMounted(async () => {
  suppliers.value = await loadAllRows('/supplier/list')
  warehouses.value = (await request.get('/warehouse/all')).data || []
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
  margin-bottom: 12px;
  display: flex;
  gap: 10px;
}
</style>
