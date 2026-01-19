<template>
  <div class="detail" v-loading="loading">
    <div class="header">
      <img class="cover" :src="albumDetail.coverUrl" />

      <div class="info">
        <div class="title">{{ albumDetail.albumName }}</div>
        <div class="singer-name" @click="goSingerDetail(albumDetail.singerId)">{{ albumDetail.singerName }}</div>
        <div class="meta">
          <span v-if="albumDetail.publishDate">发行时间: {{ albumDetail.publishDate }}</span>
        </div>
        <div class="actions">
          <button class="btn green" @click="playAll">
            <span class="btn-icon">▶</span> 播放全部
          </button>
          <button class="btn" :class="{ active: isCollected }" @click="toggleCollect">
            <span class="btn-icon">{{ isCollected ? '❤' : '♡' }}</span> {{ isCollected ? '已收藏' : '收藏' }}
          </button>
          <button class="btn">
            <span class="btn-icon">💬</span> 评论
          </button>
          <button class="btn">
            <span class="btn-icon">⋮</span> 更多
          </button>
        </div>
      </div>
    </div>

    <div class="block">
      <div class="block-title">歌曲列表</div>

      <div class="song-row" v-for="(song, idx) in songList" :key="song.id">
        <div class="idx">{{ String(idx + 1).padStart(2, "0") }}</div>
        <div class="song-main">
          <div class="song-name">{{ song.songName }}</div>
        </div>
        <div class="song-singer">{{ song.singerName }}</div>
        <div class="song-meta">{{ secondsToMMSS(song.durationSeconds) }}</div>
      </div>

      <div class="more" v-if="hasMore">
        <el-button text @click="loadMoreSongs">加载更多</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref, reactive, watch } from "vue"
import { useRoute, useRouter } from "vue-router"
import { ElMessage } from "element-plus"
import { getAlbumDetail, getAlbumSongs, collectAlbum } from "@/api/app/album"
import { useUserStore } from "@/store/user"

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)

// 专辑ID
const albumId = ref(route.params.id)

// 专辑详情
const albumDetail = ref({})

// 歌曲列表
const songList = ref([])
const cursor = ref(null)
const hasMore = ref(true)

// 收藏状态
const isCollected = ref(false)

// 监听路由变化，重新加载数据
watch(() => route.params.id, (newId) => {
  albumId.value = newId
  loadAlbumDetail()
  loadSongs()
})

// 加载专辑详情
async function loadAlbumDetail() {
  loading.value = true
  try {
    const res = await getAlbumDetail(albumId.value)
    albumDetail.value = res.data || {}
  } catch (error) {
    ElMessage.error('加载专辑详情失败')
    console.error('加载专辑详情失败:', error)
  } finally {
    loading.value = false
  }
}

// 加载歌曲列表
async function loadSongs() {
  try {
    const res = await getAlbumSongs(albumId.value, { cursor: null, size: 20 })
    const data = res.data || {}
    songList.value = data.list || []
    cursor.value = data.nextCursor || null
    hasMore.value = !!data.hasMore
  } catch (error) {
    ElMessage.error('加载歌曲列表失败')
    console.error('加载歌曲列表失败:', error)
  }
}

// 加载更多歌曲
async function loadMoreSongs() {
  if (!cursor.value || !hasMore.value) return
  
  try {
    const res = await getAlbumSongs(albumId.value, { cursor: cursor.value, size: 20 })
    const data = res.data || {}
    const newSongs = data.list || []
    songList.value = [...songList.value, ...newSongs]
    cursor.value = data.nextCursor || null
    hasMore.value = !!data.hasMore
  } catch (error) {
    ElMessage.error('加载更多歌曲失败')
    console.error('加载更多歌曲失败:', error)
  }
}

// 播放全部
function playAll() {
  ElMessage.success('播放全部')
  // 这里可以添加播放全部的逻辑
}

