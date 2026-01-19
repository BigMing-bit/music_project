<template>
  <div class="forbidden">
    <h1>403</h1>
    <p>无权限访问该页面</p>
    <el-button type="primary" @click="goHome">返回首页</el-button>
  </div>
</template>

<script setup>
import { useRouter } from "vue-router"

const router = useRouter()

function goHome() {
  router.push("/admin/index")
}
</script>

<style scoped>
.forbidden {
  height: 100%;
  min-height: 400px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  color: #666;
}

h1 {
  font-size: 72px;
  margin-bottom: 12px;
  color: #409eff;
}

p {
  font-size: 16px;
  margin-bottom: 20px;
}
</style>
