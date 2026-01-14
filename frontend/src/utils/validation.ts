export function isLatinAuth(value: string): boolean {
  // Matches backend constraint for username/password (ASCII subset).
  return /^[A-Za-z0-9._-]+$/.test(value)
}


