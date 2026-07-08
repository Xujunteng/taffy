<template>
  <div class="space-between page-header">
    <div>
      <div class="eyebrow">TAFFY GALAXY CONSOLE</div>
      <h2 class="glow-title">{{ title }}</h2>
      <div v-if="subtitle" class="muted">{{ subtitle }}</div>
    </div>
    <div class="page-actions"><slot /></div>
  </div>
</template>
<script setup>
defineProps({ title: { type: String, required: true }, subtitle: { type: String, default: '' } })
</script>
<style scoped>
.page-header { margin-bottom: 18px; align-items: flex-end; }
.eyebrow { color: #7dd3fc; font-size: 12px; letter-spacing: .18em; font-weight: 800; margin-bottom: 6px; }
h2 { margin: 0 0 7px; font-size: 30px; font-weight: 950; letter-spacing: -.04em; }
.page-actions { display: flex; gap: 10px; flex-wrap: wrap; justify-content: flex-end; }
@media (max-width: 720px) { .page-header { align-items:flex-start; flex-direction: column; } .page-actions { justify-content:flex-start; } }
</style>
