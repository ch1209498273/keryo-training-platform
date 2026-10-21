<template>
  <el-dialog
    v-model="dialogVisible"
    :before-close="handleClose"
    align-center
    title="试题管理"
    width="80%"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="试题题型" prop="quType">
            <DictListSelect v-model="form.quType" :disabled="!!form.id" dic-code="qu_type" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="难度等级" prop="difficultyLevel">
            <DictListSelect v-model="form.difficultyLevel" dic-code="qu_difficulty_level" />
          </el-form-item>
        </el-col>

        <el-col :span="12">
          <el-form-item label="所属题库" prop="repoId">
            <RepoSelect v-model="form.repoId" />
          </el-form-item>
        </el-col>

        <el-col :span="24">
          <el-form-item label="试题题干" prop="content">
            <Editor ref="editorRef" v-model="form.content" height="100px" />
          </el-form-item>
        </el-col>

        <el-col :span="24">
          <el-form-item label="试题解析" prop="analysis">
            <Editor ref="analysisRef" v-model="form.analysis" height="100px" />
          </el-form-item>
        </el-col>
      </el-row>

      <el-col v-if="form.quType" :span="24">
        <el-divider />

        <!-- 选择 / 判断类：选项 -->
        <template v-if="isChoice">
          <div class="!pb-10px">
            <el-button type="primary" @click="addAnswer">添加选项</el-button>
          </div>
          <el-table :data="form.answerList" border class="!w-full">
            <el-table-column label="序号" width="180">
              <template #default="{ $index }">第{{ $index + 1 }}项</template>
            </el-table-column>
            <el-table-column align="center" label="是否答案" width="180">
              <template #default="{ row, $index }">
                <el-checkbox
                  v-model="row.isRight"
                  label="答案"
                  @change="checkChange($event, $index)"
                />
              </template>
            </el-table-column>
            <el-table-column label="答案内容">
              <template #default="{ row }">
                <el-input v-model="row.content" placeholder="输入选项内容" />
              </template>
            </el-table-column>
            <el-table-column align="center" label="操作项" width="180px">
              <template #default="{ $index }">
                <el-button :icon="Delete" circle type="danger" @click="removeAnswer($index)" />
              </template>
            </el-table-column>
          </el-table>
        </template>

        <!-- 填空类：逐空答案 -->
        <template v-else-if="isFill">
          <div class="!pb-10px">
            <el-button type="primary" @click="addAnswer">添加空</el-button>
          </div>
          <el-table :data="form.answerList" border class="!w-full">
            <el-table-column label="空位" width="180">
              <template #default="{ $index }">第{{ $index + 1 }}空</template>
            </el-table-column>
            <el-table-column label="正确答案">
              <template #default="{ row }">
                <el-input v-model="row.content" placeholder="输入本空正确答案" />
              </template>
            </el-table-column>
            <el-table-column align="center" label="操作项" width="180px">
              <template #default="{ $index }">
                <el-button :icon="Delete" circle type="danger" @click="removeAnswer($index)" />
              </template>
            </el-table-column>
          </el-table>
          <div class="tip">题干中可用 ____ 表示填空位置，空的数量与上述一致即可。</div>
        </template>

        <!-- 简答类：参考答案 -->
        <template v-else-if="isEssay">
          <el-form-item label="参考答案">
            <div class="ai-row">
              <el-input
                v-model="form.answerList[0].content"
                type="textarea"
                :rows="4"
                placeholder="输入参考答案（学员练习时展示对照）"
              />
              <el-button :loading="aiAnswerLoading" type="primary" plain @click="aiGenerateAnswer">AI 生成</el-button>
            </div>
          </el-form-item>
        </template>
      </el-col>
    </el-form>

    <template #footer>
      <span class="dialog-footer">
        <el-button :loading="aiQuLoading" @click="aiGenerateQu">AI 出题</el-button>
        <el-button @click="handleClose">取消</el-button>
        <el-button :loading="loading" type="primary" @click="handleSave(formRef)">保存</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script lang="ts" setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { DictListSelect } from '@/components/DictListSelect'
