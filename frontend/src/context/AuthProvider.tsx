import { useEffect, useState } from 'react'
import type { ReactNode } from 'react'
import { authApi } from '../api/client'
import type { GitHubUserResponse } from '../api/types'
import { AuthContext } from './AuthContext'

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<GitHubUserResponse | null>(null)
  const [state, setState] = useState<'loading' | 'ready' | 'error' | 'disconnecting'>('loading')
  const [error, setError] = useState('')
  const [reloadToken, setReloadToken] = useState(0)

  useEffect(() => {
    const controller = new AbortController()
    authApi.current(controller.signal)
      .then((response) => { setUser(response); setState('ready') })
      .catch((requestError: unknown) => {
        if (requestError instanceof DOMException && requestError.name === 'AbortError') return
        setError(requestError instanceof Error ? requestError.message : 'GitHub connection status is unavailable.')
        setState('error')
      })
    return () => controller.abort()
  }, [reloadToken])

  function connect() {
    window.location.assign(authApi.startUrl())
  }

  async function disconnect() {
    setState('disconnecting')
    setError('')
    try {
      await authApi.disconnect()
      setUser((current) => current ? { ...current, connected: false, login: null, avatarUrl: null, connectedAt: null } : current)
      setState('ready')
    } catch (requestError) {
      setError(requestError instanceof Error ? requestError.message : 'GitHub could not be disconnected.')
      setState('error')
    }
  }

  function reload() {
    setState('loading')
    setError('')
    setReloadToken((current) => current + 1)
  }

  return (
    <AuthContext.Provider value={{ user, state, error, connect, disconnect, reload }}>
      {children}
    </AuthContext.Provider>
  )
}