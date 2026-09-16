import type { AnalysisJobResponse, ApiProblem, CodeChurnRankingResponse, ContributorOverviewResponse, CreateRepositoryRequest, CsrfTokenResponse, GitHubRepositoryPageResponse, GitHubUserResponse, HistoryPeriod, RepositoryResponse } from './types'

const configuredApiUrl = import.meta.env.VITE_API_URL?.trim() ?? ''
const apiBaseUrl = configuredApiUrl.replace(/\/$/, '')

export function apiUrl(path: string) {
  return `${apiBaseUrl}${path}`
}

export class ApiError extends Error {
  readonly status: number
  readonly rateLimitResetAt?: string

  constructor(status: number, message: string, rateLimitResetAt?: string) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.rateLimitResetAt = rateLimitResetAt
  }
}

async function request<Response>(path: string, init?: RequestInit): Promise<Response> {
  const response = await fetch(apiUrl(path), {
    ...init,
    credentials: 'include',
    headers: {
      Accept: 'application/json',
      ...init?.headers,
    },
  })

  if (!response.ok) {
    const problem = await response.json().catch(() => null) as ApiProblem | null
    throw new ApiError(response.status, problem?.detail ?? problem?.title ?? 'The request could not be completed.', problem?.rateLimitResetAt)
  }

  if (response.status === 204) return undefined as Response
  return response.json() as Promise<Response>
}

export const authApi = {
  current(signal?: AbortSignal) {
    return request<GitHubUserResponse>('/api/auth/github/me', { signal })
  },

  startUrl() {
    return apiUrl('/api/auth/github/start')
  },

  async disconnect() {
    const csrf = await request<CsrfTokenResponse>('/api/auth/csrf')
    return request<void>('/api/auth/github/disconnect', {
      method: 'POST',
      headers: { [csrf.headerName]: csrf.token },
    })
  },
}

async function csrfHeaders() {
  const csrf = await request<CsrfTokenResponse>('/api/auth/csrf')
  return { [csrf.headerName]: csrf.token }
}

export const gitHubApi = {
  repositories(page: number, perPage: number, signal?: AbortSignal) {
    const params = new URLSearchParams({ page: page.toString(), perPage: perPage.toString() })
    return request<GitHubRepositoryPageResponse>(`/api/github/repositories?${params}`, { signal })
  },

  async importRepository(githubRepositoryId: number) {
    return request<RepositoryResponse>(`/api/github/repositories/${githubRepositoryId}/import`, {
      method: 'POST',
      headers: await csrfHeaders(),
    })
  },
}

export const repositoryApi = {
  list(signal?: AbortSignal) {
    return request<RepositoryResponse[]>('/api/repositories', { signal })
  },

  get(id: string, signal?: AbortSignal) {
    return request<RepositoryResponse>(`/api/repositories/${id}`, { signal })
  },

  create(payload: CreateRepositoryRequest) {
    return request<RepositoryResponse>('/api/repositories', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
    })
  },

  analysisHistory(repositoryId: string, signal?: AbortSignal) {
    return request<AnalysisJobResponse[]>(`/api/repositories/${repositoryId}/analysis-jobs`, { signal })
  },

  async requestAnalysis(repositoryId: string, includeHistory: boolean) {
    return request<AnalysisJobResponse>(`/api/repositories/${repositoryId}/analysis-jobs`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', ...await csrfHeaders() },
      body: JSON.stringify({ includeHistory }),
    })
  },

  async cancelAnalysis(repositoryId: string, jobId: string) {
    return request<AnalysisJobResponse>(`/api/repositories/${repositoryId}/analysis-jobs/${jobId}/cancel`, {
      method: 'POST',
      headers: await csrfHeaders(),
    })
  },

  codeChurn(repositoryId: string, period: HistoryPeriod, signal?: AbortSignal) {
    const params = new URLSearchParams({ period })
    return request<CodeChurnRankingResponse>(`/api/repositories/${repositoryId}/code-churn?${params}`, { signal })
  },

  contributorOwnership(repositoryId: string, signal?: AbortSignal) {
    return request<ContributorOverviewResponse>(`/api/repositories/${repositoryId}/contributors`, { signal })
  },
}