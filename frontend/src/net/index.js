import axios from "axios"
import {ElMessage} from "element-plus";

const authItemName = "authorize"

const accessTokenHeader = () => {
    return {
        'Authorization': `Bearer ${takeAccessToken()}`
    }
}

const defaultFailure = (message, code, url) => {
    console.warn(`请求地址：${url}失败，错误码：${code}，错误信息：${message}`)
    ElMessage.warning(message)
}

const defaultError = (err) => {
    console.warn(err)
    ElMessage.warning('发生错误')
}

function internalPost(url, data, headers, success, failure, error = defaultError) {
    axios.post(url, data, {headers: headers}).then(({data}) => {
        if (data.code === 200) {
            success(data.data)
        } else {
            failure(data.message, data.code, url)
        }
    }).catch(err => error(err))
}

function post(url, data, success, failure = defaultFailure) {
    internalPost(url, data, accessTokenHeader(), success, failure)
}

function internalGet(url, headers, success, failure, error = defaultError) {
    axios.get(url, {headers: headers}).then(({data}) => {
        if (data.code === 200) {
            success(data.data)
        } else {
            failure(data.message, data.code, url)
        }
    }).catch(err => error(err))
}

function get(url, success, failure = defaultFailure) {
    internalGet(url, accessTokenHeader(), success, failure)
}

function takeAccessToken() {
    const str = localStorage.getItem(authItemName) || sessionStorage.getItem(authItemName)
    if (!str) {
        return null
    }
    // 重新封装为JSON
    const authObj = JSON.parse(str)
    if (authObj.expire <= new Date()) {
        deleteAccessToken()
        ElMessage.warning('登录已过期，请重新登录!')
        return null
    }
    return authObj.token
}

function storeAccessToken(remember, token, expire) {
    const authObj = {token: token, expire: expire}
    const str = JSON.stringify(authObj)
    if (remember) {
        localStorage.setItem(authItemName, str)
    } else {
        sessionStorage.setItem(authItemName, str)
    }
}

function deleteAccessToken() {
    localStorage.removeItem(authItemName)
    sessionStorage.removeItem(authItemName)
}

function unauthorized() {
    return !takeAccessToken()
}


function login(username, password, remember, success, failure = defaultFailure) {
    internalPost('/api/auth/login',
        {
            username: username,
            password: password
        }, {
            'Content-Type': 'application/x-www-form-urlencoded'
        }, (data) => {
            storeAccessToken(remember, data.token, data.expire)
            ElMessage.success(`登录成功，欢迎${data.username}`)
            success(data)
        }, failure)
}

function logout(success, failure = defaultFailure) {
    get(('/api/auth/logout'), () => {
        deleteAccessToken()
        ElMessage.success('退出登录成功')
        success()
    }, failure)
}

export {login, logout, post, get, unauthorized}