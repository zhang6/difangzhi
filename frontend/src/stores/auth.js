import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { supabase } from '@/lib/supabase'

export const useAuthStore = defineStore('auth', () => {
  const user = ref(JSON.parse(localStorage.getItem('yb_user') || 'null'))
  const token = ref(localStorage.getItem('yb_token') || '')

  const isLoggedIn = computed(() => !!token.value && !!user.value)
  const isAdmin = computed(() => user.value?.role === 'admin')
  const userName = computed(() => user.value?.name || '')
  const userInitial = computed(() => userName.value.charAt(0) || '?')

  async function login(username, password) {
    const { data, error } = await supabase
      .from('yb_users')
      .select('*')
      .eq('username', username)
      .single()

    if (error || !data) throw new Error('用户名或密码错误')

    const bcrypt = await import('bcryptjs')
    const valid = await bcrypt.compare(password, data.password)
    if (!valid) throw new Error('用户名或密码错误')

    const tokenValue = btoa(JSON.stringify({ id: data.id, ts: Date.now() }))
    user.value = { id: data.id, username: data.username, name: data.name, role: data.role, avatar_color: data.avatar_color }
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

  async function updateProfile(name) {
    if (!user.value) return
    const { error } = await supabase
      .from('yb_users')
      .update({ name, updated_at: new Date().toISOString() })
      .eq('id', user.value.id)
    if (error) throw error
    user.value.name = name
    localStorage.setItem('yb_user', JSON.stringify(user.value))
  }

  return { user, token, isLoggedIn, isAdmin, userName, userInitial, login, logout, updateProfile }
})
