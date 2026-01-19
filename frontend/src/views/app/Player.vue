<template>
  <div class="player-fullscreen" v-loading="loading">
    <!-- 背景模糊层 -->
    <div class="bg" :style="bgStyle"></div>
    <div class="bg-mask"></div>

    <!-- 顶部 -->
    <div class="topbar">
      <div class="brand">Harmony</div>
      <div class="top-actions">
        <el-button text class="btn-ghost" @click="goBack">返回</el-button>
      </div>
    </div>

    <!-- 主体 -->
    <div class="main">
      <!-- 左：队列 -->
      <section class="panel queue">
        <div class="panel-head">
          <div class="panel-title">播放队列</div>
          <div class="panel-sub">{{ store.queue.length }} 首</div>
        </div>

        <div class="queue-list">
          <div
              class="q-row"
              v-for="(s, i) in store.queue"
              :key="s.id"
              :class="{ active: i === store.currentIndex }"
              @click="playAt(i)"
          >
            <div class="q-idx">{{ String(i + 1).padStart(2, "0") }}</div>
            <div class="q-main">
              <div class="q-name">{{ s.songName }}</div>
              <div class="q-sub">{{ s.singerName || "未知歌手" }}</div>
            </div>
            <div class="q-time">{{ mmss(s.durationSeconds) }}</div>
          </div>
        </div>
      </section>

      <!-- 右：歌曲信息/歌词 -->
      <section class="panel right">
        <div class="album">
          <div class="cover-wrap">
            <img class="cover" :src="fixImg(store.currentSong?.coverUrl)" @error="onImgErr" />
            <!-- 中间圆形播放按钮（QQ音乐风格） -->
            <button class="center-play" type="button" @click="togglePlay" :title="store.playing ? '暂停' : '播放'">
              <span v-if="!store.playing">▶</span>
              <span v-else>❚❚</span>
            </button>
          </div>

          <div class="song-title">{{ store.currentSong?.songName || "未选择歌曲" }}</div>
          <div class="song-sub">{{ store.currentSong?.singerName || "未知歌手" }}</div>
        </div>

        <div class="lyric panel-inner">
          <div class="lyric-title">歌词区域（可接后端/第三方）</div>
          <div class="lyric-body">
            <div class="lyric-empty">暂无歌词</div>
          </div>
        </div>
      </section>
    </div>

    <!-- 底部播放器栏 -->
    <div class="footer">
      <div class="controls-left">
        <button class="icon-btn" @click="prev">⏮</button>
        <button class="icon-btn big" @click="togglePlay">
          <span v-if="!store.playing">▶</span>
          <span v-else>❚❚</span>
        </button>
        <button class="icon-btn" @click="next">⏭</button>
      </div>

      <div class="progress">
        <div class="time">{{ mmss(store.currentTime) }}</div>

        <el-slider
            v-model="slider"
            :min="0"
            :max="Math.max(1, store.duration)"
            :show-tooltip="false"
            @change="seek"
        />

        <div class="time">{{ mmss(store.duration) }}</div>
      </div>

      <div class="controls-right">
        <button class="mode-btn" @click="store.toggleMode()">
          {{ modeText }}
        </button>

        <div class="vol">
          <span class="vol-ico">🔊</span>
          <el-slider
              v-model="store.volume"
              :min="0"
              :max="1"
              :step="0.01"
              :show-tooltip="false"
              style="width: 120px"
              @change="syncVolume"
          />
        </div>
      </div>

      <!-- 真正的音频播放器 -->
      <audio
          ref="audioRef"
          :src="audioSrc"
          @loadedmetadata="onLoaded"
          @timeupdate="onTimeUpdate"
          @ended="onEnded"
      />
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, onBeforeUnmount, ref, watch } from "vue"
import { useRoute, useRouter } from "vue-router"
import { ElMessage } from "element-plus"
import { usePlayerStore } from "@/store/player"

// ✅ 你已有这些 API
import {getPlaylistSongsCursor} from "@/api/app/playlist.js"

const route = useRoute()
const router = useRouter()
const store = usePlayerStore()

const loading = ref(false)
const audioRef = ref(null)

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

const bgStyle = computed(() => ({
  backgroundImage: `url("${fixImg(store.currentSong?.coverUrl)}")`,
}))

const modeText = computed(() => {
  if (store.mode === "loop") return "列表循环"
  if (store.mode === "one") return "单曲循环"
  return "随机播放"
})

// 这里取 audioUrl（你后端如果字段不同，改这里）
const audioSrc = computed(() => {
  const s = store.currentSong
  return s?.audioUrl || s?.url || ""
})

const slider = ref(0)
watch(
    () => store.currentTime,
    (t) => {
      // 播放时跟随
      slider.value = Number(t || 0)
    }
)

