export const CAPTCHA_EXPIRE_MS = 5 * 60 * 1000

export const isCaptchaExpiredMessage = (message: unknown): boolean => {
  const text = message instanceof Error ? message.message : message
  return typeof text === 'string' && text.includes('验证码')
}

