<template>
  <ContentWrap>
    <div class="mb-10px flex items-center">
      <h3 class="!m-0 !mr-20px">培训计划管理</h3>
      <el-button type="primary" @click="openEdit()">添加计划</el-button>
    </div>

    <!-- 状态筛选 -->
    <el-radio-group v-model="query.params.status" class="!mb-15px" @change="fetchList">
      <el-radio-button value="">全部</el-radio-button>
      <el-radio-button value="DRAFT">草稿</el-radio-button>
      <el-radio-button value="PENDING">待审批</el-radio-button>
      <el-radio-button value="ACTIVE">进行中</el-radio-button>
      <el-radio-button value="ENDED">已结束</el-radio-button>
      <el-radio-button value="ARCHIVED">已归档</el-radio-button>
    </el-radio-group>

    <el-table v-loading="loading" :data="list" border>
      <el-table-column label="计划名称" prop="title" min-width="180" />
      <el-table-column label="关联培训" min-width="160">
        <template #default="{ row }">{{ trainingTitle(row.coursewareId) }}</template>
      </el-table-column>
      <el-table-column label="关联考试ID" prop="examId" width="180" show-overflow-tooltip />
      <el-table-column label="开始时间" prop="startTime" width="150" />
      <el-table-column label="结束时间" prop="endTime" width="150" />
      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag v-if="row.status === 'DRAFT'" type="info">草稿</el-tag>
          <el-tag v-else-if="row.status === 'PENDING'" type="warning">待审批</el-tag>
          <el-tag v-else-if="row.status === 'ACTIVE'" type="success">进行中</el-tag>
          <el-tag v-else-if="row.status === 'ENDED'" type="danger">已结束</el-tag>
          <el-tag v-else type="primary">已归档</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="360" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.status === 'DRAFT'" size="small" @click="openEdit(row)">编辑</el-button>
          <el-button v-if="row.status === 'DRAFT'" size="small" type="primary" @click="submitPlan(row)">提交审批</el-button>
          <el-button v-if="row.status === 'PENDING'" size="small" type="success" @click="approvePlan(row)">通过</el-button>
          <el-button v-if="row.status === 'PENDING'" size="small" type="danger" @click="rejectPlan(row)">驳回</el-button>
          <el-button v-if="['ACTIVE', 'ENDED'].includes(row.status)" size="small" type="warning" plain @click="openAssign(row)">分配学员</el-button>
          <el-button v-if="['ACTIVE', 'ENDED'].includes(row.status)" size="small" @click="archivePlan(row)">归档</el-button>
          <el-button v-if="row.status === 'DRAFT'" size="small" type="danger" plain @click="delPlan(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-model:current-page="query.current"
      v-model:page-size="query.size"
      :total="total"
      layout="total, prev, pager, next"
      @current-change="fetchList"
      style="margin-top: 20px; justify-content: flex-end"
    />

    <!-- 添加/编辑弹窗 -->
    <el-dialog v-model="showEdit" :title="editId ? '编辑培训计划' : '添加培训计划'" width="560px">
      <el-form ref="formRef" :model="editForm" label-width="100px" :rules="rules">
        <el-form-item label="计划名称" prop="title">
          <el-input v-model="editForm.title" placeholder="如：血液透析院感防控培训（第3期）" />
        </el-form-item>
        <el-form-item label="关联培训">
          <el-select v-model="editForm.coursewareId" placeholder="选择培训内容（学员学习用）" clearable style="width: 100%">
            <el-option v-for="t in trainingList" :key="t.id" :label="t.title" :value="t.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="关联考试">
          <el-select v-model="editForm.examId" placeholder="选择考核的考试" clearable style="width: 100%">
            <el-option v-for="e in examList" :key="e.id" :label="e.title" :value="e.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="适用科室">
          <el-select v-model="editForm.deptCode" placeholder="留空=全院" clearable style="width: 100%">
            <el-option v-for="d in deptList" :key="d.deptCode" :label="d.deptName" :value="d.deptCode" />
          </el-select>
        </el-form-item>
        <el-form-item label="开始时间" prop="startTime">
          <el-date-picker v-model="editForm.startTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" />
        </el-form-item>
        <el-form-item label="结束时间" prop="endTime">
          <el-date-picker v-model="editForm.endTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" />
        </el-form-item>
        <el-form-item label="培训说明">
          <el-input v-model="editForm.content" type="textarea" :rows="3" placeholder="培训内容说明" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEdit = false">取消</el-button>
        <el-button :loading="saving" type="primary" @click="savePlan">保存</el-button>
      </template>
    </el-dialog>

    <!-- 学员分配弹窗（T-13b） -->
    <el-dialog v-model="showAssign" :title="`分配学员 · ${assignPlan?.title || ''}`" width="640px">
      <el-row :gutter="12">
        <el-col :span="12">
          <h4 class="!m-0 !mb-10px">已参与学员</h4>
          <el-table v-loading="assignLoading" :data="participants" border max-height="320">
            <el-table-column prop="realName" label="姓名" width="110" />
            <el-table-column prop="userName" label="账号" width="120" />
            <el-table-column label="操作" width="90">
              <template #default="{ row }">
                <el-button size="small" type="danger" plain @click="removeUser(row.userId)">移除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-col>
        <el-col :span="12">
          <h4 class="!m-0 !mb-10px">待分配学员</h4>
          <el-table :data="assignCandidates()" border max-height="320">
            <el-table-column prop="realName" label="姓名" width="110" />
            <el-table-column prop="userName" label="账号" width="120" />
            <el-table-column label="操作" width="90">
              <template #default="{ row }">
                <el-button size="small" type="primary" @click="doAssign(row.id)">分配</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-col>
      </el-row>
      <template #footer>
        <el-button @click="showAssign = false">关闭</el-button>
      </template>
    </el-dialog>
  </ContentWrap>
