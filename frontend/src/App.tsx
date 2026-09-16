import { useEffect, useState } from 'react'
import type { FormEvent } from 'react'
import {
  Activity,
  ArrowUpRight,
  Boxes,
  CheckCircle2,
  Code2,
  GitBranch,
  LoaderCircle,
  Plus,
  Radar,
  ShieldAlert,
  Users,
} from 'lucide-react'
import './App.css'

type Repository = {
  id: string
  fullName: string
  githubUrl: string
  connectedAt: string
}

const API_URL = import.meta.env.VITE_API_URL ?? ''

const plannedSignals = [
  { icon: Activity, label: 'Code churn', detail: 'Commit and file velocity' },
  { icon: Users, label: 'Ownership', detail: 'Knowledge concentration' },
  { icon: Boxes, label: 'Dependencies', detail: 'Module relationships' },
  { icon: ShieldAlert, label: 'Risk', detail: 'Hotspots and complexity' },
]

function App() {
  const [repositories, setRepositories] = useState<Repository[]>([])
  const [githubUrl, setGithubUrl] = useState('')
  const [loading, setLoading] = useState(true)
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    fetch(`${API_URL}/api/repositories`)
      .then(async (response) => {
        if (!response.ok) throw new Error('The API is not ready yet.')
        return response.json() as Promise<Repository[]>
      })
      .then(setRepositories)
      .catch((requestError: Error) => setError(requestError.message))
      .finally(() => setLoading(false))
  }, [])

  async function connectRepository(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setSubmitting(true)
    setError('')

    try {
      const response = await fetch(`${API_URL}/api/repositories`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ githubUrl }),
      })

      if (!response.ok) {
        throw new Error(response.status === 409
          ? 'That repository is already connected.'
          : 'Enter a public GitHub repository URL.')
      }

      const repository = await response.json() as Repository
      setRepositories((current) => [repository, ...current])
      setGithubUrl('')
    } catch (requestError) {
      setError(requestError instanceof Error ? requestError.message : 'Could not connect the repository.')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <a className="brand" href="/" aria-label="Repo Intelligence home">
          <span className="brand-mark"><Radar size={19} /></span>
          <span>Repo Intelligence</span>
        </a>

        <nav aria-label="Primary navigation">
          <a className="nav-item active" href="#repositories"><GitBranch size={17} /> Repositories</a>
          <span className="nav-item disabled"><Activity size={17} /> Portfolio health</span>
        </nav>

        <div className="sidebar-status">
          <span className="status-dot" />
          <div>
            <strong>Local workspace</strong>
            <span>API + worker stack</span>
          </div>
        </div>
      </aside>

      <main>
        <header className="topbar">
          <div>
            <span className="eyebrow">Engineering overview</span>
            <h1>Repository health</h1>
          </div>
          <div className="system-state"><CheckCircle2 size={16} /> Foundation ready</div>
        </header>

        <section className="connect-band" aria-labelledby="connect-heading">
          <div className="connect-copy">
            <span className="section-index">01 / Connect</span>
            <h2 id="connect-heading">Bring your codebase into focus.</h2>
            <p>Connect a GitHub repository to establish its analysis workspace. Collection and scoring arrive in the next pipeline phase.</p>
          </div>
          <form onSubmit={connectRepository}>
            <label htmlFor="github-url">GitHub repository URL</label>
            <div className="input-row">
              <span className="input-icon"><Code2 size={19} /></span>
              <input
                id="github-url"
                type="url"
                required
                placeholder="https://github.com/owner/repository"
                value={githubUrl}
                onChange={(event) => setGithubUrl(event.target.value)}
              />
              <button type="submit" disabled={submitting}>
                {submitting ? <LoaderCircle className="spin" size={18} /> : <Plus size={18} />}
                Connect
              </button>
            </div>
            {error && <p className="form-error" role="alert">{error}</p>}
          </form>
        </section>

        <section className="content-section" id="repositories" aria-labelledby="repositories-heading">
          <div className="section-heading">
            <div>
              <span className="section-index">02 / Repositories</span>
              <h2 id="repositories-heading">Connected codebases</h2>
            </div>
            <span className="count">{repositories.length.toString().padStart(2, '0')}</span>
          </div>

          {loading ? (
            <div className="empty-state"><LoaderCircle className="spin" size={22} /> Loading repositories</div>
          ) : repositories.length === 0 ? (
            <div className="empty-state">
              <GitBranch size={24} />
              <strong>No repositories connected</strong>
              <span>Add a GitHub URL above to create the first workspace.</span>
            </div>
          ) : (
            <div className="repository-list">
              {repositories.map((repository, index) => (
                <article className="repository-row" key={repository.id} style={{ '--row': index } as React.CSSProperties}>
                  <span className="repo-glyph"><Code2 size={20} /></span>
                  <div className="repo-name">
                    <strong>{repository.fullName}</strong>
                    <span>Connected {new Date(repository.connectedAt).toLocaleDateString()}</span>
                  </div>
                  <span className="awaiting"><span /> Awaiting analysis</span>
                  <a href={repository.githubUrl} target="_blank" rel="noreferrer" aria-label={`Open ${repository.fullName} on GitHub`}>
                    <ArrowUpRight size={18} />
                  </a>
                </article>
              ))}
            </div>
          )}
        </section>

        <section className="content-section signals" aria-labelledby="signals-heading">
          <div className="section-heading">
            <div>
              <span className="section-index">03 / Signals</span>
              <h2 id="signals-heading">Analysis map</h2>
            </div>
            <span className="planned-label">Pipeline planned</span>
          </div>
          <div className="signal-grid">
            {plannedSignals.map(({ icon: Icon, label, detail }) => (
              <div className="signal-item" key={label}>
                <Icon size={20} />
                <strong>{label}</strong>
                <span>{detail}</span>
              </div>
            ))}
          </div>
        </section>
      </main>
    </div>
  )
}

export default App