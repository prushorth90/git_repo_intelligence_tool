import { AlertTriangle, FolderGit2, LoaderCircle, RefreshCw } from 'lucide-react'
import { Link } from 'react-router-dom'

type WorkspaceStateProps = {
  state: 'loading' | 'error' | 'empty'
  message?: string
  onRetry?: () => void
}

export function WorkspaceState({ state, message, onRetry }: WorkspaceStateProps) {
  const content = {
    loading: { icon: LoaderCircle, title: 'Loading repositories', detail: 'Contacting the repository intelligence API.' },
    error: { icon: AlertTriangle, title: 'Repository data unavailable', detail: message ?? 'The API request failed.' },
    empty: { icon: FolderGit2, title: 'No repositories connected', detail: 'Create a repository workspace to begin analysis.' },
  }[state]
  const Icon = content.icon

  return (
    <section className="workspace-state">
      <Icon className={state === 'loading' ? 'spin' : ''} size={24} />
      <h1>{content.title}</h1>
      <p>{content.detail}</p>
      {state === 'error' && onRetry && <button className="secondary-button" onClick={onRetry} type="button"><RefreshCw size={15} /> Retry</button>}
      {state === 'empty' && <Link className="primary-button" to="/repositories">Open repositories</Link>}
    </section>
  )
}