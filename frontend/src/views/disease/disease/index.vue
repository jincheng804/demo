<template>
  <div class="app-container">
    <el-form class="search-form" inline :model="query">
      <el-form-item label="处理状态">
        <el-select
          v-model="query.handleStatus"
          placeholder="全部"
          clearable
          style="width: 120px"
        >
          <el-option label="未处理" :value="0" /><el-option
            label="已处理"
            :value="1"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="onSearch">查询</el-button>
      </el-form-item>
    </el-form>

    <el-card class="table-card">
      <div class="toolbar">
        <el-button type="primary" v-hasPermi="'disease:add'" @click="openAdd">
          <el-icon><Plus /></el-icon>病害登记
        </el-button>
      </div>
      <el-table :data="list" v-loading="loading" border stripe>
        <el-table-column prop="hiveCode" label="蜂箱" width="110" />
        <el-table-column prop="diseaseType" label="病害类型" width="140" />
        <el-table-column prop="diseaseDate" label="发现日期" width="120" />
        <el-table-column label="严重程度" width="100">
          <template #default="{ row }">
            <el-tag
              :type="
                row.level === 3
                  ? 'danger'
                  : row.level === 2
                    ? 'warning'
                    : 'info'
              "
            >
              {{ row.level === 1 ? '轻' : row.level === 2 ? '中' : '重' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="处理状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.handleStatus === 1 ? 'success' : 'danger'">
              {{ row.handleStatus === 1 ? '已处理' : '未处理' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="140" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button
              link
              type="success"
              v-hasPermi="'disease:edit'"
              @click="markHandled(row)"
              :disabled="row.handleStatus === 1"
              >标记处理</el-button
            >
            <el-button
              link
              type="danger"
              v-hasPermi="'disease:remove'"
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
      title="病害登记"
      width="500px"
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
        <el-form-item label="病害类型" prop="diseaseType">
          <el-select v-model="form.diseaseType" style="width: 100%">
            <el-option label="蜂螨" value="蜂螨" /><el-option
              label="白垩病"
              value="白垩病"
            />
            <el-option label="囊状幼虫病" value="囊状幼虫病" /><el-option
              label="爬蜂病"
              value="爬蜂病"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="发现日期" prop="diseaseDate">
          <el-date-picker
            v-model="form.diseaseDate"
            type="date"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="严重程度">
          <el-radio-group v-model="form.level">
            <el-radio :value="1">轻</el-radio><el-radio :value="2">中</el-radio
            ><el-radio :value="3">重</el-radio>
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

    <!-- 标记处理弹窗 -->
    <el-dialog
      align-center
      v-model="handleDialog.visible"
      title="标记处理"
      width="460px"
    >
      <el-form label-width="auto">
        <el-form-item label="病害">
          <span
            >{{ handleDialog.row?.diseaseType }}（{{
              handleDialog.row?.hiveCode
            }}）</span
          >
        </el-form-item>
        <el-form-item label="处理措施">
          <el-input
            v-model="handleDialog.remark"
            type="textarea"
            :rows="3"
            placeholder="请描述处理措施，如：用药、换箱、隔离等"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="handleDialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="confirmHandle"
          >确认处理</el-button
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
const query = reactive({ pageNum: 1, pageSize: 10, handleStatus: null })
const dialog = reactive({ visible: false })
const form = reactive({})
const handleDialog = reactive({ visible: false, row: null, remark: '' })
const rules = {
  hiveId: [{ required: true, message: '请选择蜂箱', trigger: 'change' }],
  diseaseType: [
    { required: true, message: '请选择病害类型', trigger: 'change' }
  ],
  diseaseDate: [{ required: true, message: '请选择日期', trigger: 'change' }]
}

async function load() {
  loading.value = true
  try {
    const res = await request.get('/disease/list', { params: query })
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
    diseaseType: '',
    diseaseDate: '',
    level: 1,
    handleStatus: 0,
    remark: ''
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
    await request.post('/disease', form)
    ElMessage.success('登记成功')
    dialog.visible = false
    load()
  } catch (error) {
    // 请求失败已由拦截器提示，表单内容保留供重试。
  } finally {
    saving.value = false
  }
}
async function markHandled(row) {
  handleDialog.row = row
  handleDialog.remark = ''
  handleDialog.visible = true
}
async function confirmHandle() {
  if (saving.value) return
  saving.value = true
  try {
    await request.put('/disease', {
      id: handleDialog.row.id,
      handleStatus: 1,
      remark: handleDialog.remark
    })
    ElMessage.success('已标记处理')
    handleDialog.visible = false
    load()
  } catch (error) {
    // 请求失败已由拦截器提示，表单内容保留供重试。
  } finally {
    saving.value = false
  }
}
async function remove(row) {
  if (
    !(await confirmAction('确定删除该病害记录？', '提示', { type: 'warning' }))
  )
    return
  await request.delete(`/disease/${row.id}`)
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
