import { supabase } from '../supabase'
import bcrypt from 'bcryptjs'

const TABLES = {
  materials: 'chronicle_materials',
  catalogs: 'chronicle_catalogs',
  entries: 'chronicle_entries',
  events: 'chronicle_events',
  reviews: 'chronicle_reviews',
}

function toSnake(obj) {
  if (!obj) return {}
  return Object.fromEntries(
    Object.entries(obj).filter(([, v]) => v !== undefined).map(([k, v]) => [
      k.replace(/([A-Z])/g, '_$1').toLowerCase().replace(/^_/, ''),
      v
    ])
  )
}

export async function login(username, password) {
  const { data: user, error } = await supabase.from('chronicle_users').select('*').eq('username', username).single()
  if (error || !user) throw new Error('用户不存在')
  const ok = await bcrypt.compare(password, user.password)
  if (!ok) throw new Error('密码错误')
  return { token: btoa(JSON.stringify({ sub: username, role: user.role })), username: user.username, role: user.role }
}

export async function getMaterials(params = {}) {
  let q = supabase.from(TABLES.materials).select('*').order('created_at', { ascending: false })
  if (params.title) q = q.ilike('title', `%${params.title}%`)
  if (params.type) q = q.eq('type', params.type)
  const { data, error } = await q
  if (error) throw error
  return { content: data || [], totalElements: data?.length || 0 }
}

export async function getMaterial(id) {
  const { data, error } = await supabase.from(TABLES.materials).select('*').eq('id', id).single()
  if (error) throw error
  return data
}

export async function createMaterial(body) {
  const { data, error } = await supabase.from(TABLES.materials).insert(toSnake(body)).select().single()
  if (error) throw error
  return data
}

export async function updateMaterial(id, body) {
  const { data, error } = await supabase.from(TABLES.materials).update(toSnake(body)).eq('id', id).select().single()
  if (error) throw error
  return data
}

export async function deleteMaterial(id) {
  const { error } = await supabase.from(TABLES.materials).delete().eq('id', id)
  if (error) throw error
}

export async function getCatalogs() {
  const { data, error } = await supabase.from(TABLES.catalogs).select('*').order('order_num')
  if (error) throw error
  return data || []
}

export async function getCatalog(id) {
  const { data, error } = await supabase.from(TABLES.catalogs).select('*').eq('id', id).single()
  if (error) throw error
  return data
}

export async function createCatalog(body) {
  const { data, error } = await supabase.from(TABLES.catalogs).insert(toSnake(body)).select().single()
  if (error) throw error
  return data
}

export async function updateCatalog(id, body) {
  const { data, error } = await supabase.from(TABLES.catalogs).update(toSnake(body)).eq('id', id).select().single()
  if (error) throw error
  return data
}

export async function deleteCatalog(id) {
  const { error } = await supabase.from(TABLES.catalogs).delete().eq('id', id)
  if (error) throw error
}

export async function getEntries(params = {}) {
  let q = supabase.from(TABLES.entries).select('*').order('updated_at', { ascending: false })
  if (params.status) q = q.eq('status', params.status)
  const { data, error } = await q
  if (error) throw error
  return data || []
}

export async function getEntry(id) {
  const { data, error } = await supabase.from(TABLES.entries).select('*').eq('id', id).single()
  if (error) throw error
  return data
}

export async function createEntry(body) {
  const { data, error } = await supabase.from(TABLES.entries).insert(toSnake({ ...body, catalogId: body.catalogId || 1 })).select().single()
  if (error) throw error
  return data
}

export async function updateEntry(id, body) {
  const { data, error } = await supabase.from(TABLES.entries).update(toSnake(body)).eq('id', id).select().single()
  if (error) throw error
  return data
}

export async function getEvents() {
  const { data, error } = await supabase.from(TABLES.events).select('*').order('event_time', { ascending: false })
  if (error) throw error
  return data || []
}

export async function getEvent(id) {
  const { data, error } = await supabase.from(TABLES.events).select('*').eq('id', id).single()
  if (error) throw error
  return data
}

export async function createEvent(body) {
  const { data, error } = await supabase.from(TABLES.events).insert(toSnake(body)).select().single()
  if (error) throw error
  return data
}

export async function updateEvent(id, body) {
  const { data, error } = await supabase.from(TABLES.events).update(toSnake(body)).eq('id', id).select().single()
  if (error) throw error
  return data
}

export async function deleteEvent(id) {
  const { error } = await supabase.from(TABLES.events).delete().eq('id', id)
  if (error) throw error
}

export async function getStatisticsProgress() {
  const { count: total } = await supabase.from(TABLES.entries).select('*', { count: 'exact', head: true })
  const { count: completed } = await supabase.from(TABLES.entries).select('*', { count: 'exact', head: true }).eq('status', 'COMPLETED')
  const { count: inReview } = await supabase.from(TABLES.entries).select('*', { count: 'exact', head: true }).eq('status', 'IN_REVIEW')
  const { count: notStarted } = await supabase.from(TABLES.entries).select('*', { count: 'exact', head: true }).eq('status', 'NOT_STARTED')
  const { count: inProgress } = await supabase.from(TABLES.entries).select('*', { count: 'exact', head: true }).eq('status', 'IN_PROGRESS')
  return { total: total || 0, completed: completed || 0, inReview: inReview || 0, notStarted: notStarted || 0, inProgress: inProgress || 0, completionRate: total ? (completed || 0) / total * 100 : 0 }
}
