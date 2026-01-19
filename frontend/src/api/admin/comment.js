import request from "@/utils/request"

// 分页
export function getCommentPage(params) {
    return request({
        url: "/admin/comments",
        method: "get",
        params
    })
}

// 详情
export function getCommentInfo(id) {
    return request({
        url: `/admin/comments/${id}`,
        method: "get"
    })
}

// 回复
export function replyComment(id, replyContent) {
    return request({
        url: `/admin/comments/${id}/reply`,
        method: "post",
        data: { replyContent }
    })
}

// 删除单个
export function deleteComment(id) {
    return request({
        url: `/admin/comments/${id}`,
        method: "delete"
    })
}

// 批量删除
export function deleteComments(ids) {
    return request({
        url: "/admin/comments",
        method: "delete",
        data: ids
    })
}
