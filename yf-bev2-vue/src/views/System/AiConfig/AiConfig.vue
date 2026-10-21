<template>
  <ContentWrap>
    <el-card class="!mb-15px">
      <template #header>
        <div class="flex items-center justify-between">
          <span>AI 服务配置</span>
          <el-tag v-if="hasKey" type="success" size="small">已配置 API Key</el-tag>
          <el-tag v-else type="warning" size="small">未配置 API Key</el-tag>
        </div>
      </template>

      <el-form label-width="130px" style="max-width: 720px">
        <el-form-item label="服务商">
          <el-select v-model="form.provider" style="width: 100%" @change="applyTemplate">
            <el-option v-for="p in providers" :key="p.value" :label="p.label" :value="p.value" />
          </el-select>
        </el-form-item>

        <el-form-item label="接口地址">
          <el-input v-model="form.baseUrl" placeholder="OpenAI 兼容的 chat/completions 接口地址" />
          <div class="!text-12px !text-[#999]">所有主流大模型均兼容 OpenAI 协议：DeepSeek / 通义千问 / OpenAI / Kimi / 智谱</div>
        </el-form-item>

        <el-form-item label="模型名">
          <el-input v-model="form.model" placeholder="如 deepseek-v4-flash / qwen-plus / gpt-4o-mini" />
        </el-form-item>

        <el-form-item label="API Key">
          <el-input
            v-model="form.apiKey"
            type="password"
            show-password
            :placeholder="hasKey ? '已配置（' + apiKeyMasked + '），留空则保持不变' : '请输入 API Key'"
            autocomplete="off"
          />
          <div class="!text-12px !text-[#999]">Key 仅保存在本系统数据库中，不会完整回显</div>
        </el-form-item>

        <el-form-item label="启用">
          <el-switch v-model="form.enabled" :active-value="1" :inactive-value="0" active-text="启用" inactive-text="停用" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="saving" @click="handleSave">保存配置</el-button>
          <el-button :loading="testing" @click="handleTest">测试连接</el-button>
          <el-alert
            v-if="testResult"
            :closable="false"
            :type="testResult.ok ? 'success' : 'error'"
            :title="testResult.ok ? '连接成功：' + testResult.reply : '连接失败'"
            class="!mt-10px"
          />
        </el-form-item>
      </el-form>
    </el-card>
  </ContentWrap>
</template>

<script lang="ts" setup>
import { ContentWrap } from '@/components/ContentWrap'
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/config/axios'
import { AI_PROVIDER_TEMPLATES } from './aiProviderTemplates'

const providers = AI_PROVIDER_TEMPLATES

const form = ref<any>({ provider: 'deepseek', baseUrl: '', model: '', apiKey: '', enabled: 1 })
const hasKey = ref(false)
const apiKeyMasked = ref('')
const saving = ref(false)
const testing = ref(false)
const testResult = ref<any>(null)

const applyTemplate = () => {
  const p = providers.find((x) => x.value === form.value.provider)
  if (p) {
    form.value.baseUrl = p.url
    form.value.model = p.model
  }
}

const load = async () => {
  const r: any = await request.post({ url: '/api/keryo/ai-config/view' })
  const d = r.data || {}
  form.value = {
    provider: d.provider || 'custom',
    baseUrl: d.baseUrl || '',
    model: d.model || '',
    apiKey: '',
    enabled: d.enabled ?? 1
  }
  hasKey.value = !!d.hasKey
  apiKeyMasked.value = d.apiKeyMasked || ''
}

const handleSave = async () => {
  if (!form.value.baseUrl) {
    ElMessage.warning('接口地址不能为空')
    return
  }
  if (!form.value.model) {
    ElMessage.warning('模型名不能为空')
    return
  }
  saving.value = true
  try {
    const r: any = await request.post({ url: '/api/keryo/ai-config/save', data: form.value })
    if (r.code === 0) {
      ElMessage.success('AI 配置已保存')
      form.value.apiKey = ''
      testResult.value = null
      await load()
    }
  } finally {
    saving.value = false
  }
}

const handleTest = async () => {
  testing.value = true
  testResult.value = null
  try {
    const r: any = await request.post({ url: '/api/keryo/ai-config/test' })
    testResult.value = r.code === 0 ? r.data : { ok: false }
  } catch (e: any) {
    testResult.value = { ok: false }
  } finally {
    testing.value = false
  }
}

onMounted(() => {
  load()
})
</script>

