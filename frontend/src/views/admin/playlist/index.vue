<template>
  <div class="page">
    <div class="panel">
      <el-form :inline="true" :model="query" class="query-form">
        <el-form-item label="关键词">
          <el-input v-model="query.keyword" placeholder="请输入歌单名" clearable style="width: 200px" />
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
        <el-button type="primary" @click="openDialog()">新增歌单</el-button>
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
          :default-sort="{ prop: 'id', order: 'ascending' }"
      >
        <el-table-column type="selection" width="50" />
        <el-table-column prop="id" label="ID" align="center" sortable />
        <el-table-column label="歌单名" align="center" min-width="240">
          <template #default="{ row }">
            <el-popover
                placement="right-start"
                width="320"
                trigger="hover"
                @show="loadPlaylistSongs(row.id)"
            >
              <!-- 弹出内容：歌曲列表 -->
              <div v-loading="songLoadingMap.get(row.id)" style="max-height: 260px; overflow:auto;">
                <div v-if="(songMap.get(row.id) || []).length === 0" style="color:#999;">
                  暂无歌曲
                </div>
                <div
                    v-for="s in (songMap.get(row.id) || [])"
                    :key="s.id"
                    style="padding:6px 0; border-bottom:1px solid #f1f1f1;"
                >
                  <div style="font-size: 13px; font-weight: 600;">{{ s.songName }}</div>
                  <div style="font-size: 12px; color: #888;">{{ s.singerName || "-" }}</div>
                </div>
              </div>

              <!-- 单元格显示内容（点击它弹出） -->
              <template #reference>
        <span style="cursor:pointer; display:inline-flex; align-items:center; gap:6px;">
          <span>{{ row.name }}</span>
          <el-icon style="color:#409eff;"><ArrowDown /></el-icon>
        </span>
              </template>
            </el-popover>
          </template>
        </el-table-column>
        <el-table-column prop="singerName" label="主歌手" width="160" align="center"/>
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
        <el-table-column prop="collectCount" label="收藏量" width="100" align="center" />
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

        <el-table-column label="创建时间" width="180" align="center">
          <template #default="{ row }">
            {{ (row.createTime || "").replace("T", " ") }}
          </template>
        </el-table-column>

        <el-table-column label="操作" width="180" fixed="right" align="center">
          <template #default="scope">
            <el-button type="primary"  @click="openDialog(scope.row)">编辑</el-button>
            <el-button type="danger"  @click="handleDelete(scope.row.id)">删除</el-button>
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

    <!-- ✅ 新增/编辑歌单弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="640px">
      <el-form :model="form" label-width="90px">

        <el-form-item label="歌单名" required>
          <el-input v-model="form.name" placeholder="请输入歌单名" />
        </el-form-item>

        <el-form-item label="简介">
          <el-input
              v-model="form.description"
              type="textarea"
              :rows="3"
              placeholder="可选：填写歌单简介"
          />
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

         <el-form-item label="创建者类型">
          <el-tag>官方</el-tag>
        </el-form-item>

        <!-- ✅ 选择歌曲（重点） -->
        <el-form-item label="歌曲" required>
          <el-select
              v-model="form.songIds"
              multiple
              filterable
              remote
              reserve-keyword
              placeholder="输入歌曲名/歌手名搜索"
              :remote-method="remoteSearchSong"
              :loading="songLoading"
              style="width: 100%"
          >
            <el-option
                v-for="item in songOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
            />
          </el-select>
          <div style="margin-top: 6px; font-size: 12px; color: #999;">
            提示：这里选的是歌曲，歌手/专辑会跟随歌曲自动确定
          </div>
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
import { ArrowDown } from "@element-plus/icons-vue"
import {
  getPlaylistList,
  updatePlaylistStatus,
  deletePlaylists,
  getPlaylistDetail,
  savePlaylist,
  getPlaylistSongs
} from "@/api/admin/playlist"
import { selectSongs, selectSongsByIds } from "@/api/admin/song.js"
import {uploadImage} from "@/api/upload.js";

const loading = ref(false)
const list = ref([])
const total = ref(0)
const selectedIds = ref([])

