import { post } from './http'

export async function aiGenerateEntry(entryId: string, rawData: string, historyData: string): Promise<string> {
  const result = await post<{ content: string }>('/api/entries/ai/generate', { rawData, historyData })
  return result.content
}

export async function aiRewrite(content: string): Promise<string> {
  const result = await post<{ content: string }>('/api/entries/ai/rewrite', { content })
  return result.content
}

export async function aiExpand(content: string): Promise<string> {
  const result = await post<{ content: string }>('/api/entries/ai/expand', { content })
  return result.content
}

export async function aiDetectConflicts(content: string): Promise<any[]> {
  return post<any[]>('/api/entries/ai/detect', { content })
}

export async function aiBotAnswer(question: string): Promise<string> {
  const result = await post<{ answer: string }>('/api/entries/ai/bot', { prompt: question })
  return result.answer
}
