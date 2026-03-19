import { useState, useEffect, useCallback } from 'react'
import { fetchAllMessages } from './services/api'

const TYPE_LABELS = {
  A: 'Navigational Warning', B: 'Meteorological Warning', C: 'Ice Report',
  D: 'SAR Information', E: 'Weather Forecast', F: 'Pilot Service',
  J: 'SATNAV', L: 'Navigational Warning', T: 'Test'
}
const TYPE_COLORS = {
  A: { bg: '#E6F1FB', color: '#0C447C' },
  B: { bg: '#EAF3DE', color: '#27500A' },
  D: { bg: '#FCEBEB', color: '#791F1F' },
  E: { bg: '#E1F5EE', color: '#085041' },
  J: { bg: '#FAEEDA', color: '#633806' },
}
const defaultColor = { bg: '#F1EFE8', color: '#444441' }
const COUNTRY_META = {
  'Greece':         { flag: '🇬🇷', label: 'Greece' },
  'Turkey':         { flag: '🇹🇷', label: 'Turkey' },
  'United Kingdom': { flag: '🇬🇧', label: 'United Kingdom' },
  'Sweden':         { flag: '🇸🇪', label: 'Sweden' },
}

function useIsMobile() {
  const [mobile, setMobile] = useState(window.innerWidth < 768)
  useEffect(() => {
    const fn = () => setMobile(window.innerWidth < 768)
    window.addEventListener('resize', fn)
    return () => window.removeEventListener('resize', fn)
  }, [])
  return mobile
}

function TypeBadge({ type }) {
  const c = TYPE_COLORS[type] || defaultColor
  return (
    <span style={{ fontSize: 11, fontWeight: 500, padding: '2px 8px', borderRadius: 99, background: c.bg, color: c.color }}>
      {type} — {TYPE_LABELS[type] || 'Other'}
    </span>
  )
}

function MessageCard({ msg, selected, onClick }) {
  return (
    <div onClick={onClick} style={{
      background: '#fff',
      border: `${selected ? '1.5px solid #185FA5' : '0.5px solid #ddd'}`,
      borderRadius: 12, padding: '12px 14px', marginBottom: 8, cursor: 'pointer'
    }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 6 }}>
        <TypeBadge type={msg.type} />
        <span style={{ marginLeft: 'auto', fontSize: 11, color: '#888' }}>
          {msg.publishedAt ? msg.publishedAt.substring(0, 10) : ''}
        </span>
      </div>
      <div style={{ fontSize: 12, color: '#555', fontWeight: 500, marginBottom: 4 }}>{msg.station}</div>
      <div style={{ fontSize: 12, color: '#888', fontFamily: 'monospace', lineHeight: 1.5, overflow: 'hidden', display: '-webkit-box', WebkitLineClamp: 2, WebkitBoxOrient: 'vertical' }}>
        {msg.raw}
      </div>
    </div>
  )
}

function DetailPane({ msg, onBack, isMobile }) {
  if (!msg) return (
    <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', height: '100%', gap: 12, color: '#aaa', fontSize: 13, textAlign: 'center', padding: 24 }}>
      <span style={{ fontSize: 40, opacity: .3 }}>⚓</span>
      <span>Select a message to view details</span>
    </div>
  )
  return (
    <div style={{ padding: '16px 20px', overflowY: 'auto', height: '100%' }}>
      {isMobile && (
        <button onClick={onBack} style={{
          display: 'flex', alignItems: 'center', gap: 6, marginBottom: 16,
          background: 'none', border: 'none', cursor: 'pointer',
          color: '#185FA5', fontSize: 14, padding: 0
        }}>
          ← Back
        </button>
      )}
      <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 4 }}>
        <span style={{ fontSize: 20 }}>{msg.flag}</span>
        <span style={{ fontWeight: 500, fontSize: 15 }}>{msg.station}</span>
      </div>
      <div style={{ fontSize: 12, color: '#888', marginBottom: 14 }}>
        {msg.country} · {msg.typeLabel} · {msg.publishedAt}
        {msg.sourceUrl && (<> · <a href={msg.sourceUrl} target="_blank" rel="noreferrer" style={{ color: '#185FA5' }}>Source ↗</a></>)}
      </div>
      <pre style={{
        fontSize: 12, fontFamily: 'monospace', whiteSpace: 'pre-wrap',
        lineHeight: 1.6, background: '#f5f5f3', padding: 12,
        borderRadius: 8, color: '#555', overflowX: 'auto'
      }}>
        {msg.raw}
      </pre>
    </div>
  )
}

