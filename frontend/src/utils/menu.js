export function filterMenuByRoles(menuTree, roles = []) {
    const hasRole = (item) => {
        if (!item.roles || item.roles.length === 0) return true
        return item.roles.some(r => roles.includes(r))
    }

    const loop = (nodes) =>
        nodes
            .filter(hasRole)
            .map(n => ({
                ...n,
                children: n.children ? loop(n.children) : undefined
            }))
            .filter(n => !n.children || n.children.length > 0)

    return loop(menuTree)
}
