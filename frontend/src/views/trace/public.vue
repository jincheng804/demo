<template>
  <div class="public-page">
    <div class="trace-box" v-loading="loading">
      <div class="trace-brand">
        <img src="/bee.svg" alt="" /><span>蜂场养殖与蜂蜜溯源</span>
      </div>
      <div class="header" v-if="data">
        <span class="trace-eyebrow">HONEY TRACEABILITY</span>
        <h1>蜂蜜溯源档案</h1>
        <div class="product-title">
          {{ data.product.productName || '蜂蜜产品' }}
        </div>
        <p>溯源码：{{ data.traceCode }}</p>
      </div>

      <el-empty
        v-if="!loading && !data"
        :description="errorText"
        :image-size="110"
        class="trace-empty"
      />
      <el-timeline v-if="data" class="trace-timeline">
        <el-timeline-item type="primary" :timestamp="''" placement="top">
          <el-card shadow="never">
            <template #header><b>产品信息</b></template>
            <el-descriptions :column="1" border size="small">
              <el-descriptions-item label="产品名称">{{
                data.product.productName ?? '—'
              }}</el-descriptions-item>
              <el-descriptions-item label="产品批次">{{
                data.product.productBatchNo ?? '—'
              }}</el-descriptions-item>
              <el-descriptions-item label="规格">{{
                data.product.spec ?? '—'
              }}</el-descriptions-item>
              <el-descriptions-item label="装瓶数量"
                >{{ data.product.bottleCount ?? '—' }} 瓶</el-descriptions-item
              >
              <el-descriptions-item label="生产日期">{{
                data.product.produceDate ?? '—'
              }}</el-descriptions-item>
              <el-descriptions-item label="保质期"
                >{{ data.product.shelfLife ?? '—' }} 个月</el-descriptions-item
              >
            </el-descriptions>
          </el-card>
        </el-timeline-item>

        <el-timeline-item type="success" placement="top">
          <el-card shadow="never">
            <template #header><b>采收信息</b></template>
            <el-descriptions :column="1" border size="small">
              <el-descriptions-item label="采收批次">{{
                data.harvest.batchNo ?? '—'
              }}</el-descriptions-item>
              <el-descriptions-item label="蜂蜜种类">{{
                data.harvest.honeyType ?? '—'
              }}</el-descriptions-item>
              <el-descriptions-item label="采收重量"
                >{{ data.harvest.weight ?? '—' }} kg</el-descriptions-item
              >
              <el-descriptions-item label="波美度">{{
                data.harvest.moisture ?? '—'
              }}</el-descriptions-item>
              <el-descriptions-item label="摇蜜日期">{{
                data.harvest.harvestDate ?? '—'
              }}</el-descriptions-item>
            </el-descriptions>
          </el-card>
        </el-timeline-item>

        <el-timeline-item type="warning" placement="top">
          <el-card shadow="never">
            <template #header><b>蜜源地与蜂箱</b></template>
            <el-descriptions :column="1" border size="small">
              <el-descriptions-item label="蜜源地">{{
                data.source.sourceName ?? '—'
              }}</el-descriptions-item>
              <el-descriptions-item label="蜜源植物">{{
                data.source.plant ?? '—'
              }}</el-descriptions-item>
              <el-descriptions-item label="地点">{{
                data.source.location ?? '—'
              }}</el-descriptions-item>
              <el-descriptions-item label="花期"
                >{{ data.source.bloomStart ?? '—' }} 至
                {{ data.source.bloomEnd ?? '—' }}</el-descriptions-item
              >
              <el-descriptions-item label="采收蜂箱">{{
                data.hive.hiveCode ?? '—'
              }}</el-descriptions-item>
            </el-descriptions>
          </el-card>
        </el-timeline-item>

        <el-timeline-item type="info" placement="top">
          <el-card shadow="never">
            <template #header><b>质检报告</b></template>
            <el-empty
              v-if="!data.qcRecords.length"
              description="暂无质检记录"
              :image-size="60"
            />
            <el-descriptions
              v-for="q in data.qcRecords"
              :key="q.id"
              :column="1"
              border
              size="small"
              style="margin-bottom: 8px"
            >
              <el-descriptions-item label="检测项目">{{
                q.qcItems
              }}</el-descriptions-item>
              <el-descriptions-item label="结果">
                <el-tag :type="q.qcResult === 1 ? 'success' : 'danger'">
                  {{ q.qcResult === 1 ? '合格' : '不合格' }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="指标值">{{
                q.qcValue
              }}</el-descriptions-item>
              <el-descriptions-item label="检测人/日期"
                >{{ q.inspector }} / {{ q.qcDate }}</el-descriptions-item
              >
            </el-descriptions>
          </el-card>
        </el-timeline-item>

        <el-timeline-item type="danger" placement="top">
          <el-card shadow="never">
            <template #header><b>用药记录</b></template>
            <el-empty
              v-if="!data.medications.length"
              description="该批次暂无用药记录"
              :image-size="60"
            />
            <el-descriptions
              v-for="m in data.medications"
              :key="m.id"
              :column="1"
              border
              size="small"
              style="margin-bottom: 8px"
            >
              <el-descriptions-item label="药品">{{
                m.drugName
              }}</el-descriptions-item>
              <el-descriptions-item label="用药日期">{{
                m.useDate
              }}</el-descriptions-item>
              <el-descriptions-item label="安全采收日期">{{
                m.safeDate
              }}</el-descriptions-item>
            </el-descriptions>
          </el-card>
        </el-timeline-item>
      </el-timeline>

      <p class="footer-tip" v-if="data">
        本页面信息由蜂场养殖与蜂蜜溯源管理系统生成，查看次数
        {{ data.scanCount }}
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, onBeforeUnmount } from 'vue'
import { useRoute } from 'vue-router'
import request from '@/utils/request'

