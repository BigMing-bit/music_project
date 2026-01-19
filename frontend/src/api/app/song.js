import request from "@/utils/appRequest.js"

/** 歌曲详情 */
export const getSongDetail = (id) => {
    return request({
        url: `/app/songs/${id}`,
        method: "get",
    })
}

/** 播放量 +1（公开） */
export const incSongPlayCount = (id) => {
    return request({
        url: `/app/songs/playCount/${id}`,
        method: "post",
    })
}

/** 播放历史（登录） */
export const addPlayHistory = (id) => {
    return request({
        url: `/app/songs/${id}/play`,
        method: "post",
    })
}

/** 收藏/取消收藏（登录） */
export const toggleSongLike = (id) => {
    return request({
        url: `/app/songs/${id}/like`,
        method: "post",
    })
}

/** 是否收藏（登录） */
export const getSongLikeStatus = (id) => {
    return request({
        url: `/app/songs/${id}/like-status`,
        method: "get",
    })
}
