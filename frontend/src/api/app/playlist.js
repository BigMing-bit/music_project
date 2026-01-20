import request from "@/utils/appRequest.js"

/**
 * 首页推荐歌单（卡片）
 * GET /app/playlists/home?cursor=&size=4
 */
export const getPlaylistHome = (params) => {
    return request({
        url: "/app/playlists/home",
        method: "get",
        params,
    })
}

/**
 * 歌单详情（头部信息 + songCount + collected）
 * GET /app/playlists/{id}
 */
export const getPlaylistDetail = (id) => {
    return request({
        url: `/app/playlists/${id}`,
        method: "get",
    })
}

/**
 * 歌单歌曲列表（普通分页）
 * GET /app/playlists/{id}/songs?page=1&pageSize=20
 */
export const getPlaylistSongsPage = (id, params) => {
    return request({
        url: `/app/playlists/${id}/songs`,
        method: "get",
        params,
    })
}

/**
 * 歌单歌曲列表（Cursor 分页）
 * GET /app/playlists/{id}/songs/cursor?cursor=&size=20
 */
export const getPlaylistSongsCursor = (id, params) => {
    return request({
        url: `/app/playlists/${id}/songs/cursor`,
        method: "get",
        params,
    })
}

/**
 * 官方歌单列表
 * GET /app/playlists/official?cursor=&size=20
 */
export const getOfficialPlaylists = (params) => {
    return request({
        url: "/app/playlists/official",
        method: "get",
        params,
    })
}


export const getPlaylistsByTag = (params) => {
    // 确保 tagIds 为数组，并且转为逗号分隔的字符串
    const tagIds = params.tagIds || [];
    const tags = Array.isArray(tagIds) ? tagIds.join(',') : '';  // 如果为空数组，则传递空字符串
    return request({
        url: "/app/playlists/list",
        method: "get",
        params: {
            tagIds: tags,
            page: params.page || 1,
            size: params.size || 20
        }
    });
};

/**
 * 点击播放歌单：播放量 +1
 * POST /app/playlists/{id}/play
 */
export const playPlaylist = (id) => {
    return request({
        url: `/app/playlists/${id}/play`,
        method: "post",
    })
}

/**
 * 收藏/取消收藏（必须登录）
 * POST /app/playlists/{id}/collect
 */
export const togglePlaylistCollect = (id) => {
    return request({
        url: `/app/playlists/${id}/collect`,
        method: "post",
    })
}

/**
 * 编辑歌单（更新已有歌单）
 */
export const editPlaylist = (id, data) => {
    return request({
        url: `/app/playlists/${id}`,
        method: "put",
        data,
    });
};

/**
 * 创建歌单
 */
export const createPlaylist = (data) => {
    return request({
        url: "/app/playlists",
        method: "post",
        data,
    });
};