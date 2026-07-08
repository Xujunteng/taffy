<template>
  <div class="card stat-card">
    <div>
      <div class="stat-label">{{ label }}</div>
      <div class="stat-number">{{ value }}</div>
      <div v-if="desc" class="muted" style="font-size:13px">{{ desc }}</div>
    </div>
    <div class="stat-icon"><el-icon size="24"><component :is="icon" /></el-icon></div>
  </div>
</template>
<script setup>
defineProps({ label: String, value: [String, Number], desc: String, icon: { type: String, default: 'DataLine' } })
</script>
<style scoped>
.stat-card { display:flex; align-items:center; justify-content:space-between; min-height:128px; position:relative; overflow:hidden; }
.stat-card::after { content:""; position:absolute; right:-40px; bottom:-55px; width:140px; height:140px; background: radial-gradient(circle, rgba(125,211,252,.24), transparent 66%); }
</style>
