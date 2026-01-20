<template>
  <div class="layout">
    <!-- ✅ 顶部 Header（整条） -->
    <header class="header">
      <div class="header-left">
        <span class="logo" @click="$router.push('/admin/index')">Harmony · 音乐管理平台</span>
      </div>

      <div class="header-right">
        <el-dropdown @command="handleCommand">
          <div class="user-info">
            <el-avatar
                size="small"
                :src="adminUser.avatarUrl || ''"
                :icon="UserFilled"
            />
            <span class="username">
        {{ adminUser.nickname || adminUser.username || "管理员" }}
      </span>
            <el-tag size="small" type="info" style="margin-left: 6px;">
              {{ displayRoleName }}
            </el-tag>
            <el-icon style="margin-left:6px;"><ArrowDown /></el-icon>
          </div>

          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">
                个人信息
              </el-dropdown-item>
              <el-dropdown-item divided command="logout">
                退出登录
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>

    </header>

    <div class="body">
      <aside class="aside">
        <el-menu
            router
            :default-active="$route.path"
            class="menu"
            background-color="#ffffff"
            text-color="#333"
            active-text-color="#409EFF"
        >
          <template v-for="m in visibleMenus" :key="m.id">
            <el-sub-menu v-if="m.children && m.children.length" :index="String(m.id)">
              <template #title>
                <el-icon v-if="m.icon && icons[m.icon]">
                  <component :is="icons[m.icon]" />
                </el-icon>
                <span>{{ m.menuName }}</span>
              </template>

              <el-menu-item
                  v-for="c in m.children"
                  :key="c.id"
                  :index="c.path"
              >
                <el-icon v-if="c.icon && icons[c.icon]">
                  <component :is="icons[c.icon]" />
                </el-icon>
                {{ c.menuName }}
              </el-menu-item>
            </el-sub-menu>

            <el-menu-item v-else :index="m.path">
              <el-icon v-if="m.icon && icons[m.icon]">
                <component :is="icons[m.icon]" />
              </el-icon>
              <span>{{ m.menuName }}</span>
            </el-menu-item>
          </template>
        </el-menu>
      </aside>
      <main class="main">
        <div class="breadcrumb">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item @click="$router.push('/admin/index')">首页</el-breadcrumb-item>
            <el-breadcrumb-item>{{ currentTitle }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>

        <!-- ✅ 内容卡片容器 -->
        <div class="content-card">
          <router-view />
        </div>
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, onMounted, watch } from "vue"
import { useRoute, useRouter } from "vue-router"
import { ArrowDown, UserFilled} from "@element-plus/icons-vue"

import { useMenuStore } from "@/store/menu"

import { useAdminStore } from "@/store/admin"
import { removeToken } from "@/utils/token"
import { getAdminProfile } from "@/api/admin/profile"

import * as icons from "@element-plus/icons-vue"
const store = useAdminStore()
const route = useRoute()
const router = useRouter()
const menuStore = useMenuStore()
// ✅ 当前管理员（头像/昵称）
const adminUser = ref({
  id: null,
  username: "",
  nickname: "",
  avatarUrl: ""
})

// ✅ 用 localStorage 作为“最终可信角色来源”
const roleList = ref([])

function loadRolesFromStorage() {
  try {
    roleList.value = JSON.parse(localStorage.getItem("ADMIN_ROLES") || "[]")
  } catch {
    roleList.value = []
  }
}


function sortTree(list = []) {
  // 先排序当前层
  const arr = [...list].sort((a, b) => (a.sort ?? 0) - (b.sort ?? 0) || (a.id ?? 0) - (b.id ?? 0))
  // 再递归排序 children
  arr.forEach(n => {
    if (n.children?.length) n.children = sortTree(n.children)
  })
  return arr
}


const sortedMenus = computed(() => sortTree(menuStore.menus || []))

function loadAdminUserFromStorage() {
  try {
    adminUser.value = JSON.parse(localStorage.getItem("ADMIN_USER") || "{}") || {}
  } catch {
    adminUser.value = {}
  }
}

// ✅ roles：localStorage 优先，pinia 兜底
const storeRoles = computed(() => store.roles || [])
const effectiveRoles = computed(() => (roleList.value.length ? roleList.value : storeRoles.value))

