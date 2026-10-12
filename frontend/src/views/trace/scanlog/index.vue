<template>
  <div class="app-container">
    <el-form class="search-form" inline :model="query">
      <el-form-item label="溯源码">
        <el-input
          v-model="query.traceCode"
          placeholder="请输入"
          clearable
          @keyup.enter="onSearch"
        />
      </el-form-item>
      <el-form-item label="设备类型">
        <el-select
          v-model="query.deviceType"
          placeholder="全部"
          clearable
          style="width: 130px"
        >
          <el-option v-for="d in deviceTypes" :key="d" :label="d" :value="d" />
        </el-select>
      </el-form-item>
      <el-form-item label="扫码时间">
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          value-format="YYYY-MM-DD"
          range-separator="至"
          start-placeholder="开始"
          end-placeholder="结束"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="onSearch">查询</el-button>
        <el-button @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-card class="table-card">
      <el-table :data="list" v-loading="loading" border stripe>
        <el-table-column prop="traceCode" label="溯源码" min-width="160" />
        <el-table-column prop="scanTime" label="扫码时间" width="170" />
        <el-table-column prop="ip" label="IP地址" width="130" />
        <el-table-column prop="deviceType" label="设备类型" width="100">
          <template #default="{ row }">
            <el-tag>{{ row.deviceType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column
          prop="userAgent"
          label="User-Agent"
          min-width="260"
          show-overflow-tooltip
        />
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import request from '@/utils/request'

const deviceTypes = ['Android', 'iPhone', 'iPad', '手机', '微信', '浏览器']

const list = ref([])
const total = ref(0)
const loading = ref(false)
const dateRange = ref([])
const query = reactive({
  pageNum: 1,
  pageSize: 10,
  traceCode: '',
  deviceType: ''
})

function buildParams() {
  return {
    ...query,
    beginTime:
      dateRange.value && dateRange.value[0] ? dateRange.value[0] : undefined,
    endTime:
      dateRange.value && dateRange.value[1] ? dateRange.value[1] : undefined
  }
}
async function load() {
  loading.value = true
  try {
    const res = await request.get('/scanlog/list', { params: buildParams() })
    list.value = res.rows
    total.value = res.total
  } catch (error) {
    // 请求拦截器已展示错误，保留当前列表。
  } finally {
    loading.value = false
  }
}
function resetQuery() {
  Object.assign(query, { pageNum: 1, traceCode: '', deviceType: '' })
  dateRange.value = []
  load()
}

onMounted(load)
function onSearch() {
  query.pageNum = 1
  load()
}
</script>
