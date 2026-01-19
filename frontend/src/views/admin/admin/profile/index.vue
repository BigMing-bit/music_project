<template>
  <div class="panel">
    <div class="title">个人信息</div>

    <el-form :model="form" label-width="90px" style="max-width: 560px;">
      <el-form-item label="头像">
        <el-upload
            class="uploader avatar-uploader"
            :show-file-list="false"
            :http-request="handleUploadAvatar"
            accept="image/*"
        >
          <el-image
              v-if="form.avatarUrl"
              :src="form.avatarUrl"
              class="avatar-preview"
              fit="cover"
              preview-teleported
          />
          <div v-else class="upload-plus">
            <el-icon><Plus /></el-icon>
          </div>
        </el-upload>

        <el-button v-if="form.avatarUrl" type="danger" link style="margin-left:10px" @click="form.avatarUrl = ''">
          删除
        </el-button>
      </el-form-item>

      <el-form-item label="用户名">
        <el-input v-model="form.username" disabled />
      </el-form-item>

      <el-form-item label="昵称">
        <el-input v-model="form.nickname" placeholder="请输入昵称" />
      </el-form-item>

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
          <el-button @click="pwdVisible = false">取消</el-button>
          <el-button type="primary" :loading="pwdSaving" @click="submitPassword">确认修改</el-button>
        </template>
      </el-dialog>


      <el-form-item>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
        <el-button @click="load">刷新</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from "vue"
import { ElMessage } from "element-plus"
import { Plus } from "@element-plus/icons-vue"
import { getAdminProfile, updateAdminProfile ,changeAdminPassword } from "@/api/admin/profile"
import { uploadImage } from "@/api/upload"

import { removeToken } from "@/utils/token"
import { useRouter } from "vue-router"
const router = useRouter()

const saving = ref(false)

const form = reactive({
  id: null,
  username: "",
  nickname: "",
  avatarUrl: "",
})

async function load() {
  const res = await getAdminProfile()
  const a = res.data || {}
  form.id = a.id
  form.username = a.username || ""
  form.nickname = a.nickname || ""
  form.avatarUrl = a.avatarUrl || ""
}

async function handleUploadAvatar({ file }) {
  const res = await uploadImage(file)
  const url = res.data
  if (!url) return ElMessage.error("上传失败：未返回图片地址")
  form.avatarUrl = url
  ElMessage.success("上传成功")
}

// ✅ 保存个人信息：只改昵称/头像
async function save() {
  saving.value = true
  try {
    await updateAdminProfile({
      nickname: form.nickname,
      avatarUrl: form.avatarUrl
    })
    ElMessage.success("保存成功")
    await load()

    // ✅ 同步右上角显示
    const prof = await getAdminProfile()
    localStorage.setItem("ADMIN_USER", JSON.stringify(prof.data || {}))
  } finally {
    saving.value = false
  }
}

/** ================= 修改密码流程 ================= */
const pwdVisible = ref(false)
const pwdSaving = ref(false)

const pwdForm = reactive({
  oldPassword: "",
  newPassword: "",
  confirmPassword: ""
})

function resetPwdForm() {
  pwdForm.oldPassword = ""
  pwdForm.newPassword = ""
  pwdForm.confirmPassword = ""
}

async function submitPassword() {
  if (!pwdForm.oldPassword.trim()) return ElMessage.error("请输入旧密码")
  if (!pwdForm.newPassword.trim()) return ElMessage.error("请输入新密码")
  if (pwdForm.newPassword !== pwdForm.confirmPassword) {
    return ElMessage.error("两次新密码不一致")
  }

  pwdSaving.value = true
  try {
    await changeAdminPassword({
      oldPassword: pwdForm.oldPassword,
      newPassword: pwdForm.newPassword
    })

    ElMessage.success("密码修改成功，请重新登录")

    // ✅ 修改密码后：清理本地并回到登录页
    pwdVisible.value = false
    resetPwdForm()

    removeToken()
    localStorage.removeItem("ADMIN_ROLES")
    localStorage.removeItem("ADMIN_PERMS")
    localStorage.removeItem("ADMIN_USER")
    localStorage.removeItem("ADMIN_TOKEN_NAME")

    router.replace("/admin/login")
  } finally {
    pwdSaving.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.panel { background:#fff; border-radius:8px; padding:18px; box-shadow:0 2px 10px rgba(0,0,0,.05); }
.title { font-size:16px; font-weight:700; margin-bottom:14px; }

.avatar-uploader :deep(.el-upload) {
  border: 1px dashed #dcdfe6;
  border-radius: 50%;
  width: 88px;
  height: 88px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.avatar-preview {
  width: 88px;
  height: 88px;
  border-radius: 50%;
}

.upload-plus {
  width: 88px;
  height: 88px;
  display:flex;
  align-items:center;
  justify-content:center;
  color:#909399;
  font-size: 28px;
}
</style>
