<template>
  <div class="file-preview">
    <!-- PDF: 浏览器原生渲染 -->
    <iframe v-if="pdfUrl" :src="pdfUrl" class="preview-frame"></iframe>
    <!-- DOCX / PPTX: 纯前端渲染容器 -->
    <div v-else-if="rendering" ref="renderRef" class="render-box"></div>
    <div v-else class="loading-tip">{{ loadingMsg }}</div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted, onBeforeUnmount } from 'vue'
import { renderAsync } from 'docx-preview'
import { init } from 'pptx-preview'

const props = defineProps<{ fileUrl: string; fileType: string }>()

const pdfUrl = ref('')
const rendering = ref(false)
const loadingMsg = ref('加载中...')
const renderRef = ref<HTMLElement>()

let pdfBlobUrl = ''

const fetchBlob = async () => {
  const res = await fetch(props.fileUrl)
  if (!res.ok) throw new Error('文件加载失败')
  return await res.blob()
}

const load = async () => {
  const type = (props.fileType || '').toUpperCase()
  try {
    loadingMsg.value = '加载中...'
    if (type === 'PDF') {
      const blob = await fetchBlob()
      pdfBlobUrl = URL.createObjectURL(blob)
      pdfUrl.value = pdfBlobUrl
    } else if (type === 'DOCX' || type === 'DOC') {
      rendering.value = true
      loadingMsg.value = ''
      const blob = await fetchBlob()
      if (renderRef.value) await renderAsync(blob, renderRef.value)
    } else if (type === 'PPTX' || type === 'PPT') {
      rendering.value = true
      loadingMsg.value = ''
      const blob = await fetchBlob()
      const buffer = await blob.arrayBuffer()
      if (renderRef.value) {
        const previewer = init(renderRef.value, { mode: 'slide', width: 960, height: 540 })
        await previewer.preview(buffer)
      }
    } else {
      loadingMsg.value = '暂不支持预览，请下载查看'
    }
  } catch (e: any) {
    loadingMsg.value = '预览失败: ' + (e?.message || '未知错误')
    rendering.value = false
  }
}

watch(() => props.fileUrl, load)
onMounted(load)
onBeforeUnmount(() => {
  if (pdfBlobUrl) URL.revokeObjectURL(pdfBlobUrl)
})
</script>

<style scoped>
.file-preview {
  width: 100%;
  height: 100%;
  overflow: auto;
}
.preview-frame {
  width: 100%;
  height: 100%;
  border: 0;
}
.render-box {
  padding: 20px;
  background: #f5f5f5;
  min-height: 400px;
}
.loading-tip {
  padding: 60px;
  text-align: center;
  color: #999;
}
</style>

