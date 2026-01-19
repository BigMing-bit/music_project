import request from "@/utils/appRequest.js"
import appService from "@/utils/appRequest.js";

/** 获取验证码 */
export const getCaptcha = () => {
    return request({
        url: "/app/user/captcha",
        method: "get",
    })
}

/** 登录（带验证码） */
export const userLogin = (data) => {
    console.log("[USER LOGIN DATA]", data);
    return request({
        url: "/app/user/login",
        method: "post",
        data,
    })
}

/** 退出 */
export const userLogout = () => {
    return request({
        url: "/app/user/logout",
        method: "post",
    })
}

/** 获取个人信息（必须登录） */
export const getUserProfile = () => {
    return request({
        url: "/app/user",
        method: "get",
    })
}

/** 更新个人信息 */
export const updateUserProfile = (data) => {
    return request({
        url: "/app/user/update",
        method: "put",
        data,
    })
}

/** 修改密码 */
export const changePassword = (data) => {
    return request({
        url: "/app/user/change-password",
        method: "put",
        data,
    })
}

/** 注册（如果你要做） */
export const userRegister = (data) => {
    return request({
        url: "/app/user/register",
        method: "post",
        data,
    })
}

export const getMe = () => appService.get("/app/user/me")
export const getUserInfo = (id) => appService.get(`/app/user/info/${id}`)
export const getStats = () => appService.get("/app/user/stats")
export const getUserStats = (id) => appService.get(`/app/user/stats/${id}`)
export const getMyPlaylists = () => appService.get("/app/user/playlists")
export const getUserPlaylists = (id) => appService.get(`/app/user/playlists/${id}`)


export const getLikedSongs = (cursor = "", size = 20) =>
    appService.get("/app/user/liked-songs", { params: { cursor, size } })

export const getUserLikedSongs = (id, cursor = "", size = 20) =>
    appService.get(`/app/user/liked-songs/${id}`, { params: { cursor, size } })

export const getLikedPlaylists = (cursor = "", size = 20) =>
    appService.get("/app/user/liked-playlists", { params: { cursor, size } })

export const getUserLikedPlaylists = (id, cursor = "", size = 20) =>
    appService.get(`/app/user/liked-playlists/${id}`, { params: { cursor, size } })

export const getLikedAlbums = (cursor = "", size = 20) =>
    appService.get("/app/user/liked-albums", { params: { cursor, size } })

export const getUserLikedAlbums = (id, cursor = "", size = 20) =>
    appService.get(`/app/user/liked-albums/${id}`, { params: { cursor, size } })

export const getPlayHistory = (cursor = "", size = 20) =>
    appService.get("/app/user/play-history", { params: { cursor, size } })