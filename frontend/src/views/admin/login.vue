<template>
  <div class="login-page">
    <div class="login-box">
      <!-- 左侧区域 -->
      <div class="left">
        <div class="logo-circle">
          <div class="logo-text">音乐<br />后台</div>
        </div>

        <div class="welcome">
          <div class="welcome-title">欢迎使用</div>
          <div class="welcome-sub">音乐后台管理系统</div>
        </div>
      </div>

      <!-- 右侧登录区域 -->
      <div class="right">
        <div class="right-title">音乐后台</div>
        <div class="right-sub">后台管理系统</div>

        <el-form
            ref="formRef"
            :model="form"
            :rules="rules"
            class="form"
            @keyup.enter="handleLogin"
        >
          <el-form-item prop="username">
            <el-input
                v-model="form.username"
                placeholder="请输入用户名"
                size="large"
                clearable
            >
              <template #prefix>
                <el-icon><User /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item prop="password">
            <el-input
                v-model="form.password"
                placeholder="请输入密码"
                size="large"
                show-password
                clearable
                type="password"
            >
              <template #prefix>
                <el-icon><Lock /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-button
              type="primary"
              size="large"
              class="login-btn"
              :loading="loading"
              @click="handleLogin"
          >
            登录
          </el-button>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from "vue"
import { useRouter } from "vue-router"
import { ElMessage } from "element-plus"
import { User, Lock } from "@element-plus/icons-vue"
import { useAdminStore } from "@/store/admin"
import { removeToken } from "@/utils/token"   // 你项目里有这个

const router = useRouter()
const store = useAdminStore()

const formRef = ref()
const loading = ref(false)

const form = reactive({
  username: "",
  password: ""
})

const rules = {
  username: [{ required: true, message: "请输入用户名", trigger: "blur" }],
  password: [{ required: true, message: "请输入密码", trigger: "blur" }]
}

async function handleLogin() {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    loading.value = true
    try {
      // 清理旧登录信息
      removeToken()
      localStorage.removeItem("ADMIN_ROLES")
      localStorage.removeItem("ADMIN_PERMS")
      localStorage.removeItem("ADMIN_USER")
      localStorage.removeItem("ADMIN_TOKEN_NAME")

      // 登录
      await store.login(form)

      ElMessage.success("登录成功 ✅")

      // 统一跳转
      router.push("/admin/index")

    } catch (e) {
      console.log("登录失败：", e)
      ElMessage.error(e?.msg || "登录失败，请检查用户名或密码")
    } finally {
      loading.value = false
    }
  })
}

</script>


<style scoped>
.login-page {
  height: 100vh;
  background: linear-gradient(135deg, #6c7cff, #8d5cff);
  display: flex;
  justify-content: center;
  align-items: center;
}

.login-box {
  width: 900px;
  height: 460px;
  display: flex;
  border-radius: 14px;
  overflow: hidden;
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.25);
  background: #fff;
}

/* 左侧 */
.left {
  width: 45%;
  background: linear-gradient(135deg, #5f8cff, #8a5cff);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  color: #fff;
  position: relative;
}

.logo-circle {
  width: 170px;
  height: 170px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.22);
  display: flex;
  justify-content: center;
  align-items: center;
  margin-bottom: 40px;
  box-shadow: inset 0 0 20px rgba(255, 255, 255, 0.35);
}

.logo-text {
  text-align: center;
  font-size: 28px;
  font-weight: 700;
  line-height: 1.3;
}

.welcome-title {
  font-size: 22px;
  font-weight: 700;
  margin-bottom: 12px;
  text-align: center;
}

.welcome-sub {
  font-size: 14px;
  opacity: 0.9;
  text-align: center;
}

/* 右侧 */
.right {
  flex: 1;
  padding: 70px 80px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.right-title {
  font-size: 28px;
  font-weight: 800;
  text-align: center;
  margin-bottom: 10px;
}

.right-sub {
  font-size: 14px;
  text-align: center;
  color: #888;
  margin-bottom: 45px;
}

.form {
  width: 100%;
}

.login-btn {
  width: 100%;
  margin-top: 10px;
  border-radius: 8px;
}
</style>
