<template>
  <div class="app-container">
    <el-card class="table-card">
      <div class="toolbar">
        <el-button
          type="primary"
          v-hasPermi="'medication:add'"
          @click="openAdd"
        >
          <el-icon><Plus /></el-icon>用药登记
        </el-button>
      </div>
      <el-table :data="list" v-loading="loading" border stripe>
        <el-table-column prop="hiveCode" label="蜂箱" width="100" />
        <el-table-column prop="drugName" label="药品名称" min-width="130" />
        <el-table-column prop="dose" label="剂量" width="110" />
        <el-table-column prop="useDate" label="用药日期" width="110" />
        <el-table-column prop="withdrawalDays" width="100">
          <template #header><GlossaryTip term="休药期" /></template>
        </el-table-column>
        <el-table-column prop="safeDate" width="130">
          <template #header><GlossaryTip term="安全采收日期" /></template>
          <template #default="{ row }">
            <el-tag type="warning">{{ row.safeDate }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="operator" label="操作人" width="90" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button
              link
              type="danger"
              v-hasPermi="'medication:remove'"
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
      title="用药登记"
      width="560px"
      @closed="formRef?.clearValidate()"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="auto">
        <el-form-item label="蜂箱" prop="hiveId">
          <el-select
            v-model="form.hiveId"
            style="width: 100%"
            filterable
            @change="onHiveChange"
          >
            <el-option
              v-for="h in sortedHives"
              :key="h.id"
              :label="
                h.hiveCode + (h.diseaseCount > 0 ? '' : '（无未处理病害）')
              "
              :value="h.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="对应病害" v-if="hiveDiseases.length > 0">
          <div class="disease-tags">
            <el-tag
              v-for="d in hiveDiseases"
              :key="d.id"
              :type="selectedDiseaseId === d.id ? 'primary' : 'info'"
              class="disease-tag"
              @click="selectDisease(d)"
            >
              {{ d.diseaseType }}
            </el-tag>
          </div>
          <div class="form-tip">
            点击选择该蜂箱的未处理病害，自动填充药品信息
          </div>
        </el-form-item>
        <el-form-item label="对应病害" v-else>
          <span style="color: #909399; font-size: 13px"
            >该蜂箱暂无未处理病害，可手动填写药品信息</span
          >
        </el-form-item>

        <el-form-item label="药品名称" prop="drugName">
          <el-input
            v-model="form.drugName"
            placeholder="选择病害后自动填充，可修改"
          />
        </el-form-item>
        <el-form-item label="剂量">
          <el-input v-model="form.dose" placeholder="如：20ml/群" />
          <div v-if="recommendedDose" class="form-tip">
            推荐剂量：{{ recommendedDose }}
          </div>
        </el-form-item>
        <el-form-item label="用药日期" prop="useDate">
          <el-date-picker
            v-model="form.useDate"
            type="date"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="休药期">
          <template #label><GlossaryTip term="休药期" /></template>
          <el-input-number v-model="form.withdrawalDays" :min="0" :max="90" />
          <div class="form-tip">
            推荐休药期：{{ recommendedWithdrawal }}天（<GlossaryTip
              term="安全采收日期"
            />自动计算）
          </div>
        </el-form-item>
        <el-form-item label="操作人">
          <el-input
            v-model="form.operator"
            placeholder="默认当前登录人，可修改"
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
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
function today() {
  const d = new Date()
  return (
    d.getFullYear() +
    '-' +
    String(d.getMonth() + 1).padStart(2, '0') +
    '-' +
    String(d.getDate()).padStart(2, '0')
  )
}
function currentName() {
  return userStore.user?.realName || userStore.user?.username || ''
}

// 常见蜂病-药品-剂量-休药期映射
const diseaseDrugMap = {
  蜂螨: { drug: '甲酸', dose: '20-30ml/群', withdrawal: 7 },
  瓦螨: { drug: '甲酸', dose: '20-30ml/群', withdrawal: 7 },
  白垩病: { drug: '食用碱', dose: '0.5g/群', withdrawal: 5 },
  囊状幼虫病: { drug: '抗病毒862', dose: '5ml/群', withdrawal: 7 },
  美洲幼虫腐臭病: { drug: '土霉素', dose: '0.5g/群', withdrawal: 14 },
  欧洲幼虫腐臭病: { drug: '红霉素', dose: '0.25g/群', withdrawal: 10 },
  孢子虫病: { drug: '柠檬酸', dose: '1g/群', withdrawal: 5 },
  爬蜂病: { drug: '复合维生素', dose: '2g/群', withdrawal: 3 },
  麻痹病: { drug: '盐酸金刚烷胺', dose: '1g/群', withdrawal: 7 },
  黄曲霉病: { drug: '制霉菌素', dose: '0.2g/群', withdrawal: 10 }
}

const list = ref([])
const total = ref(0)
const loading = ref(false)
const hives = ref([])
const formRef = ref()
const hiveDiseases = ref([])
const selectedDiseaseId = ref(null)
const hiveDiseaseCount = ref({}) // 蜂箱ID -> 未处理病害数量
const query = reactive({ pageNum: 1, pageSize: 10 })

// 有病害的蜂箱排前面，无病害的排后面
const sortedHives = computed(() => {
  return [...hives.value].sort((a, b) => {
    const ca = hiveDiseaseCount.value[a.id] || 0
    const cb = hiveDiseaseCount.value[b.id] || 0
    if (ca > 0 && cb === 0) return -1
    if (ca === 0 && cb > 0) return 1
    return 0
  })
})
const dialog = reactive({ visible: false })
const form = reactive({})
const rules = {
  hiveId: [{ required: true, message: '请选择蜂箱', trigger: 'change' }],
  drugName: [{ required: true, message: '请输入药品名称', trigger: 'blur' }],
  useDate: [{ required: true, message: '请选择用药日期', trigger: 'change' }]
}

const recommendedDose = computed(() => {
  if (!form.drugName) return ''
  for (const key in diseaseDrugMap) {
    if (diseaseDrugMap[key].drug === form.drugName)
      return diseaseDrugMap[key].dose
  }
  return ''
})
const recommendedWithdrawal = computed(() => {
  if (!form.drugName) return 7
  for (const key in diseaseDrugMap) {
    if (diseaseDrugMap[key].drug === form.drugName)
      return diseaseDrugMap[key].withdrawal
  }
  return 7
})

async function load() {
  loading.value = true
  try {
    const res = await request.get('/medication/list', { params: query })
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
    hiveId: null,
    drugName: '',
    dose: '',
    useDate: today(),
    withdrawalDays: 7,
    operator: currentName()
  })
  hiveDiseases.value = []
  selectedDiseaseId.value = null
}
function openAdd() {
  reset()
  dialog.visible = true
}

