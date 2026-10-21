<template>
  <ContentWrap>
    <DataTable ref="table" :options="options" :query="query">
      <template #search>
        <el-input
          v-model="query.params.userName"
          class="filter-item"
          clearable
          placeholder="学员姓名/账号"
        />
        <DictListSelect
          v-model="query.params.passed"
          class="filter-item"
          dicCode="yes_no"
          title="是否通过"
        />

        <div class="filter-item !w-250px !flex !items-center">
          <div class="w-60px text-14px">分数段：</div>
          <el-input-number
            v-model="query.params.scoreMin"
            :controls="false"
            clearable
            class="!w-60px"
          />
          <span>&nbsp;到&nbsp;</span>
          <el-input-number
            v-model="query.params.scoreMax"
            :controls="false"
            clearable
            class="!w-60px"
          />
        </div>
        <el-button class="filter-item" type="success" plain :loading="exportLoading" @click="handleExport">
          导出 Excel
        </el-button>
      </template>

      <template #columns>
        <el-table-column type="selection" width="50px" />
        <el-table-column label="考生姓名" prop="userId_dictText" />
        <el-table-column align="center" label="考试次数" prop="tryCount" />
        <el-table-column align="center" label="最高分数" prop="maxScore" />
        <el-table-column align="center" label="最近分数" prop="lastScore" />
        <el-table-column align="center" label="是否通过">
          <template #default="{ row }">
            <span v-if="row.passed" class="!text-[#67c23a]">通过</span>
            <span v-else class="!text-[#f56c6c]">未通过</span>
          </template>
        </el-table-column>
        <el-table-column :align="'center'" label="操作" width="260px">
          <template #default="{ row }">
            <el-button icon="DocumentCopy" type="primary" @click="toRecord(row.userId)"
              >考试明细
            </el-button>
            <el-button icon="Refresh" type="warning" plain @click="retryExam(row)"
              >补考
            </el-button>
          </template>
        </el-table-column>
      </template>
    </DataTable>

    <PaperListDialog v-model:visible="dialogVisible" :exam-id="examId" :user-id="userId" />
  </ContentWrap>
</template>

<script lang="ts" setup>
import { ContentWrap } from '@/components/ContentWrap'
import { DataTable } from '@/components/DataTable'
import { onActivated, ref } from 'vue'
import type { OptionsType, TableQueryType } from '@/components/DataTable/src/types'
import { useRoute } from 'vue-router'
import { DictListSelect } from '@/components/DictListSelect'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/config/axios'
import PaperListDialog from '@/views/Exam/Exam/components/PaperListDialog.vue'

// 获取参数
const route = useRoute()

const examId = typeof route.query.id === 'string' ? route.query.id : ''

// 表格查询参数
let query = ref<TableQueryType>({
  current: 1,
  size: 10,
  params: {
    examId: examId,
    title: '',
    scoreMin: null,
    scoreMax: null
  }
})

// 表格默认参数
let options = ref<OptionsType>({
  listUrl: '/api/exam/exam/record/paging'
})

const table = ref()

const userId = ref<string>()
const dialogVisible = ref(false)
const exportLoading = ref(false)

const toRecord = (id: string) => {
  userId.value = id
  dialogVisible.value = true
}

// 成绩导出 Excel（T-06.2）
const handleExport = async () => {
  exportLoading.value = true
  try {
    const res: any = await request.post({
      url: '/api/exam/exam/record/export',
      data: {
        current: 1,
        size: 10000,
        params: query.value.params
      },
      responseType: 'blob'
    })
    // 触发下载
    const blob = new Blob([res], {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
    })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `成绩导出_${new Date().getTime()}.xlsx`
    a.click()
    URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch (e: any) {
    ElMessage.error('导出失败：' + (e?.message || '请重试'))
  } finally {
    exportLoading.value = false
  }
}

onActivated(() => {
  // 刷新表格
  table.value.reload()
})

// 补考（管理端发起）：重置该学员已考次数，允许再次参加考试
const retryExam = async (row: any) => {
  await ElMessageBox.confirm(
    `确认对该学员「${row.userId_dictText || row.userId}」发起补考？\n重置后可再次参加本次考试，原成绩记录保留。`,
    '补考确认',
    { type: 'warning' }
  )
  const r: any = await request.post({
    url: '/api/exam/exam/record/retry',
    data: { examId: examId, userId: row.userId }
  })
  if (r.code === 0) {
    ElMessage.success('补考已发起，该学员可再次参加考试')
    table.value.reload()
  }
}
</script>

