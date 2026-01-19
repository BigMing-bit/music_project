import request from "@/utils/request"

// 分页列表
export function getBannerList(params) {
    return request({
        url: "/admin/banners",
        method: "get",
        params
    })
}

// 新增
export function addBanner(data) {
    return request({
        url: "/admin/banners",
        method: "post",
        data
    })
}

// 更新
export function updateBanner(data) {
    return request({
        url: "/admin/banners",
        method: "put",
        data
    })
}

// 删除（批量）
export function deleteBanners(ids) {
    return request({
        url: "/admin/banners",
        method: "delete",
        data: ids
    })
}

// 状态
export function updateBannerStatus(id, status) {
    return request({
        url: `/admin/banners/${id}/status`,
        method: "put",
        data: { status }
    })
}

// 详情
export function getBannerInfo(id) {
    return request({
        url: `/admin/banners/${id}`,
        method: "get"
    })
}