// 切歌时自动播放（如果你不想自动播放，删掉 autoPlay）
watch(
    () => store.currentIndex,
    async () => {
      await autoPlay()
    }
)

function mmss(sec) {
  if (sec === null || sec === undefined) return "00:00"
  const n = Math.floor(Number(sec) || 0)
  const m = String(Math.floor(n / 60)).padStart(2, "0")
  const s = String(n % 60).padStart(2, "0")
  return `${m}:${s}`
}

async function autoPlay() {
  // 没有音频地址时提示一次
  if (!audioSrc.value) {
    store.playing = false
    return
  }
  const audio = audioRef.value
  if (!audio) return
  try {
    audio.currentTime = 0
    audio.volume = store.volume
    await audio.play()
    store.playing = true
  } catch (e) {
    store.playing = false
    // 浏览器自动播放限制：必须用户点击才能播放
  }
}

async function togglePlay() {
  const audio = audioRef.value
  if (!audio) return
  if (!audioSrc.value) {
    ElMessage.warning("暂无音频地址（检查 song.audioUrl 字段）")
    return
  }
  try {
    if (store.playing) {
      audio.pause()
      store.playing = false
    } else {
      audio.volume = store.volume
      await audio.play()
      store.playing = true
    }
  } catch (e) {
    store.playing = false
  }
}

function syncVolume() {
  const audio = audioRef.value
  if (!audio) return
  audio.volume = store.volume
}

function seek() {
  const audio = audioRef.value
  if (!audio) return
  audio.currentTime = slider.value
  store.currentTime = slider.value
}

function prev() {
  store.prev()
}

function next() {
  store.next()
}

function playAt(i) {
  store.currentIndex = i
  // 用户点击是“有效交互”，这里可以直接尝试播放
  autoPlay()
}

function onLoaded(e) {
  const audio = e.target
  store.duration = Math.floor(audio.duration || 0)
  syncVolume()
}

function onTimeUpdate(e) {
  store.currentTime = Math.floor(e.target.currentTime || 0)
}

function onEnded() {
  if (store.mode === "one") {
    // 单曲循环
    const audio = audioRef.value
    if (!audio) return
    audio.currentTime = 0
    audio.play()
    return
  }
  store.next()
}

function goBack() {
  router.back()
}

/** 进入 /app/player?playlistId=3  自动拉取歌单歌曲当队列 */
async function loadPlaylistQueue(playlistId) {
  loading.value = true
  try {
    // 用 cursor 拉全量（你可以限制最多 N 首）
    let cursor = null
    let all = []
    let guard = 0

    while (guard < 10) { // 防止死循环，最多拉 10 页
      guard++
      const res = await getPlaylistSongsCursor(playlistId, {cursor, size: 50})
      const d = res.data || {}
      const list = d.list || []
      all.push(...list)
      cursor = d.nextCursor || null
      if (!d.hasMore) break
    }

    // ✅ 重要：这里把字段归一化成 player 需要的结构
    const normalized = all.map(s => ({
      id: s.id,
      songName: s.songName,
      singerName: s.singerName,
      coverUrl: s.coverUrl,
      durationSeconds: s.durationSeconds,
      audioUrl: s.audioUrl || s.url, // 你后端真实字段在这改
    }))

    store.setQueue(normalized, 0)

    // 如果 URL 带 songId，就定位到那首
    const songId = route.query.songId
    if (songId) store.setCurrentById(songId)

    // 让用户点一下再播放也行；这里尝试自动播
    await autoPlay()
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  const playlistId = route.query.playlistId
  if (playlistId) {
    await loadPlaylistQueue(playlistId)
  }

  const onKey = (e) => {
    if (e.code === "Space") {
      e.preventDefault()
      togglePlay()
    }
  }
  window.addEventListener("keydown", onKey)
  onBeforeUnmount(() => window.removeEventListener("keydown", onKey))
})
</script>

<style scoped>
/* ✅ 全屏覆盖 */
.player-fullscreen {
  position: fixed;
  inset: 0;
  z-index: 9999;
  overflow: hidden;
  color: rgba(255, 255, 255, .92);
}

/* 背景：封面模糊图 */
.bg {
  position: absolute;
  inset: -40px;
  background-size: cover;
  background-position: center;
  filter: blur(38px) saturate(1.2);
  transform: scale(1.15);
  opacity: .85;
}

.bg-mask {
  position: absolute;
  inset: 0;
  background: linear-gradient(
      90deg,
      rgba(0, 0, 0, .45),
      rgba(0, 0, 0, .35),
      rgba(0, 0, 0, .40)
  );
}

/* 顶部条 */
.topbar {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 18px 22px 10px;
}

.brand {
  font-weight: 900;
  letter-spacing: .5px;
  font-size: 18px;
}

.btn-ghost {
  color: rgba(255, 255, 255, .9) !important;
}

