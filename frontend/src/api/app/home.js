import request from "@/utils/appRequest.js"

// 首页聚合（歌单4 + 专辑4 + 新歌9）
export function getHome() {
    return request({
        url: "/app/home",
        method: "get",
    })
}

// 轮播图
export function getHomeBanners() {
    return request({
        url: "/app/home/banners",
        method: "get",
    })
}

// 公告（可见+启用+时间窗口）
export function getHomeNotices(size = 5) {
    return request({
        url: "/app/home/notices",
        method: "get",
        params: { size },
    })
}

// 推荐歌单换一批（可选）
export function getHomePlaylists(params) {
    return request({
        url: "/app/playlists/home",
        method: "get",
        params: params,
    })
}
