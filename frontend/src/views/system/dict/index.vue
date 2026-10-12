<template>
  <div class="app-container">
    <el-card class="table-card">
      <el-tabs v-model="activeTab">
        <!-- 字典类型 -->
        <el-tab-pane label="字典类型" name="type">
          <el-button type="primary" size="small" @click="openType(null)"
            >新增类型</el-button
          >
          <el-table :data="types" border style="margin-top: 10px">
            <el-table-column prop="dictName" label="名称" />
            <el-table-column prop="dictType" label="类型编码" />
            <el-table-column label="操作" width="180">
              <template #default="{ row }">
                <el-button link type="primary" @click="openType(row)"
                  >修改</el-button
                >
                <el-button link type="success" @click="viewData(row)"
                  >查看数据</el-button
                >
                <el-button link type="danger" @click="delType(row)"
                  >删除</el-button
                >
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- 字典数据 -->
        <el-tab-pane :label="dataTitle" name="data">
          <el-button type="primary" size="small" @click="openData(null)"
            >新增数据</el-button
          >
          <el-table :data="dataList" border style="margin-top: 10px">
            <el-table-column prop="dictLabel" label="标签" />
            <el-table-column prop="dictValue" label="键值" />
            <el-table-column prop="orderNum" label="排序" width="80" />
            <el-table-column label="操作" width="140">
              <template #default="{ row }">
                <el-button link type="primary" @click="openData(row)"
                  >修改</el-button
                >
                <el-button link type="danger" @click="delData(row)"
                  >删除</el-button
                >
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <el-dialog
      align-center
      v-model="typeDialog"
      :title="typeForm.id ? '修改类型' : '新增类型'"
      width="440px"
    >
      <el-form :model="typeForm" label-width="auto">
        <el-form-item label="名称"
          ><el-input v-model="typeForm.dictName"
        /></el-form-item>
        <el-form-item label="编码"
          ><el-input v-model="typeForm.dictType"
        /></el-form-item>
        <el-form-item label="备注"
          ><el-input v-model="typeForm.remark"
        /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="typeDialog = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveType"
          >确定</el-button
        >
      </template>
    </el-dialog>

    <el-dialog
      align-center
      v-model="dataDialog"
      :title="dataForm.id ? '修改数据' : '新增数据'"
      width="440px"
    >
      <el-form :model="dataForm" label-width="auto">
        <el-form-item label="标签"
          ><el-input v-model="dataForm.dictLabel"
        /></el-form-item>
        <el-form-item label="键值"
          ><el-input v-model="dataForm.dictValue"
        /></el-form-item>
        <el-form-item label="排序"
          ><el-input-number v-model="dataForm.orderNum" :min="0"
        /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dataDialog = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveData"
          >确定</el-button
        >
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { loadAllRows } from '@/utils/pagination'
import { confirmAction } from '@/utils/interaction'
import { ref, reactive, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const activeTab = ref('type')
const types = ref([])
const dataList = ref([])
const currentType = ref(null)
const dataTitle = ref('字典数据')

const typeDialog = ref(false)
const typeForm = reactive({})
const dataDialog = ref(false)
const dataForm = reactive({})

async function loadTypes() {
  types.value = await loadAllRows('/dict/type/list')
}
function openType(row) {
  Object.assign(
    typeForm,
    row || { id: null, dictName: '', dictType: '', remark: '' }
  )
  typeDialog.value = true
}
async function saveType() {
  if (saving.value) return
  saving.value = true
  try {
    if (typeForm.id) await request.put('/dict/type', typeForm)
    else await request.post('/dict/type', typeForm)
    ElMessage.success('保存成功')
    typeDialog.value = false
    loadTypes()
  } catch (error) {
    // 请求失败已由拦截器提示，表单内容保留供重试。
  } finally {
    saving.value = false
  }
}
async function delType(row) {
  if (!(await confirmAction('确定删除？', '提示', { type: 'warning' }))) return
  await request.delete(`/dict/type/${row.id}`)
  loadTypes()
}
async function viewData(row) {
  currentType.value = row
  dataTitle.value = '数据 - ' + row.dictName
  activeTab.value = 'data'
  dataList.value = (
    await request.get('/dict/data/list', { params: { dictType: row.dictType } })
  ).data
}
function openData(row) {
  Object.assign(
    dataForm,
    row || {
      id: null,
      dictType: currentType.value.dictType,
      dictLabel: '',
      dictValue: '',
      orderNum: 1
    }
  )
  dataDialog.value = true
}
async function saveData() {
  if (saving.value) return
  saving.value = true
  try {
    if (dataForm.id) await request.put('/dict/data', dataForm)
    else await request.post('/dict/data', dataForm)
    ElMessage.success('保存成功')
    dataDialog.value = false
    viewData(currentType.value)
  } catch (error) {
    // 请求失败已由拦截器提示，表单内容保留供重试。
  } finally {
    saving.value = false
  }
}
async function delData(row) {
  if (!(await confirmAction('确定删除？', '提示', { type: 'warning' }))) return
  await request.delete(`/dict/data/${row.id}`)
  viewData(currentType.value)
}
onMounted(loadTypes)

const saving = ref(false)
</script>
