import request from "@/utils/request"

// ✅ 列表分页（管理端）
export function getSingerList(params) {
    return request({
        url: "/admin/singers/list",
        method: "get",
        params
    })
}

// ✅ 新增
export function addSinger(data) {
    return request({
        url: "/admin/singers",
        method: "post",
        data
    })
}

// ✅ 更新
export function updateSinger(data) {
    return request({
        url: "/admin/singers",
        method: "put",
        data
    })
}

// ✅ 删除（批量）
export function deleteSingers(ids) {
    return request({
        url: "/admin/singers",
        method: "delete",
        data: ids
    })
}

// ✅ 状态切换
export function updateSingerStatus(id, status) {
    return request({
        url: `/admin/singers/${id}/status`,
        method: "put",
        data: { status }
    })
}

// ✅ 详情
export function getSingerInfo(id) {
    return request({
        url: `/admin/singers/${id}`,
        method: "get"
    })
}

// ✅ 下拉选歌手
export function selectSingers(keyword) {
    return request({
        url: "/admin/singers/select",
        method: "get",
        params: { keyword }
    })
}
