import request from '@/config/axios'
import { LOGIN_REQUEST_TIMEOUT } from './loginTimeout'

export const apiLogin = (data) => {
  return request.post({
    url: '/api/sys/user/login',
    data,
    requestTimeout: LOGIN_REQUEST_TIMEOUT
  })
}

// 用户注册
export const apiRegister = (data) => {
  return request.post({
    url: '/api/sys/user/reg',
    data
  })
}

// 忘记密码申请校验（确认用户名存在，引导联系管理员）
export const apiForgetCheck = (data) => {
  return request.post({
    url: '/api/sys/user/forget-check',
    data
  })
}

export const logoutApi = () => {
  return request.post({ url: '/api/sys/user/logout' })
}

export const routesApi = (data: any) => {
  return request.post({
    url: '/api/sys/menu/routes',
    data
  })
}

