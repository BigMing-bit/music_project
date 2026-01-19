<template>
  <div class="login-page">
    <div class="card">
      <div class="title">Harmony · 用户登录</div>

      <el-form :model="form" @keyup.enter="onSubmit">
        <el-form-item>
          <el-input v-model="form.username" placeholder="请输入用户名" size="large" clearable>
            <template #prefix>
              <el-icon><User /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item prop="password">
          <el-input v-model="form.password" placeholder="请输入密码" size="large" show-password clearable type="password">
            <template #prefix>
              <el-icon><Lock /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item>
          <div class="captcha-row">
            <el-input v-model="form.captchaCode" placeholder="验证码" style="flex:1;" />
            <div class="captcha-img" @click="loadCaptcha" :title="'点击刷新'">
              <img v-if="captchaImg" :src="captchaImg" alt="captcha" />
              <span v-else>加载中</span>
            </div>
          </div>
        </el-form-item>

        <!-- 添加角色选择 -->
        <el-form-item label="登录身份">
          <el-select
              v-model="form.role"
              placeholder="请选择身份"
              clearable
              size="large"
          >
            <el-option label="普通用户" :value="0" />
            <el-option label="官方" :value="1" />
          </el-select>
        </el-form-item>


        <el-button type="primary" :loading="loading" class="btn" @click="onSubmit">
          登录
        </el-button>

        <div class="tips">
          <span class="link" @click="goHome">先逛逛</span>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from "vue";
import { ElMessage } from "element-plus";
import { useUserStore } from "@/store/user";
import { useRouter } from "vue-router";
import { userLogin, getCaptcha } from "@/api/app/user";
import { Lock, User } from "@element-plus/icons-vue";

const router = useRouter();
const userStore = useUserStore();

const loading = ref(false);
const captchaImg = ref("");

const form = reactive({
  username: "",
  password: "",
  captchaId: "",
  captchaCode: "",
  role: null, // 默认角色为普通用户（0）
});

// 加载验证码
const loadCaptcha = async () => {
  try {
    const res = await getCaptcha();
    form.captchaId = res.data.captchaId;
    captchaImg.value = res.data.img;
  } catch (error) {
    ElMessage.error("验证码加载失败");
  }
};

// 提交表单
const onSubmit = async () => {
  if (!form.username.trim()) return ElMessage.warning("请输入用户名");
  if (!form.password.trim()) return ElMessage.warning("请输入密码");
  if (!form.captchaCode.trim()) return ElMessage.warning("请输入验证码");

  if (form.role === null) {
    return ElMessage.warning("请选择登录身份");
  }

  loading.value = true;
  try {
    const res = await userLogin({
      username: form.username.trim(),
      password: form.password.trim(),
      captchaId: form.captchaId,
      captchaCode: form.captchaCode.trim(),
      role: form.role, // 只可能是 0 或 1
    });

    await userStore.login(res.data);
    ElMessage.success("登录成功");
    router.push("/app/index");
  } finally {
    loading.value = false;
  }
};



// 页面加载时，获取验证码
onMounted(() => {
  loadCaptcha();
});
</script>

<style scoped>
/* 样式根据你的需求来 */
</style>


<style scoped>
.login-page{
  min-height: calc(100vh - 64px);
  display:flex;
  align-items:center;
  justify-content:center;
  padding: 20px;
}
.card{
  width: 420px;
  background: #fff;
  border-radius: 14px;
  padding: 26px;
  box-shadow: 0 10px 30px rgba(0,0,0,.08);
}
.title{
  font-size: 18px;
  font-weight: 800;
  margin-bottom: 18px;
}
.btn{ width: 100%; height: 40px; background: #31c27c}
.tips{ margin-top: 12px; text-align:center; font-size: 13px; color:#666; }
.link{ cursor:pointer; color:#409eff; }

.captcha-row{ display:flex; gap:10px; width:100%; }
.captcha-img{
  width: 120px;
  height: 36px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  overflow:hidden;
  display:flex;
  align-items:center;
  justify-content:center;
  cursor:pointer;
  background:#fafafa;
}
.captcha-img img{
  width: 100%;
  height: 100%;
  object-fit: cover;
}
</style>
