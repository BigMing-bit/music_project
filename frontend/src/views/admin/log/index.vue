<template>
  <div class="page">
    <!-- 查询栏 -->
    <div class="panel">
      <el-form :inline="true" :model="query" class="query-form">
        <el-form-item label="关键词">
          <el-input
              v-model="query.keyword"
              placeholder="管理员/模块/动作/path/ip"
              clearable
              style="width: 240px"
              @keyup.enter="loadData"
          />
        </el-form-item>

        <el-form-item label="结果">
          <el-select v-model="query.success" placeholder="全部" clearable style="width: 120px">
            <el-option label="成功" :value="1" />
            <el-option label="失败" :value="0" />
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
        <el-button
            type="danger"
            :disabled="selectedIds.length === 0"
            @click="handleBatchDelete"
        >
          批量删除
        </el-button>

        <el-button type="warning" @click="handleClearAll">
          清空全部日志
        </el-button>
      </div>

      <div class="toolbar-right">
        共 <span class="total">{{ total }}</span> 条
      </div>
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

        <el-table-column prop="adminUsername" label="管理员" width="140" align="center" />

        <el-table-column prop="module" label="模块" width="140" align="center" show-overflow-tooltip />

        <el-table-column prop="action" label="动作" width="160" align="center" show-overflow-tooltip />

        <el-table-column prop="method" label="方法" width="90" align="center">
          <template #default="{ row }">
            <el-tag size="small" type="info">{{ row.method }}</el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="path" label="路径" min-width="260" align="center" show-overflow-tooltip />

        <el-table-column label="IP" width="140">
          <template #default="{ row }">
            {{ normalizeIp(row.ip) }}
          </template>
        </el-table-column>

        <el-table-column label="结果" width="110" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.success === 1" type="success">成功</el-tag>
            <el-tag v-else type="danger">失败</el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="createTime" label="时间" width="180" align="center">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>

        <el-table-column label="操作" width="180" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" @click="openDetail(row.id)">查看</el-button>
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

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="日志详情" width="780px">
      <div class="detail-wrap" v-loading="detailLoading">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="管理员">{{ detail.adminUsername }}</el-descriptions-item>
          <el-descriptions-item label="管理员ID">{{ detail.adminId }}</el-descriptions-item>

          <el-descriptions-item label="模块">{{ detail.module }}</el-descriptions-item>
          <el-descriptions-item label="动作">{{ detail.action }}</el-descriptions-item>

          <el-descriptions-item label="方法">{{ detail.method }}</el-descriptions-item>
          <el-descriptions-item label="IP">{{normalizeIp(detail.ip)}}</el-descriptions-item>

          <el-descriptions-item label="路径" :span="2">{{ detail.path }}</el-descriptions-item>

          <el-descriptions-item label="结果">
            <el-tag v-if="detail.success === 1" type="success">成功</el-tag>
            <el-tag v-else type="danger">失败</el-tag>
          </el-descriptions-item>

          <el-descriptions-item label="时间">{{ formatTime(detail.createTime) }}</el-descriptions-item>
        </el-descriptions>

        <div class="detail-block">
          <div class="detail-title">请求参数（params）</div>
          <el-input
              type="textarea"
              :rows="6"
              v-model="detail.params"
              readonly
              placeholder="无"
          />
        </div>

        <div v-if="detail.success === 0" class="detail-block">
          <div class="detail-title">错误信息（errorMsg）</div>
          <el-input
              type="textarea"
              :rows="4"
              v-model="detail.errorMsg"
              readonly
              placeholder="无"
          />
        </div>
      </div>

      <template #footer>
        <el-button @click="detailVisible=false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from "vue"
import { ElMessage, ElMessageBox } from "element-plus"
import { getLogList, getLogInfo, deleteLogs, clearAllLogs } from "@/api/admin/log"

const loading = ref(false)
const list = ref([])
const total = ref(0)
const selectedIds = ref([])

const DEFAULT_PAGE_SIZE = Number(localStorage.getItem("LOG_PAGE_SIZE")) || 10

const query = reactive({
  pageNum: 1,
  pageSize: DEFAULT_PAGE_SIZE,
  keyword: "",
  success: null
})

const headerStyle = {
  backgroundColor: "#f7f8fa",
  color: "#333",
  fontWeight: 600
}

function formatTime(t) {
  if (!t) return ""
  return String(t).replace("T", " ")
}

function loadData() {
  loading.value = true
  getLogList(query)
      .then(res => {
        list.value = res.data?.records || []
        total.value = res.data?.total || 0
      })
      .finally(() => (loading.value = false))
}

function resetQuery() {
  query.keyword = ""
  query.success = null
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
  localStorage.setItem("LOG_PAGE_SIZE", String(size))
  loadData()
}

function handleSelectionChange(rows) {
  selectedIds.value = rows.map(r => r.id)
}

function handleDelete(id) {
  ElMessageBox.confirm("确定删除该日志吗？", "提示", { type: "warning" })
      .then(() => deleteLogs([id]))
      .then(() => {
        ElMessage.success("删除成功")
        loadData()
      })
}

function handleBatchDelete() {
  ElMessageBox.confirm("确定批量删除选中的日志吗？", "提示", { type: "warning" })
      .then(() => deleteLogs(selectedIds.value))
      .then(() => {
        ElMessage.success("删除成功")
        loadData()
      })
}

function handleClearAll() {
  ElMessageBox.confirm(
      "⚠️ 确定要清空全部日志吗？此操作不可恢复！",
      "高危操作确认",
      { type: "warning", confirmButtonText: "确定清空", cancelButtonText: "取消" }
  )
      .then(() => clearAllLogs())
      .then(() => {
        ElMessage.success("已清空全部日志")
        query.pageNum = 1
        selectedIds.value = []
        loadData()
      })
}

const detailVisible = ref(false)
const detailLoading = ref(false)
const detail = reactive({
  id: null,
  adminId: null,
  adminUsername: "",
  module: "",
  action: "",
  method: "",
  path: "",
  params: "",
  ip: "",
  success: 1,
  errorMsg: "",
  createTime: ""
})

async function openDetail(id) {
  detailVisible.value = true
  detailLoading.value = true
  try {
    const res = await getLogInfo(id)
    const d = res.data || {}
    Object.assign(detail, {
      id: d.id,
      adminId: d.adminId,
      adminUsername: d.adminUsername || "",
      module: d.module || "",
      action: d.action || "",
      method: d.method || "",
      path: d.path || "",
      params: d.params || "",
      ip: d.ip || "",
      success: d.success ?? 1,
      errorMsg: d.errorMsg || "",
      createTime: d.createTime || ""
    })
  } finally {
    detailLoading.value = false
  }
}


function normalizeIp(ip) {
  if (!ip) return ""
  if (ip === "0:0:0:0:0:0:0:1" || ip === "::1") return "127.0.0.1"
  if (String(ip).startsWith("::ffff:")) return String(ip).substring(7)
  return ip
}


onMounted(loadData)
</script>

<style scoped>
.page {
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.panel {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}
.query-form {
  margin-bottom: -12px;
}
.toolbar {
  background: #fff;
  border-radius: 8px;
  padding: 14px 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}
.toolbar-left {
  display: flex;
  gap: 10px;
}
.toolbar-right {
  font-size: 14px;
  color: #666;
}
.total {
  font-weight: bold;
  color: #409eff;
}
.pager {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.detail-wrap {
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.detail-block {
  margin-top: 10px;
}
.detail-title {
  font-weight: 700;
  margin-bottom: 8px;
}
</style>
