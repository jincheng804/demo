<template>
  <div class="app-container">
    <el-form class="search-form" inline :model="query">
      <el-form-item label="费用类型">
        <el-select
          v-model="query.expenseType"
          placeholder="全部"
          clearable
          style="width: 130px"
        >
          <el-option v-for="t in expenseTypes" :key="t" :label="t" :value="t" />
        </el-select>
      </el-form-item>
      <el-form-item label="所属分区">
        <el-select
          v-model="query.apiaryId"
          placeholder="全部"
          clearable
          style="width: 140px"
        >
          <el-option
            v-for="a in apiaries"
            :key="a.id"
            :label="a.apiaryName"
            :value="a.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="费用日期">
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
        <el-button type="primary" v-hasPermi="'expense:add'" @click="openAdd">
          <el-icon><Plus /></el-icon>新增费用
        </el-button>
        <el-button
          type="success"
          v-hasPermi="'expense:export'"
          @click="exportData"
        >
          <el-icon><Download /></el-icon>导出
        </el-button>
      </div>
      <el-table :data="list" v-loading="loading" border stripe>
        <el-table-column prop="apiaryName" label="所属分区" width="120" />
        <el-table-column prop="expenseDate" label="费用日期" width="110" />
        <el-table-column label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="typeTag(row.expenseType)">{{
              row.expenseType
            }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="金额(元)" width="120" align="right">
          <template #default="{ row }">{{ formatAmount(row.amount) }}</template>
        </el-table-column>
        <el-table-column
          prop="summary"
          label="摘要"
          min-width="200"
          show-overflow-tooltip
        />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button
              link
              type="primary"
              v-hasPermi="'expense:edit'"
              @click="openEdit(row)"
              >修改</el-button
            >
            <el-button
              link
              type="danger"
              v-hasPermi="'expense:remove'"
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

    <el-dialog
      align-center
      v-model="dialog.visible"
      :title="dialog.title"
      width="520px"
      @closed="formRef?.clearValidate()"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="auto">
        <el-form-item label="所属分区" prop="apiaryId">
          <el-select v-model="form.apiaryId" style="width: 100%">
            <el-option
              v-for="a in apiaries"
              :key="a.id"
              :label="a.apiaryName"
              :value="a.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="费用日期" prop="expenseDate">
          <el-date-picker
            v-model="form.expenseDate"
            type="date"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="费用类型" prop="expenseType">
          <el-select v-model="form.expenseType" style="width: 100%">
            <el-option
              v-for="t in expenseTypes"
              :key="t"
              :label="t"
              :value="t"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="金额(元)" prop="amount">
          <el-input-number
            v-model="form.amount"
            :min="0"
            :precision="2"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="摘要" prop="summary">
          <el-input
            v-model="form.summary"
            type="textarea"
            placeholder="费用说明"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submit"
          >确定</el-button
        >
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { validateForm, confirmAction } from '@/utils/interaction'
import { ref, reactive, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const expenseTypes = ['人工', '饲料', '药品', '蜂具', '包装', '其他']
const typeTagMap = {
  人工: '',
  饲料: 'success',
  药品: 'danger',
  蜂具: 'warning',
  包装: 'info',
  其他: 'info'
}
const typeTag = (t) => typeTagMap[t] || ''

const list = ref([])
const total = ref(0)
const loading = ref(false)
const apiaries = ref([])
const formRef = ref()
const dateRange = ref([])
const query = reactive({
  pageNum: 1,
  pageSize: 10,
  expenseType: '',
  apiaryId: null
})
const dialog = reactive({ visible: false, title: '' })
const form = reactive({})
const rules = {
  apiaryId: [{ required: true, message: '请选择所属分区', trigger: 'change' }],
  expenseDate: [
    { required: true, message: '请选择费用日期', trigger: 'change' }
  ],
  expenseType: [
    { required: true, message: '请选择费用类型', trigger: 'change' }
  ],
  amount: [{ required: true, message: '请输入金额', trigger: 'blur' }],
  summary: [{ required: true, message: '请输入费用摘要', trigger: 'blur' }]
}

const formatAmount = (v) =>
  v == null || v === '' ? '0.00' : Number(v).toFixed(2)

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
    const res = await request.get('/expense/list', { params: buildParams() })
    list.value = res.rows
    total.value = res.total
  } catch (error) {
    // 请求拦截器已展示错误，保留当前列表。
  } finally {
    loading.value = false
  }
}
function resetQuery() {
  Object.assign(query, { pageNum: 1, expenseType: '', apiaryId: null })
  dateRange.value = []
  load()
}
function reset() {
  Object.assign(form, {
    id: null,
    apiaryId: null,
    expenseDate: '',
    expenseType: '人工',
    amount: null,
    summary: ''
  })
}
function openAdd() {
  reset()
  dialog.title = '新增成本费用'
  dialog.visible = true
}
function openEdit(row) {
  Object.assign(form, row)
  dialog.title = '修改成本费用'
  dialog.visible = true
}
async function submit() {
  if (saving.value) return
  saving.value = true
  try {
    if (!(await validateForm(formRef.value))) return
    if (form.id) await request.put('/expense', form)
    else await request.post('/expense', form)
    ElMessage.success('操作成功')
    dialog.visible = false
    load()
  } catch (error) {
    // 请求失败已由拦截器提示，表单内容保留供重试。
  } finally {
    saving.value = false
  }
}
async function remove(row) {
  if (
    !(await confirmAction(`确定删除该笔费用（${row.summary}）？`, '提示', {
      type: 'warning'
    }))
  )
    return
  await request.delete(`/expense/${row.id}`)
  ElMessage.success('删除成功')
  load()
}
async function exportData() {
  const res = await request.get('/expense/export', {
    params: buildParams(),
    responseType: 'blob'
  })
  const blob = new Blob([res], {
    type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
  })
  const url = window.URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = '成本费用数据.xlsx'
  a.click()
  window.URL.revokeObjectURL(url)
  ElMessage.success('导出成功')
}

onMounted(async () => {
  apiaries.value = (await request.get('/apiary/list')).data || []
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
