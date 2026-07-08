<template><div class="empty-box"><el-empty :description="description"><slot /></el-empty></div></template>
<script setup>defineProps({ description: { type: String, default: '暂无数据' } })</script>
