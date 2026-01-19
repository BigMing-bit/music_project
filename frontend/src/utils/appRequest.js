import axios from "axios"
import { ElMessage } from "element-plus"
import { getAppToken } from "@/utils/appToken"

const appService = axios.create({ baseURL: "/api", timeout: 10000 })

appService.interceptors.request.use((config) => {
    const token = getAppToken()
    const tokenName = "satoken"
    if (token) config.headers[tokenName] = token
    return config
})

appService.interceptors.response.use(
    (res) => {
        const data = res.data
        if (data.code !== 0) return Promise.reject(data)
        return data
    },
    (err) => {
        if (err.response?.status === 401) {
            ElMessage.warning("请先登录")
        } else {
            ElMessage.error(err.message || "网络错误")
        }
        return Promise.reject(err)
    }
)

export default appService
