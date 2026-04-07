import { useEffect, useMemo, useRef, useState } from 'react'
import { GlobalWorkerOptions, getDocument } from 'pdfjs-dist'
import type { PDFDocumentProxy } from 'pdfjs-dist'
import workerSrc from 'pdfjs-dist/build/pdf.worker.min.mjs?url'

GlobalWorkerOptions.workerSrc = workerSrc

interface PdfPreviewProps {
  fileName: string
  url: string
}

export function PdfPreview({ fileName, url }: PdfPreviewProps) {
  const frameRef = useRef<HTMLDivElement | null>(null)
  const pagesViewportRef = useRef<HTMLDivElement | null>(null)
  const pagesRef = useRef<HTMLDivElement | null>(null)
  const scrollProgressRef = useRef(0)
  const loadingTaskRef = useRef<ReturnType<typeof getDocument> | null>(null)
  const [containerWidth, setContainerWidth] = useState(960)
  const [zoom, setZoom] = useState(1)
  const [pdfDocument, setPdfDocument] = useState<PDFDocumentProxy | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    const element = frameRef.current
    if (!element || typeof ResizeObserver === 'undefined') {
      return
    }

    const updateWidth = () => {
      setContainerWidth(Math.max(element.clientWidth - 32, 320))
    }

    updateWidth()
    const observer = new ResizeObserver(updateWidth)
    observer.observe(element)
    return () => observer.disconnect()
  }, [])

  useEffect(() => {
    let cancelled = false

    setLoading(true)
    setError(null)
    setPdfDocument(null)

    if (loadingTaskRef.current) {
      loadingTaskRef.current.destroy()
      loadingTaskRef.current = null
    }

    const loadingTask = getDocument(url)
    loadingTaskRef.current = loadingTask

    void loadingTask.promise
      .then((pdf) => {
        if (cancelled) {
          pdf.destroy()
          return
        }

        setPdfDocument(pdf)
        setLoading(false)
      })
      .catch((renderError) => {
        if (!cancelled) {
          setError(renderError instanceof Error ? renderError.message : 'Unable to render PDF preview.')
          setLoading(false)
        }
      })

    return () => {
      cancelled = true
      loadingTask.destroy()
      if (loadingTaskRef.current === loadingTask) {
        loadingTaskRef.current = null
      }
    }
  }, [url])

  useEffect(() => {
    const element = pagesRef.current
    if (!element || !pdfDocument) {
      return
    }

    let cancelled = false
    const canvases: HTMLCanvasElement[] = []
    element.innerHTML = ''

    const captureScrollProgress = () => {
      const viewport = pagesViewportRef.current
      if (!viewport) {
        return
      }

      const maxScrollTop = Math.max(0, viewport.scrollHeight - viewport.clientHeight)
      scrollProgressRef.current = maxScrollTop > 0 ? viewport.scrollTop / maxScrollTop : 0
    }

    const restoreScrollProgress = () => {
      const viewport = pagesViewportRef.current
      if (!viewport) {
        return
      }

      const maxScrollTop = Math.max(0, viewport.scrollHeight - viewport.clientHeight)
      viewport.scrollTop = maxScrollTop > 0 ? scrollProgressRef.current * maxScrollTop : 0
    }

    const render = async () => {
      try {
        for (let pageNumber = 1; pageNumber <= pdfDocument.numPages; pageNumber += 1) {
          if (cancelled) {
            return
          }

          const page = await pdfDocument.getPage(pageNumber)
          const baseViewport = page.getViewport({ scale: 1 })
          const fitScale = Math.max(0.75, Math.min(2, containerWidth / baseViewport.width))
          const scale = Math.max(0.4, Math.min(3, fitScale * zoom))
          const viewport = page.getViewport({ scale })
          const outputScale = window.devicePixelRatio || 1

          const canvas = document.createElement('canvas')
          const context = canvas.getContext('2d')
          if (!context) {
            throw new Error('Canvas context is unavailable.')
          }

          canvas.className = 'pdf-preview__page'
          canvas.width = Math.floor(viewport.width * outputScale)
          canvas.height = Math.floor(viewport.height * outputScale)
          canvas.style.width = `${Math.floor(viewport.width)}px`
          canvas.style.height = `${Math.floor(viewport.height)}px`
          context.setTransform(outputScale, 0, 0, outputScale, 0, 0)

          element.appendChild(canvas)
          canvases.push(canvas)

          await page.render({
            canvas,
            canvasContext: context,
            viewport,
          }).promise
        }

        if (!cancelled) {
          window.requestAnimationFrame(() => {
            if (!cancelled) {
              restoreScrollProgress()
            }
          })
        }
      } catch (renderError) {
        if (!cancelled) {
          setError(renderError instanceof Error ? renderError.message : 'Unable to render PDF preview.')
          setLoading(false)
        }
      }
    }

    void render()

    return () => {
      cancelled = true
      captureScrollProgress()
      canvases.forEach((canvas) => canvas.remove())
    }
  }, [containerWidth, pdfDocument, zoom])

  const zoomPercent = useMemo(() => `${Math.round(zoom * 100)}%`, [zoom])

  const clampZoom = (value: number) => Math.max(0.5, Math.min(2.5, value))

  const statusText = useMemo(() => {
    if (error) {
      return error
    }
    if (loading) {
      return 'Loading preview...'
    }
    return null
  }, [error, loading])

  return (
    <div ref={frameRef} className="pdf-preview" aria-label={fileName}>
      {statusText ? <p className={error ? 'error-banner' : 'pdf-preview__status'}>{statusText}</p> : null}
      <div ref={pagesViewportRef} className="pdf-preview__viewport">
        <div ref={pagesRef} className="pdf-preview__pages" />
      </div>
      <div className="pdf-preview__footer" role="toolbar" aria-label="PDF zoom controls">
        <button
          type="button"
          className="pdf-preview__zoom-button"
          onClick={() => setZoom((current) => clampZoom(current - 0.1))}
          aria-label="Zoom out"
          title="Zoom out"
        >
          -
        </button>
        <span className="pdf-preview__zoom-value" aria-live="polite">
          {zoomPercent}
        </span>
        <button
          type="button"
          className="pdf-preview__zoom-button"
          onClick={() => setZoom((current) => clampZoom(current + 0.1))}
          aria-label="Zoom in"
          title="Zoom in"
        >
          +
        </button>
        <button
          type="button"
          className="pdf-preview__zoom-reset"
          onClick={() => setZoom(1)}
          aria-label="Reset zoom"
          title="Reset zoom"
        >
          ↺
        </button>
      </div>
    </div>
  )
}
