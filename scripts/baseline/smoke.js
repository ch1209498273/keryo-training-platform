/* Native Node.js fallback for environments without Python. */
const base = (process.env.BASELINE_API_URL || 'http://127.0.0.1:9092').replace(/\/$/, '')
const examId = '1915227067167539202'
const studentId = '1914226980618383361'

async function request(method, path, body, token) {
  const headers = {}
  if (body !== undefined) headers['content-type'] = 'application/json'
  if (token) headers.token = token
  const response = await fetch(base + path, {
    method,
    headers,
    body: body === undefined ? undefined : JSON.stringify(body)
  })
  const text = await response.text()
  try { return JSON.parse(text) } catch { return { code: response.status, msg: text } }
}

function requirePass(label, condition, detail = '') {
  if (!condition) throw new Error(`${label}: ${detail}`)
  console.log(`PASS ${label}`)
}

function requiredCredential(name) {
  const value = process.env[name]
  if (!value) throw new Error(`Set ${name} in the local environment before running API smoke`)
  return value
}

async function login(username, password) {
  const key = `baseline-${username}-${Date.now()}-${Math.random()}`
  const captchaImage = await fetch(`${base}/api/common/captcha/gen?key=${encodeURIComponent(key)}`)
  requirePass(`captcha ${username}`, captchaImage.ok)
  const captcha = await request('GET', `/api/common/captcha/debug?key=${encodeURIComponent(key)}`)
  requirePass(`captcha value ${username}`, captcha.code === 0 && captcha.data, JSON.stringify(captcha))
  const result = await request('POST', '/api/sys/user/login', {
    userName: username,
    password,
    captchaKey: key,
    captchaValue: captcha.data
  })
  requirePass(`login ${username}`, result.code === 0 && result.data && result.data.token, JSON.stringify(result))
  return result.data.token
}

async function main() {
  const admin = await login('admin', requiredCredential('BASELINE_ADMIN_PASSWORD'))
  const manager = await login('nurse_head', requiredCredential('BASELINE_MANAGER_PASSWORD'))
  const student = await login('student', requiredCredential('BASELINE_STUDENT_PASSWORD'))
  for (const [name, token] of [['admin', admin], ['manager', manager], ['student', student]]) {
    const info = await request('POST', '/api/sys/user/info', { token }, token)
    requirePass(`${name} info`, info.code === 0, JSON.stringify(info))
  }

  const training = await request('POST', '/api/keryo/training/paging', { current: 1, size: 10, params: {} }, manager)
  requirePass('training paging', training.code === 0 && training.data && training.data.records, JSON.stringify(training))
  const mine = await request('POST', '/api/keryo/training/my', {}, student)
  requirePass('student training list', mine.code === 0 && mine.data, JSON.stringify(mine))
  const plans = await request('POST', '/api/keryo/training-plan/paging', { current: 1, size: 10, params: {} }, manager)
  requirePass('manager plan paging', plans.code === 0 && plans.data && plans.data.records, JSON.stringify(plans))
  const planDetail = await request('POST', '/api/keryo/training-plan/detail', { id: 'baseline_plan' }, student)
  requirePass('student plan detail', planDetail.code === 0 && planDetail.data && planDetail.data.id === 'baseline_plan', JSON.stringify(planDetail))
  const participants = await request('POST', '/api/keryo/training-plan/participants', { planId: 'baseline_plan' }, manager)
  requirePass('plan participants', participants.code === 0 && participants.data, JSON.stringify(participants))

  const runId = `baseline_${Date.now()}`
  const created = await request('POST', '/api/keryo/training-plan/save', {
    id: runId, title: runId, content: 'repeatable smoke flow', examId,
    coursewareId: 'baseline_training', status: 'DRAFT',
    startTime: '2020-01-01 00:00:00', endTime: '2050-12-31 23:59:59'
  }, manager)
  requirePass('manager creates plan', created.code === 0 && created.data === runId, JSON.stringify(created))
  requirePass('manager assigns student', (await request('POST', '/api/keryo/training-plan/assign', { planId: runId, userIds: [studentId] }, manager)).code === 0)
  requirePass('manager submits plan', (await request('POST', '/api/keryo/training-plan/submit', { id: runId }, manager)).code === 0)
  requirePass('admin approves plan', (await request('POST', '/api/keryo/training-plan/approve', { id: runId, remark: 'baseline' }, admin)).code === 0)

  const exams = await request('POST', '/api/exam/exam/exam/client-paging', { current: 1, size: 10, params: {} }, student)
  requirePass('student exam list', exams.code === 0 && exams.data && exams.data.records, JSON.stringify(exams))
  const precheck = await request('POST', '/api/exam/paper/paper/pre-check', { id: examId }, student)
  requirePass('exam pre-check', precheck.code === 0 && precheck.data && precheck.data.validated === true, JSON.stringify(precheck))
  const paper = await request('POST', '/api/exam/paper/paper/create', { id: examId }, student)
  const paperId = paper.data && paper.data.id
  requirePass('create paper', paper.code === 0 && paperId, JSON.stringify(paper))
  const card = await request('POST', '/api/exam/paper/qu/list-card', { id: paperId }, student)
  requirePass('load answer card', card.code === 0 && card.data, JSON.stringify(card))
  const handed = await request('POST', '/api/exam/paper/paper/hand', { id: paperId }, student)
  requirePass('submit paper', handed.code === 0, JSON.stringify(handed))
  const records = await request('POST', '/api/exam/exam/record/client-paging', { current: 1, size: 10, params: {} }, student)
  requirePass('student result list', records.code === 0 && records.data && records.data.records, JSON.stringify(records))
  requirePass('logout', (await request('POST', '/api/sys/user/logout', {}, student)).code === 0)
  console.log('BASELINE_SMOKE_PASS')
}

main().catch((error) => { console.error(`BASELINE_SMOKE_FAIL ${error.message}`); process.exitCode = 1 })

