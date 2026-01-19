<template>
  <div class="profile-page">
    <!-- 顶部封面（全宽 100vw） -->
    <section class="cover-wrap">
      <div class="cover" :style="coverStyle"></div>
      <div class="cover-mask"></div>

      <!-- 中心信息 -->
      <div class="center-box">
        <div class="avatar">
          <img :src="avatarUrl" alt="avatar" />
        </div>

        <div class="name-row">
          <div class="name">{{ user.nickname || user.username || "用户" }}</div>
          <span v-if="user.role" class="vip">官方</span>
          <span v-else class="vip">普通用户</span>
        </div>


        <!-- 只展示关注 / 粉丝 -->
        <div class="ff">
          <div class="ff-item">
            <div class="num">{{ followCount }}</div>
            <div class="label">关注</div>
          </div>
          <div class="ff-line"></div>
          <div class="ff-item">
            <div class="num">{{ fanCount }}</div>
            <div class="label">粉丝</div>
          </div>
        </div>
      </div>

      <!-- 封面底部导航（像图2） -->
      <div class="nav-bar">
        <div class="nav-container">
          <div
              class="nav-item"
              :class="{ active: activeTab === 'likes' }"
              @click="switchTab('likes')"
          >我喜欢</div>

          <div
              class="nav-item"
              :class="{ active: activeTab === 'created' }"
              @click="switchTab('created')"
          >创建的歌单</div>

          <div
              class="nav-item"
              :class="{ active: activeTab === 'follow' }"
              @click="switchTab('follow')"
          >关注</div>

          <div
              class="nav-item"
              :class="{ active: activeTab === 'fans' }"
              @click="switchTab('fans')"
          >粉丝</div>

          <div
              class="nav-item"
              :class="{ active: activeTab === 'videos' }"
              @click="switchTab('videos')"
          >我上传的视频</div>

          <div
              class="nav-item"
              :class="{ active: activeTab === 'programs' }"
              @click="switchTab('programs')"
          >主播节目</div>
        </div>
      </div>
    </section>

    <!-- 操作按钮和统计标签 -->
    <div v-if="activeTab === 'likes'" class="likes-header">
      <div class="likes-tabs">
        <span class="likes-tab" :class="{ active: likesSubTab === 'songs' }" @click="switchLikesSubTab('songs')">歌曲 {{ likeSongCount }}</span>
        <span class="likes-tab" :class="{ active: likesSubTab === 'playlists' }" @click="switchLikesSubTab('playlists')">歌单 {{ likePlaylistCount }}</span>
        <span class="likes-tab" :class="{ active: likesSubTab === 'albums' }" @click="switchLikesSubTab('albums')">专辑 {{ likeAlbumCount }}</span>
        <span class="likes-tab disabled">视频 0</span>
      </div>

      <div class="likes-actions">
        <button v-if="likesSubTab === 'songs'" class="btn green" :disabled="songs.length === 0" @click="playAll">播放全部</button>
        <button v-if="likesSubTab === 'songs'" class="btn" :disabled="songs.length === 0" @click="addAllTo">添加到</button>
        <button v-if="likesSubTab === 'songs'" class="btn" :disabled="songs.length === 0" @click="downloadAll">下载</button>
        <button v-if="likesSubTab === 'songs'" class="btn" :disabled="songs.length === 0" @click="batchOp">批量操作</button>
      </div>
    </div>

    <!-- 下方内容区 -->
    <section class="content">
      <!-- 我喜欢：展示歌曲表格 -->
      <div v-if="activeTab === 'likes'" class="likes">

        <!-- 歌曲列表 -->
        <div v-if="likesSubTab === 'songs'" class="table-wrap" v-loading="loading">
          <table class="song-table">
            <thead>
            <tr>
              <th class="col-idx"></th>
              <th class="col-song">歌曲</th>
              <th class="col-singer">歌手</th>
              <th class="col-album">专辑</th>
              <th class="col-time">时长</th>
            </tr>
            </thead>
            <tbody>
            <tr v-for="(row, i) in songs" :key="row.id">
              <td class="col-idx">{{ i + 1 }}</td>

              <td class="col-song">
                <span class="link" @click="goSong(row.id)">
                  {{ row.songName }}
                </span>
                <span v-if="row.vip" class="tag">VIP</span>
              </td>

              <!-- 点击歌手进入歌手详情 -->
              <td class="col-singer">
                <span class="link" @click="goSinger(row.singerId)">{{ row.singerName || "-" }}</span>
              </td>

              <!-- 点击专辑进专辑详情（你已有 AlbumDetail.vue） -->
              <td class="col-album">
                  <span class="link" @click="goAlbum(row.albumId)">
                    {{ row.albumName || "-" }}
                  </span>
              </td>

              <td class="col-time">{{ formatDuration(row.durationSeconds) }}</td>
            </tr>

            <tr v-if="!loading && songs.length === 0">
              <td colspan="5" class="empty">暂无喜欢的歌曲</td>
            </tr>
            </tbody>
          </table>
        </div>

        <!-- 歌单列表 -->
        <div v-else-if="likesSubTab === 'playlists'" class="table-wrap" v-loading="loading">
          <div class="playlist-grid">
            <div class="playlist-card" v-for="(playlist, index) in playlists" :key="playlist.id" @click="goPlaylistDetail(playlist.id)">
              <div class="playlist-cover">
                <img :src="playlist.coverUrl" alt="{{ playlist.name }}">
                <div class="playlist-overlay">
                  <button class="play-btn" @click.stop="playPlaylistHandler(playlist.id)">▶</button>
                </div>
              </div>
              <div class="playlist-info">
                <div class="playlist-name">{{ playlist.name }}</div>
                <div class="playlist-meta">播放量：{{ playlist.playCount || 0 }} · 收藏数：{{ playlist.collectCount || 0 }}</div>
              </div>
            </div>
            <div v-if="!loading && playlists.length === 0" class="empty">暂无收藏的歌单</div>
          </div>
        </div>

        <!-- 专辑列表 -->
        <div v-else-if="likesSubTab === 'albums'" class="table-wrap" v-loading="loading">
          <div class="album-grid">
            <div class="album-card" v-for="(album, index) in albums" :key="album.id" @click="goAlbum(album.id)">
              <div class="album-cover">
                <img :src="album.coverUrl" alt="{{ album.albumName }}">
                <div class="album-overlay">
                  <button class="play-btn" @click.stop="playAlbumHandler(album.id)">▶</button>
                </div>
              </div>
              <div class="album-info">
                <div class="album-name">{{ album.albumName }}</div>
                <div class="album-meta">{{ album.singerName }} · 收藏数：{{ album.collectCount || 0 }}</div>
              </div>
            </div>
            <div v-if="!loading && albums.length === 0" class="empty">暂无收藏的专辑</div>
          </div>
        </div>
      </div>

      <!-- 我创建的歌单 -->
      <div v-else-if="activeTab === 'created'" class="created-playlists" v-loading="loading">
        <!-- 操作按钮 -->
        <div v-if="route.query.type !== 'official'" class="created-actions">
          <button class="btn green" @click="showCreatePlaylistDialog">+ 新建歌单</button>
          <button class="btn" @click="importPlaylist">导入歌单</button>
          <button class="btn" @click="restorePlaylist">恢复歌单</button>
        </div>
        
        <!-- 歌单列表 -->
        <div class="playlist-list">
          <div class="playlist-item" v-for="(playlist, index) in createdPlaylists" :key="playlist.id" @click="goPlaylistDetail(playlist.id)">
            <div class="playlist-cover">
              <img :src="playlist.coverUrl" alt="{{ playlist.name }}">
            </div>
            <div class="playlist-info">
              <div class="playlist-name">{{ playlist.name }}</div>
              <div class="playlist-meta">
                <span>{{ playlist.songCount || 0 }}首歌曲</span>
                <span>{{ playlist.playCount || 0 }}次播放</span>
              </div>
            </div>
            <div class="playlist-actions">
              <button class="play-btn" @click.stop="playPlaylistHandler(playlist.id)">▶</button>
            </div>
          </div>
          <div v-if="!loading && createdPlaylists.length === 0" class="empty">暂无创建的歌单</div>
        </div>
      </div>

      <!-- 主播节目 -->
      <div v-else-if="activeTab === 'programs'" class="programs" v-loading="loading">
        <div class="programs-list">
          <div v-if="!loading && programs.length === 0" class="empty">暂无主播节目</div>
        </div>
      </div>

      <!-- 其他 Tab：先占位 -->
      <div v-else class="placeholder">
        <div class="ph-title">{{ tabTitle }}</div>
        <div class="ph-sub">这里你后面接接口（先把 UI 跑起来）</div>
      </div>
    </section>

    <!-- 创建歌单弹窗 -->
    <div v-if="createPlaylistDialogVisible" class="dialog-overlay" @click="closeCreatePlaylistDialog">
      <div class="dialog-content" @click.stop>
        <div class="dialog-header">
          <h3>创建新歌单</h3>
          <button class="close-btn" @click="closeCreatePlaylistDialog">×</button>
        </div>
        <div class="dialog-body">
          <div class="form-item">
            <label>歌单名</label>
            <div class="input-group">
              <input type="text" v-model="playlistName" maxlength="20" placeholder="请输入歌单名称">
              <span class="char-count">{{ playlistName.length }}/20</span>
            </div>
          </div>
        </div>
        <div class="dialog-footer">
          <button class="btn" @click="closeCreatePlaylistDialog">取消</button>
          <button class="btn green" @click="submitCreatePlaylist" :disabled="!playlistName.trim()">确定</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from "vue"
