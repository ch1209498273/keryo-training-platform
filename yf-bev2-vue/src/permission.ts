import router from './router'
import { useAppStoreWithOut } from '@/store/modules/app'
import { useStorage } from '@/hooks/web/useStorage'
import type { RouteRecordRaw } from 'vue-router'
import { useTitle } from '@/hooks/web/useTitle'
import { useNProgress } from '@/hooks/web/useNProgress'
import { usePermissionStoreWithOut } from '@/store/modules/permission'
import { usePageLoading } from '@/hooks/web/usePageLoading'
import { detailApi as fetchSteInfo } from '@/api/sys/config'
import { useUserStore } from '@/store/modules/user'
import brandLoginBg from '@/assets/svgs/login-bg.svg'

const permissionStore = usePermissionStoreWithOut()

const appStore = useAppStoreWithOut()
const userStore = useUserStore()

const { getStorage } = useStorage()

const { start, done } = useNProgress()

const { loadStart, loadDone } = usePageLoading()

const fallbackSiteInfo: SiteInfoTypes = {
  id: 'local-default',
  siteName: '医护培训平台',
  loginLogo: '/logo.png',
  loginBg: brandLoginBg,
  backLogo: '/logo.png',
  copyRight: ''
}

const whiteList = ['/login'] // 不重定向白名单

router.beforeEach(async (to, from, next) => {
  start()
  loadStart()

  console.log('++++to', to)

  // 获取网站基本信息；失败时使用本地默认值，不能阻塞登录页。
  if (!appStore.getSiteInfo || !appStore.getSiteInfo.id) {
    appStore.setSiteInfo(fallbackSiteInfo)
    try {
      const res = await fetchSteInfo({})
      if (res?.data) {
        appStore.setSiteInfo(res.data)
      }
    } catch (error) {
      console.warn('读取网站配置失败，已使用本地默认配置。', error)
    }
  }

  if (userStore.getUserInfo && userStore.getUserInfo.token) {
    if (permissionStore.getIsAddRouters) {
      // 跳转到用户的首页，首个有权限的菜单
      if (to.path === '/' || to.path === '' || to.path === '/login') {
        const firstPath = await getFirstMenu()
        if (firstPath) {
          next({ path: firstPath, replace: true })
          return
        }
      }

      next()
      return
    }

    // 构建路由
    const roleRouters = getStorage('roleRouters') || []
    await permissionStore.generateRoutes(roleRouters as AppCustomRouteRecordRaw[])
    permissionStore.getAddRouters.forEach((route) => {
      router.addRoute(route as unknown as RouteRecordRaw) // 动态添加可访问路由表
    })

    const redirectPath = from.query.redirect || to.path
    const redirect = decodeURIComponent(redirectPath as string)
    const nextData = to.path === redirect ? { ...to, replace: true } : { path: redirect }

    permissionStore.setIsAddRouters(true)
    next(nextData)
  } else {
    if (whiteList.indexOf(to.path) !== -1) {
      next()
    } else {
      next(`/login?redirect=${to.path}`) // 否则全部重定向到登录页
    }
  }
})

// 获取用户权限内的第一个菜单
const getFirstMenu = async () => {
  // 获取本地缓存
  const roleRouters = getStorage('roleRouters') || []
  if (roleRouters.length === 0) {
    return null
  }

  for (const roleRouter of roleRouters) {
    console.log('roleRouter', roleRouter)
    if (roleRouter.menuType === 1) {
      const children = roleRouter.children
      for (const item of children) {
        if (item.menuType === 2 && item.path) {
          return item.path
        }
      }
    }
  }
}

router.afterEach((to) => {
  useTitle(to?.meta?.title as string)
  done() // 结束Progress
  loadDone()
})

