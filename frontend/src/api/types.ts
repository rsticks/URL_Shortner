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
  subscriptionExpiresAt: string | number | null
  subscribed: boolean
}

export type PurchaseResult = {
  username: string
  subscriptionExpiresAt: string | number | null
  subscribed: boolean
}

export type SubscriptionPlan = 'MONTHLY' | 'YEARLY'

export type UserLinkDto = {
  hash: string
  originalUrl: string
  shortUrl: string
  createdAt: string | number | number[]
}

export type DailyStatDto = {
  day: string // ISO date: YYYY-MM-DD
  clicks: number
  uniqueVisitors: number
}

export type DimStatDto = {
  value: string
  clicks: number
}

export type UrlStatsResponse = {
  urlHash: string
  from: string // ISO date
  to: string // ISO date
  daily: DailyStatDto[]
  topReferrers: DimStatDto[]
  referrerCategories: DimStatDto[]
  topLanguages: DimStatDto[]
  deviceTypes: DimStatDto[]
  osFamilies: DimStatDto[]
  osVersions: DimStatDto[]
  browserFamilies: DimStatDto[]
  browserVersions: DimStatDto[]
  countries: DimStatDto[]
  regions: DimStatDto[]
  cities: DimStatDto[]
  timezones: DimStatDto[]
  asns: DimStatDto[]
  providers: DimStatDto[]
  networkTypes: DimStatDto[]
  proxyStatuses: DimStatDto[]
  vpnStatuses: DimStatDto[]
  torStatuses: DimStatDto[]
  hoursOfDay: DimStatDto[]
  daysOfWeek: DimStatDto[]
  clientHintPlatforms: DimStatDto[]
  clientHintPlatformVersions: DimStatDto[]
  clientHintMobiles: DimStatDto[]
  clientHintModels: DimStatDto[]
}

export type LoginResponse = {
  tokenType: string
  accessToken: string
  expiresAtEpochSeconds: number
  username: string
}


