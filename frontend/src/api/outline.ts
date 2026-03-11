import { get, post, put, del } from './http'
import type { OutlineNode } from '@/types'

export async function fetchOutlineTree(yearbookId: string): Promise<OutlineNode[]> {
  const data = await get<any[]>('/api/outlines', { yearbookId })
  const nodes = (data || []).map(mapOutline)
  return buildTree(nodes)
}

function buildTree(flatNodes: OutlineNode[]): OutlineNode[] {
  const map = new Map<string, OutlineNode>()
  const roots: OutlineNode[] = []
  flatNodes.forEach((n) => map.set(n.id, { ...n, children: [] }))
  flatNodes.forEach((n) => {
    const node = map.get(n.id)!
    if (n.parent_id && map.has(n.parent_id)) {
      map.get(n.parent_id)!.children!.push(node)
    } else {
      roots.push(node)
    }
  })
  return roots
}

export async function createOutlineNode(node: any) {
  return mapOutline(await post<any>('/api/outlines', toApiFormat(node)))
}

export async function updateOutlineNode(id: string, updates: any) {
  return mapOutline(await put<any>(`/api/outlines/${id}`, toApiFormat(updates)))
}

export async function deleteOutlineNode(id: string) {
  await del(`/api/outlines/${id}`)
}

export async function batchCreateOutlineNodes(nodes: any[]) {
  return post<any[]>('/api/outlines/batch', nodes.map(toApiFormat))
}

function mapOutline(raw: any): OutlineNode {
  return {
    id: raw.id,
    yearbook_id: raw.yearbookId || raw.yearbook_id,
    parent_id: raw.parentId || raw.parent_id || null,
    title: raw.title,
    level: raw.level || 1,
    sort_order: raw.sortOrder ?? raw.sort_order ?? 0,
    status: raw.status || 'not_started',
    unit_name: raw.unitName || raw.unit_name,
    assigned_user_id: raw.assignedUserId || raw.assigned_user_id,
    created_at: raw.createdAt || raw.created_at || '',
    updated_at: raw.updatedAt || raw.updated_at || '',
  }
}

function toApiFormat(node: any): any {
  return {
    yearbookId: node.yearbook_id || node.yearbookId,
    parentId: node.parent_id || node.parentId || null,
    title: node.title,
    level: node.level,
    sortOrder: node.sort_order ?? node.sortOrder,
    status: node.status,
    unitName: node.unit_name || node.unitName,
    assignedUserId: node.assigned_user_id || node.assignedUserId,
  }
}
