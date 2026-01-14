export type Credentials = {
  username: string
  token: string
}

const STORAGE_KEY = 'url-shortener.credentials'
const AUTH_CHANGED_EVENT = 'url-shortener.auth-changed'

function notifyAuthChanged() {
  try {
    if (typeof window !== 'undefined') {
      window.dispatchEvent(new Event(AUTH_CHANGED_EVENT))
    }
  } catch {
    // ignore
  }
}

export function loadCredentials(): Credentials | null {
  try {
    const raw = sessionStorage.getItem(STORAGE_KEY)
    if (!raw) return null
    const parsed = JSON.parse(raw) as Partial<Credentials>
    if (!parsed || typeof parsed.username !== 'string' || typeof parsed.token !== 'string' || parsed.token.length === 0) {
      return null
    }
    return { username: parsed.username, token: parsed.token }
  } catch {
    return null
  }
}

export function saveCredentials(creds: Credentials) {
  sessionStorage.setItem(STORAGE_KEY, JSON.stringify(creds))
  notifyAuthChanged()
}

export function clearCredentials() {
  sessionStorage.removeItem(STORAGE_KEY)
  notifyAuthChanged()
}

export function toAuthHeader(creds: Credentials): string {
  return `Bearer ${creds.token}`
}

export function authChangedEventName() {
  return AUTH_CHANGED_EVENT
}


