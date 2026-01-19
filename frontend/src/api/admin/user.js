import request from "@/utils/request"

export function getUserList(params) {
    return request({ url: "/admin/users/list", method: "get", params })
}
export function getUserInfo(id) {
    return request({ url: `/admin/users/${id}`, method: "get" })
}
export function deleteUsers(ids) {
    return request({ url: "/admin/users", method: "delete", data: ids })
}
export function updateUserStatus(id, status) {
    return request({
        url: `/admin/songs/${id}/status`,
        method: "put",
        data: { status }
    })
}