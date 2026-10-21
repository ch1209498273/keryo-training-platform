export interface SessionExpiryActions {
  clearSession: () => void
  clearViews: () => void
  isLoginRoute: () => boolean
  redirectToLogin: () => Promise<unknown> | unknown
}

export const createSessionExpiredHandler = () => {
  let handling = false

  return (actions: SessionExpiryActions): boolean => {
    if (handling) {
      return false
    }

    handling = true

    try {
      actions.clearSession()
      actions.clearViews()

      if (actions.isLoginRoute()) {
        handling = false
        return true
      }

      void Promise.resolve(actions.redirectToLogin()).finally(() => {
        handling = false
      })
      return true
    } catch (error) {
      handling = false
      throw error
    }
  }
}

