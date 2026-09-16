export type RepositoryResponse = {
  id: string
  owner: string
  name: string
  fullName: string
  githubUrl: string
  githubRepositoryId: number | null
  defaultBranch: string
  privateRepository: boolean
  connectedAt: string
}

export type CreateRepositoryRequest = {
  githubUrl: string
}

export type AnalysisJobStatus = 'QUEUED' | 'RUNNING' | 'COMPLETED' | 'FAILED'

export type AnalysisJobResponse = {
  id: string
  repositoryId: string
  status: AnalysisJobStatus
  requestedAt: string
  startedAt: string | null
  completedAt: string | null
  failureMessage: string | null
}

export type ApiProblem = {
  title?: string
  status?: number
  detail?: string
}

export type GitHubUserResponse = {
  configured: boolean
  connected: boolean
  login: string | null
  avatarUrl: string | null
  connectedAt: string | null
}

export type CsrfTokenResponse = {
  headerName: string
  token: string
}