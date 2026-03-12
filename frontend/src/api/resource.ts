import { get, post, del } from './http'
import type { ResourceFolder, ResourceFile, PaginatedResult } from '@/types'

function mapFolder(raw: any): ResourceFolder {
  return {
    id: raw.id,
    unit_name: raw.unitName || raw.unit_name || '',
    tags: raw.tags || '',
    file_count: raw.fileCount ?? raw.file_count ?? 0,
    created_at: raw.createdAt || raw.created_at || '',
    updated_at: raw.updatedAt || raw.updated_at || '',
  }
}

function mapFile(raw: any): ResourceFile {
  return {
    id: raw.id,
    folder_id: raw.folderId || raw.folder_id || '',
    outline_id: raw.outlineId || raw.outline_id || '',
    file_name: raw.fileName || raw.file_name || '',
    file_path: raw.filePath || raw.file_path || '',
    file_size: raw.fileSize ?? raw.file_size ?? 0,
    file_type: raw.fileType || raw.file_type || '',
    upload_year: raw.uploadYear ?? raw.upload_year,
    source: raw.source || '',
    uploaded_by: raw.uploadedBy || raw.uploaded_by || '',
    created_at: raw.createdAt || raw.created_at || '',
  }
}

export async function fetchFolders(params: { keyword?: string; page?: number; pageSize?: number }): Promise<PaginatedResult<ResourceFolder>> {
  const raw = await get<any>('/api/resources/folders', params)
  return { data: (raw.data || []).map(mapFolder), total: raw.total || 0 }
}

export async function fetchFolder(id: string): Promise<ResourceFolder> {
  return mapFolder(await get<any>(`/api/resources/folders/${id}`))
}

export async function createFolder(data: Partial<ResourceFolder>): Promise<ResourceFolder> {
  return mapFolder(await post<any>('/api/resources/folders', { unitName: data.unit_name, tags: data.tags }))
}

export async function updateFolder(id: string, data: Partial<ResourceFolder>): Promise<ResourceFolder> {
  const { put } = await import('./http')
  return mapFolder(await put<any>(`/api/resources/folders/${id}`, { unitName: data.unit_name, tags: data.tags }))
}

export async function deleteFolder(id: string): Promise<void> {
  return del<void>(`/api/resources/folders/${id}`)
}

export async function fetchFiles(params: { folderId: string; year?: number; page?: number; pageSize?: number }): Promise<PaginatedResult<ResourceFile>> {
  const raw = await get<any>('/api/resources/files', params)
  return { data: (raw.data || []).map(mapFile), total: raw.total || 0 }
}

export async function createFile(data: Partial<ResourceFile>): Promise<ResourceFile> {
  const body = {
    folderId: data.folder_id, outlineId: data.outline_id,
    fileName: data.file_name, filePath: data.file_path,
    fileSize: data.file_size, fileType: data.file_type,
    uploadYear: data.upload_year, source: data.source,
    uploadedBy: data.uploaded_by,
  }
  return mapFile(await post<any>('/api/resources/files', body))
}

export async function deleteFile(id: string): Promise<void> {
  return del<void>(`/api/resources/files/${id}`)
}

export async function fetchFilesByOutline(outlineId: string): Promise<ResourceFile[]> {
  return []
}

export async function getOrCreateFolder(unitName: string): Promise<ResourceFolder> {
  try {
    const result = await fetchFolders({ keyword: unitName, page: 1, pageSize: 1 })
    const exact = result.data.find(f => f.unit_name === unitName)
    if (exact) return exact
  } catch {}
  return createFolder({ unit_name: unitName, tags: '' })
}
