<template>
  <div class="song-page" v-loading="loading">
    <!-- 顶部信息区 -->
    <div class="top">
      <div class="cover-wrap" @click="playNow">
        <img class="cover" :src="fixImg(song.coverUrl)" @error="onImgErr" />
      </div>

      <div class="meta">
        <div class="name">{{ song.songName || "未命名歌曲" }}</div>

        <div class="artist-line">
          <span class="icon">👤</span>
          <span class="artist" @click="goSinger(song.singerId)">
            {{ song.singerName || "未知歌手" }}
          </span>
        </div>

        <div class="kv">
          <div class="k">专辑：</div>
          <div class="v link" @click="goAlbum(song.albumId)">{{ song.albumName || "未知专辑" }}</div>

          <div class="k">时长：</div>
          <div class="v">{{ secondsToMMSS(song.durationSeconds) }}</div>

          <div class="k">发行时间：</div>
          <div class="v">{{ song.createTime ? String(song.createTime).slice(0, 10) : "-" }}</div>

          <div class="k">播放：</div>
          <div class="v">{{ song.playCount ?? 0 }}</div>

          <div class="k">收藏：</div>
          <div class="v">{{ song.likeCount ?? 0 }}</div>
        </div>

        <div class="actions">
          <el-button class="btn primary" @click="playNow">▶ 播放</el-button>
          <el-button class="btn" @click="toggleLike" :disabled="likeLoading">
            {{ liked ? "♥ 已收藏" : "♡ 收藏" }}
          </el-button>
          <el-button class="tn_comment" @click="comingSoon"><el-icon class="tn_icon" size="20px"><Comment /></el-icon>评论</el-button>
          <el-button class="tn_more" @click="comingSoon"><el-icon class="tn_icon" size="20px"><MoreFilled /></el-icon>更多</el-button>
        </div>
      </div>
    </div>

    <div class="divider"></div>

    <!-- 下方两列（无卡片） -->
    <div class="bottom">
      <!-- 左：歌词 -->
      <div class="left">
        <div class="section-title">歌词</div>

        <div class="empty" v-if="!song.lyric">暂无歌词</div>

        <div class="lyric" v-else>
          <pre class="lyric-pre">{{ lyricDisplay }}</pre>
          <div class="more-line" v-if="lyricLines.length > maxLines">
            <span class="more" @click="expanded = !expanded">{{ expanded ? "收起" : "展开" }}</span>
          </div>
        </div>
      </div>

      <!-- 右：简介/相关 -->
      <div class="right">
        <div class="section-title">简介</div>
        <div class="desc">
          {{ brief || "暂无简介（后端没简介字段的话，后续可加 song.description）" }}
        </div>

        <div class="gap"></div>

        <div class="section-title">相关热门歌单</div>
        <div class="rel-grid">
          <div class="rel-item" v-for="i in 2" :key="i">
            <div class="rel-cover"></div>
            <div class="rel-text">
              <div class="t1">占位歌单</div>
              <div class="t2">待接接口</div>
            </div>
          </div>
        </div>

        <div class="gap"></div>

        <div class="section-title">相关 MV</div>
        <div class="mv-box">待接 MV 接口</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from "vue"
import { useRoute, useRouter } from "vue-router"
import { ElMessage } from "element-plus"
import { useUserStore } from "@/store/user.js"
import {
  getSongDetail,
  incSongPlayCount,
  addPlayHistory,
  toggleSongLike,
  getSongLikeStatus,
} from "@/api/app/song.js"
import {Comment, MoreFilled} from "@element-plus/icons-vue";

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const id = computed(() => route.params.id)

const loading = ref(false)
const likeLoading = ref(false)

const song = ref({})
const liked = ref(false)

/* 图片 */
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

/* 时间 */
function secondsToMMSS(s) {
  if (s === null || s === undefined) return "-"
  const n = Number(s)
  if (Number.isNaN(n)) return "-"
  const m = Math.floor(n / 60)
  const sec = String(n % 60).padStart(2, "0")
  return `${m}:${sec}`
}

/* 歌词展开 */
const expanded = ref(false)
const maxLines = 18

const lyricLines = computed(() => {
  const t = song.value?.lyric || ""
  return t.split(/\r?\n/).filter((x) => x !== "")
})

const lyricDisplay = computed(() => {
  if (!song.value?.lyric) return ""
  if (expanded.value) return song.value.lyric
  return lyricLines.value.slice(0, maxLines).join("\n")
})

const brief = computed(() => {
  const ls = lyricLines.value
  if (!ls.length) return ""
  return ls.slice(0, 3).join(" ")
})

/* 跳转 */
function goSinger(singerId) {
  if (!singerId) return
  router.push(`/app/singers/${singerId}`)
}
function goAlbum(albumId) {
  if (!albumId) return
  router.push(`/app/albums/${albumId}`)
}

/* 播放逻辑：静默+1，记录历史（可选），跳播放页 */
async function playNow() {
  // 1) 播放量 +1（不提示）
  try {
    await incSongPlayCount(id.value)
    song.value.playCount = (song.value.playCount ?? 0) + 1
  } catch {}

  // 2) 登录才写历史（不提示）
  if (userStore?.token) {
    try {
      await addPlayHistory(id.value)
    } catch {}
  }

  // 3) 去播放页
  router.push({ path: "/app/player", query: { songId: id.value } })
}

