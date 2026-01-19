<template>
  <div class="page">
    <!-- 查询栏 -->
    <div class="panel">
      <el-form :inline="true" :model="query" class="query-form">
        <el-form-item label="关键词">
          <el-input v-model="query.keyword" placeholder="用户名/昵称" clearable style="width: 200px" />
        </el-form-item>

        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 120px">
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 工具栏 -->
    <div class="toolbar">
      <div class="toolbar-left">
        <el-button type="primary" @click="openDialog()">新增管理员</el-button>
        <el-button type="danger" :disabled="selectedIds.length === 0" @click="handleBatchDelete">批量删除</el-button>
      </div>
      <div class="toolbar-right">共 <span class="total">{{ total }}</span> 条</div>
    </div>

    <!-- 表格 -->
    <div class="panel">
      <el-table
          :data="list"
          stripe
          border
          v-loading="loading"
          element-loading-text="加载中..."
          @selection-change="handleSelectionChange"
          :header-cell-style="headerStyle"
      >
        <el-table-column type="selection" width="50" />
        <el-table-column prop="username" label="用户名" min-width="160" align="center" />
        <el-table-column prop="nickname" label="昵称" min-width="160" align="center" />
        <el-table-column prop="avatarUrl" label="头像" width="110" align="center">
          <template #default="{ row }">
            <el-image
                v-if="row.avatarUrl"
                :src="row.avatarUrl"
                style="width: 44px; height: 44px; border-radius: 50%;"
                fit="cover"
                preview-teleported
            />
            <span v-else class="avatar-empty">无</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="120" align="center">
          <template #default="{ row }">
            <el-switch
                v-model="row.status"
                :active-value="1"
                :inactive-value="0"
                @change="val => changeStatus(row.id, val)"
            />
          </template>
        </el-table-column>

        <el-table-column prop="lastLoginTime" label="最后登录" width="180" align="center">
          <template #default="{ row }">{{ formatTime(row.lastLoginTime) }}</template>
        </el-table-column>

        <el-table-column prop="createTime" label="创建时间" width="180" align="center">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>

        <el-table-column label="操作" width="180" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" @click="openDialog(row)">编辑</el-button>
            <el-button type="danger" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pager">
        <el-pagination
            layout="total, prev, pager, next, sizes, jumper"
            :total="total"
            :page-size="query.pageSize"
            :page-sizes="[10, 20, 50, 100]"
            :current-page="query.pageNum"
            @current-change="pageChange"
            @size-change="sizeChange"
        />
      </div>
    </div>

    <!-- 弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="560px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="用户名" required>
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>

        <el-form-item label="密码" v-if="!form.id" required>
          <el-input v-model="form.password" placeholder="请输入初始密码" show-password />
        </el-form-item>

        <el-form-item label="昵称">
          <el-input v-model="form.nickname" placeholder="昵称（可选）" />
        </el-form-item>

        <el-form-item label="头像">
          <el-upload
              class="avatar-uploader"
              :show-file-list="false"
              :http-request="handleUploadAvatar"
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
              <el-icon class="avatar-plus"><Plus /></el-icon>
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
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from "vue"
import { ElMessage, ElMessageBox } from "element-plus"
import { getAdminList, addAdmin, updateAdmin, deleteAdmins, updateAdminStatus } from "@/api/admin/admin"
import {Plus} from "@element-plus/icons-vue";
import {uploadImage} from "@/api/upload.js";

const loading = ref(false)
const list = ref([])
const total = ref(0)
const selectedIds = ref([])

const DEFAULT_PAGE_SIZE = Number(localStorage.getItem("ADMIN_PAGE_SIZE")) || 10
const query = reactive({ pageNum: 1, pageSize: DEFAULT_PAGE_SIZE, keyword: "", status: null })

const headerStyle = { backgroundColor: "#f7f8fa", color: "#333", fontWeight: 600 }

function formatTime(t) {
  if (!t) return ""
  return String(t).replace("T", " ")
}

function loadData() {
  loading.value = true
  getAdminList(query)
      .then(res => {
        list.value = res.data?.records || []
        total.value = res.data?.total || 0
      })
      .finally(() => (loading.value = false))
}

