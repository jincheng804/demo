<template>
  <div class="app-container">
    <el-card class="table-card">
      <div class="toolbar">
        <el-button type="primary" v-hasPermi="'queen:add'" @click="openAdd">
          <el-icon><Plus /></el-icon>新增蜂王
        </el-button>
      </div>
      <el-table
        :data="list"
        v-loading="loading"
        border
        stripe
        style="width: 100%"
      >
        <el-table-column prop="queenCode" label="蜂王编号" min-width="140" />
        <el-table-column prop="hiveCode" label="所在蜂箱" min-width="120" />
        <el-table-column prop="breed" label="品种" min-width="130" />
        <el-table-column prop="birthDate" min-width="120">
          <template #header><GlossaryTip term="出房日期" /></template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag
              :type="
                row.status === 1
                  ? 'success'
                  : row.status === 2
                    ? 'warning'
                    : 'danger'
              "
            >
              {{
                row.status === 1 ? '正常' : row.status === 2 ? '停产' : '淘汰'
              }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" align="center" fixed="right">
          <template #default="{ row }">
            <el-button
              link
              type="primary"
              v-hasPermi="'queen:edit'"
              @click="openEdit(row)"
              >修改</el-button
            >
            <el-button
              link
              type="danger"
              v-hasPermi="'queen:remove'"
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
      width="500px"
      @closed="formRef?.clearValidate()"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="auto">
        <el-form-item label="蜂王编号" prop="queenCode">
          <el-input v-model="form.queenCode" />
        </el-form-item>
        <el-form-item label="所在蜂箱">
          <el-select v-model="form.hiveId" style="width: 100%">
            <el-option
              v-for="h in hives"
              :key="h.id"
              :label="h.hiveCode"
              :value="h.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="品种" prop="breed">
          <el-select
            v-model="form.breed"
            style="width: 100%"
            filterable
            allow-create
            default-first-option
          >
            <el-option label="意蜂" value="意蜂" />
            <el-option label="中蜂" value="中蜂" />
            <el-option label="喀尔巴阡" value="喀尔巴阡" />
            <el-option label="东北黑蜂" value="东北黑蜂" />
            <el-option label="高加索蜂" value="高加索蜂" />
            <el-option label="卡尼鄂拉" value="卡尼鄂拉" />
          </el-select>
        </el-form-item>
        <el-form-item label="出房日期">
          <el-date-picker
            v-model="form.birthDate"
            type="date"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status">
            <el-option label="正常" :value="1" /><el-option
              label="停产"
              :value="2"
            /><el-option label="淘汰" :value="3" />
          </el-select>
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
const query = reactive({ pageNum: 1, pageSize: 10 })
const dialog = reactive({ visible: false, title: '' })
const form = reactive({})
const rules = {
  queenCode: [{ required: true, message: '请输入蜂王编号', trigger: 'blur' }]
}

async function load() {
  loading.value = true
  try {
    const res = await request.get('/queen/list', { params: query })
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
    queenCode: '',
    hiveId: null,
    breed: '',
    birthDate: '',
    status: 1
  })
}
function openAdd() {
  reset()
  dialog.title = '新增蜂王'
  dialog.visible = true
}
function openEdit(row) {
  Object.assign(form, row)
  dialog.title = '修改蜂王'
  dialog.visible = true
}
async function submit() {
  if (saving.value) return
  saving.value = true
  try {
    if (!(await validateForm(formRef.value))) return
    if (form.id) await request.put('/queen', form)
    else await request.post('/queen', form)
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
    !(await confirmAction(`确定删除蜂王【${row.queenCode}】？`, '提示', {
      type: 'warning'
    }))
  )
    return
  await request.delete(`/queen/${row.id}`)
  ElMessage.success('删除成功')
  load()
}
onMounted(async () => {
  hives.value = (await request.get('/hive/all')).data
  load()
})

const saving = ref(false)
</script>
<style scoped>
.toolbar {
  margin-bottom: 12px;
}
</style>
