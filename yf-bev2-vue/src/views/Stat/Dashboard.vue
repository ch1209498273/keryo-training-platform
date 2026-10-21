<template>
  <ContentWrap>
    <!-- 4 统计卡片 -->
    <el-row :gutter="16">
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-label">学员总数</div>
          <div class="stat-value">{{ data.studentCount ?? '—' }}</div>
          <div class="stat-desc">启用状态账号</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-label">已完成考试人次</div>
          <div class="stat-value">{{ data.completedCount ?? '—' }}</div>
          <div class="stat-desc">交卷结算成绩</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-label">平均分</div>
          <div class="stat-value">{{ data.avgScore ?? '—' }}</div>
          <div class="stat-desc">已结算成绩均值</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-label">本年度参训次数</div>
          <div class="stat-value">{{ data.annualTrainingCount ?? '—' }}</div>
          <div class="stat-desc">按培训计划参与关系统计</div>
        </div>
      </el-col>
    </el-row>

    <!-- 通过率折线 -->
    <div class="chart-wrap">
      <h3>各考试通过率</h3>
      <div v-if="!trendData.length" class="empty">暂无考试数据</div>
      <canvas v-else ref="chartRef"></canvas>
    </div>

    <!-- 参训名单 + 未完成 -->
    <el-row :gutter="16" style="margin-top: 20px">
      <el-col :span="12">
        <h3>参训名单（最近）</h3>
        <el-table :data="data.trainees || []" size="small" max-height="320">
          <el-table-column prop="realName" label="学员" width="90"></el-table-column>
          <el-table-column prop="deptName" label="科室"></el-table-column>
          <el-table-column prop="examTitle" label="考试" show-overflow-tooltip></el-table-column>
          <el-table-column prop="score" label="分数" width="80"></el-table-column>
          <el-table-column label="结果" width="70">
            <template #default="{ row }">
              <el-tag :type="row.passed === 1 ? 'success' : 'danger'" size="small">
                {{ row.passed === 1 ? '通过' : '未过' }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </el-col>
      <el-col :span="12">
        <h3>未完成名单</h3>
        <el-table :data="data.notFinished || []" size="small" max-height="320">
          <el-table-column prop="realName" label="学员" width="90"></el-table-column>
          <el-table-column prop="deptName" label="科室"></el-table-column>
          <el-table-column prop="examTitle" label="状态"></el-table-column>
        </el-table>
      </el-col>
    </el-row>
  </ContentWrap>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import request from '@/config/axios'

const data = ref<any>({})
const chartRef = ref<HTMLCanvasElement | null>(null)

const trendData = computed(() => data.value.passRateTrend || [])

function load() {
  request
    .post({ url: '/api/keryo/dashboard/overview', data: {} })
    .then((res: any) => {
      data.value = res || {}
      nextTick(drawChart)
    })
}

function drawChart() {
  // Canvas 手绘通过率折线
  const canvas = chartRef.value
  if (!canvas) return
  const ctx = canvas.getContext('2d')!
  const pts = trendData.value
  const W = (canvas.width = canvas.clientWidth * 2)
  const H = (canvas.height = 300)
  ctx.clearRect(0, 0, W, H)
  if (!pts.length) return
  const pad = { l: 50, r: 30, t: 30, b: 40 }
  const iw = W - pad.l - pad.r
  const ih = H - pad.t - pad.b
  ctx.strokeStyle = '#eee'
  ctx.fillStyle = '#666'
  ctx.font = '12px sans-serif'
  for (let g = 0; g <= 4; g++) {
    const y = pad.t + ih - (ih * g) / 4
    ctx.beginPath()
    ctx.moveTo(pad.l, y)
    ctx.lineTo(W - pad.r, y)
    ctx.stroke()
    ctx.fillText(Math.round((100 * g) / 4) + '%', 8, y + 4)
  }
  ctx.strokeStyle = '#409eff'
  ctx.lineWidth = 2
  ctx.beginPath()
  pts.forEach((p: any, i: number) => {
    const x = pad.l + (iw * i) / Math.max(1, pts.length - 1)
    const y = pad.t + ih - (ih * (p.rate || 0)) / 100
    i === 0 ? ctx.moveTo(x, y) : ctx.lineTo(x, y)
  })
  ctx.stroke()
  pts.forEach((p: any, i: number) => {
    const x = pad.l + (iw * i) / Math.max(1, pts.length - 1)
    const y = pad.t + ih - (ih * (p.rate || 0)) / 100
    ctx.fillStyle = '#409eff'
    ctx.beginPath()
    ctx.arc(x, y, 4, 0, Math.PI * 2)
    ctx.fill()
    ctx.fillStyle = '#333'
    ctx.textAlign = 'center'
    ctx.fillText(p.name, x, H - 10)
    ctx.fillStyle = '#409eff'
    ctx.fillText((p.rate || 0) + '%', x, y - 10)
  })
}

onMounted(() => {
  load()
  const timer = setInterval(load, 5 * 60 * 1000)
  window.addEventListener('resize', drawChart)
  onUnmounted(() => {
    clearInterval(timer)
    window.removeEventListener('resize', drawChart)
  })
})
</script>

<style scoped>
.stat-card {
  background: linear-gradient(135deg, #f0f7ff, #e8f3ff);
  border-radius: 8px;
  padding: 20px;
  text-align: center;
}
.stat-label { color: #666; font-size: 14px; }
.stat-value { font-size: 32px; font-weight: 700; color: #409eff; margin: 8px 0; }
.stat-desc { color: #999; font-size: 12px; }
.chart-wrap { margin-top: 24px; }
.chart-wrap h3,
h3 { color: #333; font-size: 15px; margin: 8px 0 12px; }
.chart-wrap canvas { width: 100%; }
.empty { color: #999; text-align: center; padding: 40px 0; }
</style>

