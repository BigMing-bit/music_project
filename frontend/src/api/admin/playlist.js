import request from "@/utils/request"

// ✅ 分页
export function getPlaylistList(params) {
    return request({ url: "/admin/playlists/list", method: "get", params })
}

// ✅ 新增/编辑（统一）
export function savePlaylist(data) {
    return request({ url: "/admin/playlists/save", method: "post", data })
}

// ✅ 删除（批量）
export function deletePlaylists(ids) {
    return request({ url: "/admin/playlists", method: "delete", data: ids })
}

// ✅ 上下架
export function updatePlaylistStatus(id, status) {
    return request({ url: `/admin/playlists/${id}/status`, method: "put", data: { status } })
}

// ✅ 编辑回显详情（含 songIds）
export function getPlaylistDetail(id) {
    return request({ url: `/admin/playlists/${id}/detail`, method: "get" })
}


export function getPlaylistSongs(id) {
    return request({
        url: `/admin/playlists/${id}/songs`,
        method: "get"
    })
}