const route = useRoute()
const data = ref(null)
const loading = ref(true)

const errorText = ref('暂时无法加载溯源信息，请稍后重试')
let requestVersion = 0
let alive = true
watch(
  () => route.params.code,
  async (code) => {
    const version = ++requestVersion
    loading.value = true
    data.value = null
    try {
      const res = await request.get(`/trace/public/${encodeURIComponent(code)}`)
      if (!alive || version !== requestVersion) return
      if (!res.data) {
        errorText.value = '未找到该溯源码的记录'
        return
      }
      data.value = {
        ...res.data,
        product: res.data.product || {},
        harvest: res.data.harvest || {},
        source: res.data.source || {},
        hive: res.data.hive || {},
        qcRecords: res.data.qcRecords || [],
        medications: res.data.medications || []
      }
    } catch (error) {
      if (alive && version === requestVersion)
        errorText.value = error.message || '暂时无法加载溯源信息，请稍后重试'
    } finally {
      if (alive && version === requestVersion) loading.value = false
    }
  },
  { immediate: true }
)
onBeforeUnmount(() => {
  alive = false
  requestVersion++
})
</script>

<style scoped>
.public-page {
  min-height: 100vh;
  background: #f5f6f0;
  padding: 28px 20px 40px;
}
.trace-box {
  max-width: 800px;
  margin: 0 auto;
  min-height: 300px;
}
.trace-brand {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #667b5e;
  font-size: 12px;
  margin-bottom: 24px;
}
.trace-brand img {
  width: 26px;
  height: 26px;
}
.header {
  position: relative;
  overflow: hidden;
  border-radius: 20px;
  background: #193c30;
  padding: 36px;
  margin-bottom: 32px;
  color: #fff;
}
.header::after {
  content: '';
  width: 200px;
  height: 200px;
  border-radius: 50%;
  border: 1px solid #eac77730;
  position: absolute;
  right: -70px;
  top: -80px;
  box-shadow:
    0 0 0 32px #eac77708,
    0 0 0 64px #eac77705;
  pointer-events: none;
}
.trace-eyebrow {
  color: #e4bb70;
  font-size: 10px;
  letter-spacing: 2px;
}
.header h1 {
  font-size: 28px;
  font-weight: 650;
  margin: 12px 0 18px;
}
.product-title {
  font-size: 16px;
  color: #d4dfcf;
}
.header p {
  color: #aac0ac;
  font-size: 12px;
  margin: 12px 0 0;
  overflow-wrap: anywhere;
}
.trace-timeline {
  padding-left: 10px;
}
.trace-timeline :deep(.el-card__header) {
  font-size: 15px;
}
.trace-timeline :deep(.el-timeline-item) {
  padding-bottom: 24px;
}
.trace-timeline :deep(.el-descriptions__label) {
  width: 124px;
}
.trace-timeline :deep(.el-descriptions__cell) {
  padding: 10px 14px;
  line-height: 1.7;
}
.trace-empty {
  background: #fff;
  border: 1px solid var(--border-light);
  border-radius: 16px;
  padding: 60px 24px;
}
.footer-tip {
  text-align: center;
  color: #758178;
  font-size: 11px;
  line-height: 1.8;
  margin: 20px 0 0;
}
@media (max-width: 600px) {
  .public-page {
    padding: 20px 12px 32px;
  }
  .header {
    padding: 28px 24px;
    border-radius: 16px;
  }
  .header h1 {
    font-size: 25px;
  }
  .trace-timeline {
    padding-left: 0;
  }
  .trace-timeline :deep(.el-timeline-item__wrapper) {
    padding-left: 22px;
  }
  .trace-timeline :deep(.el-card__body) {
    padding: 12px;
  }
  .trace-timeline :deep(.el-descriptions__label) {
    width: 98px;
  }
  .trace-timeline :deep(.el-descriptions__cell) {
    padding: 9px;
  }
}
</style>
