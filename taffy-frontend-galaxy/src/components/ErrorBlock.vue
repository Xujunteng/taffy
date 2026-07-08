<template><el-alert v-if="message" :title="message" type="error" show-icon :closable="false" style="margin-bottom:12px" /></template>
<script setup>defineProps({ message: String })</script>
