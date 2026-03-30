import { useSyncExternalStore } from 'react'

export type HistoryItem = {
  originalUrl: string
  shortUrl: string
  createdAt: string
}

const HISTORY_LIMIT = 12

let history: HistoryItem[] = []
const listeners = new Set<() => void>()

function emit() {
  for (const l of listeners) l()
}

function subscribe(listener: () => void) {
  listeners.add(listener)
  return () => listeners.delete(listener)
}

function getSnapshot() {
  return history
}

export function useShortenHistory(): HistoryItem[] {
  // No persistence: purely in-memory for the current tab session.
  return useSyncExternalStore(subscribe, getSnapshot, getSnapshot)
}

export function addToShortenHistory(item: HistoryItem) {
  history = [item, ...history].slice(0, HISTORY_LIMIT)
  emit()
}

export function clearShortenHistory() {
  history = []
  emit()
}

