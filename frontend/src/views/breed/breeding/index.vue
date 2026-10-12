<template>
  <div class="app-container">
    <el-form class="search-form" inline :model="query">
      <el-form-item label="类型">
        <el-select
          v-model="query.breedType"
          placeholder="全部"
          clearable
          style="width: 140px"
        >
          <el-option v-for="t in breedTypes" :key="t" :label="t" :value="t" />
        </el-select>
      </el-form-item>
      <el-form-item label="繁育日期">
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
        <el-button type="primary" v-hasPermi="'breeding:add'" @click="openAdd">
          <el-icon><Plus /></el-icon>新增繁育
        </el-button>
      </div>
      <el-table :data="list" v-loading="loading" border stripe>
        <el-table-column label="类型" width="110">
          <template #default="{ row }">
            <el-tag :type="typeTag(row.breedType)">{{ row.breedType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="breedDate" label="繁育日期" width="110" />
        <el-table-column label="主蜂箱" width="100">
          <template #default="{ row }">{{ hiveCode(row.hiveId) }}</template>
        </el-table-column>
        <el-table-column label="源蜂箱" width="100">
          <template #default="{ row }">{{
            hiveCode(row.sourceHiveId)
          }}</template>
        </el-table-column>
        <el-table-column label="目标蜂箱" width="100">
          <template #default="{ row }">{{
            hiveCode(row.targetHiveId)
          }}</template>
        </el-table-column>
        <el-table-column
          prop="quantity"
          label="数量(群)"
          width="100"
          align="right"
        />
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
              v-hasPermi="'breeding:edit'"
              @click="openEdit(row)"
              >修改</el-button
            >
            <el-button
              link
              type="danger"
              v-hasPermi="'breeding:remove'"
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
      width="560px"
      @closed="formRef?.clearValidate()"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="auto">
        <el-form-item label="类型" prop="breedType">
          <el-select v-model="form.breedType" style="width: 100%">
            <el-option v-for="t in breedTypes" :key="t" :label="t" :value="t" />
          </el-select>
        </el-form-item>
        <el-form-item label="繁育日期" prop="breedDate">
          <el-date-picker
            v-model="form.breedDate"
            type="date"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="主蜂箱">
          <el-select v-model="form.hiveId" style="width: 100%" clearable>
            <el-option
              v-for="h in hives"
              :key="h.id"
              :label="h.hiveCode"
              :value="h.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="源蜂箱" prop="sourceHiveId">
          <el-select v-model="form.sourceHiveId" style="width: 100%" clearable>
            <el-option
              v-for="h in hives"
              :key="h.id"
              :label="h.hiveCode"
              :value="h.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="目标蜂箱">
          <el-select v-model="form.targetHiveId" style="width: 100%" clearable>
            <el-option
              v-for="h in hives"
              :key="h.id"
              :label="h.hiveCode"
              :value="h.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="数量" prop="quantity">
          <el-input-number
            v-model="form.quantity"
            :min="1"
            :precision="0"
            style="width: 100%"
          />
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

const breedTypes = ['自然分蜂', '人工分蜂', '并群', '蜂王培育']
const typeTagMap = {
  自然分蜂: 'success',
  人工分蜂: 'primary',
  并群: 'warning',
  蜂王培育: 'info'
}
const typeTag = (t) => typeTagMap[t] || ''

const list = ref([])
const total = ref(0)
const loading = ref(false)
const hives = ref([])
const hiveMap = ref({})
const formRef = ref()
const dateRange = ref([])
const query = reactive({ pageNum: 1, pageSize: 10, breedType: '' })
const dialog = reactive({ visible: false, title: '' })
const form = reactive({})
const rules = {
  breedType: [{ required: true, message: '请选择类型', trigger: 'change' }],
  breedDate: [{ required: true, message: '请选择繁育日期', trigger: 'change' }],
  quantity: [{ required: true, message: '请输入数量', trigger: 'blur' }]
}

const hiveCode = (id) => (hiveMap.value[id] ? hiveMap.value[id] : id ? id : '-')

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
    const res = await request.get('/breeding/list', { params: buildParams() })
    list.value = res.rows
    total.value = res.total
  } catch (error) {
    // 请求拦截器已展示错误，保留当前列表。
  } finally {
    loading.value = false
  }
}
function resetQuery() {
  Object.assign(query, { pageNum: 1, breedType: '' })
  dateRange.value = []
  load()
}
function reset() {
  Object.assign(form, {
    id: null,
    breedType: '自然分蜂',
    breedDate: '',
    hiveId: null,
    sourceHiveId: null,
    targetHiveId: null,
    quantity: 1,
    remark: ''
  })
}
function openAdd() {
  reset()
  dialog.title = '新增繁育记录'
  dialog.visible = true
}
function openEdit(row) {
  Object.assign(form, row)
  dialog.title = '修改繁育记录'
  dialog.visible = true
}
async function submit() {
  if (saving.value) return
  saving.value = true
  try {
    if (!(await validateForm(formRef.value))) return
    if (form.id) await request.put('/breeding', form)
    else await request.post('/breeding', form)
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
    !(await confirmAction(`确定删除该【${row.breedType}】记录？`, '提示', {
      type: 'warning'
    }))
  )
    return
  await request.delete(`/breeding/${row.id}`)
  ElMessage.success('删除成功')
  load()
}

onMounted(async () => {
  hives.value = (await request.get('/hive/all')).data || []
  hives.value.forEach((h) => {
    hiveMap.value[h.id] = h.hiveCode
  })
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
