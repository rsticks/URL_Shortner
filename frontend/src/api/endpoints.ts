import type { Credentials } from '../auth/credentials'
import { http, withAuth } from './http'
import type { LoginResponse, PurchaseResult, RegisterResult, ResponseDto, SubscriptionPlan, UserLinkDto } from './types'

export async function createShortUrl(url: string, creds?: Credentials | null): Promise<ResponseDto> {
  // Backend may return either JSON: { shortUrl: "..." } or plain text: "http://...".
  const res = await http.post('/api/v1/url', { url }, withAuth(creds))
  const data: unknown = res.data
  if (typeof data === 'string') {
    return { shortUrl: data }
  }
  if (data && typeof data === 'object') {
    const anyData = data as Record<string, unknown>
    const shortUrl = anyData.shortUrl ?? anyData.shortURL ?? anyData.short_url
    if (typeof shortUrl === 'string') {
      return { shortUrl }
    }
  }
  throw new Error('Unexpected response from /api/v1/url')
}

export async function register(username: string, password: string): Promise<RegisterResult> {
  const res = await http.post<RegisterResult>('/api/v1/auth/register', { username, password })
  return res.data
}

export async function login(username: string, password: string): Promise<LoginResponse> {
  const res = await http.post<LoginResponse>('/api/v1/auth/login', { username, password })
  return res.data
}

export async function refresh(): Promise<LoginResponse> {
  const res = await http.post<LoginResponse>('/api/v1/auth/refresh')
  return res.data
}

export async function logout(): Promise<void> {
  await http.post('/api/v1/auth/logout')
}

export async function subscriptionMe(creds: Credentials): Promise<PurchaseResult> {
  const res = await http.get<PurchaseResult>('/api/v1/subscription/me', withAuth(creds))
  return res.data
}

export async function purchaseSubscription(plan: SubscriptionPlan, creds: Credentials): Promise<PurchaseResult> {
  const res = await http.post<PurchaseResult>('/api/v1/subscription/purchase', { plan }, withAuth(creds))
  return res.data
}

export async function myUrls(creds: Credentials): Promise<UserLinkDto[]> {
  const res = await http.get<UserLinkDto[]>('/api/v1/url/me', withAuth(creds))
  return res.data
}


