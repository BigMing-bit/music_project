import request from "@/utils/request"

export function getAdminList(params) {
    return request({ url: "/admin/admins/list", method: "get", params })
}
export function getAdminInfo(id) {
    return request({ url: `/admin/admins/${id}`, method: "get" })
}
export function addAdmin(data) {
    return request({ url: "/admin/admins", method: "post", data })
}
export function updateAdmin(data) {
    return request({ url: "/admin/admins", method: "put", data })
}
export function updateAdminStatus(id, status) {
    return request({ url: `/admin/admins/${id}/status`, method: "put", data: { status } })
}
export function deleteAdmins(ids) {
    return request({ url: "/admin/admins", method: "delete", data: ids })
}
