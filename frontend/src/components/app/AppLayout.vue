<template>
  <div class="app-shell">
    <!-- 顶部导航 -->
    <header class="app-header">
      <div class="left">
        <div class="logo" @click="goHome">Harmony</div>
        <nav class="nav">
          <span class="nav-item" :class="{ active: isActive('/app/index') }" @click="router.push('/app/index')">首页</span>
          <span class="nav-item" :class="{ active: isActive('/app/singers') }" @click="router.push('/app/singers')">歌手</span>
          <span class="nav-item" :class="{ active: isActive('/app/albums') }" @click="router.push('/app/albums')">新碟</span>
          <span class="nav-item" @click="comingSoon" >排行榜</span>
          <span class="nav-item" :class="{ active: isActive('/app/playlists') }" @click="router.push('/app/playlists')">分类歌单</span>
          <span class="nav-item" @click="comingSoon">雷达</span>
          <span class="nav-item" @click="comingSoon">MV</span>
          <span class="nav-item" @click="comingSoon">数字专辑</span>
        </nav>
      </div>

      <div class="right">
        <el-input
            v-model="keyword"
            class="search"
            placeholder="搜索歌曲 / 歌手 / 歌单"
            clearable
            @keyup.enter="onSearch"
        />

        <div class="user">
          <el-button v-if="!isLogin" type="primary" @click="goLogin">登录</el-button>
          <el-dropdown v-else>
            <span class="user-trigger">
              <el-avatar :size="28" :src="avatar" />
              <span class="name">{{ nickname }} <span v-if="isOfficial">【官方】</span></span> <!-- 显示官方标签 -->
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="goProfile">个人中心</el-dropdown-item>
                <el-dropdown-item divided @click="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </header>

    <!-- 主体 -->
    <main class="app-main" :class="{ 'app-main-home': isActive('/app/index') }">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { useUserStore } from "@/store/user.js";

const route = useRoute();
const router = useRouter();

const userStore = useUserStore();

const isLogin = computed(() => userStore.isLogin);
const nickname = computed(() => userStore.nickname);
const avatar = computed(() => userStore.avatar);
const isOfficial = computed(() => userStore.isOfficial);

const logout = () => {
  userStore.logout();
  ElMessage.success("已退出登录");
  router.push("/app/index");
};

const keyword = ref("");

const isActive = (prefix) => route.path.startsWith(prefix);

const onSearch = () => {
  const q = keyword.value?.trim();
  if (!q) return;
  ElMessage.info("搜索页你后面再做，这里先占位");
};

const comingSoon = () => ElMessage.info("待开发");

const goLogin = () => router.push("/app/login");
const goProfile = () => router.push("/app/user/Profile");

onMounted(() => {
  userStore.hydrate();
});
</script>

<style scoped>
.app-shell { min-height: 100vh; background: #f5f5f5;}

.app-header {
  height: 80px; background: #fff1f0;
  display: flex; align-items: center; justify-content: space-between;
  padding: 0 20px; position: sticky; top: 0; z-index: 10;
}
.left { display: flex; align-items: center; gap: 30px; flex: 1; justify-content: center; }
.logo { font-weight: 800; font-size: 18px; cursor: pointer; color: #1890ff; margin-right: 30px; }
.nav { display: flex; gap: 23px; }
.nav-item { cursor: pointer; padding: 8px 10px; font-size: 16px; color: #333; position: relative; }
.nav-item:hover { color: #1db954; }
.nav-item.active {
  color: #1890ff;
  font-weight: 500;
}
.nav-item.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 2px;
  background: #1890ff;
}
.right { display: flex; align-items: center; gap: 20px; }
.search { width: 300px; }
.user-trigger { display: inline-flex; align-items: center; gap: 8px; cursor: pointer; }
.name { color: #333; }
.app-main { max-width: 1200px; margin: 0 auto; padding: 0 18px; }
.app-main-home { max-width: none; width: 100%; margin: 0; padding: 0; }
</style>