import { ElMessage } from "element-plus"
import { useRouter, useRoute } from "vue-router"
import { useUserStore } from "@/store/user.js"
import { getMe, getUserInfo, getStats, getUserStats, getLikedSongs, getUserLikedSongs, getLikedPlaylists, getUserLikedPlaylists, getLikedAlbums, getUserLikedAlbums, getMyPlaylists, getUserPlaylists } from "@/api/app/user"
import { playPlaylist, createPlaylist, getOfficialPlaylists } from "@/api/app/playlist"

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const activeTab = ref("likes")
const likesSubTab = ref("songs")
const loading = ref(false)

const currentUser = ref({})
const user = computed(() => currentUser.value || userStore.user || {})

const followCount = ref(0)
const fanCount = ref(0)
const likeSongCount = ref(0)
const likePlaylistCount = ref(0)
const likeAlbumCount = ref(0)

const songs = ref([])
const playlists = ref([])
const albums = ref([])
const createdPlaylists = ref([])
const programs = ref([])

// 创建歌单弹窗
const createPlaylistDialogVisible = ref(false)
const playlistName = ref('')

// 头像
function defaultAvatar(seed) {
  const s = encodeURIComponent(seed || "user")
  return `https://api.dicebear.com/7.x/identicon/svg?seed=${s}`
}
const avatarUrl = computed(() => {
  const u = user.value
  return u.avatarUrl || defaultAvatar(u.username || u.nickname || "user")
})

