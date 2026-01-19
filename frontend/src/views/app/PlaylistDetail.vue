<template>
  <div class="detail-page" v-loading="loading">
    <div class="detail-inner">
      <!-- 歌单头部 -->
      <div class="header">
        <img
            class="cover"
            :src="fixImg(detail.coverUrl)"
            @error="onImgErr"
            alt="歌单封面"
        />
        <div class="info">
          <div class="title">{{ detail.name }}</div>
          <div class="creator" @click="goToProfile(detail.creatorUserId)">
          <el-icon style="float: left;margin: 2px 7px 0 0;" size="20px"><Avatar /></el-icon>{{ detail.creatorNickName }}
          </div>

          <ul class="data_info">
            <li class="data_item">
              <span class="data_label">标签:</span>
              <span class="data_value">
                <el-tag
                    v-for="t in (detail.tags || [])"
                    :key="t.id"
                    size="small"
                    style="margin-right:6px"
                >
                  {{ t.name }}
                </el-tag>
                <span v-if="!(detail.tags && detail.tags.length)">暂无</span>
              </span>
            </li>
            <li class="data_item">
              <span class="data_label">播放数:</span>
              <span class="data_value">{{ detail.playCount }}</span>
            </li>
            <li class="data_item">
              <span class="data_label">收藏量:</span>
              <span class="data_value">{{ detail.collectCount }}</span>
            </li>
          </ul>

          <div class="actions">
            <el-button class="tn_song" @click="playAll"><el-icon class="tn_icon" size="20px"><CaretRight /></el-icon>播放全部</el-button>
            <el-button class="tn_collect" @click="toggleCollect" :disabled="collectLoading">
              {{ detail.collected ? "已收藏" : "收藏" }}
            </el-button>
            <el-button class="tn_comment" @click="scrollToComments">
              <el-icon class="tn_icon" size="20px"><Comment /></el-icon>评论
            </el-button>
            <!-- 如果是自己创建的歌单，显示编辑按钮 -->
            <el-button v-if="canEdit" class="tn_edit" @click="editPlaylist">
              <el-icon class="tn_icon" size="20px"><Edit /></el-icon>编辑歌单
            </el-button>

            <el-button class="tn_more" @click="comingSoon"><el-icon class="tn_icon" size="20px"><MoreFilled /></el-icon>更多</el-button>
          </div>
        </div>
      </div>

      <!-- 歌单歌曲列表 -->
      <div class="block">
        <div
            class="song-row"
            v-for="(song, idx) in songs"
            :key="song.id"
            :class="{ active: isActive(song) }"
            @click="playSong(idx)"
        >
          <div class="idx">{{ String(idx + 1).padStart(2, "0") }}</div>
          <div class="song-main">
            <div class="song-name">{{ song.songName }}</div>
            <div class="song-sub">{{ song.singerName }}</div>
          </div>
          <div class="song-meta">{{ secondsToMMSS(song.durationSeconds) }}</div>
        </div>

        <div class="more" v-if="hasMoreSongs">
          <el-button text @click="loadMoreSongs">加载更多</el-button>
        </div>

      </div>
    </div>
  </div>
  <div class="comment-block" ref="commentSection">
    <div class="comment-title">评论 共{{ totalComments }}条评论</div>

    <!-- 输入框 -->
    <div class="comment-editor">
      <el-input
          v-model="content"
          type="textarea"
          :rows="4"
          maxlength="300"
          show-word-limit
          :word-limit="300"
          placeholder="说说你的看法吧"
      />
      <div class="comment-actions">
        <el-button type="success" @click="submitRoot" :disabled="!content.trim()">
          发表评论
        </el-button>
      </div>
    </div>

    <!-- 列表 -->
    <div v-for="c in commentList" :key="c.id" class="comment-item">
      <div class="c-left">
        <img class="avatar" :src="c.avatar" alt="User Avatar" />
      </div>
      <div class="c-right">
        <div class="c-meta">
          <span class="nick">{{ c.nickname }}</span>
        </div>
        <span class="time">{{ formatTime(c.createTime) }}</span>
        <div class="c-content">{{ c.content }}</div>

        <div class="c-ops">
          <span class="op" @click="openReply(c)">回复</span>
          <span class="op" v-if="c.replies?.length">举报</span>
        </div>

        <!-- 回复编辑器 -->
        <div v-if="replyingTo === c" class="reply-editor">
          <el-input
              v-model="replyContent"
              type="textarea"
              :rows="3"
              maxlength="300"
              show-word-limit
              placeholder="写下你的回复..."
          />
          <div class="reply-actions">
            <el-button @click="cancelReply">取消</el-button>
            <el-button type="success" @click="submitReply(c)" :disabled="!replyContent.trim()">发表回复</el-button>
          </div>
        </div>

        <!-- 查看回复按钮 -->
        <div v-if="c.replies?.length" class="view-replies" @click="toggleReplies(c)">
          <span class="op">查看{{ c.replies.length }}条回复</span>
          <span class="toggle-icon">{{ c.showReplies ? '▼' : '▶' }}</span>
        </div>

        <!-- 显示第一条回复（当未展开时） -->
        <div v-if="c.replies?.length && !c.showReplies" class="reply-preview">
          <div class="reply-item-simple">
            <span class="reply-nick">{{ c.replies[0].nickname }}</span>：
            <span class="reply-text">{{ c.replies[0].content }}</span>
          </div>
        </div>

        <!-- 回复列表（当展开时） -->
        <div v-if="c.replies?.length && c.showReplies" class="reply-list">
          <div v-for="r in c.replies" :key="r.id" class="reply-item">
            <img :src="r.avatar" :alt="r.nickname" class="reply-avatar" />
            <div class="reply-content">
              <div class="reply-header">
                <span class="reply-nick">{{ r.nickname }}</span>
              </div>
              <span class="reply-time">{{ formatTime(r.createTime) }}</span>
              <div class="reply-text">{{ r.content }}</div>
              <div v-if="r.replyToNickname" class="reply-to">
                —— <router-link :to="{ path: '/app/user/profile', query: { id: r.replyToUserId } }" class="reply-user-link">{{ r.replyToNickname }}</router-link>：{{ r.replyToContent }}
              </div>
              <div class="reply-ops">
                <span class="op" @click="openReply(r)">回复</span>
                <span class="op">举报</span>
              </div>
              <!-- 子评论的回复编辑器 -->
              <div v-if="replyingTo === r" class="reply-editor">
                <el-input
                    v-model="replyContent"
                    type="textarea"
                    :rows="3"
                    maxlength="300"
                    show-word-limit
                    placeholder="写下你的回复..."
                />
                <div class="reply-actions">
                  <el-button @click="cancelReply">取消</el-button>
                  <el-button type="success" @click="submitReply(r)" :disabled="!replyContent.trim()">发表回复</el-button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>


    <!-- 加载更多 -->
    <div class="more" v-if="hasMoreComments">
      <el-button text @click="loadMoreComments">加载更多</el-button>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref, computed } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useUserStore } from "@/store/user";
