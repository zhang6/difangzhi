import { supabase } from './supabase'
import type { Yearbook, PaginatedResult } from '@/types'

export async function fetchYearbooks(params: {
  page: number
  pageSize: number
  keyword?: string
  status?: string
}): Promise<PaginatedResult<Yearbook>> {
  let query = supabase.from('yb_yearbooks').select('*', { count: 'exact' })

  if (params.keyword) {
    query = query.ilike('name', `%${params.keyword}%`)
  }
  if (params.status && params.status !== 'all') {
    query = query.eq('status', params.status)
  }

  const from = (params.page - 1) * params.pageSize
  const to = from + params.pageSize - 1

  const { data, count, error } = await query
    .order('created_at', { ascending: false })
    .range(from, to)

  if (error) throw error
  return { data: data || [], total: count || 0 }
}

export async function createYearbook(yb: Partial<Yearbook>) {
  const { data, error } = await supabase.from('yb_yearbooks').insert(yb).select().single()
  if (error) throw error
  return data
}

export async function updateYearbook(id: string, updates: Partial<Yearbook>) {
  const { data, error } = await supabase
    .from('yb_yearbooks')
    .update({ ...updates, updated_at: new Date().toISOString() })
    .eq('id', id)
    .select()
    .single()
  if (error) throw error
  return data
}

export async function deleteYearbook(id: string) {
  await supabase.from('yb_yearbook_managers').delete().eq('yearbook_id', id)
  await supabase.from('yb_outlines').delete().eq('yearbook_id', id)
  const { error } = await supabase.from('yb_yearbooks').delete().eq('id', id)
  if (error) throw error
}

export async function fetchManagers(yearbookId: string) {
  const { data, error } = await supabase
    .from('yb_yearbook_managers')
    .select('*, user:yb_users(name)')
    .eq('yearbook_id', yearbookId)
  if (error) throw error
  return data || []
}

export async function setManager(yearbookId: string, userId: string) {
  await supabase.from('yb_yearbook_managers').delete().eq('yearbook_id', yearbookId)
  const { data, error } = await supabase
    .from('yb_yearbook_managers')
    .insert({ yearbook_id: yearbookId, user_id: userId })
    .select()
    .single()
  if (error) throw error
  return data
}
