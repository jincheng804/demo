<template>
  <button class="workspace-search" type="button" @click="open = true" aria-label="搜索功能菜单">
    <el-icon><Search /></el-icon><span>搜索功能</span><kbd>Ctrl K</kbd>
  </button>
  <el-dialog v-model="open" title="前往功能" width="560px" class="workspace-search-dialog" @opened="focusInput">
    <el-input ref="input" v-model="query" placeholder="输入功能名称，如蜂箱、采收、出库" clearable
      aria-label="搜索功能名称" @keydown.down.prevent="move(1)" @keydown.up.prevent="move(-1)" @keydown.enter.prevent="visit(results[selected])" />
    <div class="search-results" role="navigation" aria-label="功能搜索结果">
      <p v-if="!results.length" class="search-empty">没有找到匹配的功能，试试其他关键词。</p>
      <button v-for="(item, index) in results" :key="item.path" type="button" class="search-result"
        :class="{ selected: selected === index }" @click="visit(item)" @mouseenter="selected = index">
        <span class="search-result-icon"><el-icon><component :is="item.icon || 'FolderOpened'" /></el-icon></span>
        <span><strong>{{ item.title }}</strong><small>{{ item.group || '工作空间' }}</small></span>
        <el-icon class="result-arrow"><ArrowRight /></el-icon>
      </button>
    </div>
    <template #footer><span class="search-hint">↑ ↓ 选择 · Enter 打开 · Esc 关闭</span></template>
  </el-dialog>
</template>
<script setup>
import { computed, ref, watch, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { resolveMenuPath } from '@/utils/menu'
const router = useRouter(), store = useUserStore()
const open = ref(false), query = ref(''), selected = ref(0), input = ref()
function flatten(routes, base = '', group = '') {
  return routes.flatMap(route => {
    if (route.hidden) return []
    const path = resolveMenuPath(base, route.path)
    if (route.children?.length) return flatten(route.children, path, route.meta?.title || group)
    return route.meta?.title ? [{ path, title: route.meta.title, icon: route.meta.icon, group }] : []
  })
}
const results = computed(() => flatten(store.sidebarRoutes).filter(item =>
  `${item.title} ${item.group}`.toLowerCase().includes(query.value.trim().toLowerCase())).slice(0, 12))
watch(query, () => { selected.value = 0 })
watch(open, value => { if (value) { query.value = ''; selected.value = 0 } })
function move(delta) { if (results.value.length) selected.value = (selected.value + delta + results.value.length) % results.value.length }
function visit(item) { if (item) { open.value = false; router.push(item.path) } }
function focusInput() { input.value?.focus() }
function shortcut(event) {
  if ((event.ctrlKey || event.metaKey) && event.key.toLowerCase() === 'k') { event.preventDefault(); open.value = !open.value }
}
onMounted(() => window.addEventListener('keydown', shortcut))
onBeforeUnmount(() => window.removeEventListener('keydown', shortcut))
</script>
<style scoped>
.workspace-search { display:flex; align-items:center; gap:10px; border:1px solid var(--border); border-radius:8px; padding:8px 10px; background:var(--bg-page); color:var(--text-secondary); cursor:pointer; font-size:12px; }
.workspace-search kbd { margin-left:24px; font:10px inherit; border:1px solid var(--border); border-radius:4px; padding:2px 4px; background:#fff; }
.search-results { margin-top:14px; max-height:390px; overflow:auto; }
.search-result { width:100%; display:flex; align-items:center; gap:12px; border:0; background:transparent; border-radius:8px; padding:12px; color:var(--text-primary); text-align:left; cursor:pointer; }
.search-result.selected,.search-result:hover { background:#edf3ee; }
.search-result-icon { display:grid; place-items:center; width:36px; height:36px; border:1px solid #dfe7e1; border-radius:8px; color:#35684d; background:white; }
.search-result strong { display:block; font-weight:550; font-size:14px; }
.search-result small { display:block; margin-top:4px; color:var(--text-secondary); font-size:12px; }
.result-arrow { margin-left:auto; color:var(--text-secondary); }
.search-hint,.search-empty { color:var(--text-secondary); font-size:12px; }
@media(max-width:900px) { .workspace-search kbd { display:none; } }
@media(max-width:767px) { .workspace-search span,.workspace-search kbd { display:none; } .workspace-search { padding:9px; background:white; } }
</style>