import { usePlayerStore } from "@/store/player";
import { ElMessage } from "element-plus";
import { Avatar, Comment, MoreFilled, CaretRight, Edit} from "@element-plus/icons-vue";

import { getComments, postComment } from "@/api/app/comment";
import {
  getPlaylistDetail,
  getPlaylistSongsCursor,
  togglePlaylistCollect,
} from "@/api/app/playlist";

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();
const player = usePlayerStore();

const commentSection = ref(null);  // 使用 ref 创建对评论区的引用

const id = Number(route.params.id); // 歌单id

/* ---------------- 歌单详情 & 歌曲列表 ---------------- */
const loading = ref(false);
const collectLoading = ref(false);
const detail = ref({});
const songs = ref([]);
const songCursor = ref(null);
const hasMoreSongs = ref(false);

const fallbackCover =
    "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?auto=format&fit=crop&w=800&q=80";

const isActive = (song) => String(player.currentSong?.id) === String(song.id);

function fixImg(url) {
  if (!url) return fallbackCover;
  if (url.startsWith("/")) return `${location.origin}${url}`;
  return url;
}
function onImgErr(e) {
  e.target.src = fallbackCover;
}
function secondsToMMSS(seconds) {
  if (seconds == null || isNaN(seconds)) return "";
  const minutes = Math.floor(seconds / 60);
  const sec = String(seconds % 60).padStart(2, "0");
  return `${minutes}:${sec}`;
}