const isSuperAdmin = computed(() => effectiveRoles.value.includes("SUPER_ADMIN"))
const isOpAdmin = computed(() => effectiveRoles.value.includes("OP_ADMIN"))


const displayRoleName = computed(() => {
  if (isSuperAdmin.value) return "超级管理员"
  if (isOpAdmin.value) return "运营管理员"
  return "管理员"
})

const currentTitle = computed(() => route.meta.name || "页面")

function logout() {
  removeToken()
  localStorage.removeItem("ADMIN_ROLES")
  localStorage.removeItem("ADMIN_PERMS")
  localStorage.removeItem("ADMIN_USER")
  localStorage.removeItem("ADMIN_TOKEN_NAME")
  store.logout()
  router.push("/admin/login")
}

function handleCommand(cmd) {
  if (cmd === "profile") router.push("/admin/profile")
  if (cmd === "logout") logout()
}

async function refreshAdminProfile() {
  try {
    const res = await getAdminProfile()
    if (res.code === 0) {
      adminUser.value = res.data || {}
      localStorage.setItem("ADMIN_USER", JSON.stringify(adminUser.value))
    }
  } catch {
    // 请求失败不要 logout，避免误踢
  }
}

// 我这里给你一个最简单版本：根据 menuName 过滤，你也可以改成 permissionCode/menuType
const visibleMenus = computed(() => {
  const ms = sortedMenus.value || []
  return ms
      .map(m => ({
        ...m,
        children: (m.children || []).filter(c => c.visible !== 0 && c.status !== 0)
      }))
      .filter(m => {
        // 目录(1) 如果没孩子就隐藏；菜单(2) 保留
        if (Number(m.menuType) === 1) return (m.children && m.children.length > 0)
        return true
      })
})





watch(() => route.path, () => {
  loadRolesFromStorage()
})

onMounted(() => {
  loadRolesFromStorage()
  loadAdminUserFromStorage()
  refreshAdminProfile()

})
onMounted(async () => {
  await menuStore.refreshMenus()
})


</script>



<style scoped>
.layout {
  height: 100vh;
  display: flex;
  flex-direction: column;
}

/* ✅ Header 整条紫色渐变 */
.header {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 22px;
  color: #e5e7eb;
  background: linear-gradient(90deg, #1f2937, #111827);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.35);
}


.logo {
  font-size: 20px;
  font-weight: 700;
  letter-spacing: 1px;
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  gap: 10px;
}

.username {
  font-size: 14px;
}

/* ✅ body：左 + 右 */
.body {
  flex: 1;
  display: flex;
  overflow: hidden;
}

/* ✅ Main */
.main {
  flex: 1;
  background: #f5f6f7;
  padding: 16px 20px;
  overflow-y: auto;
}

/* ✅ 面包屑 */
.breadcrumb {
  margin-bottom: 14px;
}

/* ✅ 内容卡片容器 */
.content-card {
  background: #fff;
  padding: 18px;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  min-height: 500px;
}

/* ✅ menu hover 不要闪 */
:deep(.el-menu) {
  background-color: transparent;
  border-right: none;
}

:deep(.el-menu-item),
:deep(.el-sub-menu__title) {
  color: #d1d5db !important;
}

/* ✅ Sidebar */
.aside {
  width: 220px;
  background: #111827;
  border-right: 1px solid #1f2937;
  height: calc(100vh - 64px);
  overflow-y: auto;
  overflow-x: hidden;
}

.menu {
  min-height: 100%;
  border-right: none;
}


/* ✅ hover */
:deep(.el-menu-item:hover),
:deep(.el-sub-menu__title:hover) {
  background-color: rgba(255, 255, 255, 0.06) !important;
}

:deep(.el-menu-item.is-active) {
  background-color: rgba(99, 102, 241, 0.18) !important;
  color: #a5b4fc !important;
  border-right: 3px solid #6366f1;
  font-weight: 600;
}
.user-info {
  padding: 6px 10px;
  border-radius: 8px;
  transition: .2s;
}
.user-info:hover {
  background: rgba(255,255,255,0.08);
}
.username {
  max-width: 140px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