/* 主体两栏 */
.main {
  position: relative;
  height: calc(100vh - 120px);
  padding: 10px 22px 0;
  display: grid;
  grid-template-columns: 1.05fr .95fr;
  gap: 18px;
}

/* 毛玻璃面板 */
.panel {
  border: 1px solid rgba(255, 255, 255, .12);
  background: rgba(255, 255, 255, .08);
  backdrop-filter: blur(18px);
  -webkit-backdrop-filter: blur(18px);
  overflow: hidden;
}

.queue {
  display: flex;
  flex-direction: column;
}

.panel-head {
  padding: 14px 16px;
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  border-bottom: 1px solid rgba(255, 255, 255, .10);
}

.panel-title {
  font-weight: 900;
}

.panel-sub {
  opacity: .75;
  font-size: 12px;
}

.queue-list {
  overflow: auto;
  padding: 6px 0;
}

.queue-list::-webkit-scrollbar {
  width: 8px;
}

.queue-list::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, .18);
}

.q-row {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 16px;
  cursor: pointer;
  transition: background .15s ease;
}

.q-row:hover {
  background: rgba(255, 255, 255, .08);
}

.q-row.active {
  background: rgba(29, 185, 84, .18);
}

.q-idx {
  width: 34px;
  opacity: .75;
  font-weight: 700;
}

.q-main {
  flex: 1;
  min-width: 0;
}

.q-name {
  font-weight: 900;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.q-sub {
  margin-top: 4px;
  font-size: 12px;
  opacity: .75;
}

.q-time {
  width: 64px;
  text-align: right;
  opacity: .75;
  font-size: 12px;
}

/* 右侧 */
.right {
  display: flex;
  flex-direction: column;
  gap: 14px;
  padding: 16px;
}

.album {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}

.cover-wrap {
  position: relative;
  width: 220px;
  height: 220px;
}

.cover {
  width: 100%;
  height: 100%;
  object-fit: cover;
  box-shadow: 0 20px 60px rgba(0, 0, 0, .35);
}

/* ✅ QQ音乐圆形播放按钮 */
.center-play {
  position: absolute;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
  width: 86px;
  height: 86px;
  border-radius: 999px;
  border: 1px solid rgba(255, 255, 255, .55);
  background: rgba(255, 255, 255, .88);
  color: #111;
  font-size: 26px;
  font-weight: 900;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform .15s ease, filter .15s ease;
}

.center-play:hover {
  transform: translate(-50%, -50%) scale(1.04);
  filter: brightness(1.02);
}

.song-title {
  font-size: 18px;
  font-weight: 900;
}

.song-sub {
  opacity: .8;
  font-size: 13px;
}

.panel-inner {
  flex: 1;
  padding: 14px;
  border: 1px solid rgba(255, 255, 255, .10);
  background: rgba(0, 0, 0, .12);
  backdrop-filter: blur(14px);
}

.lyric-title {
  font-weight: 900;
  margin-bottom: 10px;
}

.lyric-body {
  opacity: .85;
  font-size: 13px;
}

.lyric-empty {
  opacity: .7;
}

/* 底部播放器栏 */
.footer {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  height: 86px;
  display: flex;
  align-items: center;
  gap: 18px;
  padding: 10px 20px;
  border-top: 1px solid rgba(255, 255, 255, .10);
  background: rgba(0, 0, 0, .18);
  backdrop-filter: blur(16px);
}

.controls-left {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 220px;
}

.icon-btn {
  width: 42px;
  height: 42px;
  border: 1px solid rgba(255, 255, 255, .22);
  background: rgba(255, 255, 255, .12);
  color: rgba(255, 255, 255, .95);
  cursor: pointer;
  font-size: 16px;
}

.icon-btn.big {
  width: 52px;
  height: 52px;
  font-size: 18px;
  background: rgba(29, 185, 84, .35);
  border-color: rgba(29, 185, 84, .45);
}

.progress {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 10px;
}

.time {
  width: 54px;
  text-align: center;
  font-size: 12px;
  opacity: .8;
}

.controls-right {
  width: 320px;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 12px;
}

.mode-btn {
  height: 34px;
  padding: 0 10px;
  border: 1px solid rgba(255, 255, 255, .18);
  background: rgba(255, 255, 255, .10);
  color: rgba(255, 255, 255, .92);
  cursor: pointer;
  font-size: 12px;
}

.vol {
  display: flex;
  align-items: center;
  gap: 8px;
}

.vol-ico {
  opacity: .8;
}

/* ElementPlus slider 在暗背景上更清晰一点 */
:deep(.el-slider__runway) {
  background: rgba(255, 255, 255, .18);
}

:deep(.el-slider__bar) {
  background: rgba(255, 255, 255, .75);
}

:deep(.el-slider__button) {
  border-color: rgba(255, 255, 255, .85);
}
</style>
