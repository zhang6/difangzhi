import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { supabase } from '@/api/supabase'
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
    const { data, error } = await supabase
      .from('yb_users')
      .select('*')
      .eq('username', username)
      .single()

    if (error || !data) throw new Error('用户名或密码错误')

    const bcryptModule = await import('bcryptjs')
    const bcrypt = bcryptModule.default || bcryptModule
    const valid = await bcrypt.compare(password, data.password)
    if (!valid) throw new Error('用户名或密码错误')

    const tokenValue = btoa(JSON.stringify({ id: data.id, ts: Date.now() }))
    user.value = {
      id: data.id,
      username: data.username,
      name: data.name,
      role: data.role,
      phone: data.phone,
      email: data.email,
      avatar_color: data.avatar_color,
      created_at: data.created_at,
      updated_at: data.updated_at,
    }
    token.value = tokenValue
    localStorage.setItem('yb_user', JSON.stringify(user.value))
    localStorage.setItem('yb_token', tokenValue)
    return data
  }

  function logout() {
    user.value = null
    token.value = ''
    localStorage.removeItem('yb_user')
    localStorage.removeItem('yb_token')
  }

  async function updateProfile(updates: Partial<UserProfile>) {
    if (!user.value) return
    const { error } = await supabase
      .from('yb_users')
      .update({ ...updates, updated_at: new Date().toISOString() })
      .eq('id', user.value.id)
    if (error) throw error
    Object.assign(user.value, updates)
    localStorage.setItem('yb_user', JSON.stringify(user.value))
  }

  async function fetchAllUsers(): Promise<UserProfile[]> {
    const { data, error } = await supabase
      .from('yb_users')
      .select('id, username, name, role, phone, email, created_at, updated_at')
      .order('created_at')
    if (error) throw error
    return data || []
  }

  return {
    user,
    token,
    isLoggedIn,
    isAdmin,
    isManager,
    userName,
    userRole,
    login,
    logout,
    updateProfile,
    fetchAllUsers,
  }
})
