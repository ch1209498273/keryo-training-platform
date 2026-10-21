<template>
  <ContentWrap>
    <h2 class="text-center">{{ paperData.title }}</h2>
    <p class="text-center !text-[#666]">{{ paperData.createTime }}</p>

    <el-row :gutter="24" class="!mt-50px">
      <el-col :span="8" class="text-center"> 考生姓名：{{ paperData.userId_dictText }} </el-col>
      <el-col :span="8" class="text-center"> 考试用时：{{ paperData.userTime }}分钟 </el-col>
      <el-col :span="8" class="text-center"> 考试得分：{{ paperData.userScore }} </el-col>
    </el-row>

    <!-- T-05：考试行为分析报告 -->
    <el-card v-if="cheatSummary.total > 0" class="!mt-20px">
      <template #header>
        <div class="card-header">
          <span>考试行为分析</span>
          <el-tag :type="cheatLevel.type" size="small">{{ cheatLevel.label }}</el-tag>
        </div>
      </template>
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
      <el-table v-if="cheatLogs.length > 0" :data="cheatLogs" class="!mt-15px" size="small">
        <el-table-column label="时间" prop="createTime" width="170" />
        <el-table-column label="类型" width="110">
          <template #default="{ row }">
            <el-tag v-if="row.cheatType === 'CUT_SCREEN'" size="small" type="warning">切屏</el-tag>
            <el-tag v-else-if="row.cheatType === 'IP_CHANGE'" size="small" type="danger">IP变更</el-tag>
            <el-tag v-else size="small" type="info">答题过快</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="详情" prop="content" show-overflow-tooltip />
        <el-table-column label="IP" prop="ip" width="140" />
      </el-table>
    </el-card>

    <el-card class="!mt-20px">
      <div v-for="item in paperData.quList" :key="item.id" class="qu-content">
        <div class="flex pt-10px pb-10px">
          <div>{{ item.sort }}.</div>
          <div v-html="sanitizeHtml(item.content)" class="content-p"></div>
          <div class="score-text">（得分：{{ item.actualScore }}）</div>
        </div>

        <p v-if="item.image != null && item.image != ''">
          <el-image :src="item.image" class="!max-w-full" />
        </p>

        <div v-if="item.quType === 'radio' || item.quType === 'judge' || item.quType === 'multi'">
          <div
            v-for="an in item.answerList"
            :key="an.id"
            :class="['answer-item', { 'is-checked': an.checked, 'is-right': an.isRight }]"
          >
            <div class="answer-row">
              <span>{{ an.abc }}.{{ an.content }}</span>
              <span v-if="an.isRight" class="right-mark"
                ><el-icon><Check /></el-icon
              ></span>
            </div>
            <div v-if="an.image != null && an.image != ''" class="!clear-both">
              <el-image :src="an.image" class="!max-w-full" />
            </div>
          </div>

          <div class="flex justify-between">
            <div class="!text-[#689ef9]">
              <div v-if="!item.answered"> 我的回答：未答 </div>
              <div v-else>
                我的回答：<template v-for="an in item.answerList">
                  <template v-if="an.checked">{{ an.abc }}</template>
                </template>
              </div>
            </div>
            <div :class="{ 'a-wrong': !item.isRight, 'a-right': item.isRight }">
              答题结果：<span v-if="item.isRight">正确</span><span v-else>错误</span>
            </div>
          </div>
        </div>

        <!-- FILL/ESSAY 主观题 -->
        <div v-if="item.quType === 'fill' || item.quType === 'essay'">
          <div class="subjective-answer">
            <div class="subjective-label">我的回答：</div>
            <div v-if="!item.answered" class="subjective-text !text-[#999]">未作答</div>
            <div v-else class="subjective-text">{{ item.answerText }}</div>
          </div>

          <!-- 标准答案 -->
          <div class="subjective-answer">
            <div class="subjective-label">参考答案：</div>
            <div class="subjective-text !text-[#24da70]">
              <template v-for="an in item.answerList">
                <template v-if="an.isRight">{{ an.content }}</template>
              </template>
            </div>
          </div>

          <!-- 管理端人工阅卷入口 -->
          <div v-if="isAdminMode" class="review-box">
            <el-input-number
              v-model="reviewScores[item.quId]"
              :max="Number(item.score)"
              :min="0"
              :precision="1"
              :step="0.5"
              class="!w-140px"
            />
            <el-button
              type="primary"
              size="small"
              @click="doReview(item, reviewScores[item.quId])"
              >评分</el-button
            >
            <span class="review-tip">满分 {{ item.score }} 分</span>
          </div>
        </div>
      </div>
    </el-card>
  </ContentWrap>
