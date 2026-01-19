import request from "@/utils/request"

// ✅ 专辑分页列表（含 singerName）
export function getAlbumList(params) {
    return request({
        url: "/admin/albums",
        method: "get",
        params,
    })
}

// ✅ 新增专辑
export function addAlbum(data) {
    return request({
        url: "/admin/albums",
        method: "post",
        data,
    })
}

// ✅ 更新专辑（REST：PUT）
export function updateAlbum(data) {
    return request({
        url: "/admin/albums",
        method: "put",
        data,
    })
}


// ✅ 删除专辑（支持批量）
export function deleteAlbums(ids) {
    return request({
        url: "/admin/albums",
        method: "delete",
        data: ids,
    })
}

// ✅ 上架/下架
export function updateAlbumStatus(id, status) {
    return request({
        url: `/admin/albums/${id}/status`,
        method: "put",
        data: { status },
    })
}

// ✅ 详情
export function getAlbumInfo(id) {
    return request({
        url: `/admin/albums/${id}`,
        method: "get"
    })
}


// ✅ 下拉选择专辑（如果你别处要用）
export function selectAlbums(keyword) {
    return request({
        url: "/admin/albums/select",
        method: "get",
        params: { keyword },
    })
}
