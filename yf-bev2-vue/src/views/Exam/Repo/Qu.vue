<script lang="ts" setup>
import { DataTable } from '@/components/DataTable'
import RepoSelect from '@/views/Exam/Repo/components/RepoSelect.vue'
import { DictListSelect } from '@/components/DictListSelect'
import QuFormDialog from '@/views/Exam/Repo/components/QuFormDialog.vue'
import { ContentWrap } from '@/components/ContentWrap'
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { OptionsType, TableQueryType } from '@/components/DataTable/src/types'
import { useRoute } from 'vue-router'
import { reviewApi, importApi } from '@/api/modules/exam/qu'
import request from '@/config/axios'

const quId = ref()

// 添加修改
const dialogVisible = ref(false)

// 表格查询参数
let query = ref<TableQueryType>({
  current: 1,
  size: 10,
  params: {
    repoId: null,
    quType: null,
    difficultyLevel: null,
    reviewStatus: null,
    content: ''
  }
})

// 获取参数
const route = useRoute()
query.value.params.repoId = route.query.repoId || ''

// 表格默认参数
let options = ref<OptionsType>({
  listUrl: '/api/exam/repo/qu/paging',
  delUrl: '/api/exam/repo/qu/delete',
  add: {
    enable: true,
    permission: ['repo:qu:add']
  },
  edit: {
    enable: true,
    permission: ['repo:qu:edit']
  },
  del: {
    enable: true,
    permission: ['repo:qu:delete']
  },
  batch: [{ key: 'approve', label: '一键审核通过（总护理长/管理员）' }]
})

const table = ref()

const handleAdd = () => {
  quId.value = ''
  dialogVisible.value = true
}

// 一键审核（总护理长/管理员批量置 APPROVED）
const handleBatch = async (res: any) => {
  if (res?.key !== 'approve') return
  const ids = res.ids || []
  if (!ids.length) {
    ElMessage.warning('请先勾选要审核的题目')
    return
  }
  try {
    await ElMessageBox.confirm(`确认一键审核通过 ${ids.length} 道题？（审核后学员练习即可抽到）`, '一键审核', {
      type: 'warning',
      confirmButtonText: '确认通过',
      cancelButtonText: '取消'
    })
    const r: any = await request.post({ url: '/api/exam/repo/qu/review-batch', data: { ids } })
    if (r.code === 0) {
      ElMessage.success(`已通过 ${ids.length} 道题`)
      table.value.reload()
    } else {
      ElMessage.error(r.msg || '一键审核失败')
    }
  } catch (e: any) {
    if (e !== 'cancel') {
      ElMessage.error('一键审核失败：' + (e?.message || ''))
    }
  }
}

const handleEdit = (row: any) => {
  quId.value = row.id
  dialogVisible.value = true
}

// 刷新列表
const handleRefresh = () => {
  table.value.reload()
}

// ===== 两级审核 =====
const STATUS_MAP: Record<string, { label: string; type: string }> = {
  DRAFT: { label: '草稿', type: 'info' },
  REVIEW2: { label: '待二审', type: 'warning' },
  APPROVED: { label: '已通过', type: 'success' },
  REJECTED: { label: '已驳回', type: 'danger' }
}
const statusLabel = (s?: string) => (s ? STATUS_MAP[s]?.label || s : '草稿')
const statusType = (s?: string) => ((STATUS_MAP[s!]?.type as any) || 'info')

const reviewVisible = ref(false)
const reviewRow = ref<any>({})
const reviewForm = reactive({ action: 'APPROVE', remark: '' })

// ===== 导入（Excel/Word） =====
const fileInput = ref()

const handleImportClick = () => {
  if (!query.value.params.repoId) {
    ElMessage.warning('请先在左侧选择一个题库再导入')
    return
  }
  fileInput.value.click()
}

// 下载 Excel 导入模板（GET，走 axios 以携带 token）
const downloadTemplate = async () => {
  try {
    const resp: any = await request.get({ url: '/api/exam/repo/qu/import-template', responseType: 'blob' })
    const url = window.URL.createObjectURL(resp)
    const a = document.createElement('a')
    a.href = url
    a.download = '题目导入模板.xlsx'
    a.click()
    window.URL.revokeObjectURL(url)
  } catch (err: any) {
    ElMessage.error('模板下载失败：' + (err?.message || '未知错误'))
  }
}

const handleFileChange = async (e: any) => {
  const f = e.target.files?.[0]
  if (!f) return
  if (!query.value.params.repoId) {
    ElMessage.warning('请先在左侧/搜索区选择一个题库，再导入')
    e.target.value = ''
    return
  }
  const fd = new FormData()
  fd.append('file', f)
  fd.append('repoId', query.value.params.repoId)
  try {
    const r = await importApi(fd)
    ElMessage.success(`导入完成：成功 ${r.data.success} 条，失败 ${r.data.fail} 条`)
    if (r.data.errors && r.data.errors.length) {
      console.warn('导入失败明细:', r.data.errors)
    }
    table.value.reload()
  } catch (err: any) {
    ElMessage.error('导入失败：' + (err?.message || '未知错误'))
  }
  e.target.value = ''
}

const openReview = (row: any) => {
  reviewRow.value = row
  reviewForm.action = row.reviewStatus === 'REJECTED' ? 'RESUBMIT' : 'APPROVE'
  reviewForm.remark = ''
  reviewVisible.value = true
}

const submitReview = async () => {
  const row = reviewRow.value
  if (reviewForm.action === 'RESUBMIT') {
    await reviewApi({ quId: row.id, action: 'RESUBMIT' })
  } else {
    const level = row.reviewStatus === 'REVIEW2' ? 2 : 1
    await reviewApi({ quId: row.id, level, action: reviewForm.action, remark: reviewForm.remark })
  }
  reviewVisible.value = false
  ElMessage.success('审核已提交')
  table.value.reload()
}

