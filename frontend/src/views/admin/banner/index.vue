<template>
  <div class="page">
    <!-- 查询栏 -->
    <div class="panel">
      <el-form :inline="true" :model="query" class="query-form">
        <el-form-item label="关键词">
          <el-input v-model="query.keyword" placeholder="标题/跳转目标" clearable style="width: 220px" />
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
        <el-button type="primary" @click="openDialog()">新增轮播图</el-button>
        <el-button type="danger" :disabled="selectedIds.length === 0" @click="handleBatchDelete">批量删除</el-button>
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

        <el-table-column prop="title" label="标题" min-width="200" align="center" />

        <el-table-column prop="imageUrl" label="图片" width="120" align="center">
          <template #default="{ row }">
            <el-image
                v-if="row.imageUrl"
                :src="row.imageUrl"
                class="cover-square"
                fit="cover"
                preview-teleported
            />
            <span v-else class="empty">无</span>
          </template>
        </el-table-column>

        <el-table-column prop="jumpType" label="跳转类型" width="120" align="center">
          <template #default="{ row }">
            <el-tag type="info">{{ jumpTypeText(row.jumpType) }}</el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="jumpTarget" label="跳转目标" min-width="220" align="center" show-overflow-tooltip />

        <el-table-column prop="sort" label="排序" width="90" align="center" />

        <el-table-column label="状态" width="120" align="center">
          <template #default="{ row }">
            <el-switch v-model="row.status" :active-value="1" :inactive-value="0" @change="val => changeStatus(row.id, val)" />
          </template>
        </el-table-column>

        <el-table-column label="创建时间" width="180" align="center">
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
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="620px">
      <el-form :model="form" label-width="96px">
        <el-form-item label="标题" required>
          <el-input v-model="form.title" placeholder="请输入标题" />
        </el-form-item>

        <el-form-item label="图片" required>
          <el-upload
              class="uploader"
              :show-file-list="false"
              :http-request="handleUpload"
              accept="image/*"
          >
            <el-image
                v-if="form.imageUrl"
                :src="form.imageUrl"
                class="cover-square-lg"
                fit="cover"
                preview-teleported
            />
            <div v-else class="upload-plus">
              <el-icon><Plus /></el-icon>
            </div>
          </el-upload>

          <el-button v-if="form.imageUrl" type="danger" link style="margin-left:10px" @click="form.imageUrl = ''">删除</el-button>
        </el-form-item>

        <el-form-item label="跳转类型">
          <el-select v-model="form.jumpType" style="width: 180px">
            <el-option label="无跳转" :value="0" />
            <el-option label="URL" :value="1" />
            <el-option label="歌单ID" :value="2" />
            <el-option label="专辑ID" :value="3" />
            <el-option label="歌曲ID" :value="4" />
          </el-select>
        </el-form-item>

        <el-form-item label="跳转目标">
          <el-input v-model="form.jumpTarget" placeholder="无跳转可留空" />
        </el-form-item>

        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :min="0" :max="99999" />
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
import { Plus } from "@element-plus/icons-vue"
import { getBannerList, addBanner, updateBanner, deleteBanners, updateBannerStatus } from "@/api/admin/banner"
import { uploadImage } from "@/api/upload" // 你现有的上传接口

const loading = ref(false)
const list = ref([])
const total = ref(0)
const selectedIds = ref([])

const DEFAULT_PAGE_SIZE = Number(localStorage.getItem("BANNER_PAGE_SIZE")) || 10

const query = reactive({
  pageNum: 1,
  pageSize: DEFAULT_PAGE_SIZE,
  keyword: "",
  status: null
})

const headerStyle = { backgroundColor: "#f7f8fa", color: "#333", fontWeight: 600 }

function formatTime(t) {
  if (!t) return ""
  return String(t).replace("T", " ")
}
function jumpTypeText(v) {
  if (v === 1) return "URL"
  if (v === 2) return "歌单"
  if (v === 3) return "专辑"
  if (v === 4) return "歌曲"
  return "无"
}

function loadData() {
  loading.value = true
  getBannerList(query)
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
function pageChange(p) { query.pageNum = p; loadData() }
function sizeChange(s) {
  query.pageSize = s
  query.pageNum = 1
  localStorage.setItem("BANNER_PAGE_SIZE", String(s))
  loadData()
}
function handleSelectionChange(rows) { selectedIds.value = rows.map(r => r.id) }

function changeStatus(id, status) {
  updateBannerStatus(id, status).then(() => ElMessage.success("状态更新成功"))
}

function handleDelete(id) {
  ElMessageBox.confirm("确定删除该轮播图吗？", "提示", { type: "warning" })
      .then(() => deleteBanners([id]))
      .then(() => { ElMessage.success("删除成功"); loadData() })
}
function handleBatchDelete() {
  ElMessageBox.confirm("确定批量删除选中的轮播图吗？", "提示", { type: "warning" })
      .then(() => deleteBanners(selectedIds.value))
      .then(() => { ElMessage.success("删除成功"); loadData() })
}

const dialogVisible = ref(false)
const dialogTitle = ref("新增轮播图")

const form = reactive({
  id: null,
  title: "",
  imageUrl: "",
  jumpType: 0,
  jumpTarget: "",
  sort: 0,
  status: 1
})

function openDialog(row) {
  dialogVisible.value = true
  if (row?.id) {
    dialogTitle.value = "编辑轮播图"
    Object.assign(form, {
      id: row.id,
      title: row.title || "",
      imageUrl: row.imageUrl || "",
      jumpType: row.jumpType ?? 0,
      jumpTarget: row.jumpTarget || "",
      sort: row.sort ?? 0,
      status: row.status ?? 1
    })
  } else {
    dialogTitle.value = "新增轮播图"
    Object.assign(form, { id: null, title: "", imageUrl: "", jumpType: 0, jumpTarget: "", sort: 0, status: 1 })
  }
}

function submitForm() {
  if (!form.title?.trim()) return ElMessage.warning("请输入标题")
  if (!form.imageUrl?.trim()) return ElMessage.warning("请上传图片")

  const api = form.id ? updateBanner : addBanner
  api(form).then(() => {
    ElMessage.success("保存成功")
    dialogVisible.value = false
    loadData()
  })
}

async function handleUpload({ file }) {
  const res = await uploadImage(file)
  const url = res.data || res.msg
  if (!url) return ElMessage.error("上传失败：未返回地址")
  form.imageUrl = url
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
.empty { color:#999; }

.cover-square{
  width: 44px;
  height: 44px;
  border-radius: 8px;
  overflow: hidden;
  display: inline-block;
}

.uploader :deep(.el-upload){
  width: 110px;
  height: 110px;
  border-radius: 12px;
  border: 2px dashed #d6d6d6;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  overflow: hidden;
  transition: .2s;
}
.uploader :deep(.el-upload:hover){
  border-color: #409eff;
  background: rgba(64,158,255,0.06);
}
.cover-square-lg{
  width: 110px;
  height: 110px;
  border-radius: 12px;
}
.upload-plus{
  width: 110px;
  height: 110px;
  display:flex;
  align-items:center;
  justify-content:center;
  color:#8c8c8c;
  font-size: 36px;
}
</style>
