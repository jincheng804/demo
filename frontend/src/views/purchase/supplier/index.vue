<template>
  <div class="app-container">
    <el-form class="search-form" inline :model="query">
      <el-form-item label="供应商名称">
        <el-input
          v-model="query.supplierName"
          placeholder="请输入"
          clearable
          @keyup.enter="onSearch"
        />
      </el-form-item>
      <el-form-item label="供应类型">
        <el-select
          v-model="query.supplierType"
          placeholder="全部"
          clearable
          style="width: 130px"
        >
          <el-option
            v-for="t in supplierTypes"
            :key="t"
            :label="t"
            :value="t"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="状态">
        <el-select
          v-model="query.status"
          placeholder="全部"
          clearable
          style="width: 110px"
        >
          <el-option label="合作中" :value="1" /><el-option
            label="停用"
            :value="0"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="onSearch">查询</el-button>
        <el-button @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-card class="table-card">
      <div class="toolbar">
        <el-button type="primary" v-hasPermi="'supplier:add'" @click="openAdd">
          <el-icon><Plus /></el-icon>新增供应商
        </el-button>
        <el-button
          type="success"
          v-hasPermi="'supplier:export'"
          @click="exportData"
        >
          <el-icon><Download /></el-icon>导出
        </el-button>
      </div>
      <el-table :data="list" v-loading="loading" border stripe>
        <el-table-column
          prop="supplierName"
          label="供应商名称"
          min-width="160"
        />
        <el-table-column label="供应类型" width="110">
          <template #default="{ row }">
            <el-tag :type="typeTag(row.supplierType)">{{
              row.supplierType
            }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="contact" label="联系人" width="100" />
        <el-table-column prop="phone" label="联系电话" width="130" />
        <el-table-column
          prop="address"
          label="地址"
          min-width="180"
          show-overflow-tooltip
        />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '合作中' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button
              link
              type="primary"
              v-hasPermi="'supplier:edit'"
              @click="openEdit(row)"
              >修改</el-button
            >
            <el-button
              link
              type="danger"
              v-hasPermi="'supplier:remove'"
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
        <el-form-item label="供应商名称" prop="supplierName">
          <el-input v-model="form.supplierName" />
        </el-form-item>
        <el-form-item label="供应类型" prop="supplierType">
          <el-select v-model="form.supplierType" style="width: 100%">
            <el-option
              v-for="t in supplierTypes"
              :key="t"
              :label="t"
              :value="t"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="联系人" prop="contact">
          <el-input v-model="form.contact" />
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="地址">
          <el-input v-model="form.address" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">合作中</el-radio
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

const supplierTypes = ['蜂具', '饲料', '药品', '包装']
const typeTagMap = {
  蜂具: 'warning',
  饲料: 'success',
  药品: 'danger',
  包装: 'info'
}
const typeTag = (t) => typeTagMap[t] || ''

const list = ref([])
const total = ref(0)
const loading = ref(false)
const formRef = ref()
const query = reactive({
  pageNum: 1,
  pageSize: 10,
  supplierName: '',
  supplierType: '',
  status: null
})
const dialog = reactive({ visible: false, title: '' })
const form = reactive({})
const rules = {
  supplierName: [
    { required: true, message: '请输入供应商名称', trigger: 'blur' }
  ],
  supplierType: [
    { required: true, message: '请选择供应类型', trigger: 'change' }
  ],
  phone: [
    {
      pattern: /^1[3-9]\d{9}$/,
      message: '请输入正确的手机号',
      trigger: 'blur'
    }
  ]
}

async function load() {
  loading.value = true
  try {
    const res = await request.get('/supplier/list', { params: query })
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
    supplierName: '',
    supplierType: '',
    status: null
  })
  load()
}
function reset() {
  Object.assign(form, {
    id: null,
    supplierName: '',
    supplierType: '',
    contact: '',
    phone: '',
    address: '',
    status: 1,
    remark: ''
  })
}
function openAdd() {
  reset()
  dialog.title = '新增供应商'
  dialog.visible = true
}
function openEdit(row) {
  Object.assign(form, row)
  dialog.title = '修改供应商'
  dialog.visible = true
}
async function submit() {
  if (saving.value) return
  saving.value = true
  try {
    if (!(await validateForm(formRef.value))) return
    if (form.id) await request.put('/supplier', form)
    else await request.post('/supplier', form)
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
    !(await confirmAction(`确定删除供应商【${row.supplierName}】？`, '提示', {
      type: 'warning'
    }))
  )
    return
  await request.delete(`/supplier/${row.id}`)
  ElMessage.success('删除成功')
  load()
}
async function exportData() {
  const res = await request.get('/supplier/export', {
    params: query,
    responseType: 'blob'
  })
  const blob = new Blob([res], {
    type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
  })
  const url = window.URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = '供应商数据.xlsx'
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
