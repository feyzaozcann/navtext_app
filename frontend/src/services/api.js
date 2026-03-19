// Points to your Render backend in production, localhost in dev
const BASE = import.meta.env.VITE_API_URL || '/api'

export async function fetchAllMessages() {
  const res = await fetch(`${BASE}/messages`)
  if (!res.ok) throw new Error('API error')
  return res.json()
}

export async function fetchByCountry(country) {
  const res = await fetch(`${BASE}/messages/country/${encodeURIComponent(country)}`)
  if (!res.ok) throw new Error('API error')
  return res.json()
}
