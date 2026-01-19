import request from "@/utils/request"

export function getAdminProfile() {
    return request({
        url: "/admin/profile",
        method: "get",
    })
}

export function updateAdminProfile(data) {
    return request({
        url: "/admin/profile",
        method: "put",
        data,
    })
}

// ✅ 新增：修改密码
export function changeAdminPassword(data) {
    return request({
        url: "/admin/profile/password",
        method: "put",
        data, // { oldPassword, newPassword }
    })
}
