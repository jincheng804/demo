<template>
  <el-menu-item v-if="showSingle" :index="displayPath">
    <el-icon v-if="displayIcon"><component :is="displayIcon" /></el-icon>
    <template #title>{{ displayChild.meta?.title }}</template>
  </el-menu-item>
  <el-sub-menu v-else :index="nodePath">
    <template #title>
      <el-icon v-if="item.meta?.icon"
        ><component :is="normalizeIcon(item.meta.icon)"
      /></el-icon>
      <span>{{ item.meta?.title }}</span>
    </template>
    <sidebar-item
      v-for="child in visibleChildren"
      :key="child.path"
      :item="child"
      :base-path="nodePath"
    />
  </el-sub-menu>
</template>
<script setup>
import { computed, getCurrentInstance } from 'vue'
import { resolveMenuPath } from '@/utils/menu'
const props = defineProps({
  item: { type: Object, required: true },
  basePath: { type: String, default: '' }
})
const registeredIcons = getCurrentInstance().appContext.components
const normalizeIcon = (name) =>
  name === 'Syringe' ? 'FirstAidKit' : registeredIcons[name] ? name : 'Menu'
const nodePath = computed(() =>
  resolveMenuPath(props.basePath, props.item.path)
)
const visibleChildren = computed(() =>
  (props.item.children || []).filter((c) => !c.hidden)
)
const showSingle = computed(
  () =>
    !visibleChildren.value.length ||
    (visibleChildren.value.length === 1 &&
      !(visibleChildren.value[0].children || []).some((c) => !c.hidden))
)
const displayChild = computed(() =>
  visibleChildren.value.length === 1 ? visibleChildren.value[0] : props.item
)
const displayPath = computed(() =>
  displayChild.value === props.item
    ? nodePath.value
    : resolveMenuPath(nodePath.value, displayChild.value.path)
)
const displayIcon = computed(() => {
  const name = displayChild.value.meta?.icon || props.item.meta?.icon
  return name ? normalizeIcon(name) : ''
})
</script>
