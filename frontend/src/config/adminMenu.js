export const adminMenu = [
    {
        title: "系统信息管理",
        icon: "Setting",
        roles: ["SUPER_ADMIN"],            // 只有超级管理员能看到
        children: [
            { title: "管理员管理", path: "/admin/sys/admin", roles: ["SUPER_ADMIN"] },
            { title: "角色管理", path: "/admin/sys/role", roles: ["SUPER_ADMIN"] },
            { title: "菜单管理", path: "/admin/sys/menu", roles: ["SUPER_ADMIN"] },
            { title: "日志管理", path: "/admin/sys/log", roles: ["SUPER_ADMIN"] },
        ],
    },
    {
        title: "音乐管理",
        icon: "Headset",
        roles: ["SUPER_ADMIN", "OP_ADMIN"], // 两个都能看到
        children: [
            { title: "歌曲管理", path: "/admin/music/song", roles: ["SUPER_ADMIN", "OP_ADMIN"] },
            { title: "歌手管理", path: "/admin/music/singer", roles: ["SUPER_ADMIN",  "OP_ADMIN"] }, // 举例：运营看不到
            { title: "专辑管理", path: "/admin/music/album", roles: ["SUPER_ADMIN",  "OP_ADMIN"] },
            { title: "歌单管理", path: "/admin/music/playlist", roles: ["SUPER_ADMIN",  "OP_ADMIN"] },
        ],
    },
    {
        title: "运营管理",
        icon: "TrendCharts",
        roles: ["SUPER_ADMIN", "OP_ADMIN"],
        children: [
            { title: "轮播图管理", path: "/admin/operate/banner", roles: ["SUPER_ADMIN", "OP_ADMIN"] },
            { title: "公告管理", path: "/admin/operate/notice", roles: ["SUPER_ADMIN", "OP_ADMIN"] },
            { title: "评论管理", path: "/admin/operate/comment", roles: ["SUPER_ADMIN", "OP_ADMIN"] },
        ],
    },
]
