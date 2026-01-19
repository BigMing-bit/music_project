<template>
  <div class="home-page">
    <!-- Banner -->
    <section class="hero" :style="heroStyle">
      <div class="home-inner">
        <el-carousel
            v-if="banners.length"
            height="520px"
            indicator-position="inside"
            arrow="never"
            :interval="4500"
            trigger="hover"
        >
          <el-carousel-item v-for="b in banners" :key="b.id">
            <div class="hero-slide">
              <img class="banner-img" :src="fixImg(b.imageUrl)" :alt="b.title || 'banner'" @error="onImgErr" />
              <div class="hero-mask"></div>
            </div>
          </el-carousel-item>
        </el-carousel>

        <div v-else class="banner-skeleton">
          <el-skeleton :rows="3" animated />
        </div>
      </div>
    </section>

    <!-- 歌单推荐 -->
    <section class="section">
      <div class="home-inner">
        <div class="section-head center">
          <div class="title big">歌 单 推 荐</div>
          <div class="actions">
            <el-button size="small" @click="router.push('/app/playlists')">更多</el-button>
          </div>
        </div>

        <div class="tabs center">
          <span class="tab active">为你推荐</span>
          <span class="tab">官方歌单</span>
          <span class="tab">情歌</span>
          <span class="tab">网络歌曲</span>
          <span class="tab">经典</span>
          <span class="tab">KTV热歌</span>
        </div>

        <div class="rail">
          <div class="h-scroll">
            <div class="h-scroll-inner" ref="playlistScroll">
              <div class="cover-card" v-for="p in playlists" :key="p.id">
                <div class="cover" @click="goPlaylistDetail(p.id)">
                  <img :src="fixImg(p.coverUrl)" alt="" />
                  <div class="overlay">
                    <button
                        class="play-btn"
                        title="播放"
                        @click.stop="playAndGoPlayer(p.id)"> ▶ </button>
                  </div>
                </div>
                <div class="cover-title">{{ p.name }}</div>
                <div class="cover-sub">播放量：{{ p.playCount ?? 0 }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 热门专辑 -->
    <section class="section">
      <div class="home-inner">
        <div class="section-head center">
          <div class="title big">新 碟 首 发</div>
          <div class="actions">
<!--            <el-button size="small" @click="router.push('/app/albums')">更多</el-button>-->  
          </div>
        </div>

        <div class="tabs center">
          <span class="tab active">内地</span>
          <span class="tab">港台</span>
          <span class="tab">欧美</span>
          <span class="tab">韩语</span>
          <span class="tab">日本</span>
        </div>

        <div class="rail">
          <div class="h-scroll">
            <div class="h-scroll-inner" ref="albumScroll">
              <div class="cover-card" v-for="a in albums" :key="a.id">
                <!-- 封面图片：进歌单详情 -->
                <div class="cover" @click="goAlbumDetail(a.id)">
                  <img :src="fixImg(a.coverUrl)" alt=""/>

                  <!-- 播放按钮：进播放页 -->
                  <div class="overlay">
                    <button
                        class="play-btn"
                        title="播放"
                        @click.stop="playAndGoPlayer(a.id)"
                    >
                      ▶
                    </button>
                  </div>
                </div>

                <div class="cover-title">{{ a.albumName }}</div>
                <div class="cover-sub">{{ a.singerName || "未知歌手" }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 新歌首发 -->
    <section class="section">
      <div class="home-inner">
        <div class="section-head center">
          <div class="title big">新 歌 首 发</div>
        </div>

        <div class="tabs center">
          <span class="tab active">最新</span>
          <span class="tab">内地</span>
          <span class="tab">港台</span>
          <span class="tab">欧美</span>
          <span class="tab">韩国</span>
          <span class="tab">日本</span>
        </div>

        <div class="song-actions">
          <el-button size="large" @click="comingSoon">播放全部</el-button>
        </div>

        <el-carousel v-if="songPages.length" height="420px" indicator-position="outside" arrow="never">
          <el-carousel-item v-for="(page, pi) in songPages" :key="pi">
            <div class="song-grid">
              <div class="song-row" v-for="s in page" :key="s.id">
                <div class="song-cover" @click="goPlayerBySong(s.id)">
                  <img :src="fixImg(s.coverUrl)" @error="onImgErr"  alt=""/>
                </div>
                <div class="song-main">
                  <div class="song-name" @click="goSongDetail(s.id)">
                    {{ s.songName }}
                  </div>
                  <div class="song-singer" @click="goSingerDetail(s.singerId)">
                    {{ s.singerName || "未知歌手" }}
                  </div>
                </div>

                <div class="song-duration">{{ secondsToMMSS(s.durationSeconds) }}</div>
              </div>


            </div>
          </el-carousel-item>
        </el-carousel>

        <div v-else class="empty">暂无歌曲</div>
      </div>
    </section>


    <!-- 热门专辑 -->
    <section class="section">
      <div class="home-inner">
        <div class="section-head center">
          <div class="title big">M V</div>
          <div class="actions">
            <!--            <el-button size="small" @click="router.push('/app/albums')">更多</el-button>-->
          </div>
        </div>

        <div class="tabs center">
          <span class="tab active">精选</span>
          <span class="tab">内地</span>
          <span class="tab">港台</span>
          <span class="tab">欧美</span>
          <span class="tab">韩国</span>
          <span class="tab">日本</span>
        </div>

        <div class="rail">
          <div class="h-scroll">
            <div class="h-scroll-inner" ref="mvScroll">
              <div class="cover-card" v-for="a in albums" :key="a.id">
                <!-- 封面图片：进歌单详情 -->
                <div class="cover" @click="goAlbumDetail(a.id)">
                  <img :src="fixImg(a.coverUrl)" alt=""/>

                  <!-- 播放按钮：进播放页 -->
                  <div class="overlay">
                    <button
                        class="play-btn"
                        title="播放"
                        @click.stop="playAndGoPlayer(a.id)"
                    >
                      ▶
                    </button>
                  </div>
                </div>

                <div class="cover-title">{{ a.albumName }}</div>
                <div class="cover-sub">{{ a.singerName || "未知歌手" }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>


<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from "vue"
import { useRouter } from "vue-router"
import { ElMessage } from "element-plus"
import { getHome, getHomeBanners, getHomePlaylists } from "@/api/app/home"
// ✅ 这里用你已经有的 playPlaylist（只给按钮用）
import { playPlaylist } from "@/api/app/playlist.js"
import { getRecommendPlaylists, getRecommendPlaylistsWithFilter } from "@/api/app/reco.js"
import { useUserStore } from "@/store/user"

// 如果你要用更标准的“歌单推荐卡片接口”，替换 refreshPlaylists/loadHome 的 playlists 获取：
// import { getPlaylistHome } from "@/api/app/playlist.js"

const router = useRouter()
const userStore = useUserStore()

const banners = ref([])
const playlists = ref([])
const albums = ref([])
const newSongs = ref([])

const playlistScroll = ref(null)
const albumScroll = ref(null)
const mvScroll = ref(null)

const pHover = ref(false)
const aHover = ref(false)

/* ===== banner 滚动渐隐 ===== */
const scrollY = ref(0)
const sections = ref([])

function onScroll() {
  scrollY.value = window.scrollY || 0
  const windowHeight = window.innerHeight
  
  // 为每个section设置背景色，实现平滑渐变效果
  sections.value.forEach((section, index) => {
    if (section) {
      const rect = section.getBoundingClientRect()
      const sectionTop = rect.top
      const sectionBottom = rect.bottom
      const sectionHeight = rect.height
      
      // 计算section在视口中的可见部分
      const visibleTop = Math.max(sectionTop, 0)
      const visibleBottom = Math.min(sectionBottom, windowHeight)
      const visibleHeight = visibleBottom - visibleTop
      const visibilityRatio = visibleHeight / sectionHeight
      
      // 计算背景色透明度
      // 当section在视口中的可见比例超过50%时，背景逐渐变为灰色
      // 当section在视口中的可见比例低于50%时，背景逐渐变为白色
      let opacity = 0
      if (visibilityRatio > 0.5) {
        // 可见比例超过50%，逐渐变为灰色
        opacity = (visibilityRatio - 0.5) * 2
      } else {
        // 可见比例低于50%，逐渐变为白色
        opacity = visibilityRatio * 2
      }
      
      // 应用背景色渐变效果
      section.style.backgroundColor = `rgba(245, 245, 245, ${opacity})`
    }
  })
}

const heroStyle = computed(() => {
  const y = scrollY.value
  const fadeRange = 260
  const alpha = Math.max(0, 1 - y / fadeRange)
  return {
    opacity: alpha,
    transform: `translateY(${Math.min(y * -0.08, -18)}px)`,
    transition: "opacity .12s linear, transform .12s linear",
  }
})

const fallbackCover =
    "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?auto=format&fit=crop&w=800&q=80"

function fixImg(url) {
  if (!url) return fallbackCover
  if (typeof url === "string" && url.startsWith("/")) return `${location.origin}${url}`
  return url
}
function onImgErr(e) {
  e.target.src = fallbackCover
}

function pickData(res) {
  if (!res) return null
  if (res.code !== undefined) return res.data
  if (res.data && res.data.code !== undefined) return res.data.data
  return res.data ?? res
}

function secondsToMMSS(s) {
  if (s === null || s === undefined) return ""
  const n = Number(s)
  if (Number.isNaN(n)) return ""
  const m = Math.floor(n / 60)
  const sec = String(n % 60).padStart(2, "0")
  return `${m}:${sec}`
}



/* ✅ 卡片点击：只跳转 */
function goPlaylist(id) {
  router.push(`/app/playlists/${id}`)
}
function goAlbum(id) {
  router.push(`/app/albums/${id}`)
}
/* 你有歌曲详情页就改成你的真实路由 */
// 1) 歌曲详情
function goSongDetail(id) {
  router.push(`/app/songs/${id}`)
}
function goSingerDetail(id) {
  router.push(`/app/singers/${id}`)
}
function goPlayerBySong(songId) {
  router.push({ path: "/app/player", query: { songId } })
}



function comingSoon() {
  ElMessage.info("待开发")
}

/* ✅ 播放按钮：只做 +1，不跳转 */
async function onPlayPlaylist(id) {
  // ✅ 静默 +1，不提示
  try {
    await playPlaylist(id)
    // 可选：让当前卡片数字立刻 +1（不弹提示）
    const it = playlists.value.find((x) => x.id === id)
    if (it) it.playCount = (it.playCount ?? 0) + 1
  } catch (e) {
    // 不提示（你要求不提示）
  }

  // ✅ 然后跳转详情页
  router.push(`/app/playlists/${id}`)
}

/** 新歌分页：每页 9 首（3列*3行） */
const songPages = computed(() => {
  const list = newSongs.value || []
  const pageSize = 9
  const pages = []
  for (let i = 0; i < list.length; i += pageSize) pages.push(list.slice(i, i + pageSize))
  return pages
})

async function loadHome() {
  const res = await getHome()
  const d = pickData(res) || {}

  playlists.value = d.playlists?.list || d.playlists || []
  albums.value = d.albums?.list || d.albums || []
  newSongs.value = d.hotSongs?.list || d.hotSongs || d.newSongs?.list || d.newSongs || []

  // 如果你改用 getPlaylistHome，就用：
  // const rr = await getPlaylistHome({ size: 8 })
  // const dd = pickData(rr) || {}
  // playlists.value = dd.list || []
} 



function goPlaylistDetail(id) {
  router.push(`/app/playlists/${id}`)
}

function goAlbumDetail(id) {
  router.push(`/app/albums/${id}`)
}
async function playAndGoPlayer(id) {
  // 1️⃣ 静默 +1 播放量
  try {
    await playPlaylist(id)

    // 可选：让首页数字立刻变化（体验更真实）
    const it = playlists.value.find(p => p.id === id)
    if (it) it.playCount = (it.playCount ?? 0) + 1
  } catch (e) {
    // 不提示，符合你的要求
  }

  // 2️⃣ 跳转播放页（你自己的 Player 页面）
  router.push({
    path: "/app/player",
    query: { playlistId: id }
  })
}


async function loadBanners() {
  const res = await getHomeBanners()
  const d = pickData(res) || {}
  banners.value = d.list || d.records || d.data || d || []
}



onMounted(async () => {
  onScroll()
  window.addEventListener("scroll", onScroll, { passive: true })

  // 获取所有section元素
  setTimeout(() => {
    sections.value = Array.from(document.querySelectorAll('.section'))
    onScroll() // 初始计算一次
  }, 100)

  try {
    // 先加载基础数据
    await Promise.all([loadHome(), loadBanners()])

    // 然后加载推荐数据
    await loadRecommendedPlaylists()
  } catch (e) {
    console.error(e)
    ElMessage.error("首页加载失败：检查接口/网络/拦截器")
  }
})

async function loadRecommendedPlaylists() {
  try {
    const res = await getRecommendPlaylistsWithFilter({
      userId: userStore.userId,
      size: 8
    }, {
      type: "personalized"  // 自定义过滤参数
    })

    const recommendedPlaylistIds = pickData(res) || []
    if (recommendedPlaylistIds.length > 0) {
      // 这里可以根据推荐的歌单ID获取详细信息
      // 或者直接使用推荐结果
    }
  } catch (e) {
    console.error("推荐加载失败:", e)
  }
}

onBeforeUnmount(() => {
  window.removeEventListener("scroll", onScroll)
})
</script>

<style scoped>
/* 全屏浅灰 + 内容居中 */
.home-page{
  min-height:100vh;
  padding-bottom: 0;
  background: #fff;
  overflow-y: hidden; /* 隐藏垂直滚动条 */
}
.home-inner{
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 82px;
}
.h-scroll-inner::-webkit-scrollbar {
  display: none; /* 隐藏滚动条 */
}


/* Section */
.section{
  margin-top: -20px;
  padding: 34px 0 0;
  background: #fff;
  transition: background .3s ease;
}

/* Banner */
.hero{
  margin-top: 0;
  padding: 0 0 0 0;
  will-change: transform, opacity;
  overflow: hidden;
  background: #ffffff;
  z-index: 1;
  position: relative;
  font-size: 0;
  line-height: 0;
}
.hero-slide{ position: relative; height: 520px; margin: 0; overflow: hidden; }
.banner-img{
  width:100%;
  height:100%;
  object-fit: cover;
  display:block;
  filter: saturate(1.1) contrast(1.05) brightness(1.02);
  transition: transform .6s ease;
  margin: 0;
  vertical-align: top;
}
.hero-slide:hover .banner-img{
  transform: scale(1.03);
}
.hero-mask{
  position:absolute;
  inset:0;
  background: linear-gradient(to bottom, rgba(0,0,0,.05), rgba(0,0,0,.2));
  pointer-events:none;
}

/* 轮播图指示器样式 */
:deep(.el-carousel__indicators){
  bottom: 20px;
}
:deep(.el-carousel__indicator){
  margin: 0 6px !important;
}
:deep(.el-carousel__indicator button){
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: rgba(255,255,255,.5);
  transition: all .3s ease;
}
:deep(.el-carousel__indicator.is-active button){
  width: 24px;
  border-radius: 4px;
  background: #fff;
}

/* Banner 箭头：hover 才出现 */
:deep(.el-carousel__arrow){
  opacity: 0;
  pointer-events: none;
  transition: opacity .18s ease, transform .18s ease, background .18s ease;
  transform: translateY(-50%) scale(.95);
  border-radius: 50% !important;
  width: 60px;
  height: 60px;
  background: rgba(0,0,0,.3);
  color:#fff;
  box-shadow: 0 2px 8px rgba(0,0,0,.2);
  font-size: 24px;
}
:deep(.el-carousel:hover .el-carousel__arrow){
  opacity: 1;
  pointer-events: auto;
  transform: translateY(-50%) scale(1);
}
:deep(.el-carousel__arrow:hover){
  background: rgba(0,0,0,.5);
  transform: translateY(-50%) scale(1.05);
}


.section-head{
  display:flex;
  align-items:center;
  justify-content:space-between;
  margin-bottom: 10px;
}
.section-head.center{ justify-content:center; }
.title.big{
  font-size: 30px;
  font-weight: 900;
  letter-spacing: 8px;
  color:#222;
  text-align:center;
}
.actions{ display:flex; gap:10px; }

/* tabs */
.tabs{
  display:flex;
  gap: 40px;
  padding: 8px 0 12px;
  overflow-x:auto;
}

.tabs.center{ justify-content:center; }
.tab{
  cursor:pointer;
  padding: 10px 6px;
  color:#666;
  font-size: 15px;
  position: relative;
  white-space: nowrap;
  transition: color .2s ease;
}

.tab:hover{ color:#1db954; }
.tab.active{
  color:#1db954;
  font-weight: 800;
}


/* 轨道更宽：优化你图2红框 */
.rail{
  max-width: 1400px;
  margin: 0 auto;
  padding: 0;
  overflow: visible; /* ✅ 让箭头能跑到外侧 */
  position: relative;
}

.h-scroll{
  position: relative;
  margin-top: 16px;
  overflow: visible; /* ✅ 同样保险 */
}

.h-scroll-inner{
  display:flex;
  gap: 20px;
  overflow-x:auto;
  padding: 6px 2px 12px;
  scroll-behavior: smooth;
}

/* 只修改热门专辑部分 */
.section:nth-of-type(3) .h-scroll-inner {
  display: flex;
  flex-wrap: wrap;  /* 允许换行 */
  gap: 20px;  /* 设置间隙 */
  overflow: hidden;  /* 隐藏多余内容 */
}
/* 增加歌单推荐模块的上边距 */

.section:nth-of-type(3) {
  margin-top: 0;  /* 调整这个值，确保不被上面模块遮挡 */
}

.section:nth-of-type(4) {
  margin-top: 0;  /* 调整这个值，确保不被上面模块遮挡 */
}
.section:nth-of-type(5) {
  margin-top: 0;  /* 调整这个值，确保不被上面模块遮挡 */
}




.section:nth-of-type(3) .cover-card {
  width: calc(20% - 16px);  /* 每行显示 5 个，减去间隙的宽度 */
  margin-bottom: 20px;  /* 设置垂直间隙 */
}


.section:nth-of-type(3) .rail {
  max-height: 850px;  /* 限制最大高度，显示两行 */
  overflow: hidden;  /* 隐藏超出部分 */
}


.h-scroll-inner::-webkit-scrollbar{ height: 6px; }
.h-scroll-inner::-webkit-scrollbar-track{ background:#ececec; }
.h-scroll-inner::-webkit-scrollbar-thumb{ background:#cfcfcf; }

/* 封面更大 */
.cover-card{
  width: 240px;
  cursor:pointer;
  transition: transform .18s ease;
}
.cover-card:hover{ transform: translateY(-3px); }

.cover{
  width: 227px;
  height: 227px;
  background:#e9e9e9;
  overflow:hidden;
  position: relative;
}
.cover img{
  width:100%;
  height:100%;
  object-fit: cover;
  transition: transform .25s ease;
  cursor: pointer;
}
.cover-card:hover .cover img{ transform: scale(1.04); }

.cover-title{
  margin-top: 10px;
  font-weight: 800;
  color:#111;
  font-size: 14px;
  overflow:hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.cover-sub{
  margin-top: 6px;
  color:#888;
  font-size: 14px;
}

/* hover 遮罩 + QQ风白色圆形按钮 */
.overlay {
  position: absolute;
  inset: 0;
  display: flex;
  justify-content: center;
  align-items: center;
  opacity: 0;
  background: rgba(0,0,0,.30);
  transition: opacity .6s ease;
}

.cover:hover .overlay {
  opacity: 1;

}

.play-btn {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  border: none;
  background: #fff;
  color: #111;
  font-size: 22px;
  cursor: pointer;
  opacity: 0;
  transform: scale(0.4);
  transition: all .6s ease;
}

.cover:hover .play-btn{
  opacity: 1;
  transform: scale(1.08);
  box-shadow: 0 14px 28px rgba(0,0,0,.24);
}
.play-btn.sm{
  width: 34px;
  height: 34px;
  font-size: 13px;
  opacity: 0;
  transform: scale(0.8);
  transition: all .3s ease;
}

.cover:hover .play-btn.sm{
  opacity: 1;
  transform: scale(1.08);
  box-shadow: 0 14px 28px rgba(0,0,0,.24);
}




/* 新歌 */
.song-actions
{
  margin: 12px auto 0;
  padding: 0 18px;
}
.song-grid{
  margin: 18px auto 0;
  padding: 0 0 40px 18px;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  column-gap: 42px;
  row-gap: 8px;
}
.song-row{
  display:flex;
  align-items:center;
  gap: 14px;
  padding: 12px 0;
  border-bottom: 1px solid #eaeaea;
  transition: background .15s ease;
  cursor: pointer;
}
.song-row:hover{ background: rgba(255,255,255,.55); }

.song-cover{
  width: 80px;
  height: 80px;
  background:#e9e9e9;
  overflow:hidden;
  flex: 0 0 auto;
  position: relative;
}
.song-cover img{ width:100%; height:100%; object-fit: cover; }
.overlay.small{ background: rgba(0,0,0,.16); }

.song-main{ flex: 1; min-width: 0; }
.song-name{
  font-size: 14px;
  font-weight: 900;
  color:#111;
  overflow:hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}
.song-singer{
  margin-top: 4px;
  font-size: 14px;
  color:#888;
  overflow:hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.song-name.link{
  cursor: pointer;
}
.song-name.link:hover{
  text-decoration: underline;
}

.song-singer.link2{
  cursor: pointer;
}
.song-singer.link2:hover{
  text-decoration: underline;
}

.song-duration{
  width: 54px;
  text-align:right;
  font-size: 12px;
  color:#999;
}
.empty{ text-align:center; color:#999; padding: 20px 0; }

/* 响应式 */
@media (max-width: 1100px){
  .song-grid{ grid-template-columns: repeat(2, 1fr); }
}
@media (max-width: 720px){
  .song-grid{ grid-template-columns: 1fr; }
  .title.big{ letter-spacing: 6px; }
}

/* ElementPlus 直角 */
:deep(.el-button){ border-radius: 0 !important; }
:deep(.el-carousel__container){ border-radius: 0 !important; }
:deep(.el-carousel){ margin: 0 !important; }
:deep(.el-carousel__item){ margin: 0 !important; }
</style>
