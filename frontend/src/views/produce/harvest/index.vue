<template>
  <div class="app-container">
    <el-form class="search-form" inline :model="query">
      <el-form-item label="批次号">
        <el-input v-model="query.batchNo" clearable />
      </el-form-item>
      <el-form-item label="蜂蜜种类">
        <el-select
          v-model="query.honeyType"
          placeholder="全部"
          clearable
          style="width: 120px"
        >
          <el-option label="洋槐蜜" value="洋槐蜜" /><el-option
            label="荆条蜜"
            value="荆条蜜"
          />
          <el-option label="百花蜜" value="百花蜜" /><el-option
            label="枣花蜜"
            value="枣花蜜"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="onSearch">查询</el-button>
      </el-form-item>
    </el-form>

    <el-card class="table-card">
      <div class="toolbar">
        <el-button type="primary" v-hasPermi="'harvest:add'" @click="openAdd">
          <el-icon><Plus /></el-icon>摇蜜登记
        </el-button>
      </div>
      <el-table :data="list" v-loading="loading" border stripe>
        <el-table-column prop="batchNo" label="采收批次号" width="160" />
        <el-table-column prop="hiveCode" label="蜂箱" width="100" />
        <el-table-column prop="sourceName" label="蜜源地" width="120" />
        <el-table-column prop="harvestDate" label="摇蜜日期" width="110" />
        <el-table-column prop="honeyType" label="蜂蜜种类" width="100" />
        <el-table-column prop="weight" label="重量(kg)" width="100" />
        <el-table-column prop="moisture" width="100">
          <template #header
            ><GlossaryTip
              term="波美度"
              explanation="衡量蜂蜜浓度的指标，度数越高表示蜂蜜越浓稠、含水量越低"
          /></template>
        </el-table-column>
        <el-table-column prop="operator" label="操作人" width="90" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 2 ? 'success' : 'warning'">
              {{ row.status === 2 ? '已装瓶' : '待加工' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button
              link
              type="danger"
              v-hasPermi="'harvest:remove'"
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
      title="摇蜜采收登记"
      width="540px"
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
        <el-form-item label="蜜源地">
          <el-select v-model="form.sourceId" style="width: 100%" clearable>
            <el-option
              v-for="s in sources"
              :key="s.id"
              :label="s.sourceName"
              :value="s.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="摇蜜日期" prop="harvestDate">
          <el-date-picker
            v-model="form.harvestDate"
            type="date"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="蜂蜜种类">
          <el-select v-model="form.honeyType" style="width: 100%">
            <el-option label="洋槐蜜" value="洋槐蜜" /><el-option
              label="荆条蜜"
              value="荆条蜜"
            />
            <el-option label="百花蜜" value="百花蜜" /><el-option
              label="枣花蜜"
              value="枣花蜜"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="采收重量(kg)" prop="weight">
          <el-input-number
            v-model="form.weight"
            :min="0"
            :precision="2"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item>
          <template #label
            ><GlossaryTip
              term="波美度"
              explanation="衡量蜂蜜浓度的指标，度数越高表示蜂蜜越浓稠、含水量越低"
          /></template>
          <el-input-number
            v-model="form.moisture"
            :min="30"
            :max="45"
            :precision="2"
          />
        </el-form-item>
        <el-form-item label="操作人">
          <el-input v-model="form.operator" />
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

const list = ref([])
const total = ref(0)
const loading = ref(false)
const hives = ref([])
const sources = ref([])
const formRef = ref()
const query = reactive({
  pageNum: 1,
  pageSize: 10,
  batchNo: '',
  honeyType: ''
})
const dialog = reactive({ visible: false })
const form = reactive({})
const rules = {
  hiveId: [{ required: true, message: '请选择蜂箱', trigger: 'change' }],
  harvestDate: [{ required: true, message: '请选择日期', trigger: 'change' }],
  weight: [{ required: true, message: '请输入重量', trigger: 'blur' }]
}

async function load() {
  loading.value = true
  try {
    const res = await request.get('/harvest/list', { params: query })
    list.value = res.rows
    total.value = res.total
  } catch (error) {
    // 请求拦截器已展示错误，保留当前列表。
  } finally {
    loading.value = false
  }
}
function reset() {
  Object.assign(form, {
    id: null,
    hiveId: null,
    sourceId: null,
    harvestDate: today(),
    honeyType: '洋槐蜜',
    weight: null,
    moisture: null,
    operator: currentName()
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
    const res = await request.post('/harvest', form)
    ElMessage.success('登记成功，批次号：' + res.data)
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
    !(await confirmAction('确定删除该采收记录？', '提示', { type: 'warning' }))
  )
    return
  await request.delete(`/harvest/${row.id}`)
  ElMessage.success('删除成功')
  load()
}
onMounted(async () => {
  hives.value = (await request.get('/hive/all')).data
  sources.value = (await request.get('/source/list')).data
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
}
</style>