</template>

<script lang="ts" setup>
import { ContentWrap } from '@/components/ContentWrap'
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, FormInstance, FormRules } from 'element-plus'
import request from '@/config/axios'

const loading = ref(false)
const saving = ref(false)
const list = ref<any[]>([])
const total = ref(0)
const showEdit = ref(false)
const editId = ref('')
const examList = ref<any[]>([])
const deptList = ref<any[]>([])
const trainingList = ref<any[]>([])

// 学员分配（T-13b）
const showAssign = ref(false)
const assignPlan = ref<any>(null)
const participants = ref<any[]>([])
const allUsers = ref<any[]>([])
const assignLoading = ref(false)

const query = reactive({ current: 1, size: 10, params: { status: '' } })
const editForm = reactive<any>({})
const formRef = ref<FormInstance>()
const rules = reactive<FormRules>({
  title: [{ required: true, message: '请填写计划名称', trigger: 'blur' }]
})

const fetchList = async () => {
  loading.value = true
  try {
    const r: any = await request.post({ url: '/api/keryo/training-plan/paging', data: query })
    list.value = r.data?.records || []
    total.value = r.data?.total || 0
  } finally {
    loading.value = false
  }
}

const loadExams = async () => {
  try {
    const r: any = await request.post({ url: '/api/exam/exam/exam/paging', data: { current: 1, size: 100 } })
    examList.value = r.data?.records || []
  } catch {
    examList.value = []
  }
}

const loadDepts = async () => {
  try {
    const r: any = await request.post({ url: '/api/sys/depart/tree-select', data: {} })
    deptList.value = r.data || []
  } catch {
    deptList.value = []
  }
}

const loadTrainings = async () => {
  try {
    const r: any = await request.post({
      url: '/api/keryo/training/paging',
      data: { current: 1, size: 100, params: {} }
    })
    trainingList.value = r.data?.records || []
  } catch {
    trainingList.value = []
  }
}

const trainingTitle = (id: string) => {
  const t = trainingList.value.find((x) => x.id === id)
  return t ? t.title : (id || '-')
}

const openEdit = (row?: any) => {
  editId.value = row?.id || ''
  editForm.value = row
    ? { ...row }
    : { title: '', examId: '', coursewareId: '', deptCode: '', startTime: '', endTime: '', content: '' }
  showEdit.value = true
}

