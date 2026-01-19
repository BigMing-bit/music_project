function hasRoleAccess(route, roles) {
    const needRoles = route.meta?.roles
    if (!needRoles || needRoles.length === 0) return true
    return needRoles.some(r => roles.includes(r))
}

export function filterRoutesByRoles(routes, roles) {
    return routes
        .filter(r => hasRoleAccess(r, roles))
        .map(r => ({
            ...r,
            children: r.children ? filterRoutesByRoles(r.children, roles) : undefined
        }))
        .filter(r => !r.children || r.children.length > 0)
}
