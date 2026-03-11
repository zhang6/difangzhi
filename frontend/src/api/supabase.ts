import { createClient } from '@supabase/supabase-js'

const supabaseUrl = import.meta.env.VITE_SUPABASE_URL || 'https://tqdosxaesqbwbhngvndj.supabase.co'
const supabaseKey = import.meta.env.VITE_SUPABASE_ANON_KEY || 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InRxZG9zeGFlc3Fid2Jobmd2bmRqIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjMyODAxMzIsImV4cCI6MjA3ODg1NjEzMn0.YArQlOsSoHZZRO0Gwm4zyzvDGIRl1GimlosqSPUshz8'

export const supabase = createClient(supabaseUrl, supabaseKey)