async function loadDetail() {
  loading.value = true;
  try {
    const res = await getPlaylistDetail(id);
    detail.value = res.data || {};
  } finally {
    loading.value = false;
  }
}

async function loadSongs() {
  const res = await getPlaylistSongsCursor(id, { cursor: songCursor.value, size: 30 });
  const data = res.data || {};
  songs.value.push(...(data.list || []));
  songCursor.value = data.nextCursor || null;
  hasMoreSongs.value = !!data.hasMore;
}

async function loadMoreSongs() {
  await loadSongs();
}

/* ---------------- 评论区（歌单评论：targetType=2） ---------------- */
const targetType = 2;
const targetId = id;

const commentPage = ref(1);
const commentPageSize = 10;
const hasMoreComments = ref(false);
const commentList = ref([]);
const totalComments = ref(0);

const content = ref("");
const replyingTo = ref(null);
const replyContent = ref("");


function formatTime(t) {
  if (!t) return "";
  const date = new Date(t);
  const year = date.getFullYear();
  const month = date.getMonth() + 1;
  const day = date.getDate();
  const hours = date.getHours().toString().padStart(2, '0');
  const minutes = date.getMinutes().toString().padStart(2, '0');
  return `${year}年${month}月${day}日 ${hours}:${minutes}`;
}


const canEdit = computed(() => {
  const meId = userStore.user?.id;
  const myRole = userStore.role ?? userStore.user?.role ?? 0; // 0普通 1官方
  const creatorId = detail.value?.creatorUserId;
  const creatorRole = detail.value?.creatorRole ?? 0; // 详情接口返回的 creatorRole

  if (!meId) return false;

  // 普通用户：只能编辑自己创建的
  if (myRole === 0) return String(meId) === String(creatorId);

  // 官方用户：只能编辑官方歌单（创建者也是官方）
  return creatorRole === 1;
});



// 在加载评论时，保留 showReplies 属性
async function loadComments(reset = false) {
  if (reset) {
    commentPage.value = 1;
    commentList.value = [];  // 清空现有评论
  }

  const res = await getComments({
    targetType,
    targetId,
    page: commentPage.value,
    pageSize: commentPageSize,
  });

  const data = res.data || {};
  const records = data.records || [];

  // 更新评论总数
  totalComments.value = data.total || 0;

  // 确保每个评论对象都有默认的 replies 属性
  const commentsWithShowReplies = records.map(comment => ({
    ...comment,
    showReplies: false,
    replies: comment.replies || []  // 如果没有 replies 属性，添加一个空的回复列表
  }));

  commentList.value.push(...commentsWithShowReplies);

  hasMoreComments.value = (data.current || 1) < (data.pages || 1);
}


const scrollToComments = () => {
  // 使用 .value 访问 ref
  if (commentSection.value) {
    commentSection.value.scrollIntoView({ behavior: "smooth", block: "start" });
  }
};

// 控制子评论显示
function toggleReplies(comment) {
  comment.showReplies = !comment.showReplies;
}


async function loadMoreComments() {
  commentPage.value += 1;
  await loadComments(false);
}

async function submitRoot() {
  if (!userStore?.token) {
    ElMessage.warning("请先登录再评论");
    return;
  }
  const text = content.value.trim();
  if (!text) return;

  const res = await postComment({
    targetType,
    targetId,
    parentId: 0,  // 根评论没有 parentId
    content: text,
  });

  if (res.code === 0) {
    ElMessage.success("评论成功");
    content.value = "";
    await loadComments(true); // 重载评论，并保留子评论
  } else {
    ElMessage.error(res.msg || "评论失败");
  }
}

function openReply(c) {
  replyingTo.value = c;
  replyContent.value = "";
}

function cancelReply() {
  replyingTo.value = null;
  replyContent.value = "";
}

