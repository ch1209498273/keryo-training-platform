<template>
  <div style="padding: 20px">
    <h2 style="margin-bottom: 20px">培训管理</h2>

    <el-form :inline="true" :model="query" style="margin-bottom: 20px">
      <el-form-item label="标题">
        <el-input v-model="query.title" placeholder="搜索培训" clearable />
      </el-form-item>
      <el-form-item label="类型">
        <el-select v-model="query.fileType" clearable placeholder="全部">
          <el-option label="PPTX" value="PPTX" />
          <el-option label="DOCX" value="DOCX" />
          <el-option label="PDF" value="PDF" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="fetchList">搜索</el-button>
        <el-button @click="showUpload = true">上传培训</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="list" border stripe v-loading="loading">
      <el-table-column prop="title" label="标题" min-width="200" />
      <el-table-column prop="fileType" label="类型" width="80" />
      <el-table-column label="大小" width="100">
        <template #default="{ row }">{{ formatSize(row.fileSize) }}</template>
      </el-table-column>
      <el-table-column prop="deptCode" label="中心" width="120" />
      <el-table-column label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">
            {{ row.status === 1 ? '已发布' : '草稿' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="280" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="primary" @click="preview(row)">预览</el-button>
          <el-button size="small" type="success" plain @click="openAiQu(row)">AI出题</el-button>
          <el-button size="small" @click="openEdit(row)">编辑</el-button>
          <el-popconfirm title="确定删除？" @confirm="del(row.id)">
            <template #reference>
              <el-button size="small" type="danger">删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <!-- AI 出题弹窗：根据培训内容生成题目入库 DRAFT 走审核 -->
    <el-dialog v-model="showAiQu" title="AI 出题（根据培训内容）" width="520px">
      <el-form :model="aiForm" label-width="90px">
        <el-form-item label="培训内容">
          <el-input :model-value="aiForm.coursewareTitle" disabled />
        </el-form-item>
        <el-form-item label="主题/要点">
          <el-input v-model="aiForm.topic" type="textarea" :rows="3"
            placeholder="可留空（默认用培训标题），或补充培训要点让题目更贴合" />
        </el-form-item>
        <el-form-item label="题型组合">
          <div class="type-counts">
            <div class="type-count-head">
              <span class="head-item">题型</span>
              <span class="head-item">数量</span>
              <span class="head-item">每题分</span>
            </div>
            <div v-for="t in quTypeOptions" :key="t.type" class="type-count-item">
              <span class="type-name">{{ t.label }}</span>
              <el-input-number v-model="aiForm.typeCounts[t.type]" :min="0" :max="20" size="small" />
              <el-input-number v-model="aiForm.typeScores[t.type]" :min="0" :max="100" :step="0.5" size="small" />
            </div>
            <div class="type-total" :class="{ 'not-100': typeTotalScore !== 100 }">
              合计：{{ typeTotal }} 道 / {{ typeTotalScore }} 分
              <span v-if="typeTotalScore !== 100" class="not-100-tip">（不等于 100 分，请调整！）</span>
            </div>
          </div>
        </el-form-item>
        <el-form-item label="目标题库">
          <el-select v-model="aiForm.repoId" placeholder="选择题目入库的题库" style="width: 100%">
            <el-option v-for="r in repoList" :key="r.id" :label="r.title" :value="r.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAiQu = false">取消</el-button>
        <el-button :loading="aiLoading" type="primary" @click="aiGenerateQu">生成并入库（DRAFT待审核）</el-button>
      </template>
    </el-dialog>

    <el-pagination
      v-model:current-page="query.current"
      v-model:page-size="query.size"
      :total="total"
      layout="total, prev, pager, next"
      @current-change="fetchList"
      style="margin-top: 20px; justify-content: flex-end"
    />

    <el-dialog v-model="showEdit" title="编辑培训" width="420px">
      <el-form :model="editRow" label-width="80px">
        <el-form-item label="标题"><el-input v-model="editRow.title" /></el-form-item>
        <el-form-item label="所属中心"
          ><el-input v-model="editRow.deptCode" placeholder="如 KERYO_C01"
        /></el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="editRow.status">
            <el-radio :value="1">已发布</el-radio>
            <el-radio :value="0">草稿</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEdit = false">取消</el-button>
        <el-button type="primary" @click="saveEdit">保存</el-button>
      </template>
    </el-dialog>

    <!-- 预览弹窗 -->
    <el-dialog
      v-model="showPreview"
      :title="previewRow?.title || '培训预览'"
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
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { listApi, updateApi, deleteApi } from '@/api/training/index'
import FilePreview from './components/FilePreview.vue'
import request from '@/config/axios'

const query = reactive({ current: 1, size: 10, title: '', fileType: '' })
const list = ref<any[]>([])
const total = ref(0)
const loading = ref(false)
const showUpload = ref(false)
const showEdit = ref(false)
const showPreview = ref(false)
const editRow = ref<any>({})
const previewRow = ref<any>(null)

// ===== AI 出题（根据培训内容生成题目入库 DRAFT 走审核） =====
const showAiQu = ref(false)
const aiLoading = ref(false)
const repoList = ref<any[]>([])

// 题型选项（每种可指定数量，0 = 不出）
const quTypeOptions = [
  { type: 'radio', label: '单选题' },
  { type: 'multi', label: '多选题' },
  { type: 'judge', label: '判断题' },
  { type: 'fill', label: '填空题' },
  { type: 'essay', label: '简答题' }
]

const aiForm = reactive({
  coursewareTitle: '',
  topic: '',
  count: 5,
  repoId: '',
  typeCounts: { radio: 0, multi: 0, judge: 0, fill: 0, essay: 0 } as Record<string, number>,
  typeScores: { radio: 5, multi: 6, judge: 4, fill: 2, essay: 10 } as Record<string, number>
})

// 题型合计（道数）
const typeTotal = computed(() => Object.values(aiForm.typeCounts).reduce((a, b) => a + (b || 0), 0))

// 题型总分（Σ 数量×每题分）
const typeTotalScore = computed(() =>
  quTypeOptions.reduce((acc, t) => acc + (aiForm.typeCounts[t.type] || 0) * (aiForm.typeScores[t.type] || 0), 0)
)

const loadRepos = async () => {
  try {
    const r: any = await request.post({ url: '/api/exam/repo/repo/paging', data: { current: 1, size: 100 } })
    repoList.value = r.data?.records || []
  } catch {
    repoList.value = []
  }
}

const openAiQu = (row: any) => {
  aiForm.coursewareTitle = row.title || ''
  aiForm.topic = ''
  aiForm.count = 5
  // 默认组合：合计 100 分（单选10×5 + 多选5×6 + 判断5×4 = 50+30+20）
  aiForm.typeCounts = { radio: 10, multi: 5, judge: 5, fill: 0, essay: 0 }
  aiForm.typeScores = { radio: 5, multi: 6, judge: 4, fill: 2, essay: 10 }
  aiForm.repoId = ''
  showAiQu.value = true
}

const aiGenerateQu = async () => {
  if (!aiForm.repoId) {
    ElMessage.warning('请选择目标题库')
    return
  }
  // 校验：至少选一种题型且数量>0
  const counts = Object.entries(aiForm.typeCounts)
    .filter(([, n]) => (n || 0) > 0)
    .map(([t, n]) => ({ type: t, count: n as number }))
  if (counts.length === 0) {
    ElMessage.warning('请至少为一种题型设置数量（>0）')
    return
  }
  // 校验：合计必须等于 100 分
  if (typeTotalScore.value !== 100) {
    ElMessage.error(`当前合计 ${typeTotalScore.value} 分，不等于 100 分满分，请调整题型数量或分值后再生成！`)
    return
  }
  aiLoading.value = true
  try {
    const r: any = await request.post({
      url: '/api/exam/repo/qu/ai/generate-from-courseware',
      data: {
        repoId: aiForm.repoId,
        coursewareTitle: aiForm.coursewareTitle,
        topic: aiForm.topic?.trim() || aiForm.coursewareTitle,
        count: typeTotal.value,
        quTypes: '单选、判断、填空、简答混合',
        // 精确数量+分值组合（后端优先按此生成，题目入库时带默认分值）
        typeCounts: counts.reduce((acc, c) => ({ ...acc, [c.type]: c.count }), {}),
        typeScores: counts.reduce((acc, c) => ({ ...acc, [c.type]: aiForm.typeScores[c.type] }), {})
      }
    })
    if (r.code === 0 && r.data) {
      ElMessage.success(`AI 出题完成：成功 ${r.data.success} 题，失败 ${r.data.fail} 题（已入 DRAFT，待两级审核）`)
      showAiQu.value = false
    } else {
      ElMessage.error(r.msg || 'AI 出题失败')
    }
  } catch (e: any) {
    ElMessage.error('AI 出题失败：' + (e?.message || '请稍后重试'))
  } finally {
    aiLoading.value = false
  }
}

const formatSize = (b?: number) =>
  !b ? '-' : b > 1048576 ? (b / 1048576).toFixed(1) + ' MB' : (b / 1024).toFixed(1) + ' KB'

const fetchList = async () => {
  loading.value = true
  try {
    const res: any = await listApi({
      current: query.current,
      size: query.size,
      params: {
        title: query.title || undefined,
        fileType: query.fileType || undefined
      }
    })
    const page = res.data || res
    list.value = page.records || []
    total.value = page.total || 0
  } catch (e: any) {
    ElMessage.error('加载失败: ' + (e?.message || ''))
    list.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

const preview = (row: any) => {
  previewRow.value = row
  showPreview.value = true
}

const del = async (id: string) => {
  try {
    await deleteApi(id)
    fetchList()
    ElMessage.success('已删除')
  } catch (e: any) {
    ElMessage.error('删除失败')
  }
}

const openEdit = (row: any) => {
  editRow.value = row
  showEdit.value = true
}

const saveEdit = async () => {
  try {
    await updateApi(editRow.value.id, {
      title: editRow.value.title,
      deptCode: editRow.value.deptCode,
      status: editRow.value.status
    })
    showEdit.value = false
    fetchList()
    ElMessage.success('已保存')
  } catch (e: any) {
    ElMessage.error('保存失败')
  }
}

onMounted(() => {
  fetchList()
  loadRepos()
})
</script>

<style scoped>
.type-counts {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.type-count-head {
  display: grid;
  grid-template-columns: 90px 110px 110px;
  gap: 6px;
  font-size: 12px;
  color: #999;
}

.type-count-item {
  display: grid;
  grid-template-columns: 90px 110px 110px;
  gap: 6px;
  align-items: center;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  padding: 4px 10px;
  background: #fafafa;
}

.type-name {
  font-size: 13px;
  color: #606266;
  white-space: nowrap;
}

.type-unit {
  font-size: 12px;
  color: #999;
}

.type-total {
  width: 100%;
  font-size: 13px;
  color: #2d8cf0;
  font-weight: bold;
}

.type-total.not-100 {
  color: #ff4949;
}

.not-100-tip {
  color: #ff4949;
  font-weight: normal;
}
</style>