const songMap = ref(new Map())      // playlistId -> songs[]
const songLoadingMap = ref(new Map()) // playlistId -> boolean

const DEFAULT_PAGE_SIZE = Number(localStorage.getItem("PLAYLIST_PAGE_SIZE")) || 10

const query = reactive({
  pageNum: 1,
  pageSize: DEFAULT_PAGE_SIZE,
  keyword: "",
  status: null
})

const dialogVisible = ref(false)
const dialogTitle = ref("新增歌单")

const form = reactive({
  id: null,
  name: "",
  description: "",
  coverUrl: "",
  status: 1,

  creatorType: 0,
  songIds: []
})

const headerStyle = {
  backgroundColor: "#f7f8fa",
  color: "#333",
  fontWeight: 600
}


const songOptions = ref([])
const songLoading = ref(false)

function loadData() {
  loading.value = true
  getPlaylistList(query)
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
  localStorage.setItem("PLAYLIST_PAGE_SIZE", String(size))
  loadData()
}

function handleSelectionChange(rows) {
  selectedIds.value = rows.map(r => r.id)
}

function changeStatus(id, status) {
  updatePlaylistStatus(id, status).then(() => ElMessage.success("状态更新成功"))
}

function handleDelete(id) {
  ElMessageBox.confirm("确定删除该歌单吗？", "提示", { type: "warning" })
      .then(() => deletePlaylists([id]))
      .then(() => {
        ElMessage.success("删除成功")
        loadData()
      })
}

function handleBatchDelete() {
  ElMessageBox.confirm("确定批量删除选中的歌单吗？", "提示", { type: "warning" })
      .then(() => deletePlaylists(selectedIds.value))
      .then(() => {
        ElMessage.success("删除成功")
        loadData()
      })
}

// ✅ 远程搜索歌曲
async function remoteSearchSong(keyword) {
  songLoading.value = true
  try {
    const res = await selectSongs(keyword)
    songOptions.value = res.data || []
  } finally {
    songLoading.value = false
  }
}

// ✅ 编辑回显：根据 songIds 把 option 补齐（否则 select 不显示 label）
async function loadSongOptionsByIds(ids) {
  if (!ids || ids.length === 0) return
  const res = await selectSongsByIds(ids)
  const opts = res.data || []
  const map = new Map(songOptions.value.map(i => [i.value, i]))
  opts.forEach(i => map.set(i.value, i))
  songOptions.value = Array.from(map.values())
}

async function openDialog(row) {
  dialogVisible.value = true;

  if (row?.id) {
    dialogTitle.value = "编辑歌单";

    const res = await getPlaylistDetail(row.id);
    const d = res.data;

    // 🔴 关键：再查一次歌单歌曲
    const songRes = await getPlaylistSongs(row.id);
    const songIds = (songRes.data || []).map(s => s.id);

    Object.assign(form, {
      id: d.id,
      name: d.name,
      description: d.description || "",
      coverUrl: d.coverUrl || "",
      status: d.status ?? 1,
      creatorType: d.creatorType || 0, // 默认为 0，官方歌单
      creatorAdminId: d.creatorAdminId || 1, // 默认为 1，超级管理员
      songIds
    });

    await loadSongOptionsByIds(songIds);
  } else {
    // 新增歌单时，默认值
    dialogTitle.value = "新增歌单";
    Object.assign(form, {
      id: null,
      name: "",
      description: "",
      coverUrl: "",
      status: 1,
      creatorType: 0, // 默认为 0，官方歌单
      creatorAdminId: 1, // 默认为 1，超级管理员
      songIds: []
    });
    songOptions.value = [];
  }
}



function submitForm() {
  savePlaylist(form).then(() => {
    ElMessage.success("保存成功")
    dialogVisible.value = false
    loadData()
  })
}


async function loadPlaylistSongs(playlistId) {
  // 有缓存就不请求
  if (songMap.value.has(playlistId)) return

  songLoadingMap.value.set(playlistId, true)
  try {
    const res = await getPlaylistSongs(playlistId)
    songMap.value.set(playlistId, res.data || [])
  } finally {
    songLoadingMap.value.set(playlistId, false)
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
