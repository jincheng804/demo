<template>
  <div class="app-container">
    <el-form class="search-form" inline :model="query">
      <el-form-item label="蜂箱编号">
        <el-input v-model="query.hiveCode" placeholder="请输入" clearable />
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
      <el-form-item label="状态">
        <el-select
          v-model="query.status"
          placeholder="全部"
          clearable
          style="width: 110px"
        >
          <el-option label="正常" :value="1" /><el-option
            label="空箱"
            :value="2"
          />
          <el-option label="维修" :value="3" /><el-option
            label="淘汰"
            :value="4"
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
        <el-button type="primary" v-hasPermi="'hive:add'" @click="openAdd">
          <el-icon><Plus /></el-icon>新增蜂箱
        </el-button>
      </div>
      <el-table :data="list" v-loading="loading" border stripe>
        <el-table-column prop="hiveCode" label="蜂箱编号" width="110" />
        <el-table-column prop="apiaryName" label="所属分区" width="130" />
        <el-table-column prop="boxType" label="蜂箱型号" width="110" />
        <el-table-column prop="location" label="摆放位置" min-width="120" />
        <el-table-column prop="entryDate" label="入栏日期" width="120" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.status)">{{
              statusText(row.status)
            }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button
              link
              type="primary"
              v-hasPermi="'hive:edit'"
              @click="openEdit(row)"
              >修改</el-button
            >
            <el-button
              link
              type="danger"
              v-hasPermi="'hive:remove'"
              @click="remove(row)"
              >删除</el-button
            >
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrapper">
        <el-pagination
          background
          layout="total, prev, pager, next, jumper"
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
        <el-form-item label="蜂箱编号" prop="hiveCode">
          <el-input v-model="form.hiveCode" />
        </el-form-item>
        <el-form-item label="所属分区">
          <el-select v-model="form.apiaryId" style="width: 100%">
            <el-option
              v-for="a in apiaries"
              :key="a.id"
              :label="a.apiaryName"
              :value="a.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="蜂箱型号">
          <el-select v-model="form.boxType" style="width: 100%">
            <el-option label="朗式十框" value="朗式十框" /><el-option
              label="意标箱"
              value="意标箱"
            />
            <el-option label="中蜂箱" value="中蜂箱" />
          </el-select>
        </el-form-item>
        <el-form-item label="摆放位置">
          <el-input v-model="form.location" />
        </el-form-item>
        <el-form-item label="入栏日期">
          <el-date-picker
            v-model="form.entryDate"
            type="date"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status">
            <el-option label="正常" :value="1" /><el-option
              label="空箱"
              :value="2"
            />
            <el-option label="维修" :value="3" /><el-option
              label="淘汰"
              :value="4"
            />
          </el-select>
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
const apiaries = ref([])
const formRef = ref()
const query = reactive({
  pageNum: 1,
  pageSize: 10,
  hiveCode: '',
  apiaryId: null,
  status: null
})
const dialog = reactive({ visible: false, title: '' })
const form = reactive({})
const rules = {
  hiveCode: [{ required: true, message: '请输入蜂箱编号', trigger: 'blur' }]
}

const statusMap = {
  1: ['正常', 'success'],
  2: ['空箱', 'info'],
  3: ['维修', 'warning'],
  4: ['淘汰', 'danger']
}
const statusText = (s) => (statusMap[s] || [''])[0]
const statusTag = (s) => (statusMap[s] || ['', ''])[1]

async function load() {
  loading.value = true
  try {
    const res = await request.get('/hive/list', { params: query })
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
    hiveCode: '',
    apiaryId: null,
    status: null
  })
  load()
}
function reset() {
  Object.assign(form, {
    id: null,
    hiveCode: '',
    apiaryId: null,
    boxType: '朗式十框',
    location: '',
    entryDate: '',
    status: 1,
    remark: ''
  })
}
function openAdd() {
  reset()
  dialog.title = '新增蜂箱'
  dialog.visible = true
}
function openEdit(row) {
  Object.assign(form, row)
  dialog.title = '修改蜂箱'
  dialog.visible = true
}
async function submit() {
  if (saving.value) return
  saving.value = true
  try {
    if (!(await validateForm(formRef.value))) return
    if (form.id) await request.put('/hive', form)
    else await request.post('/hive', form)
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
    !(await confirmAction(`确定删除蜂箱【${row.hiveCode}】？`, '提示', {
      type: 'warning'
    }))
  )
    return
  await request.delete(`/hive/${row.id}`)
  ElMessage.success('删除成功')
  load()
}

onMounted(async () => {
  const a = await request.get('/apiary/list')
  apiaries.value = a.data
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
