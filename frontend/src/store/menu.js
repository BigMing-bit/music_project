import { defineStore } from "pinia"
import { ref } from "vue"
import { getAdminMenuTree } from "@/api/admin/menu"

export const useMenuStore = defineStore("menu", () => {
    const menus = ref([])

    async function refreshMenus() {
        const res = await getAdminMenuTree() // ✅ 左侧菜单用这个
        menus.value = res.data || []
    }

    return { menus, refreshMenus }
})
