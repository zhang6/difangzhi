import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { post, get, put } from '@/api/http'
import type { UserProfile, UserRole } from '@/types'

export const useAuthStore = defineStore('auth', () => {
  const user = ref<UserProfile | null>(
    JSON.parse(localStorage.getItem('yb_user') || 'null')
  )
  const token = ref(localStorage.getItem('yb_token') || '')

  const isLoggedIn = computed(() => !!token.value && !!user.value)
  const isAdmin = computed(() => user.value?.role === 'admin')
  const isManager = computed(() => user.value?.role === 'manager' || isAdmin.value)
  const userName = computed(() => user.value?.name || '')
  const userRole = computed(() => user.value?.role as UserRole)

  async function login(username: string, password: string) {
    const resp = await post<{ token: string; user: any }>('/api/auth/login', { username, password })
    token.value = resp.token
    user.value = {
      id: resp.user.id,
      username: resp.user.username,
      name: resp.user.name,
      role: resp.user.role,
      avatar_color: resp.user.avatarColor,
      phone: resp.user.phone,
      email: resp.user.email,
      created_at: '',
      updated_at: '',
    }
    localStorage.setItem('yb_token', resp.token)
    localStorage.setItem('yb_user', JSON.stringify(user.value))
    return resp
  }

  function logout() {
    user.value = null
    token.value = ''
    localStorage.removeItem('yb_user')
    localStorage.removeItem('yb_token')
  }

  async function updateProfile(updates: Partial<UserProfile>) {
    if (!user.value) return
    const resp = await put<any>(`/api/users/${user.value.id}`, updates)
    Object.assign(user.value, { name: resp.name, phone: resp.phone, email: resp.email })
    localStorage.setItem('yb_user', JSON.stringify(user.value))
  }

  async function fetchAllUsers(): Promise<UserProfile[]> {
    const data = await get<any[]>('/api/users')
    return data.map((u: any) => ({
      id: u.id,
      username: u.username,
      name: u.name,
      role: u.role,
      phone: u.phone,
      email: u.email,
      avatar_color: u.avatarColor,
      created_at: u.createdAt,
      updated_at: u.updatedAt,
    }))
  }

  return {
    user, token, isLoggedIn, isAdmin, isManager, userName, userRole,
    login, logout, updateProfile, fetchAllUsers,
  }
})
