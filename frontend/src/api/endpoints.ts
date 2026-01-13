import type { Credentials } from '../auth/credentials'
import { http, withAuth } from './http'
import type { PurchaseResult, RegisterResult, ResponseDto, SubscriptionPlan, UserLinkDto } from './types'

export async function createShortUrl(url: string, creds?: Credentials | null): Promise<ResponseDto> {
  const res = await http.post<ResponseDto>('/api/v1/url', { url }, withAuth(creds))
  return res.data
}

export async function register(username: string, password: string): Promise<RegisterResult> {
  const res = await http.post<RegisterResult>('/api/v1/auth/register', { username, password })
  return res.data
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