function CountryGroup({ country, messages, selectedMsg, onSelectMsg }) {
  const [open, setOpen] = useState(true)
  const [activeStation, setActiveStation] = useState(null)
  const meta = COUNTRY_META[country] || { flag: '🌍', label: country }
  const stationNames = [...new Set(messages.map(m => m.station))]
  const filteredMsgs = activeStation ? messages.filter(m => m.station === activeStation) : messages

  return (
    <div style={{ borderBottom: '0.5px solid #eee' }}>
      <div onClick={() => setOpen(o => !o)} style={{
        display: 'flex', alignItems: 'center', gap: 8,
        padding: '10px 16px', cursor: 'pointer',
        background: open ? '#fafaf9' : '#fff',
        borderBottom: open ? '0.5px solid #eee' : 'none'
      }}>
        <span style={{ fontSize: 18 }}>{meta.flag}</span>
        <span style={{ fontWeight: 500, fontSize: 13, flex: 1 }}>{meta.label}</span>
        <span style={{ fontSize: 11, background: '#f0f0ee', color: '#666', borderRadius: 99, padding: '1px 8px' }}>{messages.length}</span>
        <span style={{ fontSize: 12, color: '#aaa', transform: open ? 'rotate(90deg)' : 'rotate(0)', transition: 'transform .2s', display: 'inline-block' }}>›</span>
      </div>
      {open && (
        <div>
          <div style={{ display: 'flex', gap: 6, flexWrap: 'wrap', padding: '8px 16px', background: '#fafaf9', borderBottom: '0.5px solid #eee' }}>
            <button onClick={() => setActiveStation(null)} style={{ fontSize: 11, padding: '3px 10px', borderRadius: 99, border: '0.5px solid #ccc', cursor: 'pointer', background: !activeStation ? '#185FA5' : '#fff', color: !activeStation ? '#fff' : '#555' }}>All</button>
            {stationNames.map(s => (
              <button key={s} onClick={() => setActiveStation(s === activeStation ? null : s)} style={{ fontSize: 11, padding: '3px 10px', borderRadius: 99, border: '0.5px solid #ccc', cursor: 'pointer', background: activeStation === s ? '#185FA5' : '#fff', color: activeStation === s ? '#fff' : '#555' }}>{s}</button>
            ))}
          </div>
          <div style={{ padding: '10px 16px' }}>
            {filteredMsgs.length === 0
              ? <div style={{ fontSize: 12, color: '#aaa', textAlign: 'center', padding: '12px 0' }}>No messages for this station.</div>
              : filteredMsgs.map(m => <MessageCard key={m.id} msg={m} selected={selectedMsg?.id === m.id} onClick={() => onSelectMsg(m)} />)
            }
          </div>
        </div>
      )}
    </div>
  )
}

