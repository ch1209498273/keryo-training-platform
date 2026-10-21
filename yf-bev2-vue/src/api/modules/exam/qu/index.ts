import request from '@/config/axios'

export const saveApi = (data: any) => {
  return request.post({
    url: '/api/exam/repo/qu/save',
    data
  })
}

export const detailApi = (data: any) => {
  return request.post({
    url: '/api/exam/repo/qu/detail',
    data
  })
}

export const reviewApi = (data: any) => {
  return request.post({
    url: '/api/exam/repo/qu/review',
    data
  })
}

export const importApi = (data: FormData) => {
  return request.post({
    url: '/api/exam/repo/qu/import',
    data,
    // 封装默认强制 application/json，FormData 必须显式 multipart（否则后端 500 "not a multipart request"）
    headersType: 'multipart/form-data'
  })
}

