import { get, post, put, del } from './http'
import type { Entry, EntryVersion, Annotation, HistoryData, PaginatedResult } from '@/types'

function mapEntry(raw: any): Entry {
  return {
    id: raw.id,
    outline_id: raw.outlineId || raw.outline_id || '',
    title: raw.title || '',
    original_content: raw.originalContent ?? raw.original_content ?? '',
    ai_content: raw.aiContent ?? raw.ai_content ?? '',
    sort_order: raw.sortOrder ?? raw.sort_order ?? 0,
    status: raw.status || 'draft',
    created_by: raw.createdBy || raw.created_by || '',
    created_at: raw.createdAt || raw.created_at || '',
    updated_at: raw.updatedAt || raw.updated_at || '',
  }
}

function mapVersion(raw: any): EntryVersion {
  return {
    id: raw.id,
    entry_id: raw.entryId || raw.entry_id || '',
    version: raw.version || 1,
    content: raw.content || '',
    revision_note: raw.revisionNote || raw.revision_note || '',
    editor_id: raw.editorId || raw.editor_id || '',
    editor_name: raw.editor?.name || '',
    created_at: raw.createdAt || raw.created_at || '',
  }
}

function mapAnnotation(raw: any): Annotation {
  return {
    id: raw.id,
    entry_id: raw.entryId || raw.entry_id || '',
    content: raw.content || '',
    author_id: raw.authorId || raw.author_id || '',
    author_name: raw.author?.name || '',
    annotation_type: raw.annotationType || raw.annotation_type || 'comment',
    process_status: raw.processStatus || raw.process_status || 'pending',
    created_at: raw.createdAt || raw.created_at || '',
    updated_at: raw.updatedAt || raw.updated_at || '',
  }
}

export async function fetchEntries(params: { outlineId: string; page?: number; pageSize?: number }): Promise<PaginatedResult<Entry>> {
  const raw = await get<any>('/api/entries', params)
  return { data: (raw.data || []).map(mapEntry), total: raw.total || 0 }
}

export async function fetchEntry(id: string): Promise<Entry> {
  return mapEntry(await get<any>(`/api/entries/${id}`))
}

export async function createEntry(data: Partial<Entry>): Promise<Entry> {
  const body = {
    outlineId: data.outline_id, title: data.title,
    originalContent: data.original_content, aiContent: data.ai_content,
    sortOrder: data.sort_order, status: data.status, createdBy: data.created_by,
  }
  return mapEntry(await post<any>('/api/entries', body))
}

export async function updateEntry(id: string, data: Partial<Entry>): Promise<Entry> {
  const body: any = {}
  if (data.title !== undefined) body.title = data.title
  if (data.original_content !== undefined) body.originalContent = data.original_content
  if (data.ai_content !== undefined) body.aiContent = data.ai_content
  if (data.status !== undefined) body.status = data.status
  if (data.sort_order !== undefined) body.sortOrder = data.sort_order
  return mapEntry(await put<any>(`/api/entries/${id}`, body))
}

export async function deleteEntry(id: string): Promise<void> {
  return del<void>(`/api/entries/${id}`)
}

export async function fetchVersions(entryId: string): Promise<EntryVersion[]> {
  const result = await get<any[]>(`/api/entries/${entryId}/versions`)
  return result.map(mapVersion)
}

export async function createVersion(data: Partial<EntryVersion>): Promise<EntryVersion> {
  const body = {
    version: data.version, content: data.content,
    revisionNote: data.revision_note, editorId: data.editor_id,
  }
  return mapVersion(await post<any>(`/api/entries/${data.entry_id}/versions`, body))
}

export async function fetchAnnotations(entryId: string): Promise<Annotation[]> {
  const result = await get<any[]>(`/api/entries/${entryId}/annotations`)
  return result.map(mapAnnotation)
}

export async function createAnnotation(data: Partial<Annotation>): Promise<Annotation> {
  const body = { content: data.content, authorId: data.author_id, annotationType: data.annotation_type }
  return mapAnnotation(await post<any>(`/api/entries/${data.entry_id}/annotations`, body))
}

export async function updateAnnotation(id: string, data: Partial<Annotation>): Promise<Annotation> {
  return mapAnnotation(await put<any>(`/api/entries/annotations/${id}`, {
    content: data.content, processStatus: data.process_status,
  }))
}

export async function deleteAnnotation(id: string): Promise<void> {
  return del<void>(`/api/entries/annotations/${id}`)
}

export async function fetchHistoryData(keyword: string): Promise<HistoryData[]> {
  const result = await get<any[]>('/api/entries/history', { keyword })
  return result.map(raw => ({
    id: raw.id,
    yearbook_name: raw.yearbookName || raw.yearbook_name || '',
    outline_section: raw.outlineSection || raw.outline_section || '',
    entry_title: raw.entryTitle || raw.entry_title || '',
    content: raw.content || '',
    year: raw.year || new Date().getFullYear(),
    created_at: raw.createdAt || raw.created_at || '',
  }))
}
