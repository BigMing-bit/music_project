<template>
  <div class="singers" v-loading="loading">
    <!-- 筛选标签 -->
    <div class="filter-tabs">
      <!-- 字母分类 -->
      <div class="tab-group">
        <span class="tab-item" :class="{ active: filter.letter === '' }" @click="filter.letter = ''; loadSingers()">全部</span>
        <span class="tab-item" v-for="letter in letters" :key="letter" :class="{ active: filter.letter === letter }" @click="filter.letter = letter; loadSingers()">{{ letter }}</span>
      </div>
      
      <!-- 地区分类 -->
      <div class="tab-group">
        <span class="tab-item" :class="{ active: filter.area === '' }" @click="filter.area = ''; loadSingers()">全部</span>
        <span class="tab-item" :class="{ active: filter.area === '1' }" @click="filter.area = '1'; loadSingers()">内地</span>
        <span class="tab-item" :class="{ active: filter.area === '2' }" @click="filter.area = '2'; loadSingers()">港台</span>
        <span class="tab-item" :class="{ active: filter.area === '3' }" @click="filter.area = '3'; loadSingers()">欧美</span>
        <span class="tab-item" :class="{ active: filter.area === '4' }" @click="filter.area = '4'; loadSingers()">日本</span>
        <span class="tab-item" :class="{ active: filter.area === '5' }" @click="filter.area = '5'; loadSingers()">韩国</span>
      </div>
      
      <!-- 性别分类 -->
      <div class="tab-group">
        <span class="tab-item" :class="{ active: filter.gender === '' }" @click="filter.gender = ''; loadSingers()">全部</span>
        <span class="tab-item" :class="{ active: filter.gender === '1' }" @click="filter.gender = '1'; loadSingers()">男</span>
        <span class="tab-item" :class="{ active: filter.gender === '2' }" @click="filter.gender = '2'; loadSingers()">女</span>
        <span class="tab-item" :class="{ active: filter.gender === '3' }" @click="filter.gender = '3'; loadSingers()">组合</span>
      </div>
      
      <!-- 风格分类 -->
      <div class="tab-group">
        <span class="tab-item" :class="{ active: filter.style === '' }" @click="filter.style = ''; loadSingers()">全部</span>
        <span class="tab-item" :class="{ active: filter.style === '1' }" @click="filter.style = '1'; loadSingers()">流行</span>
        <span class="tab-item" :class="{ active: filter.style === '2' }" @click="filter.style = '2'; loadSingers()">说唱</span>
        <span class="tab-item" :class="{ active: filter.style === '3' }" @click="filter.style = '3'; loadSingers()">国风</span>
        <span class="tab-item" :class="{ active: filter.style === '4' }" @click="filter.style = '4'; loadSingers()">摇滚</span>
        <span class="tab-item" :class="{ active: filter.style === '5' }" @click="filter.style = '5'; loadSingers()">电子</span>
        <span class="tab-item" :class="{ active: filter.style === '6' }" @click="filter.style = '6'; loadSingers()">民谣</span>
        <span class="tab-item" :class="{ active: filter.style === '7' }" @click="filter.style = '7'; loadSingers()">R&B</span>
        <span class="tab-item" :class="{ active: filter.style === '8' }" @click="filter.style = '8'; loadSingers()">民族乐</span>
        <span class="tab-item" :class="{ active: filter.style === '9' }" @click="filter.style = '9'; loadSingers()">轻音乐</span>
        <span class="tab-item" :class="{ active: filter.style === '10' }" @click="filter.style = '10'; loadSingers()">爵士</span>
        <span class="tab-item" :class="{ active: filter.style === '11' }" @click="filter.style = '11'; loadSingers()">古典</span>
        <span class="tab-item" :class="{ active: filter.style === '12' }" @click="filter.style = '12'; loadSingers()">乡村</span>
        <span class="tab-item" :class="{ active: filter.style === '13' }" @click="filter.style = '13'; loadSingers()">蓝调</span>
      </div>
    </div>

    <div class="singer-cards">
      <div class="singer-card" v-for="s in topSingers" :key="s.id" @click="goSingerDetail(s.id)">
        <div class="singer-avatar">
          <img :src="s.avatarUrl" alt="" />
        </div>
        <div class="singer-info">
          <div class="singer-name">{{ s.name }}</div>
        </div>
      </div>
    </div>


    <div class="singer-names" v-if="restSingers.length">
      <div class="name-item" v-for="s in restSingers" :key="s.id" @click="goSingerDetail(s.id)">
        {{ s.name }}
      </div>
    </div>
    <div v-if="loadingMore" class="loading-more">
      <el-icon class="is-loading"><Loading /></el-icon>
      <span>加载中...</span>
    </div>
  </div>
</template>

<script setup>
import {onMounted, onUnmounted, ref, reactive, computed} from "vue"
import { useRouter } from "vue-router"
import { ElMessage, ElIcon } from "element-plus"
import { Loading } from "@element-plus/icons-vue"
import { getSingerList } from "@/api/app/singer"


