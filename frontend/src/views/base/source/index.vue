<template>
  <div class="app-container">
    <el-card class="table-card">
      <div class="toolbar">
        <el-button type="primary" v-hasPermi="'source:add'" @click="openAdd">
          <el-icon><Plus /></el-icon>新增蜜源地
        </el-button>
      </div>
      <el-table :data="list" v-loading="loading" border stripe>
        <el-table-column prop="sourceName" label="蜜源地名称" min-width="130" />
        <el-table-column prop="plant" label="蜜源植物" width="100" />
        <el-table-column prop="location" label="地点" min-width="130" />
        <el-table-column label="花期" min-width="200">
          <template #default="{ row }">
            {{ row.bloomStart }} 至 {{ row.bloomEnd }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button
              link
              type="primary"
              v-hasPermi="'source:edit'"
              @click="openEdit(row)"
              >修改</el-button
            >
            <el-button
              link
              type="danger"
              v-hasPermi="'source:remove'"
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
      width="520px"
      @closed="formRef?.clearValidate()"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="auto">
        <el-form-item label="蜜源地名称" prop="sourceName">
          <el-input v-model="form.sourceName" />
        </el-form-item>
        <el-form-item label="蜜源植物">
          <el-input v-model="form.plant" placeholder="洋槐/荆条/油菜" />
        </el-form-item>
        <el-form-item label="地点">
          <el-input v-model="form.location" />
        </el-form-item>
        <el-form-item label="花期">
          <el-date-picker
            v-model="bloomRange"
            type="daterange"
            start-placeholder="开始"
            end-placeholder="结束"
            value-format="YYYY-MM-DD"
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

const list = ref([])
const loading = ref(false)
const formRef = ref()
const bloomRange = ref([])
const dialog = reactive({ visible: false, title: '' })
const form = reactive({})
const rules = {
  sourceName: [{ required: true, message: '请输入蜜源地名称', trigger: 'blur' }]
}

async function load() {
  loading.value = true
  try {
    const res = await request.get('/source/list')
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
    sourceName: '',
    plant: '',
    location: '',
    bloomStart: '',
    bloomEnd: '',
    remark: ''
  })
  bloomRange.value = []
}
function openAdd() {
  reset()
  dialog.title = '新增蜜源地'
  dialog.visible = true
}
function openEdit(row) {
  Object.assign(form, row)
  bloomRange.value = [row.bloomStart, row.bloomEnd]
  dialog.title = '修改蜜源地'
  dialog.visible = true
}
async function submit() {
  if (saving.value) return
  saving.value = true
  try {
    if (!(await validateForm(formRef.value))) return
    if (bloomRange.value.length === 2) {
      form.bloomStart = bloomRange.value[0]
      form.bloomEnd = bloomRange.value[1]
    }
    if (form.id) await request.put('/source', form)
    else await request.post('/source', form)
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
    !(await confirmAction(`确定删除蜜源地【${row.sourceName}】？`, '提示', {
      type: 'warning'
    }))
  )
    return
  await request.delete(`/source/${row.id}`)
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
