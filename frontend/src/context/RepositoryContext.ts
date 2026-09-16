import { createContext, useContext } from 'react'
import type { RepositorySummary } from '../data/mockData'

type RepositoryContextValue = {
  repository: RepositorySummary
  repositoryId: string
  setRepositoryId: (id: string) => void
}

export const RepositoryContext = createContext<RepositoryContextValue | null>(null)

export function useRepository() {
  const context = useContext(RepositoryContext)
  if (!context) throw new Error('useRepository must be used inside the application shell')
  return context
}