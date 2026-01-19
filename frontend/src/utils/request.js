import axios from "axios"
import { ElMessage } from "element-plus"

const adminService = axios.create({
    baseURL: "/api",
    timeout: 10000,
})

adminService.interceptors.request.use((config) => {
    console.log("[ADMIN REQUEST HIT]", config.url)
    const token = localStorage.getItem("ADMIN_TOKEN")
    const tokenName = localStorage.getItem("ADMIN_TOKEN_NAME") || "satoken"
    if (token) config.headers[tokenName] = token
    return config
})

adminService.interceptors.response.use(
    (res) => {
        const data = res.data
        if (data.code !== 0) {
            ElMessage.error(data.msg || "请求失败")
            // 处理token相关错误
            if (data.msg && (data.msg.includes("token 已被顶下线") || data.msg.includes("token 无效"))) {
                console.log("[ADMIN TOKEN ERROR]", data.msg)
                // 清除本地存储的token
                localStorage.removeItem('ADMIN_TOKEN')
                localStorage.removeItem('ADMIN_TOKEN_NAME')
                localStorage.removeItem('ADMIN_ROLES')
                localStorage.removeItem('ADMIN_PERMS')
                localStorage.removeItem('ADMIN_USER')
                // 跳转到登录页面
                if (window.location.pathname.startsWith("/admin")) {
                    window.location.href = "/admin/login"
                }
            }
            return Promise.reject(data)
        }
        return data
    },
    (err) => {
        // utils/request.js (adminService)
        if (err.response?.status === 401) {
            ElMessage.error("后台登录已过期，请重新登录")
            console.log("[ADMIN 401]", err.config?.url, err.response?.data)
            // ✅ 关键：只在后台路由范围内才跳 /admin/login
            if (window.location.pathname.startsWith("/admin")) {
                window.location.href = "/admin/login"
            }

            return Promise.reject(err)
        }
        // 处理500错误，可能包含token相关错误
        if (err.response?.status === 500) {
            const errorMsg = err.response?.data?.msg || err.message
            if (errorMsg && (errorMsg.includes("token 已被顶下线") || errorMsg.includes("token 无效"))) {
                ElMessage.error(errorMsg.includes("token 已被顶下线") ? "您的账号已在其他设备登录，请重新登录" : "登录已过期，请重新登录")
                console.log("[ADMIN TOKEN ERROR]", errorMsg)
                // 清除本地存储的token
                localStorage.removeItem('ADMIN_TOKEN')
                localStorage.removeItem('ADMIN_TOKEN_NAME')
                localStorage.removeItem('ADMIN_ROLES')
                localStorage.removeItem('ADMIN_PERMS')
                localStorage.removeItem('ADMIN_USER')
                // 跳转到登录页面
                if (window.location.pathname.startsWith("/admin")) {
                    window.location.href = "/admin/login"
                }
            }
        }
        return Promise.reject(err)
    }

    )

export default adminService
