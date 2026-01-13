export type Credentials = {
  username: string
  password: string
}

const STORAGE_KEY = 'url-shortener.credentials'

export function loadCredentials(): Credentials | null {
  try {
    const raw = sessionStorage.getItem(STORAGE_KEY)
    if (!raw) return null
    return JSON.parse(raw) as Credentials
  } catch {
    return null
  }
}

export function saveCredentials(creds: Credentials) {
  sessionStorage.setItem(STORAGE_KEY, JSON.stringify(creds))
}

export function clearCredentials() {
  sessionStorage.removeItem(STORAGE_KEY)
}

export function toBasicAuthHeader(creds: Credentials): string {
  const token = btoa(`${creds.username}:${creds.password}`)
  return `Basic ${token}`
}


