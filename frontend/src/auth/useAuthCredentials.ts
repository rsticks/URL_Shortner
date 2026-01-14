import { useEffect, useState } from 'react'
import { authChangedEventName, loadCredentials, type Credentials } from './credentials'

export function useAuthCredentials(): Credentials | null {
  const [creds, setCreds] = useState<Credentials | null>(() => loadCredentials())

  useEffect(() => {
    const onChange = () => setCreds(loadCredentials())
    onChange()
    window.addEventListener(authChangedEventName(), onChange)
    return () => window.removeEventListener(authChangedEventName(), onChange)
  }, [])

  return creds
}


