import { useMemo } from 'react'
import { Plyr, type PlyrSource } from 'plyr-react'
import 'plyr-react/plyr.css'

interface VideoPreviewProps {
  fileName: string
  src: string
}

const controls = [
  'play-large',
  'play',
  'progress',
  'current-time',
  'mute',
  'volume',
  'settings',
  'pip',
  'fullscreen',
]

function inferMimeType(source: string) {
  const lower = source.toLowerCase()
  if (lower.endsWith('.webm')) {
    return 'video/webm'
  }
  if (lower.endsWith('.ogg') || lower.endsWith('.ogv')) {
    return 'video/ogg'
  }
  if (lower.endsWith('.mov')) {
    return 'video/quicktime'
  }
  return 'video/mp4'
}

export function VideoPreview({ fileName, src }: VideoPreviewProps) {
  const source = useMemo<PlyrSource>(
    () => ({
      type: 'video',
      sources: [{ src, type: inferMimeType(src) }],
    }),
    [src],
  )

  return (
    <section className="video-preview-shell" aria-label={fileName}>
      <div className="video-preview-stage">
        <Plyr
          source={source}
          options={{
            controls,
            invertTime: false,
            keyboard: { focused: true, global: true },
            settings: ['captions', 'quality', 'speed', 'loop'],
            resetOnEnd: false,
          }}
          playsInline
          preload="metadata"
        />
      </div>
    </section>
  )
}
