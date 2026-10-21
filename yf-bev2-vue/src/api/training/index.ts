import request from '@/config/axios'

export const listApi = (data: any) => request.post({ url: '/api/keryo/training/paging', data })
export const detailApi = (data: any) => request.post({ url: '/api/keryo/training/detail', data })
export const createApi = (data: any) => request.post({ url: '/api/keryo/training', data })
export const updateApi = (id: string, data: any) =>
  request.put({ url: `/api/keryo/training/${id}`, data })
export const deleteApi = (id: string) => request.delete({ url: `/api/keryo/training/${id}` })
export const previewApi = (id: string) => request.post({ url: `/api/keryo/training/${id}/preview` })

import { PATH_URL } from '@/config/axios/service'
export const uploadUrl = PATH_URL + '/api/keryo/training/upload'

