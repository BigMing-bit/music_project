<template>
  <div class="page">
    <!-- 查询 -->
    <div class="panel">
      <el-form :inline="true" :model="query">
        <el-form-item label="关键词">
          <el-input v-model="query.keyword" placeholder="MV名/歌手名" clearable style="width:220px" />
        </el-form-item>


        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width:120px">
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
        <el-button type="primary" @click="openDialog()">新增MV</el-button>
        <el-button type="danger" :disabled="selectedIds.length===0" @click="handleBatchDelete">批量删除</el-button>
      </div>
      <div class="toolbar-right">共 <span class="total">{{ total }}</span> 条</div>
    </div>

    <!-- 表格 -->
    <div class="panel">
      <el-table
          :data="list"
          stripe border
          v-loading="loading"
          @selection-change="handleSelectionChange"
          :header-cell-style="headerStyle"
      >
        <el-table-column type="selection" width="50" />
        <el-table-column prop="mvName" label="MV名称" align="center" min-width="220" />
        <el-table-column prop="singerName" label="歌手" align="center" width="160" />
        <el-table-column label="封面" width="120" align="center">
          <template #default="{ row }">
            <el-image
                v-if="row.coverUrl"
                :src="row.coverUrl"
                style="width:80px;height:45px"
                fit="cover"
                preview-teleported
            />
            <span v-else style="color:#999">无</span>
          </template>
        </el-table-column>

        <el-table-column prop="durationSeconds" label="时长" width="120" align="center">
          <template #default="{ row }">{{ formatDuration(row.durationSeconds) }}</template>
        </el-table-column>
        <el-table-column prop="publishDate" label="发布日期" width="130" align="center" />

        <el-table-column prop="status" label="状态" width="120" align="center">
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

        <el-table-column label="操作" width="200" fixed="right" align="center">
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
        <el-form-item label="MV名称" required>
          <el-input v-model="form.mvName" placeholder="请输入MV名称" />
        </el-form-item>

        <el-form-item label="歌手">
          <el-select
              v-model="form.singerId"
              filterable
              remote
              reserve-keyword
              placeholder="请输入歌手名搜索"
              :remote-method="remoteSearchSinger"
              :loading="singerLoading"
              style="width: 100%"
          >
            <el-option
                v-for="item in singerOptions"
                :key="item.id"
                :label="item.label"
                :value="item.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="封面">
          <el-upload
              :show-file-list="false"
              :http-request="handleUploadCoverUrl"
              accept="image/*"
          >
            <el-button>选择图片</el-button>
          </el-upload>

          <div style="margin-left: 12px;">
            <el-image
                v-if="form.coverUrl"
                :src="form.coverUrl"
                style="width: 100px; height: 48px;"
                fit="cover"
                preview-teleported
            />
            <span v-else style="color:#999;">未上传</span>
          </div>
        </el-form-item>

        <el-form-item label="视频URL" required>
          <el-input v-model="form.videoUrl" placeholder="http(s)://..." />
        </el-form-item>

        <el-form-item label="时长(秒)">
          <el-input-number v-model="form.durationSeconds" :min="0" style="width: 220px" />
        </el-form-item>

        <el-form-item label="发布日期">
          <el-date-picker
              v-model="form.publishDate"
              type="date"
              value-format="YYYY-MM-DD"
              format="YYYY-MM-DD"
              placeholder="选择日期"
              style="width: 220px"
              clearable
          />
        </el-form-item>

        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :min="0" :max="99999" style="width: 220px" />
        </el-form-item>

        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
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
import { reactive, ref, onMounted } from "vue"
import { ElMessage, ElMessageBox } from "element-plus"
import { getMvList, getMvInfo, addMv, updateMv, deleteMvs, updateMvStatus } from "@/api/admin/mv"
import {uploadImage} from "@/api/upload.js";
import {selectSingers} from "@/api/admin/singer.js";

const loading = ref(false)
const list = ref([])
const total = ref(0)
const selectedIds = ref([])

const DEFAULT_PAGE_SIZE = Number(localStorage.getItem("MV_PAGE_SIZE")) || 10
const query = reactive({ pageNum: 1, pageSize: DEFAULT_PAGE_SIZE, keyword: "", singerId: null, status: null })
const headerStyle = { backgroundColor: "#f7f8fa", color: "#333", fontWeight: 600 }

