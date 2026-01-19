import request from "@/utils/request"

// 分页列表（菜单管理页面用）
export function getMenuList(params) {
    return request({
        url: "/admin/menus",
        method: "get",
        params
    })
}

// 新增
export function addMenu(data) {
    return request({
        url: "/admin/menus",
        method: "post",
        data
    })
}

// 更新
export function updateMenu(data) {
    return request({
        url: "/admin/menus",
        method: "put",
        data
    })
}

// 删除
export function deleteMenu(id) {
    return request({
        url: `/admin/menus/${id}`,
        method: "delete"
    })
}

// 删除（批量）
export function deleteMenus(ids) {
    return request({
        url: "/admin/menus",
        method: "delete",
        data: ids
    })
}

// 状态更新
export function updateMenuStatus(id, status) {
    return request({
        url: `/admin/menus/${id}/status`,
        method: "put",
        data: { status }
    })
}

// 详情
export function getMenuInfo(id) {
    return request({
        url: `/admin/menus/${id}`,
        method: "get"
    })
}

/**
 * ✅ 菜单管理页面：全量菜单树（需要 system:menu:view）
 */
export function getMenuTree(status = 1) {
    return request({
        url: "/admin/menus/tree",
        method: "get",
        params: { status }
    })
}

/**
 * ✅ 左侧动态菜单：按当前登录管理员权限过滤后的树（只要登录）
 */
export function getAdminMenuTree() {
    return request({
        url: "/admin/menus/router-tree",
        method: "get",
    })
}
