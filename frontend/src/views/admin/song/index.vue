<template>
  <div class="page">
    <div class="panel">
      <el-form :inline="true" :model="query" class="query-form">
        <el-form-item label="关键词">
          <el-input v-model="query.keyword" placeholder="请输入歌曲名" clearable style="width: 200px" />
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
    <div class="toolbar">
      <div class="toolbar-left">
        <el-button type="primary" @click="openDialog()">新增歌曲</el-button>
        <el-button
            type="danger"
            :disabled="selectedIds.length === 0"
            @click="handleBatchDelete"
        >
          批量删除
        </el-button>
      </div>
      <div class="toolbar-right">
        共 <span class="total">{{ total }}</span> 条
      </div>
    </div>
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
        <el-table-column prop="songName" label="歌曲名" style="text-align: center" align="center" min-width="200" />
        <el-table-column prop="singerName" label="歌手" style="text-align: center" align="center" width="160" />
        <el-table-column prop="albumName" label="专辑" style="text-align: center" align="center" width="160" />
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
        <el-table-column prop="playCount" label="播放量" width="100" align="center" />
        <el-table-column prop="likeCount" label="点赞量" width="100" align="center" />

        <el-table-column label="状态" width="120" align="center">
          <template #default="scope">
            <el-switch
                v-model="scope.row.status"
                :active-value="1"
                :inactive-value="0"
                @change="val => changeStatus(scope.row.id, val)"
            />
          </template>
        </el-table-column>

        <el-table-column prop="createTime" style="text-align: center" align="center" label="创建时间" width="180" />
        <el-table-column label="操作" width="180" fixed="right" align="center">
          <template #default="scope">
            <el-button type="primary"  @click="openDialog(scope.row)">编辑</el-button>
            <el-button type="danger"  @click="handleDelete(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pager">
      <!-- ✅ 分页 -->
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
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="520px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="歌曲名">
          <el-input v-model="form.songName" />
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
        <el-form-item label="专辑">
          <el-select
              v-model="form.albumId"
              filterable
              remote
              reserve-keyword
              placeholder="请输入专辑名搜索"
              :remote-method="remoteSearchAlbum"
              :loading="albumLoading"
              style="width: 100%"
          >
            <el-option
                v-for="item in albumOptions"
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
        <el-form-item label="时长(秒)">
          <el-input v-model="form.durationSeconds" />
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
  getSongList,
  addSong,
  updateSong,
  updateSongStatus,
  deleteSongs
} from "@/api/admin/song"

import { selectSingers } from "@/api/admin/singer"
import { selectAlbums } from "@/api/admin/album"
import {uploadImage} from "@/api/upload.js";

const singerOptions = ref([])
const albumOptions = ref([])

const singerLoading = ref(false)
const albumLoading = ref(false)

const loading = ref(false)
const list = ref([])
const total = ref(0)
const selectedIds = ref([])

const DEFAULT_PAGE_SIZE = Number(localStorage.getItem("SONG_PAGE_SIZE")) || 10

const query = reactive({
  pageNum: 1,
  pageSize: DEFAULT_PAGE_SIZE,
  keyword: "",
  status: null
})


function loadData() {
  loading.value = true
  getSongList(query)
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
  localStorage.setItem("SONG_PAGE_SIZE", String(size)) // 记住用户选择
  loadData()
}


const headerStyle = {
  backgroundColor: "#f7f8fa",
  color: "#333",
  fontWeight: 600
}

function handleSelectionChange(rows) {
  selectedIds.value = rows.map(r => r.id)
}

function changeStatus(id, status) {
  updateSongStatus(id, status).then(() => {
    ElMessage.success("状态更新成功")
  })
}

function handleDelete(id) {
  ElMessageBox.confirm("确定删除该歌曲吗？", "提示", { type: "warning" })
      .then(() => deleteSongs([id]))
      .then(() => {
        ElMessage.success("删除成功")
        loadData()
      })
}

function handleBatchDelete() {
  ElMessageBox.confirm("确定批量删除选中的歌曲吗？", "提示", { type: "warning" })
      .then(() => deleteSongs(selectedIds.value))
      .then(() => {
        ElMessage.success("删除成功")
        loadData()
      })
}

const dialogVisible = ref(false)
const dialogTitle = ref("新增歌曲")
const form = reactive({})

async function openDialog(row) {
  dialogVisible.value = true

  if (row) {
    dialogTitle.value = "编辑歌曲"
    Object.assign(form, row)

    // ✅ 回显：确保下拉里有当前歌手/专辑
    if (row.singerId) await remoteSearchSinger("")
    if (row.albumId) await remoteSearchAlbum("")

  } else {
    dialogTitle.value = "新增歌曲"
    Object.assign(form, {
      songName: "",
      singerId: null,
      albumId: null,
      coverUrl: "",
      audioUrl: "",
      durationSeconds: 0,
      status: 1
    })

    singerOptions.value = []
    albumOptions.value = []
  }
}



// ✅ 远程搜索歌手
async function remoteSearchSinger(keyword) {
  singerLoading.value = true
  try {
    const res = await selectSingers(keyword)
    singerOptions.value = res.data || []
  } finally {
    singerLoading.value = false
  }
}

// ✅ 远程搜索专辑
async function remoteSearchAlbum(keyword) {
  albumLoading.value = true
  try {
    const res = await selectAlbums(keyword)
    albumOptions.value = res.data || []
  } finally {
    albumLoading.value = false
  }
}

function submitForm() {
  const api = form.id ? updateSong : addSong
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
