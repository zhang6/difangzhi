import { supabase } from './supabase'
import type { Entry, EntryVersion, Annotation, Draft, PaginatedResult } from '@/types'

export async function fetchEntries(params: {
  outlineId?: string
  page: number
  pageSize: number
  status?: string
}): Promise<PaginatedResult<Entry>> {
  let query = supabase.from('yb_entries').select('*', { count: 'exact' })

  if (params.outlineId) query = query.eq('outline_id', params.outlineId)
  if (params.status && params.status !== 'all') query = query.eq('status', params.status)

  const from = (params.page - 1) * params.pageSize
  const to = from + params.pageSize - 1

  const { data, count, error } = await query
    .order('sort_order')
    .range(from, to)
  if (error) throw error
  return { data: data || [], total: count || 0 }
}

export async function createEntry(entry: Partial<Entry>) {
  const { data, error } = await supabase.from('yb_entries').insert(entry).select().single()
  if (error) throw error
  return data
}

export async function updateEntry(id: string, updates: Partial<Entry>) {
  const { data, error } = await supabase
    .from('yb_entries')
    .update({ ...updates, updated_at: new Date().toISOString() })
    .eq('id', id)
    .select()
    .single()
  if (error) throw error
  return data
}

export async function deleteEntry(id: string) {
  await supabase.from('yb_entry_versions').delete().eq('entry_id', id)
  await supabase.from('yb_annotations').delete().eq('entry_id', id)
  await supabase.from('yb_drafts').delete().eq('entry_id', id)
  const { error } = await supabase.from('yb_entries').delete().eq('id', id)
  if (error) throw error
}

export async function fetchVersions(entryId: string): Promise<EntryVersion[]> {
  const { data, error } = await supabase
    .from('yb_entry_versions')
    .select('*, editor:yb_users!yb_entry_versions_editor_id_fkey(name)')
    .eq('entry_id', entryId)
    .order('version', { ascending: false })
  if (error) throw error
  return (data || []).map((v: any) => ({
    ...v,
    editor_name: v.editor?.name,
    editor: undefined,
  }))
}

export async function createVersion(version: Partial<EntryVersion>) {
  const { data, error } = await supabase
    .from('yb_entry_versions')
    .insert(version)
    .select()
    .single()
  if (error) throw error
  return data
}

export async function fetchAnnotations(entryId: string): Promise<Annotation[]> {
  const { data, error } = await supabase
    .from('yb_annotations')
    .select('*, author:yb_users!yb_annotations_author_id_fkey(name)')
    .eq('entry_id', entryId)
    .order('created_at', { ascending: false })
  if (error) throw error
  return (data || []).map((a: any) => ({
    ...a,
    author_name: a.author?.name,
    author: undefined,
  }))
}

export async function createAnnotation(annotation: Partial<Annotation>) {
  const { data, error } = await supabase
    .from('yb_annotations')
    .insert(annotation)
    .select()
    .single()
  if (error) throw error
  return data
}

export async function updateAnnotationStatus(id: string, status: string) {
  const { error } = await supabase
    .from('yb_annotations')
    .update({ process_status: status, updated_at: new Date().toISOString() })
    .eq('id', id)
  if (error) throw error
}

export async function saveDraft(draft: Partial<Draft>) {
  const { data: existing } = await supabase
    .from('yb_drafts')
    .select('id')
    .eq('entry_id', draft.entry_id!)
    .eq('editor_id', draft.editor_id!)
    .maybeSingle()

  if (existing) {
    const { data, error } = await supabase
      .from('yb_drafts')
      .update({ content: draft.content, created_at: new Date().toISOString() })
      .eq('id', existing.id)
      .select()
      .single()
    if (error) throw error
    return data
  }

  const { data, error } = await supabase.from('yb_drafts').insert(draft).select().single()
  if (error) throw error
  return data
}

export async function fetchHistoryData(keyword: string) {
  const { data, error } = await supabase
    .from('yb_history_data')
    .select('*')
    .or(`entry_title.ilike.%${keyword}%,outline_section.ilike.%${keyword}%`)
    .order('year', { ascending: false })
  if (error) throw error
  return data || []
}
