<template>
  <div class="app-container">
    <el-card shadow="never">
      <el-form class="search-form" inline :model="query">
        <el-form-item label="预警类型">
          <el-select
            v-model="query.warningType"
            placeholder="全部"
            clearable
            style="width: 150px"
          >
            <el-option
              v-for="t in typeOptions"
              :key="t"
              :label="t"
              :value="t"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="预警级别">
          <el-select
            v-model="query.level"
            placeholder="全部"
            clearable
            style="width: 120px"
          >
            <el-option
              v-for="l in levelOptions"
              :key="l.value"
              :label="l.label"
              :value="l.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="处理状态">
          <el-select
            v-model="query.status"
            placeholder="全部"
            clearable
            style="width: 120px"
          >
            <el-option label="未处理" value="0" />
            <el-option label="已处理" value="1" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="onSearch">
            <el-icon><Search /></el-icon>查询
          </el-button>
          <el-button @click="resetQuery">重置</el-button>
          <el-button type="warning" :loading="regenLoading" @click="regenerate">
            <el-icon><Refresh /></el-icon>重新生成预警
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="table-card">
      <el-table :data="list" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column label="预警类型" width="120">
          <template #default="{ row }">
            <el-tag :type="typeTag(row.warningType)" effect="plain">{{
              row.warningType
            }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="级别" width="90">
          <template #default="{ row }">
            <el-tag :type="levelTag(row.level)">{{
              levelText(row.level)
            }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column
          prop="content"
          label="预警内容"
          min-width="260"
          show-overflow-tooltip
        />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag
              :type="
                row.status === '1' || row.status === 1 ? 'success' : 'danger'
              "
            >
              {{ row.status === '1' || row.status === 1 ? '已处理' : '未处理' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column prop="handleTime" label="处理时间" width="170">
          <template #default="{ row }">{{ row.handleTime || '—' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="110" fixed="right">
          <template #default="{ row }">
            <el-button
              link
              type="primary"
              :disabled="row.status === '1' || row.status === 1"
              v-hasPermi="'warning:handle'"
              @click="openHandle(row)"
              >处理</el-button
            >
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrapper">
        <el-pagination
          background
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          v-model:current-page="query.pageNum"
          v-model:page-size="query.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          @current-change="load"
          @size-change="onSearch"
        />
      </div>
    </el-card>

    <!-- 处理弹窗 -->
    <el-dialog
      align-center
      v-model="handleDialog.visible"
      title="处理预警"
      width="480px"
    >
      <el-alert
        :title="handleDialog.row && handleDialog.row.content"
        type="warning"
        :closable="false"
        style="margin-bottom: 14px"
        show-icon
      />
      <el-form label-width="auto">
        <el-form-item label="处理备注">
          <el-input
            v-model="handleDialog.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入处理说明（选填）"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="handleDialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="handleLoading" @click="doHandle"
          >确认处理</el-button
        >
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { confirmAction } from '@/utils/interaction'
import { ref, reactive, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const list = ref([])
const total = ref(0)
const loading = ref(false)
const regenLoading = ref(false)
const handleLoading = ref(false)
const query = reactive({
  pageNum: 1,
  pageSize: 10,
  warningType: '',
  level: '',
  status: ''
})

const typeOptions = ['休药期', '库存不足', '蜂箱异常', '待质检', '待审核']
const typeTagMap = {
  休药期: 'warning',
  库存不足: 'danger',
  蜂箱异常: 'danger',
  待质检: 'primary',
  待审核: 'warning'
}
const typeTag = (t) => typeTagMap[t] || 'info'

const levelOptions = [
  { label: '紧急', value: '1' },
  { label: '严重', value: '2' },
  { label: '提示', value: '3' }
]
const levelTag = (l) =>
  ({ 1: 'danger', 2: 'warning', 3: 'info' })[String(l)] || 'info'
const levelText = (l) => ({ 1: '紧急', 2: '严重', 3: '提示' })[String(l)] || l

const handleDialog = reactive({ visible: false, row: null, remark: '' })

async function load() {
  loading.value = true
  try {
    const res = await request.get('/warning/list', { params: query })
    list.value = res.rows || []
    total.value = res.total || 0
  } catch (error) {
    /* 请求拦截器已提示 */
  } finally {
    loading.value = false
  }
}
function onSearch() {
  query.pageNum = 1
  load()
}
function resetQuery() {
  Object.assign(query, { pageNum: 1, warningType: '', level: '', status: '' })
  load()
}
function openHandle(row) {
  handleDialog.row = row
  handleDialog.remark = ''
  handleDialog.visible = true
}
async function doHandle() {
  handleLoading.value = true
  try {
    await request.post(`/warning/handle/${handleDialog.row.id}`, {
      remark: handleDialog.remark
    })
    ElMessage.success('已处理')
    handleDialog.visible = false
    load()
  } finally {
    handleLoading.value = false
  }
}
async function regenerate() {
  if (
    !(await confirmAction(
      '将按实时规则（休药期/库存不足/蜂箱异常/待质检/待审核）补录未处理预警，是否继续？',
      '重新生成预警',
      { type: 'warning' }
    ))
  )
    return
  regenLoading.value = true
  try {
    await request.post('/warning/regenerate')
    ElMessage.success('预警已重新生成')
    onSearch()
  } finally {
    regenLoading.value = false
  }
}

onMounted(load)
</script>
<style scoped>
.search-form {
  margin-bottom: 4px;
}
</style>
