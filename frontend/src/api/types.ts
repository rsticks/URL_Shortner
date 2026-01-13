export type ErrorResponse = {
  timestamp?: string
  status?: number
  error?: string
  message?: string
}

export type ResponseDto = {
  shortUrl: string
}

export type RegisterResult = {
  id: number
  username: string
  subscriptionExpiresAt: string | null
  subscribed: boolean
}

export type PurchaseResult = {
  username: string
  subscriptionExpiresAt: string | null
  subscribed: boolean
}

export type SubscriptionPlan = 'MONTHLY' | 'YEARLY'

export type UserLinkDto = {
  hash: string
  originalUrl: string
  shortUrl: string
  createdAt: string
}


