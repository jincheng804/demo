<template>
  <span class="glossary-tip">
    <span class="glossary-text">{{ term }}</span>
    <el-tooltip :content="explanation" placement="top" :show-after="200">
      <el-icon class="glossary-icon"><QuestionFilled /></el-icon>
    </el-tooltip>
  </span>
</template>

<script setup>
import { computed } from 'vue'
import { QuestionFilled } from '@element-plus/icons-vue'

const props = defineProps({
  term: { type: String, required: true },
  explanation: { type: String, default: '' }
})

// 内置蜂场专业词汇词典
const glossary = {
  '休药期': '从最后一次用药到可以安全采收蜂蜜的间隔天数，期间禁止摇蜜',
  '群势': '蜂群中蜜蜂数量的多少，通常分为强、中、弱三个等级',
  '摇蜜': '通过摇蜜机离心作用将蜂蜜从巢脾中分离出来的采收过程',
  '饲喂': '外界蜜源不足时人工补充饲料（白糖水、花粉等）以维持蜂群生存',
  '蜜源地': '蜜蜂采集花蜜的主要植物分布区域，决定蜂蜜的花种和品质',
  '装瓶': '将原蜜按规格灌装到瓶中的生产环节，是采收和销售之间的关键步骤',
  '溯源码': '用于追溯产品从养殖到销售全流程信息的唯一标识码',
  '出房日期': '蜂王从王台中羽化出房的日期，是判断蜂王年龄和产卵能力的重要依据',
  '停产': '蜂王因季节、衰老或病害等原因停止产卵的状态',
  '封盖蜜': '蜜蜂将酿好的蜂蜜用蜂蜡封盖储存，含水量通常低于20%，为成熟蜜',
  '未封盖蜜': '尚未封盖、含水量较高的蜂蜜，需继续酿造才能达到成熟标准',
  '巢础': '人工制造的蜜蜂巢房基底，供蜜蜂筑造巢脾使用',
  '蜂螨': '寄生在蜜蜂体表的寄生虫，是蜂群主要敌害之一，严重时可导致蜂群覆灭',
  '白垩病': '蜜蜂幼虫的真菌性传染病，病死幼虫呈白垩状干尸',
  '水分': '蜂蜜中的含水量，国家标准要求一级品水分≤20%，水分过高易发酵变质',
  '淀粉酶值': '衡量蜂蜜新鲜度和加工温度的重要指标，国家标准要求≥4mL/(g·h)',
  '蔗糖含量': '蜂蜜中蔗糖的含量，国家标准要求≤5g/100g，过高可能为掺假',
  '羟甲基糠醛': '蜂蜜在高温或长期储存后产生的物质，国家标准要求≤40mg/kg，反映蜂蜜新鲜度',
  '安全采收日期': '用药后经过休药期，可以安全摇蜜采收的日期',
  '蜂王': '蜂群中唯一正常产卵的雌性蜂，是蜂群的核心，其品质直接影响蜂群发展',
  '工蜂': '生殖器官发育不完全的雌性蜂，承担采集、哺育、筑巢、守卫等全部工作',
  '雄蜂': '蜂群中的雄性蜂，唯一职能是与处女王交配，不参与采集和哺育',
  '分蜂': '蜂群自然繁殖的方式，老蜂王带领部分工蜂离开原巢另筑新巢',
  '换王': '人工淘汰老蜂王并引入新蜂王的操作，以保持蜂群旺盛的繁殖力',
}

const explanation = computed(() => props.explanation || glossary[props.term] || '')
</script>

<style scoped>
.glossary-tip {
  display: inline-flex;
  align-items: center;
  gap: 3px;
}
.glossary-icon {
  color: #c0c4cc;
  font-size: 13px;
  cursor: help;
  transition: color 0.2s;
}
.glossary-icon:hover {
  color: #409eff;
}
</style>
