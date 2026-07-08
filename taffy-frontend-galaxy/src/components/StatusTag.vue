<template><el-tag :type="tagType" effect="light">{{ normalized }}</el-tag></template>
<script setup>
import { computed } from 'vue'
const props = defineProps({ value: String })
const normalized = computed(() => props.value || '未知')
const tagType = computed(() => {
  const v = normalized.value
  if (['就绪', 'completed', 'success', '已完成', 'ready'].includes(v)) return 'success'
  if (['失败', 'failed', 'error'].includes(v)) return 'danger'
  if (['处理中', '训练中', 'processing', 'pending', 'running'].includes(v)) return 'warning'
  return 'info'
})
</script>