function formatTime(t) { return t ? String(t).replace("T", " ") : "" }
function formatDuration(sec) {
  if (!sec && sec !== 0) return ""
  const m = Math.floor(sec / 60)
  const s = sec % 60
  return `${m}:${String(s).padStart(2, "0")}`
}

function loadData() {
  loading.value = true
  const params = { ...query, singerId: query.singerId ? Number(query.singerId) : null }
  getMvList(params).then(res => {
    list.value = res.data?.records || []
    total.value = res.data?.total || 0
  }).finally(() => loading.value = false)
}

function resetQuery() {
  query.keyword = ""
  query.singerId = null
  query.status = null
  query.pageNum = 1
  loadData()
}

function pageChange(p) { query.pageNum = p; loadData() }
function sizeChange(s) {
  query.pageSize = s
  query.pageNum = 1
  localStorage.setItem("MV_PAGE_SIZE", String(s))
  loadData()
}

function handleSelectionChange(rows) { selectedIds.value = rows.map(r => r.id) }

function changeStatus(id, status) {
  updateMvStatus(id, status).then(() => ElMessage.success("状态更新成功"))
}

function handleDelete(id) {
  ElMessageBox.confirm("确定删除该MV吗？", "提示", { type: "warning" })
      .then(() => deleteMvs([id]))
      .then(() => { ElMessage.success("删除成功"); loadData() })
}

function handleBatchDelete() {
  ElMessageBox.confirm("确定批量删除选中的MV吗？", "提示", { type: "warning" })
      .then(() => deleteMvs(selectedIds.value))
      .then(() => { ElMessage.success("删除成功"); loadData() })
}

const dialogVisible = ref(false)
const dialogTitle = ref("新增MV")

const form = reactive({
  id: null,
  mvName: "",
  singerId: null,
  coverUrl: "",
  videoUrl: "",
  durationSeconds: 0,
  playCount: 0,
  publishDate: null,
  sort: 0,
  status: 1
})

function resetForm() {
  Object.assign(form, {
    id: null, mvName: "", singerId: null, coverUrl: "", videoUrl: "",
    durationSeconds: 0, playCount: 0, publishDate: null, sort: 0, status: 1
  })
}

async function openDialog(row) {
  dialogVisible.value = true
  if (row?.id) {
    dialogTitle.value = "编辑MV"
    const res = await getMvInfo(row.id)
    const d = res.data || {}
    Object.assign(form, {
      id: d.id,
      mvName: d.mvName || "",
      singerId: d.singerId ?? null,
      coverUrl: d.coverUrl || "",
      videoUrl: d.videoUrl || "",
      durationSeconds: d.durationSeconds ?? 0,
      playCount: d.playCount ?? 0,
      publishDate: d.publishDate || null,
      sort: d.sort ?? 0,
      status: d.status ?? 1
    })
  } else {
    dialogTitle.value = "新增MV"
    resetForm()
  }
}

async function submitForm() {
  if (!form.mvName) return ElMessage.error("MV名称不能为空")
  if (!form.singerId) return ElMessage.error("歌手ID不能为空")

  const api = form.id ? updateMv : addMv
  await api({ ...form })
  ElMessage.success("保存成功")
  dialogVisible.value = false
  loadData()
}


const singerLoading = ref(false)
const singerOptions = ref([])

async function remoteSearchSinger(keyword) {
  singerLoading.value = true
  try {
    const res = await selectSingers(keyword)
    singerOptions.value = res.data || []
  } finally {
    singerLoading.value = false
  }
}

async function handleUploadCoverUrl({ file }) {
  const res = await uploadImage(file)
  form.coverUrl = res.data   // ✅ 这里是完整URL：http://localhost:8080/uploads/images/xxx.jpg
  ElMessage.success("上传成功")
}

onMounted(loadData)
</script>

<style scoped>
.page { display:flex; flex-direction:column; gap:14px; }
.panel { background:#fff; border-radius:8px; padding:16px; box-shadow:0 2px 8px rgba(0,0,0,.05); }
.toolbar { background:#fff; border-radius:8px; padding:14px 16px; display:flex; justify-content:space-between; align-items:center; box-shadow:0 2px 8px rgba(0,0,0,.05); }
.toolbar-left { display:flex; gap:10px; }
.toolbar-right { font-size:14px; color:#666; }
.total { font-weight:bold; color:#409eff; }
.pager { margin-top:16px; display:flex; justify-content:flex-end; }
</style>
