<template>
  <div class="audio-shell">
    <div class="disc" :class="{ spinning: Boolean(url) }"><span>♪</span></div>
    <audio v-if="url" :src="url" controls class="audio-player"></audio>
    <EmptyState v-else description="暂无音频结果" />
  </div>
</template>
<script setup>
import EmptyState from './EmptyState.vue'
defineProps({ url: String })
</script>
<style scoped>
.audio-shell { padding: 10px; border: 1px solid rgba(255,255,255,.12); border-radius: 20px; background: rgba(255,255,255,.05); }
.disc { width: 98px; height:98px; border-radius:50%; margin: 8px auto 16px; display:grid; place-items:center; background: conic-gradient(from 20deg, #7dd3fc, #a78bfa, #fde68a, #7dd3fc); box-shadow: 0 0 36px rgba(125,211,252,.28); }
.disc span { width: 48px; height:48px; border-radius:50%; display:grid; place-items:center; background:#07102b; color:#fff; font-size:24px; }
.spinning { animation: spin 4s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }
</style>
