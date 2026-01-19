import request from "@/utils/appRequest.js"

// 平铺（可选）
export const getTagList = () => request({
    url: "/app/tags",
    method: "get",
})

// 分组（推荐：QQ音乐那样）
export const getTagGroups = () => request({
    url: "/app/tags/group",
    method: "get",
})
