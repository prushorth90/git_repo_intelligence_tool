import { createContext, useContext } from 'react'
import type { GitHubUserResponse } from '../api/types'

export type AuthContextValue = {
  user: GitHubUserResponse | null
  state: 'loading' | 'ready' | 'error' | 'disconnecting'
  error: string
  connect: () => void
  disconnect: () => Promise<void>
  reload: () => void
}

export const AuthContext = createContext<AuthContextValue | null>(null)

export function useAuth() {
  const context = useContext(AuthContext)
  if (!context) throw new Error('useAuth must be used inside AuthProvider')
  return context
}