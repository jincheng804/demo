<template>
  <div class="app-container">
    <el-card class="table-card">
      <div class="toolbar">
        <el-button type="primary" v-hasPermi="'apiary:add'" @click="openAdd">
          <el-icon><Plus /></el-icon>新增分区
        </el-button>
      </div>
      <el-table :data="list" v-loading="loading" border stripe>
        <el-table-column prop="apiaryName" label="分区名称" min-width="140" />
        <el-table-column prop="location" label="位置" min-width="160" />
        <el-table-column prop="manager" label="负责人" width="100" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="120" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button
              link
              type="primary"
              v-hasPermi="'apiary:edit'"
              @click="openEdit(row)"
              >修改</el-button
            >
            <el-button
              link
              type="danger"
              v-hasPermi="'apiary:remove'"
              @click="remove(row)"
              >删除</el-button
            >
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog
      align-center
      v-model="dialog.visible"
      :title="dialog.title"
      width="500px"
      @closed="formRef?.clearValidate()"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="auto">
        <el-form-item label="分区名称" prop="apiaryName">
          <el-input v-model="form.apiaryName" />
        </el-form-item>
        <el-form-item label="位置">
          <el-input v-model="form.location" />
        </el-form-item>
        <el-form-item label="负责人">
          <el-input v-model="form.manager" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch
            v-model="form.status"
            :active-value="1"
            :inactive-value="0"
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

const list = ref([])
const loading = ref(false)
const formRef = ref()

const dialog = reactive({ visible: false, title: '' })
const form = reactive({})

const rules = {
  apiaryName: [{ required: true, message: '请输入分区名称', trigger: 'blur' }]
}

async function load() {
  loading.value = true
  try {
    const res = await request.get('/apiary/list')
    list.value = res.data
  } catch (error) {
    // 请求拦截器已展示错误，保留当前列表。
  } finally {
    loading.value = false
  }
}

function reset() {
  Object.assign(form, {
    id: null,
    apiaryName: '',
    location: '',
    manager: '',
    status: 1,
    remark: ''
  })
}

function openAdd() {
  reset()
  dialog.title = '新增分区'
  dialog.visible = true
}
function openEdit(row) {
  Object.assign(form, row)
  dialog.title = '修改分区'
  dialog.visible = true
}
async function submit() {
  if (saving.value) return
  saving.value = true
  try {
    if (!(await validateForm(formRef.value))) return
    if (form.id) {
      await request.put('/apiary', form)
    } else {
      await request.post('/apiary', form)
    }
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
    !(await confirmAction(`确定删除分区【${row.apiaryName}】？`, '提示', {
      type: 'warning'
    }))
  )
    return
  await request.delete(`/apiary/${row.id}`)
  ElMessage.success('删除成功')
  load()
}

onMounted(load)

const saving = ref(false)
</script>
<style scoped>
.toolbar {
  margin-bottom: 12px;
}
</style>
