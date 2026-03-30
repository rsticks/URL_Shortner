export const AUTH_PASSWORD_MIN_LENGTH = 6
export const AUTH_PASSWORD_MAX_LENGTH = 72

export function isLatinAuth(value: string): boolean {
  // Matches backend constraint for username/password (ASCII subset).
  return /^[A-Za-z0-9._-]+$/.test(value)
}

export function getAuthPasswordError(value: string): string | null {
  if (value.length < AUTH_PASSWORD_MIN_LENGTH || value.length > AUTH_PASSWORD_MAX_LENGTH) {
    return `Пароль: от ${AUTH_PASSWORD_MIN_LENGTH} до ${AUTH_PASSWORD_MAX_LENGTH} символов`
  }
  if (!isLatinAuth(value)) {
    return 'Пароль: только латиница, цифры, ".", "_" или "-"'
  }
  return null
}


