import { post } from './http'

export async function aiGenerateEntry(entryId: string, rawData: string, historyData?: string): Promise<string> {
  const resp = await post<{ content: string }>(`/api/entries/${entryId}/ai-generate`, { rawData, historyData })
  return resp.content
}

export async function aiRewrite(text: string): Promise<string> {
  const resp = await post<{ content: string }>('/api/entries/ai-rewrite', { text })
  return resp.content
}

export async function aiExpand(text: string): Promise<string> {
  const resp = await post<{ content: string }>('/api/entries/ai-expand', { text })
  return resp.content
}

export async function aiDetectConflicts(content: string): Promise<Array<{
  type: string
  description: string
  location: string
  severity: 'high' | 'medium' | 'low'
}>> {
  return post('/api/entries/ai-detect', { content })
}

export async function aiBotAnswer(question: string): Promise<string> {
  const resp = await post<{ answer: string }>('/api/entries/ai-bot', { question })
  return resp.answer
}
