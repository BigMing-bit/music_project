import request from "@/utils/request"

export function getLogList(params) {
    return request({
        url: "/admin/logs",
        method: "get",
        params
    })
}

export function getLogInfo(id) {
    return request({
        url: `/admin/logs/${id}`,
        method: "get"
    })
}

export function deleteLogs(ids) {
    return request({
        url: "/admin/logs",
        method: "delete",
        data: ids
    })
}
// ✅ 清空全部日志（需要后端提供接口）
export function clearAllLogs() {
    return request({
        url: "/admin/logs/clear",
        method: "delete"
    })
}