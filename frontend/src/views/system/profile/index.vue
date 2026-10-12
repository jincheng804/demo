<template>
  <div class="app-container profile-page">
    <el-row :gutter="16">
      <!-- 左侧：用户信息卡片 -->
      <el-col :xs="24" :md="8">
        <el-card class="user-card" shadow="never">
          <div class="user-avatar">
            <el-avatar :size="80" :src="user.avatar">
              {{ (user.realName || user.username || '?').charAt(0) }}
            </el-avatar>
          </div>
          <div class="user-name">{{ user.realName || user.username }}</div>
          <div class="user-role">
            <el-tag
              v-for="r in roles"
              :key="r"
              type="primary"
              size="small"
              effect="light"
              style="margin: 0 4px"
            >
              {{ roleLabel(r) }}
            </el-tag>
          </div>
          <el-divider />
          <div class="user-info-list">
            <div class="info-item">
              <span class="info-label">用户名</span>
              <span class="info-value">{{ user.username }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">手机号</span>
              <span class="info-value">{{ user.phone || '未设置' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">邮箱</span>
              <span class="info-value">{{ user.email || '未设置' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">性别</span>
              <span class="info-value">{{ genderLabel(user.gender) }}</span>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 右侧：修改信息 + 修改密码 -->
      <el-col :xs="24" :md="16">
        <el-card class="form-card" shadow="never">
          <template #header><div class="card-title">基本信息</div></template>
          <el-form
            :model="profileForm"
            :rules="profileRules"
            ref="profileFormRef"
            label-width="auto"
          >
            <el-row :gutter="16">
              <el-col :span="12">
                <el-form-item label="真实姓名" prop="realName">
                  <el-input
                    v-model="profileForm.realName"
                    placeholder="请输入真实姓名"
                    maxlength="20"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="性别">
                  <el-radio-group v-model="profileForm.gender">
                    <el-radio :value="1">男</el-radio>
                    <el-radio :value="2">女</el-radio>
                    <el-radio :value="0">保密</el-radio>
                  </el-radio-group>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="手机号" prop="phone">
                  <el-input
                    v-model="profileForm.phone"
                    placeholder="请输入手机号"
                    maxlength="11"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="邮箱" prop="email">
                  <el-input
                    v-model="profileForm.email"
                    placeholder="请输入邮箱"
                    maxlength="50"
                  />
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item>
              <el-button
                type="primary"
                :loading="profileLoading"
                @click="saveProfile"
                >保存修改</el-button
              >
            </el-form-item>
          </el-form>
        </el-card>

        <el-card class="form-card" shadow="never" style="margin-top: 16px">
          <template #header><div class="card-title">修改密码</div></template>
          <el-form
            :model="pwdForm"
            :rules="pwdRules"
            ref="pwdFormRef"
            label-width="auto"
            style="max-width: 500px"
          >
            <el-form-item label="旧密码" prop="oldPassword">
              <el-input
                v-model="pwdForm.oldPassword"
                type="password"
                placeholder="请输入当前密码"
                show-password
                autocomplete="new-password"
              />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input
                v-model="pwdForm.newPassword"
                type="password"
                placeholder="至少6位"
                show-password
                autocomplete="new-password"
              />
            </el-form-item>
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input
                v-model="pwdForm.confirmPassword"
                type="password"
                placeholder="再次输入新密码"
                show-password
                autocomplete="new-password"
              />
            </el-form-item>
            <el-form-item>
              <el-button
                type="primary"
                :loading="pwdLoading"
                @click="changePassword"
                >确认修改</el-button
              >
              <el-button @click="resetPwdForm">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { validateForm } from '@/utils/interaction'
import { ref, reactive, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const user = ref({})
const roles = ref([])

const profileFormRef = ref()
const profileLoading = ref(false)
const profileForm = reactive({ realName: '', phone: '', email: '', gender: 0 })

const pwdFormRef = ref()
const pwdLoading = ref(false)
const pwdForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const validateConfirm = (rule, value, callback) => {
  if (value !== pwdForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const profileRules = {
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  email: [{ type: 'email', message: '邮箱格式不正确', trigger: 'blur' }]
}

const pwdRules = {
  oldPassword: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: validateConfirm, trigger: 'blur' }
  ]
}

const roleMap = {
  admin: '系统管理员',
  manager: '场长',
  farmer: '养殖员',
  producer: '采收员',
  warehouse: '仓管员',
  sales: '销售员'
}
function roleLabel(code) {
  return roleMap[code] || code
}
function genderLabel(g) {
  return g === 1 ? '男' : g === 2 ? '女' : '保密'
}

async function loadProfile() {
  const res = await request.get('/user/profile')
  user.value = res.data
  Object.assign(profileForm, {
    realName: res.data.realName || '',
    phone: res.data.phone || '',
    email: res.data.email || '',
    gender: res.data.gender ?? 0
  })
  // 从 auth/info 拿角色
  try {
    const infoRes = await request.get('/auth/info')
    roles.value = infoRes.data.roles || []
  } catch (e) {}
}

async function saveProfile() {
  if (!(await validateForm(profileFormRef.value))) return
  profileLoading.value = true
  try {
    await request.put('/user/profile', profileForm)
    ElMessage.success('个人信息更新成功')
    // 同步更新 store 里的用户信息
    if (userStore.user) {
      userStore.user.realName = profileForm.realName
    }
    loadProfile()
  } finally {
    profileLoading.value = false
  }
}

async function changePassword() {
  if (!(await validateForm(pwdFormRef.value))) return
  pwdLoading.value = true
  try {
    await request.put('/user/password', pwdForm)
    ElMessage.success('密码修改成功，请重新登录')
    resetPwdForm()
    setTimeout(() => {
      userStore.logout()
      window.location.href = '/login'
    }, 1000)
  } finally {
    pwdLoading.value = false
  }
}

function resetPwdForm() {
  Object.assign(pwdForm, {
    oldPassword: '',
    newPassword: '',
    confirmPassword: ''
  })
  pwdFormRef.value?.clearValidate()
}

onMounted(loadProfile)
</script>
<style scoped>
.profile-page {
  max-width: 1200px;
  margin: 0 auto;
}
.user-card {
  text-align: center;
}
.user-avatar {
  margin-bottom: 16px;
}
.user-avatar :deep(.el-avatar) {
  background: linear-gradient(135deg, var(--primary), #66b1ff);
  color: #fff;
  font-size: 32px;
  font-weight: 600;
  border: 3px solid #ecf5ff;
}
.user-name {
  font-size: 20px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 8px;
}
.user-role {
  margin-bottom: 8px;
}
.user-info-list {
  text-align: left;
}
.info-item {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  font-size: 13px;
  border-bottom: 1px dashed #f0f0f0;
}
.info-item:last-child {
  border-bottom: none;
}
.info-label {
  color: var(--text-secondary);
}
.info-value {
  color: var(--text-primary);
  font-weight: 500;
  max-width: 60%;
  text-align: right;
  word-break: break-all;
}
.card-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
}
</style>
