<template>
  <div class="singer-detail" v-loading="loading">
    <!-- 歌手头部信息 -->
    <div class="header">
      <div class="avatar">
        <img :src="singerDetail.avatarUrl" alt="" />
      </div>
      <div class="info">
        <div class="name">{{ singerDetail.name }}</div>
        <div class="desc">{{ singerDetail.intro || '暂无简介' }}</div>
        <div class="stats">
          <span>单曲 {{ singerDetail.songCount || 0 }}</span>
          <span>专辑 {{ singerDetail.albumCount || 0 }}</span>
          <span>MV {{ singerDetail.mvCount || 0 }}</span>
        </div>
        <div class="actions">
          <el-button type="primary" size="large" class="play-btn">
            播放歌手热门歌曲
          </el-button>
          <el-button size="large" class="follow-btn">
            <el-icon><Plus /></el-icon>
            关注 {{ singerDetail.fansCount || 0 }}万
          </el-button>
        </div>
      </div>
    </div>
    
    <!-- 歌曲列表 -->
    <div class="block">
      <div class="block-title">
        <span>热门歌曲</span>
        <a href="#" class="view-all">查看全部 &gt;</a>
      </div>
      <div class="song-list">
        <div class="song-item" v-for="(song, index) in songList" :key="song.id">
          <div class="song-index">{{ index + 1 }}</div>
          <div class="song-info">
            <div class="song-name">{{ song.songName }}</div>
            <div class="song-meta">
              <span class="song-album">{{ song.albumName || '单曲' }}</span>
            </div>
          </div>
          <div class="song-duration">{{ song.duration || '00:00' }}</div>
        </div>
      </div>
    </div>
    
    <!-- 相似歌手 -->
    <div class="block" v-if="similarSingers.length">
      <div class="block-title">相似歌手</div>
      <div class="similar-list">
        <div class="similar-item" v-for="singer in similarSingers" :key="singer.id" @click="goSingerDetail(singer.id)">
          <div class="similar-avatar">
            <img :src="singer.avatarUrl" alt="" />
          </div>
          <div class="similar-name">{{ singer.name }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref, reactive, watch } from "vue"
import { useRoute, useRouter } from "vue-router"
import { ElMessage } from "element-plus"
import { Plus } from "@element-plus/icons-vue"
import { getSingerDetail, getSingerSongs, getSimilarSingers } from "@/api/app/singer"

const route = useRoute()
const router = useRouter()
const loading = ref(false)

// 歌手ID
const singerId = ref(route.params.id)

// 歌手详情
const singerDetail = ref({})

// 歌曲列表
const songList = ref([])
const songOrder = ref('hot')
const songPagination = reactive({
  page: 1,
  pageSize: 20,
  total: 0,
})

// 相似歌手
const similarSingers = ref([])

// 监听路由变化，重新加载数据
watch(() => route.params.id, (newId) => {
  singerId.value = newId
  loadSingerDetail()
  loadSongs()
  loadSimilarSingers()
})

// 加载歌手详情
async function loadSingerDetail() {
  loading.value = true
  try {
    const res = await getSingerDetail(singerId.value)
    singerDetail.value = res.data || {}
  } catch (error) {
    ElMessage.error('加载歌手详情失败')
    console.error('加载歌手详情失败:', error)
  } finally {
    loading.value = false
  }
}

// 加载歌曲列表
async function loadSongs() {
  try {
    const res = await getSingerSongs(singerId.value, {
      page: songPagination.page,
      pageSize: songPagination.pageSize,
      orderBy: songOrder.value,
    })
    const data = res.data || {}
    songList.value = data.records || []
    songPagination.total = data.total || 0
  } catch (error) {
    ElMessage.error('加载歌曲列表失败')
    console.error('加载歌曲列表失败:', error)
  }
}

// 加载相似歌手
async function loadSimilarSingers() {
  try {
    const res = await getSimilarSingers(singerId.value)
    similarSingers.value = res.data || []
  } catch (error) {
    console.error('加载相似歌手失败:', error)
  }
}