function resetQuery() {
  query.keyword = ""
  query.status = null
  query.pageNum = 1
  loadData()
}
function pageChange(page) {
  query.pageNum = page
  loadData()
}
function sizeChange(size) {
  query.pageSize = size
  query.pageNum = 1
  localStorage.setItem("ADMIN_PAGE_SIZE", String(size))
  loadData()
}

function handleSelectionChange(rows) {
  selectedIds.value = rows.map(r => r.id)
}

function changeStatus(id, status) {
  updateAdminStatus(id, status).then(() => ElMessage.success("状态更新成功"))
}

function handleDelete(id) {
  ElMessageBox.confirm("确定删除该管理员吗？", "提示", { type: "warning" })
      .then(() => deleteAdmins([id]))
      .then(() => {
        ElMessage.success("删除成功")
        loadData()
      })
}

function handleBatchDelete() {
  ElMessageBox.confirm("确定批量删除选中的管理员吗？", "提示", { type: "warning" })
      .then(() => deleteAdmins(selectedIds.value))
      .then(() => {
        ElMessage.success("删除成功")
        loadData()
      })
}

const dialogVisible = ref(false)
const dialogTitle = ref("新增管理员")
const form = reactive({
  id: null,
  username: "",
  password: "",
  nickname: "",
  avatarUrl: "",   // ✅ 一定要加
  status: 1
})

function openDialog(row) {
  dialogVisible.value = true
  if (row?.id) {
    dialogTitle.value = "编辑管理员"
    Object.assign(form, {
      id: row.id,
      username: row.username,
      password: "",
      nickname: row.nickname || "",
      avatarUrl: row.avatarUrl || "",   // ✅ 回填
      status: row.status ?? 1
    })
  } else {
    dialogTitle.value = "新增管理员"
    Object.assign(form, {
      id: null,
      username: "",
      password: "",
      nickname: "",
      avatarUrl: "",    // ✅ 清空
      status: 1
    })
  }
}

function submitForm() {
  const api = form.id ? updateAdmin : addAdmin
  api(form).then(() => {
    ElMessage.success("保存成功")
    dialogVisible.value = false
    loadData()
  })
}

async function handleUploadAvatar({ file }) {
  const res = await uploadImage(file)
  form.avatarUrl = res.data   // ✅ 这里是完整URL：http://localhost:8080/uploads/images/xxx.jpg
  ElMessage.success("上传成功")
}



onMounted(loadData)
</script>

<style scoped>
.page { display:flex; flex-direction:column; gap:14px; }
.panel { background:#fff; border-radius:8px; padding:16px; box-shadow:0 2px 8px rgba(0,0,0,.05); }
.query-form { margin-bottom:-12px; }
.toolbar { background:#fff; border-radius:8px; padding:14px 16px; display:flex; justify-content:space-between; align-items:center; box-shadow:0 2px 8px rgba(0,0,0,.05); }
.toolbar-left { display:flex; gap:10px; }
.toolbar-right { font-size:14px; color:#666; }
.total { font-weight:bold; color:#409eff; }
.pager { margin-top:16px; display:flex; justify-content:flex-end; }


/* 让 el-upload 的点击区域就是圆 */
.avatar-uploader {
  display: inline-flex;
  align-items: center;
}

/* 虚线圆容器 */
.avatar-upload-placeholder {
  width: 110px;        /* 你截图里大概这个尺寸 */
  height: 110px;
  border-radius: 50%;
  border: 2px dashed #d6d6d6;
  background: transparent;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all .2s ease;
}
/* hover 更“丝滑”一点 */
.avatar-upload-placeholder:hover {
  border-color: #409eff;
  background: rgba(64, 158, 255, 0.06);
}

/* 加号大小和颜色 */
.avatar-plus {
  font-size: 38px;
  color: #8c8c8c;
}

/* 预览图：圆形、同尺寸 */
.avatar-preview {
  width: 110px;
  height: 110px;
  border-radius: 50%;
  overflow: hidden;
  display: block;
}
</style>
