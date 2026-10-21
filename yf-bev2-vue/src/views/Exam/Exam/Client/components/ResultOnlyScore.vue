<template>
  <ContentWrap>
    <!-- T-15：成绩未公示时，不显示分数/通过结果 -->
    <el-result
      v-if="detail.userScore == null"
      icon="info"
      title="交卷成功"
      sub-title="成绩待管理员公布，请耐心等待。"
    >
      <template #extra>
        <el-button size="large" type="primary" @click="backExamList">返回考试列表</el-button>
      </template>
    </el-result>
    <el-result
      v-else
      :icon="detail.passed ? 'success' : 'warning'"
      :title="detail.passed ? '恭喜，考试通过了哟！' : '很遗憾，本次考试未及格！'"
    >
      <template #extra>
        <div class="score-show">{{ detail.userScore }}分</div>
        <el-button size="large" type="primary" @click="backExamList">返回考试列表</el-button>
      </template>
    </el-result>

    <!-- T-05：考试行为分析报告（切屏/IP/答题过快） -->
    <el-card v-if="cheatSummary.total > 0" class="!mt-20px">
      <template #header>
        <div class="card-header">
          <span>考试行为分析</span>
          <el-tag :type="cheatLevel.type" size="small">{{ cheatLevel.label }}</el-tag>
        </div>
      </template>
      <el-alert
        :closable="false"
        :title="cheatLevel.tip"
        show-icon
        :type="cheatLevel.type"
        class="!mb-15px"
      />
      <el-descriptions :column="3" border>
        <el-descriptions-item label="切屏次数">
          {{ cheatSummary.cutScreen }} 次
        </el-descriptions-item>
        <el-descriptions-item label="IP 变更">
          {{ cheatSummary.ipChange }} 次
        </el-descriptions-item>
        <el-descriptions-item label="答题过快">
          {{ cheatSummary.fastAnswer }} 次
        </el-descriptions-item>
      </el-descriptions>
    </el-card>
  </ContentWrap>
</template>

<script lang="ts" setup>
import { ContentWrap } from '@/components/ContentWrap'
import { computed, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { cheatListMineApi, paperDetailApi } from '@/api/modules/exam/paper'

const { push } = useRouter()

const props = withDefaults(defineProps<{ paperId?: string }>(), { paperId: '' })

const detail = ref<Record<string, any>>({})
const cheatLogs = ref<any[]>([])

// 违规汇总
const cheatSummary = computed(() => {
  const logs = cheatLogs.value || []
  return {
    total: logs.length,
    cutScreen: logs.filter((l) => l.cheatType === 'CUT_SCREEN').length,
    ipChange: logs.filter((l) => l.cheatType === 'IP_CHANGE').length,
    fastAnswer: logs.filter((l) => l.cheatType === 'FAST_ANSWER').length
  }
})

// 切屏等级：≥5 标红、≥3 标黄
const cheatLevel = computed(() => {
  const n = cheatSummary.value.cutScreen
  if (n >= 5) {
    return { type: 'error', label: '严重', tip: '考试期间多次离开页面，成绩将标记为异常，请留意！' }
  }
  if (n >= 3) {
    return { type: 'warning', label: '警示', tip: '考试期间有多次切屏记录，请注意考试纪律。' }
  }
  if (cheatSummary.value.total > 0) {
    return { type: 'info', label: '提示', tip: '考试期间有轻微异常行为记录。' }
  }
  return { type: 'success', label: '正常', tip: '考试行为正常。' }
})

// 查找详情
const paperDetail = (id: string) => {
  paperDetailApi({ id }).then((res) => {
    detail.value = res.data
  })
  // 学员端：查询自己的违规记录
  cheatListMineApi({ paperId: id }).then((res) => {
    cheatLogs.value = res.data || []
  })
}

// 返回考试列表
const backExamList = () => {
  push({ name: 'ClientExamList' })
}

watch(
  () => props.paperId,
  (val) => {
    if (val) {
      paperDetail(val)
    }
  },
  { immediate: true }
)
</script>

<style lang="less" scoped>
.score-show {
  font-size: 32px;
  font-weight: bold;
  color: #ff4949;
  padding-bottom: 30px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>

