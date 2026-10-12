<template>
  <div class="app-container">
    <el-form inline class="search-form" @submit.prevent="search">
      <el-form-item label="物品名称"><el-input v-model="query.itemName" placeholder="搜索库存物品" clearable @clear="search" /></el-form-item>
      <el-form-item label="类型"><el-select v-model="query.itemType" placeholder="全部类型" clearable style="width:140px" @change="search"><el-option label="蜂具耗材" :value="1"/><el-option label="产品" :value="2"/></el-select></el-form-item>
      <el-form-item><el-button type="primary" native-type="submit"><el-icon><Search /></el-icon>查询</el-button></el-form-item>
    </el-form>
    <el-card class="table-card" shadow="never">
      <div class="toolbar">
        <el-button type="primary" v-hasPermi="'inventory:add'" @click="openAdd">
          <el-icon><Plus /></el-icon>新增物品
        </el-button>
      </div>
      <el-table :data="list" v-loading="loading" border stripe>
        <el-table-column label="类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag
              :type="row.itemType === 1 ? 'primary' : 'success'"
              size="small"
              effect="light"
            >
              {{ row.itemType === 1 ? '蜂具耗材' : '产品' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          prop="itemName"
          label="名称"
          min-width="140"
          show-overflow-tooltip
        />
        <el-table-column prop="spec" label="规格" width="100" align="center" />
        <el-table-column prop="unit" label="单位" width="70" align="center" />
        <el-table-column label="库存" width="100" align="right">
          <template #default="{ row }">
            <span
              :class="lowStock(row) ? 'status-danger' : ''"
              style="font-weight: 600"
            >
              {{ row.stock }}
            </span>
          </template>
        </el-table-column>
        <el-table-column width="100" align="right">
          <template #header
            ><GlossaryTip
              term="预警值"
              explanation="当库存数量低于此数值时触发库存不足预警，提醒及时补货或安排生产"
          /></template>
          <template #default="{ row }">
            <span style="color: #909399">{{ row.warnStock ?? '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag
              v-if="lowStock(row)"
              type="danger"
              size="small"
              effect="dark"
              >库存不足</el-tag
            >
            <el-tag v-else type="success" size="small" effect="light"
              >正常</el-tag
            >
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template #default="{ row }">
            <el-button
              link
              type="primary"
              v-hasPermi="'inventory:stock'"
              :disabled="Number(row.itemType) === 2"
              :title="Number(row.itemType) === 2 ? '成品请通过装瓶或销售出库操作' : ''"
              @click="openStock(row, 1)"
              >入库</el-button
            >
            <el-button
              link
              type="warning"
              v-hasPermi="'inventory:stock'"
              :disabled="Number(row.itemType) === 2"
              :title="Number(row.itemType) === 2 ? '成品请通过装瓶或销售出库操作' : ''"
              @click="openStock(row, 2)"
              >出库</el-button
            >
            <el-button
              link
              type="danger"
              v-hasPermi="'inventory:remove'"
              @click="remove(row)"
              >删除</el-button
            >
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrapper"><el-pagination v-model:current-page="query.pageNum" v-model:page-size="query.pageSize" :total="total" :page-sizes="[10,20,50]" layout="total, prev, pager, next, sizes" background @current-change="load" @size-change="search" /></div>
    </el-card>

    <!-- 新增物品 -->
    <el-dialog
      align-center
      v-model="addDialog"
      title="新增物品"
      width="500px"
      @closed="formRef?.clearValidate()"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="auto">
        <el-form-item label="类型">
          <el-radio-group v-model="form.itemType">
            <el-radio :value="1">蜂具耗材</el-radio>
            <el-radio :value="2" disabled>产品（装瓶后自动建账）</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="所属仓库" prop="warehouseId">
          <el-select v-model="form.warehouseId" placeholder="请选择仓库" filterable>
            <el-option v-for="warehouse in warehouses" :key="warehouse.id" :label="warehouse.whName" :value="warehouse.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="名称" prop="itemName">
          <el-input
            v-model="form.itemName"
            placeholder="请输入物品名称"
            maxlength="50"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="规格">
          <el-input v-model="form.spec" placeholder="如 500g / 1000g" />
        </el-form-item>
        <el-form-item label="单位">
          <el-input v-model="form.unit" placeholder="如 瓶 / 个 / kg" />
        </el-form-item>
        <el-form-item label="预警库存">
          <el-input-number
            v-model="form.warnStock"
            :min="0"
            :step="1"
            controls-position="right"
            style="width: 200px"
          />
          <span style="margin-left: 8px; color: #909399; font-size: 12px"
            >低于该值将预警</span
          >
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addDialog = false">取 消</el-button>
        <el-button type="primary" :loading="saving" @click="submitAdd"
          >确 定</el-button
        >
      </template>
    </el-dialog>

    <!-- 出入库 -->
    <el-dialog
      align-center
      v-model="stockDialog"
      :title="stockTitle"
      width="440px"
    >
      <el-form label-width="auto">
        <el-form-item label="物品名称">
          <span style="font-weight: 600; color: #303133">{{
            currentItem.itemName
          }}</span>
          <el-tag size="small" style="margin-left: 8px">{{
            currentItem.spec
          }}</el-tag>
        </el-form-item>
        <el-form-item label="当前库存">
          <span style="font-size: 15px; font-weight: 600; color: #409eff">
            {{ currentItem.stock }} {{ currentItem.unit }}
          </span>
        </el-form-item>
        <el-form-item :label="stockType === 1 ? '入库数量' : '出库数量'">
          <el-input-number
            v-model="stockQty"
            :min="1"
            :step="1"
            controls-position="right"
            style="width: 200px"
          />
          <span style="margin-left: 8px; color: #909399; font-size: 12px">{{
            currentItem.unit
          }}</span>
        </el-form-item>
        <el-form-item label="操作后库存" v-if="stockQty > 0">
          <span
            :style="
              afterStock < 0
                ? 'color:#f56c6c;font-weight:600'
                : 'color:#67c23a;font-weight:600'
            "
          >
            {{ afterStock }} {{ currentItem.unit }}
          </span>
          <span
            v-if="afterStock < 0"
            style="margin-left: 8px; color: #f56c6c; font-size: 12px"
            >库存不足</span
          >
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            v-model="stockRemark"
            type="textarea"
            :rows="2"
            placeholder="选填"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="stockDialog = false">取 消</el-button>
        <el-button
          type="primary"
          :disabled="afterStock < 0"
          :loading="saving"
          @click="submitStock"
          >确 定</el-button
        >
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { validateForm, confirmAction } from '@/utils/interaction'
import { ref, reactive, computed, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const list = ref([])
const warehouses = ref([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10, itemName: '', itemType: null })
function search() { query.pageNum = 1; load() }
const loading = ref(false)
const addDialog = ref(false)
const stockDialog = ref(false)
const formRef = ref()
const form = reactive({})
const currentItem = reactive({})
const stockQty = ref(1)
const stockRemark = ref('')
const stockType = ref(1)
const stockTitle = computed(() =>
  stockType.value === 1 ? '入库登记' : '出库登记'
)

const afterStock = computed(() => {
  const cur = Number(currentItem.stock) || 0
  const qty = Number(stockQty.value) || 0
  return stockType.value === 1 ? cur + qty : cur - qty
})

const rules = {
  warehouseId: [{ required: true, message: '请选择仓库', trigger: 'change' }],
  itemName: [{ required: true, message: '请输入名称', trigger: 'blur' }]
}

function lowStock(row) {
  const stock = Number(row.stock)
  const warn = Number(row.warnStock)
  return !isNaN(stock) && !isNaN(warn) && stock <= warn
}

async function load() {
  loading.value = true
  try {
    const res = await request.get('/inventory/list', {
      params: query
    })
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
    itemType: 1,
    warehouseId: warehouses.value[0]?.id,
    itemName: '',
    spec: '',
    unit: '',
    warnStock: 0
  })
  addDialog.value = true
}
async function submitAdd() {
  if (saving.value) return
  saving.value = true
  try {
    if (!(await validateForm(formRef.value))) return
    await request.post('/inventory', form)
    ElMessage.success('新增成功')
    addDialog.value = false
    load()
  } catch (error) {
    // 请求失败已由拦截器提示，表单内容保留供重试。
  } finally {
    saving.value = false
  }
}
function openStock(row, type) {
  Object.assign(currentItem, row)
  stockType.value = type
  stockQty.value = 1
  stockRemark.value = ''
  stockDialog.value = true
}
async function submitStock() {
  if (saving.value) return
  saving.value = true
  try {
    await request.post('/inventory/op', null, {
      params: {
        itemId: currentItem.id,
        bizType: stockType.value,
        quantity: stockQty.value,
        remark: stockRemark.value
      }
    })
    ElMessage.success(`${stockTitle.value}成功`)
    stockDialog.value = false
    load()
  } catch (error) {
    // 请求失败已由拦截器提示，表单内容保留供重试。
  } finally {
    saving.value = false
  }
}
async function remove(row) {
  if (
    !(await confirmAction(
      `确定删除【${row.itemName}】？删除后不可恢复`,
      '提示',
      { type: 'warning' }
    ))
  )
    return
  await request.delete(`/inventory/${row.id}`)
  ElMessage.success('删除成功')
  load()
}
onMounted(async () => {
  await load()
  try { warehouses.value = (await request.get('/warehouse/all')).data || [] } catch { /* 请求拦截器已提示 */ }
})

const saving = ref(false)
</script>
