<template>
  <div class="app-container">
    <el-card class="table-card">
      <div class="toolbar">
        <el-button type="primary" v-hasPermi="'qc:add'" @click="openAdd">
          <el-icon><Plus /></el-icon>质检录入
        </el-button>
      </div>
      <el-table :data="list" v-loading="loading" border stripe>
        <el-table-column prop="productBatchNo" label="产品批次" width="160" />
        <el-table-column
          prop="productName"
          label="产品名称"
          width="120"
          show-overflow-tooltip
        />
        <el-table-column prop="qcItems" min-width="200">
          <template #header
            ><GlossaryTip
              term="检测项目"
              explanation="蜂蜜质量检测的常规指标，包括蔗糖含量、羟甲基糠醛、水分、淀粉酶值等"
          /></template>
        </el-table-column>
        <el-table-column label="检测结果" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.qcResult === 1 ? 'success' : 'danger'">
              {{ row.qcResult === 1 ? '合格' : '不合格' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="qcValue" min-width="220" show-overflow-tooltip>
          <template #header
            ><GlossaryTip
              term="指标值"
              explanation="各检测项目的具体实测数值，用于判断是否符合国家标准"
          /></template>
        </el-table-column>
        <el-table-column prop="inspector" label="检测人" width="100" />
        <el-table-column prop="qcDate" label="检测时间" width="160" />
        <el-table-column label="操作" width="150" align="center" fixed="right">
          <template #default="{ row }">
            <el-button
              link
              type="primary"
              v-hasPermi="'qc:edit'"
              @click="openEdit(row)"
              >修改</el-button
            >
            <el-button link type="info" @click="openDetail(row)"
              >详情</el-button
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

    <!-- 质检录入/修改弹窗 -->
    <el-dialog
      align-center
      v-model="dialog.visible"
      :title="dialog.title"
      width="560px"
      @closed="formRef?.clearValidate()"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="auto">
        <el-form-item label="产品批次" prop="batchId">
          <el-select
            v-model="form.batchId"
            style="width: 100%"
            filterable
            :disabled="!!form.id"
          >
            <el-option
              v-for="p in products"
              :key="p.id"
              :label="
                p.productBatchNo +
                ' ' +
                p.productName +
                (p.qcStatus && p.qcStatus !== 0 ? '（已质检）' : '')
              "
              :value="p.id"
              :disabled="
                p.qcStatus && p.qcStatus !== 0 && p.id !== form.batchId
              "
            />
          </el-select>
        </el-form-item>
        <el-form-item label="检测项目">
          <el-input
            v-model="form.qcItems"
            type="textarea"
            placeholder="蔗糖含量;羟甲基糠醛;水分;淀粉酶值"
          />
        </el-form-item>
        <el-form-item label="检测结果">
          <el-radio-group v-model="form.qcResult">
            <el-radio :value="1">合格</el-radio
            ><el-radio :value="2">不合格</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="指标值">
          <el-input
            v-model="form.qcValue"
            type="textarea"
            placeholder="如：蔗糖≤5g/100g;水分≤20%"
          />
        </el-form-item>
        <el-form-item label="检测人">
          <el-input
            v-model="form.inspector"
            placeholder="默认当前登录人，可修改"
          />
        </el-form-item>
        <el-form-item label="检测时间">
          <el-date-picker
            v-model="form.qcDate"
            type="datetime"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
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

    <!-- 质检详情弹窗（含修改历史时间线） -->
    <el-dialog
      align-center
      v-model="detailDialog.visible"
      title="质检详情"
      width="600px"
    >
      <el-descriptions
        :column="compact ? 1 : 2"
        border
        size="default"
        class="detail-desc"
      >
        <el-descriptions-item label="产品批次">{{
          detailDialog.row?.productBatchNo
        }}</el-descriptions-item>
        <el-descriptions-item label="产品名称">{{
          detailDialog.row?.productName
        }}</el-descriptions-item>
        <el-descriptions-item label="检测结果">
          <el-tag
            :type="detailDialog.row?.qcResult === 1 ? 'success' : 'danger'"
          >
            {{ detailDialog.row?.qcResult === 1 ? '合格' : '不合格' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="检测人">{{
          detailDialog.row?.inspector
        }}</el-descriptions-item>
        <el-descriptions-item label="检测时间">{{
          detailDialog.row?.qcDate
        }}</el-descriptions-item>
        <el-descriptions-item label="检测项目" :span="2">{{
          detailDialog.row?.qcItems
        }}</el-descriptions-item>
        <el-descriptions-item label="指标值" :span="2">{{
          detailDialog.row?.qcValue || '—'
        }}</el-descriptions-item>
      </el-descriptions>

      <div class="log-section">
        <div class="log-title">修改历史</div>
        <el-timeline v-if="qcLogs.length > 0">
          <el-timeline-item
            v-for="(log, idx) in qcLogs"
            :key="log.id"
            :timestamp="formatTime(log.createTime)"
            placement="top"
            :type="idx === 0 ? 'primary' : 'info'"
          >
            <div class="log-item">
              <span class="log-operator">{{ log.operator }}</span>
              修改了「{{ log.fieldLabel }}」
              <span class="log-change">
                <span class="old-val">{{ log.oldValue || '空' }}</span>
                <el-icon class="arrow-icon"><ArrowRight /></el-icon>
                <span class="new-val">{{ log.newValue }}</span>
              </span>
            </div>
          </el-timeline-item>
        </el-timeline>
        <el-empty v-else description="暂无修改记录" :image-size="60" />
      </div>
      <template #footer>
        <el-button @click="detailDialog.visible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { loadAllRows } from '@/utils/pagination'
import { useResponsiveLayout } from '@/composables/useResponsiveLayout'
const { compact } = useResponsiveLayout()
import { validateForm } from '@/utils/interaction'
import { ref, reactive, computed, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'
import { ArrowRight } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const list = ref([])
const total = ref(0)
const loading = ref(false)
const products = ref([])
const formRef = ref()
const query = reactive({ pageNum: 1, pageSize: 10 })
const dialog = reactive({ visible: false, title: '' })
const form = reactive({})
const detailDialog = reactive({ visible: false, row: null })
const qcLogs = ref([])
const rules = {
  batchId: [{ required: true, message: '请选择产品批次', trigger: 'change' }]
}

function now() {
  const d = new Date()
  const p = (n) => String(n).padStart(2, '0')
  return (
    d.getFullYear() +
    '-' +
    p(d.getMonth() + 1) +
    '-' +
    p(d.getDate()) +
    ' ' +
    p(d.getHours()) +
    ':' +
    p(d.getMinutes()) +
    ':' +
    p(d.getSeconds())
  )
}
function formatTime(t) {
  if (!t) return ''
  return t.replace('T', ' ').substring(0, 19)
}

async function load() {
  loading.value = true
  try {
    const res = await request.get('/qc/list', { params: query })
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
    batchId: null,
    qcItems: '蔗糖含量;羟甲基糠醛;水分;淀粉酶值',
    qcResult: 1,
    qcValue: '',
    inspector: userStore.user?.realName || userStore.user?.username || '',
    qcDate: now()
  })
}
function openAdd() {
  reset()
  dialog.title = '质检录入'
  dialog.visible = true
}
function openEdit(row) {
  Object.assign(form, {
    id: row.id,
    batchId: row.batchId,
    qcItems: row.qcItems,
    qcResult: row.qcResult,
    qcValue: row.qcValue,
    inspector: row.inspector,
    qcDate: row.qcDate
  })
  dialog.title = '修改质检'
  dialog.visible = true
}
async function openDetail(row) {
  detailDialog.row = row
  detailDialog.visible = true
  // 加载修改历史
  const res = await request.get(`/qc/log/${row.id}`)
  qcLogs.value = res.data || []
}
async function submit() {
  if (saving.value) return
  saving.value = true
  try {
    if (!(await validateForm(formRef.value))) return
    if (form.id) {
      await request.put('/qc', form)
      ElMessage.success('修改成功，已记录修改历史')
    } else {
      await request.post('/qc', form)
      ElMessage.success('质检录入成功')
    }
    dialog.visible = false
    load()
  } catch (error) {
    // 请求失败已由拦截器提示，表单内容保留供重试。
  } finally {
    saving.value = false
  }
}
onMounted(async () => {
  products.value = await loadAllRows('/product/list')
  load()
})

const saving = ref(false)
</script>
<style scoped>
.toolbar {
  margin-bottom: 12px;
}
.detail-desc {
  margin-bottom: 20px;
}
.log-section {
  border-top: 1px solid var(--border-light);
  padding-top: 16px;
}
.log-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 12px;
}
.log-item {
  font-size: 13px;
  color: var(--text-regular);
  line-height: 1.8;
}
.log-operator {
  color: var(--primary);
  font-weight: 500;
}
.log-change {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  margin-left: 6px;
}
.old-val {
  color: var(--text-secondary);
  text-decoration: line-through;
}
.new-val {
  color: var(--success);
  font-weight: 500;
}
.arrow-icon {
  color: #c0c4cc;
  font-size: 12px;
}
</style>