import { QuDataType } from '@/views/Exam/Repo/types'
import { ElMessage, ElMessageBox, FormInstance, FormRules } from 'element-plus'
import RepoSelect from '@/views/Exam/Repo/components/RepoSelect.vue'
import { detailApi, saveApi } from '@/api/modules/exam/qu'
import Editor from '@/components/Editor/src/Editor.vue'
import { Delete } from '@element-plus/icons-vue'
import request from '@/config/axios'

const dialogVisible = ref(false)
const aiAnswerLoading = ref(false)
const aiQuLoading = ref(false)

// AI 生成参考答案（简答/填空）
const aiGenerateAnswer = async () => {
  if (!form.value.content?.trim()) {
    ElMessage.warning('请先填写题干')
    return
  }
  aiAnswerLoading.value = true
  try {
    const r: any = await request.post({
      url: '/api/exam/repo/qu/ai/generate-answer',
      data: { quType: form.value.quType, content: form.value.content }
    })
    if (r.code === 0 && r.data) {
      if (isEssay.value) {
        if (!form.value.answerList?.length) {
          form.value.answerList = [{ content: '', isRight: true }]
        }
        form.value.answerList[0].content = r.data
      } else if (isFill.value) {
        const blanks = String(r.data).split('|').filter(Boolean)
        form.value.answerList = blanks.map((t: string) => ({ content: t.trim(), isRight: true }))
      } else {
        ElMessage.info('AI 参考答案：' + r.data)
      }
      ElMessage.success('AI 已生成参考答案')
    } else {
      ElMessage.error(r.msg || '生成失败')
    }
  } catch (e: any) {
    ElMessage.error('AI 生成失败：' + (e?.message || '请稍后重试'))
  } finally {
    aiAnswerLoading.value = false
  }
}

// AI 出题（按主题生成并填充表单）
const aiGenerateQu = async () => {
  try {
    const { value: topic } = await ElMessageBox.prompt('输入出题主题（如：血液透析抗凝管理）', 'AI 出题', {
      confirmButtonText: '生成',
      cancelButtonText: '取消',
      inputPlaceholder: '主题'
    })
    if (!topic?.trim()) return
    aiQuLoading.value = true
    const r: any = await request.post({
      url: '/api/exam/repo/qu/ai/generate-qu',
      data: { topic: topic.trim(), quType: form.value.quType, difficulty: form.value.difficultyLevel }
    })
    if (r.code === 0 && r.data) {
      const d = r.data
      form.value.content = d.content
      form.value.analysis = d.analysis
      form.value.answerList = (d.answerList || []).map((a: any) => ({ content: a.content, isRight: a.isRight }))
      ElMessage.success('AI 出题完成，请核对后保存')
    } else {
      ElMessage.error(r.msg || '出题失败')
    }
  } catch (e: any) {
    if (e !== 'cancel') {
      ElMessage.error('AI 出题失败：' + (e?.message || '请稍后重试'))
    }
  } finally {
    aiQuLoading.value = false
  }
}

// 组件参数
const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  repoId: {
    type: String,
    default: ''
  },
  quId: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['update:visible', 'saved'])
const loading = ref(false)

const isChoice = computed(() => ['radio', 'multi', 'judge'].includes(form.value.quType || ''))
const isFill = computed(() => form.value.quType === 'fill')
const isEssay = computed(() => form.value.quType === 'essay')

const form = ref<QuDataType>({
  answerList: []
})
const formRef = ref<FormInstance>()
const rules = reactive<FormRules>({
  quType: [
    {
      required: true,
      message: '题型不能为空',
      trigger: 'blur'
    }
  ],
  repoId: [
    {
      required: true,
      message: '题库必须选择！',
      trigger: 'blur'
    }
  ],
  content: [
    {
      required: true,
      message: '题干必须填写！',
      trigger: 'blur'
    }
  ]
})

