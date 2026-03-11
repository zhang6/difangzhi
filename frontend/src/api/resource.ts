import { get, post, put, del } from './http'
import { supabase } from './supabase'
import type { ResourceFolder, ResourceFile, PaginatedResult } from '@/types'

export async function fetchFolders(params: {
  page: number
  pageSize: number
  keyword?: string
}): Promise<PaginatedResult<ResourceFolder>> {
  const resp = await get<any>('/api/resources/folders', params)
  return {
    data: (resp.data || []).map(mapFolder),
    total: resp.total || 0,
  }
}

export async function createFolder(folder: any) {
  return mapFolder(await post<any>('/api/resources/folders', folder))
}

export async function updateFolder(id: string, updates: any) {
  return mapFolder(await put<any>(`/api/resources/folders/${id}`, updates))
}

export async function deleteFolder(id: string) {
  await del(`/api/resources/folders/${id}`)
}

export async function fetchFiles(params: {
  folderId: string
  page: number
  pageSize: number
  year?: number | null
}): Promise<PaginatedResult<ResourceFile>> {
  const resp = await get<any>('/api/resources/files', {
    folderId: params.folderId,
    page: params.page,
    pageSize: params.pageSize,
    year: params.year || undefined,
  })
  return {
    data: (resp.data || []).map(mapFile),
    total: resp.total || 0,
  }
}

export async function uploadFile(
  file: File,
  folderId: string,
  userId: string,
  year?: number
) {
  const path = `materials/${folderId}/${Date.now()}_${file.name}`
  const { error: uploadError } = await supabase.storage.from('yearbook-files').upload(path, file)
  if (uploadError) throw uploadError

  return post<any>('/api/resources/files', {
    folderId,
    fileName: file.name,
    fileSize: file.size,
    fileType: file.name.split('.').pop()?.toLowerCase() || '',
    filePath: path,
    uploadedBy: userId,
    uploadYear: year || new Date().getFullYear(),
    source: 'upload',
  })
}

export async function deleteFile(id: string, filePath: string, _folderId: string) {
  if (filePath) {
    await supabase.storage.from('yearbook-files').remove([filePath])
  }
  await del(`/api/resources/files/${id}`)
}

export async function getFileUrl(filePath: string): Promise<string> {
  const { data } = await supabase.storage.from('yearbook-files').createSignedUrl(filePath, 3600)
  return data?.signedUrl || ''
}

function mapFolder(raw: any): ResourceFolder {
  return {
    id: raw.id,
    unit_name: raw.unitName || raw.unit_name,
    tags: raw.tags,
    file_count: raw.fileCount ?? raw.file_count ?? 0,
    created_at: raw.createdAt || raw.created_at || '',
    updated_at: raw.updatedAt || raw.updated_at || '',
  }
}

function mapFile(raw: any): ResourceFile {
  return {
    id: raw.id,
    folder_id: raw.folderId || raw.folder_id,
    file_name: raw.fileName || raw.file_name,
    file_path: raw.filePath || raw.file_path,
    file_size: raw.fileSize ?? raw.file_size ?? 0,
    file_type: raw.fileType || raw.file_type,
    upload_year: raw.uploadYear || raw.upload_year,
    source: raw.source || 'upload',
    uploaded_by: raw.uploadedBy || raw.uploaded_by,
    created_at: raw.createdAt || raw.created_at || '',
  }
}
