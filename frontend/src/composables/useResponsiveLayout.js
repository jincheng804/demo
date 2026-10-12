import { ref, onMounted, onBeforeUnmount } from 'vue'

// 详情表格在窄屏减少列数，保持字段名完整；离开页面时解除监听。
export function useResponsiveLayout() {
  const compact = ref(false)
  let media
  function update() {
    compact.value = media.matches
  }
  onMounted(() => {
    media = window.matchMedia('(max-width: 767px)')
    update()
    media.addEventListener('change', update)
  })
  onBeforeUnmount(() => media?.removeEventListener('change', update))
  return { compact }
}
