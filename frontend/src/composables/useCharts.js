import { onBeforeUnmount } from 'vue'
import echarts from '@/utils/charts'
export const chartPalette = [
  '#487c5b',
  '#d6a14c',
  '#84a991',
  '#b97845',
  '#7c9890',
  '#c56e62',
  '#8b839e',
  '#b8b578'
]
// 观察图表容器，兼容侧栏收缩、窗口变化和标签页切换。
export function useCharts() {
  const charts = new Map()
  let disposed = false
  const observer = new ResizeObserver((entries) => {
    entries.forEach(({ target, contentRect }) => {
      const chart = charts.get(target)
      if (
        chart &&
        !chart.isDisposed() &&
        contentRect.width > 0 &&
        contentRect.height > 0
      )
        chart.resize()
    })
  })
  function renderChart(el, option) {
    if (disposed || !el?.isConnected) return
    let chart = charts.get(el)
    if (!chart || chart.isDisposed()) {
      chart = echarts.init(el)
      charts.set(el, chart)
      observer.observe(el)
    }
    const empty =
      option.series?.length &&
      option.series.every((series) => !series.data?.length)
    chart.setOption(
      {
        color: chartPalette,
        animation: !window.matchMedia('(prefers-reduced-motion: reduce)')
          .matches,
        textStyle: { color: '#758178', fontFamily: 'sans-serif' },
        graphic: empty
          ? {
              type: 'text',
              left: 'center',
              top: 'middle',
              style: { text: '暂无数据', fill: '#758178', fontSize: 13 }
            }
          : [],
        ...option
      },
      true
    )
    return chart
  }
  function clearCharts() {
    observer.disconnect()
    charts.forEach((chart) => chart.dispose())
    charts.clear()
  }
  onBeforeUnmount(() => {
    disposed = true
    clearCharts()
  })
  return { renderChart, clearCharts }
}
