import { supabase } from './supabase'
import type { ResourceFolder, ResourceFile, PaginatedResult } from '@/types'

export async function fetchFolders(params: {
  page: number
  pageSize: number
  keyword?: string
}): Promise<PaginatedResult<ResourceFolder>> {
  let query = supabase.from('yb_material_folders').select('*', { count: 'exact' })

  if (params.keyword) {
    query = query.ilike('unit_name', `%${params.keyword}%`)
  }

  const from = (params.page - 1) * params.pageSize
  const to = from + params.pageSize - 1

  const { data, count, error } = await query.order('created_at', { ascending: false }).range(from, to)
  if (error) throw error
  return { data: data || [], total: count || 0 }
}

export async function createFolder(folder: Partial<ResourceFolder>) {
  const { data, error } = await supabase
    .from('yb_material_folders')
    .insert(folder)
    .select()
    .single()
  if (error) throw error
  return data
}

export async function updateFolder(id: string, updates: Partial<ResourceFolder>) {
  const { data, error } = await supabase
    .from('yb_material_folders')
    .update({ ...updates, updated_at: new Date().toISOString() })
    .eq('id', id)
    .select()
    .single()
  if (error) throw error
  return data
}

export async function deleteFolder(id: string) {
  await supabase.from('yb_material_files').delete().eq('folder_id', id)
  const { error } = await supabase.from('yb_material_folders').delete().eq('id', id)
  if (error) throw error
}

export async function fetchFiles(params: {
  folderId: string
  page: number
  pageSize: number
  year?: number | null
}): Promise<PaginatedResult<ResourceFile>> {
  let query = supabase
    .from('yb_material_files')
    .select('*', { count: 'exact' })
    .eq('folder_id', params.folderId)

  if (params.year) {
    query = query.eq('upload_year', params.year)
  }

  const from = (params.page - 1) * params.pageSize
  const to = from + params.pageSize - 1

  const { data, count, error } = await query.order('created_at', { ascending: false }).range(from, to)
  if (error) throw error
  return { data: data || [], total: count || 0 }
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

  const record: Partial<ResourceFile> = {
    folder_id: folderId,
    file_name: file.name,
    file_size: file.size,
    file_type: file.name.split('.').pop()?.toLowerCase() || '',
    file_path: path,
    uploaded_by: userId,
    upload_year: year || new Date().getFullYear(),
    source: 'upload',
  }

  const { data, error } = await supabase
    .from('yb_material_files')
    .insert(record)
    .select()
    .single()
  if (error) throw error

  await supabase
    .from('yb_material_folders')
    .update({ file_count: (await getFileCount(folderId)), updated_at: new Date().toISOString() })
    .eq('id', folderId)

  return data
}

async function getFileCount(folderId: string): Promise<number> {
  const { count } = await supabase
    .from('yb_material_files')
    .select('*', { count: 'exact', head: true })
    .eq('folder_id', folderId)
  return count || 0
}

export async function deleteFile(id: string, filePath: string, folderId: string) {
  if (filePath) {
    await supabase.storage.from('yearbook-files').remove([filePath])
  }
  const { error } = await supabase.from('yb_material_files').delete().eq('id', id)
  if (error) throw error

  await supabase
    .from('yb_material_folders')
    .update({ file_count: (await getFileCount(folderId)), updated_at: new Date().toISOString() })
    .eq('id', folderId)
}

export async function getFileUrl(filePath: string): Promise<string> {
  const { data } = await supabase.storage.from('yearbook-files').createSignedUrl(filePath, 3600)
  return data?.signedUrl || ''
}
