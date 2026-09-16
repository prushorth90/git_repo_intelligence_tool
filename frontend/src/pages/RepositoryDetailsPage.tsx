import { ArrowLeft, CalendarDays, GitBranch, GitFork, Globe2, LockKeyhole, Star } from 'lucide-react'
import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { repositoryApi } from '../api/client'
import type { RepositoryResponse } from '../api/types'
import { PageHeader } from '../components/PageHeader'
import { WorkspaceState } from '../components/WorkspaceState'

type DetailState = 'loading' | 'success' | 'error'
type DetailResult = {
  repositoryId: string
  state: DetailState
  repository: RepositoryResponse | null
  error: string
}

export function RepositoryDetailsPage() {
  const { repositoryId = '' } = useParams()
  const [result, setResult] = useState<DetailResult>({ repositoryId, state: 'loading', repository: null, error: '' })
  const [reloadToken, setReloadToken] = useState(0)

  useEffect(() => {
    const controller = new AbortController()
    repositoryApi.get(repositoryId, controller.signal)
      .then((response) => setResult({ repositoryId, state: 'success', repository: response, error: '' }))
      .catch((requestError: unknown) => {
        if (requestError instanceof DOMException && requestError.name === 'AbortError') return
        setResult({
          repositoryId,
          state: 'error',
          repository: null,
          error: requestError instanceof Error ? requestError.message : 'Repository details could not be loaded.',
        })
      })
    return () => controller.abort()
  }, [repositoryId, reloadToken])

  const currentResult = result.repositoryId === repositoryId ? result : { ...result, state: 'loading' as const }
  const { repository } = currentResult

  if (currentResult.state === 'loading') return <WorkspaceState state="loading" />
  if (currentResult.state === 'error') return <WorkspaceState message={currentResult.error} onRetry={() => {
    setResult({ repositoryId, state: 'loading', repository: null, error: '' })
    setReloadToken((value) => value + 1)
  }} state="error" />
  if (!repository) return null

  return (
    <div className="page-stack">
      <Link className="back-link" to="/repositories"><ArrowLeft size={15} /> Back to repositories</Link>
      <PageHeader eyebrow="Repository details" title={repository.fullName} description="Persisted repository configuration returned by the Spring Boot API." />
      <section className="details-grid">
        <article><Globe2 size={18} /><span>Visibility</span><strong>{repository.privateRepository ? 'Private' : 'Public'}</strong></article>
        <article><GitBranch size={18} /><span>Default branch</span><strong>{repository.defaultBranch}</strong></article>
        <article><CalendarDays size={18} /><span>Connected</span><strong>{new Date(repository.connectedAt).toLocaleString()}</strong></article>
        <article><LockKeyhole size={18} /><span>GitHub ID</span><strong>{repository.githubRepositoryId ?? 'Pending sync'}</strong></article>
        <article><Star size={18} /><span>Stars</span><strong>{repository.stars.toLocaleString()}</strong></article>
        <article><GitFork size={18} /><span>Forks</span><strong>{repository.forks.toLocaleString()}</strong></article>
        <article><Globe2 size={18} /><span>Language</span><strong>{repository.primaryLanguage ?? 'Unavailable'}</strong></article>
        <article><CalendarDays size={18} /><span>GitHub updated</span><strong>{repository.githubUpdatedAt ? new Date(repository.githubUpdatedAt).toLocaleString() : 'Pending sync'}</strong></article>
      </section>
      <section className="detail-panel">
        <div><span>Repository ID</span><code>{repository.id}</code></div>
        <div><span>Canonical URL</span><a href={repository.githubUrl} rel="noreferrer" target="_blank">{repository.githubUrl}</a></div>
        <div><span>Owner</span><strong>{repository.owner}</strong></div>
        <div><span>Name</span><strong>{repository.name}</strong></div>
      </section>
    </div>
  )
}