async function submitReply(root) {
  if (!userStore?.token) {
    ElMessage.warning("请先登录再回复");
    return;
  }
  const text = replyContent.value.trim();
  if (!text) return;

  const res = await postComment({
    targetType,
    targetId,
    parentId: root.id,  // 回复的父评论 id
    replyToUserId: root.userId,
    replyToCommentId: root.id,
    content: text,
  });

  if (res.code === 0) {
    ElMessage.success("回复成功");
    cancelReply();
    await loadComments(true); // 重载评论，并保留子评论
  } else {
    ElMessage.error(res.msg || "回复失败");
  }
}

/* ---------------- 其他按钮逻辑 ---------------- */
async function toggleCollect() {
  if (!userStore?.token) {
    ElMessage.warning("请先登录再收藏");
    return;
  }
  collectLoading.value = true;
  try {
    const res = await togglePlaylistCollect(id);
    const collected = res.data ?? false;
    detail.value = { ...detail.value, collected };
    detail.value.collectCount = collected
        ? (detail.value.collectCount || 0) + 1
        : Math.max(0, (detail.value.collectCount || 0) - 1);
  } catch {
    ElMessage.error("操作失败");
  } finally {
    collectLoading.value = false;
  }
}

function playSong(index) {
  player.setFromPlaylist({
    playlistId: id,
    playlistName: detail.value?.name || "",
    playlistCover: detail.value?.coverUrl || "",
  });
  player.setQueue([...songs.value], index);
  player.playAt(index);
  router.push({ path: "/app/player", query: { playlistId: id } });
}

function editPlaylist() {
  router.push({ path: `/app/playlist/edit/${id}` });
}


function goToProfile(userId) {
  if (userId) router.push({ path: `/app/user/profile`, query: { id: userId } });
  else ElMessage.error("用户信息不存在");
}


const playAll = () => console.log("播放全部");
const comingSoon = () => console.log("即将推出");

/* ---------------- 初始化 ---------------- */
onMounted(async () => {
  await loadDetail();
  await loadSongs();
  await loadComments(true);
});
</script>


<style scoped>
.detail-page {
  min-height: 100vh;
}

.detail-inner {
  max-width: 1200px;
  margin: 0 auto;  /* 居中显示 */
  background-color: transparent;  /* 去除背景色 */
  border: none;  /* 去掉所有的边框 */
}

.header {
  display: flex;
  gap: 70px;
  padding: 30px 0;
  background-color: transparent;  /* 去除背景色 */
  border: none;  /* 去掉边框 */
}

.cover {
  width: 270px;
  height: 270px;
  object-fit: cover;
  background: #eee;  /* 给封面图片一个背景颜色 */
}

.info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.creator{
  display: flex;
  align-items: center;
  font-style: unset;
  font-size: 16px;
}

.title {
  padding-top: 40px;
  font-size: 30px;
  color: #222;
}
ul{
  list-style: none;
}
.data_info{
  width: 520px;
  padding-top: 8px;
}
.data_item{
  line-height: 27px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  font-size: 14px;
}
.data_label{
  font-size: 15px;
}
.data_value{
  font-size: 14px;
  color: #222;
}
.data_info li{
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}

.actions {
  margin-top: 6px;
}

