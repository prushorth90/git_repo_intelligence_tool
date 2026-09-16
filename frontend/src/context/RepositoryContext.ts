import { createContext, useContext } from 'react'
import type { RepositoryResponse } from '../api/types'

export type RepositoryView = RepositoryResponse & {
  organization: string
  branch: string
  language: string
  healthScore: number
  commits: number
  contributors: number
  lastAnalyzed: string
  status: 'Healthy' | 'Attention' | 'At risk'
}

export type RepositoryContextValue = {
  repositories: RepositoryView[]
  repository: RepositoryView | null
  repositoryId: string
  setRepositoryId: (id: string) => void
  loadState: 'loading' | 'success' | 'error'
  error: string
  reload: () => void
  createRepository: (githubUrl: string) => Promise<RepositoryView>
  importGitHubRepository: (githubRepositoryId: number) => Promise<RepositoryView>
}

export const RepositoryContext = createContext<RepositoryContextValue | null>(null)

export function useRepository() {
  const context = useContext(RepositoryContext)
  if (!context) throw new Error('useRepository must be used inside RepositoryProvider')
  return context
}