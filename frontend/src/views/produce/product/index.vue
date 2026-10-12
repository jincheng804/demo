<template>
  <div class="app-container">
    <el-card class="table-card">
      <div class="toolbar">
        <el-button type="primary" v-hasPermi="'product:add'" @click="openAdd">
          <el-icon><Plus /></el-icon>装瓶登记
        </el-button>
      </div>
      <el-table :data="list" v-loading="loading" border stripe>
        <el-table-column prop="productBatchNo" label="产品批次号" width="160" />
        <el-table-column
          prop="harvestBatchNo"
          label="来源采收批次"
          width="160"
        />
        <el-table-column prop="productName" label="产品名称" min-width="120" />
        <el-table-column prop="spec" label="规格" width="90" />
        <el-table-column prop="bottleCount" label="装瓶数量" width="100" />
        <el-table-column label="定价(元)" width="100" align="right">
          <template #default="{ row }"
            >¥{{ Number(row.price || 0).toFixed(2) }}</template
          >
        </el-table-column>
        <el-table-column prop="produceDate" label="生产日期" width="110" />
        <el-table-column label="质检" width="90">
          <template #default="{ row }">
            <el-tag
              :type="
                row.qcStatus === 1
                  ? 'success'
                  : row.qcStatus === 2
                    ? 'danger'
                    : 'info'
              "
            >
              {{
                row.qcStatus === 1
                  ? '合格'
                  : row.qcStatus === 2
                    ? '不合格'
                    : '未检'
              }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 2 ? 'warning' : 'success'">
              {{ row.status === 2 ? '已出库' : '在库' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button
              link
              type="primary"
              v-hasPermi="'product:edit'"
              @click="openPrice(row)"
              >定价</el-button
            >
            <el-button
              link
              type="danger"
              v-hasPermi="'product:remove'"
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

    <el-dialog
      align-center
      v-model="dialog.visible"
      title="装瓶登记"
      width="580px"
      @closed="formRef?.clearValidate()"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="auto">
        <el-form-item label="来源采收批次" prop="harvestId">
          <el-select
            v-model="form.harvestId"
            style="width: 100%"
            filterable
            @change="onHarvestChange"
            placeholder="请选择待装瓶的采收批次"
          >
            <el-option
              v-for="h in availableHarvests"
              :key="h.id"
              :label="h.batchNo + ' (' + h.honeyType + ' ' + h.weight + 'kg)'"
              :value="h.id"
            />
          </el-select>
          <div v-if="bottledCount > 0" class="harvest-tip">
            另有 {{ bottledCount }} 个采收批次已装瓶，不在下拉列表中显示
          </div>
        </el-form-item>
        <el-form-item label="产品名称">
          <el-input v-model="form.productName" placeholder="留空则取蜂蜜种类" />
        </el-form-item>
        <el-form-item label="规格">
          <el-select
            v-model="form.spec"
            style="width: 100%"
            @change="calcBottleCount"
          >
            <el-option label="250g" value="250g" /><el-option
              label="500g"
              value="500g"
            />
            <el-option label="1000g" value="1000g" />
          </el-select>
        </el-form-item>
        <el-form-item label="装瓶数量" prop="bottleCount">
          <el-input-number
            v-model="form.bottleCount"
            :min="1"
            :max="maxBottles"
            style="width: 100%"
          />
          <div v-if="selectedHarvest" class="bottle-tip">
            可装 <span class="tip-num">{{ maxBottles }}</span> 瓶（{{
              selectedHarvest.weight
            }}kg ÷ {{ form.spec }}/瓶），已自动填入
          </div>
        </el-form-item>
        <el-form-item label="定价(元)">
          <el-input-number
            v-model="form.price"
            :min="0"
            :precision="2"
            :step="0.5"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="生产日期">
          <el-date-picker
            v-model="form.produceDate"
            type="date"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="保质期(月)">
          <el-input-number v-model="form.shelfLife" :min="1" :max="60" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submit"
          >确定</el-button
        >
      </template>
    </el-dialog>

    <!-- 定价弹窗 -->
    <el-dialog
      align-center
      v-model="priceDialog.visible"
      title="修改定价"
      width="420px"
    >
      <el-form label-width="auto">
        <el-form-item label="产品批次">
          <span>{{ priceDialog.row?.productBatchNo }}</span>
        </el-form-item>
        <el-form-item label="产品名称">
          <span
            >{{ priceDialog.row?.productName }}
            {{ priceDialog.row?.spec }}</span
          >
        </el-form-item>
        <el-form-item label="当前定价">
          <span style="color: #909399"
            >¥{{ Number(priceDialog.row?.price || 0).toFixed(2) }}</span
          >
        </el-form-item>
        <el-form-item label="新定价" required>
          <el-input-number
            v-model="priceDialog.newPrice"
            :min="0"
            :precision="2"
            :step="0.5"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="priceDialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="savePrice"
          >确定</el-button
        >
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { loadAllRows } from '@/utils/pagination'
import { validateForm, confirmAction } from '@/utils/interaction'
import { ref, reactive, computed, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const list = ref([])
const total = ref(0)
const loading = ref(false)
const harvests = ref([])
const formRef = ref()
const query = reactive({ pageNum: 1, pageSize: 10 })
const dialog = reactive({ visible: false })
const form = reactive({})
const priceDialog = reactive({ visible: false, row: null, newPrice: 0 })

// 当前选中的采收批次
const selectedHarvest = computed(() =>
  harvests.value.find((h) => h.id === form.harvestId)
)
// 待装瓶的采收批次（过滤已装瓶 status=2）
const availableHarvests = computed(() =>
  harvests.value.filter((h) => h.status !== 2)
)
// 已装瓶的采收批次数量
const bottledCount = computed(
  () => harvests.value.filter((h) => h.status === 2).length
)
// 最大可装瓶数 = floor(重量kg * 1000 / 规格g)
const maxBottles = computed(() => {
  if (!selectedHarvest.value || !form.spec) return 9999
  const weightG = Number(selectedHarvest.value.weight) * 1000
  const specG = parseInt(form.spec)
  return Math.floor(weightG / specG)
})

function onHarvestChange() {
  // 选择采收批次后自动带出产品名称（蜂蜜种类）
  if (selectedHarvest.value && !form.productName) {
    form.productName = selectedHarvest.value.honeyType
  }
  calcBottleCount()
}

function calcBottleCount() {
  if (selectedHarvest.value && form.spec && maxBottles.value > 0) {
    form.bottleCount = maxBottles.value
  }
}
const rules = {
  harvestId: [
    { required: true, message: '请选择来源采收批次', trigger: 'change' }
  ],
  bottleCount: [{ required: true, message: '请输入装瓶数量', trigger: 'blur' }]
}

async function load() {
  loading.value = true
  try {
    const res = await request.get('/product/list', { params: query })
    list.value = res.rows
    total.value = res.total
  } catch (error) {
    // 请求拦截器已展示错误，保留当前列表。
  } finally {
    loading.value = false
  }
}
async function loadHarvests() {
  // 只加载待装瓶的采收批次（status=1）
  harvests.value = await loadAllRows('/harvest/list', { status: 1 })
}
function reset() {
  Object.assign(form, {
    id: null,
    harvestId: null,
    productName: '',
    spec: '500g',
    bottleCount: null,
    price: 0,
    produceDate: '',
    shelfLife: 24
  })
}
function openAdd() {
  reset()
  dialog.visible = true
}
async function submit() {
  if (saving.value) return
  saving.value = true
  try {
    if (!(await validateForm(formRef.value))) return
    await request.post('/product', form)
    ElMessage.success('装瓶成功，已自动入库')
    dialog.visible = false
    load()
    loadHarvests()
  } catch (error) {
    // 请求失败已由拦截器提示，表单内容保留供重试。
  } finally {
    saving.value = false
  }
}
async function remove(row) {
  if (
    !(await confirmAction('确定删除该产品批次？', '提示', { type: 'warning' }))
  )
    return
  await request.delete(`/product/${row.id}`)
  ElMessage.success('删除成功')
  load()
  loadHarvests()
}
function openPrice(row) {
  priceDialog.row = row
  priceDialog.newPrice = Number(row.price || 0)
  priceDialog.visible = true
}
async function savePrice() {
  if (saving.value) return
  saving.value = true
  try {
    if (priceDialog.newPrice == null || priceDialog.newPrice < 0) {
      ElMessage.warning('定价不能小于0')
      return
    }
    await request.put('/product/price', null, {
      params: { id: priceDialog.row.id, price: priceDialog.newPrice }
    })
    ElMessage.success('定价更新成功')
    priceDialog.visible = false
    load()
  } catch (error) {
    // 请求失败已由拦截器提示，表单内容保留供重试。
  } finally {
    saving.value = false
  }
}
onMounted(async () => {
  loadHarvests()
  load()
})

const saving = ref(false)
</script>
<style scoped>
.toolbar {
  margin-bottom: 12px;
}
.bottle-tip {
  font-size: 12px;
  color: var(--text-secondary);
  margin-top: 6px;
  line-height: 1.5;
}
.bottle-tip .tip-num {
  color: var(--primary);
  font-weight: 600;
  font-size: 13px;
}
.harvest-tip {
  font-size: 12px;
  color: var(--warning);
  margin-top: 6px;
  line-height: 1.5;
}
</style>
