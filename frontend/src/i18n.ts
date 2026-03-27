import i18n from 'i18next'
import { initReactI18next } from 'react-i18next'

const resources = {
  en: {
    translation: {
      title: 'EasyPreviewer',
      language: 'Language',
      localeZh: '中文',
      localeEn: 'English',
      loadingAction: 'Loading...',
      sessionTitle: 'Preview',
      noSession: 'Choose a sample and open the preview.',
      unsupportedTitle: 'Unsupported',
      failedTitle: 'Preview Failed',
      readyTitle: 'Preview Ready',
      processingTitle: 'Processing',
      contentUnavailable: 'No content is available for this preview state.',
      capabilityPdf: 'PDF',
      capabilitySpreadsheet: 'Spreadsheet',
      capabilityImage: 'Image',
      capabilityText: 'Text',
      capabilityMarkdown: 'Markdown',
      capabilityUnsupported: 'Unsupported',
      statusReady: 'READY',
      statusProcessing: 'PROCESSING',
      statusUnsupported: 'UNSUPPORTED',
      statusFailed: 'FAILED',
    },
  },
  zh: {
    translation: {
      title: 'EasyPreviewer',
      language: '语言',
      localeZh: '中文',
      localeEn: 'English',
      loadingAction: '加载中...',
      sessionTitle: '预览',
      noSession: '请选择一个样例并打开预览。',
      unsupportedTitle: '不支持',
      failedTitle: '预览失败',
      readyTitle: '预览已就绪',
      processingTitle: '处理中',
      contentUnavailable: '当前状态下没有可显示的内容。',
      capabilityPdf: 'PDF',
      capabilitySpreadsheet: '表格',
      capabilityImage: '图片',
      capabilityText: '文本',
      capabilityMarkdown: 'Markdown',
      capabilityUnsupported: '不支持',
      statusReady: 'READY',
      statusProcessing: 'PROCESSING',
      statusUnsupported: 'UNSUPPORTED',
      statusFailed: 'FAILED',
    },
  },
}

i18n.use(initReactI18next).init({
  resources,
  lng: 'en',
  fallbackLng: 'en',
  interpolation: {
    escapeValue: false,
  },
})

export default i18n
