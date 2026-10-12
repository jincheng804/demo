<template>
  <div class="app-container">
    <el-form class="search-form" inline :model="query">
      <el-form-item label="用户名"
        ><el-input v-model="query.username" clearable
      /></el-form-item>
      <el-form-item label="姓名"
        ><el-input v-model="query.realName" clearable
      /></el-form-item>
      <el-form-item>
        <el-button type="primary" @click="onSearch">查询</el-button>
      </el-form-item>
    </el-form>

    <el-card class="table-card">
      <div class="toolbar">
        <el-button type="primary" v-hasPermi="'user:add'" @click="openAdd">
          <el-icon><Plus /></el-icon>新增用户
        </el-button>
      </div>
      <el-table :data="list" v-loading="loading" border stripe>
        <el-table-column prop="username" label="账号" width="120" />
        <el-table-column prop="realName" label="姓名" width="110" />
        <el-table-column prop="deptName" label="部门" width="120" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '正常' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button
              link
              type="primary"
              v-hasPermi="'user:edit'"
              @click="openEdit(row)"
              >修改</el-button
            >
            <el-button
              link
              type="warning"
              v-hasPermi="'user:reset'"
              @click="resetPwd(row)"
              >重置密码</el-button
            >
            <el-button
              link
              type="danger"
              v-hasPermi="'user:remove'"
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
      width="560px"
      @closed="formRef?.clearValidate()"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="auto">
        <el-form-item label="账号" prop="username" v-if="!form.id">
          <el-input v-model="form.username" />
        </el-form-item>
        <el-form-item label="初始密码" v-if="!form.id">
          <el-input v-model="form.password" placeholder="留空默认123456" />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="form.realName" />
        </el-form-item>
        <el-form-item label="部门">
          <el-select v-model="form.deptId" clearable style="width: 100%">
            <el-option
              v-for="d in depts"
              :key="d.id"
              :label="d.deptName"
              :value="d.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="form.roleIds" multiple style="width: 100%">
            <el-option
              v-for="r in roles"
              :key="r.id"
              :label="r.roleName"
              :value="r.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch
            v-model="form.status"
            :active-value="1"
            :inactive-value="0"
          />
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
import { ElMessage, ElMessageBox } from 'element-plus'

const list = ref([])
const total = ref(0)
const loading = ref(false)
const depts = ref([])
const roles = ref([])
const formRef = ref()
const query = reactive({
  pageNum: 1,
  pageSize: 10,
  username: '',
  realName: ''
})
const dialog = reactive({ visible: false, title: '' })
const form = reactive({})
const rules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }]
}

async function load() {
  loading.value = true
  try {
    const res = await request.get('/user/list', { params: query })
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
    username: '',
    password: '',
    realName: '',
    deptId: null,
    roleIds: [],
    phone: '',
    status: 1
  })
}
function openAdd() {
  reset()
  dialog.title = '新增用户'
  dialog.visible = true
}
async function openEdit(row) {
  const d = await request.get(`/user/${row.id}`)
  Object.assign(form, d.data)
  dialog.title = '修改用户'
  dialog.visible = true
}
async function submit() {
  if (saving.value) return
  saving.value = true
  try {
    if (!(await validateForm(formRef.value))) return
    if (form.id) await request.put('/user', form)
    else await request.post('/user', form)
    ElMessage.success('操作成功')
    dialog.visible = false
    load()
  } catch (error) {
    // 请求失败已由拦截器提示，表单内容保留供重试。
  } finally {
    saving.value = false
  }
}
async function resetPwd(row) {
  let result
  try {
    result = await ElMessageBox.prompt('请输入新密码', '重置密码', {
      inputPlaceholder: '留空则重置为123456',
      inputType: 'password'
    })
  } catch (error) {
    if (error === 'cancel' || error === 'close') return
    throw error
  }
  const { value } = result
  await request.put('/user/resetPwd', { id: row.id, password: value || '' })
  ElMessage.success('密码已重置')
}
async function remove(row) {
  if (
    !(await confirmAction(`确定删除用户【${row.username}】？`, '提示', {
      type: 'warning'
    }))
  )
    return
  await request.delete(`/user/${row.id}`)
  ElMessage.success('删除成功')
  load()
}
onMounted(async () => {
  depts.value = (await request.get('/dept/list')).data
  roles.value = (await request.get('/role/all')).data
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
