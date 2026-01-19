<template>
  <div class="page">
    <!-- 查询栏 -->
    <div class="panel">
      <el-form :inline="true" :model="query" class="query-form">
        <el-form-item label="关键词">
          <el-input v-model="query.keyword" placeholder="请输入公告标题" clearable style="width: 220px" />
        </el-form-item>
        <el-form-item label="可见">
          <el-select v-model="query.visible" placeholder="全部" clearable style="width: 120px">
            <el-option label="可见" :value="1" />
            <el-option label="隐藏" :value="0" />
          </el-select>
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
        <el-button type="primary" @click="openDialog()">新增公告</el-button>
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
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="title" label="标题" align="center" min-width="260" />

        <el-table-column prop="publisher" label="发布人" width="120" align="center" />

        <el-table-column label="封面" width="110" align="center">
          <template #default="{ row }">
            <el-image
                v-if="row.coverUrl"
                :src="row.coverUrl"
                style="width: 70px; height: 42px;"
                fit="cover"
                preview-teleported
            />
            <span v-else style="color:#999;">无</span>
          </template>
        </el-table-column>

        <el-table-column prop="visible" label="可见" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.visible === 1" type="success">可见</el-tag>
            <el-tag v-else type="info">隐藏</el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="status" label="状态" width="110" align="center">
          <template #default="{ row }">
            <el-switch
                v-model="row.status"
                :active-value="1"
                :inactive-value="0"
                @change="val => changeStatus(row.id, val)"
            />
          </template>
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
            :page-sizes="[10,20,50,100]"
            :current-page="query.pageNum"
            @current-change="pageChange"
            @size-change="sizeChange"
        />
      </div>
    </div>

    <!-- 弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="720px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="标题" required>
          <el-input v-model="form.title" placeholder="请输入公告标题" />
        </el-form-item>

        <el-form-item label="发布人">
          <el-input v-model="form.publisher" placeholder="如：Harmony 官方" />
        </el-form-item>

        <el-form-item label="展示时间">
          <div style="display:flex; gap:12px; width:100%;">
            <el-date-picker
                v-model="form.startTime"
                type="datetime"
                placeholder="开始时间（可空）"
                value-format="YYYY-MM-DD HH:mm:ss"
                format="YYYY-MM-DD HH:mm:ss"
                style="flex:1;"
                clearable
            />
            <el-date-picker
                v-model="form.endTime"
                type="datetime"
                placeholder="结束时间（可空）"
                value-format="YYYY-MM-DD HH:mm:ss"
                format="YYYY-MM-DD HH:mm:ss"
                style="flex:1;"
                clearable
            />
          </div>
        </el-form-item>


        <el-form-item label="封面">
          <el-upload :show-file-list="false" :http-request="handleUploadCover" accept="image/*">
            <el-button>选择图片</el-button>
          </el-upload>
          <div style="margin-left:12px;">
            <el-image v-if="form.coverUrl" :src="form.coverUrl" style="width:120px;height:60px;" fit="cover" preview-teleported />
            <span v-else style="color:#999;">未上传</span>
          </div>
        </el-form-item>

        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :min="0" :max="99999" />
        </el-form-item>

        <el-form-item label="可见">
          <el-radio-group v-model="form.visible">
            <el-radio :value="1">可见</el-radio>
            <el-radio :value="0">隐藏</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="内容" required>
          <el-input v-model="form.content" type="textarea" :rows="8" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible=false">取消</el-button>
        <el-button type="primary" @click="submitForm">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from "vue"
import { ElMessage, ElMessageBox } from "element-plus"
import { getNoticeList, addNotice, updateNotice, deleteNotices, updateNoticeStatus, getNoticeInfo } from "@/api/admin/notice"
import { uploadImage } from "@/api/upload"

const loading = ref(false)
const list = ref([])
const total = ref(0)
const selectedIds = ref([])

const DEFAULT_PAGE_SIZE = Number(localStorage.getItem("NOTICE_PAGE_SIZE")) || 10
const query = reactive({ pageNum: 1, pageSize: DEFAULT_PAGE_SIZE, keyword: "", status: null, visible: null })

const headerStyle = { backgroundColor: "#f7f8fa", color: "#333", fontWeight: 600 }

function formatTime(t) {
  if (!t) return ""
  return String(t).replace("T", " ")
}

function loadData() {
  loading.value = true
  getNoticeList(query)
      .then(res => {
        list.value = res.data?.records || []
        total.value = res.data?.total || 0
      })
      .finally(() => (loading.value = false))
}

function resetQuery() {
  query.keyword = ""
  query.status = null
  query.visible = null
  query.pageNum = 1
  loadData()
}

function pageChange(p) { query.pageNum = p; loadData() }
function sizeChange(s) { query.pageSize = s; query.pageNum = 1; localStorage.setItem("NOTICE_PAGE_SIZE", String(s)); loadData() }

function handleSelectionChange(rows) { selectedIds.value = rows.map(r => r.id) }

function changeStatus(id, status) {
  updateNoticeStatus(id, status).then(() => ElMessage.success("状态更新成功"))
}

function handleDelete(id) {
  ElMessageBox.confirm("确定删除该公告吗？", "提示", { type: "warning" })
      .then(() => deleteNotices([id]))
      .then(() => { ElMessage.success("删除成功"); loadData() })
}

function handleBatchDelete() {
  ElMessageBox.confirm("确定批量删除选中的公告吗？", "提示", { type: "warning" })
      .then(() => deleteNotices(selectedIds.value))
      .then(() => { ElMessage.success("删除成功"); loadData() })
}

const dialogVisible = ref(false)
const dialogTitle = ref("新增公告")

const form = reactive({
  id: null,
  title: "",
  content: "",
  coverUrl: "",
  publisher: "",
  startTime: null,
  endTime: null,
  sort: 0,
  visible: 1,
  status: 1
})


async function openDialog(row) {
  dialogVisible.value = true

  if (row?.id) {
    dialogTitle.value = "编辑公告"
    const res = await getNoticeInfo(row.id)
    const d = res.data || {}
    Object.assign(form, {
      id: d.id,
      title: d.title || "",
      content: d.content || "",
      coverUrl: d.coverUrl || "",
      publisher: d.publisher || "",
      startTime: d.startTime || null,
      endTime: d.endTime || null,
      sort: d.sort ?? 0,
      visible: d.visible ?? 1,
      status: d.status ?? 1
    })
  } else {
    dialogTitle.value = "新增公告"
    Object.assign(form, { id:null, title:"", content:"", coverUrl:"", publisher:"", startTime:null, endTime:null, sort:0, visible:1, status:1 })
  }
}

async function handleUploadCover({ file }) {
  const res = await uploadImage(file)
  form.coverUrl = res.data
  ElMessage.success("上传成功")
}

function submitForm() {

  const api = form.id ? updateNotice : addNotice
  api(form).then(() => {
    ElMessage.success("保存成功")
    dialogVisible.value = false
    loadData()
  })
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