const router = useRouter()
const loading = ref(false)

// 字母列表
const letters = ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '#']

// 筛选条件
const filter = reactive({
  name: '',
  gender: '',
  area: '',
  style: '',
  letter: '',
})

// 歌手列表
const singerList = ref([])

const topSingers = computed(() => singerList.value.slice(0, 10))
const restSingers = computed(() => singerList.value.slice(10))

// 无限滚动相关
const currentPage = ref(1)
const pageSize = ref(30)
const loadingMore = ref(false)
const hasMore = ref(true)

// 加载歌手列表
async function loadSingers(isLoadMore = false) {
  if (!isLoadMore) {
    // 重置数据
    singerList.value = []
    currentPage.value = 1
    hasMore.value = true
    loading.value = true
  } else {
    loadingMore.value = true
  }

  try {
    const res = await getSingerList({
      page: currentPage.value,
      pageSize: pageSize.value,
      name: filter.name,
      gender: filter.gender ? parseInt(filter.gender) : null,
      area: filter.area ? parseInt(filter.area) : null,
      style: filter.style ? parseInt(filter.style) : null,
      letter: filter.letter || null,
    })
    console.log('加载歌手列表成功:', res)
    const records = res.data?.records || []

    if (isLoadMore) {
      // 追加数据
      singerList.value = [...singerList.value, ...records]
    } else {
      // 替换数据
      singerList.value = records
    }

    // 检查是否还有更多数据
    hasMore.value = singerList.value.length < (res.data?.total || 0)
    // 增加页码
    currentPage.value++
  } catch (error) {
    ElMessage.error('加载歌手列表失败，请检查后端服务是否启动')
    console.error('加载歌手列表失败:', error)
    hasMore.value = false
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}

// 跳转到歌手详情页
function goSingerDetail(id) {
  router.push(`/app/singers/${id}`)
}

// 监听滚动事件，实现无限加载
function handleScroll() {
  const scrollTop = document.documentElement.scrollTop || document.body.scrollTop
  const scrollHeight = document.documentElement.scrollHeight
  const clientHeight = document.documentElement.clientHeight
  
  // 当滚动到距离底部100px时加载更多
  if (scrollTop + clientHeight >= scrollHeight - 100 && !loading.value && !loadingMore.value && hasMore.value) {
    loadSingers(true)
  }
}

// 组件挂载时加载数据并添加滚动事件监听
onMounted(() => {
  loadSingers()
  window.addEventListener('scroll', handleScroll)
})

// 组件卸载时移除滚动事件监听
onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})
</script>

<style scoped>
.singers {
  padding: 20px 0;
}

.page-title {
  font-size: 20px;
  font-weight: 700;
  color: #333;
  margin-bottom: 24px;
}


.filter-tabs {
  margin-bottom: 32px;
}

.tab-group {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
  overflow-x: auto;
  white-space: nowrap;
}

.tab-group::-webkit-scrollbar {
  height: 4px;
}

.tab-group::-webkit-scrollbar-track {
  background: #f0f0f0;
}

.tab-group::-webkit-scrollbar-thumb {
  background: #ccc;
}

.tab-item {
  cursor: pointer;
  padding: 6px 12px;
  font-style: normal;
  font-size: 13px;
  color: #666;
  transition: all 0.2s;
}

.tab-item:hover {
  color: #31c27c;
}

.tab-item.active {
  color: #fff;
  background: #31c27c;
}
.singer-cards{
  display:grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 32px;
  margin-bottom: 26px;
}

.singer-card{
  cursor:pointer;
  text-align:center;
  padding: 29px 0;
  margin-right: 2px;
  background:#fbfbfd
}

.singer-avatar{
  width: 140px;
  height: 140px;
  border-radius: 50%;
  overflow:hidden;
  margin: 0 auto 16px;
}

.singer-avatar img{
  width:100%;
  height:100%;
  object-fit:cover;
}

.singer-name{
  font-size: 15px;
  font-weight: 600;
  color:#222;
  white-space: nowrap;
  overflow:hidden;
  text-overflow: ellipsis;
}

/* 下面名字区：像图2那样多列排 */
.singer-names{
  display:grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 18px 24px; /* 行间距/列间距 */
  padding: 6px 2px 18px;
}

.name-item{
  cursor:pointer;
  font-size: 14px;
  color:#333;
  padding: 6px 0;
}

.name-item:hover{
  color:#31c27c;
}

/* 响应式 */
@media (max-width: 1200px){
  .singer-cards, .singer-names{ grid-template-columns: repeat(4, 1fr); }
}
@media (max-width: 960px){
  .singer-cards, .singer-names{ grid-template-columns: repeat(3, 1fr); }
}
@media (max-width: 768px){
  .singer-cards, .singer-names{ grid-template-columns: repeat(2, 1fr); }
}

</style>
