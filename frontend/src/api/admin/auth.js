import request from '@/utils/request'

export function adminLogin(data) {
    return request({
        url: '/admin/login',
        method: 'post',
        data
    })
}

export function adminLogout() {
    return request({
        url: '/admin/logout',
        method: 'post'
    })
}
