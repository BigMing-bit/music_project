import { createRouter, createWebHistory } from "vue-router"
import { useAdminStore } from "@/store/admin.js"
import { useUserStore } from "@/store/user.js"

const routes = [
    {
        path: "/admin/login",
        name: "AdminLogin",
        component: () => import("@/views/admin/login.vue"),
    },
    {
        path: "/admin",
        name: "AdminLayout",
        component: () => import("~/Layout.vue"),
        redirect: "/admin/index",
        children: [
            {
                path: "index",
                name: "AdminIndex",
                component: () => import("@/views/admin/index.vue"),
                meta: { name: "仪表盘" },
            },
            {
                path: "profile",
                component: () => import("@/views/admin/admin/profile/index.vue"),
                meta: { name: "个人信息" },
            },

            {path: "admin", name: "AdminManage", component: () => import("@/views/admin/admin/index.vue"), meta: { name: "管理员管理", roles: ["SUPER_ADMIN"] } },
            { path: "user", name: "UserManage", component: () => import("@/views/admin/user/index.vue"), meta: { name: "用户管理", roles: ["SUPER_ADMIN"] } },
            { path: "menu", name: "MenuManage", component: () => import("@/views/admin/menu/index.vue"), meta: { name: "菜单管理", roles: ["SUPER_ADMIN"] } },
            { path: "log", name: "LogManage", component: () => import("@/views/admin/log/index.vue"), meta: { name: "日志管理", roles: ["SUPER_ADMIN"] } },

            { path: "song", name: "SongManage", component: () => import("@/views/admin/song/index.vue"), meta: { name: "歌曲管理" } },
            { path: "singer", name: "SingerManage", component: () => import("@/views/admin/singer/index.vue"), meta: { name: "歌手管理" } },
            { path: "album", name: "AlbumManage", component: () => import("@/views/admin/album/index.vue"), meta: { name: "专辑管理" } },
            { path: "playlist", name: "PlaylistManage", component: () => import("@/views/admin/playlist/index.vue"), meta: { name: "歌单管理" } },
            { path: "mv", name: "MvManage", component: () => import("@/views/admin/mv/index.vue"), meta: { name: "Mv管理" } },

            { path: "banner", name: "BannerManage", component: () => import("@/views/admin/banner/index.vue"), meta: { name: "轮播图管理" } },
            { path: "comment", name: "CommentManage", component: () => import("@/views/admin/comment/index.vue"), meta: { name: "评论管理" } },
            { path: "notice", name: "NoticeManage", component: () => import("@/views/admin/notice/index.vue"), meta: { name: "公告管理" } },

            { path: "403", name: "Admin403", component: () => import("@/views/admin/403.vue"), meta: { name: "无权限", hidden: true } },
        ],
    },

    // ===================== 前台 =====================
    {
        path: "/app/login",
        name: "AppLogin",
        component: () => import("@/views/app/login.vue"),
        meta: { appPublic: true }, // ✅ 标记前台公开页
    },
    {
        path: "/app/register",
        name: "AppRegister",
        component: () => import("@/views/app/register.vue"),
        meta: { appPublic: true }, // ✅ 标记前台公开页
    },
    {
        path: "/app",
        name: "AppLayout",
        component: () => import("@/components/app/AppLayout.vue"), // ✅ 前台一定要用自己的Layout
        redirect: "/app/index",
        children: [
            {
                path: "index",
                name: "AppIndex",
                component: () => import("@/views/app/index.vue"),
            },
            {
                path: "playlists/:id",
                name: "AppPlaylist",
                component: () => import("@/views/app/PlaylistDetail.vue"),
                meta: {
                    isOfficial: false,
                },
            },
            { path: "playlists", name: "AppPlaylists", component: () => import("@/views/app/Playlists.vue"), meta: { name: "分类歌单" } },
            {
                path: "/app/playlist/edit/:id",
                name: "EditPlaylist",
                component: () => import("@/views/app/EditPlaylist.vue"),
                meta: { appAuth: true },
                props: true
            },


            { path: "singers", name: "AppSingers", component: () => import("@/views/app/Singers.vue"), meta: { name: "歌手列表" } },
            { path: "singers/:id", name: "AppSingerDetail", component: () => import("@/views/app/SingerDetail.vue") },
            { path: "albums", name: "AppAlbums", component: () => import("@/views/app/Albums.vue"), meta: { name: "内地新碟推荐" } },
            { path: "albums/:id", name: "AppAlbumDetail", component: () => import("@/views/app/AlbumDetail.vue") },
            {
                path: "songs/:id",
                name: "AppSongDetail",
                component: () => import("@/views/app/SongDetail.vue"),
            },

            {
                path: "user/profile",
                name: "AppUserProfile",
                component: () => import("@/views/app/user/Profile.vue"),
                meta: { appAuth: true },
            },
            {
                path: "user/editProfile",
                name: "AppEditProgile",
                component: () => import("@/views/app/user/EditProfile.vue"),
                meta: { appAuth: true },

            },
            {
                path: "player",
                name: "AppPlayer",
                component: () => import("@/views/app/Player.vue"),
            },
        ],
    },

    // ✅ 2) 404 一定放最后！否则会吞掉所有路由
    { path: "/app/:pathMatch(.*)*", redirect: "/app/index" },
    { path: "/:pathMatch(.*)*", redirect: "/admin/index" },

]

const router = createRouter({
    history: createWebHistory(),
    routes,
})


router.beforeEach((to, from, next) => {
    if (to.path.startsWith("/app")) {
        document.title = ` ${to.meta.name ? to.meta.name + ' - ' : ''} Harmony音乐 -数万音乐海量曲库新歌热歌天天畅听的音乐平台`
    } else if (to.path.startsWith("/admin")) {
        document.title = `音乐后台 - ${to.meta.name || '管理系统'}`
    } else {
        document.title = 'Harmony - 数万音乐海量曲库新歌热歌天天畅听的音乐平台'
    }
    if (to.path.startsWith("/app")) {
        const userStore = useUserStore()
        if (to.meta?.appAuth && !userStore.token) {
            return next("/app/login")
        }
        return next()
    }

    if (to.path === "/admin/login") return next()
    if (!to.path.startsWith("/admin")) return next()

    const store = useAdminStore()
    if (!store.token) return next("/admin/login")

    const needRoles = to.meta?.roles
    if (needRoles && !needRoles.some((r) => store.roles.includes(r))) {
        return next("/admin/403")
    }

    next()
})

export default router
