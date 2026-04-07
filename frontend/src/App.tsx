import { useEffect, useMemo, useState } from 'react'
import { useTranslation } from 'react-i18next'
import type { PreviewSessionResponse } from './types'
import { PreviewContent } from './components/PreviewContent'

const backendError = (fallback: string) => ({
  message: fallback,
})

const currentAppUrl = () => new URL(window.location.href)

const demoFiles = [
  {
    fileName: 'sample-complex.docx',
    label: 'DOCX',
    description: 'Structured office document with formatted text, table, and poster image.',
    rich: true,
  },
  {
    fileName: 'sample-complex.xlsx',
    label: 'XLSX',
    description: 'Multi-sheet workbook with fonts, formulas, chart, merged cells, and embedded image.',
    rich: true,
  },
  {
    fileName: 'sample-complex.pptx',
    label: 'PPTX',
    description: 'Multi-slide deck with bilingual text, image placement, table layout, and typography variety.',
    rich: true,
  },
  {
    fileName: 'sample-keynote.key',
    label: 'KEY',
    description: 'Unsupported Apple Keynote sample to demonstrate the fallback state.',
  },
  {
    fileName: 'sample.pdf',
    label: 'PDF',
    description: 'Direct PDF preview without conversion.',
  },
  {
    fileName: 'sample-readme.md',
    label: 'Markdown',
    description: 'Markdown rendered safely in read-only mode.',
  },
  {
    fileName: 'sample-notes.txt',
    label: 'Text',
    description: 'Simple text file rendered as read-only preview content.',
  },
  {
    fileName: 'sample-code.ts',
    label: 'Code',
    description: 'TypeScript sample for syntax-highlighted code preview.',
  },
  {
    fileName: 'sample-data.json',
    label: 'JSON',
    description: 'Structured JSON sample using the text/code preview path.',
  },
  {
    fileName: 'sample-grid.csv',
    label: 'CSV',
    description: 'Comma-separated data sample in the supported read-only flow.',
  },
  {
    fileName: 'preview-poster.png',
    label: 'PNG',
    description: 'Image sample used across the Office demo files as well.',
  },
  {
    fileName: 'preview-poster.jpg',
    label: 'JPG',
    description: 'JPEG image sample for browser-native image preview.',
  },
  {
    fileName: 'test_video.mp4',
    label: 'MP4',
    description: 'Inline video sample for browser-native playback preview.',
  },
  {
    fileName: 'sample-badge.svg',
    label: 'SVG',
    description: 'Vector image sample for browser-native preview.',
  },
]

function App() {
  const { t, i18n } = useTranslation()
  const query = new URLSearchParams(window.location.search)
  const embeddedFileUrl = query.get('fileUrl') || query.get('url') || ''
  const embeddedMode = embeddedFileUrl.length > 0
  const [loading, setLoading] = useState(false)
  const [session, setSession] = useState<PreviewSessionResponse | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [selectedFileName, setSelectedFileName] = useState(demoFiles[0]?.fileName ?? '')

  useEffect(() => {
    document.documentElement.classList.toggle('embed-mode', embeddedMode)
    document.body.classList.toggle('embed-mode', embeddedMode)
    return () => {
      document.documentElement.classList.remove('embed-mode')
      document.body.classList.remove('embed-mode')
    }
  }, [embeddedMode])

  const statusTitle = useMemo(() => {
    if (!session) return t('sessionTitle')
    if (session.status === 'READY') return t('readyTitle')
    if (session.status === 'PROCESSING') return t('processingTitle')
    if (session.status === 'UNSUPPORTED') return t('unsupportedTitle')
    return t('failedTitle')
  }, [session, t])

  const resolvePreview = async (nextSourceUrl: string) => {
    setLoading(true)
    setError(null)
    try {
      const response = await fetch('api/previews/resolve', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          sourceUrl: nextSourceUrl,
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

  const openDemo = async (fileName: string) => {
    const demoUrl = new URL(`demo/files/${fileName}`, currentAppUrl()).toString()
    await resolvePreview(demoUrl)
  }

  useEffect(() => {
    if (embeddedMode) {
      if (!session && !loading) {
        void resolvePreview(embeddedFileUrl)
      }
      return
    }

    if (!selectedFileName || session || loading) {
      return
    }
    void openDemo(selectedFileName)
  }, [embeddedFileUrl, embeddedMode, selectedFileName, session, loading])

  if (embeddedMode) {
    return (
      <main className="embed-shell">
        <section className="embed-frame" aria-label="Embedded preview frame">
          {session ? (
            <div className="preview-frame preview-frame--embedded">
              {loading ? (
                <div className="preview-loading" aria-live="polite">
                  <div className="preview-loading__spinner" />
                  <p className="preview-loading__text">{t('loadingAction')}</p>
                </div>
              ) : null}
              {error ? <p className="error-banner error-banner--embedded">{error}</p> : null}
              <PreviewContent session={session} emptyText={t('contentUnavailable')} />
            </div>
          ) : (
            <div className="preview-frame preview-frame--embedded">
              {loading ? (
                <div className="preview-loading" aria-live="polite">
                  <div className="preview-loading__spinner" />
                  <p className="preview-loading__text">{t('loadingAction')}</p>
                </div>
              ) : (
                <p className="empty-state empty-state--embedded">{error ?? statusTitle}</p>
              )}
            </div>
          )}
        </section>
      </main>
    )
  }

  return (
    <div className="app-shell">
      <header className="hero">
          <div className="hero__copy">
            <h1>{t('title')}</h1>
          </div>
        <label className="locale-picker locale-picker--compact">
          <span>{t('language')}</span>
          <select value={i18n.language} onChange={(event) => void i18n.changeLanguage(event.target.value)}>
            <option value="zh">{t('localeZh')}</option>
            <option value="en">{t('localeEn')}</option>
          </select>
        </label>
      </header>

      <main className="content-grid">
        <section className="panel panel--controls">
          <div className="file-list">
            {demoFiles.map((demo) => (
              <button
                key={demo.fileName}
                className={`file-list__item ${selectedFileName === demo.fileName ? 'is-active' : ''}`}
                type="button"
                disabled={loading && selectedFileName === demo.fileName}
                onClick={() => {
                  setSelectedFileName(demo.fileName)
                  void openDemo(demo.fileName)
                }}
              >
                <span>{demo.label}</span>
              </button>
            ))}
          </div>

          {error ? <p className="error-banner">{error}</p> : null}
        </section>

        <section className="panel panel--preview">
          {session ? (
            <div className="preview-frame">
              {loading ? (
                <div className="preview-loading" aria-live="polite">
                  <div className="preview-loading__spinner" />
                  <p className="preview-loading__text">{t('loadingAction')}</p>
                </div>
              ) : null}
              <PreviewContent session={session} emptyText={t('contentUnavailable')} />
            </div>
          ) : (
            <div className="preview-frame">
              {loading ? (
                <div className="preview-loading" aria-live="polite">
                  <div className="preview-loading__spinner" />
                  <p className="preview-loading__text">{t('loadingAction')}</p>
                </div>
              ) : (
                <p className="empty-state">{statusTitle}</p>
              )}
            </div>
          )}
        </section>
      </main>
    </div>
  )
}

export default App
