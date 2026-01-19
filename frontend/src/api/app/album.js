import request from "@/utils/appRequest.js"

// 专辑列表
// GET /app/albums/list?page=1&pageSize=20&name=&singerId=
export function getAlbumList(params) {
    return request({
        url: "/app/albums/list",
        method: "get",
        params,
    })
}

// 专辑详情
// GET /app/albums/{id}
export function getAlbumDetail(id) {
    return request({
        url: `/app/albums/${id}`,
        method: "get",
    })
}

// 专辑歌曲列表
// GET /app/albums/{id}/songs?cursor=&size=20
export function getAlbumSongs(id, params) {
    return request({
        url: `/app/albums/${id}/songs`,
        method: "get",
        params,
    })
}

// 收藏/取消收藏专辑
// POST /app/albums/{id}/collect
export function collectAlbum(id) {
    return request({
        url: `/app/albums/${id}/collect`,
        method: "post",
    })
}
