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
      <el-form-item label="群势">
        <el-select
          v-model="query.colonyPotential"
          placeholder="全部"
          clearable
          style="width: 120px"
        >
          <el-option v-for="s in strengths" :key="s" :label="s" :value="s" />
        </el-select>
      </el-form-item>
      <el-form-item label="巡检日期">
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
        <el-button
          type="primary"
          v-hasPermi="'inspection:add'"
          @click="openAdd"
        >
          <el-icon><Plus /></el-icon>新增巡检
        </el-button>
      </div>
      <el-table :data="list" v-loading="loading" border stripe>
        <el-table-column prop="hiveCode" label="蜂箱编号" width="110" />
        <el-table-column prop="inspectDate" label="巡检日期" width="110" />
        <el-table-column
          prop="temperature"
          label="温度(℃)"
          width="90"
          align="right"
        />
        <el-table-column
          prop="humidity"
          label="湿度(%)"
          width="90"
          align="right"
        />
        <el-table-column width="90">
          <template #header><GlossaryTip term="群势" /></template>
          <template #default="{ row }">
            <el-tag :type="strengthTag(row.colonyPotential)">{{
              row.colonyPotential
            }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="queenStatus" label="蜂王状态" width="100" />
        <el-table-column
          prop="feedStatus"
          label="饲料情况"
          min-width="120"
          show-overflow-tooltip
        />
        <el-table-column
          prop="abnormal"
          label="异常情况"
          min-width="130"
          show-overflow-tooltip
        />
        <el-table-column prop="inspector" label="巡检人" width="100" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button
              link
              type="primary"
              v-hasPermi="'inspection:edit'"
              @click="openEdit(row)"
              >修改</el-button
            >
            <el-button
              link
              type="danger"
              v-hasPermi="'inspection:remove'"
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
        <el-form-item label="巡检日期" prop="inspectDate">
          <el-date-picker
            v-model="form.inspectDate"
            type="date"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="温度(℃)" prop="temperature">
              <el-input-number
                v-model="form.temperature"
                :min="-10"
                :max="50"
                :precision="1"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="湿度(%)" prop="humidity">
              <el-input-number
                v-model="form.humidity"
                :min="0"
                :max="100"
                :precision="0"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item prop="colonyPotential">
          <template #label><GlossaryTip term="群势" /></template>
          <el-select v-model="form.colonyPotential" style="width: 100%">
            <el-option v-for="s in strengths" :key="s" :label="s" :value="s" />
          </el-select>
        </el-form-item>
        <el-form-item label="蜂王状态" prop="queenStatus">
          <el-select v-model="form.queenStatus" style="width: 100%">
            <el-option
              v-for="q in queenStatuses"
              :key="q"
              :label="q"
              :value="q"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="饲料情况">
          <el-select
            v-model="form.feedStatus"
            style="width: 100%"
            allow-create
            filterable
          >
            <el-option
              v-for="f in feedStatuses"
              :key="f"
              :label="f"
              :value="f"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="异常情况">
          <el-input
            v-model="form.abnormal"
            type="textarea"
            placeholder="无异常可留空"
          />
        </el-form-item>
        <el-form-item label="巡检人" prop="inspector">
          <el-input v-model="form.inspector" />
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

const strengths = ['强', '中', '弱']
const strengthTagMap = { 强: 'success', 中: 'warning', 弱: 'danger' }
const strengthTag = (s) => strengthTagMap[s] || ''
const queenStatuses = ['正常', '停产', '失王']
const feedStatuses = [
  '蜜粉充足',
  '蜂粮充足',
  '需补饲',
  '外界粉源好',
  '储蜜一般'
]

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
  colonyPotential: ''
})
const dialog = reactive({ visible: false, title: '' })
const form = reactive({})
const rules = {
  hiveId: [{ required: true, message: '请选择蜂箱', trigger: 'change' }],
  inspectDate: [
    { required: true, message: '请选择巡检日期', trigger: 'change' }
  ],
  colonyPotential: [
    { required: true, message: '请选择群势', trigger: 'change' }
  ],
  queenStatus: [
    { required: true, message: '请选择蜂王状态', trigger: 'change' }
  ],
  inspector: [{ required: true, message: '请输入巡检人', trigger: 'blur' }]
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
    const res = await request.get('/inspection/list', {
      params: buildParams()
    })
    list.value = res.rows
    total.value = res.total
  } catch (error) {
    // 请求拦截器已展示错误，保留当前列表。
  } finally {
    loading.value = false
  }
}
function resetQuery() {
  Object.assign(query, { pageNum: 1, hiveId: null, colonyPotential: '' })
  dateRange.value = []
  load()
}
function reset() {
  Object.assign(form, {
    id: null,
    hiveId: null,
    inspectDate: today(),
    temperature: null,
    humidity: null,
    colonyPotential: '中',
    queenStatus: '正常',
    feedStatus: '蜂粮充足',
    abnormal: '',
    inspector: currentName()
  })
}
function openAdd() {
  reset()
  dialog.title = '新增巡检记录'
  dialog.visible = true
}
function openEdit(row) {
  Object.assign(form, row)
  dialog.title = '修改巡检记录'
  dialog.visible = true
}
async function submit() {
  if (saving.value) return
  saving.value = true
  try {
    if (!(await validateForm(formRef.value))) return
    if (form.id) await request.put('/inspection', form)
    else await request.post('/inspection', form)
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
      `确定删除蜂箱【${row.hiveCode}】的巡检记录？`,
      '提示',
      { type: 'warning' }
    ))
  )
    return
  await request.delete(`/inspection/${row.id}`)
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
