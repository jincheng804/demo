<template>
  <el-container class="app-wrapper">
    <button
      v-if="isMobile && mobileMenuOpen"
      class="sidebar-mask"
      aria-label="关闭导航菜单"
      @click="mobileMenuOpen = false"
    />
    <el-aside
      :width="menuCollapsed ? '76px' : '240px'"
      class="sidebar"
      :class="{ 'mobile-open': mobileMenuOpen }"
      id="app-sidebar"
      :inert="isMobile && !mobileMenuOpen"
    >
      <router-link to="/dashboard" class="logo" aria-label="蜂场溯源系统首页">
        <img src="/bee.svg" class="logo-icon" alt="" />
        <div v-show="!menuCollapsed" class="logo-copy">
          <span>蜂场溯源系统</span><small>BEE FARM · TRACEABILITY</small>
        </div>
      </router-link>
      <div class="nav-caption" v-show="!menuCollapsed">工作空间</div>
      <el-scrollbar class="nav-scroll">
        <el-menu
          :default-active="activeMenu"
          :collapse="menuCollapsed"
          :collapse-transition="false"
          background-color="#193c30"
          text-color="#c3d2c5"
          active-text-color="#f2c977"
          router
          unique-opened
        >
          <sidebar-item
            v-for="item in menuRoutes"
            :key="item.path"
            :item="item"
          />
        </el-menu>
      </el-scrollbar>
      <div class="sidebar-footer" v-show="!menuCollapsed">
        <span class="brand-dot" />从蜂场到餐桌 · 全程可追溯
      </div>
    </el-aside>
    <el-container class="main-wrapper">
      <el-header class="navbar">
        <div class="navbar-left">
          <button
            class="collapse-btn"
            :aria-label="menuExpanded ? '收起导航菜单' : '展开导航菜单'"
            :aria-expanded="menuExpanded"
            aria-controls="app-sidebar"
            @click="toggleMenu"
          >
            <el-icon :size="20"
              ><Fold v-if="menuExpanded" /><Expand v-else
            /></el-icon>
          </button>
          <el-breadcrumb separator="/" class="breadcrumb">
            <el-breadcrumb-item :to="{ path: '/dashboard' }"
              >工作台</el-breadcrumb-item
            >
            <el-breadcrumb-item v-for="(title, i) in breadcrumbs" :key="i">{{
              title
            }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="navbar-right">
          <WorkspaceSearch />
          <span class="workspace-label">{{ today }}</span>
          <el-dropdown @command="handleCommand">
            <button class="user-info">
              <el-avatar :size="34" :src="userStore.user.avatar">{{
                userName.charAt(0)
              }}</el-avatar>
              <span class="username">{{ userName }}</span
              ><el-icon><ArrowDown /></el-icon>
            </button>
            <template #dropdown
              ><el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="logout" divided
                  >退出登录</el-dropdown-item
                >
              </el-dropdown-menu></template
            >
          </el-dropdown>
        </div>
      </el-header>
      <el-main class="app-main">
        <div class="page-heading">
          <div>
            <span class="page-eyebrow">{{
              route.path === '/dashboard'
                ? '蜂场经营概览'
                : '蜂场养殖与蜂蜜溯源'
            }}</span>
            <h1>{{ pageTitle }}</h1>
            <p>{{ pageDescription }}</p>
          </div>
          <span class="heading-badge"
            ><img src="/bee.svg" alt="" />让每一滴蜂蜜有迹可循</span
          >
        </div>
        <router-view v-slot="{ Component }"
          ><transition name="fade" mode="out-in"
            ><component :is="Component" /></transition
        ></router-view>
      </el-main>
    </el-container>
  </el-container>
</template>
<script setup>
import { computed, ref, watch, onMounted, onBeforeUnmount } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { confirmAction } from '@/utils/interaction'
import SidebarItem from './SidebarItem.vue'
import WorkspaceSearch from '@/components/WorkspaceSearch.vue'
const route = useRoute(),
  router = useRouter(),
  userStore = useUserStore()
const collapsed = ref(localStorage.getItem('bee_sidebar_collapsed') === 'true'),
  isMobile = ref(false),
  mobileMenuOpen = ref(false)
watch(collapsed, value => localStorage.setItem('bee_sidebar_collapsed', String(value)))
const menuCollapsed = computed(() => !isMobile.value && collapsed.value)
const menuExpanded = computed(() =>
  isMobile.value ? mobileMenuOpen.value : !collapsed.value
)
const today = new Intl.DateTimeFormat('zh-CN', { month: 'long', day: 'numeric', weekday: 'long' }).format(new Date())
const activeMenu = computed(() => route.path)
const menuRoutes = computed(() => userStore.sidebarRoutes)
const userName = computed(
  () => userStore.user.realName || userStore.user.username || '用户'
)
const breadcrumbs = computed(() =>
  route.matched
    .filter((m) => m.meta?.title && m.path !== '/')
    .map((m) => m.meta.title)
    .filter((t, i, a) => i === 0 || t !== a[i - 1])
)
const pageTitle = computed(
  () => route.meta.title || breadcrumbs.value.at(-1) || '工作台'
)
const descriptions = {
  dashboard: '蜂群、采收、出库与溯源数据，一目了然。',
  apiary: '管理蜂场分区与负责人，让养殖工作有序开展。',
  hive: '维护蜂箱档案，掌握每一箱蜂群的基础信息。',
  source: '记录蜜源植物与花期，追溯蜂蜜的自然源头。',
  queen: '维护蜂王档案与状态，持续关注蜂群活力。',
  colony: '记录蜂群变化，为日常养殖管理提供依据。',
  inspection: '记录日常巡检情况，及时了解蜂群状态。',
  feeding: '管理饲喂记录，清晰记录每一次养护。',
  breeding: '管理繁育记录，持续完善蜂群养殖档案。',
  disease: '记录病害发现与处理情况，跟踪防治过程。',
  medication: '管理用药与休药期记录，支持安全采收。',
  harvest: '记录摇蜜采收数据，衔接后续生产与溯源。',
  product: '管理装瓶生产与产品批次，串联采收、入库和溯源。',
  qc: '记录检测结果，跟踪批次质量与审核状态。',
  inventory: '掌握库存数量与预警信息，管理日常出入库。',
  stockrecord: '查询每一笔库存变化，核对物料流转记录。',
  warehouse: '维护仓库档案与负责人，规范物料存储管理。',
  purchase: '管理采购明细与审核，记录物料入库过程。',
  supplier: '集中维护供应商资料，方便采购协作。',
  customer: '维护客户资料与联系方式，支持销售业务。',
  outbound: '管理销售出库明细与审核，跟踪产品流转。',
  expense: '记录蜂场成本费用，让经营支出清晰可查。',
  warning: '集中查看和处理业务预警，及时跟进待办事项。',
  report: '从产量、销售、库存与成本维度查看经营数据。',
  tracecode: '管理产品溯源码与扫码记录，连接蜂场与消费者。',
  trace: '管理产品溯源码与扫码记录，连接蜂场与消费者。',
  scanlog: '查看扫码访问记录，了解产品溯源信息的关注情况。',
  profile: '管理个人资料与登录密码。',
  user: '维护系统用户资料，管理角色与账号状态。',
  role: '配置用户角色与菜单权限。',
  menu: '维护系统菜单结构与权限标识。',
  dept: '管理部门结构，明确组织与人员归属。',
  dict: '维护业务字典类型与选项。',
  operlog: '查询操作记录，方便核对系统使用情况。',
  loginlog: '查询账号登录记录与登录状态。'
}
const pageDescription = computed(() => {
  const segments = route.path.split('/').filter(Boolean)
  const key = segments.at(-1) === 'index' ? segments.at(-2) : segments.at(-1)
  return descriptions[key] || '集中管理业务信息，保持数据清晰、操作有序。'
})
function toggleMenu() {
  if (isMobile.value) mobileMenuOpen.value = !mobileMenuOpen.value
  else collapsed.value = !collapsed.value
}
watch(
  () => route.fullPath,
  () => {
    mobileMenuOpen.value = false
  }
)
let media
function updateViewport() {
  isMobile.value = media.matches
  if (!media.matches) mobileMenuOpen.value = false
}
function onKeydown(event) {
  if (event.key === 'Escape') mobileMenuOpen.value = false
}
onMounted(() => {
  media = window.matchMedia('(max-width: 767px)')
  updateViewport()
  media.addEventListener('change', updateViewport)
  window.addEventListener('keydown', onKeydown)
})
onBeforeUnmount(() => {
  media?.removeEventListener('change', updateViewport)
  window.removeEventListener('keydown', onKeydown)
})
async function handleCommand(command) {
  if (command === 'logout') {
    if (
      !(await confirmAction('确定要退出登录吗？', '退出登录', {
        type: 'warning',
        confirmButtonText: '退出登录',
        cancelButtonText: '取消'
      }))
    )
      return
    try { await userStore.signOut() } finally { router.replace('/login') }
  } else if (command === 'profile') router.push('/profile')
}
</script>
<style scoped>
.app-wrapper {
  height: 100vh;
  height: 100dvh;
  overflow: hidden;
}
.sidebar {
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
  background: #193c30;
  transition: width 0.25s ease;
  overflow: hidden;
}
.logo {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 22px;
  height: 84px;
  min-height: 84px;
  color: #fff;
  text-decoration: none;
}
.logo-icon {
  width: 34px;
  height: 34px;
  flex-shrink: 0;
}
.logo-copy {
  display: flex;
  flex-direction: column;
  gap: 5px;
  white-space: nowrap;
}
.logo-copy span {
  font-size: 17px;
  font-weight: 650;
  letter-spacing: 0.5px;
}
.logo-copy small {
  color: #9db9a5;
  font-size: 8px;
  letter-spacing: 1px;
}
.nav-caption {
  color: #8da797;
  padding: 14px 26px 12px;
  font-size: 11px;
  letter-spacing: 2px;
}
.nav-scroll {
  flex: 1;
  min-height: 0;
}
.sidebar-footer {
  display: flex;
  align-items: center;
  gap: 8px;
  border-top: 1px solid #ffffff12;
  color: #99b09f;
  padding: 22px;
  font-size: 11px;
  white-space: nowrap;
}
.brand-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #e5b453;
}
.main-wrapper {
  min-width: 0;
}
.navbar {
  height: 64px;
  padding: 0 28px;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  border-bottom: 1px solid var(--border-light);
}
.navbar-left,
.navbar-right {
  display: flex;
  align-items: center;
  gap: 18px;
  min-width: 0;
}
.collapse-btn {
  width: 36px;
  height: 36px;
  display: grid;
  place-items: center;
  padding: 0;
  border: 1px solid var(--border-light);
  border-radius: 9px;
  color: var(--text-regular);
  background: #fff;
  cursor: pointer;
  flex-shrink: 0;
}
.collapse-btn:hover {
  background: #faf3e7;
  color: var(--primary);
}
.workspace-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: #758178;
}
.workspace-label span {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #287451;
}
.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  padding: 6px 8px;
  background: transparent;
  border: 0;
  border-radius: 9px;
  color: var(--text-regular);
}
.user-info:hover {
  background: #f5f7f1;
}
.user-info .el-avatar {
  background: #eff3e9;
  color: #486b50;
  font-size: 14px;
  font-weight: 600;
}
.username {
  font-size: 13px;
  max-width: 140px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.app-main {
  padding: 26px 32px;
  min-width: 0;
  overflow-x: hidden;
  background: var(--bg-page);
}
.page-heading {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  margin-bottom: 22px;
}
.page-eyebrow {
  color: var(--primary);
  font-size: 11px;
  letter-spacing: 2px;
}
.page-heading h1 {
  font-size: 25px;
  font-weight: 650;
  margin: 8px 0;
  letter-spacing: -0.5px;
}
.page-heading p {
  font-size: 13px;
  line-height: 1.7;
  color: var(--text-secondary);
  margin: 0;
}
.heading-badge {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #7e8f75;
  font-size: 12px;
  white-space: nowrap;
}
.heading-badge img {
  width: 24px;
  height: 24px;
  opacity: 0.65;
}
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.14s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
:deep(.el-menu) {
  border-right: 0;
}
:deep(.el-menu-item),
:deep(.el-sub-menu__title) {
  height: 46px;
  line-height: 46px;
  margin: 3px 12px;
  border-radius: 8px;
  font-size: 13px;
}
:deep(.el-menu-item:hover),
:deep(.el-sub-menu__title:hover) {
  background: #ffffff0d;
}
:deep(.el-menu-item.is-active) {
  background: #f0bf6121;
  box-shadow: inset 3px 0 #edc575;
  color: #f2c977;
  font-weight: 600;
}
:deep(.el-sub-menu .el-menu-item) {
  min-width: 0;
  height: 40px;
  line-height: 40px;
  margin-left: 24px;
}
:deep(.el-menu--collapse .el-menu-item),
:deep(.el-menu--collapse .el-sub-menu__title) {
  margin-left: 10px;
  margin-right: 10px;
  width: 56px;
  padding-left: 18px !important;
}
:deep(.el-breadcrumb) {
  line-height: 1.6;
}
:deep(.el-breadcrumb__inner) {
  font-size: 12px;
}
.sidebar-mask {
  display: none;
}
@media (min-width: 1680px) {
  .app-main {
    padding-left: 48px;
    padding-right: 48px;
  }
}
@media (max-width: 1100px) {
  .heading-badge,
  .workspace-label {
    display: none;
  }
  .app-main {
    padding: 24px;
  }
}
@media (max-width: 767px) {
  .sidebar {
    position: fixed;
    inset: 0 auto 0 0;
    z-index: 2001;
    width: 240px !important;
    transform: translateX(-100%);
    transition: transform 0.2s ease;
  }
  .sidebar.mobile-open {
    transform: translateX(0);
  }
  .sidebar-mask {
    display: block;
    position: fixed;
    inset: 0;
    border: 0;
    z-index: 2000;
    background: #10261d66;
  }
  .navbar {
    height: 60px;
    padding: 0 14px;
    gap: 10px;
  }
  .navbar-left {
    gap: 10px;
  }
  .navbar-right {
    gap: 0;
  }
  .username {
    display: none;
  }
  .user-info {
    gap: 6px;
    padding: 2px;
  }
  .breadcrumb {
    max-width: calc(100vw - 150px);
  }
  .app-main {
    padding: 22px 14px;
  }
  .page-heading {
    margin-bottom: 22px;
  }
  .page-heading h1 {
    font-size: 23px;
  }
}
</style>
