import * as supabaseApi from './supabase-api'

function wrap(v) {
  return Promise.resolve({ data: v })
}

export default {
  get: (url, config) => {
    const path = url.replace('/api/', '')
    if (path.startsWith('materials')) {
      if (path.match(/\/\d+$/)) return supabaseApi.getMaterial(path.split('/')[1]).then(wrap)
      return supabaseApi.getMaterials(config?.params || {}).then(wrap)
    }
    if (path.startsWith('catalogs')) {
      if (path === 'catalogs/tree' || path === 'catalogs') return supabaseApi.getCatalogs().then(wrap)
      if (path.match(/\/\d+$/)) return supabaseApi.getCatalog(path.split('/').pop()).then(wrap)
    }
    if (path.startsWith('entries')) {
      if (path.match(/\/\d+$/)) return supabaseApi.getEntry(path.split('/')[1]).then(wrap)
      return supabaseApi.getEntries(config?.params || {}).then(wrap)
    }
    if (path.startsWith('events')) {
      if (path.match(/\/\d+$/)) return supabaseApi.getEvent(path.split('/')[1]).then(wrap)
      return supabaseApi.getEvents().then(wrap)
    }
    if (path === 'statistics/progress') return supabaseApi.getStatisticsProgress().then(wrap)
    if (path.startsWith('knowledge')) return wrap({ answer: '（AI 服务未部署）' })
    return Promise.reject(new Error('Unknown API: ' + path))
  },
  post: (url, data, config) => {
    const path = url.replace('/api/', '')
    if (path.startsWith('materials')) {
      if (path.includes('upload')) return supabaseApi.createMaterial(data || { title: '新资料' }).then(wrap)
      if (path.match(/\/\d+\/ai-summary/)) return wrap({ summary: '（AI 服务未部署）' })
      return supabaseApi.createMaterial(data || {}).then(wrap)
    }
    if (path.startsWith('catalogs')) return supabaseApi.createCatalog(data || {}).then(wrap)
    if (path.startsWith('entries')) {
      if (path.match(/\/\d+\/ai-generate/)) return wrap({ content: '（AI 服务未部署）' })
      return supabaseApi.createEntry(data || {}).then(wrap)
    }
    if (path.startsWith('events')) {
      if (path.includes('ai-generate')) return wrap([])
      return supabaseApi.createEvent(data || {}).then(wrap)
    }
    if (path.startsWith('knowledge')) return wrap({ answer: '（AI 服务未部署）' })
    return Promise.reject(new Error('Unknown API: ' + path))
  },
  put: (url, data) => {
    const path = url.replace('/api/', '')
    const id = path.split('/')[1]
    if (path.startsWith('materials/')) return supabaseApi.updateMaterial(id, data).then(wrap)
    if (path.startsWith('catalogs/')) return supabaseApi.updateCatalog(id, data).then(wrap)
    if (path.startsWith('entries/')) return supabaseApi.updateEntry(id, data).then(wrap)
    if (path.startsWith('events/')) return supabaseApi.updateEvent(id, data).then(wrap)
    return Promise.reject(new Error('Unknown API: ' + path))
  },
  delete: (url) => {
    const path = url.replace('/api/', '')
    const id = path.split('/')[1]
    if (path.startsWith('materials/')) return supabaseApi.deleteMaterial(id)
    if (path.startsWith('catalogs/')) return supabaseApi.deleteCatalog(id)
    if (path.startsWith('events/')) return supabaseApi.deleteEvent(id)
    return Promise.reject(new Error('Unknown API: ' + path))
  },
}
