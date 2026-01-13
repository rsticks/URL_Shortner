import axios from 'axios'
import type { Credentials } from '../auth/credentials'
import { toBasicAuthHeader } from '../auth/credentials'
import type { ErrorResponse } from './types'

const baseURL = import.meta.env.VITE_API_BASE ?? ''

export const http = axios.create({
  baseURL,
  headers: {
    'Content-Type': 'application/json',
  },
})

export function withAuth(creds: Credentials | null | undefined) {
  if (!creds) return {}
  return {
    headers: {
      Authorization: toBasicAuthHeader(creds),
    },
  }
}

export function formatApiError(err: unknown): string {
  if (axios.isAxiosError(err)) {
    const data = err.response?.data as ErrorResponse | undefined
    return data?.message || data?.error || err.message
  }
  return 'Unexpected error'
}


