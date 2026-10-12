<template>
  <div class="login-page">
    <!-- 左侧品牌展示区 -->
    <div class="brand-side">
      <div class="honeycomb-bg"></div>
      <div class="brand-content">
        <div class="brand-logo">
          <img src="/bee.svg" alt="蜂场溯源系统" class="brand-logo-img" />
        </div>
        <h1 class="brand-title">用心养蜂，<br />让好蜜有源可溯。</h1>
        <p class="brand-subtitle">
          蜂场养殖与蜂蜜溯源管理系统<br /><span
            >从日常养殖到消费者扫码，记录每一步。</span
          >
        </p>
        <img src="/farm-landscape.svg" class="apiary-art" alt="" aria-hidden="true" />
        <div class="brand-divider"></div>
        <ul class="brand-features">
          <li>
            <el-icon><CircleCheck /></el-icon>
            <span>蜂群养殖全流程数字化管理</span>
          </li>
          <li>
            <el-icon><CircleCheck /></el-icon>
            <span>摇蜜采收 · 批次质检 · 装瓶入库</span>
          </li>
          <li>
            <el-icon><CircleCheck /></el-icon>
            <span>一瓶一码 · 扫码溯源 · 质量可信</span>
          </li>
          <li>
            <el-icon><CircleCheck /></el-icon>
            <span>多角色权限 · 库存采购 · 统计报表</span>
          </li>
        </ul>
      </div>
      <div class="brand-footer">© 2026 蜂场养殖与蜂蜜溯源管理系统</div>
    </div>

    <!-- 右侧登录表单区 -->
    <div class="form-side">
      <div class="form-wrapper">
        <div class="form-header">
          <div class="form-logo">
            <img src="/bee.svg" alt="蜂场溯源系统" class="form-logo-img" />
          </div>
          <span class="form-eyebrow">BEE FARM WORKSPACE</span>
          <h2>欢迎回来</h2>
          <p>请使用您的账号登录系统</p>
        </div>

        <el-form
          ref="loginFormRef"
          :model="loginForm"
          :rules="rules"
          size="large"
          label-position="top"
          class="login-form"
          @submit.prevent="handleLogin"
        >
          <el-form-item label="用户名" prop="username">
            <el-input
              v-model="loginForm.username"
              placeholder="请输入用户名"
              :prefix-icon="User"
              autocomplete="username"
              clearable
            />
          </el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="请输入密码"
              :prefix-icon="Lock"
              show-password
              autocomplete="current-password"
            />
          </el-form-item>
          <el-form-item class="remember-item">
            <div class="remember-row">
              <el-checkbox v-model="rememberMe">记住用户名</el-checkbox>
            </div>
          </el-form-item>
          <el-form-item>
            <el-button
              type="primary"
              class="login-btn"
              :loading="loading"
              native-type="submit"
            >
              登录工作台
            </el-button>
          </el-form-item>
        </el-form>

        <div class="form-footer">
          <p>账号由管理员分配。如需帮助，请联系蜂场管理员。</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { User, Lock, CircleCheck } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'
import { validateForm } from '@/utils/interaction'

const router = useRouter(), route = useRoute()
const userStore = useUserStore()
const loginFormRef = ref()
const loading = ref(false)
const rememberMe = ref(false)

// 不预填任何账号密码，保证安全
const loginForm = reactive({ username: '', password: '' })

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

onMounted(() => {
  // 从 localStorage 恢复记住的用户名（仅用户名，不存密码）
  const savedUser = localStorage.getItem('bee_remember_user')
  if (savedUser) {
    loginForm.username = savedUser
    rememberMe.value = true
  }
})

