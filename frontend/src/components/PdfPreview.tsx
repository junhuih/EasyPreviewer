import { useEffect, useMemo, useRef, useState } from 'react'
import { GlobalWorkerOptions, getDocument } from 'pdfjs-dist'
import workerSrc from 'pdfjs-dist/build/pdf.worker.min.mjs?url'

GlobalWorkerOptions.workerSrc = workerSrc

interface PdfPreviewProps {
  fileName: string
  url: string
}

export function PdfPreview({ fileName, url }: PdfPreviewProps) {
  const containerRef = useRef<HTMLDivElement | null>(null)
  const [containerWidth, setContainerWidth] = useState(960)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    const element = containerRef.current
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
    const element = containerRef.current
    if (!element) {
      return
    }

    let cancelled = false
    const canvases: HTMLCanvasElement[] = []
    setLoading(true)
    setError(null)
    element.innerHTML = ''

    const render = async () => {
      try {
        const loadingTask = getDocument(url)
        const pdf = await loadingTask.promise

        for (let pageNumber = 1; pageNumber <= pdf.numPages; pageNumber += 1) {
          if (cancelled) {
            return
          }

          const page = await pdf.getPage(pageNumber)
          const baseViewport = page.getViewport({ scale: 1 })
          const scale = Math.max(0.75, Math.min(2, containerWidth / baseViewport.width))
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
          setLoading(false)
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
      canvases.forEach((canvas) => canvas.remove())
    }
  }, [containerWidth, url])

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
    <div className="pdf-preview" aria-label={fileName}>
      {statusText ? <p className={error ? 'error-banner' : 'pdf-preview__status'}>{statusText}</p> : null}
      <div ref={containerRef} className="pdf-preview__pages" />
    </div>
  )
}