// 收藏/取消收藏
async function toggleCollect() {
  if (!userStore.isLogin) {
    ElMessage.warning('请先登录')
    return
  }
  
  try {
    const res = await collectAlbum(albumId.value)
    isCollected.value = res.data
    ElMessage.success(isCollected.value ? '收藏成功' : '取消收藏成功')
  } catch (error) {
    ElMessage.error('操作失败')
    console.error('收藏操作失败:', error)
  }
}

// 跳转到歌手详情页
function goSingerDetail(singerId) {
  if (singerId) {
    router.push(`/app/singers/${singerId}`)
  }
}

// 格式化时长
function secondsToMMSS(s) {
  if (!s && s !== 0) return ""
  const m = Math.floor(s / 60)
  const sec = String(s % 60).padStart(2, "0")
  return `${m}:${sec}`
}

// 组件挂载时加载数据
onMounted(() => {
  userStore.hydrate()
  loadAlbumDetail()
  loadSongs()
})
</script>

<style scoped>
.detail { display:flex; flex-direction:column; gap: 20px; }
.header {
  display:flex; gap: 24px;
  background: #f8f8f8;
  border-radius: 8px;
  padding: 24px;
}
.cover { width: 200px; height: 200px; border-radius: 8px; object-fit: cover; }
.info { flex:1; display:flex; flex-direction:column; gap: 12px; }
.title { font-size: 24px; font-weight: 700; color:#333; }
.singer-name {
  color: #1890ff;
  font-size: 16px;
  cursor: pointer;
}
.singer-name:hover {
  text-decoration: underline;
}
.meta { display:flex; gap: 20px; color:#999; font-size: 14px; }

.actions {
  display: flex; gap: 12px;
  margin-top: 10px;
}
.btn {
  display: flex; align-items: center; gap: 6px;
  height: 36px;
  padding: 0 16px;
  border: 1px solid #dcdcdc;
  background: #fff;
  cursor: pointer;
  font-weight: 600;
  font-size: 14px;
  border-radius: 4px;
  transition: all 0.2s;
}
.btn:hover {
  border-color: #27c27a;
  color: #27c27a;
}
.btn.green {
  background: #27c27a;
  border-color: #27c27a;
  color: #fff;
}
.btn.green:hover {
  background: #22a86b;
  border-color: #22a86b;
}
.btn.active {
  color: #ff4d4f;
  border-color: #ff4d4f;
}
.btn.active:hover {
  background: #fff1f0;
}
.btn-icon {
  font-size: 16px;
}

.block {
  background: #ffffff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 1px 2px rgba(0,0,0,0.05);
}
.block-title { font-weight: 700; margin-bottom: 16px; color:#333; font-size: 16px; }

.song-row {
  display:flex; align-items:center; gap: 16px;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}
.song-row:last-child {
  border-bottom: none;
}
.idx { width: 32px; color:#999; font-size: 14px; text-align:center; font-weight: 500; }
.song-row:nth-child(1) .idx,
.song-row:nth-child(2) .idx,
.song-row:nth-child(3) .idx {
  color: #ff4d4f;
}
.song-main { flex:1; }
.song-name { color:#333; font-weight: 500; font-size: 14px; }
.song-singer { width: 150px; color:#666; font-size: 14px; }
.song-meta { color:#999; font-size: 12px; width: 80px; text-align:right; }
.more { display:flex; justify-content:center; margin-top: 16px; }

@media (max-width: 768px) {
  .header {
    flex-direction: column;
    align-items: center;
    text-align: center;
  }
  
  .cover {
    width: 160px;
    height: 160px;
  }
  
  .actions {
    justify-content: center;
    flex-wrap: wrap;
  }
  
  .song-row {
    flex-wrap: wrap;
    gap: 8px;
  }
  
  .song-singer {
    width: auto;
    flex: 1;
  }
  
  .song-meta {
    width: auto;
    text-align: left;
  }
}
</style>
