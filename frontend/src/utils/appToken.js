const KEY = "USER_TOKEN"

export const setAppToken = (t) => localStorage.setItem(KEY, t)
export const getAppToken = () => localStorage.getItem(KEY) || ""
export const removeAppToken = () => localStorage.removeItem(KEY)
