<script setup>
import { reactive, ref, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { useRouter } from 'vue-router';
import { updateUserProfile ,changePassword, getUserProfile} from '@/api/app/user';
import { Plus } from "@element-plus/icons-vue";
import { uploadImage } from "@/api/upload.js";
import {removeAppToken} from "@/utils/appToken.js";

const router = useRouter();

const formRef = ref();
const pwdVisible = ref(false);
const pwdSaving = ref(false);
const loading = ref(false);

const form = ref({
  username: '',
  nickname: '',
  password: '',
  email: '',
  gender:'',
  avatarUrl: '',
  phone: ''
});

// 加载用户信息
async function loadUserInfo() {
  loading.value = true;
  try {
    const res = await getUserProfile();
    if (res.code === 0 && res.data) {
      form.value = {
        username: res.data.username || '',
        nickname: res.data.nickname || '',
        password: '',
        email: res.data.email || '',
        gender: res.data.gender || '',
        avatarUrl: res.data.avatarUrl || '',
        phone: res.data.phone || ''
      };
    } else {
      ElMessage.error('获取用户信息失败');
    }
  } catch (error) {
    console.error('获取用户信息失败:', error);
    ElMessage.error('获取用户信息失败');
  } finally {
    loading.value = false;
  }
}


const pwdForm = reactive({
  oldPassword: "",
  newPassword: "",
  confirmPassword: ""
});

function resetPwdForm() {
  pwdForm.oldPassword = ""
  pwdForm.newPassword = ""
  pwdForm.confirmPassword = ""
}

// 上传头像
async function handleAvatarChange(options) {
  try {
    const file = options.file;
    const res = await uploadImage(file); // 你封装的上传接口

    // ✅ 上传成功后，把 OSS 返回的 URL 填到 avatarUrl，立刻预览
    form.value.avatarUrl = res.data;

    ElMessage.success("头像上传成功");

    // ✅ 通知 el-upload 上传成功（不写的话它会一直 loading）
    options.onSuccess?.(res, file);
  } catch (e) {
    options.onError?.(e);
    ElMessage.error(e?.msg || e?.message || "上传失败");
  }
}

// 提交表单
async function submitForm() {
  try {
    // 调用后台接口更新用户信息
    const res = await updateUserProfile(form.value);
    console.log('更新用户信息结果:', res);
    if (res.code === 0) {
      ElMessage.success('个人信息更新成功');
      await router.push('/app/user/profile'); // 跳转到个人主页
    } else {
      ElMessage.error('更新失败');
    }
  } catch (error) {
    console.error('更新用户信息失败:', error);
    ElMessage.error('更新失败');
  }
}

async function cancelEdit() {
  pwdVisible.value = false
}

// 提交密码
async function submitPassword() {
  if (pwdForm.newPassword !== pwdForm.confirmPassword) {
    ElMessage.error("新密码与确认密码不一致");
    return;
  }
  if (pwdForm.newPassword.length < 6) {
    ElMessage.error("新密码至少需要6个字符");
    return;
  }

  pwdSaving.value = true;
  try {
    await changePassword({
      oldPassword: pwdForm.oldPassword,
      newPassword: pwdForm.newPassword
    })
    ElMessage.success("密码修改成功，请重新登录")

    pwdVisible.value = false
    resetPwdForm()

    removeAppToken()
    localStorage.removeItem("USER_TOKEN_NAME")
    await router.replace('/app/login')
  } finally {
    pwdSaving.value = false;
  }
}

// 初始化加载用户信息
onMounted(() => {
  loadUserInfo();
});

</script>

<template>
  <div>
    <h2>编辑个人信息</h2>
    <el-form ref="formRef" :model="form" label-width="80px">
      <!-- 用户名 -->
      <el-form-item label="用户名">
        <el-input v-model="form.username" placeholder="请输入用户名" style="width: 400px;"></el-input>
      </el-form-item>
      <el-form-item label="昵称">
        <el-input v-model="form.nickname" placeholder="请输入昵称" style="width: 400px;"></el-input>
      </el-form-item>

      <!-- 密码 -->
      <el-form-item label="密码">
        <el-button @click="pwdVisible = true">修改密码</el-button>
      </el-form-item>

      <!-- ✅ 修改密码弹窗 -->
      <el-dialog v-model="pwdVisible" title="修改密码" width="420px">
        <el-form :model="pwdForm" label-width="90px">
          <el-form-item label="旧密码" required>
            <el-input v-model="pwdForm.oldPassword" show-password placeholder="请输入旧密码" />
          </el-form-item>

          <el-form-item label="新密码" required>
            <el-input v-model="pwdForm.newPassword" show-password placeholder="请输入新密码" />
          </el-form-item>

          <el-form-item label="确认密码" required>
            <el-input v-model="pwdForm.confirmPassword" show-password placeholder="再次输入新密码" />
          </el-form-item>
        </el-form>

        <template #footer>
          <el-button @click="cancelEdit">取消</el-button>
          <el-button type="primary" :loading="pwdSaving" @click="submitPassword">确认修改</el-button>
        </template>
      </el-dialog>

      <!-- 头像上传 -->
      <el-form-item label="头像">
        <el-upload
            class="avatar-uploader"
            :show-file-list="false"
            :http-request="handleAvatarChange"
            accept="image/*"
        >
          <!-- 有图：显示圆形预览 -->
          <el-image
              v-if="form.avatarUrl"
              :src="form.avatarUrl"
              class="avatar-preview"
              fit="cover"
              preview-teleported
          />
          <!-- 没图：显示虚线圆 + 加号 -->
          <div v-else class="avatar-upload-placeholder">
            <el-icon class="avatar-plus">
              <Plus/>
            </el-icon>
          </div>
        </el-upload>

        <el-button
            v-if="form.avatarUrl"
            type="danger"
            style="margin-left: 12px"
            @click="form.avatarUrl = ''"
        >
          删除
        </el-button>
      </el-form-item>

      <!-- 手机号 -->
      <el-form-item label="手机号">
        <el-input v-model="form.phone" placeholder="请输入手机号" style="width: 400px;"></el-input>
      </el-form-item>
      <el-form-item label="邮箱">
        <el-input v-model="form.email" placeholder="请输入邮箱" style="width: 400px;"></el-input>
      </el-form-item>

      <el-form-item label="性别">
        <el-radio-group v-model="form.gender">
          <el-radio :label="1">男</el-radio>
          <el-radio :label="2">女</el-radio>
        </el-radio-group>
      </el-form-item>

      <!-- 提交按钮 -->
      <el-form-item>
        <el-button type="primary" @click="submitForm">保存</el-button>
        <el-button @click="() => router.push('/app/user/profile')">取消</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<style scoped>

.avatar-uploader {
  display: inline-flex;
  align-items: center;
}

.avatar-upload-placeholder {
  width: 110px;
  height: 110px;
  border: 2px dashed #fff1f0;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;
  background: #fff1f0;
}

.avatar-upload-placeholder:hover {
  background: rgba(64, 158, 255, 0.06);
}

.avatar-plus {
  font-size: 38px;
  color: #8c8c8c;
}

.avatar-preview {
  width: 110px;
  height: 110px;
  overflow: hidden;
  display: block;
}
</style>
