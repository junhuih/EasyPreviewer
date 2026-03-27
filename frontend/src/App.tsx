import { useMemo, useState } from 'react'
import { useTranslation } from 'react-i18next'
import type { PreviewSessionResponse } from './types'
import { PreviewContent } from './components/PreviewContent'

const backendError = (fallback: string) => ({
  message: fallback,
})

function App() {
  const { t, i18n } = useTranslation()
  const [sourceUrl, setSourceUrl] = useState('')
  const [loading, setLoading] = useState(false)
  const [session, setSession] = useState<PreviewSessionResponse | null>(null)
  const [error, setError] = useState<string | null>(null)

  const statusTitle = useMemo(() => {
    if (!session) return t('sessionTitle')
    if (session.status === 'READY') return t('readyTitle')
    if (session.status === 'PROCESSING') return t('processingTitle')
    if (session.status === 'UNSUPPORTED') return t('unsupportedTitle')
    return t('failedTitle')
  }, [session, t])

  const onSubmit = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setLoading(true)
    setError(null)
    try {
      const response = await fetch('/api/previews/resolve', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          sourceUrl,
          locale: i18n.language,
        }),
      })
      const payload = await response.json()
      if (!response.ok) {
        const localized = i18n.language === 'zh' ? payload.messageZh : payload.messageEn
        throw backendError(localized ?? t('failedTitle'))
      }
      setSession(payload as PreviewSessionResponse)
    } catch (fetchError) {
      setError(fetchError instanceof Error ? fetchError.message : t('failedTitle'))
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="app-shell">
      <header className="hero">
        <div>
          <p className="eyebrow">Apache-2.0 · LibreOffice · Preview Only</p>
          <h1>{t('title')}</h1>
          <p className="subtitle">{t('subtitle')}</p>
        </div>
        <label className="locale-picker">
          <span>{t('language')}</span>
          <select value={i18n.language} onChange={(event) => void i18n.changeLanguage(event.target.value)}>
            <option value="zh">{t('localeZh')}</option>
            <option value="en">{t('localeEn')}</option>
          </select>
        </label>
      </header>

      <main className="content-grid">
        <section className="panel">
          <form onSubmit={onSubmit} className="resolve-form">
            <label htmlFor="sourceUrl">{t('sourceUrl')}</label>
            <input
              id="sourceUrl"
              name="sourceUrl"
              type="url"
              placeholder="https://example.com/sample.docx"
              value={sourceUrl}
              onChange={(event) => setSourceUrl(event.target.value)}
              required
            />
            <button disabled={loading}>{loading ? `${t('processingTitle')}...` : t('resolve')}</button>
          </form>
          <p className="notice">{t('previewOnly')}</p>
          <div className="support-box">
            <h2>{t('supportTitle')}</h2>
            <p>{t('supportBody')}</p>
          </div>
          {error ? <p className="error-banner">{error}</p> : null}
        </section>

        <section className="panel">
          <div className="session-header">
            <h2>{statusTitle}</h2>
            <span className={`status-pill ${session?.status?.toLowerCase() ?? 'idle'}`}>
              {session ? t(`status${session.status.charAt(0)}${session.status.slice(1).toLowerCase()}`) : t('sessionTitle')}
            </span>
          </div>

          {session ? (
            <>
              <dl className="session-meta">
                <div>
                  <dt>{t('fileLabel')}</dt>
                  <dd>{session.fileName}</dd>
                </div>
                <div>
                  <dt>{t('modeLabel')}</dt>
                  <dd>{t(`capability${session.previewMode.charAt(0)}${session.previewMode.slice(1).toLowerCase()}`)}</dd>
                </div>
                <div>
                  <dt>{t('localeLabel')}</dt>
                  <dd>{session.locale}</dd>
                </div>
                <div>
                  <dt>{t('messageLabel')}</dt>
                  <dd>{session.message}</dd>
                </div>
              </dl>

              <div className="preview-frame">
                <h3>{t('contentTitle')}</h3>
                <PreviewContent session={session} emptyText={t('contentUnavailable')} />
              </div>
            </>
          ) : (
            <p className="empty-state">{t('noSession')}</p>
          )}
        </section>
      </main>

      <footer className="footer">{t('footer')}</footer>
    </div>
  )
}

export default App

