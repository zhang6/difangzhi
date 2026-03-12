/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{vue,js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        primary: '#1a90ff',
        'primary-dark': '#0059b3',
        sidebar: '#0d1b2e',
        'sidebar-hover': 'rgba(255,255,255,0.08)',
      },
    },
  },
  plugins: [],
}
