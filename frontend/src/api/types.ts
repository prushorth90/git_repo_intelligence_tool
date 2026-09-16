export type RepositoryResponse = {
  id: string
  owner: string
  name: string
  fullName: string
  githubUrl: string
  githubRepositoryId: number | null
  defaultBranch: string
  privateRepository: boolean
  visibility: string
  primaryLanguage: string | null
  stars: number
  forks: number
  githubUpdatedAt: string | null
  connectedAt: string
}

export type CreateRepositoryRequest = {
  githubUrl: string
}

export type AnalysisJobStatus = 'QUEUED' | 'RUNNING' | 'COMPLETED' | 'FAILED' | 'CANCELLED'

export type AnalysisJobResponse = {
  id: string
  repositoryId: string
  status: AnalysisJobStatus
  requestedAt: string
  startedAt: string | null
  completedAt: string | null
  retryCount: number
  progressPercentage: number
  includeHistory: boolean
  failureReason: string | null
}

export type ApiProblem = {
  title?: string
  status?: number
  detail?: string
  rateLimitResetAt?: string
}

export type GitHubRepositoryResponse = {
  id: number
  owner: string
  name: string
  fullName: string
  htmlUrl: string
  defaultBranch: string
  visibility: string
  privateRepository: boolean
  primaryLanguage: string | null
  stars: number
  forks: number
  updatedAt: string
  imported: boolean
}

export type GitHubRepositoryPageResponse = {
  repositories: GitHubRepositoryResponse[]
  page: number
  perPage: number
  hasNextPage: boolean
  rateLimitRemaining: number
  rateLimitResetAt: string
}

export type HistoryPeriod = 'DAYS_30' | 'DAYS_90' | 'MONTHS_6' | 'ALL'

export type FileChurnResponse = {
  filePath: string
  language: string
  commitCount: number
  additions: number
  deletions: number
  uniqueContributors: number
  lastModifiedAt: string
  totalChurn: number
}

export type CodeChurnRankingResponse = {
  period: HistoryPeriod
  analysisId: string | null
  analyzedAt: string | null
  files: FileChurnResponse[]
}

export type ContributorOwnershipResponse = {
  contributorKey: string
  displayName: string
  totalCommits: number
  filesTouched: number
  additions: number
  deletions: number
  estimatedOwnershipPercent: number
  lastActivityAt: string
  primaryModules: string[]
}

export type ConcentratedFileResponse = {
  filePath: string
  language: string
  topContributorName: string
  topOwnershipPercent: number
  busFactor: number
  totalChurn: number
}

export type ContributorOverviewResponse = {
  analysisId: string | null
  analyzedAt: string | null
  repositoryBusFactor: number
  concentratedFileCount: number
  contributors: ContributorOwnershipResponse[]
  concentratedFiles: ConcentratedFileResponse[]
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