const savePlan = async () => {
  await formRef.value?.validate(async (ok) => {
    if (!ok) return
    saving.value = true
    try {
      const r: any = await request.post({ url: '/api/keryo/training-plan/save', data: editForm.value })
      if (r.code === 0) {
        ElMessage.success('保存成功')
        showEdit.value = false
        fetchList()
      } else {
        ElMessage.error(r.msg || '保存失败')
      }
    } finally {
      saving.value = false
    }
  })
}

const submitPlan = async (row: any) => {
  const r: any = await request.post({ url: '/api/keryo/training-plan/submit', data: { id: row.id } })
  if (r.code === 0) {
    ElMessage.success('已提交审批')
    fetchList()
  } else {
    ElMessage.error(r.msg || '提交失败')
  }
}

const approvePlan = async (row: any) => {
  const { value } = await ElMessageBox.prompt('审批意见（可留空）', '审批通过', {
    confirmButtonText: '通过',
    cancelButtonText: '取消'
  })
  const r: any = await request.post({ url: '/api/keryo/training-plan/approve', data: { id: row.id, remark: value || '' } })
  if (r.code === 0) {
    ElMessage.success('已通过，计划生效')
    fetchList()
  } else {
    ElMessage.error(r.msg || '操作失败')
  }
}

const rejectPlan = async (row: any) => {
  const { value } = await ElMessageBox.prompt('驳回原因（必填）', '审批驳回', {
    confirmButtonText: '驳回',
    cancelButtonText: '取消'
  })
  const r: any = await request.post({ url: '/api/keryo/training-plan/reject', data: { id: row.id, remark: value || '' } })
  if (r.code === 0) {
    ElMessage.success('已驳回，计划退回草稿')
    fetchList()
  } else {
    ElMessage.error(r.msg || '操作失败')
  }
}

const archivePlan = async (row: any) => {
  const r: any = await request.post({ url: '/api/keryo/training-plan/archive', data: { id: row.id } })
  if (r.code === 0) {
    ElMessage.success('已归档')
    fetchList()
  } else {
    ElMessage.error(r.msg || '操作失败')
  }
}

const delPlan = async (row: any) => {
  await ElMessageBox.confirm(`确认删除计划「${row.title}」？`, '提示', { type: 'warning' })
  const r: any = await request.post({ url: '/api/keryo/training-plan/delete', data: { id: row.id } })
  if (r.code === 0) {
    ElMessage.success('已删除')
    fetchList()
  }
}

// ===== T-13b 学员分配 =====
const openAssign = async (row: any) => {
  assignPlan.value = row
  participants.value = []
  showAssign.value = true
  await loadAssign(row.id)
}

const loadAssign = async (planId: string) => {
  assignLoading.value = true
  try {
    const [participantResponse, candidateResponse]: any[] = await Promise.all([
      request.post({ url: '/api/keryo/training-plan/participants', data: { planId } }),
      request.post({ url: '/api/keryo/training-plan/participant-candidates', data: { planId } })
    ])
    participants.value = participantResponse.data || []
    allUsers.value = candidateResponse.data || []
  } finally {
    assignLoading.value = false
  }
}

const assignCandidates = () => {
  const joined = new Set(participants.value.map((p) => p.userId))
  return allUsers.value.filter((u) => !joined.has(u.id))
}

const doAssign = async (uid: string) => {
  const r: any = await request.post({
    url: '/api/keryo/training-plan/assign',
    data: { planId: assignPlan.value.id, userIds: [uid] }
  })
  if (r.code === 0) {
    ElMessage.success('已分配')
    loadAssign(assignPlan.value.id)
  } else {
    ElMessage.error(r.msg || '分配失败')
  }
}

const removeUser = async (userId: string) => {
  await ElMessageBox.confirm('确认移除该学员？', '提示', { type: 'warning' })
  const r: any = await request.post({
    url: '/api/keryo/training-plan/remove-user',
    data: { planId: assignPlan.value.id, userId }
  })
  if (r.code === 0) {
    ElMessage.success('已移除')
    loadAssign(assignPlan.value.id)
  }
}

onMounted(() => {
  fetchList()
  loadExams()
  loadDepts()
  loadTrainings()
})
</script>

