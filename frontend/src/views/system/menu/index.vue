<template>
  <div class="app-container">
    <el-card class="table-card">
      <div class="toolbar">
        <el-button type="primary" v-hasPermi="'menu:add'" @click="openAdd">
          <el-icon><Plus /></el-icon>新增菜单
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
        <el-table-column prop="menuName" label="菜单名称" min-width="160" />
        <el-table-column label="类型" width="80">
          <template #default="{ row }">
            <el-tag size="small">{{
              row.menuType === 'M'
                ? '目录'
                : row.menuType === 'C'
                  ? '菜单'
                  : '按钮'
            }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="perms" label="权限标识" min-width="140" />
        <el-table-column prop="path" label="路由" width="110" />
        <el-table-column prop="component" label="组件" min-width="150" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button
              link
              type="primary"
              v-hasPermi="'menu:edit'"
              @click="openEdit(row)"
              >修改</el-button
            >
            <el-button
              link
              type="danger"
              v-hasPermi="'menu:remove'"
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
      width="560px"
      @closed="formRef?.clearValidate()"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="auto">
        <el-form-item label="上级菜单">
          <el-select v-model="form.parentId" style="width: 100%">
            <el-option label="顶级" :value="0" />
            <el-option
              v-for="m in flatMenus"
              :key="m.id"
              :label="m.menuName"
              :value="m.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="类型">
          <el-radio-group v-model="form.menuType">
            <el-radio value="M">目录</el-radio
            ><el-radio value="C">菜单</el-radio>
            <el-radio value="F">按钮</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="菜单名称" prop="menuName">
          <el-input v-model="form.menuName" />
        </el-form-item>
        <el-form-item label="权限标识">
          <el-input v-model="form.perms" placeholder="如 hive:add" />
        </el-form-item>
        <el-form-item label="路由地址">
          <el-input v-model="form.path" />
        </el-form-item>
        <el-form-item label="组件路径">
          <el-input v-model="form.component" placeholder="如 base/hive/index" />
        </el-form-item>
        <el-form-item label="图标">
          <el-input v-model="form.icon" />
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
  menuName: [{ required: true, message: '请输入菜单名称', trigger: 'blur' }]
}

const flatMenus = computed(() => {
  const arr = []
  const walk = (list) =>
    list.forEach((m) => {
      arr.push(m)
      m.children && walk(m.children)
    })
  walk(tree.value)
  return arr
})

async function load() {
  loading.value = true
  try {
    tree.value = (await request.get('/menu/list')).data
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
    menuType: 'C',
    menuName: '',
    perms: '',
    path: '',
    component: '',
    icon: '',
    visible: 1,
    status: 1,
    orderNum: 1
  })
}
function openAdd() {
  reset()
  dialog.title = '新增菜单'
  dialog.visible = true
}
function openEdit(row) {
  Object.assign(form, row)
  dialog.title = '修改菜单'
  dialog.visible = true
}
async function submit() {
  if (saving.value) return
  saving.value = true
  try {
    if (!(await validateForm(formRef.value))) return
    if (form.id) await request.put('/menu', form)
    else await request.post('/menu', form)
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
  if (!(await confirmAction('确定删除该菜单？', '提示', { type: 'warning' })))
    return
  await request.delete(`/menu/${row.id}`)
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
