import { ChevronLeft, ChevronRight, Download, GitFork, LockKeyhole, Star } from 'lucide-react'
import { useEffect, useState } from 'react'
import { ApiError, gitHubApi } from '../api/client'
import type { GitHubRepositoryPageResponse } from '../api/types'
import { useAuth } from '../context/AuthContext'
import { useRepository } from '../context/RepositoryContext'
import { StatusBadge } from './StatusBadge'

type CatalogResult = {
  page: number
  state: 'loading' | 'success' | 'error'
  data: GitHubRepositoryPageResponse | null
  error: string
  rateLimitResetAt?: string
}

export function GitHubRepositoryBrowser() {
  const auth = useAuth()
  const { importGitHubRepository } = useRepository()
  const [page, setPage] = useState(1)
  const [reloadToken, setReloadToken] = useState(0)
  const [result, setResult] = useState<CatalogResult>({ page: 1, state: 'loading', data: null, error: '' })
  const [importingId, setImportingId] = useState<number | null>(null)
  const [message, setMessage] = useState('')
  const [messageTone, setMessageTone] = useState<'success' | 'error'>('success')

  useEffect(() => {
    if (!auth.user?.connected) return
    const controller = new AbortController()
    gitHubApi.repositories(page, 10, controller.signal)
      .then((data) => setResult({ page, state: 'success', data, error: '' }))
      .catch((requestError: unknown) => {
        if (requestError instanceof DOMException && requestError.name === 'AbortError') return
        setResult({
          page,
          state: 'error',
          data: null,
          error: requestError instanceof Error ? requestError.message : 'GitHub repositories could not be loaded.',
          rateLimitResetAt: requestError instanceof ApiError ? requestError.rateLimitResetAt : undefined,
        })
      })
    return () => controller.abort()
  }, [auth.user?.connected, page, reloadToken])

  if (!auth.user?.connected) {
    return (
      <section className="github-browser github-browser--disconnected">
        <div><span className="eyebrow">GitHub catalog</span><h2>Import an accessible repository</h2><p>Connect GitHub from the header to browse repositories available to your account.</p></div>
        <button className="secondary-button" disabled={!auth.user?.configured} onClick={auth.connect} type="button">Connect GitHub</button>
      </section>
    )
  }

  const current = result.page === page ? result : { ...result, state: 'loading' as const }

  async function handleImport(repositoryId: number, fullName: string) {
    setImportingId(repositoryId)
    setMessage('')
    try {
      await importGitHubRepository(repositoryId)
      setResult((existing) => existing.data ? {
        ...existing,
        data: { ...existing.data, repositories: existing.data.repositories.map((repository) => repository.id === repositoryId ? { ...repository, imported: true } : repository) },
      } : existing)
      setMessage(`${fullName} was imported and analysis was queued.`)
      setMessageTone('success')
    } catch (requestError) {
      setMessage(requestError instanceof Error ? requestError.message : 'The repository could not be imported.')
      setMessageTone('error')
    } finally {
      setImportingId(null)
    }
  }

  return (
    <section className="github-browser">
      <div className="panel-header">
        <div><span className="eyebrow">GitHub catalog</span><h2>Available repositories</h2><p>Select a repository your connected account can access.</p></div>
        {current.data && <span className="rate-limit-note">{current.data.rateLimitRemaining >= 0 ? `${current.data.rateLimitRemaining} API requests remaining` : 'Rate limit unavailable'}</span>}
      </div>

      {current.state === 'loading' && <div className="catalog-state">Loading GitHub repositories...</div>}
      {current.state === 'error' && (
        <div className="catalog-state catalog-state--error">
          <strong>{current.error}</strong>
          {current.rateLimitResetAt && <span>Rate limit resets {new Date(current.rateLimitResetAt).toLocaleString()}.</span>}
          <button className="secondary-button" onClick={() => {
            setResult({ page, state: 'loading', data: null, error: '' })
            setReloadToken((value) => value + 1)
          }} type="button">Retry</button>
        </div>
      )}
      {current.state === 'success' && current.data?.repositories.length === 0 && <div className="catalog-state">No accessible repositories were returned for this page.</div>}
      {current.state === 'success' && current.data && current.data.repositories.length > 0 && (
        <div className="github-repository-grid">
          {current.data.repositories.map((repository) => (
            <article key={repository.id}>
              <div className="github-repository-card__header">
                <div><strong>{repository.fullName}</strong><span>{repository.primaryLanguage ?? 'Language unavailable'}</span></div>
                {repository.privateRepository && <LockKeyhole size={14} />}
              </div>
              <div className="github-repository-card__meta"><span><Star size={13} /> {repository.stars}</span><span><GitFork size={13} /> {repository.forks}</span><StatusBadge>{repository.visibility}</StatusBadge></div>
              <div className="github-repository-card__footer">
                <small>Updated {new Date(repository.updatedAt).toLocaleDateString()}</small>
                <button className="secondary-button" disabled={repository.imported || importingId === repository.id} onClick={() => void handleImport(repository.id, repository.fullName)} type="button">
                  <Download size={14} /> {repository.imported ? 'Imported' : importingId === repository.id ? 'Importing...' : 'Import'}
                </button>
              </div>
            </article>
          ))}
        </div>
      )}

      {message && <p className={`catalog-message catalog-message--${messageTone}`} role={messageTone === 'error' ? 'alert' : 'status'}>{message}</p>}
      <div className="pagination-controls">
        <button className="icon-button" disabled={page === 1 || current.state === 'loading'} onClick={() => setPage((value) => value - 1)} title="Previous page" type="button"><ChevronLeft size={16} /></button>
        <span>Page {page}</span>
        <button className="icon-button" disabled={!current.data?.hasNextPage || current.state === 'loading'} onClick={() => setPage((value) => value + 1)} title="Next page" type="button"><ChevronRight size={16} /></button>
      </div>
    </section>
  )
}