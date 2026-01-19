import { defineStore } from "pinia"
import { userLogin } from "@/api/app/user"
import { setAppToken, removeAppToken, getAppToken } from "@/utils/appToken"

export const useUserStore = defineStore("user", {
    state: () => ({
        token: getAppToken() || "",
        user: JSON.parse(localStorage.getItem("USER_INFO") || "{}"),
        role: 0,  // 新增：角色字段，0为普通用户，1为官方用户
    }),

    getters: {
        isLogin: (s) => !!s.token,
        nickname: (s) => s.user?.nickname || s.user?.username || "用户",
        avatar: (s) => s.user?.avatarUrl || s.user?.avatar || "",
        email: (s) => s.user?.email || "未设置",
        phone: (s) => s.user?.phone || "未设置",
        isOfficial: (s) => s.role === 1, // 判断是否为官方用户
    },

    actions: {
        async login(userData) {
            const tokenValue = userData?.tokenValue;
            if (!tokenValue) throw new Error("登录失败：未返回 tokenValue");

            this.token = tokenValue;
            setAppToken(tokenValue);

            if (userData) {
                this.user = userData;
                this.user.id = userData.id;
                this.role = userData.role; // 根据返回的数据设置角色
                localStorage.setItem("USER_INFO", JSON.stringify(userData));
            }
        },

        logout() {
            this.token = "";
            this.user = {};
            this.role = 0;  // 清除角色
            removeAppToken();
            localStorage.removeItem("USER_INFO");
        },

        hydrate() {
            this.token = getAppToken() || "";
            this.user = JSON.parse(localStorage.getItem("USER_INFO") || "{}");
            this.role = this.user.role || 0;  // 从本地存储加载角色

            if (!this.user.id) {
                console.warn('用户信息未加载，userId 为 undefined');
            } else {
                console.log('已加载用户信息:', this.user.id);
            }
        }
    },
});


