export type PreviewStatus = 'READY' | 'PROCESSING' | 'UNSUPPORTED' | 'FAILED'
export type PreviewMode = 'PDF' | 'SPREADSHEET' | 'IMAGE' | 'VIDEO' | 'TEXT' | 'MARKDOWN' | 'UNSUPPORTED'

export interface PreviewSessionResponse {
  id: string
  fileName: string
  extension: string
  locale: string
  status: PreviewStatus
  previewMode: PreviewMode
  supported: boolean
  conversionRequired: boolean
  message: string
  contentUrl: string
}