.tn_icon{
  margin-right: 4px;
  text-align: center;
}
.tn_song{
  min-width: 110px;
  min-height: 41px;
  text-align: center;
  padding: 0 18px;
  background: #31c27c;
  color: #ffffff;
  transition: background-color 0.3s ease; /* 添加过渡效果 */
}
.tn_song:hover{
  background-color: #22a86b; /* 悬浮时颜色变深 */}

.tn_collect{
  min-width: 110px;
  min-height: 41px;
  text-align: center;
  padding: 0 18px;
  transition: background-color 0.3s ease; /* 添加过渡效果 */
}

.tn_collect:hover{
  background-color: #FFf5f5f5; /* 设置悬浮时的背景颜色 */
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
.tn_edit{
  min-width: 110px;
  min-height: 41px;
  text-align: center;
  padding: 0 18px;
  transition: background-color 0.3s ease; /* 添加过渡效果 */
}
.tn_edit:hover{
  background-color: #FFf5f5f5; /* 悬浮时颜色变深 */
}

.block {
  margin-top: 18px;
  background: transparent;  /* 去除背景色 */
  padding: 0;
  border: none;  /* 去掉边框 */
}

.song-row {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 12px 0;
  cursor: pointer;
}

.song-row:hover {
  background: rgba(0, 0, 0, 0.02);
}

.song-row.active {
  background: rgba(29, 185, 84, 0.10);
}

.idx {
  width: 34px;
  color: #999;
  font-size: 14px;
  text-align: center;
  font-weight: 700;
}

.song-main {
  flex: 1;
  min-width: 0;
}

.song-name {
  color: #222;
  font-weight: 700;
  font-size: 14px;
}

.song-sub {
  color: #999;
  font-size: 12px;
  margin-top: 4px;
}

.song-meta {
  color: #999;
  font-size: 12px;
}

.more {
  display: flex;
  justify-content: center;
  margin-top: 16px;
}


:deep(.el-button) {
  border-radius: 0 !important;
}

.comment-block { margin-top: 30px; }
.comment-title { font-size: 22px; margin-bottom: 12px; }

.comment-editor {  padding: 12px; width: 800px;min-height: 102px;}
.comment-editor :deep(.el-textarea__inner) {
  position: relative;
  overflow: hidden;
  min-height: 102px;
  border: 1px solid #ececec;
  background-color: #ececec !important;
}
.comment-actions { display: flex; justify-content: flex-end; margin-top: 10px;}

.comment-item { display: flex; gap: 12px; padding: 10px 0; border-bottom: 1px solid; border-color:#ededed; width:800px;}
.c-left { flex-shrink: 0; }
.c-right { flex: 1;padding-bottom: 5px; }
.avatar { width: 44px; height: 44px; border-radius: 50%; object-fit: cover; }

.c-meta { display: flex; gap: 10px; align-items: center; }
.nick { font-weight: 700; font-size: 15px;color: #999}
.time { color: #999; font-size: 15px;}

.c-content { margin-top: 15px; font-size: 15px;}
.c-ops { margin-top: 4px; color: #999; font-size: 13px; }
.op { cursor: pointer; margin-right: 12px; }
.op:hover { color: #31c27c; }

.view-replies { margin-top: 6px; color: #999; font-size: 13px; cursor: pointer; display: flex; align-items: center; }
.view-replies:hover { color: #31c27c; }
.toggle-icon { margin-left: 4px; font-size: 10px; }

.reply-editor { margin-top: 8px; background: #fafafa; padding: 8px; }
.reply-actions { display: flex; justify-content: flex-end; gap: 8px; margin-top: 6px; }

.reply-preview {
  margin-top: 10px;
  margin-bottom: 5px;
  background: rgba(0, 0, 0, .03);
  padding: 10px 20px 0;
  width: 100%;
}

.reply-item-simple {
  font-size: 14px;
  line-height: 20px;
  padding: 10px 0;
}

.reply-item-simple .reply-nick {
  font-weight: 700;
  color: #31c27c;
  margin-right: 4px;
}

.reply-item-simple .reply-text {
  color: #333;
}

.reply-list
{
  margin-top: 10px;
  margin-bottom: 5px;
  background: rgba(0, 0, 0, .03);
  padding: 10px 20px 0;
  width: 100%;
}
.reply-item { display: flex; gap: 10px; font-size: 13px; margin-bottom: 20px; border-bottom: 1px solid #f2f2f2; width: 100%; }
.reply-avatar { width: 40px; height: 40px; border-radius: 50%; object-fit: cover; }
.reply-content { flex: 1; }
.reply-nick { font-weight: 700; margin-bottom: 4px;color: #999; }
.reply-time { color: #999; font-size: 12px; margin-bottom: 8px; }
.reply-text { line-height: 20px; font-size: 15px; margin-bottom: 8px; margin-top: 10px;}
.reply-to { color: #666; font-size: 14px; line-height: 18px; margin-bottom: 8px; }
.reply-user-link { color: #31c27c; text-decoration: none; }
.reply-user-link:hover { text-decoration: underline; }
.reply-ops { margin-top: 4px; }
.reply-ops .op { color: #999; font-size: 12px; cursor: pointer; margin-right: 12px; }
.reply-ops .op:hover { color: #31c27c; }

.more { display: flex; justify-content: center; margin-top: 12px; }



</style>
