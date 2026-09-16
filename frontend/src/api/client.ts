import type { AnalysisJobResponse, ApiProblem, CreateRepositoryRequest, RepositoryResponse } from './types'

const configuredApiUrl = import.meta.env.VITE_API_URL?.trim() ?? ''
const apiBaseUrl = configuredApiUrl.replace(/\/$/, '')

export class ApiError extends Error {
  readonly status: number

  constructor(status: number, message: string) {
    super(message)
    this.name = 'ApiError'
    this.status = status
  }
}

async function request<Response>(path: string, init?: RequestInit): Promise<Response> {
  const response = await fetch(`${apiBaseUrl}${path}`, {
    ...init,
    headers: {
      Accept: 'application/json',
      ...init?.headers,
    },
  })

  if (!response.ok) {
    const problem = await response.json().catch(() => null) as ApiProblem | null
    throw new ApiError(response.status, problem?.detail ?? problem?.title ?? 'The request could not be completed.')
  }

  return response.json() as Promise<Response>
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
}