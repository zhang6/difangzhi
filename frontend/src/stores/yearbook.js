import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useYearbookStore = defineStore('yearbook', () => {
  const currentYearbook = ref(JSON.parse(sessionStorage.getItem('yb_current') || 'null'))
  const currentOutline = ref(JSON.parse(sessionStorage.getItem('yb_outline') || 'null'))

  function setCurrentYearbook(yb) {
    currentYearbook.value = yb
    sessionStorage.setItem('yb_current', JSON.stringify(yb))
  }

  function setCurrentOutline(outline) {
    currentOutline.value = outline
    sessionStorage.setItem('yb_outline', JSON.stringify(outline))
  }

  function clear() {
    currentYearbook.value = null
    currentOutline.value = null
    sessionStorage.removeItem('yb_current')
    sessionStorage.removeItem('yb_outline')
  }

  return { currentYearbook, currentOutline, setCurrentYearbook, setCurrentOutline, clear }
})
