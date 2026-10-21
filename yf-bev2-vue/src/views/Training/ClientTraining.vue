<template>
  <div style="padding: 20px">
    <h2 style="margin-bottom: 20px">培训</h2>

    <!-- 搜索 -->
    <el-form :inline="true" :model="query" style="margin-bottom: 20px">
      <el-form-item label="关键词">
        <el-input v-model="query.title" placeholder="搜索培训标题" clearable style="width: 220px" />
      </el-form-item>
      <el-form-item label="类型">
        <el-select v-model="query.fileType" clearable placeholder="全部" style="width: 120px">
          <el-option label="PPTX" value="PPTX" />
          <el-option label="DOCX" value="DOCX" />
          <el-option label="PDF" value="PDF" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="fetchList">搜索</el-button>
      </el-form-item>
    </el-form>

    <!-- 卡片列表（只显示已发布） -->
    <div v-loading="loading" style="min-height: 300px">
      <el-row :gutter="20">
        <el-col
          v-for="cw in list"
          :key="cw.id"
          :xs="24"
          :sm="12"
          :md="8"
          :lg="6"
          style="margin-bottom: 20px"
        >
          <el-card shadow="hover" style="cursor: pointer" @click="openPreview(cw)">
            <div style="display: flex; align-items: center; gap: 10px">
              <div
                :style="iconStyle(cw.fileType)"
                style="
                  width: 48px;
                  height: 48px;
                  border-radius: 8px;
                  display: flex;
                  align-items: center;
                  justify-content: center;
                  font-size: 12px;
                  color: #fff;
                  font-weight: bold;
                  flex-shrink: 0;
                "
              >
                {{ cw.fileType }}
              </div>
              <div style="min-width: 0">
                <div
                  style="
                    font-weight: 600;
                    overflow: hidden;
                    text-overflow: ellipsis;
                    white-space: nowrap;
                  "
                  >{{ cw.title }}</div
                >
                <div style="font-size: 12px; color: #999; margin-top: 4px"
                  >{{ formatSize(cw.fileSize) }} · {{ cw.createTime?.slice(0, 10) }}</div
                >
              </div>
            </div>
            <div
              v-if="cw.examId"
              style="display: flex; align-items: center; justify-content: space-between; margin-top: 12px"
              @click.stop
            >
              <el-tag size="small" type="warning">含关联考试</el-tag>
              <el-button link type="primary" @click.stop="openExam(cw.examId)">去考试</el-button>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <el-empty v-if="!loading && list.length === 0" description="暂无培训内容" />
    </div>

    <el-pagination
      v-model:current-page="query.current"
      v-model:page-size="query.size"
      :total="total"
      layout="total, prev, pager, next"
      @current-change="fetchList"
      style="justify-content: flex-end"
    />

    <!-- 预览弹窗 -->
    <el-dialog
      v-model="showPreview"
      :title="previewTitle"
      width="85%"
      top="5vh"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <div style="height: 80vh">
        <FilePreview
          v-if="previewRow"
          :file-url="previewRow.fileUrl"
          :file-type="previewRow.fileType"
        />
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/config/axios'
import FilePreview from './components/FilePreview.vue'
import { useRouter } from 'vue-router'

const query = reactive({ current: 1, size: 12, title: '', fileType: '' })
const list = ref<any[]>([])
const total = ref(0)
const loading = ref(false)

const showPreview = ref(false)
const previewTitle = ref('')
const previewRow = ref<any>(null)
const router = useRouter()

const iconStyle = (t: string) => {
  const map: Record<string, string> = {
    PPTX: '#e67326',
    PPT: '#e67326',
    DOCX: '#2b6fb3',
    DOC: '#2b6fb3',
    PDF: '#d93025'
  }
  return { background: map[t] || '#999' }
}

const formatSize = (b?: number) =>
  !b ? '-' : b > 1048576 ? (b / 1048576).toFixed(1) + ' MB' : (b / 1024).toFixed(1) + ' KB'

const fetchList = async () => {
  loading.value = true
  try {
    // T-13b：学员端只返回参与计划关联的培训内容
    const res: any = await request.post({ url: '/api/keryo/training/my', data: {} })
    const rows = res.data || []
    list.value = query.title
      ? rows.filter((r: any) => (r.title || '').includes(query.title))
      : rows
    total.value = list.value.length
  } catch (e: any) {
    ElMessage.error('加载失败: ' + (e?.message || ''))
    list.value = []
  } finally {
    loading.value = false
  }
}

const openPreview = (cw: any) => {
  previewTitle.value = cw.title
  previewRow.value = cw
  showPreview.value = true
}

const openExam = (examId: string) => {
  router.push({ name: 'ExamClientDetail', query: { id: examId } })
}

onMounted(fetchList)
</script>

