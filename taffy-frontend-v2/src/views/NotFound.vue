<template>
  <div class="page" style="display:flex;align-items:center;justify-content:center;min-height:60vh">
    <div style="text-align:center">
      <h1 style="font-size:96px;color:#dcdfe6;margin:0">404</h1>
      <p style="font-size:18px;color:#909399;margin:16px 0 32px">页面不存在或已被移除</p>
      <el-space>
        <el-button @click="$router.back()">返回上页</el-button>
        <el-button type="primary" @click="$router.push('/dashboard')">回到首页</el-button>
      </el-space>
    </div>
  </div>
</template>
