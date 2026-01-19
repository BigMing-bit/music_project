import request from "@/utils/appRequest"

// 评论分页
export const getComments = (params) => request({
    url: "/app/comments",
    method: "get",
    params
})

// 发表评论/回复
export const postComment = (data) => request({
    url: "/app/comments",
    method: "post",
    data
})



