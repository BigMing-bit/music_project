import request from "@/utils/appRequest.js"

/**
 * 推荐歌曲（userId 可选）
 * GET /api/reco/songs?userId=&size=12
 */
export const getRecommendSongs = (params) => {
    return request({
        url: "/api/reco/songs",
        method: "get",
        params,
    })
}

/**
 * 推荐歌单（seedPlaylistId 可选）
 * GET /api/reco/playlists?userId=&seedPlaylistId=&size=12
 */
export const getRecommendPlaylists = (params) => {
    return request({
        url: "/api/reco/playlists",
        method: "get",
        params,
    })
}

/**
 * 推荐歌单（带过滤参数）
 * POST /api/reco/playlists/filtered?userId=&seedPlaylistId=&size=12
 */
export const getRecommendPlaylistsWithFilter = (params, filters) => {
    return request({
        url: "/api/reco/playlists/filtered",
        method: "post",
        params,
        data: filters,
    })
}

/**
 * 重建推荐模型
 * POST /api/reco/rebuild?topK=30
 */
export const rebuildRecoModel = (params) => {
    return request({
        url: "/api/reco/rebuild",
        method: "post",
        params,
    })
}