async function onHiveChange() {
  selectedDiseaseId.value = null
  form.drugName = ''
  form.dose = ''
  if (!form.hiveId) {
    hiveDiseases.value = []
    return
  }
  // 获取该蜂箱的未处理病害
  const res = await request.get('/disease/list', {
    params: { pageNum: 1, pageSize: 50, hiveId: form.hiveId, handleStatus: 0 }
  })
  hiveDiseases.value = res.rows || []
}

function selectDisease(d) {
  selectedDiseaseId.value = d.id
  const info = diseaseDrugMap[d.diseaseType]
  if (info) {
    form.drugName = info.drug
    form.dose = info.dose
    form.withdrawalDays = info.withdrawal
  } else {
    form.drugName = ''
    form.dose = ''
    form.withdrawalDays = 7
  }
}

async function submit() {
  if (saving.value) return
  saving.value = true
  try {
    if (!(await validateForm(formRef.value))) return
    await request.post('/medication', form)
    ElMessage.success('登记成功')
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
    !(await confirmAction('确定删除该用药记录？', '提示', { type: 'warning' }))
  )
    return
  await request.delete(`/medication/${row.id}`)
  ElMessage.success('删除成功')
  load()
}
onMounted(async () => {
  hives.value = (await request.get('/hive/all')).data
  // 并行查询每个蜂箱的未处理病害数量
  const counts = {}
  await Promise.all(
    hives.value.map(async (h) => {
      try {
        const res = await request.get('/disease/list', {
          params: { pageNum: 1, pageSize: 1, hiveId: h.id, handleStatus: 0 }
        })
        counts[h.id] = res.total || 0
      } catch (e) {
        counts[h.id] = 0
      }
    })
  )
  hiveDiseaseCount.value = counts
  load()
})

const saving = ref(false)
</script>
<style scoped>
.toolbar {
  margin-bottom: 12px;
}
.disease-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.disease-tag {
  cursor: pointer;
  transition: all 0.2s;
}
.disease-tag:hover {
  transform: translateY(-1px);
}
.form-tip {
  font-size: 12px;
  color: var(--text-secondary);
  margin-top: 4px;
  line-height: 1.5;
}
</style>
