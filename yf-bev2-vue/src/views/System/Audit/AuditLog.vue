<template>
  <ContentWrap>
    <div class="mb-15px flex items-center gap-10px flex-wrap">
      <span class="text-14px text-gray-500">数据保留：仅复制归档，不删除源数据</span>
      <el-button :loading="retentionLoading" @click="previewRetention">查看待归档量</el-button>
      <el-button type="warning" :loading="retentionLoading" @click="archiveRetention">
        执行非破坏归档
      </el-button>
      <el-tag v-if="retentionSummary" type="info">{{ retentionSummary }}</el-tag>
    </div>
    <div class="mb-15px flex items-center gap-10px flex-wrap">
      <el-input
        v-model="query.params.userName"
        clearable
        class="!w-180px"
        placeholder="操作账号"
        @keyup.enter="search"
      />
      <el-input
        v-model="query.params.realName"
        clearable
        class="!w-150px"
        placeholder="操作人姓名"
        @keyup.enter="search"
      />
      <el-input
        v-model="query.params.module"
        clearable
        class="!w-150px"
        placeholder="业务模块"
        @keyup.enter="search"
      />
      <el-select v-model="query.params.result" clearable class="!w-120px" placeholder="结果">
        <el-option label="成功" value="SUCCESS" />
        <el-option label="失败" value="FAIL" />
      </el-select>
      <el-date-picker
        v-model="timeRange"
        type="datetimerange"
        value-format="YYYY-MM-DD HH:mm:ss"
        start-placeholder="开始时间"
        end-placeholder="结束时间"
        class="!w-360px"
        range-separator="至"
      />
      <el-button type="primary" @click="search">查询</el-button>
      <el-button @click="reset">重置</el-button>
    </div>

    <el-table v-loading="loading" :data="list" border>
      <el-table-column label="时间" prop="createTime" width="175" />
      <el-table-column label="操作账号" prop="userName" width="120" />
      <el-table-column label="操作人" prop="realName" width="110" />
      <el-table-column label="模块" prop="module" width="140" />
      <el-table-column label="操作" prop="operation" min-width="170" show-overflow-tooltip />
      <el-table-column label="请求" min-width="220" show-overflow-tooltip>
        <template #default="{ row }">{{ row.method }} {{ row.url }}</template>
      </el-table-column>
      <el-table-column label="结果" width="80" align="center">
        <template #default="{ row }">
          <el-tag :type="row.result === 'SUCCESS' ? 'success' : 'danger'">
            {{ row.result === 'SUCCESS' ? '成功' : '失败' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="耗时(ms)" prop="costTime" width="95" align="right" />
      <el-table-column label="参数/错误" min-width="220" show-overflow-tooltip>
        <template #default="{ row }">{{ row.errorMsg || row.params || '-' }}</template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-model:current-page="query.current"
      v-model:page-size="query.size"
      :total="total"
      layout="total, sizes, prev, pager, next"
      :page-sizes="[10, 20, 50]"
      class="mt-15px justify-end"
      @current-change="fetchList"
      @size-change="fetchList"
    />
  </ContentWrap>
</template>

<script lang="ts" setup>
import { ContentWrap } from '@/components/ContentWrap'
import request from '@/config/axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import { onMounted, reactive, ref } from 'vue'

const loading = ref(false)
const list = ref<any[]>([])
const total = ref(0)
const timeRange = ref<[string, string] | undefined>(undefined)
const retentionLoading = ref(false)
const retentionSummary = ref('')

const query = reactive({
  current: 1,
  size: 10,
  params: {
    userName: '',
    realName: '',
    module: '',
    result: '' as string | undefined,
    createTimeStart: undefined as string | undefined,
    createTimeEnd: undefined as string | undefined
  }
})

const fetchList = async () => {
  loading.value = true
  try {
    query.params.createTimeStart = timeRange.value?.[0]
    query.params.createTimeEnd = timeRange.value?.[1]
    const response: any = await request.post({
      url: '/api/keryo/audit-log/paging',
      data: query
    })
    list.value = response.data?.records || []
    total.value = response.data?.total || 0
  } finally {
    loading.value = false
  }
}

const search = () => {
  query.current = 1
  fetchList()
}

const reset = () => {
  query.current = 1
  query.params.userName = ''
  query.params.realName = ''
  query.params.module = ''
  query.params.result = undefined
  timeRange.value = undefined
  fetchList()
}

const formatRetention = (data: Record<string, number>) => {
  const labels: Record<string, string> = {
    auditLog: '审计',
    examRecord: '成绩',
    paper: '试卷',
    paperQuestion: '试卷题目',
    paperAnswer: '答案',
    cheatLog: '违规日志'
  }
  return Object.entries(data || {})
    .map(([key, value]) => `${labels[key] || key} ${value}`)
    .join('，')
}

const previewRetention = async () => {
  retentionLoading.value = true
  try {
    const response: any = await request.post({ url: '/api/keryo/retention/preview', data: {} })
    retentionSummary.value = `可归档：${formatRetention(response.data || {})}`
  } finally {
    retentionLoading.value = false
  }
}

const archiveRetention = async () => {
  await ElMessageBox.confirm(
    '将复制超过保留期的审计、成绩和试卷数据到归档表，源数据不会删除。是否继续？',
    '确认非破坏归档',
    { type: 'warning', confirmButtonText: '继续', cancelButtonText: '取消' }
  )
  retentionLoading.value = true
  try {
    const response: any = await request.post({ url: '/api/keryo/retention/archive', data: {} })
    retentionSummary.value = `本次归档：${formatRetention(response.data || {})}`
    ElMessage.success('归档完成，源数据未删除')
  } finally {
    retentionLoading.value = false
  }
}

onMounted(fetchList)
</script>

