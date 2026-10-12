export function resolveMenuPath(basePath = '', routePath = '') {
  if (routePath.startsWith('/')) return routePath
  return `${basePath.replace(/\/$/, '')}/${routePath}`.replace(/\/{2,}/g, '/')
}
export function collectMenuPaths(routes, basePath = '') {
  return routes.flatMap((route) => {
    if (route.hidden) return []
    const path = resolveMenuPath(basePath, route.path)
    return [path, ...collectMenuPaths(route.children || [], path)]
  })
}
