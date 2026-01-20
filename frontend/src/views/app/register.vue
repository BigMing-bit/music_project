<template>
  <div class="login-page">
    <div class="card">
      <div class="title">Harmony · 用户注册</div>

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
          <el-input v-model="form.phone" placeholder="请输入手机号" size="large" clearable>
            <template #prefix>
              <el-icon><Phone /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item>
          <div class="captcha-row">
            <el-input v-model="form.smsCode" placeholder="请输入手机验证码" style="flex:1;" />
            <el-button 
              type="primary" 
              :disabled="countdown > 0" 
              @click="sendSmsCode"
            >
              {{ countdown > 0 ? `${countdown}秒后重发` : '获取验证码' }}
            </el-button>
          </div>
        </el-form-item>

        <el-form-item>
          <div class="captcha-row">
            <el-input v-model="form.captchaCode" placeholder="请输入图形验证码" style="flex:1;" />
            <div class="captcha-img" @click="loadCaptcha" :title="'点击刷新'">
              <img v-if="captchaImg" :src="captchaImg" alt="captcha" />
              <span v-else>加载中</span>
            </div>
          </div>
        </el-form-item>

        <!-- 添加角色选择 -->
        <el-form-item label="注册身份">
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
          注册
        </el-button>

        <div class="tips">
          <span class="link" @click="goLogin">已有账号？去登录</span>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from "vue";
import { ElMessage } from "element-plus";
import { useRouter } from "vue-router";
import { userRegister, getCaptcha } from "@/api/app/user";
import { Lock, User, Avatar, Phone } from "@element-plus/icons-vue";
import request from "@/utils/appRequest";

const router = useRouter();

const loading = ref(false);
const captchaImg = ref("");
const countdown = ref(0);
let countdownTimer = null;

const form = reactive({
  username: "",
  password: "",
  phone: "",
  smsCode: "",
  captchaId: "",
  captchaCode: "",
  role: 0, // 默认角色为普通用户（0）
});

// 加载图形验证码
const loadCaptcha = async () => {
  try {
    const res = await getCaptcha();
    form.captchaId = res.data.captchaId;
    captchaImg.value = res.data.img;
  } catch (error) {
    ElMessage.error("验证码加载失败");
  }
};

// 发送短信验证码
const sendSmsCode = async () => {
  if (!form.phone) {
    return ElMessage.warning("请输入手机号");
  }
  
  if (!/^1[3-9]\d{9}$/.test(form.phone)) {
    return ElMessage.warning("请输入正确的手机号");
  }

  try {
    const res = await request({
      url: "/app/user/send-sms",
      method: "post",
      data: {
        phone: form.phone
      }
    });
    
    ElMessage.success("验证码发送成功");
    startCountdown();
  } catch (error) {
    ElMessage.error("验证码发送失败，请稍后重试");
  }
};

// 倒计时函数
const startCountdown = () => {
  countdown.value = 60;
  if (countdownTimer) {
    clearInterval(countdownTimer);
  }
  countdownTimer = setInterval(() => {
    if (countdown.value > 0) {
      countdown.value--;
    } else {
      clearInterval(countdownTimer);
    }
  }, 1000);
};

// 提交表单
const onSubmit = async () => {
  if (!form.username.trim()) return ElMessage.warning("请输入用户名");
  if (!form.password.trim()) return ElMessage.warning("请输入密码");
  if (!form.phone) return ElMessage.warning("请输入手机号");
  if (!form.smsCode) return ElMessage.warning("请输入短信验证码");
  if (!form.captchaCode.trim()) return ElMessage.warning("请输入图形验证码");

  loading.value = true;
  try {
    const res = await userRegister({
      username: form.username.trim(),
      password: form.password.trim(),
      phone: form.phone,
      smsCode: form.smsCode,
      captchaId: form.captchaId,
      captchaCode: form.captchaCode.trim(),
      role: form.role,
    });

    ElMessage.success("注册成功，请登录");
    router.push("/app/login");
  } catch (error) {
    ElMessage.error(error.response?.data?.message || "注册失败，请稍后重试");
  } finally {
    loading.value = false;
  }
};

// 跳转到登录页
const goLogin = () => {
  router.push("/app/login");
};

// 页面加载时，获取图形验证码
onMounted(() => {
  loadCaptcha();
});
</script>

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