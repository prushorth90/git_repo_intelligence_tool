import { useEffect, useState } from 'react'
import type { ReactNode } from 'react'
import { repositoryApi } from '../api/client'
import { gitHubApi } from '../api/client'
import type { RepositoryResponse } from '../api/types'
import { RepositoryContext } from './RepositoryContext'
import type { RepositoryView } from './RepositoryContext'

const analyticsProfiles = [
  { healthScore: 82, commits: 2847, contributors: 24, status: 'Healthy' as const },
  { healthScore: 68, commits: 1932, contributors: 16, status: 'Attention' as const },
  { healthScore: 54, commits: 1108, contributors: 11, status: 'At risk' as const },
]

function toRepositoryView(repository: RepositoryResponse, index: number): RepositoryView {
  const profile = analyticsProfiles[index % analyticsProfiles.length]
  return {
    ...repository,
    organization: repository.owner,
    branch: repository.defaultBranch,
    language: repository.primaryLanguage ?? 'Not analyzed',
    lastAnalyzed: 'queued',
    ...profile,
  }
}

export function RepositoryProvider({ children }: { children: ReactNode }) {
  const [repositories, setRepositories] = useState<RepositoryView[]>([])
  const [repositoryId, setRepositoryId] = useState('')
  const [loadState, setLoadState] = useState<'loading' | 'success' | 'error'>('loading')
  const [error, setError] = useState('')
  const [reloadToken, setReloadToken] = useState(0)

  useEffect(() => {
    const controller = new AbortController()
    repositoryApi.list(controller.signal)
      .then((response) => {
        const nextRepositories = response.map(toRepositoryView)
        setRepositories(nextRepositories)
        setRepositoryId((current) => nextRepositories.some((item) => item.id === current)
          ? current
          : (nextRepositories[0]?.id ?? ''))
        setLoadState('success')
      })
      .catch((requestError: unknown) => {
        if (requestError instanceof DOMException && requestError.name === 'AbortError') return
        setError(requestError instanceof Error ? requestError.message : 'Repositories could not be loaded.')
        setLoadState('error')
      })
    return () => controller.abort()
  }, [reloadToken])

  async function createRepository(githubUrl: string) {
    const created = await repositoryApi.create({ githubUrl })
    const repository = toRepositoryView(created, repositories.length)
    setRepositories((current) => [repository, ...current])
    setRepositoryId(repository.id)
    return repository
  }

  async function importGitHubRepository(githubRepositoryId: number) {
    const imported = await gitHubApi.importRepository(githubRepositoryId)
    const repository = toRepositoryView(imported, repositories.length)
    setRepositories((current) => [repository, ...current])
    setRepositoryId(repository.id)
    return repository
  }

  function reload() {
    setLoadState('loading')
    setError('')
    setReloadToken((current) => current + 1)
  }

  const repository = repositories.find((item) => item.id === repositoryId) ?? null

  return (
    <RepositoryContext.Provider value={{
      repositories,
      repository,
      repositoryId,
      setRepositoryId,
      loadState,
      error,
      reload,
      createRepository,
      importGitHubRepository,
    }}>
      {children}
    </RepositoryContext.Provider>
  )
}