async function handleLogin() {
  if (loading.value) return
  loading.value = true
  try {
    if (!(await validateForm(loginFormRef.value))) return
    await userStore.login(loginForm)
    // 记住我：仅保存用户名
    if (rememberMe.value) {
      localStorage.setItem('bee_remember_user', loginForm.username)
    } else {
      localStorage.removeItem('bee_remember_user')
    }
    ElMessage.success('登录成功')
    const target = typeof route.query.redirect === 'string' && route.query.redirect.startsWith('/') && !route.query.redirect.startsWith('//') && !route.query.redirect.startsWith('/login') ? route.query.redirect : '/'
    router.replace(target)
  } catch (e) {
    // 错误已在拦截器提示
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.apiary-art { display:block; width:100%; max-width:430px; margin:18px 0 8px; border-radius:10px; }

.login-page {
  min-height: 100vh;
  min-height: 100dvh;
  display: grid;
  grid-template-columns: 1.05fr 1fr;
  background: #f6f7f1;
}
.brand-side {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  background: #193c30;
  padding: 80px 48px;
}
.honeycomb-bg {
  position: absolute;
  inset: 0;
  background: radial-gradient(ellipse at 90% 10%, #72935628, transparent 60%),
    radial-gradient(ellipse at 10% 90%, #e8b95716, transparent 50%);
}
.honeycomb-bg::after {
  content: '';
  position: absolute;
  width: 640px;
  height: 640px;
  right: -330px;
  top: -260px;
  border-radius: 50%;
  border: 1px solid #f1c77a18;
  box-shadow:
    0 0 0 90px #f1c77a05,
    0 0 0 180px #f1c77a04;
}
.honeycomb-bg::before {
  content: '';
  position: absolute;
  inset: 0;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='90' height='156' viewBox='0 0 90 156'%3E%3Cpath d='M45 0L90 26v52l-45 26L0 78V26zM45 104v52M0 78v52l45 26 45-26V78' fill='none' stroke='%23ffffff' stroke-opacity='.045'/%3E%3C/svg%3E");
  mask-image: linear-gradient(to bottom, transparent, #000);
}
.brand-content {
  z-index: 1;
  width: 100%;
  max-width: 460px;
  color: #fff;
}
.brand-logo {
  width: 72px;
  height: 72px;
  background: #f4cd8618;
  border: 1px solid #f4cd8628;
  border-radius: 20px;
  display: grid;
  place-items: center;
  margin-bottom: 42px;
}
.brand-logo-img {
  width: 50px;
  height: 50px;
}
.brand-title {
  font-size: clamp(30px, 3.1vw, 46px);
  line-height: 1.45;
  letter-spacing: -1px;
  font-weight: 650;
  margin: 0 0 24px;
}
.brand-subtitle {
  color: #d3dfce;
  font-size: 15px;
  line-height: 2;
  margin: 0;
}
.brand-subtitle span {
  color: #a4bca9;
  font-size: 13px;
}
.brand-divider {
  width: 42px;
  height: 3px;
  border-radius: 3px;
  background: #e7bb64;
  margin: 34px 0 26px;
}
.brand-features {
  list-style: none;
  padding: 0;
  margin: 0;
}
.brand-features li {
  display: flex;
  align-items: center;
  gap: 12px;
  color: #c7d7c6;
  font-size: 13px;
  padding: 10px 0;
}
.brand-features .el-icon {
  color: #e7bb64;
  font-size: 17px;
}
.brand-footer {
  position: absolute;
  bottom: 28px;
  color: #95ae9b;
  font-size: 11px;
  text-align: center;
  left: 24px;
  right: 24px;
}
.form-side {
  padding: 48px 32px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.form-wrapper {
  width: 100%;
  max-width: 432px;
  padding: 40px;
  border-radius: 20px;
  background: #fff;
  border: 1px solid #e7ebdf;
  box-shadow: 0 20px 60px #23382d06;
}
.form-header {
  margin-bottom: 32px;
}
.form-logo {
  display: none;
}
.form-eyebrow {
  font-size: 10px;
  color: #a96916;
  letter-spacing: 2px;
}
.form-header h2 {
  margin: 12px 0 10px;
  font-size: 28px;
  font-weight: 650;
  letter-spacing: -0.5px;
}
.form-header p {
  margin: 0;
  color: #758178;
  font-size: 13px;
}
.login-form :deep(.el-form-item__label) {
  font-size: 13px;
  padding-bottom: 8px;
}
.login-form :deep(.el-input__wrapper) {
  padding: 3px 14px;
  border-radius: 9px;
}
.login-form :deep(.el-input__inner) {
  height: 40px;
  font-size: 14px;
}
.login-form :deep(.el-input__prefix) {
  color: #8d9b87;
}
.remember-item {
  margin-bottom: 16px;
}
.remember-row {
  display: flex;
  width: 100%;
}
.remember-row :deep(.el-checkbox__label) {
  font-size: 13px;
  color: #758178;
}
.login-btn {
  width: 100%;
  height: 46px;
  font-size: 14px;
  font-weight: 600;
  border-radius: 9px;
}
.form-footer {
  border-top: 1px solid #edf0e7;
  padding-top: 20px;
  margin-top: 26px;
}
.form-footer p {
  margin: 0;
  font-size: 11px;
  color: #758178;
  line-height: 1.8;
}
@media (max-width: 960px) {
  .login-page {
    grid-template-columns: 1fr;
  }
  .brand-side {
    display: none;
  }
  .form-side {
    padding: 32px 20px;
    background: radial-gradient(ellipse at 50% 0, #eee2c5, transparent 70%);
  }
  .form-logo {
    display: grid;
    place-items: center;
    width: 52px;
    height: 52px;
    margin-bottom: 22px;
    border-radius: 14px;
    background: #faf3e7;
  }
  .form-logo-img {
    width: 36px;
    height: 36px;
  }
}
@media (max-width: 480px) {
  .form-wrapper {
    padding: 30px 24px;
  }
}
</style>
