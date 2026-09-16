import { ArrowUpRight, GitBranch, Plus, Search, X } from 'lucide-react'
import { useDeferredValue, useState } from 'react'
import type { FormEvent } from 'react'
import { Link } from 'react-router-dom'
import { DataTable } from '../components/DataTable'
import type { TableColumn } from '../components/DataTable'
import { PageHeader } from '../components/PageHeader'
import { StatusBadge } from '../components/StatusBadge'
import { useRepository } from '../context/RepositoryContext'
import type { RepositoryView } from '../context/RepositoryContext'

type SubmissionState = 'idle' | 'submitting' | 'success' | 'error'

export function RepositoriesPage() {
  const { repositories, createRepository } = useRepository()
  const [query, setQuery] = useState('')
  const [createOpen, setCreateOpen] = useState(repositories.length === 0)
  const [githubUrl, setGithubUrl] = useState('')
  const [submissionState, setSubmissionState] = useState<SubmissionState>('idle')
  const [message, setMessage] = useState('')
  const deferredQuery = useDeferredValue(query)
  const filtered = repositories.filter((repository) =>
    repository.fullName.toLowerCase().includes(deferredQuery.toLowerCase()),
  )

  async function handleCreate(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setSubmissionState('submitting')
    setMessage('')
    try {
      const repository = await createRepository(githubUrl)
      setGithubUrl('')
      setSubmissionState('success')
      setMessage(`${repository.fullName} was connected and analysis was queued.`)
    } catch (requestError) {
      setSubmissionState('error')
      setMessage(requestError instanceof Error ? requestError.message : 'The repository could not be created.')
    }
  }

  const columns: TableColumn<RepositoryView>[] = [
    {
      key: 'repository',
      header: 'Repository',
      render: (row) => (
        <div className="primary-cell">
          <span className="table-icon"><GitBranch size={16} /></span>
          <span><strong>{row.fullName}</strong><small>{row.privateRepository ? 'Private' : 'Public'} · {row.defaultBranch}</small></span>
        </div>
      ),
    },
    { key: 'connected', header: 'Connected', render: (row) => new Date(row.connectedAt).toLocaleDateString() },
    { key: 'branch', header: 'Default branch', render: (row) => <span className="code-path">{row.defaultBranch}</span> },
    { key: 'analysis', header: 'Analysis', align: 'right', render: () => <StatusBadge tone="warning">Queued</StatusBadge> },
    {
      key: 'details',
      header: '',
      align: 'right',
      render: (row) => <Link className="table-icon-button" title={`View ${row.name}`} to={`/repositories/${row.id}`}><ArrowUpRight size={16} /></Link>,
    },
  ]

  return (
    <div className="page-stack">
      <PageHeader
        eyebrow="Portfolio"
        title="Repositories"
        description="Browse connected codebases and open their persisted repository details."
        action={<button className="primary-button" onClick={() => setCreateOpen((open) => !open)} type="button">{createOpen ? <X size={16} /> : <Plus size={16} />}{createOpen ? 'Close' : 'Add repository'}</button>}
      />

      {createOpen && (
        <section className="create-panel">
          <div><span className="eyebrow">New connection</span><h2>Connect a GitHub repository</h2><p>The API stores the repository and queues its first analysis job.</p></div>
          <form onSubmit={handleCreate}>
            <label htmlFor="repository-url">GitHub repository URL</label>
            <div className="create-form-row">
              <input id="repository-url" onChange={(event) => setGithubUrl(event.target.value)} placeholder="https://github.com/owner/repository" required type="url" value={githubUrl} />
              <button className="primary-button" disabled={submissionState === 'submitting'} type="submit">{submissionState === 'submitting' ? 'Connecting...' : 'Connect'}</button>
            </div>
            {message && <p className={`form-message form-message--${submissionState}`} role={submissionState === 'error' ? 'alert' : 'status'}>{message}</p>}
          </form>
        </section>
      )}

      <section className="table-panel">
        <div className="panel-header panel-header--toolbar">
          <div>
            <h2>Connected repositories</h2>
            <p>{repositories.length} {repositories.length === 1 ? 'codebase' : 'codebases'} from the API</p>
          </div>
          <label className="search-control"><Search size={15} /><input aria-label="Search repositories" onChange={(event) => setQuery(event.target.value)} placeholder="Search repositories" value={query} /></label>
        </div>
        <DataTable columns={columns} emptyMessage={repositories.length === 0 ? 'No repositories connected. Add one above to begin.' : 'No repositories match this search.'} getRowKey={(row) => row.id} rows={filtered} />
      </section>
    </div>
  )
}