export function saveBlob(blob, filename) {
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = filename
  document.body.appendChild(link)
  link.click()
  link.remove()
  URL.revokeObjectURL(url)
}
export function normalizeAudioUrl(data, taskId) {
  if (data?.audioUrl) return data.audioUrl
  if (data?.audioOutputPath) return data.audioOutputPath
  if (taskId) return `/api/tts/download/${taskId}`
  return ''
}
export function asArray(value) {
  if (Array.isArray(value)) return value
  if (Array.isArray(value?.records)) return value.records
  if (Array.isArray(value?.list)) return value.list
  if (Array.isArray(value?.items)) return value.items
  return []
}
