<template>
  <div class="app-container">
    <el-form class="search-form" inline :model="query">
      <el-form-item label="仓库名称">
        <el-input
          v-model="query.whName"
          placeholder="请输入"
          clearable
          @keyup.enter="onSearch"
        />
      </el-form-item>
      <el-form-item label="仓库类型">
        <el-select
          v-model="query.whType"
          placeholder="全部"
          clearable
          style="width: 130px"
        >
          <el-option v-for="t in types" :key="t" :label="t" :value="t" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态">
        <el-select
          v-model="query.status"
          placeholder="全部"
          clearable
          style="width: 110px"
        >
          <el-option label="正常" :value="1" /><el-option
            label="停用"
            :value="0"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="创建时间">
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
        <el-button type="primary" v-hasPermi="'warehouse:add'" @click="openAdd">
          <el-icon><Plus /></el-icon>新增仓库
        </el-button>
        <el-button
          type="success"
          v-hasPermi="'warehouse:export'"
          @click="exportData"
        >
          <el-icon><Download /></el-icon>导出
        </el-button>
      </div>
      <el-table :data="list" v-loading="loading" border stripe>
        <el-table-column prop="whName" label="仓库名称" min-width="140" />
        <el-table-column prop="whType" label="仓库类型" width="110">
          <template #default="{ row }">
            <el-tag :type="typeTag(row.whType)">{{ row.whType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="location" label="位置" min-width="150" />
        <el-table-column prop="manager" label="负责人" width="100" />
        <el-table-column prop="phone" label="联系电话" width="130" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '正常' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button
              link
              type="primary"
              v-hasPermi="'warehouse:edit'"
              @click="openEdit(row)"
              >修改</el-button
            >
            <el-button
              link
              type="danger"
              v-hasPermi="'warehouse:remove'"
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
        <el-form-item label="仓库名称" prop="whName">
          <el-input v-model="form.whName" />
        </el-form-item>
        <el-form-item label="仓库类型" prop="whType">
          <el-select v-model="form.whType" style="width: 100%">
            <el-option v-for="t in types" :key="t" :label="t" :value="t" />
          </el-select>
        </el-form-item>
        <el-form-item label="位置">
          <el-input v-model="form.location" />
        </el-form-item>
        <el-form-item label="负责人">
          <el-input v-model="form.manager" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">正常</el-radio
            ><el-radio :value="0">停用</el-radio>
          </el-radio-group>
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

const types = ['原料库', '成品库', '蜂具库']
const typeTagMap = { 原料库: 'warning', 成品库: 'success', 蜂具库: 'info' }
const typeTag = (t) => typeTagMap[t] || ''

const list = ref([])
const total = ref(0)
const loading = ref(false)
const formRef = ref()
const dateRange = ref([])
const query = reactive({
  pageNum: 1,
  pageSize: 10,
  whName: '',
  whType: '',
  status: null
})
const dialog = reactive({ visible: false, title: '' })
const form = reactive({})
const rules = {
  whName: [{ required: true, message: '请输入仓库名称', trigger: 'blur' }],
  whType: [{ required: true, message: '请选择仓库类型', trigger: 'change' }]
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
    const res = await request.get('/warehouse/list', { params: buildParams() })
    list.value = res.rows
    total.value = res.total
  } catch (error) {
    // 请求拦截器已展示错误，保留当前列表。
  } finally {
    loading.value = false
  }
}
function resetQuery() {
  Object.assign(query, { pageNum: 1, whName: '', whType: '', status: null })
  dateRange.value = []
  load()
}
function reset() {
  Object.assign(form, {
    id: null,
    whName: '',
    whType: '',
    location: '',
    manager: '',
    phone: '',
    status: 1,
    remark: ''
  })
}
function openAdd() {
  reset()
  dialog.title = '新增仓库'
  dialog.visible = true
}
function openEdit(row) {
  Object.assign(form, row)
  dialog.title = '修改仓库'
  dialog.visible = true
}
async function submit() {
  if (saving.value) return
  saving.value = true
  try {
    if (!(await validateForm(formRef.value))) return
    if (form.id) await request.put('/warehouse', form)
    else await request.post('/warehouse', form)
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
    !(await confirmAction(`确定删除仓库【${row.whName}】？`, '提示', {
      type: 'warning'
    }))
  )
    return
  await request.delete(`/warehouse/${row.id}`)
  ElMessage.success('删除成功')
  load()
}
async function exportData() {
  const res = await request.get('/warehouse/export', {
    params: buildParams(),
    responseType: 'blob'
  })
  const blob = new Blob([res], {
    type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
  })
  const url = window.URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = '仓库数据.xlsx'
  a.click()
  window.URL.revokeObjectURL(url)
  ElMessage.success('导出成功')
}

onMounted(load)

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