// 封面（你换成自己的背景也行）
const DEFAULT_COVER =
    "https://images.unsplash.com/photo-1444703686981-a3abbc4d4fe3?auto=format&fit=crop&w=2400&q=80"


const coverStyle = computed(() => {
  const u = user.value
  const url = u?.coverUrl || DEFAULT_COVER

  return {
    backgroundImage: `url('${url}')`,
  }
})


const tabTitle = computed(() => {
  const map = {
    likes: "我喜欢",
    bought: "我的已购",
    created: "我创建的歌单",
    follow: "关注",
    fans: "粉丝",
    videos: "我上传的视频",
  }
  return map[activeTab.value] || ""
})

function switchTab(t) {
  activeTab.value = t
  if (t === "likes") {
    likesSubTab.value = "songs"
    fetchLikedSongs()
  } else if (t === "created") {
    fetchCreatedPlaylists()
  } else if (t === "programs") {
    // 加载主播节目数据
    fetchPrograms()
  }
}

function switchLikesSubTab(subTab) {
  likesSubTab.value = subTab
  if (subTab === "songs") {
    fetchLikedSongs()
  } else if (subTab === "playlists") {
    fetchLikedPlaylists()
  } else if (subTab === "albums") {
    fetchLikedAlbums()
  }
}

// ========== 跳转（你路由已有 singers/:id albums/:id player） ==========
function goSinger(id) {
  if (!id) return
  router.push(`/app/singers/${id}`)
}
function goAlbum(id) {
  if (!id) return
  router.push(`/app/albums/${id}`)
}
function goSong(id) {
  if (!id) return
  router.push(`/app/songs/${id}`)
}

function goPlaylistDetail(id) {
  if (!id) return
  router.push(`/app/playlists/${id}`)
}

// ========== 操作按钮 ==========
function playAll() {
  ElMessage.success("播放全部（后面接播放器队列）")
}
function addAllTo() {
  ElMessage.info("添加到（后面做弹窗选择歌单）")
}
function downloadAll() {
  ElMessage.info("下载（后面做下载逻辑）")
}
function batchOp() {
  ElMessage.info("批量操作（后面加勾选）")
}

