<template>
  <div class="app-container">
    <el-card class="table-card">
      <el-table :data="list" v-loading="loading" border stripe>
        <el-table-column prop="itemName" label="物品" min-width="140" />
        <el-table-column prop="unit" label="单位" width="70" align="center" />
        <el-table-column label="类型" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="tagType(row.bizType)" size="small">{{
              bizText(row.bizType)
            }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="数量" width="90" align="right">
          <template #default="{ row }">
            <span :style="{ color: row.quantity > 0 ? '#67c23a' : '#f56c6c' }">
              {{ row.quantity > 0 ? '+' : '' }}{{ row.quantity }}
            </span>
          </template>
        </el-table-column>
        <el-table-column
          prop="stockAfter"
          label="操作后库存"
          width="100"
          align="right"
        />
        <el-table-column label="操作人" width="100" align="center">
          <template #default="{ row }">{{
            row.operatorName || row.operator || '-'
          }}</template>
        </el-table-column>
        <el-table-column label="审核人" width="100" align="center">
          <template #default="{ row }">
            <span
              :style="{
                color:
                  row.auditorName && row.auditorName !== '-'
                    ? '#409eff'
                    : '#c0c4cc'
              }"
              >{{ row.auditorName || '-' }}</span
            >
          </template>
        </el-table-column>
        <el-table-column label="时间" width="165" align="center">
          <template #default="{ row }">{{
            formatTime(row.createTime)
          }}</template>
        </el-table-column>
        <el-table-column
          prop="remark"
          label="备注"
          min-width="160"
          show-overflow-tooltip
        />
        <el-table-column label="操作" width="80" align="center" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="viewDetail(row)"
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

    <!-- 流水详情弹窗 -->
    <el-dialog
      align-center
      v-model="detailDialog"
      title="出入库流水详情"
      width="640px"
      :close-on-click-modal="false"
    >
      <div v-if="detail" class="detail-content">
        <!-- 基本信息 -->
        <el-descriptions
          :column="compact ? 1 : 3"
          border
          size="small"
          class="detail-desc"
        >
          <el-descriptions-item label="物品">{{
            detail.itemName || '-'
          }}</el-descriptions-item>
          <el-descriptions-item label="类型">
            <el-tag :type="tagType(detail.bizType)" size="small">{{
              bizText(detail.bizType)
            }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="数量">
            <span
              :style="{
                color: detail.quantity > 0 ? '#67c23a' : '#f56c6c',
                fontWeight: 600
              }"
            >
              {{ detail.quantity > 0 ? '+' : '' }}{{ detail.quantity }}
            </span>
          </el-descriptions-item>
          <el-descriptions-item label="操作后库存">{{
            detail.stockAfter
          }}</el-descriptions-item>
          <el-descriptions-item label="单位">{{
            detail.unit || '-'
          }}</el-descriptions-item>
          <el-descriptions-item label="操作人">{{
            detail.operatorName || detail.operator || '-'
          }}</el-descriptions-item>
          <el-descriptions-item label="业务单号">{{
            detail.bizNo || '-'
          }}</el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">{{
            detail.remark || '-'
          }}</el-descriptions-item>
        </el-descriptions>

        <!-- 审批流程时间线 -->
        <div class="flow-section">
          <div class="flow-title">审批流程</div>
          <el-timeline class="flow-timeline">
            <el-timeline-item
              v-for="(node, idx) in detail.flowNodes"
              :key="idx"
              :type="node.status === 1 ? 'success' : 'info'"
              :hollow="node.status !== 1"
            >
              <div class="flow-node">
                <div class="flow-node-header">
                  <span class="flow-node-name">{{ node.nodeName }}</span>
                  <el-tag
                    v-if="node.status === 1"
                    type="success"
                    size="small"
                    effect="plain"
                    >已完成</el-tag
                  >
                  <el-tag v-else type="info" size="small" effect="plain"
                    >待处理</el-tag
                  >
                </div>
                <div class="flow-node-meta">
                  <span
                    >操作人：<b>{{ node.operatorName }}</b></span
                  >
                  <span>时间：{{ node.operateTime }}</span>
                </div>
                <div class="flow-node-desc">{{ node.description }}</div>
              </div>
            </el-timeline-item>
          </el-timeline>
        </div>
      </div>
      <template #footer>
        <el-button @click="detailDialog = false">关 闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { useResponsiveLayout } from '@/composables/useResponsiveLayout'
const { compact } = useResponsiveLayout()
import { ref, reactive, onMounted } from 'vue'
import request from '@/utils/request'

const list = ref([])
const total = ref(0)
const loading = ref(false)
const query = reactive({ pageNum: 1, pageSize: 10 })
const detailDialog = ref(false)
const detail = ref(null)

const map = {
  1: ['入库', 'success'],
  2: ['出库', 'warning'],
  3: ['盘盈', 'primary'],
  4: ['盘亏', 'danger']
}
const bizText = (t) => (map[t] || ['未知'])[0]
const tagType = (t) => (map[t] || ['', 'info'])[1]

function formatTime(t) {
  if (!t) return '-'
  const d = new Date(t)
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}

async function load() {
  loading.value = true
  try {
    const res = await request.get('/stockrecord/list', { params: query })
    list.value = res.rows
    total.value = res.total
  } catch (error) {
    // 请求拦截器已展示错误，保留当前列表。
  } finally {
    loading.value = false
  }
}

async function viewDetail(row) {
  const res = await request.get(`/stockrecord/${row.id}`)
  detail.value = res.data
  detailDialog.value = true
}

onMounted(load)
</script>
<style scoped>
.detail-desc {
  margin-bottom: 20px;
}
.flow-section {
  border-top: 1px solid var(--border-light);
  padding-top: 16px;
}
.flow-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 12px;
}
.flow-timeline {
  padding-left: 8px;
}
.flow-node {
  padding-bottom: 4px;
}
.flow-node-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}
.flow-node-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
}
.flow-node-meta {
  display: flex;
  gap: 8px 20px;
  flex-wrap: wrap;
  font-size: 12px;
  color: var(--text-secondary);
  margin-bottom: 4px;
}
.flow-node-meta b {
  color: var(--text-regular);
  font-weight: 500;
}
.flow-node-desc {
  font-size: 13px;
  color: var(--text-regular);
}
</style>
