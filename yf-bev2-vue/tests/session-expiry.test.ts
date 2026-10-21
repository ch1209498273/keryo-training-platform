import assert from 'node:assert/strict'
import test from 'node:test'
import { createSessionExpiredHandler } from '../src/config/axios/sessionExpiry.ts'
import { LOGIN_REQUEST_TIMEOUT } from '../src/api/login/loginTimeout.ts'
import { CAPTCHA_EXPIRE_MS, isCaptchaExpiredMessage } from '../src/components/InputCaptcha/src/captchaExpiry.ts'

test('session expiry clears local state and schedules only one login redirect', async () => {
  const handleSessionExpired = createSessionExpiredHandler()

  let clearSessionCalls = 0
  let clearViewsCalls = 0
  let redirectCalls = 0
  let releaseRedirect!: () => void
  const redirectPending = new Promise<void>((resolve) => {
    releaseRedirect = resolve
  })

  const actions = {
    clearSession: () => {
      clearSessionCalls += 1
    },
    clearViews: () => {
      clearViewsCalls += 1
    },
    isLoginRoute: () => false,
    redirectToLogin: () => {
      redirectCalls += 1
      return redirectPending
    }
  }

  assert.equal(handleSessionExpired(actions), true)
  assert.equal(handleSessionExpired(actions), false)
  assert.equal(clearSessionCalls, 1)
  assert.equal(clearViewsCalls, 1)
  assert.equal(redirectCalls, 1)

  releaseRedirect()
  await redirectPending
})

test('session expiry clears local state without navigating when already on login', () => {
  const handleSessionExpired = createSessionExpiredHandler()

  let clearSessionCalls = 0
  let clearViewsCalls = 0
  let redirectCalls = 0

  const handled = handleSessionExpired({
    clearSession: () => {
      clearSessionCalls += 1
    },
    clearViews: () => {
      clearViewsCalls += 1
    },
    isLoginRoute: () => true,
    redirectToLogin: () => {
      redirectCalls += 1
    }
  })

  assert.equal(handled, true)
  assert.equal(clearSessionCalls, 1)
  assert.equal(clearViewsCalls, 1)
  assert.equal(redirectCalls, 0)
})

test('login verification request is limited to three seconds', () => {
  assert.equal(LOGIN_REQUEST_TIMEOUT, 3000)
})

test('captcha expiry is aligned with the backend and expired messages are recognized', () => {
  assert.equal(CAPTCHA_EXPIRE_MS, 300000)
  assert.equal(isCaptchaExpiredMessage('图形验证码不正确或已失效！'), true)
  assert.equal(isCaptchaExpiredMessage('用户名或密码错误'), false)
})

