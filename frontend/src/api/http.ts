import axios from 'axios'
import type { Credentials } from '../auth/credentials'
import { loadCredentials, saveCredentials, clearCredentials, toAuthHeader } from '../auth/credentials'
import type { ErrorResponse } from './types'
import type { LoginResponse } from './types'

const baseURL = import.meta.env.VITE_API_BASE ?? ''

export const http = axios.create({
  baseURL,
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json',
  },
})

let refreshPromise: Promise<LoginResponse> | null = null

async function doRefresh(): Promise<LoginResponse> {
  if (!refreshPromise) {
    refreshPromise = http.post<LoginResponse>('/api/v1/auth/refresh').then((r) => r.data).finally(() => {
      refreshPromise = null
    })
  }
  return refreshPromise
}

// Auto-refresh access token on 401 and retry once.
http.interceptors.response.use(
  (resp) => resp,
  async (error) => {
    if (!axios.isAxiosError(error)) throw error
    const status = error.response?.status
    const config = error.config as any
    const url = (config?.url as string | undefined) ?? ''

    const isAuthCall =
      url.includes('/api/v1/auth/login') || url.includes('/api/v1/auth/register') || url.includes('/api/v1/auth/refresh')

    if (status === 401 && config && !config._retry && !isAuthCall) {
      config._retry = true
      try {
        const refreshed = await doRefresh()
        const existing = loadCredentials()
        const next: Credentials = {
          username: refreshed.username || existing?.username || '',
          token: refreshed.accessToken,
        }
        saveCredentials(next)
        config.headers = config.headers ?? {}
        config.headers.Authorization = toAuthHeader(next)
        return http(config)
      } catch (e) {
        clearCredentials()
        throw e
      }
    }
    throw error
  },
)

export function withAuth(creds: Credentials | null | undefined) {
  if (!creds) return {}
  return {
    headers: {
      Authorization: toAuthHeader(creds),
    },
  }
}

export function formatApiError(err: unknown): string {
  if (axios.isAxiosError(err)) {
    if (err.response?.status === 401) {
      // If no credentials saved, it's most likely a login mistake.
      // If credentials exist, token likely expired and refresh failed.
      return loadCredentials() ? 'Сессия истекла (войдите заново)' : 'Неверный логин или пароль'
    }
    const data = err.response?.data as ErrorResponse | undefined
    return data?.message || data?.error || err.message
  }
  if (err instanceof Error) return err.message
  return 'Unexpected error'
}


