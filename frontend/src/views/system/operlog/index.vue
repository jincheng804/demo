<template>
  <div class="app-container">
    <el-card class="table-card">
      <el-form inline :model="query">
        <el-form-item label="模块">
          <el-input v-model="query.title" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="onSearch">查询</el-button>
        </el-form-item>
      </el-form>
      <el-table :data="list" v-loading="loading" border>
        <el-table-column prop="title" label="模块" width="130" />
        <el-table-column prop="method" label="方式" width="80" />
        <el-table-column prop="operName" label="操作人" width="110" />
        <el-table-column prop="operUrl" label="URL" min-width="180" />
        <el-table-column prop="ip" label="IP" width="130" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '正常' : '异常' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="operTime" label="时间" min-width="160" />
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import request from '@/utils/request'

const list = ref([])
const total = ref(0)
const loading = ref(false)
const query = reactive({ pageNum: 1, pageSize: 10, title: '' })

async function load() {
  loading.value = true
  try {
    const res = await request.get('/log/oper/list', { params: query })
    list.value = res.rows
    total.value = res.total
  } catch (error) {
    // 请求拦截器已展示错误，保留当前列表。
  } finally {
    loading.value = false
  }
}
onMounted(load)
function onSearch() {
  query.pageNum = 1
  load()
}
</script>
