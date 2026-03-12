import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useYearbookStore = defineStore('yearbook', () => {
  const currentYearbookId = ref(localStorage.getItem('current_yb_id') || '')
  const currentYearbookName = ref(localStorage.getItem('current_yb_name') || '')
  const currentOutlineId = ref(localStorage.getItem('current_outline_id') || '')
  const currentOutlineTitle = ref(localStorage.getItem('current_outline_title') || '')

  function setCurrentYearbook(id: string, name: string) {
    currentYearbookId.value = id
    currentYearbookName.value = name
    localStorage.setItem('current_yb_id', id)
    localStorage.setItem('current_yb_name', name)
  }

  function setCurrentOutline(id: string, title: string) {
    currentOutlineId.value = id
    currentOutlineTitle.value = title
    localStorage.setItem('current_outline_id', id)
    localStorage.setItem('current_outline_title', title)
  }

  function clearCurrent() {
    currentYearbookId.value = ''
    currentYearbookName.value = ''
    currentOutlineId.value = ''
    currentOutlineTitle.value = ''
    localStorage.removeItem('current_yb_id')
    localStorage.removeItem('current_yb_name')
    localStorage.removeItem('current_outline_id')
    localStorage.removeItem('current_outline_title')
  }

  return {
    currentYearbookId, currentYearbookName, currentOutlineId, currentOutlineTitle,
    setCurrentYearbook, setCurrentOutline, clearCurrent,
  }
})
