import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { Yearbook, OutlineNode } from '@/types'

export const useYearbookStore = defineStore('yearbook', () => {
  const currentYearbook = ref<Yearbook | null>(
    JSON.parse(sessionStorage.getItem('yb_current') || 'null')
  )
  const currentOutline = ref<OutlineNode | null>(
    JSON.parse(sessionStorage.getItem('yb_outline') || 'null')
  )

  function setCurrentYearbook(yb: Yearbook | null) {
    currentYearbook.value = yb
    if (yb) {
      sessionStorage.setItem('yb_current', JSON.stringify(yb))
    } else {
      sessionStorage.removeItem('yb_current')
    }
  }

  function setCurrentOutline(outline: OutlineNode | null) {
    currentOutline.value = outline
    if (outline) {
      sessionStorage.setItem('yb_outline', JSON.stringify(outline))
    } else {
      sessionStorage.removeItem('yb_outline')
    }
  }

  function clear() {
    currentYearbook.value = null
    currentOutline.value = null
    sessionStorage.removeItem('yb_current')
    sessionStorage.removeItem('yb_outline')
  }

  return { currentYearbook, currentOutline, setCurrentYearbook, setCurrentOutline, clear }
})
