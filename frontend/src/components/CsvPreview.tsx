import { useEffect, useState } from 'react'

interface CsvPreviewProps {
  url: string
}

function parseCsv(text: string): string[][] {
  const rows: string[][] = []
  let row: string[] = []
  let value = ''
  let inQuotes = false

  for (let index = 0; index < text.length; index += 1) {
    const char = text[index]
    const next = text[index + 1]

    if (char === '"') {
      if (inQuotes && next === '"') {
        value += '"'
        index += 1
      } else {
        inQuotes = !inQuotes
      }
      continue
    }

    if (char === ',' && !inQuotes) {
      row.push(value)
      value = ''
      continue
    }

    if ((char === '\n' || char === '\r') && !inQuotes) {
      if (char === '\r' && next === '\n') {
        index += 1
      }
      row.push(value)
      rows.push(row)
      row = []
      value = ''
      continue
    }

    value += char
  }

  if (value.length > 0 || row.length > 0) {
    row.push(value)
    rows.push(row)
  }

  return rows.filter((currentRow) => currentRow.some((cell) => cell.length > 0))
}

export function CsvPreview({ url }: CsvPreviewProps) {
  const [rows, setRows] = useState<string[][]>([])
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    let cancelled = false
    fetch(url)
      .then(async (response) => {
        if (!response.ok) {
          throw new Error('Unable to load CSV preview.')
        }
        const text = await response.text()
        if (!cancelled) {
          setRows(parseCsv(text))
          setError(null)
        }
      })
      .catch((fetchError) => {
        if (!cancelled) {
          setError(fetchError instanceof Error ? fetchError.message : 'Unable to load CSV preview.')
        }
      })

    return () => {
      cancelled = true
    }
  }, [url])

  if (error) {
    return <p className="error-banner">{error}</p>
  }

  if (!rows.length) {
    return <p className="empty-state">Loading preview...</p>
  }

  const [header, ...body] = rows

  return (
    <div className="csv-preview">
      <table className="csv-preview__table">
        <thead>
          <tr>
            {header.map((cell, index) => (
              <th key={`header-${index}`}>{cell}</th>
            ))}
          </tr>
        </thead>
        <tbody>
          {body.map((row, rowIndex) => (
            <tr key={`row-${rowIndex}`}>
              {row.map((cell, cellIndex) => (
                <td key={`cell-${rowIndex}-${cellIndex}`}>{cell}</td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}
