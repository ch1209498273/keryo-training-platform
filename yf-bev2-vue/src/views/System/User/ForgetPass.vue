<template>
  <ContentWrap>
    <DataTable ref="table" :options="options" :query="query">
      <template #search>
        <el-input
          v-model="query.params.userName"
          class="filter-item !w-180px"
          clearable
          placeholder="搜索用户名"
        />
        <el-select v-model="query.params.status" class="filter-item !w-120px" clearable placeholder="状态">
          <el-option label="待处理" :value="0" />
          <el-option label="已处理" :value="1" />
        </el-select>
      </template>

      <template #columns>
        <el-table-column label="用户名" prop="userName" min-width="120" />
        <el-table-column label="姓名" prop="realName" min-width="100" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'warning'">
              {{ row.status === 1 ? '已处理' : '待处理' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="申请时间" prop="createTime" width="170" />
        <el-table-column label="处理备注" prop="remark" min-width="140">
          <template #default="{ row }">{{ row.remark || '-' }}</template>
        </el-table-column>
        <el-table-column label="处理时间" width="170">
          <template #default="{ row }">{{ row.handleTime || '-' }}</template>
        </el-table-column>
        <el-table-column :align="'center'" label="操作" width="120">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 0"
              type="primary"
              size="small"
              @click="handle(row)"
            >
              标记已处理
            </el-button>
            <span v-else>-</span>
          </template>
        </el-table-column>
      </template>
    </DataTable>
  </ContentWrap>
</template>

<script lang="ts" setup>
import { ContentWrap } from '@/components/ContentWrap'
import { DataTable } from '@/components/DataTable'
import { ref } from 'vue'
import type { OptionsType, TableQueryType } from '@/components/DataTable/src/types'
import { ElMessage, ElMessageBox } from 'element-plus'
import { forgetPassHandleApi } from '@/api/sys/user'

// 表格查询参数
let query = ref<TableQueryType>({
  current: 1,
  size: 10,
  params: {
    userName: '',
    status: undefined
  }
})

// 表格默认参数
let options = ref<OptionsType>({
  listUrl: '/api/keryo/forget-pass/paging'
})

const table = ref()

// 标记已处理
const handle = async (row: any) => {
  const remark = await ElMessageBox.prompt('请填写处理备注（如：已重置密码并通知本人）', '标记已处理', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    inputPlaceholder: '处理备注（可选）'
  }).catch(() => null)
  if (remark === null) return

  try {
    await forgetPassHandleApi({ id: row.id, remark: remark.value })
    ElMessage.success('已标记处理')
    table.value.reload()
  } catch {
    // 错误提示由请求拦截器统一弹出
  }
}
</script>