const reviewStatusOptions = [
  { value: 'DRAFT', label: '草稿' },
  { value: 'REVIEW2', label: '待二审' },
  { value: 'APPROVED', label: '已通过' },
  { value: 'REJECTED', label: '已驳回' }
]
</script>

<template>
  <ContentWrap>
    <DataTable
      ref="table"
      :options="options"
      :query="query"
      @on-add="handleAdd"
      @on-batch="handleBatch"
      @on-edit="handleEdit"
    >
      <template #search>
        <el-form inline class="qu-search-form">
          <el-form-item label="题库">
            <repo-select v-model="query.params.repoId" style="width: 190px" />
          </el-form-item>
          <el-form-item label="题型">
            <dict-list-select v-model="query.params.quType" dic-code="qu_type" title="全部题型" />
          </el-form-item>
          <el-form-item label="难度">
            <dict-list-select v-model="query.params.difficultyLevel" dic-code="qu_difficulty_level" title="全部难度" />
          </el-form-item>
          <el-form-item label="状态">
            <el-select
              v-model="query.params.reviewStatus"
              placeholder="全部状态"
              clearable
              style="width: 130px"
            >
              <el-option
                v-for="opt in reviewStatusOptions"
                :key="opt.value"
                :label="opt.label"
                :value="opt.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="关键词">
            <el-input v-model="query.params.content" placeholder="题干/选项" clearable style="width: 190px" />
          </el-form-item>
          <el-form-item>
            <el-button type="success" plain icon="Upload" @click="handleImportClick">导入</el-button>
            <el-button icon="Download" @click="downloadTemplate">模板</el-button>
          </el-form-item>
        </el-form>
        <input
          ref="fileInput"
          type="file"
          accept=".xls,.xlsx,.docx"
          style="display: none"
          @change="handleFileChange"
        />
      </template>

      <template #columns>
        <el-table-column type="selection" width="50px" />
        <el-table-column label="试题内容" prop="contentText" min-width="340" show-overflow-tooltip />
        <el-table-column label="所属题库" prop="repoId_dictText" min-width="120" show-overflow-tooltip />
        <el-table-column align="center" label="题型" prop="quType_dictText" width="90" />
        <el-table-column align="center" label="难度" prop="difficultyLevel_dictText" width="80" />
        <el-table-column align="center" label="审核状态" width="100">
          <template #default="scope">
            <el-tag :type="statusType(scope.row.reviewStatus)" disable-transitions>
              {{ statusLabel(scope.row.reviewStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column align="center" label="创建人" prop="createBy_dictText" width="100" />
        <el-table-column align="center" label="创建时间" prop="createTime" width="160" show-overflow-tooltip />
        <el-table-column align="center" label="操作" width="100" fixed="right">
          <template #default="scope">
            <el-button
              v-if="scope.row.reviewStatus !== 'APPROVED'"
              type="primary"
              link
              @click="openReview(scope.row)"
            >
              审核
            </el-button>
            <span v-else>—</span>
          </template>
        </el-table-column>
      </template>
    </DataTable>

    <!-- 两级审核弹窗 -->
    <el-dialog v-model="reviewVisible" title="题目两级审核" width="540px">
      <el-descriptions :column="1" border size="small">
        <el-descriptions-item label="当前状态">
          {{ statusLabel(reviewRow.reviewStatus) }}
        </el-descriptions-item>
        <el-descriptions-item label="一审人">
          {{ reviewRow.firstReviewer_dictText || '—' }}
        </el-descriptions-item>
        <el-descriptions-item label="二审人">
          {{ reviewRow.secondReviewer_dictText || '—' }}
        </el-descriptions-item>
        <el-descriptions-item label="审核意见">
          {{ reviewRow.reviewRemark || '—' }}
        </el-descriptions-item>
      </el-descriptions>

      <el-form :model="reviewForm" label-width="80px" style="margin-top: 18px">
        <el-form-item label="审核动作">
          <el-radio-group v-model="reviewForm.action">
            <el-radio value="APPROVE">通过</el-radio>
            <el-radio value="REJECT">驳回</el-radio>
            <el-radio v-if="reviewRow.reviewStatus === 'REJECTED'" value="RESUBMIT">
              重新提交
            </el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="审核意见">
          <el-input v-model="reviewForm.remark" type="textarea" :rows="3" placeholder="可填写审核意见" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="reviewVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReview">提交审核</el-button>
      </template>
    </el-dialog>

    <QuFormDialog
      v-model:visible="dialogVisible"
      :qu-id="quId"
      :repo-id="query.params.repoId"
      @saved="handleRefresh"
    />
  </ContentWrap>
</template>

<style scoped>
/* 搜索区排版（纯 CSS，避免 sass 依赖） */
.qu-search-form {
  display: flex;
  flex-wrap: wrap;
  gap: 0 12px;
  align-items: center;
  padding: 14px 4px 2px;
}
.qu-search-form :deep(.el-form-item) {
  margin-right: 0;
  margin-bottom: 12px;
}
.qu-search-form :deep(.el-form-item__label) {
  color: #606266;
  font-weight: 500;
}

/* 表格美化 */
:deep(.el-table) {
  --el-table-header-bg-color: #f5f7fa;
  --el-table-header-text-color: #303133;
  font-size: 13px;
}
:deep(.el-table th.el-table__cell) {
  font-weight: 600;
  height: 46px;
}
:deep(.el-table td.el-table__cell) {
  height: 44px;
}
:deep(.el-table .el-table__row:hover > td.el-table__cell) {
  background: #f5faff;
}
:deep(.el-table .el-table-column--selection .cell) {
  padding: 0 10px;
}
</style>

