import request from "@/utils/request"

export function getMvList(params) {
    return request({ url: "/admin/mvs", method: "get", params })
}

export function getMvInfo(id) {
    return request({ url: `/admin/mvs/${id}`, method: "get" })
}

export function addMv(data) {
    return request({ url: "/admin/mvs", method: "post", data })
}

export function updateMv(data) {
    return request({ url: "/admin/mvs", method: "put", data })
}

export function deleteMvs(ids) {
    return request({ url: "/admin/mvs", method: "delete", data: ids })
}

export function updateMvStatus(id, status) {
    return request({ url: `/admin/mvs/${id}/status`, method: "put", data: { status } })
}
