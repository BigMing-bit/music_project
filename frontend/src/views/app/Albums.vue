<template>
  <div class="albums" v-loading="loading">
    <!-- 页面标题和筛选 -->
    <div class="album-header">
      <div class="page-title">新碟首发</div>
      <div class="filter-tabs">
        <span class="filter-tab active">内地</span>
        <span class="filter-tab">港台</span>
        <span class="filter-tab">欧美</span>
        <span class="filter-tab">韩语</span>
        <span class="filter-tab">日本</span>
      </div>
      <div class="more-link">更多 ></div>
    </div>
    <!-- 专辑列表 -->
    <div class="album-grid">
      <div class="album-card" v-for="album in albumList" :key="album.id" @click="goAlbumDetail(album.id)">
        <div class="album-cover">
          <img :src="album.coverUrl" alt=""  />
        </div>
        <div class="album-info">
          <div class="album-name">{{ album.albumName }}</div>
          <div class="album-singer">{{ album.singerName }}</div>
        </div>
      </div>
    </div>
    
    <!-- 分页 -->
    <div class="pagination">
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.pageSize"
        :page-sizes="[20, 30, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="pagination.total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref, reactive } from "vue"
import { useRouter } from "vue-router"
import { ElMessage } from "element-plus"
import { getAlbumList } from "@/api/app/album"

const router = useRouter()
const loading = ref(false)

// 筛选条件
const filter = reactive({
  name: '',
  singerId: null,
})

// 分页信息
const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0,
})

// 专辑列表
const albumList = ref([])

// 加载专辑列表
async function loadAlbums() {
  loading.value = true
  try {
    const res = await getAlbumList({
      page: pagination.page,
      pageSize: pagination.pageSize,
      name: filter.name,
      singerId: filter.singerId,
    })
    const data = res.data || {}
    albumList.value = data.records || []
    pagination.total = data.total || 0
  } catch (error) {
    ElMessage.error('加载专辑列表失败')
    console.error('加载专辑列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 分页大小变化
function handleSizeChange(size) {
  pagination.pageSize = size
  pagination.page = 1
  loadAlbums()
}

// 当前页码变化
function handleCurrentChange(current) {
  pagination.page = current
  loadAlbums()
}

// 跳转到专辑详情页
function goAlbumDetail(id) {
  router.push(`/app/albums/${id}`)
}

// 组件挂载时加载数据
onMounted(() => {
  loadAlbums()
})
</script>

<style scoped>
.albums {
  padding: 20px 0;
}

/* 头部区域 */
.album-header {
  text-align: center;
  margin-bottom: 32px;
  position: relative;
}

.page-title {
  font-size: 24px;
  font-weight: 700;
  color: #333;
  margin-bottom: 16px;
}

/* 筛选标签 */
.filter-tabs {
  display: flex;
  justify-content: center;
  gap: 24px;
  margin-bottom: 16px;
}

.filter-tab {
  cursor: pointer;
  font-size: 14px;
  color: #666;
  transition: color 0.2s;
}

.filter-tab:hover {
  color: #31c27c;
}

.filter-tab.active {
  color: #31c27c;
}

/* 更多链接 */
.more-link {
  position: absolute;
  right: 0;
  top: 8px;
  cursor: pointer;
  font-size: 14px;
  color: #999;
}

.more-link:hover {
  color: #31c27c;
}

.filter-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
  padding: 20px;
  background: #f8f8f8;
  border-radius: 8px;
}

.filter-input {
  width: 300px;
}

.album-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 24px;
  margin-bottom: 32px;
}

.album-card {
  cursor: pointer;
  border-radius: 8px;
  overflow: hidden;
  transition: transform 0.2s, box-shadow 0.2s;
}

.album-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.album-cover {
  width: 100%;
  aspect-ratio: 1;
  overflow: hidden;
}

.album-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.album-info {
  padding: 12px;
}

.album-name {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.album-singer {
  font-size: 12px;
  color: #999;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 32px;
}

@media (max-width: 768px) {
  .filter-bar {
    flex-direction: column;
  }
  
  .filter-input {
    width: 100%;
  }
  
  .album-grid {
    grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
    gap: 16px;
  }
}
</style>