</template>

<script lang="ts" setup>
import { ContentWrap } from '@/components/ContentWrap'
import { computed, ref, watch } from 'vue'
import { cheatListApi, paperFullDetailApi, reviewEssayApi } from '@/api/modules/exam/paper'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { sanitizeHtml } from '@/utils/sanitize'

const route = useRoute()
// 管理端进入（带 mode=full 且非学员端来源）→ 显示阅卷入口
const isAdminMode = ref(!!route.query.isAdmin)

const props = withDefaults(defineProps<{ paperId?: string }>(), { paperId: '' })

const paperData = ref<any>({ quList: [] })
const reviewScores = ref<Record<string, number>>({})
const cheatLogs = ref<any[]>([])

// 违规汇总（管理端可见全量）
const cheatSummary = computed(() => {
  const logs = cheatLogs.value || []
  return {
    total: logs.length,
    cutScreen: logs.filter((l) => l.cheatType === 'CUT_SCREEN').length,
    ipChange: logs.filter((l) => l.cheatType === 'IP_CHANGE').length,
    fastAnswer: logs.filter((l) => l.cheatType === 'FAST_ANSWER').length
  }
})
const cheatLevel = computed(() => {
  const n = cheatSummary.value.cutScreen
  if (n >= 5) return { type: 'error', label: '严重' }
  if (n >= 3) return { type: 'warning', label: '警示' }
  if (cheatSummary.value.total > 0) return { type: 'info', label: '提示' }
  return { type: 'success', label: '正常' }
})

const paperDetail = (id: string) => {
  paperFullDetailApi({ id }).then((res) => {
    paperData.value = res.data
    // 初始化阅卷分数为当前实际得分
    for (const item of paperData.value.quList || []) {
      if (item.quType === 'essay') {
        reviewScores.value[item.quId] = Number(item.actualScore || 0)
      }
    }
  })
  // 查询违规记录（管理端）
  cheatListApi({ paperId: id }).then((res) => {
    cheatLogs.value = res.data || []
  })
}

// 人工阅卷评分
const doReview = (item: any, score: number) => {
  if (score === undefined || score === null) {
    ElMessage.warning('请输入评分')
    return
  }
  reviewEssayApi({ paperId: props.paperId, quId: item.quId, actualScore: score }).then(() => {
    ElMessage.success('评分成功')
    paperDetail(props.paperId)
  })
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
.qu-content {
  border-bottom: #eee 1px solid;
  padding-bottom: 10px;
  margin-bottom: 30px;
}

.qu-content div {
  line-height: 24px;
}

.answer-item {
  border: 1px solid #e0e0e0;
  border-radius: 5px;
  padding: 8px 10px;
  margin-bottom: 10px;
}

.answer-item.is-right {
  border-color: #24da70;
}

.answer-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.answer-item.is-checked {
  font-weight: bold;
}

.score-text {
  color: #e6a23c;
  font-weight: bold;
}

.right-mark {
  color: #24da70;
  font-weight: bold;
}

.content-p :deep(p) {
  margin-top: 0;
  margin-bottom: 0;
}

.a-right {
  color: #24da70;
}

.a-wrong {
  color: #ff0000;
}

.subjective-answer {
  margin-top: 8px;
  padding: 8px 12px;
  background: #f8f9fb;
  border-radius: 5px;
}

.subjective-label {
  font-weight: bold;
  color: #606266;
  margin-bottom: 4px;
}

.subjective-text {
  white-space: pre-wrap;
  word-break: break-all;
  line-height: 1.6;
}

.review-box {
  margin-top: 10px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.review-tip {
  color: #999;
  font-size: 13px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>

