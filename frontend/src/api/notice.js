import request from "@/utils/request"

export function getLatestNotices(size = 10) {
    return request({ url: "/notices/latest", method: "get", params: { size } })
}

export function getNoticeDetail(id) {
    return request({ url: `/notices/${id}`, method: "get" })
}
