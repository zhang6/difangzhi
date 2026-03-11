import { supabase } from './supabase'
import type { OutlineNode } from '@/types'

export async function fetchOutlineTree(yearbookId: string): Promise<OutlineNode[]> {
  const { data, error } = await supabase
    .from('yb_outlines')
    .select('*, assigned_user:yb_users!yb_outlines_assigned_user_id_fkey(name)')
    .eq('yearbook_id', yearbookId)
    .order('sort_order')

  if (error) throw error

  const nodes = (data || []).map((n: any) => ({
    ...n,
    assigned_user_name: n.assigned_user?.name,
    assigned_user: undefined,
  }))
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

export async function createOutlineNode(node: Partial<OutlineNode>) {
  const { data, error } = await supabase.from('yb_outlines').insert(node).select().single()
  if (error) throw error
  return data
}

export async function updateOutlineNode(id: string, updates: Partial<OutlineNode>) {
  const { data, error } = await supabase
    .from('yb_outlines')
    .update({ ...updates, updated_at: new Date().toISOString() })
    .eq('id', id)
    .select()
    .single()
  if (error) throw error
  return data
}

export async function deleteOutlineNode(id: string) {
  await supabase.from('yb_outlines').delete().eq('parent_id', id)
  const { error } = await supabase.from('yb_outlines').delete().eq('id', id)
  if (error) throw error
}

export async function batchCreateOutlineNodes(nodes: Partial<OutlineNode>[]) {
  const { data, error } = await supabase.from('yb_outlines').insert(nodes).select()
  if (error) throw error
  return data
}