// 监听数据变化
watch(
  () => props.visible,
  (val) => {
    dialogVisible.value = val
  }
)

// 监听数据变化
watch(
  () => props.quId,
  (val) => {
    if (val) {
      loadData(val)
    } else {
        form.value = { answerList: [] }
    }
  }
)

watch(
  () => props.repoId,
  (val) => {
    form.value.repoId = val
  }
)

watch(
  () => form.value.quType,
  (val) => {
    if (val) {
      autoFill(val)
    }
  }
)

const handleClose = () => {
  dialogVisible.value = false
  emit('update:visible', false)
}

// 保存题目
const handleSave = async (formEl: FormInstance | undefined) => {
  if (!formEl) return
  await formEl?.validate(async (isValid) => {
    if (isValid && checkItems()) {
      loading.value = true
      saveApi(form.value)
        .then(() => {
          ElMessage({
            message: '题目添加成功！',
            type: 'success'
          })

          dialogVisible.value = false
          emit('update:visible', false)
          emit('saved')
          loading.value = false

          // 重置表单，保留题库
          formEl?.resetFields()
          form.value.repoId = props.repoId
        })
        .catch(() => {
          loading.value = false
        })
    }
  })
}

// 加载详情
const loadData = (repoId: string) => {
  detailApi({ id: repoId }).then(({ data }) => {
    form.value = data
  })
}

// 添加一个选项 / 空
const addAnswer = () => {
  if (!form.value.answerList) {
    form.value.answerList = []
  }

  const isText = isFill.value || isEssay.value
  form.value.answerList.push({ content: '', isRight: isText })
}

// 移除选项
const removeAnswer = (i: number) => {
  form.value.answerList?.splice(i, 1)
}

// 自动填充选项
const autoFill = (quType: string) => {
  if (form.value.id) {
    return
  }
  // 先清理
  form.value.answerList = []

  if (quType === 'radio' || quType === 'multi') {
    for (let i = 0; i < 3; i++) {
      form.value.answerList?.push({ content: '', isRight: false })
    }
  } else if (quType === 'judge') {
    form.value.answerList?.push({ content: '正确', isRight: false })
    form.value.answerList?.push({ content: '错误', isRight: false })
  } else if (quType === 'fill' || quType === 'essay') {
    // 填空 / 简答：答案恒为正确项
    form.value.answerList?.push({ content: '', isRight: true })
  }
}

const checkChange = (val: boolean, index: number) => {
  //
  console.log(val + '::' + index)

  if (!form.value.answerList || form.value.answerList.length === 0) {
    return
  }

  if (val && (form.value.quType === 'radio' || form.value.quType === 'judge')) {
    for (let i = 0; i < form.value.answerList.length; i++) {
      if (index !== i) {
        form.value.answerList[i].isRight = false
      }
    }
  }
}

const checkItems = () => {
  const list = form.value.answerList || []

  if (list.length === 0) {
    ElMessage({
      message: '答案不能为空！',
      type: 'error'
    })
    return false
  }

  if (isFill.value || isEssay.value) {
    for (const it of list) {
      if (!it.content || !it.content.trim()) {
        ElMessage({
          message: '请填写所有空的答案 / 参考答案！',
          type: 'error'
        })
        return false
      }
    }
    return true
  }

  let total = 0
  let checked = 0

  for (const it of list) {
    if (it.isRight) {
      checked += 1
    }
    total += 1
  }

  if (checked === 0) {
    ElMessage({
      message: '请至少选择一个选项作为正确答案！',
      type: 'error'
    })
    return false
  }

  return true
}

// 加载第一页数据
onMounted(() => {
  // 查询详情
  if (props.quId) {
    loadData(props.quId)
  }
})
</script>

<style scoped>
.tip {
  color: #909399;
  font-size: 13px;
  margin-top: 8px;
}
.ai-row {
  display: flex;
  gap: 8px;
  width: 100%;
  align-items: flex-start;
}
.ai-row .el-input {
  flex: 1;
}
</style>

