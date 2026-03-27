import { useEffect, useMemo, useState } from 'react'
import Markdown from 'react-markdown'
import rehypeSanitize from 'rehype-sanitize'
import remarkGfm from 'remark-gfm'
import hljs from 'highlight.js/lib/core'
import javascript from 'highlight.js/lib/languages/javascript'
import json from 'highlight.js/lib/languages/json'
import xml from 'highlight.js/lib/languages/xml'
import css from 'highlight.js/lib/languages/css'
import python from 'highlight.js/lib/languages/python'
import java from 'highlight.js/lib/languages/java'
import type { PreviewSessionResponse } from '../types'
import { CsvPreview } from './CsvPreview'
import { PdfPreview } from './PdfPreview'

hljs.registerLanguage('javascript', javascript)
hljs.registerLanguage('json', json)
hljs.registerLanguage('xml', xml)
hljs.registerLanguage('css', css)
hljs.registerLanguage('python', python)
hljs.registerLanguage('java', java)

interface PreviewContentProps {
  session: PreviewSessionResponse
  emptyText: string
}

export function PreviewContent({ session, emptyText }: PreviewContentProps) {
  const [textContent, setTextContent] = useState<string>('')
  const [textError, setTextError] = useState<string | null>(null)
  const [frameLoading, setFrameLoading] = useState(false)

  useEffect(() => {
    setFrameLoading(session.previewMode === 'SPREADSHEET' && session.extension !== 'csv')
  }, [session.contentUrl, session.extension, session.previewMode])

  useEffect(() => {
    if (session.previewMode !== 'TEXT' && session.previewMode !== 'MARKDOWN') {
      setTextContent('')
      setTextError(null)
      return
    }

    let cancelled = false
    fetch(session.contentUrl)
      .then(async (response) => {
        if (!response.ok) {
          throw new Error('Unable to load preview content.')
        }
        const data = await response.text()
        if (!cancelled) {
          setTextContent(data)
        }
      })
      .catch((error) => {
        if (!cancelled) {
          setTextError(error instanceof Error ? error.message : 'Unable to load preview content.')
        }
      })

    return () => {
      cancelled = true
    }
  }, [session.contentUrl, session.previewMode])

  const highlighted = useMemo(() => {
    if (session.previewMode !== 'TEXT' || !textContent) {
      return ''
    }
    return hljs.highlightAuto(textContent).value
  }, [session.previewMode, textContent])

  if (session.status === 'UNSUPPORTED') {
    return (
      <div className="unsupported-preview">
        <p className="unsupported-preview__eyebrow">Unsupported file type</p>
        <h2 className="unsupported-preview__title">{session.fileName}</h2>
      </div>
    )
  }

  if (session.status !== 'READY') {
    return <p className="empty-state">{emptyText}</p>
  }

  if (session.previewMode === 'PDF') {
    return <PdfPreview fileName={session.fileName} url={session.contentUrl} />
  }

  if (session.previewMode === 'IMAGE') {
    return <img className="image-preview" src={session.contentUrl} alt={session.fileName} />
  }

  if (session.previewMode === 'VIDEO') {
    return (
      <video className="video-preview" src={session.contentUrl} controls preload="metadata">
        {session.fileName}
      </video>
    )
  }

  if (textError) {
    return <p className="error-banner">{textError}</p>
  }

  if (session.previewMode === 'SPREADSHEET') {
    if (session.extension === 'csv') {
      return <CsvPreview url={session.contentUrl} />
    }
    return (
      <div className="viewer-frame-shell">
        {frameLoading ? (
          <div className="preview-loading preview-loading--surface" aria-live="polite">
            <div className="preview-loading__spinner" />
            <p className="preview-loading__text">Loading spreadsheet preview...</p>
          </div>
        ) : null}
        <iframe
          className="viewer-frame spreadsheet-frame"
          src={session.contentUrl}
          title={session.fileName}
          onLoad={() => setFrameLoading(false)}
        />
      </div>
    )
  }

  if (session.previewMode === 'MARKDOWN') {
    return (
      <article className="markdown-body">
        <Markdown remarkPlugins={[remarkGfm]} rehypePlugins={[rehypeSanitize]}>
          {textContent}
        </Markdown>
      </article>
    )
  }

  if (session.previewMode === 'TEXT') {
    return (
      <pre className="code-preview">
        <code dangerouslySetInnerHTML={{ __html: highlighted || textContent }} />
      </pre>
    )
  }

  return <p className="empty-state">{emptyText}</p>
}
