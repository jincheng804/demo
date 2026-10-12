<template>
  <div class="app-container">
    <el-card class="table-card">
      <div class="toolbar">
        <el-button type="primary" v-hasPermi="'role:add'" @click="openAdd">
          <el-icon><Plus /></el-icon>新增角色
        </el-button>
      </div>
      <el-table :data="list" v-loading="loading" border stripe>
        <el-table-column prop="roleName" label="角色名称" width="150" />
        <el-table-column prop="roleCode" label="角色编码" width="150" />
        <el-table-column label="数据范围" width="120">
          <template #default="{ row }">
            {{
              row.dataScope === 1
                ? '全部数据'
                : row.dataScope === 2
                  ? '本部门'
                  : '本人数据'
            }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '正常' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="150" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button
              link
              type="primary"
              v-hasPermi="'role:edit'"
              @click="openEdit(row)"
              >修改</el-button
            >
            <el-button
              link
              type="danger"
              v-hasPermi="'role:remove'"
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
      width="640px"
      @closed="formRef?.clearValidate()"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="auto">
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="form.roleName" />
        </el-form-item>
        <el-form-item label="角色编码" prop="roleCode">
          <el-input v-model="form.roleCode" />
        </el-form-item>
        <el-form-item label="数据范围">
          <el-select v-model="form.dataScope">
            <el-option label="全部数据" :value="1" /><el-option
              label="本部门"
              :value="2"
            />
            <el-option label="本人数据" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="菜单权限">
          <el-tree
            :data="menuTreeData"
            show-checkbox
            node-key="id"
            :default-checked-keys="form.menuIds"
            :props="{ label: 'menuName', children: 'children' }"
            ref="treeRef"
            default-expand-all
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
import { ref, reactive, onMounted, nextTick } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const list = ref([])
const loading = ref(false)
const formRef = ref()
const treeRef = ref()
const menuTreeData = ref([])
const dialog = reactive({ visible: false, title: '' })
const form = reactive({})
const rules = {
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  roleCode: [{ required: true, message: '请输入角色编码', trigger: 'blur' }]
}

async function load() {
  loading.value = true
  try {
    list.value = (await request.get('/role/list')).data
  } catch (error) {
    // 请求拦截器已展示错误，保留当前列表。
  } finally {
    loading.value = false
  }
}
function reset() {
  Object.assign(form, {
    id: null,
    roleName: '',
    roleCode: '',
    dataScope: 1,
    menuIds: [],
    remark: ''
  })
}
async function openAdd() {
  reset()
  dialog.title = '新增角色'
  dialog.visible = true
  await nextTick()
  treeRef.value && treeRef.value.setCheckedKeys([])
}
async function openEdit(row) {
  const d = await request.get(`/role/${row.id}`)
  Object.assign(form, d.data)
  dialog.title = '修改角色'
  dialog.visible = true
  await nextTick()
  treeRef.value.setCheckedKeys(form.menuIds || [])
}
async function submit() {
  if (saving.value) return
  saving.value = true
  try {
    if (!(await validateForm(formRef.value))) return
    form.menuIds = treeRef.value.getCheckedKeys()
    // 半选父节点也包含
    const half = treeRef.value.getHalfCheckedKeys()
    form.menuIds = [...form.menuIds, ...half]
    if (form.id) await request.put('/role', form)
    else await request.post('/role', form)
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
    !(await confirmAction(`确定删除角色【${row.roleName}】？`, '提示', {
      type: 'warning'
    }))
  )
    return
  await request.delete(`/role/${row.id}`)
  ElMessage.success('删除成功')
  load()
}

onMounted(async () => {
  menuTreeData.value = (await request.get('/menu/list')).data
  load()
})

const saving = ref(false)
</script>
<style scoped>
.toolbar {
  margin-bottom: 12px;
}
</style>