export default function App() {
  const isMobile = useIsMobile()
  const [messages, setMessages] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [selectedMsg, setSelectedMsg] = useState(null)
  const [search, setSearch] = useState('')
  const [typeFilter, setTypeFilter] = useState('')
  const [lastUpdated, setLastUpdated] = useState(null)
  // Mobile: 'list' or 'detail'
  const [mobileView, setMobileView] = useState('list')

  const load = useCallback(async () => {
    setLoading(true); setError(null)
    try {
      const data = await fetchAllMessages()
      setMessages(data); setLastUpdated(new Date())
    } catch (e) {
      setError('Failed to load messages.')
    } finally { setLoading(false) }
  }, [])

  useEffect(() => { load() }, [load])

  const filtered = messages.filter(m => {
    if (typeFilter && m.type !== typeFilter) return false
    if (search) {
      const q = search.toLowerCase()
      return m.station?.toLowerCase().includes(q) || m.raw?.toLowerCase().includes(q) || m.country?.toLowerCase().includes(q)
    }
    return true
  })

  const byCountry = filtered.reduce((acc, m) => {
    if (!acc[m.country]) acc[m.country] = []
    acc[m.country].push(m)
    return acc
  }, {})

  const sarCount = messages.filter(m => m.type === 'D').length

  function handleSelectMsg(m) {
    setSelectedMsg(m)
    if (isMobile) setMobileView('detail')
  }

  function handleBack() {
    setMobileView('list')
    setSelectedMsg(null)
  }

  // Sidebar content
  const sidebar = (
    <div style={{
      display: 'flex', flexDirection: 'column',
      height: isMobile ? '100vh' : '100%',
      borderRight: isMobile ? 'none' : '0.5px solid #e5e5e5',
      overflow: 'hidden'
    }}>
      {/* Header */}
      <div style={{ padding: '14px 16px', borderBottom: '0.5px solid #e5e5e5', flexShrink: 0 }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 10 }}>
          <div style={{ width: 32, height: 32, background: '#185FA5', borderRadius: 8, display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: 16 }}>⚓</div>
          <div>
            <div style={{ fontWeight: 500, fontSize: 15 }}>NAVTEX Monitor</div>
            <div style={{ fontSize: 11, color: '#888' }}>🇹🇷 🇬🇷 🇬🇧 🇸🇪 Live Maritime Warnings</div>
          </div>
          <div style={{ marginLeft: 'auto', fontSize: 10, background: '#e8f5e9', color: '#2e7d32', padding: '2px 7px', borderRadius: 99 }}>● LIVE</div>
        </div>

        {/* Stats */}
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: 6, marginBottom: 10 }}>
          {[['Total', messages.length], ['SAR', sarCount], ['Updated', lastUpdated ? lastUpdated.toLocaleTimeString('en-GB', { hour: '2-digit', minute: '2-digit' }) : '—']].map(([l, v]) => (
            <div key={l} style={{ background: '#f5f5f3', borderRadius: 8, padding: '6px 8px' }}>
              <div style={{ fontSize: 10, color: '#888' }}>{l}</div>
              <div style={{ fontSize: 16, fontWeight: 500 }}>{v}</div>
            </div>
          ))}
        </div>

        <input value={search} onChange={e => setSearch(e.target.value)} placeholder="Search station or content..." style={{ width: '100%', fontSize: 13, padding: '7px 10px', border: '0.5px solid #ccc', borderRadius: 8, marginBottom: 6, boxSizing: 'border-box' }} />
        <select value={typeFilter} onChange={e => setTypeFilter(e.target.value)} style={{ width: '100%', fontSize: 13, padding: '6px 8px', border: '0.5px solid #ccc', borderRadius: 8 }}>
          <option value="">All message types</option>
          <option value="A">A — Navigational Warning</option>
          <option value="B">B — Meteorological Warning</option>
          <option value="D">D — SAR Information</option>
          <option value="E">E — Weather Forecast</option>
          <option value="J">J — SATNAV</option>
        </select>
      </div>

      {/* Message list */}
      <div style={{ flex: 1, overflowY: 'auto' }}>
        {loading && <div style={{ textAlign: 'center', padding: '2rem', color: '#888', fontSize: 13 }}>⏳ Loading messages...</div>}
        {error && <div style={{ padding: '1rem', color: '#c62828', fontSize: 13 }}>⚠️ {error} <button onClick={load} style={{ marginLeft: 8, color: '#185FA5', background: 'none', border: 'none', cursor: 'pointer' }}>Retry</button></div>}
        {!loading && !error && Object.entries(byCountry).map(([country, msgs]) => (
          <CountryGroup key={country} country={country} messages={msgs} selectedMsg={selectedMsg} onSelectMsg={handleSelectMsg} />
        ))}
        {!loading && !error && Object.keys(byCountry).length === 0 && <div style={{ textAlign: 'center', padding: '2rem', color: '#aaa', fontSize: 13 }}>No messages found.</div>}
      </div>

      {/* Footer */}
      <div style={{ padding: '8px 16px', borderTop: '0.5px solid #eee', fontSize: 10, color: '#aaa', display: 'flex', justifyContent: 'space-between', flexShrink: 0 }}>
        <span>hnhs.gr · kiyiemniyeti.gov.tr · admiralty.co.uk · sjofartsverket.se</span>
        <button onClick={load} style={{ background: 'none', border: 'none', cursor: 'pointer', color: '#185FA5', fontSize: 11 }}>↻ Refresh</button>
      </div>
    </div>
  )

  const detail = (
    <div style={{ overflow: 'hidden', display: 'flex', flexDirection: 'column', height: isMobile ? '100vh' : '100%' }}>
      <div style={{ padding: '14px 20px', borderBottom: '0.5px solid #e5e5e5', flexShrink: 0 }}>
        <span style={{ fontWeight: 500, fontSize: 15 }}>
          {selectedMsg ? `${selectedMsg.station} — ${selectedMsg.typeLabel}` : 'Message Detail'}
        </span>
      </div>
      <div style={{ flex: 1, overflow: 'hidden' }}>
        <DetailPane msg={selectedMsg} onBack={handleBack} isMobile={isMobile} />
      </div>
    </div>
  )

  // Mobile: show one panel at a time
  if (isMobile) {
    return (
      <div style={{ width: '100vw', height: '100vh', overflow: 'hidden' }}>
        {mobileView === 'list' ? sidebar : detail}
      </div>
    )
  }

  // Desktop: side by side
  return (
    <div style={{ display: 'grid', gridTemplateColumns: '320px 1fr', height: '100vh', fontFamily: 'system-ui, sans-serif' }}>
      {sidebar}
      {detail}
    </div>
  )
}
