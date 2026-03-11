import { get, post, put } from './http'
import type { Entry, EntryVersion, Annotation, PaginatedResult, HistoryData } from '@/types'

export async function fetchEntries(params: {
  outlineId?: string
  page: number
  pageSize: number
  status?: string
}): Promise<PaginatedResult<Entry>> {
  const resp = await get<any>('/api/entries/paged', {
    outlineId: params.outlineId,
    page: params.page,
    pageSize: params.pageSize,
  })
  return {
    data: (resp.data || []).map(mapEntry),
    total: resp.total || 0,
  }
}

export async function createEntry(entry: any) {
  return mapEntry(await post<any>('/api/entries', {
    outlineId: entry.outline_id || entry.outlineId,
    title: entry.title,
    sortOrder: entry.sort_order ?? entry.sortOrder ?? 0,
    status: entry.status || 'draft',
    createdBy: entry.created_by || entry.createdBy,
  }))
}

export async function updateEntry(id: string, updates: any) {
  return mapEntry(await put<any>(`/api/entries/${id}`, {
    title: updates.title,
    originalContent: updates.original_content ?? updates.originalContent,
    aiContent: updates.ai_content ?? updates.aiContent,
    status: updates.status,
    sortOrder: updates.sort_order ?? updates.sortOrder,
  }))
}

export async function deleteEntry(id: string) {
  const { del: delFn } = await import('./http')
  await delFn(`/api/entries/${id}`)
}

export async function fetchVersions(entryId: string): Promise<EntryVersion[]> {
  const data = await get<any[]>(`/api/entries/${entryId}/versions`)
  return (data || []).map(mapVersion)
}

export async function createVersion(version: any) {
  return post<any>(`/api/entries/${version.entry_id || version.entryId}/versions`, {
    version: version.version,
    content: version.content,
    revisionNote: version.revision_note || version.revisionNote,
    editorId: version.editor_id || version.editorId,
  })
}

export async function fetchAnnotations(entryId: string): Promise<Annotation[]> {
  const data = await get<any[]>(`/api/entries/${entryId}/annotations`)
  return (data || []).map(mapAnnotation)
}

export async function createAnnotation(annotation: any) {
  return post<any>(`/api/entries/${annotation.entry_id || annotation.entryId}/annotations`, {
    content: annotation.content,
    authorId: annotation.author_id || annotation.authorId,
    annotationType: annotation.annotation_type || annotation.annotationType || 'mine',
  })
}

export async function updateAnnotationStatus(id: string, status: string) {
  return put<any>(`/api/entries/annotations/${id}/status`, { status })
}

export async function fetchHistoryData(keyword: string): Promise<HistoryData[]> {
  const data = await get<any[]>('/api/entries/history', { keyword })
  return (data || []).map((h: any) => ({
    id: h.id,
    yearbook_name: h.yearbookName || h.yearbook_name,
    outline_section: h.outlineSection || h.outline_section,
    entry_title: h.entryTitle || h.entry_title,
    content: h.content,
    year: h.year,
    created_at: h.createdAt || h.created_at || '',
  }))
}

function mapEntry(raw: any): Entry {
  return {
    id: raw.id,
    outline_id: raw.outlineId || raw.outline_id,
    title: raw.title,
    original_content: raw.originalContent || raw.original_content,
    ai_content: raw.aiContent || raw.ai_content,
    sort_order: raw.sortOrder ?? raw.sort_order ?? 0,
    status: raw.status || 'draft',
    created_by: raw.createdBy || raw.created_by,
    created_at: raw.createdAt || raw.created_at || '',
    updated_at: raw.updatedAt || raw.updated_at || '',
  }
}

function mapVersion(raw: any): EntryVersion {
  return {
    id: raw.id,
    entry_id: raw.entryId || raw.entry_id,
    version: raw.version,
    content: raw.content,
    revision_note: raw.revisionNote || raw.revision_note,
    editor_id: raw.editorId || raw.editor_id,
    editor_name: raw.editorName || raw.editor_name,
    created_at: raw.createdAt || raw.created_at || '',
  }
}

function mapAnnotation(raw: any): Annotation {
  return {
    id: raw.id,
    entry_id: raw.entryId || raw.entry_id,
    content: raw.content,
    author_id: raw.authorId || raw.author_id,
    author_name: raw.authorName || raw.author_name,
    annotation_type: raw.annotationType || raw.annotation_type || 'mine',
    process_status: raw.processStatus || raw.process_status || 'pending',
    created_at: raw.createdAt || raw.created_at || '',
    updated_at: raw.updatedAt || raw.updated_at || '',
  }
}