// ========== 创建歌单相关操作 ==========
function showCreatePlaylistDialog() {
  playlistName.value = ''
  createPlaylistDialogVisible.value = true
}
function closeCreatePlaylistDialog() {
  createPlaylistDialogVisible.value = false
  playlistName.value = ''
}
async function submitCreatePlaylist() {
  if (!playlistName.value.trim()) {
    ElMessage.warning('请输入歌单名称')
    return
  }
  
  try {
    const res = await createPlaylist({
      name: playlistName.value.trim(),
      description: '',
      coverUrl: '',
      songIds: []
    })
    
    if (res?.data) {
      ElMessage.success('歌单创建成功')
      closeCreatePlaylistDialog()
      // 重新加载创建的歌单列表
      await fetchCreatedPlaylists()
    } else {
      ElMessage.error('歌单创建失败')
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('歌单创建失败')
  }
}
function importPlaylist() {
  ElMessage.info("导入歌单（后面做弹窗）")
}
function restorePlaylist() {
  ElMessage.info("恢复歌单（后面做弹窗）")
}

async function playPlaylistHandler(id) {
  try {
    await playPlaylist(id)
    ElMessage.success("开始播放歌单")
    await router.push({path: "/app/player", query: {playlistId: id}})
  } catch (e) {
    console.error(e)
    ElMessage.error("播放歌单失败")
  }
}

// ========== 数据 ==========
function formatDuration(sec) {
  if (sec == null) return "-"
  const n = Number(sec)
  if (Number.isNaN(n)) return "-"
  const m = Math.floor(n / 60)
  const s = n % 60
  return `${String(m).padStart(2, "0")}:${String(s).padStart(2, "0")}`
}

function pickList(res) {
  if (!res) return []
  if (Array.isArray(res)) return res
  if (Array.isArray(res.list)) return res.list
  if (Array.isArray(res.records)) return res.records
  if (Array.isArray(res.data)) return res.data
  return []
}

async function fetchProfileAndCounts() {
  loading.value = true
  try {
    const userId = route.query.id
    const userType = route.query.type
    
    if (userType === 'official') {
      // 显示官方个人信息
      currentUser.value = {
        id: 0,
        username: 'official',
        nickname: 'Harmony官方歌单',
        avatarUrl: 'http://localhost:8080/uploads/images/ae96ff42b31c4b4193a25d4aa554ad68.jpg',
        coverUrl: 'https://y.qq.com/ryqq/static/media/bg_profile.bd497b5a.jpg?max_age=2592000',
        isOfficial: 1
      }
      
      // 官方统计信息
      followCount.value = 0
      fanCount.value = 0
      likeSongCount.value = 0
      likePlaylistCount.value = 0
      likeAlbumCount.value = 0
    } else if (userId && userId !== userStore.userId) {
      // 加载指定用户的信息
      const userRes = await getUserInfo(userId)
      const userInfo = userRes.data || {}
      currentUser.value = userInfo

      // 加载指定用户的统计信息
      const stRes = await getUserStats(userId)
      const st = stRes.data || {}
      followCount.value = st.followCount ?? 0
      fanCount.value = st.fanCount ?? 0
      likeSongCount.value = st.likeSongCount ?? 0
      likePlaylistCount.value = st.likePlaylistCount ?? 0
      likeAlbumCount.value = st.likeAlbumCount ?? 0
    } else {
      // 加载当前登录用户的信息
      const meRes = await getMe()
      const me = meRes.data || {}
      userStore.user = me
      localStorage.setItem("USER_INFO", JSON.stringify(me))
      currentUser.value = me

      // 加载当前登录用户的统计信息
      const stRes = await getStats()
      const st = stRes.data || {}
      followCount.value = st.followCount ?? 0
      fanCount.value = st.fanCount ?? 0
      likeSongCount.value = st.likeSongCount ?? 0
      likePlaylistCount.value = st.likePlaylistCount ?? 0
      likeAlbumCount.value = st.likeAlbumCount ?? 0
    }
  } catch (e) {
    console.error(e)
    ElMessage.error("加载个人信息失败（检查 /app/user/me /app/user/stats）")
  } finally {
    loading.value = false
  }
}

async function fetchLikedSongs() {
  loading.value = true
  try {
    const userId = route.query.id
    const userType = route.query.type
    let res
    if (userType === 'official') {
      // 官方用户不显示收藏歌曲
      songs.value = []
      likeSongCount.value = 0
    } else if (userId && userId !== userStore.userId) {
      // 加载指定用户的收藏歌曲
      res = await getUserLikedSongs(userId, "", 500)

      const list = pickList(res.data)

      // 这里按你后端 SongListVo/SongVo 字段兼容一下
      songs.value = (list || []).map((x) => ({
        id: x.id,
        songName: x.songName || x.name,
        singerId: x.singerId,
        singerName: x.singerName || x.artist,
        albumId: x.albumId,
        albumName: x.albumName || x.album,
        durationSeconds: x.durationSeconds,
        vip: x.vip || false,
      }))

      likeSongCount.value = songs.value.length
    } else {
      // 加载当前登录用户的收藏歌曲
      res = await getLikedSongs("", 500)

      const list = pickList(res.data)

      // 这里按你后端 SongListVo/SongVo 字段兼容一下
      songs.value = (list || []).map((x) => ({
        id: x.id,
        songName: x.songName || x.name,
        singerId: x.singerId,
        singerName: x.singerName || x.artist,
        albumId: x.albumId,
        albumName: x.albumName || x.album,
        durationSeconds: x.durationSeconds,
        vip: x.vip || false,
      }))

      likeSongCount.value = songs.value.length
    }
  } catch (e) {
    console.error(e)
    ElMessage.error("加载我喜欢失败（检查 /app/user/liked-songs）")
  } finally {
    loading.value = false
  }
}

async function fetchLikedPlaylists() {
  loading.value = true
  try {
    const userId = route.query.id
    const userType = route.query.type
    let res
    if (userType === 'official') {
      // 官方用户不显示收藏歌单
      playlists.value = []
      likePlaylistCount.value = 0
    } else if (userId && userId !== userStore.userId) {
      // 加载指定用户的收藏歌单
      res = await getUserLikedPlaylists(userId, "", 500)

      const list = pickList(res.data)

      // 这里按你后端 PlaylistVo 字段兼容一下
      playlists.value = (list || []).map((x) => ({
        id: x.id,
        name: x.name || x.playlistName,
        coverUrl: x.coverUrl,
        playCount: x.playCount,
        collectCount: x.collectCount,
      }))

      likePlaylistCount.value = playlists.value.length
    } else {
      // 加载当前登录用户的收藏歌单
      res = await getLikedPlaylists("", 500)

      const list = pickList(res.data)

      // 这里按你后端 PlaylistVo 字段兼容一下
      playlists.value = (list || []).map((x) => ({
        id: x.id,
        name: x.name || x.playlistName,
        coverUrl: x.coverUrl,
        playCount: x.playCount,
        collectCount: x.collectCount,
      }))

      likePlaylistCount.value = playlists.value.length
    }
  } catch (e) {
    console.error(e)
    ElMessage.error("加载收藏歌单失败（检查 /app/user/liked-playlists）")
  } finally {
    loading.value = false
  }
}

async function fetchLikedAlbums() {
  loading.value = true
  try {
    const userId = route.query.id
    const userType = route.query.type
    let res
    if (userType === 'official') {
      // 官方用户不显示收藏专辑
      albums.value = []
      likeAlbumCount.value = 0
    } else if (userId && userId !== userStore.userId) {
      // 加载指定用户的收藏专辑
      res = await getUserLikedAlbums(userId, "", 500)

      const list = pickList(res.data)

      // 这里按你后端 AlbumVo 字段兼容一下
      albums.value = (list || []).map((x) => ({
        id: x.id,
        albumName: x.albumName || x.name,
        singerName: x.singerName || x.artist,
        coverUrl: x.coverUrl,
        collectCount: x.collectCount,
      }))

      likeAlbumCount.value = albums.value.length
    } else {
      // 加载当前登录用户的收藏专辑
      res = await getLikedAlbums("", 500)

      const list = pickList(res.data)

      // 这里按你后端 AlbumVo 字段兼容一下
      albums.value = (list || []).map((x) => ({
        id: x.id,
        albumName: x.albumName || x.name,
        singerName: x.singerName || x.artist,
        coverUrl: x.coverUrl,
        collectCount: x.collectCount,
      }))

      likeAlbumCount.value = albums.value.length
    }
  } catch (e) {
    console.error(e)
    ElMessage.error("加载收藏专辑失败（检查 /app/user/liked-albums）")
  } finally {
    loading.value = false
  }
}

async function fetchCreatedPlaylists() {
  loading.value = true
  try {
    const userId = route.query.id
    const userType = route.query.type
    let res
    if (userType === 'official') {
      // 加载官方歌单
      res = await getOfficialPlaylists({ size: 20 })
      const list = pickList(res.data)

      // 这里按你后端 PlaylistHomeVo 字段兼容一下
      createdPlaylists.value = (list || []).map((x) => ({
        id: x.id,
        name: x.name,
        coverUrl: x.coverUrl,
        songCount: 0, // 官方歌单暂时不显示歌曲数量
        playCount: x.collectCount || 0 // 用收藏数作为播放量展示
      }))
    } else if (userId && userId !== userStore.userId) {
      // 加载指定用户的歌单
      res = await getUserPlaylists(userId)
      const list = pickList(res.data)

      // 这里按你后端 PlaylistVo 字段兼容一下
      createdPlaylists.value = (list || []).map((x) => ({
        id: x.id,
        name: x.name,
        coverUrl: x.coverUrl,
        songCount: x.songCount || x.songCount,
        playCount: x.playCount || x.playCount
      }))
    } else {
      // 加载当前登录用户的歌单
      res = await getMyPlaylists()
      const list = pickList(res.data)

      // 这里按你后端 PlaylistVo 字段兼容一下
      createdPlaylists.value = (list || []).map((x) => ({
        id: x.id,
        name: x.name,
        coverUrl: x.coverUrl,
        songCount: x.songCount || x.songCount,
        playCount: x.playCount || x.playCount
      }))
    }
  } catch (e) {
    console.error(e)
    ElMessage.error("加载创建歌单失败")
  } finally {
    loading.value = false
  }
}

async function fetchPrograms() {
  loading.value = true
  try {
    const userType = route.query.type
    if (userType === 'official') {
      // 官方用户暂时不显示主播节目
      programs.value = []
    } else {
      // 加载用户的主播节目
      // 这里可以根据实际情况修改，例如从后端获取主播节目
      programs.value = []
    }
  } catch (e) {
    console.error(e)
    ElMessage.error("加载主播节目失败")
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  userStore.hydrate()
  await fetchProfileAndCounts()
  await fetchLikedSongs()
  await fetchLikedPlaylists()
  await fetchLikedAlbums()
  await fetchCreatedPlaylists()
})

// 监听路由参数变化
watch(
  () => [route.query.id, route.query.type],
  async (newVal, oldVal) => {
    await fetchProfileAndCounts()
    await fetchCreatedPlaylists()
    await fetchLikedSongs()
    await fetchLikedPlaylists()
    await fetchLikedAlbums()
  }
)
</script>

<style scoped>
.profile-page{
  width: 100%;
  background: #fff;
  margin: 0;
  padding: 0;
}

/* ========== 封面全宽铺满（关键：100vw + 向左拉回） ========== */
.cover-wrap{
  position: relative;
  width: 100vw;
  height: 380px;
  margin-left: calc(50% - 50vw);
  overflow: hidden;
  margin-bottom: 0;
}

.cover{
  position: absolute;
  inset: 0;
  background-size: cover;
  background-position: center;
  transform: scale(1.03);
  filter: saturate(1.02);
}
.cover-mask{
  position: absolute;
  inset: 0;
  background: radial-gradient(1000px 520px at 50% 40%, rgba(0,0,0,0.1), rgba(0,0,0,.1));
}

.center-box{
  position: relative;
  z-index: 2;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #fff;
  padding-top: 0;
  transform: translateY(-20px);
}

.avatar{
  width: 120px;
  height: 120px;
  border-radius: 50%;
  overflow: hidden;
  border: 3px solid rgba(255,255,255,.92);
  box-shadow: 0 4px 12px rgba(0,0,0,.3);
}
.avatar img{
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.name-row{
  margin-top: 15px;
  display:flex;
  align-items:center;
  gap: 8px;
}
.name{
  font-size: 28px;
  padding: 0 10px;
  font-weight: 600;
  letter-spacing: .4px;
}
.vip{
  font-size: 12px;
  font-weight: 800;
  padding: 2px 8px;
  border-radius: 999px;
  background: rgba(255, 210, 90, .92);
  color: #1b1b1b;
}

.ff{
  margin-top: 10px;
  display:flex;
  align-items:center;
  gap: 15px;
}
.ff-item{
  text-align:center;
  min-width: 60px;
}
.ff-item .num{
  font-size: 20px;
  font-weight: 800;
  line-height: 1;
  color: #fff;
}
.ff-item .label{
  margin-top: 2px;
  font-size: 12px;
  font-weight: 700;
  opacity: .95;
  color: #fff;
  display: block;
}
.ff-line{
  width: 1px;
  height: 25px;
  background: rgba(255,255,255,.35);
}

.nav-bar{
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  height: 54px;
  z-index: 3;
  background: rgba(0,0,0,.1);
  backdrop-filter: blur(10px);
}
.nav-container{
  max-width: 1200px;
  margin: 0 auto;
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: flex-start;
  gap: 24px;
  padding: 0 20px;
  height: 100%;
}
.nav-item{
  color: rgba(255,255,255,.92);
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  padding: 8px 10px;
  position: relative;
}
.nav-item:hover{
  color: #27c27a;
}
.nav-item.active{
  color: #27c27a;
}
.nav-item.active::after{
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 2px;
  background: #27c27a;
}

/* 操作按钮和统计标签 */
.likes-header{
  max-width: 1200px;
  margin: 0 auto;
  padding: 18px 20px;
  display:flex;
  flex-direction: column;
  align-items:flex-start;
  gap: 16px;
  background: #fff;
  border-bottom: 1px solid #eee;
}

.likes-tabs{
  display:flex;
  gap: 20px;
  font-size: 14px;
  font-weight: 700;
}
.likes-tab{
  cursor: pointer;
  color: #666;
  padding: 8px 0;
}
.likes-tab.active{
  color:#27c27a;
  border-bottom: 2px solid #27c27a;
}
.likes-tab.disabled{
  color:#aaa;
  font-weight: 700;
}

.likes-actions{
  display:flex;
  gap: 10px;
}
.btn{
  height: 36px;
  padding: 0 16px; /* ✅ 14px + 2px */
  border: 1px solid #dcdcdc;
  background: #fff;
  cursor: pointer;
  font-weight: 600;
  font-size: 14px;
  transition: all 0.2s;
}
.btn:hover{
  border-color: #27c27a;
  color: #27c27a;
}
.btn.green{
  background: #27c27a;
  border-color: #27c27a;
  color: #fff;
  display: flex;
  align-items: center;
  gap: 6px;
}
.btn.green:hover{
  background: #22a86b;
  border-color: #22a86b;
}
.btn:disabled{
  opacity: .45;
  cursor: not-allowed;
}

/* ========== 下方内容区 ========== */
.content{
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px 60px;
}


.table-wrap{
  padding-top: 14px;
}
.song-table{
  width: 100%;
  border-collapse: collapse;
}
.song-table thead th{
  text-align:left;
  font-size: 14px;
  color:#999;
  font-weight: 700;
  padding: 14px 0;
}
.song-table tbody td{
  padding: 18px 0;
  border-top: 1px solid #f1f1f1;
  font-size: 15px;
  color:#222;
}
.song-table tbody tr:hover{
  background: #fafafa;
}

.col-idx{ width: 70px; color:#999; }
.col-time{ width: 120px; text-align: right; color:#999; }
.col-singer{ width: 220px; }
.col-album{ width: 320px; }

.link{
  cursor: pointer;
}
.link:hover{
  color:#27c27a;
}

.tag{
  margin-left: 10px;
  font-size: 12px;
  font-weight: 800;
  padding: 2px 8px;
  border: 1px solid rgba(39,194,122,.6);
  color:#27c27a;
}

.empty{
  text-align:center;
  padding: 40px 0;
  color:#999;
}

/* 歌单列表样式 */
.playlist-grid{
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 20px;
  padding: 20px 0;
}

.playlist-card{
  cursor: pointer;
  transition: transform 0.2s ease;
}

.playlist-card:hover{
  transform: translateY(-4px);
}

.playlist-cover{
  position: relative;
  width: 100%;
  padding-top: 100%; /* 1:1 宽高比 */
  overflow: hidden;
  border-radius: 8px;
  background: #f1f1f1;
}

.playlist-cover img{
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.playlist-card:hover .playlist-cover img{
  transform: scale(1.05);
}

.playlist-overlay{
  position: absolute;
  inset: 0;
  background: rgba(0,0,0,0.3);
  display: flex;
  justify-content: center;
  align-items: center;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.playlist-card:hover .playlist-overlay{
  opacity: 1;
}

.playlist-cover .play-btn{
  width: 50px;
  height: 50px;
  border-radius: 50%;
  border: none;
  background: #fff;
  color: #111;
  font-size: 20px;
  cursor: pointer;
  display: flex;
  justify-content: center;
  align-items: center;
  transition: all 0.3s ease;
  transform: scale(0.8);
  box-shadow: 0 4px 12px rgba(0,0,0,0.3);
}

.playlist-card:hover .playlist-cover .play-btn{
  transform: scale(1);
}

.playlist-cover .play-btn:hover{
  transform: scale(1.1);
  background: #27c27a;
  color: #fff;
}

.playlist-info{
  margin-top: 10px;
  padding: 0 4px;
}

/* 专辑列表样式 */
.album-grid{
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 20px;
  padding: 20px 0;
}

.album-card{
  cursor: pointer;
  transition: transform 0.2s ease;
}

.album-card:hover{
  transform: translateY(-4px);
}

.album-cover{
  position: relative;
  width: 100%;
  padding-top: 100%; /* 1:1 宽高比 */
  overflow: hidden;
  border-radius: 8px;
  background: #f1f1f1;
}

.album-cover img{
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.album-card:hover .album-cover img{
  transform: scale(1.05);
}

.album-overlay{
  position: absolute;
  inset: 0;
  background: rgba(0,0,0,0.3);
  display: flex;
  justify-content: center;
  align-items: center;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.album-card:hover .album-overlay{
  opacity: 1;
}

.album-cover .play-btn{
  width: 50px;
  height: 50px;
  border-radius: 50%;
  border: none;
  background: #fff;
  color: #111;
  font-size: 20px;
  cursor: pointer;
  display: flex;
  justify-content: center;
  align-items: center;
  transition: all 0.3s ease;
  transform: scale(0.8);
  box-shadow: 0 4px 12px rgba(0,0,0,0.3);
}

.album-card:hover .album-cover .play-btn{
  transform: scale(1);
}

.album-cover .play-btn:hover{
  transform: scale(1.1);
  background: #27c27a;
  color: #fff;
}

.album-info{
  margin-top: 10px;
  padding: 0 4px;
}

.playlist-name{
  font-size: 14px;
  font-weight: 600;
  color: #222;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-bottom: 4px;
}

.playlist-meta{
  font-size: 12px;
  color: #888;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 占位 */
.placeholder{
  padding: 40px 0;
  text-align:center;
}
.ph-title{
  font-size: 22px;
  font-weight: 800;
  color:#222;
}
.ph-sub{
  margin-top: 10px;
  color:#888;
}

/* 响应式 */
@media (max-width: 960px){
  .nav-bar{ gap: 28px; }
  .nav-item{ font-size: 18px; }
  .name{ font-size: 30px; }
  .ff-item .num{ font-size: 28px; }
  .ff-item .label{ font-size: 16px; }
  .col-album{ width: 220px; }
}

/* ========== 我创建的歌单样式 ========== */
.created-actions {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
  margin-top: 20px;
  padding: 0 20px;
}

.created-actions .btn {
  padding: 6px 12px;
  border: 1px solid #d9d9d9;
  background: #fff;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
}

.created-actions .btn:hover {
  border-color: #409eff;
  color: #409eff;
}

.created-actions .btn.green {
  background: #52c41a;
  border-color: #52c41a;
  color: #fff;
}

.created-actions .btn.green:hover {
  background: #73d13d;
  border-color: #73d13d;
}

.playlist-list {
  padding: 0 20px;
}

.playlist-item {
  display: flex;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background-color 0.3s;
}

.playlist-item:hover {
  background-color: #f5f5f5;
}

.playlist-item .playlist-cover {
  width: 64px;
  height: 64px;
  margin-right: 16px;
  overflow: hidden;
  border-radius: 4px;
  position: relative;
  padding-top: 0;
  background: none;
}

.playlist-item .playlist-cover img {
  position: static;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.playlist-item .playlist-info {
  flex: 1;
  min-width: 0;
  margin-top: 0;
  padding: 0;
}

.playlist-item .playlist-name {
  font-size: 16px;
  font-weight: 500;
  margin-bottom: 8px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  color: #222;
}

.playlist-item .playlist-meta {
  font-size: 12px;
  color: #999;
  display: flex;
  gap: 20px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.playlist-item .playlist-actions {
  margin-left: 16px;
}

.playlist-item .play-btn {
  width: 32px;
  height: 32px;
  border: 1px solid #d9d9d9;
  background: #fff;
  border-radius: 50%;
  cursor: pointer;
  font-size: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
  position: static;
  transform: none;
  box-shadow: none;
}

.playlist-item .play-btn:hover {
  border-color: #409eff;
  color: #409eff;
  background: #ecf5ff;
}

/* 创建歌单弹窗 */
.dialog-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.dialog-content {
  width: 400px;
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
}

.dialog-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid #eee;
}

.dialog-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.dialog-header .close-btn {
  width: 24px;
  height: 24px;
  border: none;
  background: none;
  font-size: 20px;
  cursor: pointer;
  color: #999;
  display: flex;
  align-items: center;
  justify-content: center;
}

.dialog-header .close-btn:hover {
  color: #333;
}

.dialog-body {
  padding: 20px;
}

.form-item {
  margin-bottom: 20px;
}

.form-item label {
  display: block;
  margin-bottom: 8px;
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

.input-group {
  position: relative;
}

.input-group input {
  width: 100%;
  height: 40px;
  padding: 0 12px;
  border: 1px solid #dcdcdc;
  border-radius: 4px;
  font-size: 14px;
  box-sizing: border-box;
}

.input-group input:focus {
  outline: none;
  border-color: #27c27a;
}

.input-group .char-count {
  position: absolute;
  right: 12px;
  bottom: 10px;
  font-size: 12px;
  color: #999;
}

.dialog-footer {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 10px;
  padding: 16px 20px;
  border-top: 1px solid #eee;
  background: #fafafa;
}

.dialog-footer .btn {
  height: 32px;
  padding: 0 16px;
  font-size: 14px;
}

.dialog-footer .btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.dialog-footer .btn:disabled:hover {
  border-color: #dcdcdc;
  color: #666;
  background: #fff;
}

.dialog-footer .btn.green:disabled {
  background: #27c27a;
  border-color: #27c27a;
  color: #fff;
  opacity: 0.6;
}

.dialog-footer .btn.green:disabled:hover {
  background: #27c27a;
  border-color: #27c27a;
  color: #fff;
}

</style>
