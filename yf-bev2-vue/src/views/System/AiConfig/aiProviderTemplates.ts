export type AiProviderTemplate = {
  value: string
  label: string
  url: string
  model: string
}

/**
 * Provider templates used by the AI configuration page.
 * The backend sends requests to the full OpenAI-compatible chat/completions URL.
 */
export const AI_PROVIDER_TEMPLATES: AiProviderTemplate[] = [
  {
    value: 'deepseek',
    label: 'DeepSeek',
    url: 'https://api.deepseek.com/chat/completions',
    model: 'deepseek-v4-flash'
  },
  {
    value: 'qwen',
    label: '通义千问（阿里）',
    url: 'https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions',
    model: 'qwen-plus'
  },
  { value: 'openai', label: 'OpenAI', url: 'https://api.openai.com/v1/chat/completions', model: 'gpt-4o-mini' },
  { value: 'kimi', label: 'Kimi（月之暗面）', url: 'https://api.moonshot.cn/v1/chat/completions', model: 'moonshot-v1-8k' },
  { value: 'zhipu', label: '智谱 GLM', url: 'https://open.bigmodel.cn/api/paas/v4/chat/completions', model: 'glm-4-flash' },
  { value: 'custom', label: '自定义', url: '', model: '' }
]

