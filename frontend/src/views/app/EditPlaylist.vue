<template>
  <div class="playlist-edit">
    <el-form :model="playlist" ref="formRef" label-width="120px">
      <el-form-item label="歌单名称">
        <el-input v-model="playlist.name" placeholder="请输入歌单名称" />
      </el-form-item>

      <el-form-item label="歌单描述">
        <el-input v-model="playlist.description" placeholder="请输入歌单描述" />
      </el-form-item>

      <el-form-item label="歌单封面">
        <el-upload
            class="avatar-uploader"
            :show-file-list="false"
            :http-request="handleCoverUpload"
            accept="image/*"
        >
          <!-- 有图：显示圆形预览 -->
          <el-image
              v-if="playlist.coverUrl"
              :src="playlist.coverUrl"
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
            v-if="playlist.coverUrl"
            type="danger"
            style="margin-left: 12px"
            @click="playlist.coverUrl = ''"
        >
          删除
        </el-button>
      </el-form-item>

      <!-- ✅ 标签：QQ音乐风格（上面选中；下面分组点击） -->
      <el-form-item label="歌单标签">
        <div class="tag-box">
          <!-- 顶部：已选标签 -->
          <div class="tag-selected">
            <div class="tag-selected-list">
        <span
            v-for="t in selectedTagObjects"
            :key="t.id"
            class="tag-chip"
        >
          {{ t.name }}
          <span class="tag-chip-x" @click="removeTag(t.id)">×</span>
        </span>
              <span v-if="selectedTagObjects.length === 0" class="tag-empty">请选择标签</span>
            </div>

            <div class="tag-limit">
              还可选择 <b>{{ remainCount }}</b> 个
            </div>
          </div>

          <!-- 内容：分组标签 -->
          <div class="tag-panel">
            <div
                v-for="g in tagGroups"
                :key="g.type"
                class="tag-group"
            >
              <div class="tag-group-title">{{ g.typeName }}</div>

              <div class="tag-group-items">
          <span
              v-for="t in g.tags"
              :key="t.id"
              class="tag-item"
              :class="{ active: playlist.tagIds.includes(t.id), disabled: isDisabled(t.id) }"
              @click="toggleTag(t.id)"
          >
            {{ t.name }}
          </span>
              </div>
            </div>
          </div>
        </div>
      </el-form-item>


      <el-button type="primary" @click="savePlaylist">保存</el-button>
    </el-form>
  </div>
</template>

<script setup>
import { ref, onMounted ,computed} from "vue"
import { useRoute, useRouter } from "vue-router"
import { ElMessage } from "element-plus"
import { Plus } from "@element-plus/icons-vue"
import { getPlaylistDetail, editPlaylist as editPlaylistApi } from "@/api/app/playlist"
import { uploadImage } from "@/api/upload"
import { getTagGroups} from "@/api/app/tag"


const route = useRoute()
const router = useRouter()
const formRef = ref()

const playlist = ref({
  name: "",
  description: "",
  coverUrl: "",
  genre: "",
  tagIds: [] // ✅ 新增
})

const tagGroups = ref([])

const MAX_TAGS = 3
const remainCount = computed(() => {
  const len = (playlist.value.tagIds || []).length
  return Math.max(0, MAX_TAGS - len)
})



const tagMap = computed(() => {
  const m = new Map()
  for (const g of tagGroups.value) {
    for (const t of g.tags || []) m.set(t.id, t)
  }
  return m
})


const selectedTagObjects = computed(() => {
  return (playlist.value.tagIds || [])
      .map(id => tagMap.value.get(id))
      .filter(Boolean)
})

const loadTagGroups = async () => {
  const res = await getTagGroups()
  if (res.code === 0) tagGroups.value = res.data || []
  else ElMessage.error(res.msg || "标签加载失败")
}

// ✅ 点击切换标签
function toggleTag(id) {
  const ids = playlist.value.tagIds || []
  const idx = ids.indexOf(id)

  // 已选 -> 取消
  if (idx !== -1) {
    ids.splice(idx, 1)
    return
  }

  // 未选 -> 超过限制则不允许
  if (ids.length >= MAX_TAGS) {
    ElMessage.warning(`最多选择 ${MAX_TAGS} 个标签`)
    return
  }

  ids.push(id)
}

