import request from "@/utils/appRequest.js"

// 歌手列表
// GET /app/singers/list?page=1&pageSize=30&name=&gender=&status=
export function getSingerList(params) {
    return request({
        url: "/app/singers/list",
        method: "get",
        params,
    })
}



// 歌手详情
// GET /app/singers/{id}
export function getSingerDetail(id) {
    return request({
        url: `/app/singers/${id}`,
        method: "get",
    })
}

// 歌手歌曲列表
// GET /app/singers/{id}/songs?page=1&pageSize=20&orderBy=
export function getSingerSongs(id, params) {
    return request({
        url: `/app/singers/${id}/songs`,
        method: "get",
        params,
    })
}

// 相似歌手
// GET /app/singers/{id}/similar?limit=6
export function getSimilarSingers(id, limit = 6) {
    return request({
        url: `/app/singers/${id}/similar`,
        method: "get",
        params: { limit },
    })
}
