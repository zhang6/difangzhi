export type UserRole = 'admin' | 'manager' | 'editor'

export interface UserProfile {
  id: string
  username: string
  password?: string
  name: string
  role: UserRole
  avatar_color?: string
  phone?: string
  email?: string
  created_at: string
  updated_at: string
}

export type YearbookStatus = 'not_started' | 'in_progress' | 'completed'

export interface Yearbook {
  id: string
  name: string
  start_date: string
  end_date: string
  cover_type: 'default_1' | 'default_2' | 'default_3' | 'custom'
  cover_url?: string
  status: YearbookStatus
  progress: number
  created_by?: string
  created_at: string
  updated_at: string
  managers?: YearbookManager[]
}

export interface YearbookManager {
  id: string
  yearbook_id: string
  user_id: string
  user_name?: string
  created_at: string
}

export type OutlineStatus = 'not_started' | 'in_progress' | 'submitted'

export interface OutlineNode {
  id: string
  yearbook_id: string
  parent_id: string | null
  title: string
  level: number
  sort_order: number
  status: OutlineStatus
  unit_name?: string
  assigned_user_id?: string
  assigned_user_name?: string
  created_at: string
  updated_at: string
  children?: OutlineNode[]
}

export interface ResourceFolder {
  id: string
  unit_name: string
  tags?: string
  file_count: number
  created_at: string
  updated_at: string
}

export interface ResourceFile {
  id: string
  folder_id: string
  outline_id?: string
  file_name: string
  file_path?: string
  file_size: number
  file_type?: string
  upload_year?: number
  source: 'upload' | 'split' | 'outline'
  uploaded_by?: string
  created_at: string
}

export type EntryStatus = 'draft' | 'editing' | 'submitted'

export interface Entry {
  id: string
  outline_id: string
  title: string
  original_content?: string
  ai_content?: string
  sort_order: number
  status: EntryStatus
  created_by?: string
  created_at: string
  updated_at: string
}

export interface EntryVersion {
  id: string
  entry_id: string
  version: number
  content?: string
  revision_note?: string
  editor_id?: string
  editor_name?: string
  created_at: string
}

export interface Annotation {
  id: string
  entry_id: string
  content: string
  author_id: string
  author_name?: string
  annotation_type: 'mine' | 'revision'
  process_status: 'pending' | 'processed'
  created_at: string
  updated_at: string
}

export interface Draft {
  id: string
  entry_id: string
  content?: string
  editor_id?: string
  created_at: string
}

export interface HistoryData {
  id: string
  yearbook_name?: string
  outline_section?: string
  entry_title?: string
  content?: string
  year?: number
  created_at: string
}

export interface PaginatedResult<T> {
  data: T[]
  total: number
}

export const STATUS_LABEL: Record<string, string> = {
  not_started: '未开始',
  in_progress: '编纂中',
  completed: '已完成',
  submitted: '已提交',
  draft: '草稿',
  editing: '编辑中',
}

export const STATUS_TYPE: Record<string, string> = {
  not_started: 'info',
  in_progress: 'warning',
  completed: 'success',
  submitted: 'success',
  draft: 'info',
  editing: 'warning',
}
