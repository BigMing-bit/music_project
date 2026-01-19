import { defineStore } from "pinia"
import { adminLogin } from "@/api/admin/auth" // 你的登录接口函数名，按你项目改
import { setToken, removeToken } from "@/utils/token"

export const useAdminStore = defineStore("admin", {
    state: () => ({
        token: localStorage.getItem("ADMIN_TOKEN") || "",
        tokenName: localStorage.getItem("ADMIN_TOKEN_NAME") || "satoken",
        roles: JSON.parse(localStorage.getItem("ADMIN_ROLES") || "[]"),
        perms: JSON.parse(localStorage.getItem("ADMIN_PERMS") || "[]"),
        user: JSON.parse(localStorage.getItem("ADMIN_USER") || "{}"),
    }),

    actions: {
        async login(form) {
            // res 结构按你的 request 拦截器：成功会返回 data（Result.data）
            const res = await adminLogin(form)
            const data = res.data || res   // 双保险：有的封装直接返回 data

            // ✅ token
            this.token = data.token
            this.tokenName = data.tokenName || "satoken"
            setToken(this.token) // 你原来怎么存 token 就继续用

            // ✅ roles / permissions（关键）
            this.roles = data.roles || []
            this.perms = data.permissions || []

            // ✅ user info（可选）
            this.user = {
                id: data.id,
                username: data.username,
                nickname: data.nickname,
                status: data.status
            }

            // ✅ 持久化（关键）
            localStorage.setItem("ADMIN_TOKEN", this.token)
            localStorage.setItem("ADMIN_TOKEN_NAME", this.tokenName)
            localStorage.setItem("ADMIN_ROLES", JSON.stringify(this.roles))
            localStorage.setItem("ADMIN_PERMS", JSON.stringify(this.perms))
            localStorage.setItem("ADMIN_USER", JSON.stringify(this.user))
        },

        logout() {
            removeToken()
            localStorage.removeItem("ADMIN_TOKEN")
            localStorage.removeItem("ADMIN_TOKEN_NAME")
            localStorage.removeItem("ADMIN_ROLES")
            localStorage.removeItem("ADMIN_PERMS")
            localStorage.removeItem("ADMIN_USER")

            this.token = ""
            this.tokenName = "satoken"
            this.roles = []
            this.perms = []
            this.user = {}
        }
    }
})
