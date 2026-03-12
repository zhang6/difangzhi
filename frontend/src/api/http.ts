const BASE_URL = import.meta.env.VITE_API_BASE_URL ?? ''

function getToken(): string {
  return localStorage.getItem('yb_token') || ''
}

async function request<T>(url: string, options: RequestInit = {}): Promise<T> {
  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
    ...(options.headers as Record<string, string> || {}),
  }

  const token = getToken()
  if (token) {
    headers['Authorization'] = `Bearer ${token}`
  }

  const resp = await fetch(`${BASE_URL}${url}`, {
    ...options,
    headers,
  })

  if (!resp.ok) {
    const text = await resp.text().catch(() => '')
    throw new Error(text || `HTTP ${resp.status}`)
  }

  const contentType = resp.headers.get('content-type')
  if (contentType?.includes('application/json')) {
    return resp.json()
  }
  return null as T
}

export function get<T>(url: string, params?: Record<string, any>): Promise<T> {
  const query = params
    ? '?' + Object.entries(params)
        .filter(([, v]) => v !== undefined && v !== null && v !== '')
        .map(([k, v]) => `${encodeURIComponent(k)}=${encodeURIComponent(v)}`)
        .join('&')
    : ''
  return request<T>(url + query)
}

export function post<T>(url: string, body?: any): Promise<T> {
  return request<T>(url, { method: 'POST', body: body ? JSON.stringify(body) : undefined })
}

export function put<T>(url: string, body?: any): Promise<T> {
  return request<T>(url, { method: 'PUT', body: body ? JSON.stringify(body) : undefined })
}

export function del<T>(url: string): Promise<T> {
  return request<T>(url, { method: 'DELETE' })
}
