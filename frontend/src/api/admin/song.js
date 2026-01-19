import request from "@/utils/request"

// ✅ 歌曲分页列表
export function getSongList(params) {
    return request({
        url: "/admin/songs/list",
        method: "get",
        params
    })
}

// ✅ 新增歌曲
export function addSong(data) {
    return request({
        url: "/admin/songs/save",
        method: "post",
        data
    })
}

// ✅ 更新歌曲
export function updateSong(data) {
    return request({
        url: "/admin/songs",
        method: "put",
        data
    })
}

// ✅ 删除歌曲（支持批量）
export function deleteSongs(ids) {
    return request({ url: "/admin/songs",
        method: "delete",
        data: ids })
}

// ✅ 上架/下架
export function updateSongStatus(id, status) {
    return request({
        url: `/admin/songs/${id}/status`,
        method: "put",
        data: { status }
    })
}

// ✅ 详情
export function getSongDetail(id) {
    return request({
        url: `/admin/songs/list/${id}`,
        method: "get"
    })
}

export function selectSongs(keyword) {
    return request({
        url: "/admin/songs/select",
        method: "get",
        params: { keyword }
    })
}

export function selectSongsByIds(ids) {
    return request({
        url: "/admin/songs/byIds",
        method: "get",
        params: { ids }
    })
}

