<template>
  <div class="app-container">
    <el-form class="search-form" inline :model="query">
      <el-form-item label="蜂箱">
        <el-select
          v-model="query.hiveId"
          placeholder="全部"
          clearable
          style="width: 150px"
        >
          <el-option
            v-for="h in hives"
            :key="h.id"
            :label="h.hiveCode"
            :value="h.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="饲料类型">
        <el-select
          v-model="query.feedType"
          placeholder="全部"
          clearable
          style="width: 130px"
        >
          <el-option v-for="t in feedTypes" :key="t" :label="t" :value="t" />
        </el-select>
      </el-form-item>
      <el-form-item label="饲喂日期">
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
        <el-button type="primary" v-hasPermi="'feeding:add'" @click="openAdd">
          <el-icon><Plus /></el-icon>新增饲喂
        </el-button>
      </div>
      <el-table :data="list" v-loading="loading" border stripe>
        <el-table-column prop="hiveCode" label="蜂箱编号" width="110" />
        <el-table-column prop="feedDate" label="饲喂日期" width="110" />
        <el-table-column label="饲料类型" width="110">
          <template #default="{ row }">
            <el-tag :type="feedTypeTag(row.feedType)">{{
              row.feedType
            }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="amount" label="用量" width="100" align="right" />
        <el-table-column prop="unit" label="单位" width="80" />
        <el-table-column prop="operator" label="操作人" width="110" />
        <el-table-column
          prop="remark"
          label="备注"
          min-width="150"
          show-overflow-tooltip
        />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button
              link
              type="primary"
              v-hasPermi="'feeding:edit'"
              @click="openEdit(row)"
              >修改</el-button
            >
            <el-button
              link
              type="danger"
              v-hasPermi="'feeding:remove'"
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
        <el-form-item label="蜂箱" prop="hiveId">
          <el-select v-model="form.hiveId" style="width: 100%">
            <el-option
              v-for="h in hives"
              :key="h.id"
              :label="h.hiveCode"
              :value="h.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="饲喂日期" prop="feedDate">
          <el-date-picker
            v-model="form.feedDate"
            type="date"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="饲料类型" prop="feedType">
          <el-select v-model="form.feedType" style="width: 100%">
            <el-option v-for="t in feedTypes" :key="t" :label="t" :value="t" />
          </el-select>
        </el-form-item>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="用量" prop="amount">
              <el-input-number
                v-model="form.amount"
                :min="0"
                :precision="2"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="单位" prop="unit">
              <el-select v-model="form.unit" style="width: 100%">
                <el-option label="kg" value="kg" /><el-option
                  label="L"
                  value="L"
                />
                <el-option label="袋" value="袋" /><el-option
                  label="包"
                  value="包"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="操作人" prop="operator">
          <el-input v-model="form.operator" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" />
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
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
function today() {
  const d = new Date()
  return (
    d.getFullYear() +
    '-' +
    String(d.getMonth() + 1).padStart(2, '0') +
    '-' +
    String(d.getDate()).padStart(2, '0')
  )
}
function currentName() {
  return userStore.user?.realName || userStore.user?.username || ''
}

const feedTypes = ['白糖水', '花粉', '代用花粉']
const feedTypeTagMap = { 白糖水: 'warning', 花粉: 'success', 代用花粉: 'info' }
const feedTypeTag = (t) => feedTypeTagMap[t] || ''

const list = ref([])
const total = ref(0)
const loading = ref(false)
const hives = ref([])
const formRef = ref()
const dateRange = ref([])
const query = reactive({
  pageNum: 1,
  pageSize: 10,
  hiveId: null,
  feedType: ''
})
const dialog = reactive({ visible: false, title: '' })
const form = reactive({})
const rules = {
  hiveId: [{ required: true, message: '请选择蜂箱', trigger: 'change' }],
  feedDate: [{ required: true, message: '请选择饲喂日期', trigger: 'change' }],
  feedType: [{ required: true, message: '请选择饲料类型', trigger: 'change' }],
  amount: [{ required: true, message: '请输入用量', trigger: 'blur' }],
  unit: [{ required: true, message: '请选择单位', trigger: 'change' }],
  operator: [{ required: true, message: '请输入操作人', trigger: 'blur' }]
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
    const res = await request.get('/feeding/list', { params: buildParams() })
    list.value = res.rows
    total.value = res.total
  } catch (error) {
    // 请求拦截器已展示错误，保留当前列表。
  } finally {
    loading.value = false
  }
}
function resetQuery() {
  Object.assign(query, { pageNum: 1, hiveId: null, feedType: '' })
  dateRange.value = []
  load()
}
function reset() {
  Object.assign(form, {
    id: null,
    hiveId: null,
    feedDate: today(),
    feedType: '白糖水',
    amount: null,
    unit: 'kg',
    operator: currentName(),
    remark: ''
  })
}
function openAdd() {
  reset()
  dialog.title = '新增饲喂记录'
  dialog.visible = true
}
function openEdit(row) {
  Object.assign(form, row)
  dialog.title = '修改饲喂记录'
  dialog.visible = true
}
async function submit() {
  if (saving.value) return
  saving.value = true
  try {
    if (!(await validateForm(formRef.value))) return
    if (form.id) await request.put('/feeding', form)
    else await request.post('/feeding', form)
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
    !(await confirmAction(
      `确定删除蜂箱【${row.hiveCode}】的饲喂记录？`,
      '提示',
      { type: 'warning' }
    ))
  )
    return
  await request.delete(`/feeding/${row.id}`)
  ElMessage.success('删除成功')
  load()
}

onMounted(async () => {
  hives.value = (await request.get('/hive/all')).data || []
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
