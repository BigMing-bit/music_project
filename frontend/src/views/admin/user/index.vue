<template>
  <div class="page">
    <!-- 查询栏 -->
    <div class="panel">
      <el-form :inline="true" :model="query" class="query-form">
        <el-form-item label="关键词">
          <el-input v-model="query.keyword" placeholder="用户名/昵称/手机号/邮箱" clearable style="width: 260px" />
        </el-form-item>

        <el-form-item label="性别">
          <el-select v-model="query.gender" placeholder="全部" clearable style="width: 120px">
            <el-option label="男" :value="1" />
            <el-option label="女" :value="2" />
            <el-option label="未知" :value="0" />
          </el-select>
        </el-form-item>

        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 120px">
            <el-option label="正常" :value="1" />
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
        <el-button type="danger" :disabled="selectedIds.length === 0" @click="handleBatchDelete">
          批量删除
        </el-button>
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

        <el-table-column prop="username" label="用户名" min-width="150" align="center" />
        <el-table-column prop="nickname" label="昵称" min-width="150" align="center" />

        <el-table-column label="头像" width="110" align="center">
          <template #default="{ row }">
            <el-image
                v-if="row.avatarUrl"
                :src="row.avatarUrl"
                style="width: 44px; height: 44px; border-radius: 50%;"
                fit="cover"
                preview-teleported
            />
            <span v-else style="color:#999;">无</span>
          </template>
        </el-table-column>

        <el-table-column label="性别" width="90" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.gender === 1">男</el-tag>
            <el-tag v-else-if="row.gender === 2" type="danger">女</el-tag>
            <el-tag v-else type="info">未知</el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="phone" label="手机号" width="140" align="center" />
        <el-table-column prop="email" label="邮箱" min-width="180" align="center" />

        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? "正常" : "禁用" }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="创建时间" width="180" align="center">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>

        <el-table-column label="操作" width="180" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" @click="openView(row)">查看</el-button>
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

    <!-- 查看弹窗（只读） -->
    <el-dialog v-model="dialogVisible" title="用户详情" width="620px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="用户名">
          <el-input v-model="form.username" disabled />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="form.nickname" disabled />
        </el-form-item>
        <el-form-item label="角色">
          <el-input :model-value="roleText(form.role)" disabled />
        </el-form-item>

        <el-form-item label="头像">
          <el-image
              v-if="form.avatarUrl"
              :src="form.avatarUrl"
              style="width: 64px; height: 64px; border-radius: 50%;"
              fit="cover"
              preview-teleported
          />
          <span v-else style="color:#999;">无</span>
        </el-form-item>

        <el-form-item label="手机号">
          <el-input v-model="form.phone" disabled />
        </el-form-item>

        <el-form-item label="邮箱">
          <el-input v-model="form.email" disabled />
        </el-form-item>

        <el-form-item label="性别">
          <el-input :model-value="genderText(form.gender)" disabled />
        </el-form-item>

        <el-form-item label="状态">
          <el-input :model-value="form.status === 1 ? '正常' : '禁用'" disabled />
        </el-form-item>

        <el-form-item label="创建时间">
          <el-input :model-value="formatTime(form.createTime)" disabled />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from "vue"
import { ElMessage, ElMessageBox } from "element-plus"
import { getUserList, deleteUsers} from "@/api/admin/user"

const loading = ref(false)
const list = ref([])
const total = ref(0)
const selectedIds = ref([])

const DEFAULT_PAGE_SIZE = Number(localStorage.getItem("USER_PAGE_SIZE")) || 10
const query = reactive({ pageNum: 1, pageSize: DEFAULT_PAGE_SIZE, keyword: "", gender: null, status: null })

const headerStyle = { backgroundColor: "#f7f8fa", color: "#333", fontWeight: 600 }

function formatTime(t) {
  if (!t) return ""
  return String(t).replace("T", " ")
}
function genderText(g) {
  if (g === 1) return "男"
  if (g === 2) return "女"
  return "未知"
}

function roleText(r) {
  if (r === 0) return "普通用户"
  if (r === 1) return "官方用户"
  return "未知"
}

function loadData() {
  loading.value = true
  getUserList(query)
      .then(res => {
        list.value = res.data?.records || []
        total.value = res.data?.total || 0
      })
      .finally(() => (loading.value = false))
}

function resetQuery() {
  query.keyword = ""
  query.gender = null
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
  localStorage.setItem("USER_PAGE_SIZE", String(size))
  loadData()
}

function handleSelectionChange(rows) {
  selectedIds.value = rows.map(r => r.id)
}

function handleDelete(id) {
  ElMessageBox.confirm("确定删除该用户吗？（不可恢复）", "提示", { type: "warning" })
      .then(() => deleteUsers([id]))
      .then(() => {
        ElMessage.success("删除成功")
        loadData()
      })
}

function handleBatchDelete() {
  ElMessageBox.confirm("确定批量删除选中的用户吗？（不可恢复）", "提示", { type: "warning" })
      .then(() => deleteUsers(selectedIds.value))
      .then(() => {
        ElMessage.success("删除成功")
        loadData()
      })
}

const dialogVisible = ref(false)
const form = reactive({})

function openView(row) {
  dialogVisible.value = true
  Object.assign(form, row)
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
</style>
