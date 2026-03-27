import { useEffect, useMemo, useState } from 'react'
import { useTranslation } from 'react-i18next'
import type { PreviewSessionResponse } from './types'
import { PreviewContent } from './components/PreviewContent'

const backendError = (fallback: string) => ({
  message: fallback,
})

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
    fileName: 'sample-badge.svg',
    label: 'SVG',
    description: 'Vector image sample for browser-native preview.',
  },
]

function App() {
  const { t, i18n } = useTranslation()
  const [loading, setLoading] = useState(false)
  const [session, setSession] = useState<PreviewSessionResponse | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [selectedFileName, setSelectedFileName] = useState(demoFiles[0]?.fileName ?? '')

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
      const response = await fetch('/api/previews/resolve', {
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
    const demoUrl = new URL(`/demo/files/${fileName}`, window.location.origin).toString()
    await resolvePreview(demoUrl)
  }

  useEffect(() => {
    if (!selectedFileName || session || loading) {
      return
    }
    void openDemo(selectedFileName)
  }, [selectedFileName])

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
            <>
              <div className="preview-frame">
                <PreviewContent session={session} emptyText={t('contentUnavailable')} />
              </div>
            </>
          ) : (
            <p className="empty-state">{loading ? t('loadingAction') : statusTitle}</p>
          )}
        </section>
      </main>
    </div>
  )
}

export default App
