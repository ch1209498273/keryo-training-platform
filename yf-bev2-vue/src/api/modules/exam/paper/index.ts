import request from '@/config/axios'

export const createApi = (data: any) => {
  return request.post({
    url: '/api/exam/paper/paper/create',
    data
  })
}

// 查找答题卡信息
export const quCardApi = (data: any) => {
  return request.post({
    url: '/api/exam/paper/qu/list-card',
    data
  })
}

// 查找详情用于答题
export const quDetailApi = (data: any) => {
  return request.post({
    url: '/api/exam/paper/qu/detail-for-answer',
    data
  })
}

// 交卷操作
export const handApi = (data: any) => {
  return request.post({
    url: '/api/exam/paper/paper/hand',
    data
  })
}

// 答题操作
export const fillAnswerApi = (data: any) => {
  return request.post({
    url: '/api/exam/paper/qu/fill-answer',
    data
  })
}

// 考试校验
export const preCheckApi = (data: any) => {
  return request.post({
    url: '/api/exam/paper/paper/pre-check',
    data
  })
}
// 考试详情
export const paperDetailApi = (data: any) => {
  return request.post({
    url: '/api/exam/paper/paper/detail',
    data
  })
}

// 考试实时状态
export const realTimeStateApi = (data: any) => {
  return request.post({
    url: '/api/exam/paper/paper/real-time-state',
    data
  })
}


// 考试详情-试题列表
export const paperFullDetailApi = (data: any) => {
  return request.post({
    url: '/api/exam/paper/paper/full-detail',
    data
  })
}

// 上报切屏事件（T-05 防切屏）
export const cheatReportApi = (data: any) => {
  return request.post({
    url: '/api/exam/cheat/report',
    data
  })
}

// 查询违规记录（T-05 防切屏）
export const cheatListApi = (data: any) => {
  return request.post({
    url: '/api/exam/cheat/list',
    data
  })
}

// 学员查询自己的违规记录（T-05 考试结果页分析报告）
export const cheatListMineApi = (data: any) => {
  return request.post({
    url: '/api/exam/cheat/list-mine',
    data
  })
}

// ESSAY 主观题人工阅卷（T-05）
export const reviewEssayApi = (data: any) => {
  return request.post({
    url: '/api/exam/paper/qu/review-essay',
    data
  })
}
