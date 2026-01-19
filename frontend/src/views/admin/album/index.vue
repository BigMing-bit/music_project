<template>
  <div class="page">
    <!-- ✅ 查询栏 -->
    <div class="panel">
      <el-form :inline="true" :model="query" class="query-form">
        <el-form-item label="关键词">
          <el-input v-model="query.keyword" placeholder="请输入专辑名" clearable style="width: 200px" />
        </el-form-item>

        <el-form-item label="歌手">
          <el-select
              v-model="query.singerId"
              filterable
              remote
              clearable
              reserve-keyword
              placeholder="输入歌手名搜索"
              :remote-method="remoteSearchSinger"
              :loading="singerLoading"
              style="width: 200px"
          >
            <el-option
                v-for="item in singerOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 120px">
            <el-option label="上架" :value="1" />
            <el-option label="下架" :value="0" />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- ✅ 工具栏 -->
    <div class="toolbar">
      <div class="toolbar-left">
        <el-button type="primary" @click="openDialog()">新增专辑</el-button>
        <el-button type="danger" :disabled="selectedIds.length === 0" @click="handleBatchDelete">
          批量删除
        </el-button>
      </div>

      <div class="toolbar-right">
        共 <span class="total">{{ total }}</span> 条
      </div>
    </div>

    <!-- ✅ 表格 -->
    <div class="panel">
      <el-table
          :data="list"
          stripe
          border
          v-loading="loading"
          element-loading-text="加载中..."
          @selection-change="handleSelectionChange"
          :header-cell-style="headerStyle"
          :default-sort="{ prop: 'id', order: 'ascending' }"
      >
        <el-table-column type="selection" width="50" />
        <el-table-column prop="id" label="ID" align="center" sortable />
        <el-table-column prop="albumName" label="专辑名" align="center" min-width="220" />
        <el-table-column label="出版日期" align="center" min-width="160">
          <template #default="{ row }">
            {{ formatDate(row.publishDate) }}
          </template>
        </el-table-column>

        <el-table-column prop="singerName" label="歌手" align="center" width="180" />

        <el-table-column prop="coverUrl" label="封面" style="text-align: center" align="center" width="100" >
          <template #default="{ row }">
            <el-image
                v-if="row.coverUrl"
                :src="row.coverUrl"
                style="width: 65px; height: 60px;"
                fit="cover"
                preview-teleported
            />

            <span v-else style="color:#999;">无</span>
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

        <el-table-column label="创建时间" width="180" align="center">
          <template #default="{ row }">
            {{ (row.createTime || "").replace("T", " ") }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary"  @click="openDialog(row)">编辑</el-button>
            <el-button type="danger"  @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- ✅ 分页 -->
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

    <!-- ✅ 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="560px">

      <el-form :model="form" label-width="90px">
        <el-form-item label="专辑名" required>
          <el-input v-model="form.albumName" placeholder="请输入专辑名" />
        </el-form-item>
        <el-form-item label="出版日期" required>
          <el-date-picker
              v-model="form.publishDate"
              type="date"
              value-format="YYYY-MM-DD"
              format="YYYY-MM-DD"
          />

        </el-form-item>

        <el-form-item label="歌手" required>
          <el-select
              v-model="form.singerId"
              filterable
              remote
              reserve-keyword
              placeholder="输入歌手名搜索"
              :remote-method="remoteSearchSinger"
              :loading="singerLoading"
              style="width: 100%"
          >
            <el-option
                v-for="item in singerOptions"
                :key="item.value"
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

        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">上架</el-radio>
            <el-radio :value="0">下架</el-radio>
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
import {
  getAlbumList,
  addAlbum,
  updateAlbum,
  updateAlbumStatus,
  deleteAlbums,
  getAlbumInfo
} from "@/api/admin/album"
import { selectSingers } from "@/api/admin/singer"
import {uploadImage} from "@/api/upload.js";

const loading = ref(false)
const list = ref([])
const total = ref(0)
const selectedIds = ref([])

const DEFAULT_PAGE_SIZE = Number(localStorage.getItem("ALBUM_PAGE_SIZE")) || 10

const query = reactive({
  pageNum: 1,
  pageSize: DEFAULT_PAGE_SIZE,
  keyword: "",
  status: null,
  singerId: null,
})

const singerOptions = ref([])
const singerLoading = ref(false)

const headerStyle = {
  backgroundColor: "#f7f8fa",
  color: "#333",
  fontWeight: 600
}

function loadData() {
  loading.value = true
  getAlbumList(query)
      .then(res => {
        list.value = res.data?.records || []
        total.value = res.data?.total || 0
      })
      .finally(() => (loading.value = false))
}

function formatDate(v) {
  if (!v) return ""
  return String(v).slice(0, 10)
}



function resetQuery() {
  query.keyword = ""
  query.status = null
  query.singerId = null
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
  localStorage.setItem("ALBUM_PAGE_SIZE", String(size))
  loadData()
}

function handleSelectionChange(rows) {
  selectedIds.value = rows.map(r => r.id)
}

function changeStatus(id, status) {
  updateAlbumStatus(id, status).then(() => ElMessage.success("状态更新成功"))
}

function handleDelete(id) {
  ElMessageBox.confirm("确定删除该专辑吗？", "提示", { type: "warning" })
      .then(() => deleteAlbums([id]))
      .then(() => {
        ElMessage.success("删除成功")
        loadData()
      })
}

function handleBatchDelete() {
  ElMessageBox.confirm("确定批量删除选中的专辑吗？", "提示", { type: "warning" })
      .then(() => deleteAlbums(selectedIds.value))
      .then(() => {
        ElMessage.success("删除成功")
        loadData()
      })
}

// ✅ 远程搜索歌手（用于查询栏 + 弹窗）
async function remoteSearchSinger(keyword) {
  singerLoading.value = true
  try {
    const res = await selectSingers(keyword)
    singerOptions.value = res.data || []
  } finally {
    singerLoading.value = false
  }
}

const dialogVisible = ref(false)
const dialogTitle = ref("新增专辑")

const form = reactive({
  id: null,
  albumName: "",
  singerId: null,
  coverUrl: "",
  publishDate: "",   // ✅ 加这一行
  description: "",
  status: 1,
})

async function openDialog(row) {
  dialogVisible.value = true



  if (row?.id) {
    dialogTitle.value = "编辑专辑"

    // ✅ 拉详情，避免列表字段不全（你也可以直接 Object.assign(form,row)）
    const res = await getAlbumInfo(row.id)
    const d = res.data || {}

    console.log("publishDate raw:", d.publishDate, typeof d.publishDate)

    Object.assign(form, {
      id: d.id,
      albumName: d.albumName || row.albumName,
      singerId: d.singerId || row.singerId,
      publishDate: d.publishDate ? String(d.publishDate).slice(0, 10) : "",
      coverUrl: d.coverUrl || "",
      description: d.description || "",
      status: d.status ?? 1,
    })

    // ✅ 让 singer 下拉能回显 label：把当前 singer 放进 options（最稳）
    if (form.singerId) {
      await remoteSearchSinger("") // 拉一批
      // 如果你的 selectSingers("") 返回不包含当前 singer（例如数据很多），就再按关键字拉一次：
      // await remoteSearchSinger(row.singerName || "")
    }
  } else {
    dialogTitle.value = "新增专辑"
    Object.assign(form, {
      id: null,
      albumName: "",
      singerId: null,
      publishDate: "",
      coverUrl: "",
      description: "",
      status: 1,
    })
  }
}

function submitForm() {
  const api = form.id ? updateAlbum : addAlbum
  api(form).then(() => {
    ElMessage.success("保存成功")
    dialogVisible.value = false
    loadData()
  })
}

async function handleUploadCoverUrl({ file }) {
  const res = await uploadImage(file)
  form.coverUrl = res.data   // ✅ 这里是完整URL：http://localhost:8080/uploads/images/xxx.jpg
  ElMessage.success("上传成功")
}


onMounted(loadData)
</script>

<style scoped>
.page {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

/* ✅ 后台常见白底面板 */
.panel {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

/* ✅ 查询栏紧凑 */
.query-form {
  margin-bottom: -12px;
}

/* ✅ 工具栏（按钮那一条） */
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

/* ✅ 分页 */
.pager {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
