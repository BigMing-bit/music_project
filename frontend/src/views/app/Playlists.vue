<script setup>
import {ref, onMounted} from "vue";
import {useRouter} from "vue-router";
import {getTagList, getTagGroups} from "@/api/app/tag";
import {getPlaylistsByTag, playPlaylist} from "@/api/app/playlist";
import {ElMessage} from "element-plus";

const router = useRouter();

// 获取标签列表
const tagList = ref([]);
const tagGroups = ref([]);
// 当前选中的标签
const selectedTag = ref(null);
// 当前展示的歌单列表
const playlists = ref([]);
const loading = ref(false);

// 获取标签数据
const fetchTags = async () => {
  try {
    const tagRes = await getTagList();
    tagList.value = tagRes.data || [];
    const groupRes = await getTagGroups();
    tagGroups.value = groupRes.data || [];
  } catch (e) {
    console.error('获取标签数据失败:', e);
  }
}


// 获取歌单数据
const fetchPlaylists = async (selectedTags = []) => {
  loading.value = true;

  const tagIds = Array.isArray(selectedTags) ? selectedTags : [];

  try {
    const res = await getPlaylistsByTag({tagIds: tagIds, page: 1, size: 20});
    playlists.value = res.data?.records || res.data || [];
  } catch (error) {
    console.error("获取歌单失败", error);
  } finally {
    loading.value = false;
  }
};

// 切换标签时更新歌单列表
const selectTag = (tag) => {
  selectedTag.value = tag;
  const tagIds = tag ? [tag.id] : [];
  fetchPlaylists(tagIds);
}

// 清除选中的标签
const clearSelectedTag = () => {
  selectedTag.value = null;
  fetchPlaylists([]);
}

// 跳转到歌单详情页
const goPlaylistDetail = (id) => {
  router.push(`/app/playlists/${id}`);
};

// 播放歌单并跳转到播放器
const playAndGoPlayer = async (id) => {
  try {
    await playPlaylist(id);
    await router.push(`/app/playlists/${id}`);
  } catch (error) {
    console.error("播放歌单失败", error);
    ElMessage.error("播放失败");
  }
};

onMounted(() => {
  fetchTags();
  fetchPlaylists([]); // 默认加载所有歌单
});
</script>

<template>
  <div class="playlists-page">
    <!-- 标签分组 -->
    <div class="tags-section">
      <div 
          class="tag-group" 
          v-for="group in tagGroups" 
          :key="group.id"
      >
        <h3 class="group-name">{{ group.name }}</h3>
        <div class="tags-container">
          <span 
              class="tag-item" 
              v-for="tag in group.tags" 
              :key="tag.id"
              @click="selectTag(tag)"
              :class="{ active: selectedTag?.id === tag.id }"
          >
            {{ tag.name }}
          </span>
        </div>
      </div>
    </div>

    <!-- 歌单标题/选中标签 -->
    <div class="playlist-header">
      <div class="selected-tag" v-if="selectedTag">
        <span class="tag-name">{{ selectedTag.name }}</span>
        <span class="close-btn" @click="clearSelectedTag">×</span>
      </div>
      <h2 v-else>推荐歌单</h2>
    </div>

    <!-- 歌单展示 -->
    <div class="playlist-container" v-if="!loading">
      <div class="playlist-grid">
        <div v-for="playlist in playlists" :key="playlist.id" class="cover-card">
          <div class="cover" @click="goPlaylistDetail(playlist.id)">
            <img :src="playlist.coverUrl" alt="封面"/>
            <div class="overlay">
              <button
                  class="play-btn"
                  title="播放"
                  @click.stop="playAndGoPlayer(playlist.id)"> ▶ </button>
            </div>
          </div>
          <div class="cover-title">{{ playlist.name }}</div>
          <div class="cover-sub">播放量：{{ playlist.playCount ?? 0 }}</div>
        </div>
      </div>
    </div>

    <div v-else class="loading">加载中...</div>
  </div>
</template>

<style scoped>
.playlists-page {
  width: 100%;
  margin: 0 auto;
  padding: 20px;
  box-sizing: border-box;
}

.tags-section {
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid #f0f0f0;
}

.tag-group {
  margin-bottom: 20px;
}

.group-name {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 10px;
  color: #333;
}

.tags-container {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.tag-item {
  padding: 6px 16px;
  background-color: #f5f5f5;
  color: #333;
  cursor: pointer;
  border-radius: 20px;
  font-size: 14px;
  transition: all 0.2s ease;
}

.tag-item:hover {
  background-color: #e8f5e8;
  color: #27c27a;
}

.tag-item.active {
  background-color: #27c27a;
  color: white;
}

.selected-tag {
  display: inline-flex;
  align-items: center;
  background-color: #e8f5e8;
  color: #27c27a;
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 16px;
  font-weight: 600;
}

.tag-name {
  margin-right: 10px;
}

.close-btn {
  cursor: pointer;
  font-size: 18px;
  font-weight: bold;
  line-height: 1;
}

.playlist-header {
  margin-bottom: 20px;
  display: flex;
  align-items: center;
}

.playlist-header h2 {
  font-size: 20px;
  font-weight: 600;
  color: #333;
  margin: 0;
}

.playlist-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.playlist-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 15px;
  max-width: 1400px;
}

/* 封面更大 */
.cover-card{
  width: 100%;
  cursor:pointer;
  transition: transform .18s ease;
}
.cover-card:hover{ transform: translateY(-3px); }

.cover{
  width: 225px;
  height: 225px;
  padding-bottom: 100%;
  background:#e9e9e9;
  overflow:hidden;
  position: relative;
}
.cover img{
  position: absolute;
  top: 0;
  left: 0;
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

.loading {
  text-align: center;
  padding: 40px 0;
  color: #999;
}

@media (max-width: 768px) {
  .playlist-grid {
    grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
    gap: 15px;
  }
  
  .playlist-cover {
    height: 140px;
  }
}
</style>
