<template>
  <el-tag :type="tagType" effect="light">{{ value || '未知' }}</el-tag>
</template>
<script setup>
import { computed } from 'vue'
const props = defineProps({ value: String })
const tagType = computed(() => {
  const value = String(props.value || '').toLowerCase()
  if (['就绪', 'completed', 'success', '已完成'].includes(props.value) || ['completed', 'success'].includes(value)) return 'success'
  if (['失败'].includes(props.value) || ['failed', 'error', 'fail'].includes(value)) return 'danger'
  if (['处理中', '训练中'].includes(props.value) || ['processing', 'pending', 'running'].includes(value)) return 'warning'
  return 'info'
})
</script>
