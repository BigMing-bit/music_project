import request from "@/utils/request"

export function getNoticeList(params) {
    return request({ url: "/admin/notices", method: "get", params })
}

export function addNotice(data) {
    return request({ url: "/admin/notices", method: "post", data })
}

export function updateNotice(data) {
    return request({ url: "/admin/notices", method: "put", data })
}

export function updateNoticeStatus(id, status) {
    return request({ url: `/admin/notices/${id}/status`, method: "put", data: { status } })
}

export function deleteNotices(ids) {
    return request({ url: "/admin/notices", method: "delete", data: ids })
}

export function getNoticeInfo(id) {
    return request({ url: `/admin/notices/${id}`, method: "get" })
}