function openPlayer() {
  router.push({ path: "/app/player", query: { songId: id.value } })
}

/* 收藏 */
async function toggleLike() {
  if (!userStore?.token) {
    ElMessage.warning("请先登录再收藏")
    return
  }
  likeLoading.value = true
  try {
    const res = await toggleSongLike(id.value)
    liked.value = !!res.data

    // 让数字立即变化
    if (liked.value) song.value.likeCount = (song.value.likeCount ?? 0) + 1
    else song.value.likeCount = Math.max(0, (song.value.likeCount ?? 0) - 1)
  } catch {
    ElMessage.error("操作失败")
  } finally {
    likeLoading.value = false
  }
}

/* ✅ 重点：解决 401 —— 未登录不请求 like-status */
async function load() {
  loading.value = true
  try {
    const res = await getSongDetail(id.value)
    song.value = res.data || {}

    // ✅ 没 token 就直接跳过，不请求接口
    if (!userStore?.token) {
      liked.value = false
      return
    }

    // ✅ 有 token 才查状态，且 401 静默
    try {
      const r = await getSongLikeStatus(id.value)
      liked.value = !!r.data
    } catch {
      liked.value = false
    }
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.song-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 26px 18px 60px;
  background: #fff;
}

/* 顶部 */
.top {
  display: grid;
  grid-template-columns: 260px 1fr;
  gap: 26px;
  padding: 22px 0;
}

.cover-wrap {
  width: 260px;
  height: 260px;
  background: #f4f4f4;
  cursor: pointer;
}
.cover {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.meta {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.name {
  font-size: 34px;
  font-weight: 900;
  color: #111;
  line-height: 1.2;
}

.artist-line {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  color: #666;
  font-size: 15px;
}
.artist {
  cursor: pointer;
  color: #111;
}
.artist:hover {
  color: #1db954;
}

.kv {
  margin-top: 8px;
  display: grid;
  grid-template-columns: 90px 1fr 90px 1fr;
  row-gap: 10px;
  column-gap: 16px;
  font-size: 14px;
}
.k { color: #888; }
.v { color: #333; }
.link { cursor: pointer; }
.link:hover { color: #1db954; }

.actions {
  margin-top: 12px;
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.btn {
  min-width: 110px;
  height: 42px;
  padding: 0 18px;
  border: 1px solid #e6e6e6;
  background: #fff;
  cursor: pointer;
  font-size: 14px;
}
.btn:hover {
  border-color: #cfcfcf;
}
.btn.primary {
  background: #1db954;
  border-color: #1db954;
  color: #fff;
}

.tn_comment{
  min-width: 110px;
  min-height: 41px;
  text-align: center;
  padding: 0 18px;
  transition: background-color 0.3s ease; /* 添加过渡效果 */
}
.tn_comment:hover{
  background-color: #FFf5f5f5; /* 悬浮时颜色变深 */
}

.tn_more{
  min-width: 110px;
  min-height: 41px;
  text-align: center;
  padding: 0 18px;
  transition: background-color 0.3s ease; /* 添加过渡效果 */
}
.tn_more:hover{
  background-color: #FFf5f5f5; /* 悬浮时颜色变深 */
}

/* 分割线（你截图那种） */
.divider {
  height: 1px;
  background: #f0f0f0;
  margin: 10px 0 18px;
}

/* 下方两列 */
.bottom {
  display: grid;
  grid-template-columns: 1.2fr 0.8fr;
  gap: 36px;
  padding: 12px 0;
}

.section-title {
  font-weight: 900;
  font-size: 16px;
  color: #111;
  margin-bottom: 12px;
}

.left {
  min-height: 520px;
}

.lyric {
  max-height: 520px;
  overflow: auto;
  padding-right: 10px;
}
.lyric-pre {
  margin: 0;
  white-space: pre-wrap;
  line-height: 1.9;
  font-size: 14px;
  color: #333;
}
.more-line { margin-top: 10px; }
.more {
  cursor: pointer;
  color: #1db954;
  font-weight: 700;
}

.desc {
  color: #333;
  line-height: 1.8;
  font-size: 14px;
}

.empty { color: #999; }

.gap { height: 22px; }

/* 相关歌单占位 */
.rel-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 14px;
}
.rel-item {
  display: flex;
  gap: 10px;
  align-items: center;
}
.rel-cover {
  width: 60px;
  height: 60px;
  background: #f3f3f3;
}
.rel-text .t1 {
  font-weight: 800;
  font-size: 13px;
  color: #111;
}
.rel-text .t2 {
  font-size: 12px;
  color: #999;
}

.mv-box {
  height: 170px;
  background: #f7f7f7;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #999;
}

/* 响应式 */
@media (max-width: 980px) {
  .top { grid-template-columns: 220px 1fr; }
  .cover-wrap { width: 220px; height: 220px; }
  .bottom { grid-template-columns: 1fr; }
}
</style>