// 删除标签
function removeTag(id) {
  const ids = playlist.value.tagIds || []
  const idx = ids.indexOf(id)
  if (idx !== -1) ids.splice(idx, 1)
}

// 超过 MAX_TAGS 时，未选的都禁用（让 UI 灰掉）
function isDisabled(id) {
  const ids = playlist.value.tagIds || []
  return ids.length >= MAX_TAGS && !ids.includes(id)
}


const loadPlaylistDetail = async () => {
  const { id } = route.params
  const res = await getPlaylistDetail(id)

  // ✅ 这里假设详情里有 tags: [{id,name}]
  playlist.value = {
    name: res.data?.name || "",
    description: res.data?.description || "",
    coverUrl: res.data?.coverUrl || "",
    tagIds: (res.data?.tags || []).map(t => t.id),
  }
}


// ✅ el-upload 自定义上传：options.file 才是真文件
async function handleCoverUpload(options) {
  try {
    const file = options.file
    const res = await uploadImage(file) // 你封装的上传接口

    // ✅ 上传成功后，把 OSS 返回的 URL 填到 coverUrl，立刻预览
    playlist.value.coverUrl = res.data

    ElMessage.success("封面上传成功")

    // ✅ 通知 el-upload 上传成功（不写的话它会一直 loading）
    options.onSuccess?.(res, file)
  } catch (e) {
    options.onError?.(e)
    ElMessage.error(e?.msg || e?.message || "上传失败")
  }
}

const savePlaylist = async () => {
  const { id } = route.params
  const payload = {
    name: playlist.value.name,
    description: playlist.value.description,
    coverUrl: playlist.value.coverUrl,
    tagIds: playlist.value.tagIds
  }

  const res = await editPlaylistApi(id, payload)

  // 你的后端 Result 结构是 code/msg/data：
  if (res.code === 0) {
    ElMessage.success("歌单更新成功")
    router.push(`/app/playlists/${id}`)
  } else {
    ElMessage.error(res.msg || "保存失败")
  }
}

onMounted(async () => {
  await loadTagGroups()
  await loadPlaylistDetail()
})

</script>

<style scoped>
.playlist-edit {
  margin: 20px;
}

.avatar-uploader {
  display: inline-flex;
  align-items: center;
}

.avatar-upload-placeholder {
  width: 110px;
  height: 110px;
  border-radius: 50%;
  border: 2px dashed #d6d6d6;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;
}

.avatar-upload-placeholder:hover {
  border-color: #409eff;
  background: rgba(64, 158, 255, 0.06);
}

.avatar-plus {
  font-size: 38px;
  color: #8c8c8c;
}

.avatar-preview {
  width: 110px;
  height: 110px;
  border-radius: 50%;
  overflow: hidden;
  display: block;
}
.tag-box {
  width: 580px;
}

/* 顶部：已选区域 */
.tag-selected {
  border: 1px solid #e5e5e5;
  padding: 10px 12px;
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: center;
}

.tag-selected-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  min-height: 30px;
  align-items: center;
}

.tag-empty {
  color: #999;
  font-size: 14px;
}

.tag-limit {
  color: #999;
  font-size: 14px;
  white-space: nowrap;
}

/* 选中标签 chip */
.tag-chip {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  background: #9e9e9e;
  color: #fff;
  font-size: 13px;
  padding: 4px 10px;
  line-height: 22px;
  height: 26px;
  border-radius: 0;
}

.tag-chip-x {
  cursor: pointer;
  font-weight: 700;
  opacity: 0.9;
  line-height: 1;
}

/* 下方滚动面板 */
.tag-panel {
  border: 1px solid #e5e5e5;
  border-top: none;
  max-height: 330px;
  overflow: auto;
  padding: 16px 18px;
}

/* 分组 */
.tag-group {
  display: block;
  padding: 6px 0 6px;
}

.tag-group-title {
  color: #b5b5b5;
  font-size: 13px;
  font-weight: 600;
}

/* 标签列表（像 QQ 音乐那样一行多列） */
.tag-group-items {
  display: grid;
  grid-template-columns: repeat(5, minmax(40px, 1fr));
  gap: 0 15px;
}

.tag-item {
  cursor: pointer;
  font-size: 13px;
  color: #333;
  user-select: none;
}

.tag-item.active {
  color: #31c27c;
  font-weight: 700;
}

.tag-item.disabled {
  cursor: not-allowed;
}

</style>
