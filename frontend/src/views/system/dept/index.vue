<template>
  <div class="app-container">
    <el-card class="table-card">
      <div class="toolbar">
        <el-button type="primary" v-hasPermi="'dept:add'" @click="openAdd">
          <el-icon><Plus /></el-icon>新增部门
        </el-button>
      </div>
      <el-table
        :data="tree"
        v-loading="loading"
        border
        row-key="id"
        :tree-props="{ children: 'children' }"
        default-expand-all
      >
        <el-table-column prop="deptName" label="部门名称" min-width="180" />
        <el-table-column prop="leader" label="负责人" width="120" />
        <el-table-column prop="phone" label="联系电话" width="140" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '正常' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button
              link
              type="primary"
              v-hasPermi="'dept:edit'"
              @click="openEdit(row)"
              >修改</el-button
            >
            <el-button
              link
              type="danger"
              v-hasPermi="'dept:remove'"
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
        <el-form-item label="上级部门">
          <el-select v-model="form.parentId" style="width: 100%">
            <el-option label="顶级部门" :value="0" />
            <el-option
              v-for="d in flatDepts"
              :key="d.id"
              :label="d.deptName"
              :value="d.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="部门名称" prop="deptName">
          <el-input v-model="form.deptName" />
        </el-form-item>
        <el-form-item label="负责人">
          <el-input v-model="form.leader" />
        </el-form-item>
        <el-form-item label="电话">
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
import { ref, reactive, computed, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const tree = ref([])
const loading = ref(false)
const formRef = ref()
const dialog = reactive({ visible: false, title: '' })
const form = reactive({})
const rules = {
  deptName: [{ required: true, message: '请输入部门名称', trigger: 'blur' }]
}

const flatDepts = computed(() => {
  const arr = []
  const walk = (list) =>
    list.forEach((d) => {
      arr.push(d)
      d.children && walk(d.children)
    })
  walk(tree.value)
  return arr
})

async function load() {
  loading.value = true
  try {
    tree.value = (await request.get('/dept/list')).data
  } catch (error) {
    // 请求拦截器已展示错误，保留当前列表。
  } finally {
    loading.value = false
  }
}
function reset() {
  Object.assign(form, {
    id: null,
    parentId: 0,
    deptName: '',
    leader: '',
    phone: '',
    status: 1
  })
}
function openAdd() {
  reset()
  dialog.title = '新增部门'
  dialog.visible = true
}
function openEdit(row) {
  Object.assign(form, row)
  dialog.title = '修改部门'
  dialog.visible = true
}
async function submit() {
  if (saving.value) return
  saving.value = true
  try {
    if (!(await validateForm(formRef.value))) return
    if (form.id) await request.put('/dept', form)
    else await request.post('/dept', form)
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
    !(await confirmAction(`确定删除部门【${row.deptName}】？`, '提示', {
      type: 'warning'
    }))
  )
    return
  await request.delete(`/dept/${row.id}`)
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
