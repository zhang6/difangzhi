import { get, post, put, del } from './http'
import type { OutlineNode } from '@/types'

function mapOutline(raw: any): OutlineNode {
  return {
    id: raw.id,
    yearbook_id: raw.yearbookId || raw.yearbook_id || '',
    parent_id: raw.parentId || raw.parent_id || null,
    title: raw.title,
    level: raw.level || 1,
    sort_order: raw.sortOrder ?? raw.sort_order ?? 0,
    status: raw.status || 'not_started',
    unit_name: raw.unitName || raw.unit_name || '',
    assigned_user_id: raw.assignedUserId || raw.assigned_user_id || '',
    assigned_user: raw.assignedUser ? {
      id: raw.assignedUser.id,
      name: raw.assignedUser.name,
      username: raw.assignedUser.username,
      role: raw.assignedUser.role,
      avatar_color: raw.assignedUser.avatarColor,
    } : undefined,
    created_at: raw.createdAt || raw.created_at || '',
    updated_at: raw.updatedAt || raw.updated_at || '',
    children: [],
  }
}

function buildTree(flat: OutlineNode[]): OutlineNode[] {
  const map = new Map<string, OutlineNode>()
  flat.forEach(n => map.set(n.id, { ...n, children: [] }))
  const roots: OutlineNode[] = []
  map.forEach(n => {
    if (n.parent_id && map.has(n.parent_id)) {
      map.get(n.parent_id)!.children!.push(n)
    } else {
      roots.push(n)
    }
  })
  const sort = (nodes: OutlineNode[]) => {
    nodes.sort((a, b) => (a.sort_order || 0) - (b.sort_order || 0))
    nodes.forEach(n => sort(n.children || []))
  }
  sort(roots)
  return roots
}

export async function fetchOutlineTree(yearbookId: string): Promise<OutlineNode[]> {
  const flat = await get<any[]>('/api/outlines', { yearbookId })
  return buildTree(flat.map(mapOutline))
}

export async function fetchOutlineFlat(yearbookId: string): Promise<OutlineNode[]> {
  const flat = await get<any[]>('/api/outlines', { yearbookId })
  return flat.map(mapOutline)
}

export async function createOutlineNode(data: Partial<OutlineNode>): Promise<OutlineNode> {
  const body = {
    yearbookId: data.yearbook_id, parentId: data.parent_id,
    title: data.title, unitName: data.unit_name, status: data.status,
    level: data.level, sortOrder: data.sort_order,
  }
  return mapOutline(await post<any>('/api/outlines', body))
}

export async function batchCreateOutlineNodes(nodes: any[]): Promise<OutlineNode[]> {
  const body = nodes.map(n => ({
    yearbookId: n.yearbook_id, parentId: n.parent_id,
    title: n.title, unitName: n.unit_name, status: n.status || 'not_started',
    level: n.level, sortOrder: n.sort_order,
  }))
  const result = await post<any[]>('/api/outlines/batch', body)
  return result.map(mapOutline)
}

export async function updateOutlineNode(id: string, data: Partial<OutlineNode> & { assigned_user_id?: string }): Promise<OutlineNode> {
  const body: any = {}
  if (data.title !== undefined) body.title = data.title
  if (data.unit_name !== undefined) body.unitName = data.unit_name
  if (data.status !== undefined) body.status = data.status
  if (data.sort_order !== undefined) body.sortOrder = data.sort_order
  if (data.assigned_user_id !== undefined) body.assignedUserId = data.assigned_user_id
  return mapOutline(await put<any>(`/api/outlines/${id}`, body))
}

export async function deleteOutlineNode(id: string): Promise<void> {
  return del<void>(`/api/outlines/${id}`)
}

export async function fetchMyTasks(userId: string): Promise<OutlineNode[]> {
  const flat = await get<any[]>('/api/outlines/my-tasks', { userId })
  return flat.map(mapOutline)
}
