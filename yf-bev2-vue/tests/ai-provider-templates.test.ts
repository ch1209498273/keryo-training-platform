import assert from 'node:assert/strict'
import test from 'node:test'
import { AI_PROVIDER_TEMPLATES } from '../src/views/System/AiConfig/aiProviderTemplates.ts'

test('DeepSeek template uses the current endpoint and model id', () => {
  const deepseek = AI_PROVIDER_TEMPLATES.find((provider) => provider.value === 'deepseek')

  assert.ok(deepseek)
  assert.equal(deepseek.url, 'https://api.deepseek.com/chat/completions')
  assert.equal(deepseek.model, 'deepseek-v4-flash')
})

