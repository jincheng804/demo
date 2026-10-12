<template>
  <div class="app-container">
    <el-form class="search-form" inline :model="query">
      <el-form-item label="蜂箱">
        <el-select
          v-model="query.hiveId"
          placeholder="全部"
          clearable
          style="width: 140px"
        >
          <el-option
            v-for="h in hives"
            :key="h.id"
            :label="h.hiveCode"
            :value="h.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="onSearch">查询</el-button>
      </el-form-item>
    </el-form>

    <el-card class="table-card">
      <div class="toolbar">
        <el-button type="primary" v-hasPermi="'colony:add'" @click="openAdd">
          <el-icon><Plus /></el-icon>新增记录
        </el-button>
      </div>
      <el-table :data="list" v-loading="loading" border stripe>
        <el-table-column prop="hiveCode" label="蜂箱" width="110" />
        <el-table-column prop="recordDate" label="记录日期" width="120" />
        <el-table-column prop="colonyStrength" label="群势(框)" width="100" />
        <el-table-column prop="broodFrame" label="子脾(框)" width="100" />
        <el-table-column prop="honeyFrame" label="蜜脾(框)" width="100" />
        <el-table-column prop="beeCount" label="蜂量" width="100" />
        <el-table-column prop="remark" label="备注" min-width="140" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button
              link
              type="primary"
              v-hasPermi="'colony:edit'"
              @click="openEdit(row)"
              >修改</el-button
            >
            <el-button
              link
              type="danger"
              v-hasPermi="'colony:remove'"
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
        <el-form-item label="记录日期" prop="recordDate">
          <el-date-picker
            v-model="form.recordDate"
            type="date"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="群势(框)">
          <el-input-number v-model="form.colonyStrength" :min="0" :max="30" />
        </el-form-item>
        <el-form-item label="子脾(框)">
          <el-input-number v-model="form.broodFrame" :min="0" :max="30" />
        </el-form-item>
        <el-form-item label="蜜脾(框)">
          <el-input-number v-model="form.honeyFrame" :min="0" :max="30" />
        </el-form-item>
        <el-form-item label="蜂量">
          <el-input v-model="form.beeCount" placeholder="足框/足脾" />
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

const list = ref([])
const total = ref(0)
const loading = ref(false)
const hives = ref([])
const formRef = ref()
const query = reactive({ pageNum: 1, pageSize: 10, hiveId: null })
const dialog = reactive({ visible: false, title: '' })
const form = reactive({})
const rules = {
  hiveId: [{ required: true, message: '请选择蜂箱', trigger: 'change' }],
  recordDate: [{ required: true, message: '请选择日期', trigger: 'change' }]
}

async function load() {
  loading.value = true
  try {
    const res = await request.get('/colony/list', { params: query })
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
    recordDate: '',
    colonyStrength: null,
    broodFrame: null,
    honeyFrame: null,
    beeCount: '',
    remark: ''
  })
}
function openAdd() {
  reset()
  dialog.title = '新增记录'
  dialog.visible = true
}
function openEdit(row) {
  Object.assign(form, row)
  dialog.title = '修改记录'
  dialog.visible = true
}
async function submit() {
  if (saving.value) return
  saving.value = true
  try {
    if (!(await validateForm(formRef.value))) return
    if (form.id) await request.put('/colony', form)
    else await request.post('/colony', form)
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
  if (!(await confirmAction('确定删除该记录？', '提示', { type: 'warning' })))
    return
  await request.delete(`/colony/${row.id}`)
  ElMessage.success('删除成功')
  load()
}
onMounted(async () => {
  hives.value = (await request.get('/hive/all')).data
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
