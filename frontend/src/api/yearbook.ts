import { get, post, put, del } from './http'
import type { Yearbook, PaginatedResult } from '@/types'

export async function fetchYearbooks(params: {
  page: number
  pageSize: number
  keyword?: string
  status?: string
}): Promise<PaginatedResult<Yearbook>> {
  const resp = await get<any>('/api/yearbooks', params)
  return {
    data: (resp.data || []).map(mapYearbook),
    total: resp.total || 0,
  }
}

export async function createYearbook(yb: any) {
  return mapYearbook(await post<any>('/api/yearbooks', yb))
}

export async function updateYearbook(id: string, updates: any) {
  return mapYearbook(await put<any>(`/api/yearbooks/${id}`, updates))
}

export async function deleteYearbook(id: string) {
  await del(`/api/yearbooks/${id}`)
}

export async function fetchManagers(yearbookId: string) {
  return get<any[]>(`/api/yearbooks/${yearbookId}/managers`)
}

export async function setManager(yearbookId: string, userId: string) {
  return post<any>(`/api/yearbooks/${yearbookId}/managers`, { userId })
}

function mapYearbook(raw: any): Yearbook {
  return {
    id: raw.id,
    name: raw.name,
    start_date: raw.startDate || raw.start_date,
    end_date: raw.endDate || raw.end_date,
    cover_type: raw.coverType || raw.cover_type || 'default_1',
    cover_url: raw.coverUrl || raw.cover_url,
    status: raw.status || 'not_started',
    progress: raw.progress || 0,
    created_by: raw.createdBy || raw.created_by,
    created_at: raw.createdAt || raw.created_at || '',
    updated_at: raw.updatedAt || raw.updated_at || '',
  }
}