// 跳转到歌手详情页
function goSingerDetail(id) {
  router.push(`/app/singers/${id}`)
}

// 组件挂载时加载数据
onMounted(() => {
  loadSingerDetail()
  loadSongs()
  loadSimilarSingers()
})
</script>

<style scoped>
.singer-detail {
  padding: 20px 0;

}

.header {
  display: flex;
  gap: 32px;
  background: #f8f8f8;
  border-radius: 8px;
  padding: 40px;
  margin-bottom: 24px;
}

.avatar {
  width: 200px;
  height: 200px;
  border-radius: 50%;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
}

.avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.name {
  font-size: 36px;
  font-weight: 700;
  color: #333;
}

.desc {
  color: #666;
  line-height: 1.6;
  font-size: 14px;
  max-width: 600px;
}

.stats {
  display: flex;
  gap: 32px;
  color: #999;
  font-size: 14px;
}

.actions {
  display: flex;
  gap: 16px;
  margin-top: 8px;
}

.play-btn {
  width: 200px;
  background: #31c27c !important;
  border-color: #31c27c !important;
}

.play-btn:hover {
  background: #27a065 !important;
  border-color: #27a065 !important;
}

.follow-btn {
  width: 160px;
  border-color: #d9d9d9 !important;
  color: #666 !important;
}

.follow-btn:hover {
  border-color: #31c27c !important;
  color: #31c27c !important;
}

.block {
  background: #ffffff;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 1px 2px rgba(0,0,0,0.05);
  margin-bottom: 24px;
}

.block-title {
  font-weight: 700;
  margin-bottom: 20px;
  color: #333;
  font-size: 18px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.view-all {
  color: #666;
  font-size: 14px;
  text-decoration: none;
}

.view-all:hover {
  color: #31c27c;
}

.song-list {
  display: flex;
  flex-direction: column;
  gap: 0;
}

.song-item {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 16px 0;
  border-bottom: 1px solid #f0f0f0;
  transition: background-color 0.2s;
}

.song-item:hover {
  background-color: #f9f9f9;
}

.song-item:last-child {
  border-bottom: none;
}

.song-index {
  width: 32px;
  font-size: 16px;
  color: #999;
  font-weight: 500;
}

.song-item:nth-child(1) .song-index,
.song-item:nth-child(2) .song-index,
.song-item:nth-child(3) .song-index {
  color: #ff4d4f;
  font-weight: 700;
}

.song-info {
  flex: 1;
  min-width: 0;
}

.song-name {
  color: #333;
  font-weight: 500;
  font-size: 14px;
  margin-bottom: 6px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.song-meta {
  display: flex;
  align-items: center;
  gap: 16px;
}

.song-album {
  color: #999;
  font-size: 12px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  flex: 1;
}

.song-duration {
  color: #999;
  font-size: 12px;
  width: 80px;
  text-align: right;
}

.similar-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 32px;
}

.similar-item {
  cursor: pointer;
  text-align: center;
  transition: transform 0.2s;
}

.similar-item:hover {
  transform: translateY(-4px);
}

.similar-avatar {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  overflow: hidden;
  margin: 0 auto 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  background: linear-gradient(135deg, #31c27c 0%, #2196f3 100%);
}

.similar-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.similar-name {
  font-size: 14px;
  color: #333;
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

@media (max-width: 768px) {
  .header {
    flex-direction: column;
    align-items: center;
    text-align: center;
    padding: 32px 24px;
  }
  
  .avatar {
    width: 160px;
    height: 160px;
  }
  
  .name {
    font-size: 28px;
  }
  
  .stats {
    justify-content: center;
    gap: 24px;
  }
  
  .actions {
    justify-content: center;
  }
  
  .play-btn {
    width: 160px;
  }
  
  .follow-btn {
    width: 140px;
  }
  
  .similar-list {
    grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
    gap: 24px;
  }
  
  .similar-avatar {
    width: 80px;
    height: 80px;
  }
}
</style>
