import { get, post, put, del } from './http'
import type { Yearbook, PaginatedResult } from '@/types'

function mapYearbook(raw: any): Yearbook {
  return {
    id: raw.id,
    name: raw.name,
    start_date: raw.startDate || raw.start_date || '',
    end_date: raw.endDate || raw.end_date || '',
    cover_type: raw.coverType || raw.cover_type || 'default_1',
    cover_url: raw.coverUrl || raw.cover_url || '',
    status: raw.status || 'not_started',
    progress: raw.progress || 0,
    created_by: raw.createdBy || raw.created_by || '',
    created_at: raw.createdAt || raw.created_at || '',
    updated_at: raw.updatedAt || raw.updated_at || '',
  }
}

export async function fetchYearbooks(params: { page?: number; pageSize?: number; keyword?: string; status?: string }): Promise<PaginatedResult<Yearbook>> {
  const raw = await get<any>('/api/yearbooks', params)
  return { data: (raw.data || []).map(mapYearbook), total: raw.total || 0 }
}

export async function fetchStats(): Promise<Record<string, number>> {
  return get<any>('/api/yearbooks/stats')
}

export async function fetchYearbook(id: string): Promise<Yearbook> {
  return mapYearbook(await get<any>(`/api/yearbooks/${id}`))
}

export async function createYearbook(data: Partial<Yearbook>): Promise<Yearbook> {
  const body = {
    name: data.name,
    startDate: data.start_date,
    endDate: data.end_date,
    coverType: data.cover_type,
    status: data.status,
    progress: data.progress || 0,
    createdBy: data.created_by,
  }
  return mapYearbook(await post<any>('/api/yearbooks', body))
}

export async function updateYearbook(id: string, data: Partial<Yearbook>): Promise<Yearbook> {
  const body: any = {}
  if (data.name) body.name = data.name
  if (data.start_date) body.startDate = data.start_date
  if (data.end_date) body.endDate = data.end_date
  if (data.cover_type) body.coverType = data.cover_type
  if (data.status) body.status = data.status
  if (data.progress !== undefined) body.progress = data.progress
  return mapYearbook(await put<any>(`/api/yearbooks/${id}`, body))
}

export async function deleteYearbook(id: string): Promise<void> {
  return del<void>(`/api/yearbooks/${id}`)
}

export async function fetchManagers(yearbookId: string): Promise<any[]> {
  return get<any[]>(`/api/yearbooks/${yearbookId}/managers`)
}

export async function addManager(yearbookId: string, userId: string): Promise<any> {
  return post<any>(`/api/yearbooks/${yearbookId}/managers`, { userId })
}

export async function removeManagerApi(yearbookId: string, userId: string): Promise<void> {
  return del<void>(`/api/yearbooks/${yearbookId}/managers/${userId}`)
}
